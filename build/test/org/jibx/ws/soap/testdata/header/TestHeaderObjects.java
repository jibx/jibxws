/*
Copyright (c) 2007, Sosnoski Software Associates Limited
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

import org.jibx.ws.soap.testdata.SoapMaker;

/**
 * Constructs objects to be used in testing, both as Java objects and as SOAP messages.
 *
 * @author Nigel Charman
 */
public class TestHeaderObjects {

    /**
     * A request object for a specific locale. 
     */
    public static final Locale EN = new Locale("en");

    /**
     * A request object for a specific locale. 
     */
    public static final Locale FR = new Locale("fr");

    /**
     * A request object for a specific locale and country. 
     */
	public static final Locale EN_US = new Locale("en", "US");
	
    /**
     * The SOAP header corresponding to the locale {@link #EN}.
     */
	public static final String REQUEST_SOAP_HEADER = SoapMaker.header(
	  "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\">\n"
	+ "<t2:lang>en</t2:lang>\n" 
	+ "</t2:locale>\n");

    /**
     * The SOAP header corresponding to the locale {@link #EN}, with the mustUnderstand attribute set.
     */
    public static final String REQUEST_SOAP_HEADER_MUST_UNDERSTAND = SoapMaker.header(
      "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\" " 
    +      "SOAP:mustUnderstand=\"1\">\n"
    + "<t2:lang>en</t2:lang>\n" 
    + "</t2:locale>\n");

    /**
     * The SOAP header corresponding to two locales {@link #EN} and {@link #FR}.
     */
    public static final String REQUEST_SOAP_HEADER2 = SoapMaker.header(
      "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\">\n"
    + "<t2:lang>en</t2:lang>\n" 
    + "</t2:locale>\n"
    + "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\">\n"
    + "<t2:lang>fr</t2:lang>\n" 
    + "</t2:locale>\n");

    /**
     * The SOAP header corresponding to two locales {@link #EN} and {@link #FR}.
     */
    public static final String REQUEST_SOAP_HEADER_WITH_ATTR = SoapMaker.header(
      "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\" " 
    +      "SOAP:mustUnderstand=\"1\" SOAP:actor=\"http://schemas.xmlsoap.org/soap/actor/next\">\n"
    + "<t2:lang>en</t2:lang>\n" 
    + "</t2:locale>\n"
    + "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\" SOAP:mustUnderstand=\"1\" >\n"
    + "<t2:lang>fr</t2:lang>\n" 
    + "</t2:locale>\n");

    /**
     * The SOAP message containing the {@link #REQUEST_SOAP_HEADER} and body.
     */
    public static final String REQUEST_SOAP = SoapMaker.envelope(
        REQUEST_SOAP_HEADER,
        org.jibx.ws.soap.testdata.basic.TestObjects.REQUEST_SOAP_BODY);

    /**
     * The SOAP message containing the {@link #REQUEST_SOAP_HEADER} and body.
     */
    public static final String REQUEST_SOAP_EMPTY_HEADER = SoapMaker.envelope(
        "<SOAP:Header/>",
        org.jibx.ws.soap.testdata.basic.TestObjects.REQUEST_SOAP_BODY);
    
    /**
     * The SOAP message containing the {@link #REQUEST_SOAP_HEADER_MUST_UNDERSTAND} and body.
     */
    public static final String REQUEST_SOAP_MUST_UNDERSTAND = SoapMaker.envelope(
        REQUEST_SOAP_HEADER_MUST_UNDERSTAND,
        org.jibx.ws.soap.testdata.basic.TestObjects.REQUEST_SOAP_BODY);

    /**
     * The SOAP message containing the {@link #REQUEST_SOAP_HEADER2} and body.
     */
    public static final String REQUEST_SOAP2 = SoapMaker.envelope(
        REQUEST_SOAP_HEADER2,
        org.jibx.ws.soap.testdata.basic.TestObjects.REQUEST_SOAP_BODY);

    /**
     * The SOAP header corresponding to the locale {@link #EN_US}.
     */
    public static final String RESPONSE_SOAP_HEADER = SoapMaker.header(
      "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\">\n"
    + "<t2:lang>en</t2:lang>\n" 
    + "<t2:country>US</t2:country>\n" 
    + "</t2:locale>\n");

    /**
     * The SOAP header corresponding to the locale {@link #EN_US}.
     */
    public static final String RESPONSE_SOAP_HEADER_MUST_UNDERSTAND = SoapMaker.header(
      "<t2:locale xmlns:t2=\"http://org.jibx.ws/test2\" SOAP:mustUnderstand=\"1\">\n"
    + "<t2:lang>en</t2:lang>\n" 
    + "<t2:country>US</t2:country>\n" 
    + "</t2:locale>\n");

    /**
     * The SOAP message containing the {@link #RESPONSE_SOAP_HEADER} and body.
     */
	public static final String RESPONSE_SOAP = SoapMaker.envelope(
        RESPONSE_SOAP_HEADER,
        org.jibx.ws.soap.testdata.basic.TestObjects.RESPONSE_SOAP_BODY);

    /**
     * The SOAP message containing the {@link #RESPONSE_SOAP_HEADER} and body.
     */
    public static final String RESPONSE_SOAP_MUST_UNDERSTAND = SoapMaker.envelope(
        RESPONSE_SOAP_HEADER_MUST_UNDERSTAND,
        org.jibx.ws.soap.testdata.basic.TestObjects.RESPONSE_SOAP_BODY);
}
