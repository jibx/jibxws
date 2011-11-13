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

package org.jibx.ws.example.fault.custom.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.example.fault.custom.common.Greetee;
import org.jibx.ws.example.fault.custom.common.Welcome;
import org.jibx.ws.example.fault.custom.common.ZorroFault;
import org.jibx.ws.io.handler.ExceptionReader;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.client.SoapClient;

/**
 * A simple client to demonstrate receiving a SOAP Fault with custom details. The {@link SoapClient} by default adds an
 * inbound SOAP Fault handler using the binding factory passed to the constructor. In this example, the binding file
 * contains a definition for the ZorroFault class, which is passed in the Soap Fault details.
 * 
 * @see ExceptionReader
 * @author Nigel Charman
 */
public final class HelloClient
{
    private IBindingFactory m_fact;
    private String m_location;

    private HelloClient(String location) throws JiBXException {
        m_location = location;
        m_fact = BindingDirectory.getFactory("example_binding", "org.jibx.ws.example.fault.custom.common");
    }

    private Welcome sayHello(Greetee s) throws WsException, IOException {
        SoapClient client = new SoapClient(m_location, m_fact);
        Welcome welcome = (Welcome) client.call(s);
        return welcome;
    }

    /**
     * Sends a name to the web service, which throws a SOAP fault.  
     *
     * @param args optional first arg contains target host and port specification ("http://localhost:8080" by default),
     *             optional second arg contains path to web app ("jibx-ws-soap-fault-custom/welcome-service" by default). 
     *             optional third arg contains name to be greeted by hello service ("World" by default). 
     */
    public static void main(String[] args) {
        String target = "http://localhost:8080";
        String path = "jibx-ws-soap-fault-custom/welcome-service";
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
        } catch (SoapFaultException e) {
            List details = e.getFault().getDetails();
            if (details.size() > 0) {
                for (Iterator iterator = details.iterator(); iterator.hasNext();) {
                    Object detail = iterator.next();
                    if (detail instanceof ZorroFault) {
                        ZorroFault ze = (ZorroFault) detail;
                        System.err.println("Sorry " + ze.getName() + " the following error was sent by the actor "
                            + e.getFault().getFaultActor());
                        System.err.println("\"" + ze.getMessage() + "\"");
                    } else {
                        System.err.println(detail);
                    }
                }
            } else {
                System.err.println("Error - expected fault details to be included.");
            }
        } catch (WsException e) {
            e.printStackTrace();
        }
    }
}
