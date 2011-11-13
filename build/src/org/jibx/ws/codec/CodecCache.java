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
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for codec instances. The methods exposed by this class are not threadsafe, so the using code needs to
 * synchronize before accessing the cache.
 * 
 * @author Dennis M. Sosnoski
 */
public class CodecCache
{
    /** Map from media type key to codec instance. */
    private final Map m_codecMap;

    /**
     * Constructor.
     */
    public CodecCache() {
        m_codecMap = new HashMap();
    }

    /**
     * Get a codec. If an instance of the codec has been created previously, that instance will be returned. Otherwise a
     * new codec will be created, cached, and returned.
     * <p>
     * Any parameters on the mediatype are ignored.
     * 
     * @param mediaType the media type 
     * @return codec
     * @throws IOException if no codecs for specified mediatype
     */
    public final XmlCodec getCodec(MediaType mediaType) throws IOException {
        String key = CodecDirectory.getCodecKey(mediaType);
        XmlCodec codec = (XmlCodec)m_codecMap.get(key);
        if (codec == null) {
            codec = CodecDirectory.getCodec(mediaType);
            if (codec == null) {
                throw new IOException("Unknown media type '" + mediaType + "'");
            }
            m_codecMap.put(key, codec);
        }
        return codec;
    }
}
