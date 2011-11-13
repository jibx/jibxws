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

package org.jibx.ws.encoding.dime;

/**
 * Wrapper for input stream using DIME encoding. DIME messages are sent as a sequence of one or more parts. Each part is
 * sent as one or more records. Each record starts with a header giving the length and blocking flags, and potentially
 * other information such as content type and message identifier.
 * 
 * This wrapper provides the DIME data to the application as one stream per part. Parts must be fully consumed before
 * moving on to the next part, and messages must be fully consumed before moving on to the next message.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class DimeCommon
{
    private DimeCommon() {
    }
    
    /** Default byte buffer size used with DIME encoding. */
    public static final int DEFAULT_BUFFER_SIZE = 0x10000;
    
    /** Fixed header size for non-initial chunks (minimum header). */
    public static final int HEADER_SIZE = 12;
    
    //
    // Flags and fields in first byte of DIME record header
    public static final int VERSION_MASK = 0xF8;
    public static final int VERSION_VALUE = 0x08;
    public static final int MESSAGE_BEGIN_FLAG = 0x04;
    public static final int MESSAGE_END_FLAG = 0x02;
    public static final int CHUNK_FLAG = 0x01;
    
    //
    // Fields in second byte of DIME record header
    public static final int TYPE_FORMAT_MASK = 0xF0;
    public static final int TYPE_UNCHANGED = 0x00;
    public static final int TYPE_MEDIA = 0x10;
    public static final int TYPE_URI = 0x20;
    public static final int TYPE_UNKNOWN = 0x30;
    public static final int TYPE_NONE = 0x40;
    
    //
    // Message states
    public static final int MESSAGE_START = 0;         // expecting start of message next
    public static final int MESSAGE_MIDDLE = 1;        // expecting message part next
    public static final int MESSAGE_CHUNK = 2;         // expecting part chunk next
    public static final int MESSAGE_END = 3;           // end of message reached
    
    private static String s_hexChars = "0123456789abcdef"; 
    
    /**
     * Get printable representation of bytes in array.
     * 
     * @param byts bytes
     * @param offset starting position
     * @param length number of bytes
     * @return text printable representationt
     */
    public static String dumpBytes(byte[] byts, int offset, int length) {
        StringBuffer buff = new StringBuffer(length * 3);
        for (int i = 0; i < length; i++) {
            buff.append(' ');
            byte byt = byts[offset + i ];
            buff.append(s_hexChars.charAt((byt >> 4) & 0xF));
            buff.append(s_hexChars.charAt(byt & 0xF));
        }
        return buff.toString();
    }
}
