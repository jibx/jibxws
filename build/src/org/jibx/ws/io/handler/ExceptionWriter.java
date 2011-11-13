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
import java.io.PrintWriter;
import java.io.StringWriter;

import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.WsException;
import org.jibx.ws.context.OutContext;

/**
 * Writes an exception as an XML element.
 * 
 * @author Nigel Charman
 */
public final class ExceptionWriter implements OutHandler
{
    private static final String ELEMENT_NAME = "Exception";
    private static final String MESSAGE_SEPARATOR = ": ";

    private final Throwable m_throwable;
    private final boolean m_includeStackTrace;

    /**
     * Writes an exception as an XML element, with an element name of &lt;Exception&gt;.
     * 
     * @param t the exception to write
     * @param includeStackTrace true if a stack trace should be included in the XML.
     */
    public ExceptionWriter(Throwable t, boolean includeStackTrace) {
        m_throwable = t;
        m_includeStackTrace = includeStackTrace;
    }

    /**
     * Writes the throwable to the <code>xmlWriter</code>, wrapped in an &lt;Exception&gt; element.
     * 
     * {@inheritDoc}
     */
    public void invoke(OutContext context, IXMLWriter xmlWriter) throws IOException, WsException {
        xmlWriter.startTagClosed(0, ELEMENT_NAME);
        if (m_includeStackTrace) {
            StringWriter sw = new StringWriter();
            m_throwable.printStackTrace(new PrintWriter(sw));
            xmlWriter.writeTextContent(sw.toString());
        } else {
            xmlWriter.writeTextContent(m_throwable.getClass().getName() + MESSAGE_SEPARATOR
                + m_throwable.getLocalizedMessage());
        }
        xmlWriter.endTag(0, ELEMENT_NAME);
    }
}
