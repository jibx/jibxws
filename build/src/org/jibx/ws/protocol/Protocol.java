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

package org.jibx.ws.protocol;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.client.Client;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.process.Processor;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;

/**
 * Provides an interface for creating a processor specific to a messaging protocol.
 * 
 * @author Nigel Charman
 */
public interface Protocol
{
    /**
     * Returns a processor for this protocol.
     * 
     * @return protocol specific processor
     */
    Processor createProcessor();

    /**
     * Returns a processor for this protocol, configured according to the specified exchange context.
     * 
     * @param exchangeContext provides the context for the message exchange that the processor is to perform
     * 
     * @return protocol specific processor
     */
    Processor createProcessor(ExchangeContext exchangeContext);

    /**
     * Create a client to connect to a service at the specified location using this protocol. The client will use the
     * specified JiBX binding factory for marshalling and unmarshalling the message body. The binding factory may also
     * be used for protocol specific details, such as unmarshalling any SOAP fault details. See the relevant subclass
     * for details.
     * 
     * @param location the location of the service
     * @param factory the factory containing bindings for the outbound and inbound message body. Bindings are only
     * required for non-empty outbound or inbound bodies. May also be used for protocol specific details. See the
     * relevant <code>WsClient</code> subclass for details.
     * @param options options for outbound message
     * @throws WsConfigurationException on configuration exception, for instance the location is invalid.
     * @throws WsBindingException if client cannot be created due to an error with the JiBX bindings
     * @return the client
     */
    Client createClient(String location, IBindingFactory factory, MessageOptions options)
        throws WsBindingException, WsConfigurationException;

    /**
     * Returns the protocol name.
     * 
     * @return name of the protocol
     */
    String getName();

    /**
     * Returns a factory that creates services for this protocol.
     * 
     * @return service factory
     */
    ServiceFactory getServiceFactory();

    /**
     * Returns a mapper for mapping media type code to media types for this protocol.
     * @return media type mapper
     */
    MediaTypeMapper getMediaTypeMapper();
    
    /**
     * Returns message specific properties for an outbound message.  Some properties are protocol specific, for 
     * example SOAP 1.2 bindings dictate different media types than SOAP 1.1.   
     * 
     * @param opname operation name (<code>null</code> if unspecified)
     * @param msgOptions options for media types of the message
     * @return properties protocol specific message properties
     * @throws WsConfigurationException on configuration error
     */
    MessageProperties buildMessageProperties(String opname, MessageOptions msgOptions)
        throws WsConfigurationException;

    /**
     * Get the operation name for a request.
     * 
     * @param conn input connection used for request
     * @return operation name, or <code>null</code> if not supplied
     */
    String getOperationName(InConnection conn);
}
