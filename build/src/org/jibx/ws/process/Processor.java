/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.process;

import java.io.IOException;

import org.jibx.ws.WsException;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.MessageContext;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnection;

/**
 * Converts messages to and from a specific protocol and communicates the messages over a specific transport.
 * 
 * @author Nigel Charman
 */
public interface Processor
{
    /**
     * Set the context of the message exchange that is to be processed. Must be set before using the Processor.
     * The processing of the message calls the handlers that have been set in the exchange context.
     *
     * @param exchangeCtx the context of the message exchange.  Contains the outbound and inbound contexts.
     */
    void setExchangeContext(ExchangeContext exchangeCtx);
    
    /**
     * Invokes the processor to send and receive messages. The order of sending and receiving messages is defined by the
     * {@link ExchangeContext}.
     * 
     * @param oconn outbound connection
     * @param iconn inbound connection
     * 
     * @throws IOException on an I/O error, for example unable to connect to server
     * @throws WsException on any error other than I/O, for example invalid format of the response message
     */
    void invoke(OutConnection oconn, InConnection iconn) throws IOException, WsException;

    /**
     * Sends the message using the specified operationName.
     * 
     * @param conn transport connection
     * 
     * @throws IOException on an I/O error, for example unable to connect to server
     * @throws WsException on any error other than I/O
     */
    void sendMessage(OutConnection conn) throws IOException, WsException;

    /**
     * Waits for a message and reads the message.
     * 
     * @param conn transport connection
     * @throws IOException on an I/O error, for example unable to connect to server
     * @throws WsException on any error other than I/O, for example invalid format of the response message
     */
    void receiveMessage(InConnection conn) throws IOException, WsException;

    /**
     * Resets the state of this processor, and of all associated Commands and Handlers. This should be called before
     * reusing the processor.
     */
    void reset();

    /**
     * Returns the MessageContext that the processor is currently using.
     * 
     * @return message context
     */
    MessageContext getCurrentMessageContext();

    /**
     * Returns the next MessageContext in the ExchangeContext.
     * 
     * @return next message context, or <code>null</code> if this is the last message context in the exchange context
     */
    MessageContext getNextMessageContext();

    /**
     * Notifies the processor that the current MessageContext is finished and the processor should move on to the next
     * one.
     */
    void switchMessageContext();
}
