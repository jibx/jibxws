/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited All rights reserved.
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

import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnection;

/**
 * A Test Stub that spies on the data written to the output stream, and allows dummy data to be returned from the input
 * stream.
 * 
 * Test methods are available on the objects returned by {@link #getInbound()} and {@link #getOutbound()}.
 * 
 * Typical usage would call {@link StubbedInboundConnection#setInBytes(byte[])} with the dummy data to be returned from
 * the input stream before using the connection. After using the connection,
 * {@link StubbedOutboundServerConnection#getOutBytes()} would be called to return the bytes written to the output
 * stream.
 * 
 * @author Nigel Charman
 */
public final class StubbedDuplexServerConnection implements DuplexConnection
{
    private final StubbedInboundConnection m_inbound;
    private final StubbedOutboundServerConnection m_outbound;

    /**
     * Create the stubbed duplex connections.
     */
    public StubbedDuplexServerConnection() {
        m_inbound = new StubbedInboundConnection();
        m_outbound = new StubbedOutboundServerConnection();
    }

    /** {@inheritDoc} */
    public InConnection getInbound() {
        return m_inbound;
    }

    /** {@inheritDoc} */
    public OutConnection getOutbound() {
        return m_outbound;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }
}
