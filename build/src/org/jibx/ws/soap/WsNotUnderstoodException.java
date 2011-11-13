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

package org.jibx.ws.soap;

import org.jibx.ws.WsException;

/**
 * Indicates that a handler could not correctly process a mandatory SOAP header. If a SOAP header handler is responsible
 * for handling a specific header and it cannot obey the semantics of the header, and process correctly to those
 * semantics, it must check for the presence of a SOAP mustUnderstand attribute. If this attribute is set to "1", then
 * the handler is to raise a <code>WsNotUnderstoodException</code> to indicate that the header could not be handled.
 * 
 * @author Nigel Charman
 */
public class WsNotUnderstoodException extends WsException
{
    private static final long serialVersionUID = 6033040961759910604L;

    /**
     * @param msg the exception message
     */
    public WsNotUnderstoodException(String msg) {
        super(msg);
    }

    /**
     * @param msg the exception message
     * @param root cause
     */
    public WsNotUnderstoodException(String msg, Throwable root) {
        super(msg, root);
    }
}
