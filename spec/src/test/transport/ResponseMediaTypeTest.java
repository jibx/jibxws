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

package transport;

import java.util.List;

import org.concordion.integration.junit4.ConcordionRunner;
import org.jibx.ws.codec.CodecDirectory;
import org.jibx.ws.codec.MediaType;
import org.junit.runner.RunWith;

/**
 * Fixture class for Concordion tests in ResponseMediaType.html.  Runs as JUnit test.  
 */
@RunWith(ConcordionRunner.class)
public class ResponseMediaTypeTest
{
    /**
     * Returns the media types that are supported in their preferred oreder.
     *
     * @return ordered list of {@link MediaType}
     */
    public List getOrderedMediaTypes() {
        return CodecDirectory.getMediaTypes();
    }
    
    /**
     * Returns the media type string that will be set as the response content-type, given the specified accept string
     * and request content-type.
     *
     * @param acceptString the media type accept string
     * @param contentType the content-type of the request
     * @return media type string 
     * @throws Exception on any error
     */
    public String getResponseMediaType(String acceptString, String contentType) throws Exception {
        if (acceptString.equals("{not present}")) {
            acceptString = null;
        }
        MediaType responseMediaType = CodecDirectory.getAcceptableMediaType(acceptString, new MediaType(contentType));
        return responseMediaType == null ? null : responseMediaType.toString();
    }

    /**
     * Returns the media type string that will be set as the response content-type, given the specified accept string.
     *
     * @param acceptString the media type accept string
     * @return media type string 
     * @throws Exception on any error
     */
    public String getResponseMediaType(String acceptString) throws Exception {
        MediaType responseMediaType = CodecDirectory.getAcceptableMediaType(acceptString, new MediaType("x/y"));
        return responseMediaType == null ? "{error}" : responseMediaType.toString();
    }
}
