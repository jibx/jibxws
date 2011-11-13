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

package org.jibx.ws.context;

import java.util.ArrayList;

/**
 *  Maintains the context for all messages in a message exchange.
 * 
 * @author Nigel Charman
 */
public final class ExchangeContext extends Context
{
    private final ArrayList m_msgCtxs = new ArrayList();
    private int m_currMsgCtxIdx;

    /**
     * Creates an {@link ExchangeContext} for an IN-ONLY message exchange.
     *
     * @param inboundContext the context for the inbound message
     * @return exchange context
     */
    public static ExchangeContext createInOnlyExchange(InContext inboundContext) {
        return new ExchangeContext(inboundContext);
    }
    
    /**
     * Creates an {@link ExchangeContext} for an OUT-ONLY message exchange.
     *
     * @param outboundContext the context for the outbound message
     * @return exchange context
     */
    public static ExchangeContext createOutOnlyExchange(OutContext outboundContext) {
        return new ExchangeContext(outboundContext);
    }
    
    /**
     * Creates an {@link ExchangeContext} for an OUT-IN message exchange.
     *
     * @param outboundContext the context for the outbound message
     * @param inboundContext the context for the inbound message
     * @return exchange context
     */
    public static ExchangeContext createOutInExchange(OutContext outboundContext, InContext inboundContext) {
        return new ExchangeContext(outboundContext, inboundContext);
    }

    /**
     * Creates an {@link ExchangeContext} for an IN-OUT message exchange.
     *
     * @param inboundContext the context for the inbound message
     * @param outboundContext the context for the outbound message
     * @return exchange context
     */
    public static ExchangeContext createInOutExchange(InContext inboundContext, OutContext outboundContext) {
        return new ExchangeContext(inboundContext, outboundContext);
    }

    private ExchangeContext(MessageContext context) {
        context.setExchangeContext(this);
        m_msgCtxs.add(context);
    }
    
    private ExchangeContext(MessageContext context1, MessageContext context2) {
        context1.setExchangeContext(this);
        m_msgCtxs.add(context1);
        
        context2.setExchangeContext(this);
        m_msgCtxs.add(context2);
    }

    /**
     * Returns the context of the current message being processed, or <code>null</code> if the message exchange is 
     * complete. 
     *
     * @return current message context, or <code>null</code> if the exchange is complete 
     */
    public MessageContext getCurrentMessageContext() {
        return messageContextAt(m_currMsgCtxIdx);
    }
    
    /**
     * Returns the next message context in the exchange context, or <code>null</code> if the message exchange is 
     * complete or the current message context is the last one.
     *
     * @return next message context, or <code>null</code> if the exchange will be complete after the current message
     * context 
     */
    public MessageContext getNextMessageContext() {
        return messageContextAt(m_currMsgCtxIdx + 1);
    }

    private MessageContext messageContextAt(int idx) {
        MessageContext messageContext = null;
        if (idx < m_msgCtxs.size()) {
            messageContext = (MessageContext) m_msgCtxs.get(idx);
        }
        return messageContext;
    }
    
    /**
     * Signals that the current message context is complete, and the exchange should move to the next message context. 
     */
    public void switchMessageContext() {
        m_currMsgCtxIdx++;
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        super.reset();
        m_currMsgCtxIdx = 0;
        for (int i = 0; i < m_msgCtxs.size(); i++) {
            ((MessageContext) m_msgCtxs.get(i)).reset();
        }
    }
}
