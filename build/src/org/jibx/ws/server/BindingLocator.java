/*
 * Copyright (c) 2008, Sosnoski Software Associates Limited. All rights reserved.
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

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;

/**
 * Contains reference details to enable a JiBX {@link IBindingFactory} to be located in the {@link BindingDirectory}.
 * <p>
 * Both {@link #setPackageName(String)} and {@link #setBindingName(String)} must be called prior to calling
 * {@link #getBindingFactory()}. The {@link #getBindingFactory()} method locates the binding factory using the
 * {@link BindingDirectory}.
 * 
 * @author Nigel Charman
 */
public final class BindingLocator
{
    private String m_bindingName;
    private String m_packageName;

    /**
     * Set the name of the binding definition to be looked up. 
     * 
     * @param bindingName the name of the binding definition
     */
    public void setBindingName(String bindingName) {
        this.m_bindingName = bindingName;
    }

    /**
     * Sets the package name for the binding definition lookup.
     * 
     * @param packageName the name of the package that the relevant binding is compiled to
     */
    public void setPackageName(String packageName) {
        this.m_packageName = packageName;
    }

    /**
     * Returns the binding factory for this reference using the  {@link BindingDirectory}. 
     * 
     * @return binding factory
     * @throws WsBindingException if unable to find binding in the directory
     * @throws WsConfigurationException if either package or binding name are not set
     */
    public IBindingFactory getBindingFactory() throws WsBindingException, WsConfigurationException {
        IBindingFactory factory = null;

        if (m_bindingName == null || m_packageName == null) {
            throw new WsConfigurationException(
                "Binding definition lookup error: both binding and package name must be set.");
        }
        try {
            factory = BindingDirectory.getFactory(m_bindingName, m_packageName);
        } catch (JiBXException e) {
            throw new WsBindingException("Error accessing binding for binding '" + m_bindingName + "' with package '"
                + m_packageName + "'.", e);
        }

        return factory;
    }
}
