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

/**
 * Received message attributes. These may be supplied directly by the transport, or determined as part of the receive
 * message processing.
 * 
 * @author Dennis M. Sosnoski
 */
public interface InMessageAttributes
{
    /**
     * Get character encoding.
     * 
     * @return message body character encoding, or <code>null</code> if not known
     */
    String getCharacterEncoding();

    /**
     * Get content type.
     * 
     * @return message body content type, or <code>null</code> if none known
     */
    String getContentType();

    /**
     * Get operation name.
     * 
     * @return name of operation used to submit message, or <code>null</code> if no operations defined by transport
     */
    String getOperationName();

    /**
     * Get apparent message origin. The format of the origin is determined by the transport, but should be a unique
     * identifier within that transport.
     * 
     * @return message origin, or <code>null</code> if not known
     */
    String getOrigin();

    /**
     * Get message destination. The format of the destination is determined by the transport, but should be a unique
     * identifier within that transport.
     * 
     * @return message destination, or <code>null</code> if not known
     */
    String getDestination();

    /**
     * Get message ID. The format of the ID is determined by the transport, but should be a unique identifier within
     * that transport and origin.
     * 
     * @return message ID, or <code>null</code> if not known
     */
    String getId();
}
