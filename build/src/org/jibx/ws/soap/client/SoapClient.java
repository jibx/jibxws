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

package org.jibx.ws.soap.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.client.Client;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.io.handler.InHandler;
import org.jibx.ws.io.handler.MarshallingOutHandler;
import org.jibx.ws.io.handler.OutHandler;
import org.jibx.ws.io.handler.UnmarshallingInHandler;
import org.jibx.ws.process.Processor;
import org.jibx.ws.soap.SoapFault;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.SoapPhase;
import org.jibx.ws.soap.SoapProcessor;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.MessageProperties;

/**
 * A client for accessing SOAP web services.
 * <p>
 * Typically, this class will be configured to use JiBX bindings for marshalling the request payload and unmarshalling
 * the response payload for example using:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * SoapClient soapClient = new SoapClient(&quot;http://mystockws.com&quot;, BindingDirectory.getFactory(Ticker.class));
 * response = soapClient.call(request);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * To configure a client with different bindings for Request and Response classes:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * SoapClient soapClient = new SoapClient(path);
 * soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Request.class));
 * soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Response.class));
 * response = soapClient.call(request);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * If a SOAP Fault occurs, the <code>call</code> method will throw a {@link SoapFaultException}. Any detail elements
 * of the SOAP fault are skipped unless handlers are configured to read the SOAP fault detail elements, see
 * {@link #addInFaultDetailsHandler(InHandler)} or {@link #addInFaultDetailsBindingFactory(IBindingFactory)}.
 * <p>
 * By default an encoding of UTF-8 is used, the outbound XML is unformatted and contains an XML declaration of
 * <code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;</code>. The encoding, XML declaration and formatting of the
 * outbound message can be overridden by calling {@link #setMessageOptions(MessageOptions)}.
 * 
 * @author Nigel Charman
 */
public final class SoapClient extends Client
{
    /** The version of the SOAP protocol. */
    private SoapProtocol m_protocol = SoapProtocol.SOAP1_1;

    /** The processor that implements the SOAP protocol. */
    private SoapProcessor m_processor;

    /** A list of {@link OutHandler}s for writing the SOAP header contents. The handlers will be called sequentially. */
    private List m_outHeaderHandlers;

    /** A list of {@link InHandler}s for reading the SOAP header contents. The handlers will be called sequentially. */
    private List m_inHeaderHandlers;

    /** A list of {@link InHandler}s for reading the SOAP fault contents. The handlers will be called sequentially. */
    private List m_inFaultDetailsHandlers;

    /** The operation name. For SOAP over HTTP the SOAP Action will be set to this value. */
    private String m_operationName;

    /** The optional SOAP encoding style to set in the SOAP message. */
    private String m_encodingStyle;

    /** An optional SOAP fault resolver. */
    private SoapFaultResolver m_soapFaultResolver;

    private InContext m_inCtx;

    private OutContext m_outCtx;

