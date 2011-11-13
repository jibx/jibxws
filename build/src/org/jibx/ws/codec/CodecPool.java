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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jibx.ws.WsException;
import org.jibx.ws.util.ExpandingPool;

/**
 * Multipool of instances of codecs. This allows codecs of any requested type to be reused, creating new instances when
 * necessary. The methods defined by this class are not threadsafe, so the using code needs to synchronize before
 * accessing the pool.
 * 
 * @author Dennis M. Sosnoski
 */
public final class CodecPool
{
    /** Map from media type key to codec instance. */
    private final Map m_codecMap;

    /**
     * Constructor. This creates a pool for each supported media type, initially with a single instance of that codec.
     */
    public CodecPool() {
        m_codecMap = new HashMap();
        List mediaTypes = CodecDirectory.getMediaTypes();
        for (int i = 0; i < mediaTypes.size(); i++) {
            MediaType mediaType = (MediaType)mediaTypes.get(i);
            XmlCodec codec = CodecDirectory.getCodec(mediaType);
            if (codec != null) {
                InstancePool pool = new InstancePool(mediaType);
                pool.releaseInstance(codec);
                m_codecMap.put(CodecDirectory.getCodecKey(mediaType), pool);
            }
        }
    }

    /**
     * Get a codec. If an instance of the codec has been created previously, that instance will be returned. Otherwise a
     * new codec will be created, and returned.
     * 
     * <p>
     * Any parameters on the mediatype are ignored.
     * 
     * @param mediaType the media type 
     * @return codec
     * @throws IOException for unsupported media type
     * @throws WsException for error creating codec
     */
    public XmlCodec getCodec(MediaType mediaType) throws IOException, WsException {
        String key = CodecDirectory.getCodecKey(mediaType);
        InstancePool pool = (InstancePool)m_codecMap.get(key);
        if (pool == null) {
            throw new IOException("Unsupported media type '" + mediaType + "'");
        } else {
            return (XmlCodec)pool.getInstance();
        }
    }

    /**
     * Release a codec.
     * 
     * @param codec the codec to release
     */
    public void releaseCodec(XmlCodec codec) {
        String key = CodecDirectory.getCodecKey(codec.getMediaType());
        InstancePool pool = ((InstancePool)m_codecMap.get(key));
        pool.releaseInstance(codec);
    }
    
    /**
     * Pool for instances of a codec.
     */
    private static class InstancePool extends ExpandingPool
    {
        private final MediaType m_mediaType;
        
        /**
         * Constructor.
         * 
         * @param mediaType
         */
        public InstancePool(MediaType mediaType) {
            m_mediaType = mediaType;
        }
        
        /**
         * {@inheritDoc}
         */
        protected Object createInstance() {
            return CodecDirectory.getCodec(m_mediaType);
        }
    }
}
