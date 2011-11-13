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

package org.jibx.ws.example.headers.server;

import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.example.headers.common.Greetee;
import org.jibx.ws.example.headers.common.Locale;
import org.jibx.ws.example.headers.common.QOS;
import org.jibx.ws.example.headers.common.Welcome;

/**
 * A simple class to demonstrate basic JiBX/WS server usage.
 * 
 * @author Nigel Charman
 */
public final class HelloServer
{
    /**
     * Returns a welcome message.
     *
     * @param greeetee the party that the welcome is for
     * @param inCtx inbound message context
     * @param outCtx outbound message context
     * @return welcome message 
     */
    public Welcome welcomeService(Greetee greeetee, InContext inCtx, OutContext outCtx) {
        Locale locale = (Locale) inCtx.getAttribute("example.locale");
        QOS qos = null;
        String greeting = "Hi";
        if (locale != null) {
            if (locale.getLanguage().equals("en")) {
                greeting = "Hello";
                qos = new QOS(8, "OK");
            } else if (locale.getLanguage().equals("fr")) {
                greeting = "Bonjour";
                qos = new QOS(9, "Très bon");
            }
        }
        outCtx.setAttribute("comm.qos", qos);
        
        return new Welcome(greeting + " " + greeetee.getName() + "!");
    }
}
