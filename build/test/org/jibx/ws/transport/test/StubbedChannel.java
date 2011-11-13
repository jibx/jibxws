/*
 * Copyright (c) 2007-2008, Sosnoski Software Associates Limited. All rights reserved.
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
import org.jibx.ws.transport.Channel;
import org.jibx.ws.transport.DuplexConnection;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.OutConnection;

/**
 * A test stub that provides connections for testing and allows parameters to be spied on. Note that this is not thread
 * safe, so tests are to be run serially.
 * 
 * @author Nigel Charman
 */
public final class StubbedChannel implements Channel
{

    /** Singleton instance of class. */
    private static final StubbedChannel s_channel = new StubbedChannel();

    /** Data to be read. This is used by the next connection to be created, then set to <code>null</code>. */
    private byte[] m_indata;

    /** Data written. This is set by the close() method, so that the connection can be released. */
    private byte[] m_outdata;

    private StubbedDuplexConnection m_conn;
    private MessageProperties m_properties;

    private XmlOptions m_options;

    private StubbedChannel() {
        m_options = new XmlOptions();
    }

    // =========================================
    // Methods for providing the stubbed channel
    // =========================================
    /**
     * {@inheritDoc}
     */
    public DuplexConnection getDuplex(MessageProperties properties, XmlOptions xmlOptions) throws IOException {
        m_properties = properties;
        m_options = xmlOptions;
        return getConnection();
    }

    /**
     * {@inheritDoc}
     */
    public InConnection getInbound() throws IOException {
        return m_conn.getInbound();
    }

    /**
     * {@inheritDoc}
     */
    public OutConnection getOutbound(MessageProperties properties, XmlOptions xmlOptions) throws IOException {
        return getDuplex(properties, xmlOptions).getOutbound();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        m_outdata = (m_conn == null) ? null : m_conn.getOutBytes();
        m_conn = null;
        m_properties = null;
        m_indata = null;
    }

    // ========================================
    // Methods for providing the test interface
    // ========================================

    /**
     * Get the connection.
     * 
     * @return connection
     */
    public StubbedDuplexConnection getConnection() {
        if (m_conn == null) {
            m_conn = new StubbedDuplexConnection(m_options);
            if (m_indata != null) {
                m_conn.setInBytes(m_indata);
            }
        }
        return m_conn;
    }

    /**
     * Get the singleton instance.
     * 
     * @return instance
     */
    public static StubbedChannel getInstance() {
        return s_channel;
    }

    /**
     * Get the inbound connection directly.
     * 
     * @return inbound connection
     */
    public static StubbedInboundConnection getInConnection() {
        return (StubbedInboundConnection) s_channel.getConnection().getInbound();
    }

    /**
     * Get the outbound connection directly.
     * 
     * @return outbound connection
     */
    public static StubbedOutboundConnection getOutConnection() {
        return (StubbedOutboundConnection) s_channel.getConnection().getOutbound();
    }

    /**
     * Set the bytes to be read from the input stream of this channel.
     * 
     * @param bytes data to return
     */
    public static void setInBytes(byte[] bytes) {
        if (s_channel.m_conn == null) {
            s_channel.m_indata = bytes;
        } else {
            s_channel.m_conn.setInBytes(bytes);
        }
    }

    /**
     * Set the string to be read from the input stream of this channel.
     * 
     * @param data fake input
     */
    public static void setInput(String data) {
        setInBytes(data.getBytes());
    }

    /**
     * Get the bytes that have been written to the output stream of this channel.
     * 
     * @return bytes data already written
     */
    public static byte[] getOutBytes() {
        if (s_channel.m_conn == null) {
            return s_channel.m_outdata;
        } else {
            return s_channel.m_conn.getOutBytes();
        }
    }

    /**
     * Get the string that has been written to the output stream of this channel.
     * 
     * @return data already written
     */
    public static String getOutput() {
        return new String(getOutBytes());
    }

    /**
     * Returns the properties passed to the previous {@link #getDuplex(MessageProperties, XmlOptions)} or
     * {@link #getOutbound(MessageProperties, XmlOptions)} call.
     * 
     * @return properties
     */
    public static MessageProperties getProperties() {
        return getInstance().m_properties;
    }
}
