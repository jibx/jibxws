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

package org.jibx.ws.http.client.javase;

import java.net.MalformedURLException;
import java.net.URL;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.transport.Channel;
import org.jibx.ws.transport.Transport;
import org.jibx.ws.transport.TransportOptions;

/**
 * Transport implementation for HTTP protocol.
 * 
 * @author Dennis M. Sosnoski
 */
public class HttpTransport implements Transport
{
    /** 
     * {@inheritDoc} 
     * @param transportOptions options for customizing the transport. For HttpChannel, this must be an object of type 
     * {@link HttpTransportOptions}.
     */
    public Channel buildDuplexChannel(String endpoint, TransportOptions transportOptions) 
            throws WsConfigurationException {
        try {
            URL url = new URL(endpoint);
            return new HttpChannel(url, (HttpTransportOptions)transportOptions);
        } catch (MalformedURLException e) {
            throw new WsConfigurationException("Unable to create URL for endpoint '" + endpoint + '\'', e);
        }
    }

    /** {@inheritDoc} */
    public TransportOptions newTransportOptions() {
        return new HttpTransportOptions();
    }
}
