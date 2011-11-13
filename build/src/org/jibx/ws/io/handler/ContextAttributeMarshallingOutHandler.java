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

package org.jibx.ws.io.handler;

import java.io.IOException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;

/**
 * A marshaller that retrieves the object to be marshalled from the current {@link OutContext}. The object is retrieved
 * from the attribute using the attribute name passed in the constructor as a key.
 * 
 * @author Nigel Charman
 */
public final class ContextAttributeMarshallingOutHandler implements OutHandler
{
    private final MarshallingPayloadWriter m_marshaller;
    private final String m_attributeName;

    /**
     * Create the marshaller using the binding factory for the "target class". This method can only be used with target
     * classes that are mapped in only one binding. Note that there is no restriction on the "payload class" being the
     * same as the target class. The only restriction is that the binding factory for the target class must include a
     * binding for the payload class.
     * 
     * @param clazz the target class
     * @param attributeName the key to retrieve the object to be marshalled from the current context
     * @throws WsBindingException on any error in finding or accessing factory, or creating marshaller
     */
    public ContextAttributeMarshallingOutHandler(Class clazz, String attributeName) throws WsBindingException {
        m_marshaller = new MarshallingPayloadWriter(clazz);
        m_attributeName = attributeName;
    }

    /**
     * Create the marshaller using the binding factory for the "target class". This method can only be used with target
     * classes that are mapped in only one binding. Note that there is no restriction on the "payload class" being the
     * same as the target class. The only restriction is that the binding factory for the target class must include a
     * binding for the payload class.
     * 
     * @param className the name of the target class
     * @param attributeName the key to retrieve the object to be marshalled from the current context
     * @throws WsBindingException on any error in finding or accessing factory, or creating marshaller
     * @throws WsConfigurationException if specified class cannot be found
     */
    public ContextAttributeMarshallingOutHandler(String className, String attributeName) throws WsBindingException,
        WsConfigurationException {

        try {
            Class clazz = Class.forName(className);
            m_marshaller = new MarshallingPayloadWriter(clazz);
            m_attributeName = attributeName;
        } catch (ClassNotFoundException e) {
            throw new WsConfigurationException("Unable to create ContextualPayloadMarshaller.  Class not found: "
                + className);
        }
    }

    /**
     * Create the marshaller using the binding factory for the specified binding name and binding package name. See
     * {@link BindingDirectory#getFactory(String, String)} for further definition of the required binding name and
     * binding package name.
     * 
     * @param bindingName binding name
     * @param packageName target package for binding
     * @param attributeName the key to retrieve the object to be marshalled from the current context
     * @throws WsBindingException on any error in finding or accessing factory, or creating marshaller
     */
    public ContextAttributeMarshallingOutHandler(String bindingName, String packageName, String attributeName)
        throws WsBindingException {
        m_marshaller = new MarshallingPayloadWriter(bindingName, packageName);
        m_attributeName = attributeName;
    }

    /**
     * Create the marshaller using the specified binding factory.
     * 
     * @param factory the binding factory
     * @param attributeName the key to retrieve the object to be marshalled from the current context
     * @throws WsBindingException on any error in creating marshaller
     */
    public ContextAttributeMarshallingOutHandler(IBindingFactory factory, String attributeName) 
            throws WsBindingException {
        m_marshaller = new MarshallingPayloadWriter(factory);
        m_attributeName = attributeName;
    }

    /**
     * {@inheritDoc} 
     * Retrieves the object from the OutContext and marshals it. The object is retrieved using the
     * attributeName specified in the constructor.
     */
    public void invoke(OutContext context, IXMLWriter xmlWriter) throws IOException, WsException {
        Object payload = context.getAttribute(m_attributeName);
        if (payload == null) {
            throw new WsException("Unable to write header data since attribute " + m_attributeName
                + " is null in OutContext");
        }
        m_marshaller.invoke(xmlWriter, payload);
    }
}
