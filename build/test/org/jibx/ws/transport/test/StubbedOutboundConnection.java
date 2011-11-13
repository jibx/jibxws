/*
Copyright (c) 2007-2008, Sosnoski Software Associates Limited
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.OutByteBuffer;
import org.jibx.runtime.impl.UTF8StreamWriter;
import org.jibx.ws.io.XmlOptions;
import org.jibx.ws.transport.OutConnectionBase;
import org.jibx.ws.transport.OutServerConnection;

/**
 * A Test Stub that spies on the data written to the output stream.
 * 
 * @author Nigel Charman
 */
public class StubbedOutboundConnection extends OutConnectionBase implements OutServerConnection
{
    private static final int BUFFER_SIZE = 4096;
    private IXMLWriter m_writer;
    private ByteArrayOutputStream m_os;
    
    /**
     * Constructor. This passes the options on to the base class handling.
     * 
     * @param options xml formatting options
     */
    public StubbedOutboundConnection(XmlOptions options) {
        super(options);
    }

    // ============================================
    // Methods for providing the stubbed connection
    // ============================================
    /**
     * Returns a writer that is being spied upon.  Call {@link #getOutBytes()} to get the bytes written to this writer.
     *  
     * {@inheritDoc}
     */
	public IXMLWriter getNormalWriter(String[] uris) throws IOException {
	    if (m_writer == null) {
    	    UTF8StreamWriter writer = new UTF8StreamWriter(uris);
            m_os = new ByteArrayOutputStream(BUFFER_SIZE);
            OutByteBuffer buff = new OutByteBuffer();
            buff.setOutput(m_os);
            writer.setBuffer(buff);
            initializeWriter(writer);
            m_writer = writer;
	    }
        return m_writer;
	}

    /** {@inheritDoc} */
	public void close() throws IOException {
	    if (m_writer != null) {
	        m_writer.close();
	    }
	}

    // ========================================
    // Methods for providing the test interface
    // ========================================
    /**
     * Get the bytes that have be written to the output stream of this connection.
     *
     * @return bytes data already written
     */
    public byte[] getOutBytes() {
        if (m_os == null) {
            return null;
        } else {
            return m_os.toByteArray();
        }
    }

    /** {@inheritDoc} */
    public IXMLWriter getFaultWriter(String[] uris) throws IOException {
        return getNormalWriter(uris);
    }

    /** {@inheritDoc} */
    public boolean isCommitted() {
        return false;
    }

    /** {@inheritDoc} */
    public void sendNotFoundError() throws IOException {
    }

    /** {@inheritDoc} */
    public void setInternalServerError() {
    }

    /** {@inheritDoc} */
    public void outputComplete() {
    }
}
