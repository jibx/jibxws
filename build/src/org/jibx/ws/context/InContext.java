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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jibx.runtime.IXMLReader;
import org.jibx.ws.WsException;
import org.jibx.ws.io.PayloadReader;
import org.jibx.ws.io.handler.InHandler;

/**
 * The context for the processing of an inbound message. The context provides access to the handler
 * objects for each phase of processing. It can also be used to store and retrieve state using the
 * {@link #setAttribute(String, Object)} and {@link #getAttribute(String)} methods.
 * 
 * @author Nigel Charman
 */
public final class InContext extends MessageContext
{
    private PayloadReader m_bodyReader;
    private Map m_handlersByPhase;

    /**
     * Sets the reader for the body of the message.
     *
     * @param bodyReader the reader for the message body
     */
    public void setBodyReader(PayloadReader bodyReader) {
        m_bodyReader = bodyReader;
    }
    
    /**
     * Invokes the reader for the body of the message and stores the body for subsequent retrieval with 
     * {@link #getBody()}.
     * 
     * @param xmlReader the reader to read XML content from
     * @throws IOException on any I/O error
     * @throws WsException on errors other than I/O, for example invalid XML format
     */
    public void invokeBodyReader(IXMLReader xmlReader) throws IOException, WsException {
        if (m_bodyReader != null) {
            Object body = m_bodyReader.invoke(xmlReader);
            setBody(body);
        }
    }
    
    /**
     * Adds a handler to the specified phase of the inbound message handling.
     * 
     * @param phase the phase to add the handler to
     * @param handler the handler to add
     */
    public void addHandler(Phase phase, InHandler handler) {
        if (handler != null) {
            if (m_handlersByPhase == null) {
                m_handlersByPhase = new HashMap();
            }
            ArrayList handlers = (ArrayList) m_handlersByPhase.get(phase);
            if (handlers == null) {
                handlers = new ArrayList();
                m_handlersByPhase.put(phase, handlers);
            }
            handlers.add(handler);
        }
    }

    /**
     * Returns whether the specified phase has any handlers.
     * 
     * @param phase the phase
     * @return <code>true</code> if the specified phase has 1 or more handlers, <code>false</code> otherwise
     */
    public boolean hasHandlers(Phase phase) {
        if (m_handlersByPhase == null) {
            return false;
        }
        ArrayList handlers = (ArrayList) m_handlersByPhase.get(phase);
        return handlers != null && handlers.size() > 0;
    }

    /**
     * Sequentially invoke the processing of the inbound handlers for a phase, until one of the handlers is successful.
     * 
     * @param phase the phase to invoke the handlers for
     * @param xmlReader the reader to read XML content from
     * @return true if a handler was successful handler, false otherwise
     * @throws IOException on any I/O error
     * @throws WsException on errors other than I/O, for example invalid XML format
     */
    public Object invokeInHandlers(Phase phase, IXMLReader xmlReader) throws IOException, WsException {
        if (m_handlersByPhase == null) {
            return null;
        }
        ArrayList handlers = (ArrayList) m_handlersByPhase.get(phase);
        if (handlers != null) {
            for (int i = 0; i < handlers.size(); i++) {
                InHandler handler = (InHandler) handlers.get(i);
                Object obj = handler.invoke(this, xmlReader);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOutbound() {
        return false;
    }

    /**
     * Resets the state of this context and all associated commands and handlers for subsequent re-use of this context.
     */
    public void reset() {
        super.reset();
        m_bodyReader.reset();
    }
}
