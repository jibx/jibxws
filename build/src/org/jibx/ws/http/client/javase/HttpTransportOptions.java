/*
Copyright (c) 2009, Sosnoski Software Associates Limited. 
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

package org.jibx.ws.http.client.javase;

import org.jibx.ws.transport.InputStreamInterceptable;
import org.jibx.ws.transport.OutputStreamInterceptable;
import org.jibx.ws.transport.TransportOptions;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;

/**
 * Defines options for customizing the HTTP client transport.
 * 
 * @author Nigel Charman
 */
public final class HttpTransportOptions implements TransportOptions, InputStreamInterceptable, OutputStreamInterceptable
{
    private OutputStreamInterceptor m_outputStreamInterceptor;
    private InputStreamInterceptor m_inputStreamInterceptor;
    
    /**
     * Get outputStreamInterceptor.
     *
     * @return outputStreamInterceptor
     */
    public OutputStreamInterceptor getOutputStreamInterceptor() {
        return m_outputStreamInterceptor;
    }
    
    /**
     * Set outputStreamInterceptor.
     *
     * @param outputStreamInterceptor the interceptor
     */
    public void setOutputStreamInterceptor(OutputStreamInterceptor outputStreamInterceptor) {
        m_outputStreamInterceptor = outputStreamInterceptor;
    }
    
    /**
     * Get inputStreamInterceptor.
     *
     * @return inputStreamInterceptor
     */
    public InputStreamInterceptor getInputStreamInterceptor() {
        return m_inputStreamInterceptor;
    }
    
    /**
     * Set inputStreamInterceptor.
     *
     * @param inputStreamInterceptor the interceptor
     */
    public void setInputStreamInterceptor(InputStreamInterceptor inputStreamInterceptor) {
        m_inputStreamInterceptor = inputStreamInterceptor;
    }

}
