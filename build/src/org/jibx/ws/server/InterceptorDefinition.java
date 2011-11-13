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

package org.jibx.ws.server;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.transport.interceptor.InputStreamInterceptor;
import org.jibx.ws.transport.interceptor.OutputStreamInterceptor;

/**
 * Generic definition of an interceptor for intercepting data, for example {@link InputStreamInterceptor} and 
 * {@link OutputStreamInterceptor}s.
 * <p>
 * When using JiBX to configure the service, this is populated from the XML service definition document by JiBX
 * unmarshalling.
 * <p> 
 * A new instance of the specified interceptor class will be constructed with the specified arguments for
 * each service instance. Since a separate interceptor class instance is created for each context these do not need to
 * be threadsafe - only one thread will call any given interceptor instance at a time. However, they do need to be 
 * serially reusable.
 * 
 * @author Nigel Charman
 */
public final class InterceptorDefinition extends ObjectDefinition
{
    /** Constructor. */
    public InterceptorDefinition() {
        super("interceptor");
    }
    
    /** {@inheritDoc} */
    protected void postInit() throws WsConfigurationException {
    }
}
