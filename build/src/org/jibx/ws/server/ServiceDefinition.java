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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.transport.OutputCompletionListener;
import org.jibx.ws.util.Utility;

/**
 * Defines a service to be invoked by JiBX/WS.
 * <p>
 * After setting the required properties on this class, the {@link #init()} method must be called to initialize this
 * class.
 * <p>
 * When using JiBX to configure the service, this class is populated from the XML service definition document by JiBX
 * unmarshalling and the <code>init()</code> method is automatically called after the properties have been set.
 * 
 * @author Dennis M. Sosnoski
 */
public final class ServiceDefinition
{
    /** The name of the protocol to use for the service. */
    private String m_protocolName;
    
    /** Schema file name, with name also used as default for base name. */
//    private String m_schemaName;

    /** Base name, used for service, binding, port type, and port. */
//    private String m_baseName;

    /** Service name, generated if not supplied. */
    private String m_serviceName;

    /** Binding name (in SOAP terms), generated if not supplied. */
//    private String m_soapBindingName;

    /** Port type name, generated if not supplied. */
//    private String m_portTypeName;

    /** Port name, generated if not supplied. */
//    private String m_portName;

    /** Namespace for WSDL definitions. */
//    private String m_wsdlNamespace;

    /** Actual service object. */
    private Object m_serviceObject;

    /** Actual service class. */
    private String m_serviceClassName;
    private Class m_serviceClass;

    /** Exception handler object. */
    private ServiceExceptionHandler m_serviceExceptionHandlerObject;

    /** Exception handler class. */
    private Class m_serviceExceptionHandlerClass;

    /** Binding factory for inbound request body, only needed if multiple bindings to message classes. */
    private IBindingFactory m_inBodyBindingFactory;

    /** Binding factory for outbound response body, only needed if multiple bindings to message classes. */
    private IBindingFactory m_outBodyBindingFactory;

    /** Handlers used for reading and writing headers. */
    private List m_hdefs;

    /** Operations included in service interface. */
    private List m_opdefs;

    /** Whether to include stack trace in default fault handling. */
    private boolean m_includeStackTraceOnFault;

    /** Output Completion Listener object. */
    private OutputCompletionListener m_outputCompletionListenerObject;

    /** Output Completion Listener class. */
    private Class m_outputCompletionListenerClass;

    /** Options for formatting outbound XML. */
    private XmlOptions m_xmlOptions;

    /** List of {@link TransportOptionsDefinition}. */
    private List m_transportOptDefs;

    /** Path to WSDL file. */
    private String m_wsdlFilepath;

    /** Whether to transform WSDL location using WSDL request. */
    private boolean m_wsdlLocationTransform;
    
    /**
     * Method called after unmarshalling is complete.
     * 
     * @param ctx unmarshalling context
     * @throws JiBXException if error in configuration
     */
    protected void postset(IUnmarshallingContext ctx) throws JiBXException {
        try {
            init();
        } catch (WsConfigurationException e) {
            throw new JiBXException("Error initializing ServiceDefinition", e);
        }
    }

    /**
     * This method must be called after all properties have been set. This fills in any names not supplied by the
     * binding and initializes the contained definition objects (eg. handler definitions).
     * 
     * @throws WsConfigurationException on configuration error
     */
    public void init() throws WsConfigurationException {
        // initialize all of the handler definitions
        for (int i = 0; i < getHandlerDefinitions().size(); i++) {
            HandlerDefinition hdef = (HandlerDefinition) getHandlerDefinitions().get(i);
            hdef.init();
        }

        checkForDuplicateTransportOptionDefinitions();
        for (Iterator iterator = getTransportOptionsDefinitions().iterator(); iterator.hasNext();) {
            TransportOptionsDefinition todef = (TransportOptionsDefinition) iterator.next();
            todef.init();
        }
        
        // handle any names not defined by configuration
//        if (m_baseName == null) {
//            if (m_schemaName != null) {
//                String name = m_schemaName;
//                int split = name.lastIndexOf('.');
//                if (split > 0) {
//                    name = name.substring(0, split);
//                }
//                m_baseName = name;
//            }
//        }
//        if (m_serviceName == null) {
//            m_serviceName = m_baseName + "Service";
//        }
//        if (m_soapBindingName == null) {
//            m_soapBindingName = m_baseName + "Binding";
//        }
//        if (m_portTypeName == null) {
//            m_portTypeName = m_baseName + "Interface";
//        }
//        if (m_portName == null) {
//            m_portName = m_baseName;
//        }
        if (m_serviceName == null) {
            m_serviceName = "Service";
        }

        if (getProtocolName() == null) {
            setProtocolName("SOAP1.1");
        }
    }

