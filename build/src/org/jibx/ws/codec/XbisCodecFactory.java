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
import org.xbis.JibxReader;
import org.xbis.JibxWriter;

/**
 * XBIS XML encoding factory. This creates encoding implementations using XBIS input and output.
 * 
 * @author Dennis M. Sosnoski
 */
public final class XbisCodecFactory implements CodecFactory
{
    /**
     * {@inheritDoc}
     */
    public XmlCodec createInstance(MediaType mediaType) {
        return new XbisCodec(mediaType);
    }
    
    /**
     * XBIS XML codec implementation. This reads and writes XBIS compact XML format. It currently ignores all formatting
     * options, including the character encoding.
     * 
     * @author Dennis M. Sosnoski
     */
    private static final class XbisCodec implements XmlCodec
    {
        /** Media type supported by codec. */
        private final MediaType m_mediaType;
        
        /** Current writer instance (<code>null</code> if no writer created). */
        private JibxWriter m_writer;
        
        /** Current reader instance (<code>null</code> if no reader created). */
        private JibxReader m_reader;
        
        /**
         * Constructor.
         * 
         * @param mediaType media type that this codec is implementing
         */
        public XbisCodec(MediaType mediaType) {
            m_mediaType = mediaType;
        }
        
        /**
         * Get the media type that this codec is implementing.
         * 
         * @return name
         */
        public MediaType getMediaType() {
            return m_mediaType;
        }
        
        /**
         * Initialize XBIS writer. This must be called before beginning a new output operation using the codec. The
         * returned writer instance may be the same one previously returned, or a new one.
         * 
         * @param buff output buffer to be used
         * @param charcode (ignored)
         * @param uris ordered array of URIs for namespaces used in document
         * @return writer configured for the specified namespaces
         * @throws IOException on error setting output
         */
        public IXMLWriter getWriter(IOutByteBuffer buff, String charcode, String[] uris) throws IOException {
            if (m_writer == null) {
                m_writer = new JibxWriter(uris, buff);
            } else {
                m_writer.setNamespaceUris(uris);
                m_writer.setBuffer(buff);
            }
            return m_writer;
        }
        
        /**
         * Initialize XML reader. This must be called before beginning a new input operation using the codec. The
         * returned reader instance may be the same one previously returned, or a new one.
         * 
         * @param buff input buffer to be used
         * @param charcode (ignored)
         * @param name document name (<code>null</code> if unknown)
         * @param reset force reader reset (previous state cleared, even if same buffer)
         * @return reader configured for the input stream and character encoding
         * @throws IOException on error reading input stream
         */
        public IXMLReader getReader(IInByteBuffer buff, String charcode, String name, boolean reset)
        throws IOException {
            if (m_reader == null) {
                m_reader = new JibxReader(buff);
            } else {
                m_reader.setBuffer(buff);
            }
            if (reset) {
                m_reader.reset();
            }
            return m_reader;
        }
    }
}
