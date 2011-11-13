/*
 * Copyright (c) 2009, Sosnoski Software Associates Limited. All rights reserved.
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
import org.jibx.ws.server.InterceptorDefinition;
import org.jibx.ws.server.TransportOptions;
import org.jibx.ws.server.TransportOptionsDefinition;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;

/**
 * Defines options that are specific to the HTTP servlet transport.
 * 
 *  @author Nigel Charman
 */
public final class HttpServletOptionsDefinition implements TransportOptionsDefinition
{
    /** Input stream interceptor for HTTP servlet only. */
    private InterceptorDefinition m_inputStreamInterceptorDef;

    /** Input stream interceptor for HTTP servlet only. */
    private InterceptorDefinition m_outputStreamInterceptorDef;

    /** {@inheritDoc} */
    public void init() throws WsConfigurationException {
        if (m_inputStreamInterceptorDef != null) {
            m_inputStreamInterceptorDef.init();
        }
        if (m_outputStreamInterceptorDef != null) {
            m_outputStreamInterceptorDef.init();
        }
    }

    /** {@inheritDoc} */
    public TransportOptions createTransportOptions() throws WsConfigurationException { 
        return new HttpServletOptions(this);
    }
    
    /**
     * Get the definition of the {@link InputStreamInterceptor}. 
     *
     * @return input stream interceptor definition, or <code>null</code> if no input stream interceptor 
     */
    protected InterceptorDefinition getInputStreamInterceptorDefinition() {
        return m_inputStreamInterceptorDef;
    }

    /**
     * Set the definition of the {@link InputStreamInterceptor}. 
     *
     * @param inputStreamInterceptorDef input stream interceptor definition
     */
    public void setInputStreamInterceptorDefinition(InterceptorDefinition inputStreamInterceptorDef) {
        m_inputStreamInterceptorDef = inputStreamInterceptorDef;
    }

    /**
     * Get the definition of the {@link OutputStreamInterceptor}. 
     *
     * @return output stream interceptor definition, or <code>null</code> if no output stream interceptor 
     */
    protected InterceptorDefinition getOutputStreamInterceptorDefinition() {
        return m_outputStreamInterceptorDef;
    }

    /**
     * Set the definition of the {@link OutputStreamInterceptor}. 
     *
     * @param outputStreamInterceptorDef output stream interceptor definition
     */
    public void setOutputStreamInterceptorDefinition(InterceptorDefinition outputStreamInterceptorDef) {
        m_outputStreamInterceptorDef = outputStreamInterceptorDef;
    }
}
