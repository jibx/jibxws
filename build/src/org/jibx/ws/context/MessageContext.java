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


/**
 * Defines the context for sending or receiving an individual message.
 * 
 * @author Nigel Charman
 */
public abstract class MessageContext extends Context
{
    private ExchangeContext m_exchangeContext;
    private Object m_body;

    /**
     * Get the context of the enclosing message exchange. The exchange context stores state across all messages in the
     * message exchange.
     * 
     * @return exchangeContext the context of the message exchange
     */
    public final ExchangeContext getExchangeContext() {
        return m_exchangeContext;
    }

    /**
     * Set the context of the enclosing message exchange. This method is only intended for use by the ExchangeContext.
     * 
     * @param exchangeContext the context of the message exchange
     */
    final void setExchangeContext(ExchangeContext exchangeContext) {
        m_exchangeContext = exchangeContext;
    }

    /**
     * Returns whether the message context is for an outbound or inbound message.
     *
     * @return <code>true</code> if the context is for an outbound message, <code>false</code> for inbound.
     */
    public abstract boolean isOutbound();

    /**
     * Get message body.
     *
     * @return body
     */
    public final Object getBody() {
        return m_body;
    }

    /**
     * Set message body.
     *
     * @param body the message body
     */
    public final void setBody(Object body) {
        this.m_body = body;
    }

    /**
     * Resets the state of this context and all associated commands and handlers for subsequent re-use of this context.
     */
    public void reset() {
        super.reset();

        m_body = null;
    }
}
