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
import java.io.OutputStream;

import org.jibx.ws.context.MessageContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.CopiedOutputStream;

/**
 * An interceptor that sniffs the output on the way through.  
 * <p>It can run in two modes.
 * <ol>
 * <li>If constructed with a callback, on completion it will invoke the callback with a copy of the input. This is 
 * convenient for use on the server side when using servlets (In order to access the outbound 
 * message context on the server, you will need to implement an 
 * {@link org.jibx.ws.transport.OutputCompletionListener}.</li>
 * <li>If constructed with an output stream, it will copy the output to that output stream. This is convenient for 
 * using on the client side.</li>
 * </ol>   
 * 
 * @author Nigel Charman
 */
public final class CopiedOutputStreamInterceptor implements OutputStreamInterceptor
{
    private static final int INITIAL_BUFFER_SIZE = 4096;
    private final OutputStream m_copyStream;
    private final String m_attributeName;
    private MessageContext m_ctx;
    
    /**
     * Constructor. When the input is complete, this will put the copy of the input into an attribute in the current 
     * {@link MessageContext}. 
     * 
     * @param attributeName the name of the attribute in which to store the result
     */
    public CopiedOutputStreamInterceptor(String attributeName) {
        m_attributeName = attributeName;
        m_copyStream = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
    }
    
    /**
     * Constructor. This will duplicate the output to the specified output stream.
     * 
     * @param copyStream the stream to copy the output to
     */
    public CopiedOutputStreamInterceptor(OutputStream copyStream) {
        m_attributeName = null;
        m_copyStream = copyStream;
    }
    
    /** {@inheritDoc} */
    public void setMessageContext(OutContext ctx) { 
        m_ctx = ctx;
    }

    /** {@inheritDoc} */
    public OutputStream intercept(OutputStream outputStream) {
        return new CopiedOutputStream(outputStream, m_copyStream);
    }

    /** {@inheritDoc} */
    public void outputComplete() {
        if (m_ctx != null && m_attributeName != null) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) m_copyStream;
            m_ctx.setAttribute(m_attributeName, baos.toByteArray());
            baos.reset();
        }
    }
}
