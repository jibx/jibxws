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

import org.jibx.ws.codec.CodecDirectory;


/**
 * Defines options for the encoding, XML declaration and formatting of the XML message, and the media types for the
 * message.
 * 
 * @author Nigel Charman
 */
public final class MessageOptions
{
    /** Default media types accepted (must precede default instance creation!). */
    private static final String[] DEFAULT_INMEDIA_TYPE_CODES = new String[] { CodecDirectory.TEXT_MEDIA_CODE };
    
    /** Media type short code to be used for output. */
    private String m_outMediaTypeCode;
    
    /** Media type short code(s) to be accepted for input (<code>null</code> if unspecified). */
    private String[] m_inMediaTypeCodes;
    
    /** XML formatting options. */
    private XmlOptions m_xmlOptions;
    
    /**
     * Default constructor for UTF-8, with no formatting and XML declaration containing XML version only.
     */
    public MessageOptions() {
        m_xmlOptions = new XmlOptions();
        setOutMediaTypeCode(CodecDirectory.TEXT_MEDIA_CODE);
        setInMediaTypeCodes(DEFAULT_INMEDIA_TYPE_CODES);
    }

    /**
     * Copy constructor.
     * 
     * @param options the options to copy
     */
    public MessageOptions(MessageOptions options) {
        m_xmlOptions = new XmlOptions(options.getXmlOptions());
        setOutMediaTypeCode(options.getOutMediaTypeCode());
        setInMediaTypeCodes(options.getInMediaTypeCodes());
    }

    /**
     * Construct with specified encoding, no formatting and XML declaration containing XML version only.
     *  
     * @param encoding the encoding to apply to the message
     */
    public MessageOptions(XmlEncoding encoding) {
        this();
        setEncoding(encoding);
    }
    
    /**
     * Construct with specified encoding and XML declaration, with no formatting.
     *  
     * @param encoding the encoding to apply to the message
     * @param includeEncodingDecl <code>true</code> to include the encoding in the XML declaration, <code>false</code>
     * omits the encoding in the XML declaration.  
     * @param standaloneDecl <code>true</code> to include <code>standalone="yes"</code> in the XML declaration, 
     * <code>false</code> to include <code>standalone="no"</code> in the XML declaration, <code>null</code> to exclude 
     * the standalone attribute from the XML declaration,
     */
    public MessageOptions(XmlEncoding encoding, boolean includeEncodingDecl, Boolean standaloneDecl) {
        
        this(encoding);
        setIncludeEncodingDecl(includeEncodingDecl);
        setStandaloneDecl(standaloneDecl);
    }
    
    /**
     * Construct with message formatting details.
     *  
     * @param indentChar the whitespace character to use for indenting XML.
     * @param indentCount how many indentChars to indent by, disable
     * indentation if negative (zero means new line only)
     * @param newLine sequence of characters used for a line ending
     * (<code>null</code> means use the single character '\n')
     */
    public MessageOptions(char indentChar, int indentCount, String newLine) {
        this();
        setIndentChar(indentChar);
        setIndentCount(indentCount);
        setNewLine(newLine);
    }

    /**
     * Construct with specified message encoding and formatting, with default XML declaration containing XML version 
     * only.
     *  
     * @param encoding the encoding to apply to the message
     * @param indentChar the whitespace character to use for indenting XML.
     * @param indentCount how many indentChars to indent by, disable
     * indentation if negative (zero means new line only)
     * @param newLine sequence of characters used for a line ending
     * (<code>null</code> means use the single character '\n')
     */
    public MessageOptions(XmlEncoding encoding, char indentChar, int indentCount, String newLine) {
        this(encoding);
        setIndentChar(indentChar);
        setIndentCount(indentCount);
        setNewLine(newLine);
    }

    /**
     * Construct with specified message encoding, formatting and XML declaration.
     *  
     * @param encoding the encoding to apply to the message
     * @param includeEncodingDecl <code>true</code> to include the encoding in the XML declaration, <code>false</code>
     * omits the encoding in the XML declaration.  
     * @param standaloneDecl <code>true</code> to include <code>standalone="yes"</code> in the XML declaration, 
     * <code>false</code> to include <code>standalone="no"</code> in the XML declaration, <code>null</code> to exclude 
     * the standalone attribute from the XML declaration
     * @param indentChar the whitespace character to use for indenting XML
     * @param indentCount how many indentChars to indent by, disable
     * indentation if negative (zero means new line only)
     * @param newLine sequence of characters used for a line ending
     * (<code>null</code> means use the single character '\n')
     */
    public MessageOptions(XmlEncoding encoding, boolean includeEncodingDecl, Boolean standaloneDecl, char indentChar,
            int indentCount, String newLine) {
        this(encoding);
        setIncludeEncodingDecl(includeEncodingDecl);
        setStandaloneDecl(standaloneDecl);
        setIndentChar(indentChar);
        setIndentCount(indentCount);
        setNewLine(newLine);
    }

    /**
     * Gets the encoding to use for the message.
     *
     * @return encoding to use
     */
    public XmlEncoding getEncoding() {
        return m_xmlOptions.getEncoding();
    }

