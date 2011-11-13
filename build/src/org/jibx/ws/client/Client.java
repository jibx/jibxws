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
package org.jibx.ws.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.io.PayloadReader;
import org.jibx.ws.io.PayloadWriter;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.soap.client.SoapClient;
import org.jibx.ws.transport.Channel;
import org.jibx.ws.transport.Transport;
import org.jibx.ws.transport.TransportDirectory;
import org.jibx.ws.transport.TransportOptions;

/**
 * A generic client for connecting to services. Provides 
 * common methods that are not protocol specific. For access to protocol specific features, such as SOAP headers, refer
 * to the protocol specific subclass (for example, {@link SoapClient}).
 * 
 * @author Nigel Charman
 */
public abstract class Client
{
    private static final Log logger = LogFactory.getLog(Client.class);
    
    /** Connects the transport that will carry the messages. */
    private Channel m_channel;
    
    /** The handler that writes the body contents. */
    private PayloadWriter m_bodyWriter;

    /** The handler that reads the body contents. */
    private PayloadReader m_bodyReader;
    
    /** Internal flag to determine whether the processor needs to be rebuilt before subsequent use. */
    private boolean m_modified;
    
    /** The options to apply to the outbound XML message. */
    private MessageOptions m_messageOptions;

    /** The location of the service to connect to. */
    private final String m_serviceLocation;

    private final Transport m_transport;
    
    private TransportOptions m_transportOptions;

    /**
     * Constructor. Sets the location of the service to connect to.
     * 
     * @param location the service location
     */
    protected Client(String location) {
        if (location == null) {
            throw new NullPointerException("Service location must be non-null");
        }
        m_serviceLocation = location;
        m_transport = TransportDirectory.getTransport(m_serviceLocation);    
    }
    
    /**
     * Sets a single JiBX binding factory to be used for for marshalling and unmarshalling the body elements.  This 
     * binding factory should include bindings for any elements that are to be contained in the outbound or inbound SOAP
     * body.
     * <p>
     * For finer grain control of the different elements use {@link #setOutBodyBindingFactory(IBindingFactory)} and
     * {@link #setInBodyBindingFactory(IBindingFactory)}.
     * 
     * @param factory the binding factory to use for outbound and/or inbound SOAP bodies, and/or SOAP fault details
     * @throws WsBindingException if marshaller cannot be created due to an error with the JiBX bindings
     */
    public void setBindingFactory(IBindingFactory factory) throws WsBindingException {
        setOutBodyBindingFactory(factory);
        setInBodyBindingFactory(factory);
    }

    /**
     * Sets the JiBX binding factory for the outbound body.
     * 
     * @param factory the JiBX binding factory to use for writing the outbound body
     * @throws WsBindingException if marshaller cannot be created due to an error with the JiBX bindings
     */
    public final void setOutBodyBindingFactory(IBindingFactory factory) throws WsBindingException {
        setBodyWriter(new MarshallingPayloadWriter(factory));
    }

    /**
     * Sets the writer for the outbound body.
     * 
     * @param bodyWriter writer for outbound body
     */
    private void setBodyWriter(PayloadWriter bodyWriter) {
        m_bodyWriter = bodyWriter;
        setModified(true);
    }

    /**
     * Sets options for the encoding, XML declaration and formatting of the outbound message.
     * 
     * @param options specifies the options for the outbound message
     */
    public final void setMessageOptions(MessageOptions options) {
        try {
            closeChannel();  // will need a new channel for the new output options
        } catch (IOException e) {
            logger.warn("Unable to close existing channel", e);
        }
        m_messageOptions = new MessageOptions(options);
        setModified(true);
    }

    /**
     * Sets options for configuring the transport. 
     *
     * @param transportOptions options
     */
    public void setTransportOptions(TransportOptions transportOptions) {
        m_transportOptions = transportOptions;
        setModified(true);
    }
    
    /**
     * Sets the JiBX binding factory for the inbound body.
     * 
     * @param factory the JiBX binding factory to use for reading the inbound body
     * @throws WsBindingException if unmarshaller cannot be created due to an error with the JiBX bindings
     */
    public final void setInBodyBindingFactory(IBindingFactory factory) throws WsBindingException {
        setBodyReader(new UnmarshallingPayloadReader(factory));
    }

    /**
     * Sets the reader for the inbound body.
     * 
     * @param bodyReader reader for the inbound body
     */
    private void setBodyReader(PayloadReader bodyReader) {
        m_bodyReader = bodyReader;
        setModified(true);
    }

    /**
     * Send a request to the service and wait for the response to be returned.
     * 
     * @param request object to be marshalled to XML as body of request (may be <code>null</code>, for an empty
     * request body)
     * @return response object unmarshalled from body of response (may be <code>null</code>, for an empty response
     * body)
     * @throws IOException on error in communicating with service
     * @throws WsException on error in request processing, or a WsConfigurationException if either:
     * <ul>
     * <li><code>request</code> is non null and a binding factory or handler has not been set for the outbound body,
     * or</li>
     * <li>the response contains a non-empty body and a binding factory or handler has not been set for the inbound
     * body.</li>
     * </ul>
     */
    public abstract Object call(Object request) throws IOException, WsException;

    /**
     * Indicates whether an option has been modified that will require the Processor to be rebuilt.
     * 
     * @return <code>true</code> if modified
     */
    protected final boolean isModified() {
        return m_modified;
    }

    /**
     * Flags that an option has been modified that will require the Processor to be rebuilt.
     * 
     * @param modified set to <code>true</code> if modified
     */
    protected final void setModified(boolean modified) {
        m_modified = modified;
    }

    /**
     * Get channel.
     * 
     * @return channel
     * @throws WsConfigurationException if the service location URL specifies an unknown transport
     */
    protected final Channel getChannel() throws WsConfigurationException {
        if (m_channel == null) {
            m_channel = m_transport.buildDuplexChannel(getServiceLocation(), getTransportOptions());
        }
        
        return m_channel;
    }

    /**
     * Get writer for outbound body.
     * 
     * @return outbound body marshaller
     */
    protected final PayloadWriter getBodyWriter() {
        return m_bodyWriter;
    }

    /**
     * Get reader for inbound body.
     * 
     * @return inbound body reader
     */
    protected final PayloadReader getBodyReader() {
        return m_bodyReader;
    }

    /**
     * Get the outbound message options. If options have not been set using {@link #setMessageOptions(MessageOptions)},
     * default message options will be returned.
     * 
     * @return message options - will always be non-null
     */
    protected final MessageOptions getMessageOptions() {
        if (m_messageOptions == null) {
            m_messageOptions = new MessageOptions();
        }
        return m_messageOptions;
    }

    /**
     * Get transport options. If options have not been set using {@link #setTransportOptions(TransportOptions)}, default
     * transport options will be returned. 
     *
     * @return transport options - will always be non-null
     */
    protected final TransportOptions getTransportOptions() {
        if (m_transportOptions == null) {
            m_transportOptions = m_transport.newTransportOptions();
        }
        return m_transportOptions;
    }
    
    /**
     * Get serviceLocation.
     *
     * @return serviceLocation
     */
    protected final String getServiceLocation() {
        return m_serviceLocation;
    }
    
    /**
     * Free resources and end client usage.
     * 
     * @throws IOException on error closing channel
     */
    public final void close() throws IOException {
        closeChannel();
    }

    private void closeChannel() throws IOException {
        if (m_channel != null) {
            m_channel.close();
            m_channel = null;
        }
    }
}
