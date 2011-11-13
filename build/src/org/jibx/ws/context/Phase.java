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

package org.jibx.ws.context;

/**
 * Represents a phase of message processing.  Common phases are defined as constants in this class.  Phases that are
 * specific to a protocol are defined in a class specific to that protocol.
 * 
 * @see org.jibx.ws.soap.SoapPhase for phases specific to SOAP processing
 * @author Nigel Charman
 */
public final class Phase
{
    /**
     * Represents the processing of the body of a message.
     */
    public static final Phase BODY = new Phase("body");

    private String m_name;

    /**
     * Constructor.
     * 
     * @param name a short name for this phase
     */
    public Phase(String name) {
        m_name = name;
    }

    /**
     * Returns the name of this phase.
     *
     * @return a short name describing the phase
     */
    public String getName() {
        return m_name;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        if (m_name == null) {
            return 0;
        }
        return m_name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Phase)) {
            return false;
        }
        final Phase other = (Phase) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }
}
