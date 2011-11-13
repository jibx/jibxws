/*
Copyright (c) 2009, Sosnoski Software Associates Limited. 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.ws.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.text.ParseException;

import org.junit.Test;

/**
 * Tests {@link CodecDirectory}. See also ResponseMediaTypeTest.java.
 */
public class CodecDirectoryTest
{
    private static final MediaType TEXT_XML_MEDIA_TYPE;
    private static final MediaType XBIS_MEDIA_TYPE;
    private static final MediaType SOAP_XBIS_MEDIA_TYPE;
    private static final MediaType UNKNOWN_MEDIA_TYPE;
    private static final MediaType MOST_PREFERRED_MEDIA_TYPE;
    
    
    static {
        try {
            UNKNOWN_MEDIA_TYPE = new MediaType("donkey/*");
            TEXT_XML_MEDIA_TYPE = new MediaType("text/xml");
            SOAP_XBIS_MEDIA_TYPE = new MediaType("application/soap+x-xbis");
            XBIS_MEDIA_TYPE = new MediaType("application/x-xbis");
            MOST_PREFERRED_MEDIA_TYPE = XBIS_MEDIA_TYPE;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } 
    }
    
    @Test
    public final void getCodec_shouldReturnMatchingCodec() throws Exception {
        XmlCodec codec = CodecDirectory.getCodec(TEXT_XML_MEDIA_TYPE);
        assertThat("Media type", codec.getMediaType(), is(TEXT_XML_MEDIA_TYPE));
        assertThat("Codec class", codec, instanceOf(TextCodecFactory.TextCodec.class));
    }

    @Test
    public final void getCodec_shouldReturnNullForUnknownCodec() throws Exception {
        assertThat(CodecDirectory.getCodec(new MediaType("text/html")), is(nullValue()));
    }

    @Test
    public final void hasCodecFor_shouldReturnTrueIfCodecAvailable() throws Exception {
        assertThat(CodecDirectory.hasCodecFor(new MediaType("application/soap+xml")), is(true));
    }
    
    @Test
    public final void hasCodecFor_shouldIgnoreParameters() throws Exception {
        assertThat(CodecDirectory.hasCodecFor(new MediaType("application/soap+xml;action=xyz")), is(true));
    }

    @Test
    public final void hasCodecFor_shouldReturnFalseIfCodecUnavailable() throws Exception {
        assertThat(CodecDirectory.hasCodecFor(new MediaType("donkey/xml")), is(false));
    }
}