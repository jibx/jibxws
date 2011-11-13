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

package org.jibx.ws.codec;

import java.io.IOException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.IInByteBuffer;
import org.jibx.runtime.impl.IOutByteBuffer;
import org.jibx.runtime.impl.ISO88591StreamWriter;
import org.jibx.runtime.impl.IXMLReaderFactory;
import org.jibx.runtime.impl.InputStreamWrapper;
import org.jibx.runtime.impl.RuntimeSupport;
import org.jibx.runtime.impl.StreamWriterBase;
import org.jibx.runtime.impl.UTF8StreamWriter;

/**
 * Text XML encoding factory. This creates encoding implementations using normal text output.
 * 
 * @author Dennis M. Sosnoski
 */
public final class TextCodecFactory implements CodecFactory
{
    /**
     * Factory for creating XML readers.
     */
    private static final IXMLReaderFactory s_readerFactory = RuntimeSupport.loadFactory();

    /**
     * {@inheritDoc}
     */
    public XmlCodec createInstance(MediaType mediatype) {
        return new TextCodec(mediatype);
    }
    
    /**
     * Text XML encoding implementation. This uses normal text output, and supports the full range of XML formatting
     * options.
     * 
     * @author Dennis M. Sosnoski
     */
    public static final class TextCodec implements XmlCodec
    {
        /** Default output character encoding. */
        public static final String DEFAULT_ENCODING = "UTF-8";
        
        /** Media type implemented by codec. */
        private final MediaType m_mediaType;
        
        /** Current writer instance (<code>null</code> if no writer created). */
        private StreamWriterBase m_writer;
        
        /** Input stream wrapper used by reader (<code>null</code> if no reader created). */
        private InputStreamWrapper m_inWrapper;
        
        /** Current reader instance (<code>null</code> if no reader created). */
        private IXMLReader m_reader;
        
        /**
         * Constructor.
         * 
         * @param mediaType the media type that this codec is implementing.
         */
        public TextCodec(MediaType mediaType) {
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
         * Initialize text XML writer. This must be called before beginning a new output operation using the codec. The
         * returned writer instance may be the same one previously returned, or a new one.
         * 
         * @param buff output buffer to be used
         * @param charcode character encoding to be used (<code>null</code> if unspecified)
         * @param uris ordered array of URIs for namespaces used in document
         * @return writer configured for the specified character encoding and namespaces
         * @throws IOException on error setting output
         */
        public IXMLWriter getWriter(IOutByteBuffer buff, String charcode, String[] uris) throws IOException {
            if (m_writer != null && (charcode == null || charcode.equalsIgnoreCase(m_writer.getEncodingName()))) {
                
                // reuse the current writer
                m_writer.setNamespaceUris(uris);
                
            } else {
                
                // create a new writer instance
                if (charcode == null) {
                    charcode = DEFAULT_ENCODING;
                }
                if ("UTF-8".equalsIgnoreCase(charcode)) {
                    m_writer = new UTF8StreamWriter(uris);
                } else if ("ISO-8859-1".equalsIgnoreCase(charcode)) {
                    m_writer = new ISO88591StreamWriter(uris);
                } else {
                    throw new IllegalStateException("Unimplemented character encoding " + charcode);
                }
                
            }
            m_writer.setBuffer(buff);
            return m_writer;
        }
        
        /**
         * Initialize text XML reader. This must be called before beginning a new input operation using the codec. The
         * returned reader instance may be the same one previously returned, or a new one.
         * 
         * @param buff input buffer to be used
         * @param charcode character encoding of document (<code>null</code> if unspecified, ignored if not
         * supported)
         * @param name document name (<code>null</code> if unknown)
         * @param reset force reader reset (ignored for text input)
         * @return reader configured for the input stream and character encoding
         * @throws IOException on error reading input stream
         */
        public IXMLReader getReader(IInByteBuffer buff, String charcode, String name, boolean reset)
        throws IOException {
            if (m_inWrapper == null) {
                m_inWrapper = new InputStreamWrapper();
            }
            m_inWrapper.reset();
            m_inWrapper.setBuffer(buff);
            m_inWrapper.setEncoding(charcode);
            try {
                if (m_reader == null) {
                    m_reader = s_readerFactory.createReader(m_inWrapper.getReader(), name, true);
                } else {
                    m_reader = s_readerFactory.recycleReader(m_reader, m_inWrapper.getReader(), name);
                }
            } catch (JiBXException e) {
                throw new IOException("Error creating reader: " + e.getMessage());
            }
            return m_reader;
        }
    }
}
