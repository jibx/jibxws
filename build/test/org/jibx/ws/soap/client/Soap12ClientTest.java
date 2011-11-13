/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited All rights reserved.
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

package org.jibx.ws.soap.client;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.runtime.BindingDirectory;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.soap.SoapFault;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.test.StubbedChannel;

/**
 * Tests that the SOAPClient works correctly using a dummy transport for SOAP 1.2.
 * 
 * @author Nigel Charman
 */
public final class Soap12ClientTest extends TestCase
{
    /** Remove this and uncomment tests when SOAP 1.2 implemented. */
    public void testNothing() {
    }
//    static {
//        WsTestHelper.loadBindings();
//        WsTestHelper.loadTestTransport();
//    }
//
//    private static final String ENVELOPE_WITH_EMPTY_BODY = SoapMaker.envelope(SoapMaker.body(""));
//
//    private SoapClient m_soapClient;
//
//    /**
//     * Constructs the {@link SoapClient} using the dummy transport.
//     */
//    protected void setUp() throws Exception {
//        StubbedChannel.getInstance().close();
//        m_soapClient = new SoapClient("stub:", BindingDirectory.getFactory(Person.class));
//        m_soapClient.setProtocol(SoapProtocol.SOAP1_2);
//    }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} creates a valid SOAP request.
//     * 
//     * @throws Throwable
//     */
//    public void testCallCreatesSOAPRequest() throws Throwable {
//        StubbedChannel.setInput(ENVELOPE_WITH_EMPTY_BODY);
//        
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        m_soapClient.setOperationName("testSoapClientAction");
//        m_soapClient.call(TestObjects.REQUEST_OBJECT);
//
//        XMLUnit.setIgnoreWhitespace(true);
//        try {
//            XMLAssert.assertXMLEqual("SOAP Request: ", TestObjects.REQUEST_SOAP, StubbedChannel.getOutput());
//        } finally {
//            XMLUnit.setIgnoreWhitespace(false);
//        }
//    }
//
//    // /**
//    // * Tests that {@link WsClient#call(Object, String)} correctly sets the SOAPAction.
//    // *
//    // * @throws Throwable
//    // */
//    // public void testSOAPCallSetsSOAPAction() throws Throwable {
//    // StubbedChannel.setFakeResponse(TestObjects.RESPONSE_SOAP);
//    // m_soapClient.call(TestObjects.REQUEST_OBJECT, "testSoapClientAction");
//    //        
//    // List properties = m_helper.getObservedProperties();
//    // assertEquals("Properties size ", 1, properties.size());
//    // assertEquals("ContentType: ", new Property("Content-Type", "text/xml"), (Property) properties.get(0));
//    // assertEquals("SOAPAction: ", new Property("SOAPAction", "\"testSoapClientAction\""), (Property)
//    // properties.get(1));
//    // }
//    //
//    // /**
//    // * Tests that {@link WsClient#call(Object, String)} correctly sets an empty SOAPAction.
//    // *
//    // * @throws Throwable
//    // */
//    // public void testSOAPCallSetsEmptySOAPAction() throws Throwable {
//    // StubbedChannel.setFakeResponse(TestObjects.RESPONSE_SOAP);
//    // m_soapClient.call(TestObjects.REQUEST_OBJECT, "");
//    //        
//    // List properties = m_helper.getObservedProperties();
//    // assertEquals("Properties size ", 1, properties.size());
//    // assertEquals("ContentType: ", new Property("Content-Type", "text/xml"), (Property) properties.get(0));
//    // assertEquals("SOAPAction: ", new Property("SOAPAction", "\"\""), (Property) properties.get(1));
//    // }
//    //
//    // /**
//    // * Tests that {@link WsClient#call(Object, String)} correctly sets a blank SOAPAction.
//    // *
//    // * @throws Throwable
//    // */
//    // public void testSOAPCallSetsBlankSOAPAction() throws Throwable {
//    // StubbedChannel.setFakeResponse(TestObjects.RESPONSE_SOAP);
//    // m_soapClient.call(TestObjects.REQUEST_OBJECT, null);
//    //        
//    // List properties = m_helper.getObservedProperties();
//    // assertEquals("Properties size ", 1, properties.size());
//    // assertEquals("ContentType: ", new Property("Content-Type", "text/xml"), (Property) properties.get(0));
//    // assertEquals("SOAPAction: ", new Property("SOAPAction", ""), (Property) properties.get(1));
//    // }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} correctly omits action if null.
//     * 
//     * @throws Throwable
//     */
//    public void testSOAPCallSetsActionOnContentType() throws Throwable {
//        StubbedChannel.setInput(ENVELOPE_WITH_EMPTY_BODY);
//        
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        m_soapClient.setOperationName("testActionURI");
//        m_soapClient.call(TestObjects.REQUEST_OBJECT);
//
//        assertRequestProperties("application/soap+xml", "UTF-8", "testActionURI", "application/soap+xml");
//    }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} correctly omits action if null.
//     * 
//     * @throws Throwable
//     */
//    public void testSOAPCallOmitsActionIfNull() throws Throwable {
//        StubbedChannel.setInput(ENVELOPE_WITH_EMPTY_BODY);
//        
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        m_soapClient.call(TestObjects.REQUEST_OBJECT);
//
//        assertRequestProperties("application/soap+xml", "UTF-8", null, "application/soap+xml");
//    }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} correctly omits action if blank.
//     * 
//     * @throws Throwable
//     */
//    public void testSOAPCallOmitsActionIfBlank() throws Throwable {
//        StubbedChannel.setInput(ENVELOPE_WITH_EMPTY_BODY);
//        
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        m_soapClient.setOperationName("");
//        m_soapClient.call(TestObjects.REQUEST_OBJECT);
//
//        assertRequestProperties("application/soap+xml", "UTF-8", null, "application/soap+xml");
//    }
//
//	/**
//	 * Checks that the <code>properties</code> list contains properties with Content-Type and Accept set to the specified parameters.
//	 * 
//	 * @param contentType
//	 * @param acceptType
//	 */
//	private void assertRequestProperties(String contentType, String charset, String action, String acceptType) throws Exception {
//        MessageProperties properties = StubbedChannel.getProperties();
//		assertNotNull(properties);
//        assertEquals("ContentType: ", new MediaType(contentType), properties.getContentType());
//        assertEquals("Charset: ", charset, properties.getCharset());
//        assertEquals("SOAP Action: ", action, properties.getOperation());
//        assertEquals("Accept types length: ", 1, properties.getAcceptTypes().length);
//        assertEquals("Accept: ", new MediaType(acceptType), properties.getAcceptTypes()[0]);
//	}
//    
//    
//    /**
//     * Tests that {@link SoapClient#call(Object)} unmarshalls the SOAP m_response correctly.
//     * 
//     * @throws Throwable
//     */
//    public void testCallUnmarshalsSOAPResponse() throws Throwable {
//        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
//        
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
//        m_soapClient.setOperationName("testSoapClientAction");
//        Customer result = (Customer) m_soapClient.call(TestObjects.REQUEST_OBJECT);
//
//        assertEquals("Response Object:", TestObjects.RESPONSE_OBJECT, result);
//    }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} throws a {@link SoapFaultException} when a SOAP fault is
//     * returned.
//     * 
//     * @throws Throwable
//     */
//    public void testCallThrowsSoapFaultExceptionOnSOAPFault() throws Throwable {
//        StubbedChannel.setInput(TestObjects.SIMPLE_SOAP_FAULT);
//
//        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
//        try {
//            m_soapClient.setOperationName("testSoapClientAction");
//            m_soapClient.call(TestObjects.REQUEST_OBJECT);
//            fail("Expected SOAPFault to throw SoapFaultException");
//        } catch (SoapFaultException e) {
//            assertEquals("Expected SOAPFault exception message to match faultstring", 
//                TestObjects.FAULT_RESPONSE_FAULTSTRING, e.getMessage());
//            assertEquals("SoapFaultException.getFault().getFaultCode()", SoapFault.FAULT_CODE_SERVER, 
//                e.getFault().getFaultCode());
//        }
//    }
}
