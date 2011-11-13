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

package org.jibx.ws.soap;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.client.Client;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.process.Processor;
import org.jibx.ws.protocol.Protocol;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceExceptionHandler;
import org.jibx.ws.server.ServiceExceptionHandlerFactory;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.soap.client.SoapClient;
import org.jibx.ws.soap.server.SoapFaultHandler;
import org.jibx.ws.soap.server.SoapService;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;

/**
 * Defines a specific version of the SOAP protocol.
 * 
 * @author Nigel Charman
 */
public final class SoapProtocol implements Protocol
{
//    private static final MediaType SOAP1_2_TEXT_XML_MEDIA_TYPE = new MediaType("application", "soap+xml");

    /**  HTTP header field to carry operation name (SOAP 1.1 only). */
    public static final String SOAPACTION_HEADER = "SOAPAction";

    /** SOAP Version 1.1. */
    public static final SoapProtocol SOAP1_1 = new SoapProtocol(SoapVersion.SOAP1_1,
        "http://schemas.xmlsoap.org/soap/envelope/");

//    /** SOAP Version 1.2. */
//    public static final SoapProtocol SOAP1_2 = new SoapProtocol(SoapVersion.SOAP1_2,
//        "http://www.w3.org/2003/05/soap-envelope");

    private final SoapVersion m_soapVersion;

    private final String m_soapUri;

    private static final MediaTypeMapper s_soap11MediaTypeMapper;
//    private static final MediaTypeMapper s_soap12MediaTypeMapper;

    private static final ServiceFactory s_serviceFactory = new ServiceFactory() {
        public Service createInstance(ServiceDefinition sdef) throws WsException {
            // Always returns SOAP1.1 mapper and processor.  Need to add logic to switch on service def and/or 
            // incoming payload protocol.
            return new SoapService(sdef, s_soap11MediaTypeMapper, new SoapProcessor(SoapVersion.SOAP1_1), 
                getDefaultExceptionHandlerFactory(sdef.getIncludeStackTraceOnFault()));
        }
    };

    private static final ServiceExceptionHandlerFactory s_exceptionHandlerFactoryWithTrace = 
            new ServiceExceptionHandlerFactory() {
        public ServiceExceptionHandler createServiceExceptionHandler() {
            return new SoapFaultHandler(true);
        }
    };

    private static final ServiceExceptionHandlerFactory s_exceptionHandlerFactoryNoTrace = 
            new ServiceExceptionHandlerFactory() {
        public ServiceExceptionHandler createServiceExceptionHandler() {
            return new SoapFaultHandler(false);
        }
    };
    
    static {
        s_soap11MediaTypeMapper = new MediaTypeMapper() {
            /**  {@inheritDoc} */
            public MediaType getMediaTypeFor(String code) throws WsConfigurationException {
                if (code == null || CodecDirectory.TEXT_MEDIA_CODE.equals(code)) {
                    return CodecDirectory.TEXT_XML_MEDIA_TYPE;
                } 
                
                MediaType type = new MediaType("application", "soap+" + code);
                if (!CodecDirectory.hasCodecFor(type)) {
                    throw new WsConfigurationException("No codec available for media type '" + type 
                        + "' based on media type code '" + code + "' with protocol SOAP 1.1");
                }
                return type;
            }
        };

//        s_soap12MediaTypeMapper = new MediaTypeMapper() {
//            /**  {@inheritDoc} */
//            public MediaType getMediaTypeFor(String code) throws WsConfigurationException {
//                if (code == null || CodecDirectory.TEXT_MEDIA_CODE.equals(code)) {
//                    return SOAP1_2_TEXT_XML_MEDIA_TYPE;
//                } 
//                
//                MediaType type = new MediaType("application", "soap+" + code);
//                if (!CodecDirectory.hasCodecFor(type)) {
//                    throw new WsConfigurationException("No codec available for media type '" + type 
//                        + "' based on media type code '" + code + "' with protocol SOAP 1.2");
//                }
//                return type;
//            }
//        };
    }

    
    /**
     * Constructor.
     * 
     * @param soapVersion the version of the SOAP specification
     * @param uri SOAP namespace URI
     */
    private SoapProtocol(SoapVersion soapVersion, String uri) {
        m_soapVersion = soapVersion;
        m_soapUri = uri;
    }

