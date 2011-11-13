/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited. All rights reserved.
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
package org.jibx.ws.io;

import junit.framework.TestCase;

public class MessageOptionsTest extends TestCase
{

    public void testConstructor1() throws Throwable {
        MessageOptions messageOptions = new MessageOptions();
        assertEquals("messageOptions.getEncoding()", XmlEncoding.UTF_8, messageOptions.getEncoding());
        assertNull("messageOptions.getStandaloneDecl()", messageOptions.getStandaloneDecl());
        assertEquals("messageOptions.getIndentCount()", -1, messageOptions.getIndentCount());
        assertFalse("messageOptions.isIncludeEncodingDecl()", messageOptions.isIncludeEncodingDecl());
    }

    public void testConstructor2() throws Throwable {
        XmlEncoding encoding = XmlEncoding.UTF_8;
        MessageOptions messageOptions = new MessageOptions(encoding, true, Boolean.TRUE, ' ', 100, "testMessageOptionsNewLine");
        assertEquals("messageOptions.getNewLine()", "testMessageOptionsNewLine", messageOptions.getNewLine());
        assertSame("messageOptions.getEncoding()", encoding, messageOptions.getEncoding());
        assertEquals("messageOptions.getIndentChar()", ' ', messageOptions.getIndentChar());
        assertEquals("messageOptions.getStandaloneDecl()", true, messageOptions.getStandaloneDecl().booleanValue());
        assertEquals("messageOptions.getIndentCount()", 100, messageOptions.getIndentCount());
        assertTrue("messageOptions.isIncludeEncodingDecl()", messageOptions.isIncludeEncodingDecl());
    }

    public void testConstructor3() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(' ', 100, "testMessageOptionsNewLine");
        assertEquals("messageOptions.getNewLine()", "testMessageOptionsNewLine", messageOptions.getNewLine());
        assertEquals("messageOptions.getEncoding()", XmlEncoding.UTF_8, messageOptions.getEncoding());
        assertEquals("messageOptions.getIndentChar()", ' ', messageOptions.getIndentChar());
        assertNull("messageOptions.getStandaloneDecl()", messageOptions.getStandaloneDecl());
        assertEquals("messageOptions.getIndentCount()", 100, messageOptions.getIndentCount());
        assertFalse("messageOptions.isIncludeEncodingDecl()", messageOptions.isIncludeEncodingDecl());
    }

    public void testGetEncodingDeclString1() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(' ', 100, "testMessageOptionsNewLine");
        messageOptions.setIncludeEncodingDecl(true);
        String result = messageOptions.getEncodingDeclString();
        assertEquals("messageOptions.getEncodingDeclString()", "UTF-8", result);
    }

    public void testGetEncodingDeclString2() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(' ', 100, "testMessageOptionsNewLine");
        messageOptions.setIncludeEncodingDecl(false);
        String result = messageOptions.getEncodingDeclString();
        assertNull("messageOptions.getEncodingDeclString()", result);
    }

    public void testGetStandaloneDeclString1() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(null, true, Boolean.TRUE, ' ', 100, "testMessageOptionsNewLine");
        String result = messageOptions.getStandaloneDeclString();
        assertEquals("getStandaloneDeclString()", "yes", result);
    }

    public void testGetStandaloneDeclString2() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(null, true, Boolean.FALSE);
        String result = messageOptions.getStandaloneDeclString();
        assertEquals("getStandaloneDeclString()", "no", result);
    }

    public void testGetStandaloneDeclString3() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(null, true, null);
        String result = messageOptions.getStandaloneDeclString();
        assertNull("getStandaloneDeclString()", result);
    }

    public void testSetEncoding() throws Throwable {
        XmlEncoding encoding = XmlEncoding.UTF_8;
        MessageOptions messageOptions = new MessageOptions();
        messageOptions.setEncoding(encoding);
        assertSame("messageOptions.getEncoding()", encoding, messageOptions.getEncoding());
    }

    public void testSetIncludeEncodingDecl() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(' ', 100, "testMessageOptionsNewLine");
        messageOptions.setIncludeEncodingDecl(true);
        assertTrue("messageOptions.isIncludeEncodingDecl()", messageOptions.isIncludeEncodingDecl());
    }

    public void testSetIndentChar() throws Throwable {
        MessageOptions messageOptions = new MessageOptions();
        messageOptions.setIndentChar(' ');
        assertEquals("messageOptions.getIndentChar()", ' ', messageOptions.getIndentChar());
    }

    public void testSetIndentCount() throws Throwable {
        MessageOptions messageOptions = new MessageOptions();
        messageOptions.setIndentCount(100);
        assertEquals("messageOptions.getIndentCount()", 100, messageOptions.getIndentCount());
    }

    public void testSetStandaloneDecl() throws Throwable {
        MessageOptions messageOptions = new MessageOptions();
        messageOptions.setStandaloneDecl(Boolean.FALSE);
        assertEquals("messageOptions.getStandaloneDecl()", false, messageOptions.getStandaloneDecl().booleanValue());
    }

    public void testGetEncodingDeclStringThrowsNullPointerException() throws Throwable {
        MessageOptions messageOptions = new MessageOptions(new MessageOptions().getEncoding(), true, Boolean.TRUE);
        messageOptions.setIncludeEncodingDecl(true);
        messageOptions.setEncoding(null);
        try {
            messageOptions.getEncodingDeclString();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
