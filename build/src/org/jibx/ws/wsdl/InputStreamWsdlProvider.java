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

package org.jibx.ws.wsdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Returns WSDL that has been read from the given input stream.
 */
public class InputStreamWsdlProvider implements WsdlProvider
{
    private byte[] wsdlBytes;

    /** 
     * Constructor.
     * 
     * @param inputStream stream to read WSDL from 
     * @throws IOException on error reading stream
     */
    public InputStreamWsdlProvider(InputStream inputStream) throws IOException {
        wsdlBytes = inputStreamToBytes(inputStream);
    }

    private byte[] inputStreamToBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[1024];
        int len;

        try {
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
    
            return out.toByteArray();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void writeWSDL(OutputStream outputStream, HttpServletRequest req) throws IOException {
        outputStream.write(wsdlBytes);
    }

    public InputStream getWSDL(HttpServletRequest req) throws IOException {
        return new ByteArrayInputStream(wsdlBytes);
    }
}
