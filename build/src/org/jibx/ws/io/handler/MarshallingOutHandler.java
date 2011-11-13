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
package org.jibx.ws.io.handler;

import java.io.IOException;

import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.WsException;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;

/**
 * Payload marshaller, where the payload that is passed to the constructor is marshalled on a subsequent call to 
 * {@link #invoke(OutContext, IXMLWriter)}.
 * 
 *  @author Nigel Charman
 */
public class MarshallingOutHandler implements OutHandler
{
    private final Object m_payload;
    
    /**
     * Constructor.
     * 
     * @param payload the payload to be marshalled
     */
    public MarshallingOutHandler(Object payload) {
        m_payload = payload;
    }

    /** {@inheritDoc} */
    public void invoke(OutContext context, IXMLWriter xmlWriter) throws IOException, WsException {
        MarshallingPayloadWriter marshaller = new MarshallingPayloadWriter(m_payload.getClass());
        marshaller.invoke(xmlWriter, m_payload);
    }
}
