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

package org.jibx.ws.codec;

import java.io.IOException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.IInByteBuffer;
import org.jibx.runtime.impl.IOutByteBuffer;

/**
 * Encoding access interface. This just provides access to XML readers and writers supporting the encoding. Instances of
 * this class are not threadsafe, and if shared between threads require a synchronization step before reuse.
 * 
 * @author Dennis M. Sosnoski
 */
public interface XmlCodec
{
    /**
     * Get the media type implemented by this codec.
     * 
     * @return type
     */
    MediaType getMediaType();
    
    /**
     * Initialize writer for this codec. This must be called before beginning a new output operation using the codec.
     * The returned writer instance may be the same one previously returned, or a new one.
     * 
     * @param buff output buffer to be used
     * @param charcode character encoding to be used (<code>null</code> if unspecified, ignored if not supported)
     * @param uris ordered array of URIs for namespaces used in document
     * @return writer configured for the specified character encoding and namespaces
     * @throws IOException on error setting output 
     */
    IXMLWriter getWriter(IOutByteBuffer buff, String charcode, String[] uris) throws IOException;
    
    /**
     * Initialize reader for this codec. This must be called before beginning a new input operation using the codec. The
     * returned reader instance may be the same one previously returned, or a new one.
     * 
     * @param buff input buffer to be used
     * @param charcode character encoding of document (<code>null</code> if unspecified, ignored if not supported)
     * @param name document name (<code>null</code> if unknown)
     * @param reset force reader reset (previous state cleared, even if same buffer)
     * @return reader configured for the input stream and character encoding
     * @throws IOException on error reading input stream
     */
    IXMLReader getReader(IInByteBuffer buff, String charcode, String name, boolean reset) throws IOException;
 }