    /** 
     * Checks that only one {@link TransportOptionsDefinition} instance exists for each 
     * {@link TransportOptionsDefinition} class.
     */
    private void checkForDuplicateTransportOptionDefinitions() {
        if (m_transportOptDefs != null) {
            Set definedOptions = new HashSet();
            for (Iterator iterator = m_transportOptDefs.iterator(); iterator.hasNext();) {
                TransportOptionsDefinition def = (TransportOptionsDefinition) iterator.next();
                String type = def.getClass().getName();
                if (definedOptions.contains(type)) {
                    throw new IllegalArgumentException("Duplicate transport options definition of type " + type);
                }
                definedOptions.add(type);
            }
        }
    }
    
//    /**
//     * Sets the base name used to derive other names for this service. If supplied, this replaces the schema file name
//     * as the base for generating other names used in the WSDL for the service.
//     * 
//     * @param baseName base name
//     */
//    public void setBaseName(String baseName) {
//        m_baseName = baseName;
//    }

    /**
     * Set name of protocol to use for service.
     * 
     * @param name protocol name
     */
    public void setProtocolName(String name) {
        m_protocolName = name;
    }

    /**
     * Get the name of protocol to use for this service.
     * 
     * @return protocol name
     */
    public String getProtocolName() {
        return m_protocolName;
    }

//    /**
//     * Sets the name of the XML schema to be used for WSDL generation.
//     * 
//     * @param schemaName schema name
//     */
//    public void setSchemaName(String schemaName) {
//        m_schemaName = schemaName;
//    }
//
//    /**
//     * Get name of XML schema to be used for WSDL generation.
//     * 
//     * @return schema name
//     */
//    public String getSchemaName() {
//        return m_schemaName;
//    }
//
//    /**
//     * Sets the namespace URI for WSDL definitions. This is required if WSDL generation is to be supported, but is
//     * optional and unused otherwise.
//     * 
//     * @param wsdlNamespace the namespace
//     */
//    public void setWsdlNamespace(String wsdlNamespace) {
//        m_wsdlNamespace = wsdlNamespace;
//    }
//
//    /**
//     * Get namespace for generated WSDL.
//     * 
//     * @return schema name
//     */
//    public String getWsdlNamespace() {
//        return m_wsdlNamespace;
//    }

    /**
     * Sets the number of spaces to indent output XML. The default is to suppress all indentation and generate XML with
     * no added spaces or line breaks. Alternatively, for more control over output format, use
     * {@link #setXmlOptions(XmlOptions)}.
     * 
     * @param indentCount number of spaces to use
     */
    public void setIndentCount(int indentCount) {
        getXmlOptions().setIndentCount(indentCount);
    }

    /**
     * Sets the options for formatting of the outbound XML message for the service.
     * 
     * @param options message options
     */
    public void setXmlOptions(XmlOptions options) {
        m_xmlOptions = options;
    }

    /**
     * Returns the formatting options for outbound XML. If no options have been set, this will create default options.
     *  
     * @return XML formatting options 
     */
    public XmlOptions getXmlOptions() {
        if (m_xmlOptions == null) {
            m_xmlOptions = new XmlOptions();
        }
        return m_xmlOptions;
    }

    
    /**
     * Sets the optional name to be used for this service<!-- in generated WSDL-->. If not supplied, a name is generated
     * by appending "Service" to the base name.
     * 
     * @param serviceName the service name
     */
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    /**
     * Get service name.
     * 
     * @return service name
     */
    public String getServiceName() {
        return m_serviceName;
    }

//    /**
//     * Sets the optional name to be used for the service binding in generated WSDL. If not supplied, a name is 
//     * generated by appending "Binding" to the base name.
//     * 
//     * @param soapBindingName the binding name
//     */
//    public void setSoapBindingName(String soapBindingName) {
//        m_soapBindingName = soapBindingName;
//    }
//
//    /**
//     * Get SOAP binding name.
//     * 
//     * @return SOAP binding name
//     */
//    public String getSoapBindingName() {
//        return m_soapBindingName;
//    }
//
//    /**
//     * Sets the optional name to be used for the service port type in generated WSDL. If not supplied, a name is
//     * generated by appending "Interface" to the base name.
//     * 
//     * @param portTypeName the port type name
//     */
//    public void setPortTypeName(String portTypeName) {
//        m_portTypeName = portTypeName;
//    }
//
//    /**
//     * Get port type name.
//     * 
//     * @return port type name
//     */
//    public String getPortTypeName() {
//        return m_portTypeName;
//    }
//
//    /**
//     * Sets the optional name to be used for the service port in generated WSDL. If not supplied, the base name is 
//     * used as the port name.
//     * 
//     * @param portName port name
//     */
//    public void setPortName(String portName) {
//        m_portName = portName;
//    }
//
//    /**
//     * Get port name.
//     * 
//     * @return port name
//     */
//    public String getPortName() {
//        return m_portName;
//    }

