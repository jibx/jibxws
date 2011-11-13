/*
 * Copyright (c) 2007-2008, Sosnoski Software Associates Limited. All rights reserved.
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
import org.jibx.ws.io.handler.InHandler;
import org.jibx.ws.io.handler.OutHandler;


/**
 * Defines a handler to be invoked during processing, for example for SOAP header handling. Handlers to be used for a
 * service are set using {@link ServiceDefinition#setHandlerDefinitions(java.util.List)}.
 * <p>
 * When using JiBX to configure the service, this is populated from the XML service definition document by JiBX
 * unmarshalling. A new instance of the specified handler class will be constructed with the specified arguments for
 * each service instance. Since a separate handler class instance is created for each context these do not need to be
 * threadsafe - only one thread will call any given handler instance at a time.
 * <p>
 * If not using JiBX to configure the service, a handler object can be specified in place of specifying the handler
 * class and arguments. Since only a single instance of this handler object will be used, the object must be
 * thread-safe.
 * 
 * @author Nigel Charman
 */

public final class HandlerDefinition extends ObjectDefinition
{
    /** Constructor. */
    public HandlerDefinition() {
        super("handler");
    }
    
    /**
     * Sets a handler object. If this is set to a non <code>null</code> value, the handler class and args do not need
     * to be set, and will be ignored. If set, the handler object must be thread safe. The specified
     * object must implement either the {@link InHandler} or {@link OutHandler} interface. 
     * 
     * @param obj handler object
     */
    public void setHandlerObject(Object obj) {
        setDefinedObject(obj);
    }

    /** {@inheritDoc} */
    protected void postInit() throws WsConfigurationException {
    }
}
