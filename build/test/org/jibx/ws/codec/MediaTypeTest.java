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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.jibx.ws.codec.MediaType.Parameter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link MediaType}
 */
public class MediaTypeTest
{
    @Test
    public final void constructor_shouldParseValidString() throws Exception {
        MediaType mediaType = new MediaType(" text/html; charset=UTF-8 ");
        assertThat(mediaType.getPrimaryType(), is("text"));
        assertThat(mediaType.getSubType(), is("html"));
        List parameterList = mediaType.getParameterList();
        assertThat("parameter list length", parameterList.size(), is(1));
        assertThat(((Parameter)parameterList.get(0)).getName(), is("charset"));
        assertThat(((Parameter)parameterList.get(0)).getValue(), is("UTF-8"));
    }

    @Test(expected=ParseException.class)
    public final void constructor_shouldThrowParseExceptionGivenInvalidString() throws Exception {
        new MediaType(" text; charset=UTF-8 ");
    }
    
    @Test(expected=ParseException.class)
    public final void constructor_shouldThrowParseExceptionGivenInvalidString2() throws Exception {
        new MediaType(" /html; charset=UTF-8 ");
    }
    
    @Test(expected=ParseException.class)
    public final void constructor_shouldThrowParseExceptionGivenInvalidString3() throws Exception {
        new MediaType(" text/; charset=UTF-8 ");
    }
    
    @Test
    public final void constructor_shouldParseMultipleParameters() throws Exception {
        assertThat(new MediaType("text/html; charset=UTF-8; q=0.5"), 
           equalTo(new MediaType("text", "html", params("charset", "UTF-8", "q", "0.5"))));
    }

    @Test
    public final void constructor_shouldParseParametersWithoutValues() throws Exception {
        assertThat(new MediaType("text/html; charset=UTF-8; q"), 
           equalTo(new MediaType("text", "html", params("charset", "UTF-8", "q", null))));
    }
    
    @Test
    public final void constructor_shouldParseParametersWithoutValues2() throws Exception {
        assertThat(new MediaType("text/html; p; q=1.0"), 
           equalTo(new MediaType("text", "html", params("p", null, "q", "1.0"))));
    }
    
    @Test
    public final void constructor_withIgnoreParameters_shouldIgnoreParameters() throws Exception {
        assertThat(new MediaType("text/html; charset=UTF-8; q=0.5", true), 
           equalTo(new MediaType("text/html")));
    }

    @Test
    public final void constructor_shouldParseStringWithNoParameters() throws Exception {
        assertThat(new MediaType("text/html"), 
           equalTo(new MediaType("text", "html")));
    }
    
    @Test
    public final void accept_shouldReturnTrueOnMatch() throws Exception {
        Assert.assertTrue(new MediaType("text/xml").accepts(new MediaType("text", "xml")));
    }

    @Test
    public final void accept_shouldReturnFalseOnNonMatch1() throws Exception {
        Assert.assertFalse(new MediaType("text/html").accepts(new MediaType("text", "xml")));
    }

    @Test
    public final void accept_shouldReturnFalseOnNonMatch2() throws Exception {
        Assert.assertFalse(new MediaType("text/xml").accepts(new MediaType("soap", "xml")));
    }

    @Test
    public final void accept_shouldReturnTrueOnWildcardSubtypeMatch() throws Exception {
        Assert.assertTrue(new MediaType("text/*").accepts(new MediaType("text", "xml")));
    }

    @Test
    public final void accept_shouldReturnFalseOnWildcardSubtypeWithTypeMismatch() throws Exception {
        Assert.assertFalse(new MediaType("text/*").accepts(new MediaType("soap", "xml")));
    }

    @Test
    public final void accept_shouldReturnTrueOnWildcard() throws Exception {
        Assert.assertTrue(new MediaType("*/*").accepts(new MediaType("text", "html")));
    }

    @Test
    public final void accept_shouldReturnTrueOnALL_MEDIA_TYPES() throws Exception {
        Assert.assertTrue(MediaType.ALL_MEDIA_TYPES.accepts(new MediaType("soap", "xml")));
    }

    @Test
    public final void accept_shouldIgnoreParameters() throws Exception {
        Assert.assertTrue(new MediaType("text/xml; q=0.8").accepts(new MediaType("text/xml; charset=UTF-8")));
    }

    @Test
    public final void testEqualTo() throws Exception {
        assertThat(new MediaType(" text/html; charset=UTF-8 "),
           equalTo(new MediaType("text", "html", params("charset", "UTF-8"))));
    }

    @Test
    public final void equalTo_shouldHaveCaseInsensitiveType() throws Exception {
        assertThat(new MediaType(" TeXt/html  ; charset=UTF-8"), 
           equalTo(new MediaType("text", "html", params("charset", "UTF-8"))));
    }