    /**
     * Sets the encoding to use for the message.
     *
     * @param encoding to use
     */
    public void setEncoding(XmlEncoding encoding) {
        m_xmlOptions.setEncoding(encoding);
    }
    
    /**
     * Get the media type short code to be used for output.
     * 
     * @return media type short code (e.g. "xml" or "x-xbis")
     */
    public String getOutMediaTypeCode() {
        return m_outMediaTypeCode;
    }

    /**
     * Set the media type short code to be used for output.
     * 
     * @param code media type short code (e.g. "xml" or "x-xbis")
     */
    public void setOutMediaTypeCode(String code) {
        m_outMediaTypeCode = code;
    }

    /**
     * Get the media type short code(s) to be accepted for input.
     * 
     * @return accepted media type short codes (e.g. "xml", "x-xbis") (<code>null</code> if unspecified) 
     */
    public String[] getInMediaTypeCodes() {
        return m_inMediaTypeCodes;
    }
    
    /**
     * Set the media type short code(s) to be accepted for input.
     * 
     * @param codes accepted media type short codes (e.g. "xml", "x-xbis") (<code>null</code> if unspecified)
     */
    public void setInMediaTypeCodes(String[] codes) {
        m_inMediaTypeCodes = new String[codes.length];
        System.arraycopy(codes, 0, m_inMediaTypeCodes, 0, codes.length);
    }
    
    /**
     * Get the number of indent characters to indent the XML by. Disables indentation if negative (zero means new line
     * only).
     * 
     * @return indentCount
     */
    public int getIndentCount() {
        return m_xmlOptions.getIndentCount();
    }

    /**
     * Set the number of indent characters to indent the XML by. Disables indentation if negative (zero means new line
     * only).
     * 
     * @param indentCount the number of indent characters
     */
    public void setIndentCount(int indentCount) {
        m_xmlOptions.setIndentCount(indentCount);
    }

    /**
     * Get the character to use for indenting the XML.
     * 
     * @return indentChar
     */
    public char getIndentChar() {
        return m_xmlOptions.getIndentChar();
    }

    /**
     * Set the character to use for indenting the XML.
     * 
     * @param indentChar the indent character
     */
    public void setIndentChar(char indentChar) {
        m_xmlOptions.setIndentChar(indentChar);
    }

    /**
     * Get the string to use for new lines.
     * 
     * @return newLine
     */
    public String getNewLine() {
        return m_xmlOptions.getNewLine();
    }

    /**
     * Set the string to use for new lines.
     * 
     * @param newLine the new line separator
     */
    public void setNewLine(String newLine) {
        m_xmlOptions.setNewLine(newLine);
    }

    /**
     * Get the setting for the standalone attribute of the XML declaration. 
     * 
     * @return <code>true</code> to include <code>standalone="yes"</code> in the XML declaration, 
     * <code>false</code> to include <code>standalone="no"</code> in the XML declaration, <code>null</code> to exclude 
     * the standalone attribute from the XML declaration,
     */
    public Boolean getStandaloneDecl() {
        return m_xmlOptions.getStandaloneDecl();
    }

    /**
     * Set the standalone attribute of the XML declaration. 
     * 
     * @param standaloneDecl <code>true</code> to include <code>standalone="yes"</code> in the XML declaration, 
     * <code>false</code> to include <code>standalone="no"</code> in the XML declaration, <code>null</code> to exclude 
     * the standalone attribute from the XML declaration,
     * 
     */
    public void setStandaloneDecl(Boolean standaloneDecl) {
        m_xmlOptions.setStandaloneDecl(standaloneDecl);
    }

    /**
     * Get the setting for whether to include the encoding attribute in the XML declaration. 
     *
     * @return <code>true</code> to include the encoding in the XML declaration, <code>false</code> to omit the encoding
     * in the XML declaration.
     */
    public boolean isIncludeEncodingDecl() {
        return m_xmlOptions.isIncludeEncodingDecl();
    }

    /**
     * Set whether to include the encoding attribute in the XML declaration.
     *
     * @param includeEncodingDecl <code>true</code> to include the encoding in the XML declaration, <code>false</code> 
     * to omit the encoding in the XML declaration.
     */
    public void setIncludeEncodingDecl(boolean includeEncodingDecl) {
        m_xmlOptions.setIncludeEncodingDecl(includeEncodingDecl);
    }
    
    /**
     * Returns the value of the encoding attribute for the XML declaration.
     *
     * @return the value of the encoding attribute, <code>null</code> for no encoding attribute.
     */
    public String getEncodingDeclString() {
        return m_xmlOptions.getEncodingDeclString();
    }

    /**
     * Returns the value of the standalone attribute for the XML declaration.
     *
     * @return the value of the standalone attribute, <code>null</code> for no standalone attribute.
     */
    public String getStandaloneDeclString() {
        return m_xmlOptions.getStandaloneDeclString();
    }

    /**
     * Returns the Xml options for the message.
     *
     * @return xml formatting options
     */
    public XmlOptions getXmlOptions() {
        return m_xmlOptions;
    }
}
