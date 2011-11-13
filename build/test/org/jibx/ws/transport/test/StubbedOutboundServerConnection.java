/*
Copyright (c) 2007, Sosnoski Software Associates Limited
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.ws.transport.test;

import java.io.IOException;

import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.transport.OutServerConnection;

/**
 * A Test Stub that spies on the data written to the output stream and on errors set on the connection.  
 * 
 * @author Nigel Charman
 */ 
public final class StubbedOutboundServerConnection extends StubbedOutboundConnection implements OutServerConnection
{
    private boolean m_committed = false;

    private boolean m_notFoundError = false;

    private boolean m_internalServerError = false;
    
    /** Constructor. */
    public StubbedOutboundServerConnection() {
        super(new XmlOptions());
    }


    // ============================================
    // Methods for providing the stubbed connection
    // ============================================
    /**
     * Returns the value set by {@link #setCommitted(boolean)} or false if this has not been called.
     *
     * {@inheritDoc}
     */
    public boolean isCommitted() {
        return m_committed;
    }

    /**
     * {@inheritDoc}
     */
    public void sendNotFoundError() throws IOException {
        m_notFoundError = true;
    }

    /**
     * {@inheritDoc}
     */
    public void setInternalServerError() {
        m_internalServerError = true;
    }

    // ========================================
    // Methods for providing the test interface
    // ========================================
    /**
     * Returns true if {@link #setInternalServerError()} has been called, false otherwise.
     *
     * @return internal server error flag
     */
    public boolean isInternalServerError() {
        return m_internalServerError;
    }

    /**
     * Returns true if {@link #sendNotFoundError()} has been called, false otherwise.
     *
     * @return is not found error flag
     */
    public boolean isNotFoundError() {
        return m_notFoundError;
    }

    /**
     * Allows the commit state to be set, controls the value returned by {@link #isCommitted()}. 
     *
     * @param committed mock setting
     */
    public void setCommitted(boolean committed) {
        m_committed = committed;
    }
}
