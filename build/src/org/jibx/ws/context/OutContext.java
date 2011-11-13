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

package org.jibx.ws.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.WsException;
import org.jibx.ws.io.PayloadWriter;
import org.jibx.ws.io.handler.OutHandler;

/**
 * The context for the processing of an outbound message. The context provides access to the phase object for each phase
 * of processing. It can also be used to store and retrieve state using the {@link #setAttribute(String, Object)} and
 * {@link #getAttribute(String)} methods.
 * 
 * @author Nigel Charman
 */
public final class OutContext extends MessageContext
{
    private PayloadWriter m_bodyWriter;
    private Map m_handlersByPhase;

    /**
     * Sets the writer for the body of the message.
     * 
     * @param bodyWriter the message body writer
     */
    public void setBodyWriter(PayloadWriter bodyWriter) {
        m_bodyWriter = bodyWriter;
    }

    /**
     * Invokes the writing of the body of the message.  Assumes that the setBody() method has been called with the
     * body of the message.
     * 
     * @param xmlWriter the writer to write the body to
     * @throws IOException on any I/O error
     * @throws WsException on errors other than I/O, for example invalid XML format
     */
    public void invokeBodyWriter(IXMLWriter xmlWriter) throws IOException, WsException {
        if (m_bodyWriter != null) {
            m_bodyWriter.invoke(xmlWriter, getBody());
        }
    }

    /**
     * Adds a handler to the specified phase of the outbound message handling.
     * 
     * @param phase the phase to add the handler to
     * @param handler the handler to add
     */
    public void addHandler(Phase phase, OutHandler handler) {
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
     * Invoke the processing of the phase.
     * 
     * @param phase the phase to invoke the handlers for
     * @param xmlWriter the writer to write XML content to
     * @throws IOException on any I/O error
     * @throws WsException on errors other than I/O, for example invalid XML format
     */
    public void invokeHandlers(Phase phase, IXMLWriter xmlWriter) throws IOException, WsException {
        if (m_handlersByPhase != null) {
            ArrayList handlers = (ArrayList) m_handlersByPhase.get(phase);
            if (handlers != null) {
                for (int i = 0; i < handlers.size(); i++) {
                    ((OutHandler) handlers.get(i)).invoke(this, xmlWriter);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOutbound() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        super.reset();
        m_bodyWriter.reset();
    }
}
