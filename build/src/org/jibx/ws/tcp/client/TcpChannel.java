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

package org.jibx.ws.tcp.client;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.codec.CodecCache;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.encoding.dime.DimeCommon;
import org.jibx.ws.encoding.dime.DimeInputBuffer;
import org.jibx.ws.encoding.dime.DimeOutputBuffer;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.transport.Channel;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.OutConnection;
import org.jibx.ws.transport.OutConnectionBase;
import org.jibx.ws.transport.SimpleDuplexConnection;

/**
 * A client connection implementing DIME message exchange over a TCP/IP socket connection.
 * 
 * @author Dennis M. Sosnoski
 */
public final class TcpChannel implements Channel
{
    private static final String TCP_LEAD = "tcp://";
    
    /** Endpoint address for setting source information when unmarshalling. */
    private final String m_endpoint;

    /** Actual socket used for all exchanges. */
    private Socket m_socket;
    
    /** DIME input buffer. */
    private DimeInputBuffer m_dimeInput;
    
    /** DIME output buffer. */
    private DimeOutputBuffer m_dimeOutput;
    
    /** Cache for codec instances. */
    private final CodecCache m_codecCache;
    
    /**
     * Constructor. This currently uses only the host name or address and the port number in the supplied endpoint
     * address. If the port number is followed by a path, the path is ignored. Passing the path information on to the
     * host may be useful in the future.
     * 
     * @param endpoint endpoint address
     * @throws WsConfigurationException if endpoint address invalid 
     */
    protected TcpChannel(String endpoint) throws WsConfigurationException {
        if (endpoint.toLowerCase().startsWith(TCP_LEAD)) {
            m_endpoint = endpoint;
            endpoint = endpoint.substring(TCP_LEAD.length());
            int split = endpoint.indexOf(':');
            if (split > 0) {
                String host = endpoint.substring(0, split);
                endpoint = endpoint.substring(split + 1);
                split = endpoint.indexOf('/');
                String port = null;
                if (split >= 0) {
                    port = endpoint.substring(0, split);
                } else {
                    port = endpoint;
                }
                try {
                    int portnum = Integer.parseInt(port);
                    m_socket = new Socket(host, portnum);
                    m_socket.setTcpNoDelay(true);
                } catch (NumberFormatException e) {
                    throw new WsConfigurationException("Error parsing port number for endpoint '" + endpoint + '\'', e);
                } catch (IOException e) {
                    throw new WsConfigurationException("Unable to create socket connection to endpoint '" + endpoint
                        + '\'', e);
                }
            } else {
                throw new WsConfigurationException("Missing port number in endpoint '" + endpoint + '\'');
            }
            m_codecCache = new CodecCache();
        } else {
            throw new IllegalArgumentException("Endpoint '" + endpoint + "' is not using the tcp protocol");
        }
    }
    
    /**
     * Setup output using the socket connection. This uses only the SEND_TYPE property from the supplied list (if
     * present).
     * 
     * @param msgProps message specific properties
     * @throws IOException 
     */
    private void setupOutput(MessageProperties msgProps) throws IOException {
        if (m_dimeOutput == null) {
            m_dimeOutput = new DimeOutputBuffer();
            OutByteBuffer obuff = new OutByteBuffer();
            m_dimeOutput.setBuffer(obuff);
            obuff.setOutput(m_socket.getOutputStream());
        }
        MediaType type = msgProps.getContentType();
        int typecode = type == null ? DimeCommon.TYPE_NONE : DimeCommon.TYPE_MEDIA;
        m_dimeOutput.nextMessage();
        m_dimeOutput.nextPart(null, typecode, type == null ? null : type.toString());
    }
    
    /** {@inheritDoc} */
    public InConnection getInbound() {
        return new TcpInConnection();
    }
    
    /** {@inheritDoc} */
    public OutConnection getOutbound(MessageProperties msgProps, XmlOptions xmlOptions) throws IOException { 
        setupOutput(msgProps);
        return new TcpOutConnection(msgProps, xmlOptions);
    }
    
    /** {@inheritDoc} */
    public DuplexConnection getDuplex(MessageProperties msgProps, XmlOptions xmlOptions) throws IOException {
        setupOutput(msgProps);
        return new SimpleDuplexConnection(new TcpInConnection(), new TcpOutConnection(msgProps, xmlOptions));
    }
    
    /** {@inheritDoc} */
    public void close() throws IOException {
        m_socket.close();
    }
    
    private class TcpInConnection implements InConnection
    {
        /** Message initialized at DIME transport layer flag. */
        private boolean m_initialized;
        
        /** Reader currently in use. */
        private IXMLReader m_reader;
        
        /**
         * Make sure the connection has been initialized before returning any information from the message.
         */
        private void checkInitialized() {
            if (!m_initialized) {
                throw new IllegalStateException("Internal error - connection not initialized");
            }
        }
        
        /** {@inheritDoc} */
        public void init() throws IOException {
            if (!m_initialized) {
                if (m_dimeInput == null) {
                    m_dimeInput = new DimeInputBuffer();
                    InByteBuffer ibuff = new InByteBuffer();
                    m_dimeInput.setBuffer(ibuff);
                    ibuff.setInput(m_socket.getInputStream());
                }
                if (m_dimeInput.nextMessage() && m_dimeInput.nextPart()) {
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
                    m_reader = m_codecCache.getCodec(mediaType).getReader(m_dimeInput, null, m_endpoint, false);
                    m_reader.init();
                    m_initialized = true;
                } else {
                    throw new IOException("No data present");
                }
            }
        }
        
        /** {@inheritDoc} */
        public String getCharacterEncoding() {
            checkInitialized();
            return m_reader.getInputEncoding();
        }
        
        /** {@inheritDoc} */
        public String getContentType() {
            checkInitialized();
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
            checkInitialized();
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
            checkInitialized();
            return m_reader;
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

        /** {@inheritDoc} */
        public void close() throws IOException {
            if (m_dimeInput != null) {
                m_dimeInput.finish();
            }
            m_reader = null;
        }
    }
    
    private class TcpOutConnection extends OutConnectionBase
    {
        /** Message formatting and media type options. */
        private final MessageProperties m_msgProps;
        
        /** Message initialized at DIME transport layer flag. */
        private boolean m_initialized;
        
        /** Writer currently in use. */
        private IXMLWriter m_writer;

        /**
         * Constructor. This just passes the configured output options on to the base class constructor.
         * @param msgProps message specific properties
         * @param xmlOptions XML formatting options
         */
        public TcpOutConnection(MessageProperties msgProps, XmlOptions xmlOptions) {
            super(xmlOptions);
            m_msgProps = msgProps;
        }

        /** {@inheritDoc} */
        public IXMLWriter getNormalWriter(String[] uris) throws IOException {
            if (!m_initialized) {
                
                // create the writer instance
                m_writer = m_codecCache.getCodec(m_msgProps.getContentType()).getWriter(m_dimeOutput, null, uris);
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
        public void outputComplete() {
        }

        /** {@inheritDoc} */
        public void close() throws IOException {
            if (m_initialized) {
                m_writer.flush();
                m_dimeOutput.endMessage();
                m_dimeOutput.flush();
            }
        }
    }
}
