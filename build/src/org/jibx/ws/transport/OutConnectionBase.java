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

package org.jibx.ws.transport;

import java.io.IOException;

import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.io.XmlOptions;

/**
 * Simple implementation of {@link OutConnection} which handles indentation and writer construction.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class OutConnectionBase implements OutConnection
{
    /** Indentation count per nesting level. */
    private int m_indentCount;
    
    /** Character used for indentation. */
    private char m_indentChar;
    
    /** Newline sequence. */
    private String m_newline;
    
    /** Include XML declaration at start of document flag. */
    private boolean m_includeDecl;
    
    /** Character encoding to use for XML declaration at start of document. */
    private String m_charEncoding;
    
    /** Text to use for 'standalone' attribute of XML declaration. */
    private String m_standaloneText;
    
    /**
     * Constructor from xml options.
     * 
     * @param opts XML formatting options
     */
    public OutConnectionBase(XmlOptions opts) {
        setIndentSpaces(opts.getIndentCount(), opts.getNewLine(), opts.getIndentChar());
        boolean incl = opts.getEncodingDeclString() != null || opts.getStandaloneDeclString() != null;
        setXmlDeclaration(incl, opts.getEncodingDeclString(), opts.getStandaloneDeclString());
    }

    /**
     * {@inheritDoc}
     */
    public void setIndentSpaces(int count, String newline, char indent) {
        m_indentCount = count;
        m_newline = newline;
        m_indentChar = indent;
    }

    /**
     * {@inheritDoc}
     */
    public void setXmlDeclaration(boolean incl, String charcode, String standalone) {
        m_includeDecl = incl;
        m_charEncoding = charcode;
        m_standaloneText = standalone;
    }
    
    /**
     * Initialize the writer. This sets the indentation to be used for output, and also generates the XML declaration
     * at the start of the output document, if configured.
     * 
     * @param writer writer
     * @throws IOException on I/O error
     */
    protected void initializeWriter(IXMLWriter writer) throws IOException {
        writer.init();
        writer.setIndentSpaces(m_indentCount, m_newline, m_indentChar);
        if (m_includeDecl) {
            writer.writeXMLDecl("1.0", m_charEncoding, m_standaloneText);
        }
    }
}
