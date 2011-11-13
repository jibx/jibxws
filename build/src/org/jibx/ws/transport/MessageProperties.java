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

package org.jibx.ws.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jibx.ws.codec.MediaType;

/**
 * Defines transport layer properties that are message specific.
 * 
 *  @author Nigel Charman
 */
public final class MessageProperties
{
    /** Media type of message. */
    private MediaType m_contentType;
    
    /** Media type(s) to be accepted for response. */
    private MediaType[] m_acceptTypes;

    /** Operation name. */
    private String m_opname;

    /** charset of message. */
    private String m_charset;

    /** Additional properties. */
    private final Map m_properties = new HashMap();

    /**
     * Set media type of message.
     *
     * @param contentType media type
     */
    public void setContentType(MediaType contentType) {
        m_contentType = contentType;
    }

    /**
     * Returns media type of message.
     *
     * @return media type
     */
    public MediaType getContentType() {
        return m_contentType;
    }

    /**
     * Set media type(s) to be accepted for response. 
     *
     * @param acceptTypes acceptable media types
     */
    public void setAcceptTypes(MediaType[] acceptTypes) {
        m_acceptTypes = acceptTypes;
    }
   
    /**
     * Returns media type(s) to be accepted for response. 
     *
     * @return acceptable media types
     */
    public MediaType[] getAcceptTypes() {
        return m_acceptTypes;
    }

    /**
     * Sets the name of the operation. Optional.  For HTTP, this will add an "action" parameter with this name to the
     * content-type header.  SOAP 1.2 messages will set this if they require "action" parameters. SOAP 1.1 messages
     * will not set this, but will set "SOAPAction" properties instead.     
     *
     * @param opname operation name
     */
    public void setOperation(String opname) {
        m_opname = opname;
    }
    
    /**
     * Returns the name of the operation. Optional. See {@link #setOperation(String)} for further details.
     *   
     * @return operation name, or <code>null</code> if not set.
     */    
    public String getOperation() {
        return m_opname;
    }

    /**
     * Sets the charset of the message.
     *
     * @param charset the charset
     */
    public void setCharset(String charset) {
        m_charset = charset;
    }
    
    /**
     * Returns the charset of the message.
     *
     * @return the charset
     */
    public String getCharset() {
        return m_charset;
    }
    
    /**
     * Sets an additional property for this message.
     * 
     * @param name the name of the property to add
     * @param value the object to associate with this property name
     */
    public void setProperty(String name, String value) {
        m_properties.put(name, value);
    }
    
    /**
     * Returns the object associated with the given property name in this message. Returns <code>null</code> if no
     * property exists by the given name.
     * 
     * @param name the name of the property
     * @return the object associated with the property name, or <code>null</code> if no property exists matching the
     * given name
     */
    public String getProperty(String name) {
        return (String) m_properties.get(name);
    }

    /**
     * Returns the names of all properties. Returns an empty set if no properties exist.
     * 
     * @return a set of {@link String}s of all properties names.
     */
    public Set getPropertyNames() {
        return m_properties.keySet();
    }
}
