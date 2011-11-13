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

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.io.XmlReaderWrapper;

/**
 * Reads an exception from an XML element.
 * 
 * @author Nigel Charman
 */
public final class ExceptionReader implements InHandler
{
    private static final String ELEMENT_NAME = "Exception";

    /**
     * Reads an exception from an XML element, with an element name of &lt;Exception&gt;.
     * 
     * @param context the current inbound context
     * @param xmlReader the current xml reader
     * @return <code>true</code>
     * @throws IOException on I/O error
     * @throws WsException on parse error, or incorrect start tag
     */
    public Object invoke(InContext context, IXMLReader xmlReader) throws IOException, WsException {
        String content = null;
        XmlReaderWrapper rdr = XmlReaderWrapper.createXmlReaderWrapper(xmlReader);
        try {
            rdr.parsePastStartTag(null, ELEMENT_NAME);
            content = rdr.parseContentText(null, ELEMENT_NAME);
        } catch (JiBXException e) {
            throw new WsException("Unable to parse exception: ", e);
        }
        return content;
    }
}