    /**
     * Get service object.
     * 
     * @return service object
     */
    public Object getServiceObject() {
        return m_serviceObject;
    }

    /**
     * Sets the object to be used for processing requests for this service. The object will potentially be used by
     * multiple service instances, so the methods that implement the service operations must be thread safe.
     * Alternatively, if using the Spring Framework, the object can use a scoped proxy to create new object instances
     * for the desired object scope.
     * <p>
     * Either this method or {@link #setServiceClassName(String)} must be called.
     * 
     * @param serviceObject the service service object
     */
    public void setServiceObject(Object serviceObject) {
        m_serviceObject = serviceObject;
    }

    /**
     * Sets the fully qualified name of class used to process requests for this service. All operations defined for the
     * service must be implemented by methods of this class. A separate instance of the specified class will be
     * constructed for each service instance, unless all of the operations use static methods, in which case only a
     * single instance of the specified class will be constructed.
     * <p>
     * Either this method or {@link #setServiceObject(Object)} must be called.
     * 
     * @param serviceClassName the full name of the service class
     */
    public void setServiceClassName(String serviceClassName) {
        m_serviceClassName = serviceClassName;
    }

    /**
     * Get service class information.
     * 
     * @return service class information
     * @throws WsConfigurationException if service class unavailable
     */
    public Class getServiceClass() throws WsConfigurationException {
        if (m_serviceClass == null) {
            if (m_serviceObject != null) {
                m_serviceClass = m_serviceObject.getClass();
            } else {
                m_serviceClass = Utility.loadClass(m_serviceClassName);
                if (m_serviceClass == null) {
                    throw new WsConfigurationException("Service class " + m_serviceClassName
                        + " not found in classpath");
                }
            }
        }
        return m_serviceClass;
    }

    /**
     * Get fault handler object.
     * 
     * @return fault handler object
     */
    public ServiceExceptionHandler getServiceExceptionHandlerObject() {
        return m_serviceExceptionHandlerObject;
    }

    /**
     * Sets the object to be used for handling exceptions in processing the message or executing the service method. 
     * The object will potentially be used by multiple service instances, so the methods must be thread safe.
     * Alternatively, if using the Spring Framework, the object can use a scoped proxy to create new object instances
     * for the desired object scope.
     * <p>
     * Either this method or {@link #setServiceExceptionHandlerClassName(String)} must be called.
     * 
     * @param serviceExceptionHandler the service exception handler object
     */
    public void setServiceExceptionHandlerObject(ServiceExceptionHandler serviceExceptionHandler) {
        m_serviceExceptionHandlerObject = serviceExceptionHandler;
    }

    /**
     * Sets the fully qualified name of class used for handling faults. A separate instance of the specified class will 
     * be constructed for each service instance.
     * <p>
     * Either this method or {@link #setServiceExceptionHandlerObject(ServiceExceptionHandler)} must be called.
     * 
     * @param serviceExceptionHandlerClassName the full name of the fault handler class
     * @throws WsConfigurationException if fault handler class unavailable
     */
    public void setServiceExceptionHandlerClassName(String serviceExceptionHandlerClassName) 
            throws WsConfigurationException {
        
        if (serviceExceptionHandlerClassName != null) {
            m_serviceExceptionHandlerClass = Utility.loadClass(serviceExceptionHandlerClassName);
            if (m_serviceExceptionHandlerClass == null) {
                throw new WsConfigurationException("Fault handler class " + serviceExceptionHandlerClassName
                    + " not found in classpath");
            }
        }
    }

    /**
     * Get fault handler class information.
     * 
     * @return fault handler class information
     */
    public Class getServiceExceptionHandlerClass() {
        return m_serviceExceptionHandlerClass;
    }

