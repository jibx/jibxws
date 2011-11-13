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

package org.jibx.ws.pox.client;

import java.io.IOException;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.client.Client;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.pox.PoxProcessor;
import org.jibx.ws.pox.PoxProtocol;
import org.jibx.ws.process.Processor;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.MessageProperties;

/**
 * A client for invoking services using Plain Old XML (POX) messages.
 * 
 * @author Nigel Charman
 */
public final class PoxClient extends Client
{
    private PoxProcessor m_processor;
    private OutContext m_outCtx;
    private InContext m_inCtx;

    /**
     * Create a POX client to connect to a service at the specified location. 
     * 
     * @param location the location of the service
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public PoxClient(String location) throws WsConfigurationException {
        super(location);
    }
    
    /**
     * Create a POX client to connect to a service at the specified location. The client will use the specified JiBX
     * binding factory for marshalling and unmarshalling the message body.
     * 
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound and inbound message body. Bindings are only
     * required for non-empty outbound or inbound bodies.
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     */
    public PoxClient(String location, IBindingFactory factory) throws WsBindingException, WsConfigurationException {
        super(location);
        setBindingFactory(factory);
    }

    /**
     * Create a POX client to connect to a service at the specified location. The client will use the specified JiBX
     * binding factory for marshalling and unmarshalling the message body.
     * 
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound and inbound message body. Bindings are only
     * required for non-empty outbound or inbound bodies.
     * @param options output options
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     */
    public PoxClient(String location, IBindingFactory factory, MessageOptions options) throws WsBindingException,
        WsConfigurationException {
        this(location, factory);
        setMessageOptions(options);
    }

    /**
     * {@inheritDoc}
     */
    public Object call(Object request) throws IOException, WsException {
        if (getBodyWriter() == null && request != null) {
            throw new WsConfigurationException(
                "Binding factory or handler must be defined for the outbound message body");
        }

        Processor processor = getProcessor();

        m_outCtx.setBody(request);

        String opname = null;
        MessageProperties msgProps = PoxProtocol.INSTANCE.buildMessageProperties(opname, getMessageOptions());
        DuplexConnection duplex = getChannel().getDuplex(msgProps, getMessageOptions().getXmlOptions());
        processor.invoke(duplex.getOutbound(), duplex.getInbound());

        return m_inCtx.getBody();
    }

    /**
     * Returns a POX processor configured according to the current options set on the client. If no options have changed
     * since the last call to this method, the current processor will be returned.
     * 
     * @return PoxProcessor
     * @throws WsException
     */
    private Processor getProcessor() throws WsException {
        if (m_processor != null) {
            m_processor.reset();
        }
        if (m_processor == null || isModified()) {
            m_outCtx = new OutContext();
            if (getBodyWriter() != null) {
                m_outCtx.setBodyWriter(getBodyWriter());
            }

            m_inCtx = new InContext();
            if (getBodyReader() != null) {
                m_inCtx.setBodyReader(getBodyReader());
            }
            m_processor = (PoxProcessor) PoxProtocol.INSTANCE.createProcessor(ExchangeContext.createOutInExchange(
                m_outCtx, m_inCtx));
        }
        return m_processor;
    }
    
    

}
