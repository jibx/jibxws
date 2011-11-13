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
package org.jibx.ws.io.handler;

import java.io.IOException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IXMLReader;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.io.UnmarshallingPayloadReader;

/**
 * Unmarshalls the payload and makes it available through a subsequent call to {@link #getPayload()}.  Since this 
 * handler is stateful, it is only appropriate for client code, and is not supported for server side handling.
 * 
 * @author Nigel Charman
 */
public class UnmarshallingInHandler implements InHandler
{
    private Object m_payload;
    private final UnmarshallingPayloadReader m_unmarshaller;

    /**
     * Create the unmarshaller using the binding factory for the "target class". This method can only be used with
     * target classes that are mapped in only one binding. Note that there is no restriction on the "payload class"
     * being the same as the target class. The only restriction is that the binding factory for the target class must
     * include a binding for the payload class.
     * 
     * @param clazz the target class
     * @throws WsBindingException on any error in finding or accessing factory, or creating unmarshaller
     */
    public UnmarshallingInHandler(Class clazz) throws WsBindingException {
        m_unmarshaller = new UnmarshallingPayloadReader(clazz);
    }

    /**
     * Create the unmarshaller using the binding factory for the specified binding name and binding package name. See
     * {@link BindingDirectory#getFactory(String, String)} for further definition of the required binding name and
     * binding package name.
     * 
     * @param bindingName binding name
     * @param packageName target package for binding
     * @throws WsBindingException on any error in finding or accessing factory, or creating unmarshaller
     */ 
    public UnmarshallingInHandler(String bindingName, String packageName) throws WsBindingException {
        m_unmarshaller = new UnmarshallingPayloadReader(bindingName, packageName);
    }

    /**
     * Create the unmarshaller using the specified binding factory.
     * 
     * @param factory the binding factory
     * @throws WsBindingException on any error in creating unmarshaller
     */
    public UnmarshallingInHandler(IBindingFactory factory) throws WsBindingException {
        m_unmarshaller = new UnmarshallingPayloadReader(factory);
    }

    /** {@inheritDoc} */
    public Object invoke(InContext context, IXMLReader xmlReader) throws IOException, WsException {
        m_payload = m_unmarshaller.invoke(xmlReader);
        return m_payload;
    }
    
    /** 
     * Returns the payload that was unmarshalled in the previous call to {@link #invoke(InContext, IXMLReader)}.
     *
     * @return payload 
     */
    public Object getPayload() {
        return m_payload;
    }
}