    /**
     * Calls {@link #setBindingFactory(IBindingFactory)} with the factory retrieved from the specified locator.
     * 
     * @param locator the locator for the binding factory to use for all request and response message bodies
     * @throws WsBindingException if unable to find binding in the directory
     * @throws WsConfigurationException if either package or binding name are not set
     */
    private void setBindingLocator(BindingLocator locator) throws WsBindingException, WsConfigurationException {
        if (locator != null) {
            setBindingFactory(locator.getBindingFactory());
        }
    }

    /**
     * Calls {@link #setInBodyBindingFactory(IBindingFactory)} with the factory retrieved from the specified locator.
     * 
     * @param locator the locator for the binding factory to use for all request message bodies
     * @throws WsBindingException if unable to find binding in the directory
     * @throws WsConfigurationException if either package or binding name are not set
     */
    private void setInBodyBindingLocator(BindingLocator locator) throws WsBindingException, WsConfigurationException {
        if (locator != null) {
            setInBodyBindingFactory(locator.getBindingFactory());
        }
    }

    /**
     * Calls {@link #setOutBindingFactory(IBindingFactory)} with the factory retrieved from the specified locator.
     * 
     * @param locator the locator for the binding factory to use for all response message bodies
     * @throws WsBindingException if unable to find binding in the directory
     * @throws WsConfigurationException if either package or binding name are not set
     */
    private void setOutBodyBindingLocator(BindingLocator locator) throws WsBindingException, WsConfigurationException {
        if (locator != null) {
            setOutBodyBindingFactory(locator.getBindingFactory());
        }
    }

    /**
     * Sets the optional JiBX binding factory used for the body of the request and response messages exchanged by
     * operations. For finer-grain control, call either or both of {@link #setInBodyBindingFactory(IBindingFactory)} and
     * {@link #setOutBodyBindingFactory(IBindingFactory)}.
     * <p>
     * If no binding factory is specified, JiBX/WS assumes that there is a single binding factory that includes bindings
     * for all of the possible request and response message bodies, and will look up this factory based on the class of
     * one of the request or response message bodies.
     * 
     * @param factory the binding factory to use for all request and response message bodies
     */
    public void setBindingFactory(IBindingFactory factory) {
        setInBodyBindingFactory(factory);
        setOutBodyBindingFactory(factory);
    }

    /**
     * Sets the optional JiBX binding factory used for the request message bodies received by operations.
     * 
     * @param factory the binding factory to use for all request message bodies
     */
    public void setInBodyBindingFactory(IBindingFactory factory) {
        m_inBodyBindingFactory = factory;
    }

    /**
     * Returns the optional JiBX binding factory used for the request message bodies received by operations.
     * 
     * @return the binding factory to use for all request message bodies
     */
    public IBindingFactory getInBodyBindingFactory() {
        return m_inBodyBindingFactory;
    }

    /**
     * Sets the optional JiBX binding factory used for the response message bodies sent by operations. 
     * 
     * @param factory the binding factory to use for all response message bodies
     */
    public void setOutBodyBindingFactory(IBindingFactory factory) {
        m_outBodyBindingFactory = factory;
    }

    /**
     * Returns the optional JiBX binding factory used for the response message bodies sent by operations.
     * 
     * @return the binding factory to use for all response message bodies
     */
    public IBindingFactory getOutBodyBindingFactory() {
        return m_outBodyBindingFactory;
    }
    
    /**
     * Sets the definitions of the handlers for the service.
     * 
     * @param hdefs a list of {@link HandlerDefinition}
     */
    public void setHandlerDefinitions(List hdefs) {
        m_hdefs = hdefs;
    }

    /**
     * Get handler definitions.
     * 
     * @return list of {@link HandlerDefinition}s
     */
    public List getHandlerDefinitions() {
        if (m_hdefs == null) {
            return Collections.EMPTY_LIST;
        }
        return m_hdefs;
    }

    /**
     * Sets the operations to be made available for the service. Each operation uses an associated method within the
     * service class specified by the {@link #setServiceClassName(String)} or {@link #setServiceObject(Object)} methods.
     * <p>
     * With the doc/lit style of web services supported by JiBX/WS the input message element name always determines the
     * particular operation to be performed. JiBX/WS actually finds the input message element name corresponding to each
     * operation by doing a reverse lookup of the methods in the service class. Since only one type of object can be
     * associated with an element, there's a fixed linkage between the element name and the object type. This means that
     * the type of the input parameter used for each operation method within a service must be unique.
     * 
     * @param opdefs a list of {@link OperationDefinition}
     */
    public void setOperationDefinitions(List opdefs) {
        m_opdefs = opdefs;
    }

