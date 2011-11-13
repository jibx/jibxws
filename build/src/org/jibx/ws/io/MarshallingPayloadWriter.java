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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsException;

/**
 * Marshalls a payload to an XML writer using JiBX bindings. The payload must be of a class that is in the binding
 * factory that this class has been constructed with.
 * <p>
 * This class is serially reusable.
 * 
 * @author Nigel Charman
 */
public final class MarshallingPayloadWriter implements PayloadWriter
{
    private static final Log logger = LogFactory.getLog(MarshallingPayloadWriter.class);
    
    private final MarshallingContext m_marshaller;
    private final String[] m_namespaces;

    /**
     * Configure the marshaller using the binding factory for the "target class". This method can only be used with
     * target classes that are mapped in only one binding. Note that there is no restriction on the "payload class"
     * being the same as the target class. The only restriction is that the binding factory for the target class must
     * include a binding for the payload class.
     * 
     * @param clazz the target class
     * @throws WsBindingException on any error in finding or accessing factory, or creating marshaller
     */
    public MarshallingPayloadWriter(Class clazz) throws WsBindingException {
        IBindingFactory factory;
        try {
            factory = BindingDirectory.getFactory(clazz);
        } catch (JiBXException e) {
            throw new WsBindingException("Error accessing binding.", e);
        }

        m_marshaller = createMarshaller(factory);
        m_namespaces = m_marshaller.getNamespaces();
    }

    /**
     * Create the marshaller using the binding factory for the specified binding name and binding package name. See
     * {@link BindingDirectory#getFactory(String, String)} for further definition of the required binding name and
     * binding package name.
     * 
     * @param bindingName binding name
     * @param packageName target package for binding
     * @throws WsBindingException on any error in finding or accessing factory, or creating marshaller
     */ 
    public MarshallingPayloadWriter(String bindingName, String packageName) throws WsBindingException {
        IBindingFactory factory;
        try {
            factory = BindingDirectory.getFactory(bindingName, packageName);
        } catch (JiBXException e) {
            throw new WsBindingException("Error accessing binding.", e);
        }

        m_marshaller = createMarshaller(factory);
        m_namespaces = m_marshaller.getNamespaces();
    }

    /**
     * Configure the marshaller using the specified binding factory.
     * 
     * @param factory the binding factory
     * @throws WsBindingException if marshaller cannot be created
     */
    public MarshallingPayloadWriter(IBindingFactory factory) throws WsBindingException {
        m_marshaller = createMarshaller(factory);
        m_namespaces = m_marshaller.getNamespaces();
    }
    
    /** {@inheritDoc}  */
    public void invoke(IXMLWriter xmlWriter, Object payload) throws IOException, WsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to marshall payload '" + payload + "'");
        }
        if (!(payload instanceof IMarshallable)) {
            throw new WsException("Payload object must have a defined mapping");
        }
            
        // find first difference between current namespaces and those needed for payload 
        String[] nss = xmlWriter.getNamespaces();
        int limit = Math.min(nss.length, m_namespaces.length);
        int index = 0;
        for (; index < limit; index++) {
            if (!nss[index].equals(m_namespaces[index])) {
                break;
            }
        }
        if (index < m_namespaces.length) {
            
            // push payload namespaces as extension
            int base = xmlWriter.getNamespaceCount();
            String[] extnss = new String[m_namespaces.length - index];
            System.arraycopy(m_namespaces, index, extnss, 0, extnss.length);
            xmlWriter.pushExtensionNamespaces(extnss);
            
            // build translation table to correct namespaces when accessed by index
            int[] xlates = new int[m_namespaces.length];
            for (int i = 0; i < xlates.length; i++) {
                if (i < index) {
                    xlates[i] = i;
                } else {
                    xlates[i] = base + i - index;
                }
            }
            xmlWriter.pushTranslationTable(xlates);
            
        }
        try {
            m_marshaller.setXmlWriter(xmlWriter);

            try {
                ((IMarshallable) payload).marshal(m_marshaller);
            } catch (JiBXException e) {
                throw new WsException("Unable to marshal payload.", e);
            }
        } finally {
            if (index < m_namespaces.length) {
                xmlWriter.popTranslationTable();
                xmlWriter.popExtensionNamespaces();
            }
        }
    }

    private MarshallingContext createMarshaller(IBindingFactory factory) throws WsBindingException {
        MarshallingContext marshaller;
        try {
            marshaller = (MarshallingContext) factory.createMarshallingContext();
        } catch (JiBXException e) {
            throw new WsBindingException("Unable to create MarshallingContext.", e);
        }
        return marshaller;
    }

    /** {@inheritDoc}  */
    public void reset() {
        m_marshaller.reset();
    }
}