    /**
     * Create a SOAP 1<!-- -->.1 client to connect to a service at the specified location.  
     * 
     * @param location the location of the service
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public SoapClient(String location) throws WsConfigurationException {
        super(location);
    }

    /**
     * Create a SOAP 1<!-- -->.1 client to connect to a service at the specified location. The client will use the
     * specified JiBX binding factory for marshalling and unmarshalling the SOAP body and unmarshalling any SOAP fault
     * details.
     * 
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound SOAP body, inbound SOAP body and inbound SOAP
     * fault details. Bindings are only required for non-empty outbound or inbound SOAP bodies. If SOAP fault details
     * are received that do not match any of the bindings, the SOAP fault details will be skipped.
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public SoapClient(String location, IBindingFactory factory) throws WsBindingException, WsConfigurationException {
        super(location);
        setBindingFactory(factory);
    }

    /**
     * Create a SOAP 1<!-- -->.1 client to connect to a service at the specified location. The client will use the
     * specified JiBX binding factory for marshalling and unmarshalling the SOAP body and unmarshalling any SOAP fault
     * details.
     * 
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound SOAP body, inbound SOAP body and inbound SOAP
     * fault details. Bindings are only required for non-empty outbound or inbound SOAP bodies. If SOAP fault details
     * are received that do not match any of the bindings, the SOAP fault details will be skipped.
     * @param options output options
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public SoapClient(String location, IBindingFactory factory, MessageOptions options) throws WsBindingException,
        WsConfigurationException {
        this(location, factory);
        setMessageOptions(options);
    }

    /**
     * Create a SOAP client to connect to a service at the specified location. The client will use the specified JiBX
     * binding factory for marshalling and unmarshalling the SOAP body and unmarshalling any SOAP fault details.
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound SOAP body, inbound SOAP body and inbound SOAP
     * fault details. Bindings are only required for non-empty outbound or inbound SOAP bodies. If SOAP fault details
     * are received that do not match any of the bindings, the SOAP fault details will be skipped.
     * @param options output options
     * @param protocol the version of the SOAP protocol for the client to use
     * 
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public SoapClient(String location, IBindingFactory factory, MessageOptions options, SoapProtocol protocol)
        throws WsBindingException, WsConfigurationException {
        this(location, factory, options);
        setProtocol(protocol);
    }

    /**
     * Set SOAP protocol version. See {@link SoapProtocol} for supported protocols.
     * 
     * @param protocol the version of SOAP protocol
     */
    public void setProtocol(SoapProtocol protocol) {
        m_protocol = protocol;
        setModified(true);
    }

    /**
     * Sets a single JiBX binding factory to be used for for marshalling and unmarshalling the SOAP body and
     * unmarshalling any SOAP fault details.  This binding factory should include bindings for any elements that are to
     * be contained in the outbound or inbound SOAP body (if any) and in custom SOAP fault details (if any).
     * <p>
     * For finer grain control of the different elements use {@link #setOutBodyBindingFactory(IBindingFactory)},
     * {@link #setInBodyBindingFactory(IBindingFactory)} and {@link #addInFaultDetailsBindingFactory(IBindingFactory)}.
     * 
     * @param factory the binding factory to use for outbound and/or inbound SOAP bodies, and/or SOAP fault details
     * @throws WsBindingException if marshaller cannot be created due to an error with the JiBX bindings
     */
    public void setBindingFactory(IBindingFactory factory) throws WsBindingException {
        setOutBodyBindingFactory(factory);
        setInBodyBindingFactory(factory);
        addInFaultDetailsBindingFactory(factory);
        setModified(true);
    }

    /**
     * Sets the name of the operation to be called. For SOAP over HTTP the SOAP Action will be set to this value. This
     * is not normally required for document/literal services since the element type of the marshalled request object is
     * expected to identify the actual operation to be performed. The operation name should only be set if you have a
     * specific need to set the SOAPAction header.
     * 
     * @param operationName the operation name
     */
    public void setOperationName(String operationName) {
        m_operationName = operationName;
    }

    /**
     * Sets the optional <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383495">SOAP encodingStyle</a>
     *  attribute on the SOAP envelope.  If this method is not called, or the encodingStyle parameter is set to 
     *  <code>null</code>, then no encodingStyle attribute is added.
     *
     * @param encodingStyle value to set in encodingStyle attribute
     */
    public void setSoapEncodingStyle(String encodingStyle) {
        m_encodingStyle = encodingStyle;
    }
    
