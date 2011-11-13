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

package org.jibx.ws.http.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.InByteBuffer;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.CodecPool;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.codec.XmlCodec;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.Service;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnectionBase;
import org.jibx.ws.transport.OutServerConnection;
import org.jibx.ws.transport.StreamBufferInPool;
import org.jibx.ws.transport.StreamBufferOutPool;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;
import org.jibx.ws.wsdl.WsdlProvider;

/**
 * Web service request handler servlet. The configuration information for this servlet is obtained from one or more
 * service definition files located with the WEB-INF directory of the web application. The particular service definition
 * files handled by an instance of this servlet are configured as initialization parameters for the servlet. If the
 * servlet is invoked with any path information in the request the path information is used to identify the particular
 * service being requested. As a special case, the request parameter "?WSDL" is recognized as a request for the WSDL
 * service description.
 * 
 * @author Dennis M. Sosnoski
 */
public final class WsServletDelegate
{
    // TODO: make this a configuration parameter
    private static final int BUFFER_SIZE = 8192;

    /** The key of the character set parameter, which can be used as a parameter to the CONTENT_TYPE header field. */  
    private static final String CHARSET_KEY = "charset";
    
    private static final Log logger = LogFactory.getLog(WsServletDelegate.class);

    /** Pool of codecs used for input and output. Access to this pool must be synchronized on the pool object. */
    private static final CodecPool s_codecPool = new CodecPool();

    /** Input byte buffer pool. Access to this pool must be synchronized on the {@link #s_codecPool} object. */
    private static final StreamBufferInPool s_inBufferCache = new StreamBufferInPool(BUFFER_SIZE);

    /** Output byte buffer pool. Access to this pool must be synchronized on the {@link #s_codecPool} object. */
    private static final StreamBufferOutPool s_outBufferCache = new StreamBufferOutPool(BUFFER_SIZE);

    /** Maps the incoming request to a service. */
    private ServiceMapper m_serviceMapper;
    
    /**
     * Sets the {@link ServiceMapper} which will determine which service to call based on the incoming request.
     * 
     * @param mapper the service mapper
     */
    void setServiceMapper(ServiceMapper mapper) {
        m_serviceMapper = mapper;
    }

