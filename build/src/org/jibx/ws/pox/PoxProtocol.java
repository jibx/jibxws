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

package org.jibx.ws.pox;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.UnhandledWsException;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.client.Client;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.pox.client.PoxClient;
import org.jibx.ws.pox.server.PoxService;
import org.jibx.ws.process.Processor;
import org.jibx.ws.protocol.Protocol;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceExceptionHandler;
import org.jibx.ws.server.ServiceExceptionHandlerFactory;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.OutServerConnection;

/**
 * Defines a protocol for Plain Old XML (POX).  This protocol communicates using XML without extra headers. 
 * 
 * @author Nigel Charman
 */
public final class PoxProtocol implements Protocol
{
    /** POX protocol instance. */
    public static final PoxProtocol INSTANCE = new PoxProtocol();

    private static ServiceFactory s_serviceFactory = new ServiceFactory() {
        public Service createInstance(ServiceDefinition sdef) throws WsException {
            return new PoxService(sdef, new PoxProcessor(), s_mediaTypeMapper, 
                getDefaultExceptionHandlerFactory(sdef.getIncludeStackTraceOnFault()));
        }
    };

    private static final MediaTypeMapper s_mediaTypeMapper;

    private static ServiceExceptionHandlerFactory s_defaultExceptionHandlerFactory = 
            new ServiceExceptionHandlerFactory() {
        public ServiceExceptionHandler createServiceExceptionHandler() {
            return new ServiceExceptionHandler() {
                public void handleException(Throwable e, Processor processor, OutServerConnection outConn) {
                    throw UnhandledWsException.wrap(e);
                }
            };
        }
    };
    
    static {
        s_mediaTypeMapper = new MediaTypeMapper() {
            /**
             * {@inheritDoc}
             */
            public MediaType getMediaTypeFor(String code) throws WsConfigurationException {
                if (code == null || CodecDirectory.TEXT_MEDIA_CODE.equals(code)) {
                    return CodecDirectory.TEXT_XML_MEDIA_TYPE;
                } 
                
                MediaType type = new MediaType("application", code);
                if (!CodecDirectory.hasCodecFor(type)) {
                    throw new WsConfigurationException("No codec available for media type '" + type 
                        + "' based on media type code '" + code + "' with protocol POX");
                }
                return type;
            }
        };
    }

    /**
     * {@inheritDoc}
     * 
     * @return a {@link PoxClient}
     */
    public Client createClient(String location, IBindingFactory factory, MessageOptions options)
        throws WsBindingException, WsConfigurationException {

        return new PoxClient(location, factory, options);
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link PoxProcessor}
     */
    public Processor createProcessor(ExchangeContext exchangeContext) {
        return new PoxProcessor(exchangeContext);
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@link PoxProcessor}
     */
    public Processor createProcessor() {
        return new PoxProcessor();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "POX";
    }

    /**
     * {@inheritDoc}
     */
    public ServiceFactory getServiceFactory() {
        return s_serviceFactory;
    }

    /**
     * {@inheritDoc}
     */
    public MediaTypeMapper getMediaTypeMapper() {
        return s_mediaTypeMapper;
    }

    /**
     * {@inheritDoc}
     */
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

        if (charset != null) {
            props.setCharset(charset);
        }

        return props;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException since operation names are currently unsupported by this implementation
     */
    public String getOperationName(InConnection conn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates {@link ServiceExceptionHandler}s that rethrow the exception, wrapping checked exceptions in an 
     * {@link UnhandledWsException}.
     * 
     * @param includeStackTrace ignored
     * @return factory 
     */
    public static ServiceExceptionHandlerFactory getDefaultExceptionHandlerFactory(boolean includeStackTrace) {
        return s_defaultExceptionHandlerFactory;
    }
}
