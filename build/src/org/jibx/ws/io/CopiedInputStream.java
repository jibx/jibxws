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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Duplicating input stream. This acts as a splitter, writing all data read to a supplied output stream.
 * 
 * @author Dennis M. Sosnoski
 */
public class CopiedInputStream extends FilterInputStream
{
    /** Duplicate output stream. */
    private final OutputStream m_stream;
    
    /**
     * Constructor.
     * 
     * @param in stream supplying data
     * @param out destinator for copy of all data read
     */
    public CopiedInputStream(InputStream in, OutputStream out) {
        super(in);
        m_stream = out;
    }
    
    /** {@inheritDoc} */
    public void close() throws IOException {
        super.close();
        m_stream.close();
    }
    
    /** {@inheritDoc} */
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Method not supported");
    }
    
    /** {@inheritDoc} */
    public boolean markSupported() {
        return false;
    }
    
    /** {@inheritDoc} */
    public int read(byte[] b, int off, int len) throws IOException {
        int actual = super.read(b, off, len);
        if (actual > 0) {
            m_stream.write(b, off, actual);
        }
        return actual;
    }
    
    /** {@inheritDoc} */
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException("Method not supported");
    }
    
    /** {@inheritDoc} */
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException("Method not supported");
    }

    /** {@inheritDoc} */
    public int read() throws IOException {
        int chr = super.read();
        if (chr >= 0) {
            m_stream.write(chr);
        }
        return chr;
    }
}