    /**
     * Sets the outbound header handlers. Replaces existing header handlers, if any.
     * 
     * @param outHeaderHandlers the list of {@link OutHandler}
     * @throws WsConfigurationException if the list contains objects of type other than {@link OutHandler}s.
     */
    public void setOutHeaderHandlers(List outHeaderHandlers) throws WsConfigurationException {
        for (Iterator iter = outHeaderHandlers.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof OutHandler)) {
                throw new WsConfigurationException(
                    "The list passed to setOutHeaderHandlers must only contain objects of type OutHandler");
            }
        }
        m_outHeaderHandlers = outHeaderHandlers;
        setModified(true);
    }

    /**
     * Adds the specified header handler to the outbound processing. Header handlers are called sequentially in the
     * order that they have been added.
     * 
     * @param headerHandler the handler that will write the header
     */
    public void addOutHeaderHandler(OutHandler headerHandler) {
        if (m_outHeaderHandlers == null) {
            m_outHeaderHandlers = new ArrayList();
        }
        m_outHeaderHandlers.add(headerHandler);
        setModified(true);
    }

    /**
     * Adds the specified object as a header to the outbound message. For this to work, there must be exactly one JiBX
     * binding compiled for the class of the object. An appropriate handler is created that will marshal the 
     * specified object. 
     * 
     * @param obj the object to include in the header
     * @throws WsException if binding cannot be found, or other error
     */
    public void addOutHeader(Object obj) throws WsException {
        addOutHeaderHandler(new MarshallingOutHandler(obj));
    }

    /**
     * Removes all outbound header handlers.
     */
    public void removeOutHeaderHandlers() {
        m_outHeaderHandlers = null;
        setModified(true);
    }
    
    /**
     * Sets the inbound header handlers. Replaces existing header handlers, if any.
     * 
     * @param inHeaderHandlers the list of {@link InHandler}
     * @throws WsConfigurationException if the list contains objects of type other than {@link InHandler}s.
     */
    public void setInHeaderHandlers(List inHeaderHandlers) throws WsConfigurationException {
        for (Iterator iter = inHeaderHandlers.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof InHandler)) {
                throw new WsConfigurationException(
                    "The list passed to setInHeaderHandlers must only contain objects of type InHandler");
            }
        }
        m_inHeaderHandlers = inHeaderHandlers;
        setModified(true);
    }

    /**
     * Adds the specified header handler to the inbound processing. For each SOAP header, the header handlers are
     * invoked sequentially in the order that they have been added, until a handler returns true from the
     * {@link InHandler#invoke(InContext, org.jibx.runtime.IXMLReader)} method, when the processing of that header is
     * deemed to be complete, and processing carries on with the next header.
     * 
     * @param headerHandler the handler that will write the header
     */
    public void addInHeaderHandler(InHandler headerHandler) {
        if (m_inHeaderHandlers == null) {
            m_inHeaderHandlers = new ArrayList();
        }
        m_inHeaderHandlers.add(headerHandler);
        setModified(true);
    }

    /**
     * Removes all inbound header handlers.
     */
    public void removeInHeaderHandlers() {
        m_inHeaderHandlers = null;
        setModified(true);
    }
    
    /**
     * Adds a JiBX binding factory for the inbound SOAP fault details. Only required if SOAP fault details are to be
     * read. Handlers will be called sequentially. The first handler that can read a particular fault detail will set
     * the details on the {@link SoapFault} and all other handlers will be skipped.
     * 
     * 
     * @param factory the JiBX binding factory to use for reading the inbound SOAP body
     * @throws WsBindingException if unmarshaller cannot be created due to an error with the JiBX bindings
     */
    public void addInFaultDetailsBindingFactory(IBindingFactory factory) throws WsBindingException {
        addInFaultDetailsHandler(new UnmarshallingInHandler(factory));
    }

    /**
     * Adds a handler to use for reading the SOAP fault details. Only required if SOAP fault details are to be read.
     * Handlers will be called sequentially. The first handler that can read a particular fault detail will set the
     * details on the {@link SoapFault} and all other handlers will be skipped.
     * 
     * @param inFaultDetailsHandler the handler to use for reading the SOAP fault details
     */
    public void addInFaultDetailsHandler(InHandler inFaultDetailsHandler) {
        if (m_inFaultDetailsHandlers == null) {
            m_inFaultDetailsHandlers = new ArrayList();
        }
        m_inFaultDetailsHandlers.add(inFaultDetailsHandler);
        setModified(true);
    }

    /**
     * Removes all inbound fault details handlers.
     */
    public void removeInFaultDetailsHandlers() {
        m_inFaultDetailsHandlers = null;
        setModified(true);
    }
    
    /**
     * Removes all header handlers and inbound fault details handlers.
     */
    public void removeAllHandlers() {
        removeOutHeaderHandlers();
        removeInHeaderHandlers();
        removeInFaultDetailsHandlers();
    }
    
    /**
     * Sets the SOAP fault resolver. If set to a non null value, this resolver will be called in place of the default
     * default soap fault handling, when a SOAP fault occurs.
     * 
     * @param soapFaultResolver the resolver
     */
    public void setSoapFaultResolver(SoapFaultResolver soapFaultResolver) {
        this.m_soapFaultResolver = soapFaultResolver;
    }

    /**
     * Call the service with specified operation name.
     * 
     * @param request object to be marshalled to XML as body of request (may be <code>null</code>, for an empty
     * request body)
     * 
     * @return response object unmarshalled from body of response (may be <code>null</code>, for an empty response
     * body)
     * 
     * @throws IOException on error in communicating with service
     * @throws WsException on error in SOAP request processing, or a WsConfigurationException if either:
     * <ul>
     * <li>the service location has not been set before invoking this method, or</li>
     * <li><code>request</code> is non null and a binding factory or handler has not been set for the outbound SOAP
     * body, or</li>
     * <li>the SOAP response contains a non-empty SOAP body and a binding factory or handler has not been set for the
     * inbound SOAP body.</li>
     * </ul>
     * @throws SoapFaultException if the service returns a SOAP fault, and a custom {@link SoapFaultResolver} has not
     * been set.
     */
    public Object call(Object request) throws IOException, WsException {
        if (getBodyWriter() == null && request != null) {
            throw new WsConfigurationException(
                "Binding factory or handler must be defined for the outbound SOAP body");
        }

        Processor processor = getProcessor();
        m_outCtx.setBody(request);

        MessageProperties msgProps = m_protocol.buildMessageProperties(m_operationName, getMessageOptions());
        DuplexConnection duplex = getChannel().getDuplex(msgProps, getMessageOptions().getXmlOptions());
        processor.invoke(duplex.getOutbound(), duplex.getInbound());

        Object body = m_inCtx.getBody();
        if (body instanceof SoapFault) {
            return handleFault((SoapFault) body);
        }
        
        return m_inCtx.getBody();
    }

    private Object handleFault(SoapFault fault) {
        if (m_soapFaultResolver != null) {
            return m_soapFaultResolver.handleFault(fault);
        }
        throw new SoapFaultException(fault);
    }

    /**
     * Returns a SOAP processor configured according to the current options set on the client. If no options have
     * changed since the last call to this method, the current processor will be returned.
     * 
     * @return SoapProcessor
     * @throws WsException
     */
    private Processor getProcessor() throws WsException {
        if (m_processor != null) {
            m_processor.reset();
        }
        if (m_processor == null || isModified()) {
            m_outCtx = new OutContext();
            if (m_outHeaderHandlers != null) {
                for (Iterator iter = m_outHeaderHandlers.iterator(); iter.hasNext();) {
                    OutHandler handler = (OutHandler) iter.next();
                    m_outCtx.addHandler(SoapPhase.HEADER, handler);
                }
            }
            if (getBodyWriter() != null) {
                m_outCtx.setBodyWriter(getBodyWriter());
            }
            m_inCtx = new InContext();
            if (m_inHeaderHandlers != null) {
                for (Iterator iter = m_inHeaderHandlers.iterator(); iter.hasNext();) {
                    InHandler handler = (InHandler) iter.next();
                    m_inCtx.addHandler(SoapPhase.HEADER, handler);
                }
            }
            if (getBodyReader() != null) {
                m_inCtx.setBodyReader(getBodyReader());
            }
            if (m_inFaultDetailsHandlers != null) {
                for (Iterator iter = m_inFaultDetailsHandlers.iterator(); iter.hasNext();) {
                    InHandler handler = (InHandler) iter.next();
                    m_inCtx.addHandler(SoapPhase.BODY_FAULT, handler);
                }
            }
            m_processor = (SoapProcessor) m_protocol.createProcessor(ExchangeContext.createOutInExchange(m_outCtx, m_inCtx));
            m_processor.setSoapEncodingStyle(m_encodingStyle);
            setModified(false);
        }
        return m_processor;
    }
}
