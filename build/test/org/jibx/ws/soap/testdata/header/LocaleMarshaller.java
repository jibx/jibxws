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

package org.jibx.ws.soap.testdata.header;

import java.io.IOException;

import org.jibx.runtime.IXMLWriter;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.handler.OutHandler;

/**
 * A handler that can marshall Locale objects.  This is not threadsafe and has been created just for testing.
 */
public class LocaleMarshaller implements OutHandler
{
    private static Locale s_locale;
    private MarshallingPayloadWriter m_marshaller;

    /**
     * @throws WsBindingException 
     */
    public LocaleMarshaller() throws WsBindingException {
        m_marshaller = new MarshallingPayloadWriter(Locale.class);
    }

    public void invoke(OutContext context, IXMLWriter xmlWriter) throws IOException, WsException {
        m_marshaller.invoke(xmlWriter, s_locale);
    }

    public static void setLocale(Locale l) {
        s_locale = l;
    }
}
