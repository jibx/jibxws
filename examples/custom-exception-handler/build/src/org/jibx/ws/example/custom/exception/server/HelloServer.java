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

package org.jibx.ws.example.custom.exception.server;

import org.jibx.ws.example.custom.exception.common.Greetee;
import org.jibx.ws.example.custom.exception.common.ServiceError;
import org.jibx.ws.example.custom.exception.common.Welcome;
import org.jibx.ws.soap.SoapFault;
import org.jibx.ws.soap.SoapFaultException;

/**
 * Demonstrates application specific exceptions returning custom SOAP faults. Throwing a {@link SoapFaultException} 
 * allows the details of the SOAP fault to be specified.  In this case an actor is specified in the {@link SoapFault}
 * and a ZorroFault is returned in the fault details.  
 * 
 * @author Nigel Charman
 */
public final class HelloServer
{
    /**
     * Returns a welcome message to anyone whose name starts with Z.
     * 
     * @param greetee the party that the welcome is for
     * @return welcome message
     * @throws SoapFaultException if server fault occurs
     */
    public Object welcomeService(Greetee greetee) {
        char firstChar = greetee.getName().charAt(0);
        if (firstChar != 'z' && firstChar != 'Z') {
            return new ServiceError("Bad Name", "Sorry, service only available for names starting with Z !");
        }
        return new Welcome("Howdy " + greetee.getName() + "!");
    }
}
