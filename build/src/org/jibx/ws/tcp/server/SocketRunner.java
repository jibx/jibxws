/*
 * Copyright (c) 2007-2009, Sosnoski Software Associates Limited. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.ws.tcp.server;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.codec.CodecCache;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.codec.XmlCodec;
import org.jibx.ws.encoding.dime.DimeCommon;
import org.jibx.ws.encoding.dime.DimeInputBuffer;
import org.jibx.ws.encoding.dime.DimeOutputBuffer;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.protocol.Protocol;
import org.jibx.ws.protocol.ProtocolDirectory;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.server.ServicePool;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnectionBase;
import org.jibx.ws.transport.OutServerConnection;

/**
 * Handles the client connection to a service. Each service uses a unique port, and each client receives its own socket
 * when it connects to that port, so this just needs to implement the actual processing loop.
 * 
 * @author Dennis M. Sosnoski
 */
public class SocketRunner implements Runnable
{
    private static final Log s_logger = LogFactory.getLog(SocketRunner.class);
    
    /** Socket to be handled. */
    private final Socket m_socket;
    
    /** Service accessed by this socket. */
    private final ServiceDefinition m_sdef;
    
    /** Server which started this instance. */
    private final TcpServer m_server;
    
    /** Client address (used for identifying the source of the unmarshalled messages). */
    private final String m_clientAddress;
    
    /** Cache for codec instances. */
    private final CodecCache m_codecCache;
    
    /** Next runner in server list (<code>null</code> if none). */
    SocketRunner m_next;
    
    /** Preceding runner in server list (<code>null</code> if none). */
    SocketRunner m_last;
    
    /** DIME input buffer. */
    private DimeInputBuffer m_dimeInput;
    
    /** DIME output buffer. */
    private DimeOutputBuffer m_dimeOutput;
    
    /** Thread exit flag. */
    private boolean m_exit;
    
    /**
     * Constructor.
     * 
     * @param socket socket to be handled
     * @param sdef definition of service to be accessed by this socket
     * @param server server which started this instance
     * @throws IOException on error accessing input or output streams
     */
    public SocketRunner(Socket socket, ServiceDefinition sdef, TcpServer server) throws IOException {
        m_socket = socket;
        m_socket.setTcpNoDelay(true);
        m_sdef = sdef;
        m_server = server;
        m_clientAddress = socket.getInetAddress().getHostAddress();
        m_dimeInput = new DimeInputBuffer();
        InByteBuffer ibuff = new InByteBuffer();
        m_dimeInput.setBuffer(ibuff);
        ibuff.setInput(socket.getInputStream());
        m_dimeOutput = new DimeOutputBuffer();
        OutByteBuffer obuff = new OutByteBuffer();
        obuff.setOutput(socket.getOutputStream());
        m_dimeOutput.setBuffer(obuff);
        m_codecCache = new CodecCache();
    }
    
    /**
     * Set thread exit flag. This also terminates the socket connection if called with value <code>true</code>.
     * 
     * @param exit <code>true</code> if thread is to exit, <code>false</code> otherwise
     */
    public synchronized void setExit(boolean exit) {
        m_exit = exit;
        if (exit) {
            try {
                m_socket.close();
            } catch (IOException e) {
                // nothing to be done if this fails
            }
        }
    }
    
