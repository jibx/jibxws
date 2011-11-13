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

package org.jibx.ws.util;

import java.util.ArrayList;

import org.jibx.ws.WsException;


/**
 * Growable pool of objects of some type. Subclasses are used to create new instances of the appropriate type as needed.
 * The methods defined by this class are not threadsafe, so the using code needs to synchronize before accessing the
 * pool.
 * 
 * TODO: implement timer thread to clean up the pools by deleting instances if no longer used
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class ExpandingPool
{
    /** Service instances currently available in pool. */
    private final ArrayList m_availables;
    
    /** Total number of service instances created and not released. */
    private int m_totalCount;
    
    /** Lowest number of available service instances since reset. */
    private int m_lowCount;
    
//    /** Number of passes since last create or release. */
//    private int m_passCount;
    
    /**
     * Constructor.
     */
    public ExpandingPool() {
        m_availables = new ArrayList();
    }
    
    /**
     * Create a new instance. This must be implemented by subclasses to create a new instance of the appropriate type
     * for the pool.
     * 
     * @return instance
     * @exception WsException on error creating instance
     */
    protected abstract Object createInstance() throws WsException;
    
    /**
     * Get an instance. This will either remove and return an available instance from this pool, or create a new
     * instance if none are currently available.
     * 
     * @return instance
     * @exception WsException on error creating instance
     */
    public Object getInstance() throws WsException {
        int count = m_availables.size();
        if (m_lowCount > count) {
            m_lowCount = count;
        }
        if (count == 0) {
            m_totalCount++;
            return createInstance();
        } else {
            return m_availables.remove(count - 1);
        }
    }
    
    /**
     * Release an instance, returning it to the available list.
     * 
     * @param inst instance to release
     */
    public void releaseInstance(Object inst) {
        m_availables.add(inst);
    }
}
