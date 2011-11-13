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

package org.jibx.ws.transport;

import java.io.IOException;

/**
 * Represents a communications link between two parties used for a single message exchange. 
 * 
 * Implementing classes may add transport dependent features.
 * 
 * @author Nigel Charman
 */
public interface DuplexConnection
{
    /**
     * Get the connection used to receive the response message. The {@link #getOutbound()} connection needs to be used
     * to send the request message before the inbound connection is available.
     * 
     * @return connection
     */
    InConnection getInbound();
    
    /**
     * Get the connection used to send a message. The outbound connection needs to be used before the inbound
     * connection is available.
     * 
     * @return connection
     */
    OutConnection getOutbound();
    
    /**
     * Close the connection. This should be called once usage of the connection is completed.
     * @throws IOException on error closing connection
     */
    void close() throws IOException;
}
