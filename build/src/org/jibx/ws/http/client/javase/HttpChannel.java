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

package org.jibx.ws.http.client.javase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.codec.CodecCache;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.transport.Channel;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.OutConnection;
import org.jibx.ws.transport.OutConnectionBase;
import org.jibx.ws.transport.SimpleDuplexConnection;
import org.jibx.ws.transport.StreamBufferInPool;
import org.jibx.ws.transport.StreamBufferOutPool;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;

/**
 * Channel for communicating with an HTTP endpoint. The methods exposed by this class are not threadsafe, so
 * synchronization must be used if the channel is shared between threads.
 * 
 * @author Dennis M. Sosnoski
 */
public final class HttpChannel implements Channel
{
    /** Key string for property defining the content type of the message being sent. */
    private static final String CONTENT_TYPE = "Content-Type";

    /** The key of the character set parameter, which can be used as a parameter to the CONTENT_TYPE header field. */  
    private static final String CHARSET_KEY = "charset";

    /** The key of the action parameter, which can be used as part of the CONTENT_TYPE parameter. */  
    private static final String ACTION_KEY = "action";

    /** Key string for property defining the content type(s) to be accepted for a response message. */
    private static final String ACCEPT_TYPE = "Accept";

    
    // TODO: make this a configuration parameter
    private static final int BUFFER_SIZE = 8192;
    
    /** URL for HTTP endpoint. */
    private final URL m_url;
    
    /** Cache for codec instances. */
    private final CodecCache m_codecCache;
    
    /** Input byte buffer pool. */
    private final StreamBufferInPool m_inBufferCache;
    
    /** Output byte buffer pool. */
    private final StreamBufferOutPool m_outBufferCache;
    
    private HttpTransportOptions m_transportOptions;
    
    /**
     * Constructor.
     * 
     * @param url the target location
     * @param transportOptions options for customizing the transport. For HttpChannel, this must be an object of type 
     * {@link HttpTransportOptions}.
     */
    public HttpChannel(URL url, HttpTransportOptions transportOptions) {
        m_url = url;
        m_transportOptions = transportOptions;
        m_codecCache = new CodecCache();
        m_inBufferCache = new StreamBufferInPool(BUFFER_SIZE);
        m_outBufferCache = new StreamBufferOutPool(BUFFER_SIZE);
    }
    
    /**
     * Setup output using an HTTP connection. This sets output to use the POST operation, and sets headers from the
     * properties map.
     * @param connection
     * @param props
     * 
     * @throws ProtocolException
     */
    private static void setupOutput(HttpURLConnection connection, MessageProperties props) throws ProtocolException {
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty(CONTENT_TYPE, getContentTypeProperty(props));
        connection.setRequestProperty(ACCEPT_TYPE, getAcceptTypeProperty(props));
        for (Iterator iter = props.getPropertyNames().iterator(); iter.hasNext();) {
            String propertyName = (String)iter.next();
            connection.setRequestProperty(propertyName, props.getProperty(propertyName));
        }
    }
    
    private static String getContentTypeProperty(MessageProperties props) {
        MediaType contentType = props.getContentType();
        MediaType.Parameter charset = null;
        MediaType.Parameter action = null;
        if (props.getCharset() != null) {
            charset = new MediaType.Parameter(CHARSET_KEY, props.getCharset().toLowerCase());
        }
        if (props.getOperation() != null) {
            action = new MediaType.Parameter(ACTION_KEY, props.getOperation());
        }
        return contentType.toStringWithParams(new MediaType.Parameter[] {charset, action});
    }

    private static String getAcceptTypeProperty(MessageProperties props) {
        MediaType[] acceptTypes = props.getAcceptTypes();
        if (acceptTypes == null || acceptTypes.length == 0) {
            throw new IllegalArgumentException("Internal JiBX/WS error. Expected Accept media type(s) to be set.");
        }
        StringBuffer buff = new StringBuffer(64);
        for (int i = 0; i < acceptTypes.length; i++) {
            if (i > 0) {
                buff.append(", ");
            }
            buff.append(acceptTypes[i].toString());
        }
        return buff.toString();
    }

    /** {@inheritDoc} */
    public InConnection getInbound() throws IOException, WsConfigurationException {
        HttpURLConnection connection = (HttpURLConnection)m_url.openConnection();
        connection.connect();
        return createInConnection(connection);
    }

    /** {@inheritDoc} */
    public OutConnection getOutbound(MessageProperties properties, XmlOptions xmlOptions) throws IOException, 
            WsConfigurationException {
        HttpURLConnection connection = (HttpURLConnection)m_url.openConnection();
        connection.setDoInput(false);
        setupOutput(connection, properties);
        connection.connect();
        return createOutConnection(connection, properties, xmlOptions);
    }

