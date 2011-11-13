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
package org.jibx.ws.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides functionality common to all Context classes.
 * 
 * @author Nigel Charman
 */
public abstract class Context
{
    private static final Log logger = LogFactory.getLog(Context.class);
    private final Map m_attributes = new HashMap();

    /**
     * Returns the object associated with the given attribute name in this context. Returns <code>null</code> if no
     * attribute exists by the given name.
     * 
     * @param name the name of the attribute
     * @return the object associated with the attribute name, or <code>null</code> if no attribute exists matching the
     * given name
     */
    public final Object getAttribute(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting attribute " + name);
        }
        return m_attributes.get(name);
    }

    /**
     * Returns the names of all attributes in this context. Returns an empty set if no attributes exist.
     * 
     * @return a set of {@link String}s of all attributes names.
     */
    public final Set getAttributeNames() {
        return m_attributes.keySet();
    }

    /**
     * Removes the attribute with the given name from this context. Subsequent calls to {@link #getAttribute(String)}
     * for this name will return <code>null</code>.
     * 
     * @param name the name of the attribute to be removed
     */
    public final void removeAttribute(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing attribute " + name);
        }
        m_attributes.remove(name);
    }

    /**
     * Binds an object to a given attribute name in this context.
     * 
     * @param name the name of the attribute to add
     * @param value the object to associate with this attribute name
     */
    public final void setAttribute(String name, Object value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting attribute " + name + " to " + value);
        }
        m_attributes.put(name, value);
    }

    /**
     * Resets the state of this context, so that it can be reused by a subsequent message exchange.
     */
    public void reset() {
        logger.debug("Resetting attributes");
        m_attributes.clear();
    }
}
