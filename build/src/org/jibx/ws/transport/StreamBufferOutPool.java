/*
 * Copyright (c) 2008-2009, Sosnoski Software Associates Limited. All rights reserved.
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

import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.ws.util.ExpandingPool;

/**
 * Pool of output stream buffers. The methods exposed by this class are not threadsafe, so the using code needs to
 * synchronize before accessing the pool.
 * 
 * @author Dennis M. Sosnoski
 */
public class StreamBufferOutPool extends ExpandingPool
{
    /** Number of bytes in initial buffer byte array. */
    private final int m_size;
    
    /**
     * Constructor.
     * 
     * @param size initial byte buffer size in bytes
     */
    public StreamBufferOutPool(int size) {
        m_size = size;
    }
    
    /**
     * Release a buffer, ending usage and returning it to the available list. Any exceptions thrown while ending usage
     * of the buffer are ignored.
     * 
     * @param buff buffer to release
     */
    public void endUsage(OutByteBuffer buff) {
        try {
            buff.finish();
        } catch (Exception e) { /* deliberately left empty */ }
        buff.reset();
        super.releaseInstance(buff);
    }

    /**
     * Create new instance for pool.
     * 
     * @return instance
     */
    protected Object createInstance() {
        return new OutByteBuffer(m_size);
    }
}
