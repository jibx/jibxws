/*
 * Copyright (c) 2007-2009, Sosnoski Software Associates Limited. All rights reserved.
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

import java.io.IOException;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.io.XmlOptions;

/**
 * Represents a persistent communications link between two parties. This isolates the client code from issues with
 * transport reuse, while allowing for efficient persistent links with supported transports. Although the most common
 * case is for channels to use a single transport in both directions, channels with different inbound and outbound
 * transports are also supported, as are inbound- and outbound-only channels.
 * 
 * @author Dennis M. Sosnoski
 */
public interface Channel
{
    /**
     * Get a duplex connection for this channel. This method must be called for each request-response message exchange.
     * @param msgProps message specific properties
     * @param xmlOptions XML formatting options
     * @return connection
     * @throws IOException on I/O error
     * @throws WsConfigurationException on configuration error
     */
    DuplexConnection getDuplex(MessageProperties msgProps, XmlOptions xmlOptions) throws IOException, 
        WsConfigurationException;
    
    /**
     * Get a send-only connection for this channel. This method must be called for each message to be sent without a
     * response.
     * @param msgProps message specific properties
     * @param xmlOptions XML formatting options
     * @return connection
     * @throws IOException on I/O error
     * @throws WsConfigurationException on configuration error
     */
    OutConnection getOutbound(MessageProperties msgProps, XmlOptions xmlOptions) throws IOException, 
        WsConfigurationException;
    
    /**
     * Get a receive-only connection for this channel. This method must be called for each message to be received, and
     * will wait for a message to be available before returning.
     * 
     * @return connection
     * @throws IOException on I/O error
     * @throws WsConfigurationException on configuration error
     */
    InConnection getInbound() throws IOException, WsConfigurationException;
    
    /**
     * Close the channel. Implementations should disconnect and free any resources allocated to the channel.
     * @throws IOException on I/O error
     */
    void close() throws IOException;
}
