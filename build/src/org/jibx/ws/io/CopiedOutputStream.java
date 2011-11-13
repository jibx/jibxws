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

package org.jibx.ws.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Duplicating output stream. This acts as a splitter, feeding all data written to each supplied stream.
 * 
 * @author Dennis M. Sosnoski
 */
public class CopiedOutputStream extends OutputStream
{
    /** First output stream. */
    private final OutputStream m_stream1;
    
    /** Second output stream. */
    private final OutputStream m_stream2;
    
    /**
     * Constructor.
     * 
     * @param out1 first output stream
     * @param out2 second output stream
     */
    public CopiedOutputStream(OutputStream out1, OutputStream out2) {
        m_stream1 = out1;
        m_stream2 = out2;
    }
    
    /** {@inheritDoc} */
    public void close() throws IOException {
        m_stream1.close();
        m_stream2.close();
    }
    
    /** {@inheritDoc} */
    public void flush() throws IOException {
        m_stream1.flush();
        m_stream2.flush();
    }
    
    /** {@inheritDoc} */
    public void write(int b) throws IOException {
        m_stream1.write(b);
        m_stream2.write(b);
    }
    
    /** {@inheritDoc} */
    public void write(byte[] b, int off, int len) throws IOException {
        m_stream1.write(b, off, len);
        m_stream2.write(b, off, len);
    }
    
    /** {@inheritDoc} */
    public void write(byte[] b) throws IOException {
        m_stream1.write(b);
        m_stream2.write(b);
    }
}