    @Test
    public final void equalTo_shouldHaveCaseInsensitiveSubtype() throws Exception {
        assertThat(new MediaType(" text/HTml  ; charset=UTF-8"), 
           equalTo(new MediaType("text", "html", params("charset", "UTF-8"))));
    }

    @Test
    public final void equalTo_shouldHaveCaseSensitiveParameters() throws Exception {
        assertThat(new MediaType(" text/html  ; chARset=UTF-8"), 
       not(equalTo(new MediaType("text", "html", params("charset", "UTF-8")))));
    }

    @Test
    public final void testHashCodeTrue() throws Exception {
        Assert.assertTrue(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                     ==  (new MediaType("text", "html", params("charset", "UTF-8"))).hashCode());
    }

    @Test
    public final void testHashCodeFalse1() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text1", "html", params("charset", "UTF-8"))).hashCode());
    }

    @Test
    public final void testHashCodeFalse2() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html1", params("charset", "UTF-8"))).hashCode());
    }
    
    @Test
    public final void testHashCodeFalse3() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html", params("charset1", "UTF-8"))).hashCode());
    }
    
    @Test
    public final void testHashCodeFalse4() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html", params("charset", "UTF-16"))).hashCode());
    }
    
    @Test
    public final void testHashCodeFalse5() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html")).hashCode());
    }
    
    @Test
    public final void testHashCodeFalse6() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html", params("charset", "UTF-16", "q", "0.5"))).hashCode());
    }
    
    @Test
    public final void testHashCodeFalse7() throws Exception {
        Assert.assertFalse(new MediaType(" text/html; charset=UTF-8 ").hashCode()
                      ==  (new MediaType("text", "html", params("charset", null))).hashCode());
    }
    
    @Test
    public final void toString_shouldReturnCorrectString() throws Exception {
        MediaType mediaType = new MediaType(" text/hTMl;   charset=UTF-8 ");
        assertThat(mediaType.toString(), is("text/html; charset=UTF-8"));
    }
    
    @Test
    public final void toString_shouldReturnCorrectStringIfInvokedTwice() throws Exception {
        MediaType mediaType = new MediaType(" text/hTMl;   charset=UTF-8 ");
        assertThat(mediaType.toString(), is("text/html; charset=UTF-8"));
        assertThat(mediaType.toString(), is("text/html; charset=UTF-8"));  
    }
    
    @Test
    public final void toString_shouldReturnDifferentStringAfterParametersAdded() throws Exception {
        MediaType mediaType = new MediaType(" text/hTMl;   charset=UTF-8 ");
        assertThat(mediaType.toString(), is("text/html; charset=UTF-8"));
        mediaType.addParameter(new Parameter("action", "man"));
        assertThat(mediaType.toString(), is("text/html; charset=UTF-8; action=man"));  
    }
    
    @Test
    public final void toShortString_shouldReturnShortString() throws Exception {
        MediaType mediaType = new MediaType(" text/hTMl;   charset=UTF-8 ");
        assertThat(mediaType.getBaseType(), is("text/html"));
    }
    
    @Test
    public final void toShortString_shouldReturnSameStringIfInvokedTwice() throws Exception {
        MediaType mediaType = new MediaType(" text/hTMl;   charset=UTF-8 ");
        assertThat(mediaType.getBaseType(), is("text/html"));
        assertThat(mediaType.getBaseType(), is("text/html"));  // check cached version too
    }
    
    @Test
    public final void toStringWithParams_shouldAddParametersToString() throws Exception {
        Parameter charset = new Parameter("charset", "utf-16");
        Parameter action = new Parameter("action", "man");
        assertThat(new MediaType("text/html").toStringWithParams(new Parameter[] {charset, action}),
                is("text/html; charset=utf-16; action=man"));
    }
    
    @Test
    public final void toStringWithParams_shouldIgnoreNullParameters1() throws Exception {
        Parameter charset = new Parameter("charset", "utf-16");
        assertThat(new MediaType("text/html").toStringWithParams(new Parameter[] {charset, null}),
                is("text/html; charset=utf-16"));
    }
    
    @Test
    public final void toStringWithParams_shouldIgnoreNullParameters2() throws Exception {
        assertThat(new MediaType("text/html").toStringWithParams(new Parameter[] {null, null}),
                is("text/html"));
    }

    @Test(expected=IllegalStateException.class)
    public final void addParameter_shouldThrowIllegalStateExceptionIfMediaTypeFrozen() throws Exception {
        new MediaType("text/html").freeze().addParameter(new Parameter("action", "man"));
    }
    
    private List params(String name, String value) {
        List parameters = new ArrayList();
        parameters.add(new MediaType.Parameter(name, value));
        return parameters;
    }

    private List params(String name1, String value1, String name2, String value2) {
        List list = params(name1, value1);
        list.add(new MediaType.Parameter(name2, value2));
        return list;
    }

}
