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

package org.jibx.ws.io;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsException;

/**
 * Unmarshalls a payload from an XML reader using JiBX bindings. The object to unmarshall must be of a class that is in
 * the binding factory that this class has been constructed with.
 * <p>
 * This class is serially reusable.
 * 
 * @author Nigel Charman
 */
public final class UnmarshallingPayloadReader implements PayloadReader
{
    private final UnmarshallingContext m_unmarshallCtx;

    /**
     * Create the unmarshaller using the binding factory for the "target class". This method can only be used with
     * target classes that are mapped in only one binding. Note that there is no restriction on the "payload class"
     * being the same as the target class. The only restriction is that the binding factory for the target class must
     * include a binding for the payload class.
     * 
     * @param clazz the target class
     * @throws WsBindingException on any error in finding or accessing factory, or creating unmarshaller
     */
    public UnmarshallingPayloadReader(Class clazz) throws WsBindingException {
        IBindingFactory factory;
        try {
            factory = BindingDirectory.getFactory(clazz);
        } catch (JiBXException e) {
            throw new WsBindingException("Error accessing binding.", e);
        }

        m_unmarshallCtx = createUnmarshallingContext(factory);
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
    public UnmarshallingPayloadReader(String bindingName, String packageName) throws WsBindingException {
        IBindingFactory factory;
        try {
            factory = BindingDirectory.getFactory(bindingName, packageName);
        } catch (JiBXException e) {
            throw new WsBindingException("Error accessing binding.", e);
        }
        
        m_unmarshallCtx = createUnmarshallingContext(factory);
    }

    /**
     * Create the unmarshaller using the specified binding factory.
     * 
     * @param factory the binding factory
     * @throws WsBindingException on any error in creating unmarshaller
     */
    public UnmarshallingPayloadReader(IBindingFactory factory) throws WsBindingException {
        m_unmarshallCtx = createUnmarshallingContext(factory);
    }

    private UnmarshallingContext createUnmarshallingContext(IBindingFactory factory) throws WsBindingException {
        UnmarshallingContext ctx = null;
        try {
            ctx = (UnmarshallingContext) factory.createUnmarshallingContext();
        } catch (JiBXException e) {
            throw new WsBindingException("Unable to create UnmarshallingContext.", e);
        }
        return ctx;
    }

    /** {@inheritDoc} */
    public Object invoke(IXMLReader xmlReader) throws WsException {
        Object payload = null;
        m_unmarshallCtx.reset();
        m_unmarshallCtx.setDocument(xmlReader);
        String name;
        try {
            name = m_unmarshallCtx.toStart();
            IUnmarshaller unmarshaller = m_unmarshallCtx.getUnmarshaller(xmlReader.getNamespace(), name);
            if (unmarshaller != null) {
                payload = unmarshaller.unmarshal(null, m_unmarshallCtx);
            } 
        } catch (JiBXException e) {
            throw new WsException("Error in unmarshalling.", e);
        }

        return payload;
    }

    /** {@inheritDoc}  */
    public void reset() {
        m_unmarshallCtx.reset();
    }
}