    /**
     * Get the appropriate namespace URI.
     * 
     * @return URI
     */
    public String getUri() {
        return m_soapUri;
    }

    /**
     * {@inheritDoc}
     * 
     * @return a {@link SoapProcessor}
     */
    public Processor createProcessor() {
        return new SoapProcessor(m_soapVersion);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return a {@link SoapProcessor}
     */
    public Processor createProcessor(ExchangeContext exchangeContext) {
        return new SoapProcessor(m_soapVersion, exchangeContext);
    }

    /**
     * {@inheritDoc} 
     * 
     * @return a {@link SoapClient}
     */
    public Client createClient(String location, IBindingFactory factory, MessageOptions options)
        throws WsBindingException, WsConfigurationException {

        return new SoapClient(location, factory, options, this);
    }

    /**  {@inheritDoc} */
    public ServiceFactory getServiceFactory() {
        return s_serviceFactory;
    }
    
    /**  {@inheritDoc} */
    public String getName() {
        return m_soapVersion.toString();
    }

    /**  {@inheritDoc} */
    public MediaTypeMapper getMediaTypeMapper() {
//        if (m_soapVersion.equals(SoapVersion.SOAP1_1)) {
            return s_soap11MediaTypeMapper;
//        } else {
//            return s_soap12MediaTypeMapper;
//        }
    }

    /**  {@inheritDoc} */
    public MessageProperties buildMessageProperties(String opname, MessageOptions msgOptions)
            throws WsConfigurationException {
        
        String charset = msgOptions.getEncoding().toString();
        String outtype = msgOptions.getOutMediaTypeCode();
        String[] intypes = msgOptions.getInMediaTypeCodes();        
        
        MessageProperties props = new MessageProperties();
        props.setContentType(getMediaTypeMapper().getMediaTypeFor(outtype));
        
        MediaType[] acceptTypes = new MediaType[intypes.length];
        for (int i = 0; i < intypes.length; i++) {
            acceptTypes[i] = getMediaTypeMapper().getMediaTypeFor(intypes[i]);
        }
        props.setAcceptTypes(acceptTypes);
        
        if (m_soapVersion.equals(SoapVersion.SOAP1_1)) {
            String soapAction;
            if (opname == null) {
                soapAction = ""; // No value means that there is no indication of the intent of the message.
            } else {
                soapAction = "\"" + opname + "\"";
            }
            props.setProperty(SOAPACTION_HEADER, soapAction);
//        } else if (m_soapVersion.equals(SoapVersion.SOAP1_2)) {
//            if (opname != null && opname.length() > 0) {
//                props.setOperation(opname);
//            }
        } else {
            throw new IllegalStateException("Internal error - unsupported SOAP version " + m_soapVersion);
        }
        if (charset != null) {
            props.setCharset(charset);
        }

        return props;
    }

    /**  {@inheritDoc} */
    public String getOperationName(InConnection conn) {
        String opname = null;
        if (m_soapVersion.equals(SoapVersion.SOAP1_1)) {
            opname = conn.getProperty(SOAPACTION_HEADER);
            if (opname != null && opname.startsWith("\"") && opname.endsWith("\"")) {
                opname = opname.substring(1, opname.length() - 1);
            }
//        } else if (m_soapVersion.equals(SoapVersion.SOAP1_2)) {
//            opname = conn.getOperationName();
        }
        return opname;
    }

    /**
     * Creates {@link ServiceExceptionHandler}s that send a SOAP fault response message.
     * 
     * @param includeStackTrace <code>true</code> to include the stack trace in the SOAP fault, <code>false</code> 
     * otherwise.
     * @return factory 
     */
    public static ServiceExceptionHandlerFactory getDefaultExceptionHandlerFactory(final boolean includeStackTrace) {
        if (includeStackTrace) {
            return s_exceptionHandlerFactoryWithTrace;
        } else {
            return s_exceptionHandlerFactoryNoTrace;
        }
    }
}
