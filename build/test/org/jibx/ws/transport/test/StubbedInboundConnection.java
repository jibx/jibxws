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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.io.XmlReaderWrapper;
import org.jibx.ws.transport.InConnection;

/**
 * A test stub that allows dummy data to be returned from the input stream.
 * 
 * @author Nigel Charman
 */
public final class StubbedInboundConnection implements InConnection
{
    private ByteArrayInputStream m_is;
    private String m_characterEncoding;
    private String m_contentType;
    private String m_destination;
    private String m_id;
    private String m_operationName;
    private String m_origin;
    private boolean m_hasError;

    // ============================================
    // Methods for providing the stubbed connection
    // ============================================
    /** {@inheritDoc} */
	public void init() throws IOException {
	}
	
    /**
     * Returns a reader containing the bytes previously set by calling {@link #setInBytes(byte[])}.
     * 
     * {@inheritDoc}
     */
	public IXMLReader getReader() throws IOException {
        if (m_is == null) {
            throw new IllegalStateException("Call setInBytes() before calling getReader() for testing");
        }
		try {
			return XmlReaderWrapper.createXmlReaderWrapper(m_is, "UTF-8").getReader();
		} catch (JiBXException e) {
			throw new IOException("Error intializing response reader: " + e.getMessage());
		}
	}

    /**
     * Get characterEncoding.
     *
     * @return characterEncoding
     */
    public String getCharacterEncoding() {
        return m_characterEncoding;
    }


    /**
     * Get contentType.
     *
     * @return contentType
     */
    public String getContentType() {
        return m_contentType;
    }

    /**
     * Get destination.
     *
     * @return destination
     */
    public String getDestination() {
        return m_destination;
    }


    /**
     * Get id.
     *
     * @return id
     */
    public String getId() {
        return m_id;
    }


    /**
     * Get operationName.
     *
     * @return operationName
     */
    public String getOperationName() {
        return m_operationName;
    }


    /**
     * Get origin.
     *
     * @return origin
     */
    public String getOrigin() {
        return m_origin;
    }

    /** {@inheritDoc} */
    public String getProperty(String name) {
        return null;
    }
    
    /** {@inheritDoc} */
    public boolean hasError() throws IOException {
        return m_hasError;
    }
    
    /** {@inheritDoc} */
    public String getErrorMessage() throws IOException {
        return null;
    }
    
    // ========================================
    // Methods for providing the test interface
    // ========================================
    /**
     * Set the data that will be returned in the input stream of this connection.
     * 
     * @param bytes data to be returned
     */
    public void setInBytes(byte[] bytes) {
        m_is = new ByteArrayInputStream(bytes);
    }

    /**
     * Set characterEncoding.
     *
     * @param characterEncoding encoding
     */
    public void setCharacterEncoding(String characterEncoding) {
        m_characterEncoding = characterEncoding;
    }

    /**
     * Set contentType.
     *
     * @param contentType content type
     */
    public void setContentType(String contentType) {
        m_contentType = contentType;
    }

    /**
     * Set destination.
     *
     * @param destination destination
     */
    public void setDestination(String destination) {
        m_destination = destination;
    }

    /**
     * Set id.
     *
     * @param id id
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * Set operationName.
     *
     * @param operationName operationName
     */
    public void setOperationName(String operationName) {
        m_operationName = operationName;
    }

    /**
     * Set origin.
     *
     * @param origin origin
     */
    public void setOrigin(String origin) {
        m_origin = origin;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
    }

    /**
     * Set hasError.
     *
     * @param hasError hasError
     */
    public void setHasError(boolean hasError) {
        m_hasError = hasError;
    }

    /** {@inheritDoc} */
    public void inputComplete() {
    }
}
