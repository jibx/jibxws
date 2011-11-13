/*
Copyright (c) 2007-2008, Sosnoski Software Associates Limited
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

package org.jibx.ws.example.spring.hello.soap.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jibx.ws.WsException;
import org.jibx.ws.example.spring.hello.common.Greetee;
import org.jibx.ws.example.spring.hello.common.Welcome;
import org.jibx.ws.io.handler.ExceptionReader;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.client.SoapClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A simple client to demonstrate basic JiBX/WS usage using Spring to configure the client.
 *
 * @author Nigel Charman
 */
public final class HelloClient 
{
    private SoapClient client;


    public HelloClient(String beanName) {
        BeanFactory factory = new ClassPathXmlApplicationContext("hello_client_config.xml");
        client = (SoapClient) factory.getBean(beanName);
        client.addInFaultDetailsHandler(new ExceptionReader());
    }
    
    public void run(Greetee sender) {
        try {
            Welcome welcome = (Welcome) client.call(sender);
            System.out.println(welcome.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WsException e) {
            System.out.println("exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (SoapFaultException e) {
            System.err.println("SOAP Fault");
            System.err.println("----------");
            System.err.println(e.getFault());
            List details = e.getFault().getDetails();
            if (details.size() > 0) {
                System.err.println();
                System.err.println("Details");
                System.err.println("-------");
                for (Iterator iterator = details.iterator(); iterator.hasNext();) {
                    System.err.println(iterator.next());
                }
            }
        }
    }
    
    
    /**
     * Sends a name to the web service, which replies with a welcome message.  
     *
     * @param args optional first arg contains target host and port specification ("http://localhost:8080" by default),
     *             optional second arg contains name to be greeted by hello service ("World" by default). 
     */
    public static void main(String[] args) {
        Greetee sender = new Greetee("World");
        
        HelloClient client = new HelloClient("hello-soap-client");

        for (int i=0; i<3; i++) {
            client.run(sender);
        }
    }
}
