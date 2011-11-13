/*
 * Copyright (c) 2007-2008, Sosnoski Software Associates Limited All rights reserved.
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

package org.jibx.ws.transport.test;

import java.io.IOException;

import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnection;

/**
 * A Test Stub that spies on the data written to the output stream, and allows dummy data to be returned from the input
 * stream.
 * 
 * Typical usage would call {@link #setInBytes(byte[])} with the dummy data to be returned from the input stream before
 * using the connection. After using the connection, {{@link #getOutBytes()}} would be called to return the bytes
 * written to the output stream.
 * 
 * @author Nigel Charman
 */
public final class StubbedDuplexConnection implements DuplexConnection
{
    private final StubbedInboundConnection m_inbound;
    private final StubbedOutboundConnection m_outbound;
    private boolean m_disconnected = false;

    /**
     * Constructs a StubbedDuplexConnection.
     * 
     * @param options xml options to create outbound connection with
     */
    public StubbedDuplexConnection(XmlOptions options) {
        m_inbound = new StubbedInboundConnection();
        m_outbound = new StubbedOutboundConnection(options);
    }

    // ============================================
    // Methods for providing the stubbed connection
    // ============================================
    /**
     * {@inheritDoc}
     * 
     * @return returns a {@link StubbedInboundConnection}
     */
    public InConnection getInbound() {
        return m_inbound;
    }

    /**
     * {@inheritDoc}
     * 
     * @return returns a {@link StubbedOutboundConnection}
     */
    public OutConnection getOutbound() {
        return m_outbound;
    }

    /**
     * Sets a flag that can be interrogated with {@link #isDisconnected()}.
     */
    public void disconnect() {
        m_disconnected = true;
    }

    // ========================================
    // Methods for providing the test interface
    // ========================================
    /**
     * Set the data that will be returned in the input stream of this connection.
     * 
     * @param bytes data to be returned
     */
    void setInBytes(byte[] bytes) {
        m_inbound.setInBytes(bytes);
    }

    /**
     * Get the bytes that have be written to the output stream of this connection.
     * 
     * @return bytes data already written
     */
    byte[] getOutBytes() {
        return m_outbound.getOutBytes();
    }

    /**
     * Returns whether {@link #disconnect()} has been called.
     * 
     * @return disconnected
     */
    public boolean isDisconnected() {
        return m_disconnected;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }
}
