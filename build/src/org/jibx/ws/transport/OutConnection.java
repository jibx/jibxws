/*
 * Copyright (c) 2007-2008, Sosnoski Software Associates Limited. All rights reserved.
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
import org.jibx.ws.WsException;

/**
 * Represents a one way connection used for sending a message.
 * 
 * @author Nigel Charman
 */
public interface OutConnection
{
    /**
     * Set nesting indentation. This is advisory only, and implementations of this interface are free to ignore it. The
     * intent is to indicate that the generated output should use indenting to illustrate element nesting. To be
     * effective, this method should be called before the first call get a writer.
     * 
     * @param count number of character to indent per level, or disable indentation if negative (zero means new line
     * only)
     * @param newline sequence of characters used for a line ending (<code>null</code> means use the single character
     * '\n')
     * @param indent whitespace character used for indentation
     */
    void setIndentSpaces(int count, String newline, char indent);
    
    /**
     * Set the XML declaration information. This is advisory only, and implementations of this interface are free to
     * ignore it. The indent is to control whether the generated output document includes an XML declaration, and if it
     * does, to provide the values for attributes of the declaration.
     * 
     * @param incl include XML declaration flag
     * @param charcode character code for XML declaration (<code>null</code> if none)
     * @param standalone text for standalone attributes of XML declaration (<code>null</code> if none)
     */
    void setXmlDeclaration(boolean incl, String charcode, String standalone);
    
    /**
     * Get a writer for normal XML data to be sent.
     * 
     * @param uris ordered array of URIs for namespaces used in document
     * @return writer (<code>null</code> if normal response not supported)
     * @throws IOException on I/O error
     * @throws WsException on JiBX error 
     */
    IXMLWriter getNormalWriter(String[] uris) throws IOException, WsException;
    
    /**
     * Get a writer for fault data to be sent.
     * 
     * @param uris ordered array of URIs for namespaces used in document
     * @return writer (<code>null</code> if fault response not supported)
     * @throws IOException on I/O error
     * @throws WsException on JiBX error 
     */
    IXMLWriter getFaultWriter(String[] uris) throws IOException, WsException;
    
    /**
     * Indicates that the writing of the output is complete.
     */
    void outputComplete();
    
    /**
     * End the writer usage and release resources.
     * 
     * @throws IOException on I/O error
     */
    void close() throws IOException;
}