    /**
     * Thread execution method. The execution loop is simple, consisting of the thread reading messages from the
     * socket and running the associated service code to send the response. Execution continues until either the
     * socket is closed or the exit flag is set.
     */
    public void run() {
        try {
            while (!m_exit && m_dimeInput.nextMessage() && m_dimeInput.nextPart()) {
                Service serv = null;
                try {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Beginning processing of receive message from " + m_clientAddress);
                    }
                    MediaType mediaType = null;
                    if (m_dimeInput.getPartTypeCode() == DimeCommon.TYPE_MEDIA) {
                        String partTypeText = m_dimeInput.getPartTypeText();
                        try {
                            MediaType partMediaType = new MediaType(partTypeText);
                            if (CodecDirectory.hasCodecFor(partMediaType)) {
                                mediaType = partMediaType;
                            }
                        } catch (ParseException e) {
                            throw new IOException("Unable to parse media type '" + partTypeText + "'");
                        }
                    }
                    if (mediaType == null) {
                        mediaType = CodecDirectory.TEXT_XML_MEDIA_TYPE;
                    }
                    XmlCodec codec = m_codecCache.getCodec(mediaType);
                    IXMLReader reader = codec.getReader(m_dimeInput, null, m_clientAddress, false);
                    Protocol protocol = ProtocolDirectory.getProtocol(m_sdef.getProtocolName());
                    ServiceFactory serviceFactory = protocol.getServiceFactory();
                    serv = ServicePool.getInstance(serviceFactory, m_sdef);
                    serv.processRequest(new TcpInConnection(m_dimeInput, reader), new TcpOutConnection(codec, 
                        serv.getXmlOptions()));
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Completed processing of receive message from " + m_clientAddress);
                    }
                } finally {
                    if (serv != null) {
                        serv.releaseInstance();
                    }
                }
            }
        } catch (Exception e) {
            
            // just log the error and exit
            e.printStackTrace();
            s_logger.error("TCP service error with client " + m_clientAddress, e);
            
        } finally {
            
            // make sure the socket is closed
            try {
                m_socket.close();
            } catch (IOException e) {
                // nothing to be done if this fails
            }
            
            // unlink from service list
            m_server.unlink(this);
        }
    }
    
    /**
     * Inbound connection (data received from client).
     */
    private static class TcpInConnection implements InConnection
    {
        /** DIME input buffer. */
        private final DimeInputBuffer m_dimeInput;
        
        /** XML reader instance. */
        private final IXMLReader m_reader;
        
        /**
         * Constructor.
         * 
         * @param dimein
         * @param reader
         */
        public TcpInConnection(DimeInputBuffer dimein, IXMLReader reader) {
            m_dimeInput = dimein;
            m_reader = reader;
        }

        /** {@inheritDoc} */
        public String getCharacterEncoding() {
            return m_reader.getInputEncoding();
        }
        
        /** {@inheritDoc} */
        public String getContentType() {
            if (m_dimeInput.getPartTypeCode() == DimeCommon.TYPE_MEDIA) {
                return m_dimeInput.getPartTypeText();
            } else {
                return null;
            }
        }
        
        /** {@inheritDoc} */
        public String getDestination() {
            return null;
        }
        
        /** {@inheritDoc} */
        public String getId() {
            return m_dimeInput.getPartIdentifier();
        }
        
        /** {@inheritDoc} */
        public String getOperationName() {
            return null;
        }
        
        /** {@inheritDoc} */
        public String getOrigin() {
            return null;
        }
        
        /** {@inheritDoc} */
        public String getProperty(String name) {
            return null;
        }
        
        /** {@inheritDoc} */
        public IXMLReader getReader() {
            return m_reader;
        }
        
        /** {@inheritDoc} */
        public void init() throws IOException {
            m_reader.init();
        }

        /** {@inheritDoc} */
        public void close() throws IOException {
            m_dimeInput.finish();
        }

        /** {@inheritDoc} */
        public boolean hasError() throws IOException {
            return false;
        }

        /** {@inheritDoc} */
        public String getErrorMessage() throws IOException {
            return null;
        }

        /** {@inheritDoc} */
        public void inputComplete() {
        }
    }
    
    /**
     * Outbound connection (data sent to client).
     */
    private class TcpOutConnection extends OutConnectionBase implements OutServerConnection
    {
        /** Codec used for response to client. */
        private final XmlCodec m_codec;
        
        /** XML writer instance. */
        private IXMLWriter m_writer;
        
        /** Message initialized at DIME transport layer flag. */
        private boolean m_initialized;
        
        /**
         * Constructor.
         * 
         * @param codec
         * @param xmlOptions formatting options for XML
         */
        public TcpOutConnection(XmlCodec codec, XmlOptions xmlOptions) {
            super(xmlOptions);
            m_codec = codec;
        }

        /**
         * {@inheritDoc}
         */
        public IXMLWriter getNormalWriter(String[] uris) throws IOException {
            if (!m_initialized) {
                
                // initialize DIME output first, then hook to writer, so data offset will be past header
                MediaType mediaType = m_codec.getMediaType();
                int typecode = mediaType == null ? DimeCommon.TYPE_NONE : DimeCommon.TYPE_MEDIA;
                m_dimeOutput.nextMessage();
                m_dimeOutput.nextPart(null, typecode, mediaType.toString());
                m_writer = m_codec.getWriter(m_dimeOutput, null, uris);
                initializeWriter(m_writer);
                m_initialized = true;
                
            }
            return m_writer;
        }
        
        /** {@inheritDoc} */
        public IXMLWriter getFaultWriter(String[] uris) throws IOException {
            return getNormalWriter(uris);
        }
        
        /** {@inheritDoc} */
        public void close() throws IOException {
            if (m_initialized) {
                m_writer.flush();
                m_dimeOutput.endMessage();
                m_dimeOutput.flush();
            }
            m_initialized = false;
        }
        
        /** {@inheritDoc} */
        public boolean isCommitted() {
            return true;
        }
        
        /** {@inheritDoc} */
        public void sendNotFoundError() throws IOException {
        }
        
        /** {@inheritDoc} */
        public void setInternalServerError() {
        }

        /** {@inheritDoc} */
        public void outputComplete() {
        }
    }
}
