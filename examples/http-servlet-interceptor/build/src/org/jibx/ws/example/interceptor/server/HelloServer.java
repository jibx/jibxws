/*
Copyright (c) 2007, Sosnoski Software Associates Limited. 
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

package org.jibx.ws.example.interceptor.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.ws.context.InContext;
import org.jibx.ws.example.interceptor.common.Greetee;
import org.jibx.ws.example.interceptor.common.Welcome;

/**
 * Demonstrates retrieving a copy of the input message.
 * 
 * @author Nigel Charman
 */
public final class HelloServer
{
    private static final Log logger = LogFactory.getLog(HelloServer.class);
    /**
     * Logs a copy of the inbound message and returns a welcome message.
     *
     * @param greeetee the party that the welcome is for
     * @param inCtx the inbound context
     * @return welcome message 
     */
    public Welcome welcomeService(Greetee greeetee, InContext inCtx) {
        byte[] requestBytes = (byte[]) inCtx.getAttribute("request.copy");
        if (requestBytes == null) {
            logger.error("No input data found - interceptor not configured correctly");
        } else {
            System.out.println("Request is " + new String(requestBytes));
        }

        String greeting = "Hello, Hello ";
        return new Welcome(greeting + " " + greeetee.getName() + "!");
    }
}
