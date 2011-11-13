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

import org.jibx.runtime.IXMLReader;
import org.jibx.ws.WsException;
import org.jibx.ws.context.InContext;

/**
 * Provides an interface for handlers to be invoked during the inbound processing of a message. The handlers have access
 * to both the {@link InContext} and the {@link IXMLReader}, and have the option of reading XML content.
 * 
 * @author Nigel Charman
 */
public interface InHandler
{
    /**
     * Invokes the handler. If the handler processes the current element it needs to parse past the end tag of that
     * element before returning.
     * 
     * @param context the context of the current message being received
     * @param xmlReader a reader for the XML message positioned at the start of the XML content for which the handler is
     * configured
     * @return the object the handler has read from the XML content, or <code>null</code> if the handler has not 
     * processed the XML content
     * @throws IOException on I/O error reading the XML content
     * @throws WsException on errors other than I/O errors
     */
    Object invoke(InContext context, IXMLReader xmlReader) throws IOException, WsException;
}
