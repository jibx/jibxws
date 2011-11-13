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

package org.jibx.ws.http.servlet;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.TransportOptions;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;

/**
 * Optional settings that are specific to Http Servlet transport.
 * <p>
 * A separate instance of these options will be created per {@link Service} instance, and associated with the 
 * <code>Service</code>.  The instance will be serially reused across calls to the <code>Service</code>.
 * 
 * @author Nigel Charman
 */
public final class HttpServletOptions implements TransportOptions
{
    /** Interceptor for intercepting HTTP input. */
    private InputStreamInterceptor m_inputStreamInterceptor;

    /** Interceptor for intercepting HTTP output. */
    private OutputStreamInterceptor m_outputStreamInterceptor;

    /**
     * Construct the options from the supplied definition. 
     * 
     * @param hsodef http servlet options definition
     * @throws WsConfigurationException on error in the supplied definition
     */
    public HttpServletOptions(HttpServletOptionsDefinition hsodef)
            throws WsConfigurationException {

        if (hsodef.getInputStreamInterceptorDefinition() != null) {
            Object interceptor = hsodef.getInputStreamInterceptorDefinition().getObject();
            if (!(interceptor instanceof InputStreamInterceptor)) {
                throw new WsConfigurationException("Error: Interceptor class '" + interceptor.getClass()
                    + "' must implement InputStreamInterceptor");
            }
            m_inputStreamInterceptor = (InputStreamInterceptor) interceptor;
        }
        if (hsodef.getOutputStreamInterceptorDefinition() != null) {
            Object interceptor = hsodef.getOutputStreamInterceptorDefinition().getObject();
            if (!(interceptor instanceof OutputStreamInterceptor)) {
                throw new WsConfigurationException("Error: Interceptor class '" + interceptor.getClass()
                    + "' must implement OutputStreamInterceptor");
            }
            m_outputStreamInterceptor = (OutputStreamInterceptor) interceptor;
        }
    }

    /** {@inheritDoc} */
    public void setMessageContexts(InContext inCtx, OutContext outCtx) {
        if (m_inputStreamInterceptor != null) {
            m_inputStreamInterceptor.setMessageContext(inCtx);
        }
        if (m_outputStreamInterceptor != null) {
            m_outputStreamInterceptor.setMessageContext(outCtx);
        }
    }
    
    /**
     * Returns the {@link InputStreamInterceptor} for intercepting HTTP input.
     * 
     * @return interceptor
     */
    public InputStreamInterceptor getInputStreamInterceptor() {
        return m_inputStreamInterceptor;
    }

    /**
     * Returns the {@link OutputStreamInterceptor} for intercepting HTTP output.
     * 
     * @return interceptor
     */
    public OutputStreamInterceptor getOutputStreamInterceptor() {
        return m_outputStreamInterceptor;
    }
}
