/*
Copyright (c) 2007, Sosnoski Software Associates Limited.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.ws.pox;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.ws.WsException;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.MessageContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.process.Processor;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnection;
import org.jibx.ws.transport.WsTransportException;

/**
 * Processor for POX messages. The sequence of message exchanges is defined by the {@link ExchangeContext}, which
 * contains a list of {@link MessageContext}s, corresponding to the expected sequence of outbound and inbound messages.
 * Each <code>MessageContext</code> is configured with appropriate handlers to handle the content of the message.
 * <p>
 * The processor can be invoked using the {@link #invoke(OutConnection, InConnection)} method, which
 * sequentially sends and receives messages according to the {@link MessageContext}.
 * <p>
 * Alternatively, the caller can send and receive messages using the individual send and receive methods.
 *
 * @author Nigel Charman
 */
public final class PoxProcessor implements Processor
{
    private static final Log logger = LogFactory.getLog(PoxProcessor.class);

    /** The namespaces required to construct an XML writer for POX messages. */
    private static final String[] DEFAULT_NS = new String[] {"", MarshallingContext.XML_NAMESPACE};

    private ExchangeContext m_exchangeCtx;

    /**
     * Creates a PoxProcessor. The outbound message is formatted according to the XML formatting options in the outbound
     * context.
     * <p>
     * The exchange context must be set with {@link #setExchangeContext(ExchangeContext)} before the processor can be 
     * used.
     * <p>
     * The processing of the message body calls the handlers that have been set in the exchange context.
     */
    public PoxProcessor() {
    }

    /**
     * Creates a PoxProcessor. The outbound message is formatted according to the XML formatting options in the outbound
     * context.
     * <p>
     * The processing of the message body calls the handlers that have been set in the exchange context.
     *
     * @param exchangeContext the context of the message exchange.  Contains the outbound and inbound contexts.
     */
    public PoxProcessor(ExchangeContext exchangeContext) {
        m_exchangeCtx = exchangeContext;
    }

    /**
     * {@inheritDoc}
     */
    public void invoke(OutConnection oconn, InConnection iconn) throws IOException, WsException {
        MessageContext msgCtx = m_exchangeCtx.getCurrentMessageContext();
        while (msgCtx != null) {
            if (msgCtx.isOutbound()) {
                sendMessage(oconn);
            } else {
                receiveMessage(iconn);
            }
            m_exchangeCtx.switchMessageContext();
            msgCtx = m_exchangeCtx.getCurrentMessageContext();  // sendMessage and receiveMessage update the context
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(OutConnection conn) throws IOException, WsException {
        if (m_exchangeCtx.getCurrentMessageContext() == null) {
            throw new IllegalStateException("No message context available for sending message");
        }
        if (!m_exchangeCtx.getCurrentMessageContext().isOutbound()) {
            throw new IllegalStateException("Cannot send message when current message context is inbound");
        }
        OutContext context = (OutContext) m_exchangeCtx.getCurrentMessageContext();

        try {
            logger.debug("Starting send message");
            context.invokeBodyWriter(conn.getNormalWriter(DEFAULT_NS));
            logger.debug("Message sent");
        } finally {
            conn.outputComplete();
            conn.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void receiveMessage(InConnection conn) throws IOException, WsException {
        if (m_exchangeCtx.getCurrentMessageContext() == null) {
            throw new IllegalStateException("No message context available for receiving message");
        }
        if (m_exchangeCtx.getCurrentMessageContext().isOutbound()) {
            throw new IllegalStateException("Cannot receive message when current message context is outbound");
        }
        InContext context = (InContext) m_exchangeCtx.getCurrentMessageContext();

        try {
            conn.init();
            if (conn.hasError()) {
                throw new WsTransportException(conn.getErrorMessage());
            }
            logger.debug("Starting receive message");
            context.invokeBodyReader(conn.getReader());
            if (context.getBody() == null) {
                throw new WsException("No handlers could be found for unmarshalling the body payload");
            }
            logger.debug("Message received");
        } finally {
            conn.inputComplete();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setExchangeContext(ExchangeContext exchangeCtx) {
        m_exchangeCtx = exchangeCtx;
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        m_exchangeCtx.reset();
    }

    /**
     * {@inheritDoc}
     */
    public MessageContext getCurrentMessageContext() {
        return m_exchangeCtx.getCurrentMessageContext();
    }

    /**
     * {@inheritDoc}
     */
    public MessageContext getNextMessageContext() {
        return m_exchangeCtx.getNextMessageContext();
    }

    /**
     * {@inheritDoc}
     */
    public void switchMessageContext() {
        m_exchangeCtx.switchMessageContext();
    }
}
