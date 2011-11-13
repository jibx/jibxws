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

package org.jibx.ws.example.interceptor.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.example.interceptor.common.Greetee;
import org.jibx.ws.example.interceptor.common.Welcome;
import org.jibx.ws.soap.client.SoapClient;
import org.jibx.ws.transport.InputStreamInterceptable;
import org.jibx.ws.transport.OutputStreamInterceptable;
import org.jibx.ws.transport.TransportDirectory;
import org.jibx.ws.transport.TransportOptions;
import org.jibx.ws.transport.interceptor.CopiedInputStreamInterceptor;
import org.jibx.ws.transport.interceptor.CopiedOutputStreamInterceptor;

/**
 * A simple client to demonstrate basic JiBX/WS usage. This version adds interceptors to record the outbound and 
 * inbound data. 
 * 
 * @author Nigel Charman
 */
public final class HelloClient
{
    private IBindingFactory m_fact;
    private String m_location;
    private ByteArrayOutputStream m_requestBaos;
    private ByteArrayOutputStream m_responseBaos;
    private CopiedOutputStreamInterceptor m_outInterceptor;
    private CopiedInputStreamInterceptor m_inInterceptor;
    
    private HelloClient(String location) throws JiBXException {
        m_location = location;
        m_fact = BindingDirectory.getFactory(Greetee.class);
        m_requestBaos = new ByteArrayOutputStream(4096);
        m_responseBaos = new ByteArrayOutputStream(4096);
        m_outInterceptor = new CopiedOutputStreamInterceptor(m_requestBaos);
        m_inInterceptor = new CopiedInputStreamInterceptor(m_responseBaos);
    }

    private Welcome sayHello(Greetee s) throws WsException, IOException {
        SoapClient client = new SoapClient(m_location, m_fact);
        TransportOptions transportOptions = TransportDirectory.newTransportOptions(m_location);
        if (transportOptions instanceof OutputStreamInterceptable) {
            ((OutputStreamInterceptable)transportOptions).setOutputStreamInterceptor(m_outInterceptor);
        }
        if (transportOptions instanceof InputStreamInterceptable) {
            ((InputStreamInterceptable)transportOptions).setInputStreamInterceptor(m_inInterceptor);
        }
        client.setTransportOptions(transportOptions);
        
        Welcome welcome = (Welcome) client.call(s);
        System.out.println("Request is " + m_requestBaos.toString());
        System.out.println("Response is " + m_responseBaos.toString());
        return welcome;
    }
    
    /**
     * Sends a name to the web service, which replies with a welcome message.  
     *
     * @param args optional first arg contains target host and port specification ("http://localhost:8080" by default),
     *             optional second arg contains path to web app ("jibx-ws-interceptor/welcome-service" by default). 
     *             optional third arg contains name to be greeted by hello service ("World" by default). 
     */
    public static void main(String[] args) {
        String target = "http://localhost:8080";
        String path = "jibx-ws-interceptor/welcome-service";
        Greetee sender = new Greetee("World");
        
        if (args.length > 0) {
            target = args[0];
        }
        if (args.length > 1) {
            path = args[1];
        }
        if (args.length > 2) {
            sender = new Greetee(args[2]);
        }
        
        HelloClient client = null;
        try {
            client = new HelloClient(target + "/" + path);
        } catch (JiBXException e) {
            System.err.println("Unable to create client.");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            Welcome welcome = client.sayHello(sender);
            System.out.println(welcome.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WsException e) {
            e.printStackTrace();
        }
    }
}
