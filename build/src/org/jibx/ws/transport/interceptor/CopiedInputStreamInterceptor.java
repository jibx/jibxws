/*
 * Copyright (c) 2009, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.transport.interceptor;
        
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jibx.ws.context.InContext;
import org.jibx.ws.context.MessageContext;
import org.jibx.ws.io.CopiedInputStream;

/**
 * An interceptor that sniffs the input on the way through.  
 * <p>It can run in two modes.
 * <ol>
 * <li>If constructed with an attribute name, on completion it will put the result into an attribute in the current 
 * {@link MessageContext}. This is convenient for use on the server side.</li>
 * <li>If constructed with an output stream, it will copy the input to that output stream. This is convenient for 
 * use on the client side.</li>
 * </ol>   
 * 
 * @author Nigel Charman
 */
public final class CopiedInputStreamInterceptor implements InputStreamInterceptor
{
    private static final int INITIAL_BUFFER_SIZE = 4096;
    private final OutputStream m_os;
    private final String m_attributeName;
    private MessageContext m_ctx;

    /**
     * Constructor. When the input is complete, this will put the copy of the input into an attribute in the current 
     * {@link MessageContext}. 
     * 
     * @param attributeName the name of the attribute in which to store the result
     */
    public CopiedInputStreamInterceptor(String attributeName) {
        m_attributeName = attributeName;
        m_os = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
    }

    /**
     * Constructor. This will duplicate the input to the specified output stream.
     * 
     * @param copyStream the stream to copy the input to
     */
    public CopiedInputStreamInterceptor(OutputStream copyStream) {
        m_attributeName = null;
        m_os = copyStream;
    }
    
    /** {@inheritDoc} */
    public void setMessageContext(InContext ctx) { 
        m_ctx = ctx;
    }

    /** {@inheritDoc} */
    public InputStream intercept(InputStream inputStream) {
        return new CopiedInputStream(inputStream, m_os);
    }

    /** {@inheritDoc} */
    public void inputComplete() {
        if (m_ctx != null && m_attributeName != null) {
            ByteArrayOutputStream baos = ((ByteArrayOutputStream)m_os);
            m_ctx.setAttribute(m_attributeName, baos.toByteArray());
            baos.reset();
        }
    }
}
