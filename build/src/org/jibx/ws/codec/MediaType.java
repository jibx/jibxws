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

package org.jibx.ws.codec;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Represents an Internet Media Type (MIME type).
 * <p>
 * Since the type and subtype are defined in RFC 2616 to be case insensitive, these are stored in lower case. 
 * Parameters might be case sensitive and are stored in their original case.
 * <p>
 * The {@link #toString()} method outputs the type and subtype in lower case.
 * 
 * @author Nigel Charman
 */
public final class MediaType
{
    private final String m_primaryType;
    private final String m_subtype;
    private List m_parameterList;
    private int m_hashCode;
    
    /** A MediaType that matches all types. For HTTP, this is the default if no Accept header is present. */
    public static final MediaType ALL_MEDIA_TYPES = new MediaType("*", "*");
    private String m_longString;
    private String m_baseType;
    private boolean m_frozen;
    
    /**
     * Constructor.
     * 
     * @param primaryType the media type (for example, "text")
     * @param subType the media subtype (for example, "xml")
     * @param parameterList a list of {@link Parameter}
     */
    public MediaType(String primaryType, String subType, List parameterList) {
        if (primaryType == null || primaryType.length() == 0 || subType == null || subType.length() == 0) {
            throw new IllegalArgumentException("Media type and subtype must both be non-null, non-empty strings");
        }
        if (parameterList == null) {
            throw new IllegalArgumentException("ParameterList must be non-null");
        }
        m_primaryType = primaryType.toLowerCase();
        m_subtype = subType.toLowerCase();
        m_parameterList = parameterList;
    }
    
    /**
     * Constructor.
     * 
     * @param primaryType the media type (for example, "text")
     * @param subType the media subtype (for example, "xml")
     */
    public MediaType(String primaryType, String subType) {
        this(primaryType, subType, Collections.EMPTY_LIST);
    }
    
    /**
     * Constructor using a media type string. For example, "text/html; charset=ISO-8859-4; q=0.5" will be parsed into
     * an InternetMediaType with type="text", subtype="html" and a parameter list containing the 2 parameters.
     * 
     * @param mediaType Internet Media Type string
     * @throws ParseException on error parsing mediaType
     */
    public MediaType(String mediaType) throws ParseException {
        this(mediaType, false);
    }
    
    /**
     * Constructor using a media type string, with the option to ignore parameters. 
     * <p>
     * For example, if ignoreParameters is true, "text/html; charset=ISO-8859-4; q=0.5" will be parsed into
     * an InternetMediaType with type="text", subtype="html" and an empty parameter list.
     * 
     * @param mediaType Internet Media Type string
     * @param ignoreParameters <code>true</code> to ignore parameters, <code>false</code> to parse the parameters. 
     * Setting this value to false is the same as calling the {@link MediaType#MediaType(String)} constructor.
     * @throws ParseException on error parsing mediaType
     */
    public MediaType(String mediaType, boolean ignoreParameters) throws ParseException {
        try {
            StringTokenizer st = new StringTokenizer(mediaType, "/");
            m_primaryType = st.nextToken().trim().toLowerCase();
            m_subtype = st.nextToken(";").trim().toLowerCase().substring(1); // substring to remove the "/" first char
            if (m_primaryType.length() == 0 || m_subtype.length() == 0) {
                throw new ParseException("Type and subtype must contain values in '" + mediaType + "'", -1);
            }
            if (ignoreParameters) {
                m_parameterList = Collections.EMPTY_LIST;
            } else {
                m_parameterList = new ArrayList();
                while (st.hasMoreTokens()) {
                    addParameter(Parameter.parse(st.nextToken().trim()));
                }
            }
        } catch (NoSuchElementException e) {
            throw new ParseException("Unable to parse mediaType '" + mediaType + "'", -1);
        }
    }

    /**
     * Adds the specified parameter to the parameter list.
     *
     * @param parameter to add
     */
    public void addParameter(Parameter parameter) {
        if (m_frozen) {
            throw new IllegalStateException("MediaType is frozen");
        }
        if (m_parameterList.isEmpty()) {
            m_parameterList = new ArrayList();
        }
        m_parameterList.add(parameter);
        m_longString = null;         
    }

    /**
     * Get primary type, ignoring the "/" character and subtype.
     *
     * @return type primary media type (eg. "text" for "text/html").
     */
    public String getPrimaryType() {
        return m_primaryType;
    }

    /**
     * Get media sub type.
     *
     * @return type primary media type (eg. "html" for "text/html").
     */
    public String getSubType() {
        return m_subtype;
    }

    /**
     * Get parameterList.
     *
     * @return parameterList list of {@link Parameter}
     */
    public List getParameterList() {
        return Collections.unmodifiableList(m_parameterList);
    }
    
    /**
     * Represents an Internet Media Type parameter.
     */
    public static final class Parameter 
    {
        private final String m_name;
        private final String m_value;
        private int m_hashCode;
        
        /**
         * Constructor for parameter with no value.
         * 
         * @param name parameter name
         */
        public Parameter(String name) {
            this(name, null);
        }

        /**
         * Constructor.
         * 
         * @param name parameter name
         * @param value parameter value
         */
        public Parameter(String name, String value) {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Media type parameter name must be non-null, non-empty string");
            }
            m_name = name;
            m_value = value;
        }

        /** Parses the mediaTypeParam into a new Parameter. */
        private static Parameter parse(String mediaTypeParam) {
            int sep = mediaTypeParam.indexOf('=');
            if (sep == -1) {
                return new Parameter(mediaTypeParam);
            } else {
                return new Parameter(mediaTypeParam.substring(0, sep), mediaTypeParam.substring(sep + 1));
            }
        }

        /**
         * Get name.
         *
         * @return name parameter name
         */
        public String getName() {
            return m_name;
        }

        /**
         * Get value.
         *
         * @return value parameter value
         */
        public String getValue() {
            return m_value;
        }
        
        /** {@inheritDoc} */
        public int hashCode() {
            final int prime = 31;
            if (m_hashCode == 0) {
                m_hashCode = 1;
                m_hashCode = prime * m_hashCode + (m_name.hashCode());
                m_hashCode = prime * m_hashCode + ((m_value == null) ? 0 : m_value.hashCode());
            }
            return m_hashCode;
        }

        /** {@inheritDoc} */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Parameter other = (Parameter) obj;
            if (!m_name.equals(other.m_name)) {
                return false;
            }
            if (m_value == null) {
                return (other.m_value == null);
            } else { 
                return m_value.equals(other.m_value);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * Returns as an Internet Media Type string (eg. "text/html;charset=ISO-8859-4").
     */
    public String toString() {
        if (m_longString == null) {
            StringBuffer buff = new StringBuffer(64);
            buff.append(getBaseType());
            for (Iterator iterator = m_parameterList.iterator(); iterator.hasNext();) {
                Parameter param = (Parameter) iterator.next();
                buff.append("; ").append(param.m_name).append('=').append(param.m_value);
            }
            m_longString = buff.toString();
        }
        return m_longString;
    }

    /**
     * Returns the MediaType string with extra parameters added.
     * 
     * @param extraParams extra parameters to add to the media type string.  <code>null</code> parameters are ignored 
     * @return as an Internet Media Type string (eg. "text/html;charset=ISO-8859-4").
     */
    public String toStringWithParams(Parameter[] extraParams) {
        StringBuffer buff = new StringBuffer(64);
        buff.append(toString());
        for (int i = 0; i < extraParams.length; i++) {
            Parameter param = extraParams[i];
            if (param != null) {
                buff.append("; ").append(param.m_name).append('=').append(param.m_value);
            }
        }
        return buff.toString();
    }
    
    /**
     * Returns the MediaType string with parameters stripped (eg. "text/html").
     * 
     * @return MediaType with parameters stripped 
     */
    public String getBaseType() {
        if (m_baseType == null) {
            StringBuffer buff = new StringBuffer(64);
            buff.append(m_primaryType).append('/').append(m_subtype);
            m_baseType = buff.toString();
        }
        return m_baseType;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        if (m_hashCode == 0) {
            m_hashCode = 1;
            m_hashCode = prime * m_hashCode + ((m_parameterList == null) ? 0 : m_parameterList.hashCode());
            m_hashCode = prime * m_hashCode + ((m_subtype == null) ? 0 : m_subtype.hashCode());
            m_hashCode = prime * m_hashCode + ((m_primaryType == null) ? 0 : m_primaryType.hashCode());
        }
        return m_hashCode;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MediaType other = (MediaType) obj;
        if (!m_subtype.equals(other.m_subtype)) {
            return false;
        }
        if (!m_primaryType.equals(other.m_primaryType)) {
            return false;
        }
        return (m_parameterList.equals(other.m_parameterList));
    }

    /**
     * Checks whether this MediaType will accept the specified MediaType.  
     * <p>
     * If this MediaType contains an asterisk in the type or subtype, the asterisk will be treated as a wildcard that 
     * accepts any string.  For example:
     * <ul>
     * <li><code>new MediaType("text", "xml").accepts(new MediaType("text", "xml"))</code> returns <code>true</code>,  
     * <li><code>new MediaType("text", "*").accepts(new MediaType("text", "xml"))</code> returns <code>true</code>,
     * <li><code>new MediaType("*", "*").accepts(new MediaType("text", "xml"))</code> returns <code>true</code>,  
     * </ul>
     * This is as per the Accept request header specified in RFC 2616, which states 'The asterisk "*" character is used 
     * to group media types into ranges, with "*<!-- -->/*" indicating all media types and "type/*" indicating all 
     * subtypes of that type.'
     * <p>
     * This method ignores the parameters of the MediaType.
     *  
     * @param other the MediaType to check for acceptance
     * @return true if this MediaType will accept the specified MediaType
     */
    public boolean accepts(MediaType other) {
        if (this == ALL_MEDIA_TYPES) {
            return true;
        }
        if (m_subtype.equals("*")) {
            if (m_primaryType.equals(other.m_primaryType) || m_primaryType.equals("*")) {
                return true;
            }
        }
        if (m_subtype.equals(other.m_subtype)) {
            if (m_primaryType.equals(other.m_primaryType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures that the MediaType cannot be modified.
     *
     * @return frozen instance of this media type
     */
    public MediaType freeze() {
        m_frozen = true;
        return this;
    }
}

