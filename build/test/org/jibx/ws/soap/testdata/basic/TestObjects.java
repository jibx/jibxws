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

package org.jibx.ws.soap.testdata.basic;

import org.jibx.ws.soap.testdata.SoapMaker;

/**
 * Constructs objects to be used in testing, both as Java objects and as SOAP messages.
 *
 * @author Nigel Charman
 */
public class TestObjects {

    /**
     * A request object for a specific person. 
     */
	public static final Person REQUEST_OBJECT = new Person(123456789);
	
    /**
     * The SOAP body corresponding to the {@link #REQUEST_OBJECT}.
     */
    public static final String REQUEST_SOAP_BODY = SoapMaker.body(
          "<t1:request xmlns:t1=\"http://org.jibx.ws/test1\">\n"
        + "<t1:cust-num>123456789</t1:cust-num>\n" 
        + "</t1:request>\n");

    /**
     * The SOAP body corresponding to the {@link #REQUEST_OBJECT}.
     */
    public static final String DETAILED_SOAP_FAULT = SoapMaker.envelope(SoapMaker.body(
          "<SOAP:Fault>\n"
        + "<faultcode>SOAP:Client</faultcode>\n"
        + "<faultstring>Invalid message format</faultstring>\n"
        + "<faultactor>http://example.org/someactor</faultactor>\n"
        + "<detail>\n"
        + "<m:msg xmlns:m='http://example.org/faults/exceptions'>" 
        + "There were lots of elements in the message that I did not understand"
        + "</m:msg>\n"
        + "<m:Exception xmlns:m='http://example.org/faults/exceptions'>\n"
        + "<m:ExceptionType>Severe</m:ExceptionType>\n"
        + "</m:Exception>\n"
        + "</detail>\n"
        + "</SOAP:Fault>\n"));
    
    /**
     * The SOAP message corresponding to the {@link #REQUEST_OBJECT}.
     */
	public static final String REQUEST_SOAP = SoapMaker.envelope(REQUEST_SOAP_BODY);

    /**
     * A m_response Customer object for the specific person. 
     */
    public static final Object RESPONSE_OBJECT = createExpectedNormalResponseObject();


    /**  The SOAP body corresponding to the {@link #RESPONSE_OBJECT}. */
    public static final String RESPONSE_SOAP_BODY = SoapMaker.body(
          "<t1:customer xmlns:t1=\"http://org.jibx.ws/test1\">\n"
        + "<t1:person>\n"
        + "<t1:cust-num>123456789</t1:cust-num>\n"
        + "<t1:first-name>John</t1:first-name>\n" 
        + "<t1:last-name>Smith</t1:last-name>\n"
        + "</t1:person>\n"
        + "<t1:street>12345 Happy Lane</t1:street>\n"
        + "<t1:city>Plunk</t1:city>\n"
        + "<t1:state>WA</t1:state>\n"
        + "<t1:zip>98059</t1:zip>\n"
        + "<t1:phone>888.555.1234</t1:phone>\n"
        + "</t1:customer>\n");

    /**
     * The SOAP message corresponding to the {@link #RESPONSE_OBJECT}.
     */
	public static final String RESPONSE_SOAP = SoapMaker.envelope(RESPONSE_SOAP_BODY);
	
    /**
     * A SOAP Fault string that is used for constructing the {@link #SIMPLE_SOAP_FAULT}.
     */
	public static final String FAULT_RESPONSE_FAULTSTRING = "Internal server error: Error Unmarshalling Request";
	/**
     * A sample SOAP fault message. 
	 */
    public static final String SIMPLE_SOAP_FAULT = SoapMaker.soapFault(SoapMaker.SERVER_FAULTCODE, FAULT_RESPONSE_FAULTSTRING);
    
    /**
     * Returns a {@link Customer} object that corresponds to the {@link #RESPONSE_SOAP} message.
     * 
     * @return customer
     */
	public static Object createExpectedNormalResponseObject() {
		Customer cust = new Customer();
		Person person = new Person();
		person.customerNumber = 123456789;
		person.firstName = "John";
		person.lastName = "Smith";
		cust.person = person;
		cust.street = "12345 Happy Lane";
		cust.city = "Plunk";
		cust.state = "WA";
		cust.zip = new Integer(98059);
		cust.phone = "888.555.1234";
		return cust;
	}

}