    /**
     * POST request handler. This processes the incoming request message and generates the response.
     * 
     * @param req servlet request information
     * @param rsp servlet response information
     * @exception ServletException on message content or operational error
     * @exception IOException on error reading or writing
     */
    public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        logger.debug("Entered WsServletDelegate.doPost()");
        Service serv = null;
        XmlCodec incodec = null;
        XmlCodec outcodec = null;
        InByteBuffer inbuff = null;
        OutByteBuffer outbuff = null;
        try {
            // make sure we have a service instance
            serv = m_serviceMapper.getServiceInstance(req);
            if (serv == null) {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                MediaType intype = getMediaType(req.getContentType(), serv.getMediaTypeMapper());
                MediaType outtype = getAcceptableMediaType(req.getHeader("Accept"), intype);

                synchronized (s_codecPool) {
                    // allocated codec(s) and buffers for the input and output
                    incodec = s_codecPool.getCodec(intype);
                    if (intype.equals(outtype)) {
                        outcodec = incodec;
                    } else {
                        outcodec = s_codecPool.getCodec(outtype);
                    }
                    inbuff = (InByteBuffer) s_inBufferCache.getInstance();
                    outbuff = (OutByteBuffer) s_outBufferCache.getInstance();
                }

                // pass the processing on to the service
                InboundConnection inconn = new InboundConnection(req, incodec, inbuff);
                OutboundConnection outconn = new OutboundConnection(rsp, req.getCharacterEncoding(), 
                    serv.getXmlOptions(), outcodec, outbuff);
                HttpServletOptions options = (HttpServletOptions) serv.getTransportOptions(HttpServletOptions.class);
                if (options != null) {
                    if (options.getInputStreamInterceptor() != null) {
                        inconn.setInterceptor(options.getInputStreamInterceptor());
                    } 
                    if (options.getOutputStreamInterceptor() != null) {
                        outconn.setInterceptor(options.getOutputStreamInterceptor());
                    }
                }
                serv.processRequest(inconn, outconn);
            }

        } catch (WsException e) {
            logger.error("Error processing request", e);
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            synchronized (s_codecPool) {

                // release all resources acquired for processing request
                if (serv != null) {
                    serv.releaseInstance();
                }
                if (incodec != null) {
                    s_codecPool.releaseCodec(incodec);
                }
                if (outcodec != null && outcodec != incodec) {
                    s_codecPool.releaseCodec(outcodec);
                }
                if (inbuff != null) {
                    s_inBufferCache.endUsage(inbuff);
                }
                if (outbuff != null) {
                    s_outBufferCache.endUsage(outbuff);
                }

            }
        }
    }

    /**
     * Obtain the media type from the media type string.  If <code>mediastring</code> is <code>null</code>, this returns
     * the default text media type for the protocol that the service is using.
     * <p>
     * All of the parameters on the <code>mediastring</code> are ignored.  
     *
     * @param mediastring media type string (e.g., "text/xml" or "application/soap+xml;action=xyz")
     * @param protocol the protocol that the service is using
     * @return media type with parameters removed
     * @throws ServletException if <code>mediastring</code> contains no supported media type 
     */
    private MediaType getMediaType(String mediastring, MediaTypeMapper mapper) throws ServletException {
        MediaType media;
        if (mediastring == null) {
            try {
                media = mapper.getMediaTypeFor(null);
            } catch (WsConfigurationException e) {
                throw new ServletException("Internal JiBX/WS error. Unable to find default media type due to '"
                    + e.getMessage() + "'.");
            }
        } else {
            try {
                media = new MediaType(mediastring, true);
            } catch (ParseException e) {
                throw new ServletException("Error parsing media type in content-type from request: " + mediastring);
            }
            if (!CodecDirectory.hasCodecFor(media)) {
                throw new ServletException("No supported media type in content-type from request: " + mediastring);
            }
        }
        return media;
    }


    /**
     * See {@link CodecDirectory#getAcceptableMediaType(String)}.
     */
    private MediaType getAcceptableMediaType(String acceptable, MediaType contentType) throws ServletException {
        try {
            return CodecDirectory.getAcceptableMediaType(acceptable, contentType);
        } catch (ParseException e) {
            throw new ServletException("Error parsing media type in accept-type from request: " + acceptable, e);
        }
    }

    /**
     * GET request handler. The only type of GET request supported is one to get the WSDL for a service.
     * 
     * @param context the servlet context
     * @param req servlet request information
     * @param rsp servlet response information
     * @exception ServletException on message content or operational error
     * @exception IOException on error reading or writing
     */
    protected void doGet(ServletContext context, HttpServletRequest req, HttpServletResponse rsp)
        throws ServletException, IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("Entered WsServletDelegate.doGet() with query string: " + req.getQueryString());
        }
        
        // look up service information and check request
        if ("wsdl".equalsIgnoreCase(req.getQueryString())) {
            Service service = null;
            try {
                logger.debug("Looking up service for WSDL");
                service = m_serviceMapper.getServiceInstance(req);
                if (service == null) {
                    logger.debug("Unable to find service for WSDL");
                    rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                } else {
                    WsdlProvider wsdlProvider = service.getWsdlProvider();
                    if (wsdlProvider == null) {
                        logger.debug("Unable to WSDL provider for service");
                        rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    } else {
                        logger.debug("Returning WSDL");
                        wsdlProvider.writeWSDL(rsp.getOutputStream(), req);
                    }
                }
            } catch (WsException e) {
                logger.error("Error creating WSDL", e);
                throw new ServletException(e.getMessage(), e);
            } finally {
                // release all resources acquired for processing request
                if (service != null) {
                    service.releaseInstance();
                }
            }
        } else {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    /**
     * Inbound connection (data received from client).
     */
    private static class InboundConnection implements InConnection
    {
        /** Request data. */
        private final HttpServletRequest m_request;

        /** Codec to be used for input. */
        private final XmlCodec m_codec;

        /** Buffer used for input data. */
        private final InByteBuffer m_buffer;

        /** Reader for connection. */
        private IXMLReader m_reader;

        /** An interceptor for intercepting input stream, or <code>null</code> if no interceptor. */  
        private InputStreamInterceptor m_interceptor;

        /**
         * Constructor.
         * 
         * @param request
         * @param codec
         * @param buff
         */
        public InboundConnection(HttpServletRequest request, XmlCodec codec, InByteBuffer buff) {
            m_request = request;
            m_codec = codec;
            m_buffer = buff;
        }

        /** {@inheritDoc} */
        public String getCharacterEncoding() {
            return m_request.getCharacterEncoding();
        }

        /** {@inheritDoc} */
        public String getContentType() {
            return m_request.getContentType();
        }

        /** {@inheritDoc} */
        public String getDestination() {
            return null;
        }

        /** {@inheritDoc} */
        public String getId() {
            return null;
        }

        /** {@inheritDoc} */
        public String getOperationName() {
            return null;
        }

        /** {@inheritDoc} */
        public String getOrigin() {
            return m_request.getRemoteHost();
        }

        /** {@inheritDoc} */
        public String getProperty(String name) {
            return m_request.getHeader(name);
        }

        /** {@inheritDoc} */
        public IXMLReader getReader() throws IOException {
            if (m_reader == null) {
                InputStream inputStream = m_request.getInputStream();
                if (m_interceptor != null) {
                    inputStream = m_interceptor.intercept(inputStream);
                }
                m_buffer.setInput(inputStream);
                m_reader = m_codec.getReader(m_buffer, m_request.getCharacterEncoding(), m_request.getRemoteAddr(),
                    true);
            }
            return m_reader;
        }

        /** {@inheritDoc} */
        public void init() throws IOException {
            getReader();
            m_reader.init();
        }

        /** {@inheritDoc} */
        public boolean hasError() throws IOException {
            return false;
        }

        /** {@inheritDoc} */
        public String getErrorMessage() throws IOException {
            return null;
        }
        
        /**
         * Sets an interceptor for intercepting the input stream.
         *
         * @param interceptor the interceptor
         */
        private void setInterceptor(InputStreamInterceptor interceptor) throws WsConfigurationException {
            m_interceptor = interceptor;
        }
        
        public void inputComplete() {
            if (m_interceptor != null) {
                m_interceptor.inputComplete();
            }
        }

        /** {@inheritDoc} */
        public void close() throws IOException {
            m_buffer.finish();
        }
    }

    /**
     * Outbound connection (data sent to client).
     */
    private static class OutboundConnection extends OutConnectionBase implements OutServerConnection
    {
        /** Response data. */
        private final HttpServletResponse m_response;

        /** Codec to be used for output. */
        private final XmlCodec m_codec;

        /** Character encoding to be used for output (<code>null</code> if unknown or not applicable). */
        private final String m_characterCode;

        /** Buffer used by connection. */
        private OutByteBuffer m_buffer;

        /** Writer for connection. */
        private IXMLWriter m_writer;

        /** An interceptor for intercepting output stream, or <code>null</code> if no interceptor. */  
        private OutputStreamInterceptor m_interceptor;

        /**
         * Constructor.
         * 
         * @param response
         * @param charcode
         * @param xmlOptions formatting options for outbound XML
         * @param codec
         * @param buff
         */
        public OutboundConnection(HttpServletResponse response, String charcode, XmlOptions xmlOptions, XmlCodec codec, 
                OutByteBuffer buff) {
            super(xmlOptions);
            m_response = response;
            m_buffer = buff;
            m_codec = codec;
            m_characterCode = charcode;
        }


        /** {@inheritDoc} */
        public void sendNotFoundError() throws IOException {
            m_response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        /** {@inheritDoc} */
        public void setInternalServerError() {
            m_response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        /** {@inheritDoc} */
        public boolean isCommitted() {
            return m_response.isCommitted();
        }

        /** {@inheritDoc} */
        public IXMLWriter getNormalWriter(String[] uris) throws IOException {
            if (m_writer == null) {

                // first set the output content type
                MediaType contentType = m_codec.getMediaType();

                MediaType.Parameter charset = null;
                if (m_characterCode != null) {
                    charset = new MediaType.Parameter(CHARSET_KEY, m_characterCode);
                }
                m_response.setContentType(contentType.toStringWithParams(new MediaType.Parameter[]{charset}));

                // set up the actual writer
                OutputStream outputStream = m_response.getOutputStream();
                if (m_interceptor != null) {
                    outputStream = m_interceptor.intercept(outputStream);
                }
                
                m_buffer.setOutput(outputStream);
                
                m_writer = m_codec.getWriter(m_buffer, null, uris);
                initializeWriter(m_writer);
            }
            return m_writer;
        }

        /** {@inheritDoc} */
        public IXMLWriter getFaultWriter(String[] uris) throws IOException {
            return getNormalWriter(uris);
        }

        /**
         * Sets an interceptor for intercepting the output stream.
         *
         * @param interceptor the interceptor
         */
        private void setInterceptor(OutputStreamInterceptor interceptor) {
            m_interceptor = interceptor;
        }

        /** {@inheritDoc} */
        public void outputComplete() {
            if (m_interceptor != null) {
                m_interceptor.outputComplete();
            }
        }

        /** {@inheritDoc} */
        public void close() throws IOException {
            logger.debug("Closing output connection");
            if (m_writer != null) {
                logger.debug("Closing writer");
                m_writer.close();
                m_writer.reset();
            }
        }
    }
}