    /** {@inheritDoc} */
    public DuplexConnection getDuplex(MessageProperties properties, XmlOptions xmlOptions) throws IOException, 
            WsConfigurationException {
        HttpURLConnection connection = (HttpURLConnection)m_url.openConnection();
        connection.setDoInput(true);
        setupOutput(connection, properties);
        connection.connect();
        return new SimpleDuplexConnection(createInConnection(connection), 
            createOutConnection(connection, properties, xmlOptions));
    }
    
    private HttpInConnection createInConnection(HttpURLConnection connection) throws WsConfigurationException {
        HttpInConnection inConn = new HttpInConnection(connection);
        if (m_transportOptions.getInputStreamInterceptor() != null) {
            inConn.setInterceptor(m_transportOptions.getInputStreamInterceptor());
        }
        return inConn;
    }
    
    private HttpOutConnection createOutConnection(HttpURLConnection connection, MessageProperties properties, 
            XmlOptions xmlOptions) throws WsConfigurationException {
        HttpOutConnection outConn = new HttpOutConnection(connection, properties, xmlOptions);
        if (m_transportOptions.getOutputStreamInterceptor() != null) {
            outConn.setInterceptor(m_transportOptions.getOutputStreamInterceptor());
        }
        return outConn;
    }
    
    /** {@inheritDoc} */
    public void close() {
    }
    
    /**
     * An inbound connection wrapper for HttpURLConnection. 
     */
    private class HttpInConnection implements InConnection
    {
        private static final int MIN_HTTP_ERROR_CODE = 400;

        private static final int ERROR_BUFFER_SIZE = 4000;

        /** Actual connection. */
        private final HttpURLConnection m_connection;
        
        /** Buffer used by connection. */
        private InByteBuffer m_buffer;
        
        /** Reader for connection. */
        private IXMLReader m_reader;

        /** An interceptor to intercept the input stream. */
        private InputStreamInterceptor m_interceptor;

        /**
         * @param connection
         */
        public HttpInConnection(HttpURLConnection connection) {
            m_connection = connection;
        }
        
        /** {@inheritDoc} */
        public void init() throws IOException, WsException {
            getReader();
            m_reader.init();
        }
        
        /** {@inheritDoc} */
        public String getCharacterEncoding() {
            return m_connection.getContentEncoding();
        }
        
        /** {@inheritDoc} */
        public String getContentType() {
            return m_connection.getContentType();
        }
        
        /** {@inheritDoc} */
        public String getDestination() {
            return null;
        }
        
        /** {@inheritDoc} */
        public String getId() {
            return null;
        }
        
        /** 
         * {@inheritDoc}
         *
         * Gets the name of the operation. For HTTP, this will parse the content-type header for an "action" 
         * parameter and return the value of this parameter if found.
         * <p>
         * This allows the operation name to be retrieved for SOAP 1.2 messages. SOAP 1.1 messages
         * will need to call {@link #getProperty(String)} with a parameter of {@link SoapProtocol#SOAPACTION_HEADER}
         * instead.     
         */
        public String getOperationName() {
            String opname = null;
            String contentTypeProp = getProperty(CONTENT_TYPE);
            if (contentTypeProp != null) {
                int i = contentTypeProp.indexOf("action=");
                if (i != -1) {
                    opname = contentTypeProp.substring(i);
                }
            }
            return opname;
        }
        
        /** {@inheritDoc} */
        public String getOrigin() {
            return null;
        }
        
        /** {@inheritDoc} */
        public String getProperty(String name) {
            return m_connection.getHeaderField(name);
        }
        
        /** {@inheritDoc} */
        public IXMLReader getReader() throws IOException, WsException {
            if (m_reader == null) {
                MediaType mediaType = getContentMediaType();
                m_buffer = (InByteBuffer)m_inBufferCache.getInstance();
                
                InputStream inputStream;
                if (hasError()) {
                    inputStream = m_connection.getErrorStream();
                } else {
                    inputStream = m_connection.getInputStream();
                }
                
                if (m_interceptor != null) {
                    inputStream = m_interceptor.intercept(inputStream);
                }
                
                m_buffer.setInput(inputStream);
                m_reader = m_codecCache.getCodec(mediaType).getReader(m_buffer,
                        getCharacterEncoding(), m_connection.getURL().toExternalForm(), true);
            }
            return m_reader;
        }

