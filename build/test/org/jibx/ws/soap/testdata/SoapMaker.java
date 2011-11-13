/*
Copyright (c) 2007, Sosnoski Software Associates Ltd.
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

package org.jibx.ws.soap.testdata;

import org.jibx.runtime.QName;

/**
 * A test helper class that makes SOAP messages.
 */
public class SoapMaker
{
    /**
     * The SOAP Fault code representing a SOAP Server fault.
     */
    public static final String SERVER_FAULTCODE = "SOAP:Server";

    /**
     * The SOAP Fault code representing a SOAP Must Understand fault.
     */
    public static final String MUSTUNDERSTAND_FAULTCODE = "SOAP:MustUnderstand";
    
    /**
     * Wraps the specified contents in a SOAP envelope.
     *
     * @param contents the contents to envelope
     * @return a SOAP Envelope containing the contents
     */
    public static String envelope(String contents) {
    	return "<?xml version=\"1.0\"?>\n"
    			+ "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
    			+ contents + "</SOAP:Envelope>\n";
    }

    /**
     * Wraps the specified contents in a SOAP envelope.
     *
     * @param content1
     * @param content2
     * @return a SOAP envelope containing content1 followed by content2
     */
    public static String envelope(String content1, String content2) {
        return envelope(content1 + "\n" + content2);
    }

    /**
     * Wraps the specified contents in a SOAP header.
     *
     * @param contents the contents to wrap in a header
     * @return a SOAP header containing the contents
     */
    public static String header(String contents) {
        return "<SOAP:Header>\n" + contents + "</SOAP:Header>\n";
    }
    
    /**
     * Wraps the specified contents in a SOAP body.
     *
     * @param contents the contents to wrap
     * @return a SOAP Body containing the contents
     */
    public static String body(String contents) {
    	return "<SOAP:Body>\n" + contents + "</SOAP:Body>\n";
    }

    /**
     * Creates a SOAP fault message for the given fault code and string.
     *
     * @param faultCode The SOAP Fault Code to use in the SOAP message.
     * @param faultString The SOAP Fault String to use in the SOAP message.
     * @return a fully constructed SOAP Fault message
     */
    public static String soapFault(String faultCode, String faultString) {
        return envelope(body("<SOAP:Fault>\n"
                + "<faultcode>" + faultCode + "</faultcode>\n"
                + "<faultstring>" + faultString + "</faultstring>\n"
                + "</SOAP:Fault>\n"));
    }

    /**
     * Creates a SOAP Fault message with the 'SOAP:Server' fault code and the given fault string.
     *
     * @param faultString The SOAP Fault String to use in the SOAP message.
     * @return a fully constructed SOAP Server Fault message
     */
    public static String soapServerFault(String faultString) {
        return soapFault(SoapMaker.SERVER_FAULTCODE, faultString);
    }
}
