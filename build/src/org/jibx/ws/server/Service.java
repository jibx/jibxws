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

package org.jibx.ws.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.process.Processor;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutServerConnection;
import org.jibx.ws.transport.OutputCompletionEvent;
import org.jibx.ws.transport.OutputCompletionListener;
import org.jibx.ws.util.ExpandingPool;
import org.jibx.ws.wsdl.InputStreamWsdlProvider;
import org.jibx.ws.wsdl.WsdlLocationToRequestUrlAdapter;
import org.jibx.ws.wsdl.WsdlProvider;

/**
 * Base service implementation for extension by protocol specific implementations.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class Service
{
    private static final Log logger = LogFactory.getLog(Service.class);

    /** Instance of service class used for processing requests. */
    private final Object m_serviceObj;

    private final ServiceExceptionHandler m_serviceExceptionHandler;
    
    /** Owning pool of instances. */
    private ExpandingPool m_owningPool;
    
    /** Map from input class name (of SOAP body) to {@link OperationDefinition}. */ 
    private final Map m_operationByBodyMap;

    /** Binding factory for outbound body. */
    private IBindingFactory m_outBodyBindingFactory;

    /** Binding factory for inbound body. */
    private IBindingFactory m_inBodyBindingFactory;

    /** Formatting options for outbound XML. */
    private final XmlOptions m_xmlOptions;

    /** Listener for output completion. */
    private OutputCompletionListener m_outputCompletionListener;

    /** Transport specific options.Map from {@link TransportOptions} class name to {@link TransportOptions} instance. */
    private Map m_transportOptionsMap = Collections.EMPTY_MAP;

    /** Processor for processing the messages. */
    private final Processor m_processor; 
    
    private final MediaTypeMapper m_mediaTypeMapper;

    /**
     * Create service from definition.
     * 
     * @param sdef service definition information
     * @param processor for processing the message
     * @param mediaTypeMapper to map media type code to media type
     * @param defaultExceptionHandlerFactory for creating exception handler if none defined in service definition
     * @throws WsException on error creating the service
     */
    public Service(ServiceDefinition sdef, Processor processor, MediaTypeMapper mediaTypeMapper, 
            ServiceExceptionHandlerFactory defaultExceptionHandlerFactory) throws WsException {
        try {
            m_processor = processor;
            m_mediaTypeMapper = mediaTypeMapper;

            // build map from request body object class to operation
            m_operationByBodyMap = new HashMap();
            Class clas = null;
            boolean hasInputs = false;
            boolean hasOutputs = false;
            for (int i = 0; i < sdef.getOperationDefinitions().size(); i++) {
                OperationDefinition odef = (OperationDefinition) sdef.getOperationDefinitions().get(i);
                Operation op = Operation.newInstance(sdef.getServiceClass(), odef);
                String iname = "";
                if (op.getInputClass() != null) {
                    clas = op.getInputClass();
                    iname = clas.getName();
                    hasInputs = true;
                } else if (clas == null && op.getOutputClass() != null) {
                    clas = op.getOutputClass();
                    hasOutputs = true;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding operation '" + op.toString() + "' to service map with key '" + iname + "'");
                }
                m_operationByBodyMap.put(iname, op);
            }

            if (sdef.getServiceObject() != null) {
                m_serviceObj = sdef.getServiceObject();
            } else {
                // if any of the operation are non static, we need to create a new object for the service
                boolean hasInstanceMethod = false;
                for (Iterator operationIter = m_operationByBodyMap.values().iterator(); operationIter.hasNext();) {
                    Operation operation = (Operation) operationIter.next();
                    if (!operation.isStaticMethod()) {
                        hasInstanceMethod = true;
                        break;
                    }
                }
                if (hasInstanceMethod) {
                    m_serviceObj = sdef.getServiceClass().newInstance();
                } else {
                    m_serviceObj = null;
                }
            }

            if (sdef.getServiceExceptionHandlerObject() != null) {
                m_serviceExceptionHandler = sdef.getServiceExceptionHandlerObject();
            } else if (sdef.getServiceExceptionHandlerClass() != null) {
                m_serviceExceptionHandler = 
                    (ServiceExceptionHandler) sdef.getServiceExceptionHandlerClass().newInstance();
            } else {
                m_serviceExceptionHandler = defaultExceptionHandlerFactory.createServiceExceptionHandler();
            }
            
            m_outBodyBindingFactory = sdef.getOutBodyBindingFactory();
            m_inBodyBindingFactory = sdef.getInBodyBindingFactory();

            // set the factory for building marshalling and unmarshalling contexts
            if (m_outBodyBindingFactory == null && m_inBodyBindingFactory == null && clas != null) {
                try {
                    m_outBodyBindingFactory = BindingDirectory.getFactory(clas);
                    m_inBodyBindingFactory = m_outBodyBindingFactory;
                } catch (JiBXException e) {
                    throw new WsConfigurationException(
                        "Unable to create service since bindings not compiled for class " + clas.getName(), e);
                }
            }
            if (hasInputs && (m_inBodyBindingFactory == null)) {
                throw new WsConfigurationException("Binding factories must be defined for body of inbound messages");
            }
            if (hasOutputs && (m_outBodyBindingFactory == null)) {
                throw new WsConfigurationException("Binding factories must be defined for body of outbound messages");
            }
            
            if (sdef.getOutputCompletionListenerObject() != null) {
                m_outputCompletionListener = sdef.getOutputCompletionListenerObject();
            } else if (sdef.getOutputCompletionListenerClass() != null) {
                m_outputCompletionListener = 
                    (OutputCompletionListener) sdef.getOutputCompletionListenerClass().newInstance();
            }
            
            m_xmlOptions = sdef.getXmlOptions();

            Collection transportOptionsDefinitions = sdef.getTransportOptionsDefinitions();
            if (transportOptionsDefinitions.size() > 0) {
                m_transportOptionsMap = new HashMap();
                for (Iterator iterator = transportOptionsDefinitions.iterator(); iterator.hasNext();) {
                    TransportOptionsDefinition def = (TransportOptionsDefinition) iterator.next();
                    TransportOptions transportOptions = def.createTransportOptions();
                    m_transportOptionsMap.put(transportOptions.getClass().getName(), transportOptions);
                }
            }
        
            try {
                if (sdef.getWsdlFilepath() != null) {
                    InputStream wsdlStream = Service.class.getResourceAsStream(sdef.getWsdlFilepath());
                    if (wsdlStream == null) {
                        throw new WsConfigurationException("Unable to open WSDL file '" + sdef.getWsdlFilepath() + "'");
                    }
                    WsdlProvider wsdlProvider = new InputStreamWsdlProvider(wsdlStream);
                    if (sdef.getWsdlLocationTransform()) {
                        wsdlProvider = new WsdlLocationToRequestUrlAdapter(wsdlProvider);
                    }
                    setWsdlProvider(wsdlProvider);
                }
            } catch (IOException e) {
                throw new WsConfigurationException("Error reading WSDL file '" + sdef.getWsdlFilepath() + "'");
            }
        } catch (InstantiationException e) {
            throw new WsException("Error creating endpoint service object", e);
        } catch (IllegalAccessException e) {
            throw new WsException("Unable to create endpoint service object", e);
        }
    }

    /**
     * Sets the owning pool for this service. This method must only be called by the owning pool, immediately after
     * construction of the service. 
     *
     * @param pool owning pool
     */
    final void setOwningPool(ExpandingPool pool) {
        m_owningPool = pool;
    }
    
    /**
     * Release instance, returning it to the available list. This method must be called when processing is completed.
     */
    public void releaseInstance() {
        synchronized (m_owningPool) {
            m_owningPool.releaseInstance(this);
        }
    }

    /**
     * Process service request. This first unmarshalls and processes the request headers through any configured
     * handlers, then passes the unmarshalled body payload object to the appropriate service for actual request
     * processing. The object returned by the service (if any) is then serialized out as the response body payload,
     * along with any headers added by handlers during the outbound processing.
     * 
     * @param iconn the connection that the request is to be read from
     * @param oconn the connection that the response is to written to
     */
    public final void processRequest(InConnection iconn, OutServerConnection oconn) {
        OutContext outCtx = null; 
        try {
            getProcessor().receiveMessage(iconn);
            Object body = getProcessor().getCurrentMessageContext().getBody();

            try {
                Object response = invokeOperation(body);

                getProcessor().switchMessageContext();
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending response " + response);
                }
                outCtx = (OutContext) getProcessor().getCurrentMessageContext();
                outCtx.setBody(response);
                getProcessor().sendMessage(oconn);
            } catch (NoSuchMethodException e) {
                oconn.sendNotFoundError();
            }
        } catch (Throwable e) {
            // check if it's too late to send a fault response
            if (oconn.isCommitted()) {
                logger.error("Aborted response due to error after commit", e);
            } else {
                getProcessor().switchMessageContext();
                outCtx = (OutContext) getProcessor().getCurrentMessageContext();
                getServiceExceptionHandler().handleException(e, getProcessor(), oconn);
            }
        } finally {
            try {
                if (m_outputCompletionListener != null && outCtx != null) {
                    logger.debug("Calling output completion listener");
                    m_outputCompletionListener.onComplete(new OutputCompletionEvent(outCtx));
                }
            } finally {
                getProcessor().reset();
            }
        }
    }

    private Object invokeOperation(Object payload) throws NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, WsException {

        Operation op = getOperation(payload);
        if (op == null) {
            throw new NoSuchMethodException("No operation defined for payload type " + payload == null ? "null"
                : payload.getClass().getName());
        }

        return op.invoke(m_serviceObj, payload, getProcessor());
    }

    /**
     * Get operation for unmarshalled request body.
     * 
     * @param body unmarshalled request data
     * @return corresponding operation information
     */
    private Operation getOperation(Object body) {
        String cname = "";
        if (body != null) {
            cname = body.getClass().getName();
        }
        if (logger.isDebugEnabled()) {
	        logger.debug("Searching for operation mapped by class name '" + cname + "'");
	    }
        return (Operation) m_operationByBodyMap.get(cname);
    }

    /**
     * Returns the binding factory for the binding that contains the definitions for all objects used by the outbound
     * body payload for this service.
     * 
     * @return bindingFactory
     */
    public final IBindingFactory getOutBodyBindingFactory() {
        return m_outBodyBindingFactory;
    }

    /**
     * Returns the binding factory for the binding that contains the definitions for all objects used by the inbound
     * body payload for this service.
     * 
     * @return bindingFactory
     */
    public final IBindingFactory getInBodyBindingFactory() {
        return m_inBodyBindingFactory;
    }

    /**
     * Returns the {@link Operation}s that this service offers.
     * 
     * @return a collection of {@link Operation}
     */
    public final Collection getOperations() {
        return Collections.unmodifiableCollection(m_operationByBodyMap.values());
    }

    /**
     * Get serviceExceptionHandler.
     *
     * @return serviceExceptionHandler
     */
    protected ServiceExceptionHandler getServiceExceptionHandler() {
        return m_serviceExceptionHandler;
    }

    /**
     * Create body handlers and add them to the contexts.
     *
     * @param inCtx inbound message context
     * @param outCtx outbound message context 
     * @throws WsBindingException on error creating handlers
     */
    protected void createBodyHandlers(InContext inCtx, OutContext outCtx) throws WsBindingException {
        IBindingFactory outBodyBindingFactory = getOutBodyBindingFactory();
        if (outBodyBindingFactory != null) {
            outCtx.setBodyWriter(new MarshallingPayloadWriter(outBodyBindingFactory));
        }
    
        IBindingFactory inBodyBindingFactory = getInBodyBindingFactory();
        if (inBodyBindingFactory != null) {
            inCtx.setBodyReader(new UnmarshallingPayloadReader(inBodyBindingFactory));
        }
    }
    /**
     * Sets the message contexts on all transport options associated with this service. 
     * 
     * @param inCtx inbound message context
     * @param outCtx outbound message context
     */
    protected void setContextOnTransportOptions(InContext inCtx, OutContext outCtx) {
        for (Iterator iterator = m_transportOptionsMap.values().iterator(); iterator.hasNext();) {
            TransportOptions options = (TransportOptions) iterator.next();
            options.setMessageContexts(inCtx, outCtx);
        }
    }
   
    /**
     * Get the processor for processing the messages.
     * 
     * @return processor
     */
    protected Processor getProcessor() {
        return m_processor;
    }

   /**
    * Returns formatting options for outbound xml.
    *
    * @return xml formatting options
    */
   public XmlOptions getXmlOptions() {
       return m_xmlOptions;
   }
   
   /**
    * Returns the transport options of the specified type.
    *
    * @param optionsClass the type of transport options that are required
    * @return transport options of the specified type, or <code>null</code> if no options defined for this type of 
    * transport
    */
   public TransportOptions getTransportOptions(Class optionsClass) {
       return (TransportOptions) m_transportOptionsMap.get(optionsClass.getName());
   }

    /**
     * Returns a mapper for mapping media codes to media types.
     *
     * @return media type mapper
     */
    public MediaTypeMapper getMediaTypeMapper() {
        return m_mediaTypeMapper;
    }

    /**
     * Sets a WSDL provider that will create or retrieve the WSDL for this service.
     *
     * @param wsdlProvider WSDL provider
     */
    public abstract void setWsdlProvider(WsdlProvider wsdlProvider);

    /**
     * Returns the WSDL provider for this service. 
     *
     * @return WSDL provider  or null if no WSDL provider defined
     */
    public abstract WsdlProvider getWsdlProvider();
}