        private MediaType getContentMediaType() throws IOException {
            MediaType mediaType = null;
            String ctype = getContentType();
            MediaType contentType;
            if (ctype != null) {
                try {
                    contentType = new MediaType(ctype);
                } catch (ParseException e) {
                    throw new IOException("Unable to parse content-type '" + ctype + "'");
                }
                if (CodecDirectory.hasCodecFor(contentType)) {
                    mediaType = contentType;
                }
            }
            if (mediaType == null) {
                mediaType = CodecDirectory.TEXT_XML_MEDIA_TYPE;
            }
            return mediaType;
        }

        /** {@inheritDoc} */
        public boolean hasError() throws IOException {
            return m_connection.getResponseCode() >= MIN_HTTP_ERROR_CODE;
        }

        /** {@inheritDoc} */
        public String getErrorMessage() throws IOException {
            if (!hasError()) {
                return null;
            }
            
            StringBuffer error = new StringBuffer(ERROR_BUFFER_SIZE);
            String newLine = System.getProperty("line.separator");
            error.append(m_connection.getResponseCode()).append(" ").append(m_connection.getResponseMessage())
                    .append(newLine);
            InputStream errorStream = m_connection.getErrorStream();
            if (m_interceptor != null) {
                errorStream = m_interceptor.intercept(errorStream);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = in.readLine()) != null) {
                error.append(line).append(newLine);
            }
            return error.toString();
        }

        /** 
         * Sets the interceptor for intercepting the input stream.
         * @param interceptor the interceptor
         * @throws WsConfigurationException on error configuring interceptor, for example more than one input stream
         * interceptor is configured 
         */
        public void setInterceptor(InputStreamInterceptor interceptor) throws WsConfigurationException  {
            if (m_interceptor != null) {
                throw new WsConfigurationException("Only a single input stream interceptor is supported.");
            }
            m_interceptor = interceptor;
        }

        public void inputComplete() {
            if (m_interceptor != null) {
                m_interceptor.inputComplete();
            }
        }
        
        /** {@inheritDoc} */
        public void close() throws IOException {
            if (m_buffer != null) {
                m_inBufferCache.endUsage(m_buffer);
                m_buffer = null;
            }
            m_reader = null;
        }
    }

    /**
     * An outbound connection wrapper for HttpURLConnection. 
     */
    private class HttpOutConnection extends OutConnectionBase
    {
        /** Actual connection. */
        private final HttpURLConnection m_connection;
        
        /** Buffer used by connection. */
        private OutByteBuffer m_buffer;
        
        /** Writer for connection. */
        private IXMLWriter m_writer;

        /** An interceptor to intercept the output stream. */
        private OutputStreamInterceptor m_interceptor;

        private MessageProperties m_msgProps;
        
        /**
         * @param connection
         * @param msgProps message specific properties
         * @param xmlOptions XML formatting options
         */
        public HttpOutConnection(HttpURLConnection connection, MessageProperties msgProps, XmlOptions xmlOptions) {
            super(xmlOptions);
            m_connection = connection;
            m_msgProps = msgProps;
        }
        
        /** {@inheritDoc} */
        public IXMLWriter getNormalWriter(String[] uris) throws IOException, WsException {
            if (m_writer == null) {
                m_buffer = (OutByteBuffer)m_outBufferCache.getInstance();

                OutputStream outputStream = m_connection.getOutputStream();
                if (m_interceptor != null) {
                    outputStream = m_interceptor.intercept(outputStream);
                }
                m_buffer.setOutput(outputStream);
                m_writer = m_codecCache.getCodec(m_msgProps.getContentType()).getWriter(m_buffer, null, uris);
                initializeWriter(m_writer);
            }
            return m_writer;
        }
        
        /** {@inheritDoc} */
        public IXMLWriter getFaultWriter(String[] uris) throws IOException, WsException {
            return getNormalWriter(uris);
        }
        
        /** 
         * Sets the interceptor for intercepting the output stream.
         * @param interceptor the interceptor
         * @throws WsConfigurationException on error configuring interceptor, for example more than one output stream
         * interceptor is configured 
         */
        public void setInterceptor(OutputStreamInterceptor interceptor) throws WsConfigurationException {
            if (m_interceptor != null) {
                throw new WsConfigurationException("Only a single output stream interceptor is supported.");
            }
            this.m_interceptor = interceptor;
        }

        /** {@inheritDoc} */
        public void outputComplete() {
            if (m_interceptor != null) {
                m_interceptor.outputComplete();
            }
        }

        /** {@inheritDoc} */
        public void close() throws IOException {
            if (m_writer != null) {
                m_writer.close();
                m_writer.reset();
            }
            if (m_buffer != null) {
                m_outBufferCache.endUsage(m_buffer);
                m_buffer = null;
            }
        }
    }
}