    /**
     * Get operation definitions.
     * 
     * @return list of {@link OperationDefinition}s
     */
    public List getOperationDefinitions() {
        return m_opdefs;
    }

    /**
     * Sets whether a stack trace should be included when a SOAP fault is created as the result of an unhandled error.
     * 
     * @param includeStackTraceOnFault set to <code>true</code> if a stack trace should be created, <code>false</code>
     * otherwise.
     */
    public void setIncludeStackTraceOnFault(boolean includeStackTraceOnFault) {
        m_includeStackTraceOnFault = includeStackTraceOnFault;
    }

    /**
     * Returns whether a stack trace should be included when a SOAP fault is created as the result of an unhandled
     * error.
     * 
     * @return <code>true</code> if a stack trace should be created, <code>false</code> otherwise.
     */
    public boolean getIncludeStackTraceOnFault() {
        return m_includeStackTraceOnFault;
    }
    
    /**
     * Sets the fully qualified name of class to be notified of the completion of output. A separate instance of the 
     * specified class will be constructed for each service instance.
     * 
     * @param outputCompletionListenerClassName the full name of the listener class
     * @throws WsConfigurationException if listener class unavailable
     * @see #setOutputCompletionListener(OutputCompletionListener)
     */
    public void setOutputCompletionListenerClassName(String outputCompletionListenerClassName) 
            throws WsConfigurationException {
        
        if (outputCompletionListenerClassName != null) {
            m_outputCompletionListenerClass = Utility.loadClass(outputCompletionListenerClassName);
            if (m_outputCompletionListenerClass == null) {
                throw new WsConfigurationException("Output Listener class " + outputCompletionListenerClassName
                    + " not found in classpath");
            }
        }
    }

    /**
     * Sets the object to be notified of the completion of output. 
     * 
     * @param listener the listener object to be notified
     * @see #setOutputCompletionListenerClassName(String)
     */
    public void setOutputCompletionListener(OutputCompletionListener listener) {
        this.m_outputCompletionListenerObject = listener;
    }

    /**
     * Get outputCompletionListenerObject.
     *
     * @return outputCompletionListenerObject
     */
    public OutputCompletionListener getOutputCompletionListenerObject() {
        return m_outputCompletionListenerObject;
    }

    /**
     * Get outputCompletionListenerClass.
     *
     * @return outputCompletionListenerClass
     */
    public Class getOutputCompletionListenerClass() {
        return m_outputCompletionListenerClass;
    }

    /**
     * Sets the definition of options that are specific to a particular transport. At most one definition may be 
     * supplied for each transport.
     * 
     * @param definitions list of {@link TransportOptionsDefinition}. The list must contain no more than one 
     * <code>TransportOptionsDefinition</code> object for each <code>TransportOptionsDefinition</code> class.
     * @throws IllegalArgumentException if a duplicate transport options definition is found     
     */
    public void setTransportOptionsDefinitions(List definitions) {
        m_transportOptDefs = definitions;
    }

    /**
     * Gets a list of all defined {@link TransportOptionsDefinition}s.
     * 
     * @return the transport options definitions that have been defined, or an empty list if none have been defined. 
     */
    protected Collection getTransportOptionsDefinitions() {
        if (m_transportOptDefs == null) {
            return Collections.EMPTY_LIST;
        }
        return m_transportOptDefs;
    }

    /**
     * Sets the path to an existing WSDL file.
     * 
     * @param wsdlFilepath path to WSDL file
     */
    public void setWsdlFilepath(String wsdlFilepath) {
        m_wsdlFilepath = wsdlFilepath;
    }

    /**
     * Gets the path to existing WSDL file.
     *
     * @return path to WSDL file
     */
    protected String getWsdlFilepath() {
        return m_wsdlFilepath;
    }

    /**
     * Sets whether WSDL locations should be transformed using the location of the WSDL request. 
     * 
     * @param transform set to <code>true</code> if a location should be transformed, <code>false</code>
     * otherwise.
     */
    public void setWsdlLocationTransform(boolean transform) {
        m_wsdlLocationTransform = transform;
    }

    /**
     * Get wsdlLocationTransform.
     *
     * @return wsdlLocationTransform
     */
    protected boolean getWsdlLocationTransform() {
        return m_wsdlLocationTransform;
    }
}
