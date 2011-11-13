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

package org.jibx.ws.soap.server;

import org.jibx.ws.WsException;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.handler.InHandler;
import org.jibx.ws.io.handler.OutHandler;
import org.jibx.ws.process.Processor;
import org.jibx.ws.server.HandlerDefinition;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceExceptionHandlerFactory;
import org.jibx.ws.soap.SoapPhase;
import org.jibx.ws.wsdl.WsdlProvider;

/**
 * Service implementation for a document-literal web service using SOAP. This builds on the basic SOAP mapping
 * implementation, attaching service endpoint information to allow the actual processing of requests.
 * 
 * @author Dennis M. Sosnoski
 */
public final class SoapService extends Service // implements WsdlProvider
{
	private WsdlProvider m_wsdlProvider;

//    /** Service definitions, needed for creating WSDL. */
//    private final ServiceDefinition m_serviceDef;
//
//    /** WSDL definitions for service. Lazy create, only if needed. */
//    private Definitions m_wsdlDefinitions;

    /**
     * Create service from definition.
     * 
     * @param sdef service definition information
     * @param processor for processing the message
     * @param mediaTypeMapper to map media type code to media type
     * @param defaultExceptionHandlerFactory for creating exception handler if none defined in service definition
     * @throws WsException on error creating the service
     */
    public SoapService(ServiceDefinition sdef, MediaTypeMapper mediaTypeMapper, Processor processor, 
            ServiceExceptionHandlerFactory defaultExceptionHandlerFactory) throws WsException {
        
        super(sdef, processor, mediaTypeMapper, defaultExceptionHandlerFactory);

        processor.setExchangeContext(createExchangeContext(sdef));
//        m_serviceDef = sdef;
    }

    /**
     * Creates an ExchangeContext instance from the supplied ServiceDefinition.
     * 
     * @param sdef the service definition
     * @return exchange context based on the service definition
     * @throws WsException on error creating exchange context, for example handler object cannot be created
     */
    private ExchangeContext createExchangeContext(ServiceDefinition sdef) throws WsException {
        InContext inCtx = new InContext();
        OutContext outCtx = new OutContext();

        createBodyHandlers(inCtx, outCtx);
        createHeaderHandlers(sdef, outCtx, inCtx);
        setContextOnTransportOptions(inCtx, outCtx);

        return ExchangeContext.createInOutExchange(inCtx, outCtx);
    }

    /**
     * Create header handlers based on the service definition and add them to the contexts.
     *
     * @param sdef service definition
     * @param inCtx inbound message context
     * @param outCtx outbound message context 
     * @throws WsException on error creating handlers
     */
    private void createHeaderHandlers(ServiceDefinition sdef, OutContext outCtx, InContext inCtx) throws WsException {
        if (sdef.getHandlerDefinitions() != null) {
            for (int i = 0; i < sdef.getHandlerDefinitions().size(); i++) {
                HandlerDefinition hdef = (HandlerDefinition) sdef.getHandlerDefinitions().get(i);
                Object handler = hdef.getObject();
                if (handler instanceof InHandler) {
                    inCtx.addHandler(SoapPhase.HEADER, (InHandler) handler);
                } else if (handler instanceof OutHandler) {
                    outCtx.addHandler(SoapPhase.HEADER, (OutHandler) handler);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void setWsdlProvider(WsdlProvider wsdlProvider) {
        m_wsdlProvider = wsdlProvider;
    }

    /** {@inheritDoc} */
    public WsdlProvider getWsdlProvider() {
        return m_wsdlProvider;
    }

//    /**
//     * Get WSDL definitions for this service. The returned object can only be used in a single-threaded manner because
//     * of the use of a DOM schema representation, which is not threadsafe (even for read operations, in the case of 
//     * the Xerces DOM shipped with recent JDKs).
//     * 
//     * @param schemaProvider provides the schema to be used in the WSDL
//     * @return definitions object, or <code>null</code> if unable to create WSDL
//     * @throws WsException on error reading or parsing schema document
//     */
//    public synchronized Definitions getDefinitions(SchemaSource schemaProvider) throws WsException {
//        if (m_wsdlDefinitions == null) {
//            Element schema;
//            InputStream schemaStream = null;
//            try {
//
//                // read the schema to DOM representation
//                DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
//                fact.setNamespaceAware(true);
//                DocumentBuilder bldr = fact.newDocumentBuilder();
//                schemaStream = schemaProvider.getSchemaAsStream(m_serviceDef.getSchemaName());
//                Document doc = bldr.parse(schemaStream);
//                schema = doc.getDocumentElement();
//
//                // generate WSDL definition using schema
//                m_wsdlDefinitions = Generator.generate(this, m_serviceDef, schema);
//
//            } catch (ParserConfigurationException e) {
//                throw new WsException("Unable to configure parser for " + m_serviceDef.getSchemaName() 
//                    + " of service " + m_serviceDef.getServiceName() + ": " + e.getMessage());
//            } catch (SAXException e) {
//                throw new WsException("Error parsing schema " + m_serviceDef.getSchemaName() + " for service "
//                    + m_serviceDef.getServiceName() + ": " + e.getMessage());
//            } catch (IOException e) {
//                throw new WsException("Error parsing schema " + m_serviceDef.getSchemaName() + " for service "
//                    + m_serviceDef.getServiceName() + ": " + e.getMessage());
//            } catch (JiBXException e) {
//                throw new WsException("Error accessing bindings for service " + m_serviceDef.getServiceName() + ": "
//                    + e.getMessage());
//            } finally {
//                if (schemaStream != null) {
//                    try {
//                        schemaStream.close();
//                    } catch (IOException ignore) {
//                    }
//                }
//            }
//        }
//        return m_wsdlDefinitions;
//    }
}
