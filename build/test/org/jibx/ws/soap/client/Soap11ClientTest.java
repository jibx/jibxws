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

import java.io.IOException;
import java.rmi.ServerException;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.codec.MediaType;
import org.jibx.ws.io.MessageOptions;
import org.jibx.ws.io.XmlEncoding;
import org.jibx.ws.io.handler.UnmarshallingInHandler;
import org.jibx.ws.soap.SoapFault;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.WsNotUnderstoodException;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.soap.testdata.header.Locale;
import org.jibx.ws.soap.testdata.header.TestHeaderObjects;
import org.jibx.ws.transport.MessageProperties;
import org.jibx.ws.transport.test.StubbedChannel;

/**
 * Tests that the SOAPClient works correctly using a dummy transport for SOAP 1.1.
 * These tests use the {@link Customer} and {@link Person} objects.  In order for the tests to work, these objects must 
 * already have been bound using the binding compiler.
 * 
 * @author Nigel Charman
 */
public final class Soap11ClientTest extends TestCase
{
    private static final String EXPECTED_REQUEST_XML_FORMATTED = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" 
                + "    <SOAP:Body>\n"
                + "        <t1:request xmlns:t1=\"http://org.jibx.ws/test1\">\n"
                + "            <t1:cust-num>123456789</t1:cust-num>\n" 
                + "        </t1:request>\n" 
                + "    </SOAP:Body>\n"
                + "</SOAP:Envelope>";
    private static final String EXPECTED_REQUEST_XML_UNFORMATTED = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" 
                + "<SOAP:Body>"
                + "<t1:request xmlns:t1=\"http://org.jibx.ws/test1\">"
                + "<t1:cust-num>123456789</t1:cust-num>" 
                + "</t1:request>" 
                + "</SOAP:Body>"
                + "</SOAP:Envelope>";
    private static final String EXPECTED_REQUEST_XML_WITH_ENCODING_STYLE = 
        "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" 
        + "<SOAP:Body>"
        + "<t1:request xmlns:t1=\"http://org.jibx.ws/test1\">"
        + "<t1:cust-num>123456789</t1:cust-num>" 
        + "</t1:request>" 
        + "</SOAP:Body>"
        + "</SOAP:Envelope>";
    private static final String ENVELOPE_WITH_EMPTY_BODY = SoapMaker.envelope(SoapMaker.body(""));
    private SoapClient m_soapClient;

    static {
        WsTestHelper.loadBindings();
        WsTestHelper.loadTestTransport();
    }
    
    /**
     * Constructs the {@link SoapClient} using the dummy transport.
     */
    protected void setUp() throws Exception {
        StubbedChannel.getInstance().close();
        m_soapClient = new SoapClient("stub:");
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        XMLUnit.setIgnoreWhitespace(false);
    }
    
    /**
     * Test SoapClient can send empty SOAP body.
     * 
     * @throws Throwable
     */
    public void testSoapClientWithEmptyBody() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
        m_soapClient.call(null);

        XMLAssert.assertXMLEqual("SOAP Request: ", ENVELOPE_WITH_EMPTY_BODY, StubbedChannel.getOutput());
    }

    /**
     * Tests that {@link SoapClient#call(Object)} creates a valid SOAP request.
     * 
     * @throws Throwable
     */
    public void testCallCreatesSOAPRequest() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        invokeCall();

        XMLAssert.assertXMLEqual("SOAP Request: ", TestObjects.REQUEST_SOAP, StubbedChannel.getOutput());
    }

    /**
     * Tests that {@link SoapClient#call(Object)} creates a valid SOAP header.
     * 
     * @throws Throwable
     */
     public void testCallCreatesSoapHeader() throws Throwable {
         StubbedChannel.setInput(TestHeaderObjects.RESPONSE_SOAP);
         m_soapClient.addOutHeader(TestHeaderObjects.EN);
         
         invokeCall();
        
         XMLAssert.assertXMLEqual("SOAP Request: ", TestHeaderObjects.REQUEST_SOAP, StubbedChannel.getOutput());
     }
    
     /**
      * Tests that {@link SoapClient#call(Object)} creates a valid SOAP header with two elements.
      * 
      * @throws Throwable
      */
      public void testCallCreatesTwoSoapHeaders() throws Throwable {
          StubbedChannel.setInput(TestHeaderObjects.RESPONSE_SOAP);
          m_soapClient.addOutHeader(TestHeaderObjects.EN);
          m_soapClient.addOutHeader(TestHeaderObjects.FR);
          
          invokeCall();
         
          XMLAssert.assertXMLEqual("SOAP Request: ", TestHeaderObjects.REQUEST_SOAP2, StubbedChannel.getOutput());
      }
      
    /**
     * Tests that {@link SoapClient#call(Object)} correctly sets the SOAPAction.
     * 
     * @throws Throwable
     */
    public void testSOAPCallSetsSOAPAction() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
        
        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
        m_soapClient.setOperationName("testSoapClientAction");
        m_soapClient.call(TestObjects.REQUEST_OBJECT);

        MessageProperties properties = StubbedChannel.getProperties();
        assertRequestProperties(properties, "text/xml", "UTF-8", "text/xml", "\"testSoapClientAction\"");
    }

    /**
     * Tests that {@link SoapClient#call(Object)} correctly sets an empty SOAPAction.
     * 
     * @throws Throwable
     */
    public void testSOAPCallSetsEmptySOAPAction() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
        
        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
        m_soapClient.setOperationName("");
        m_soapClient.call(TestObjects.REQUEST_OBJECT);

        MessageProperties properties = StubbedChannel.getProperties();
        assertRequestProperties(properties, "text/xml", "UTF-8", "text/xml", "\"\"");
    }

    /**
     * Tests that {@link SoapClient#call(Object)} correctly sets a blank SOAPAction.
     * 
     * @throws Throwable
     */
    public void testSOAPCallSetsBlankSOAPAction() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
        
        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
        m_soapClient.call(TestObjects.REQUEST_OBJECT);

        MessageProperties properties = StubbedChannel.getProperties();
        assertRequestProperties(properties, "text/xml", "UTF-8", "text/xml", "");
    }

	/**
	 * Checks that the <code>properties</code> list contains properties with Content-Type, Accept and SOAPAction set to the specified parameters.
	 * 
	 * @param properties
	 * @param contentType
	 * @param acceptType
	 * @param soapAction
	 */
	private void assertRequestProperties(MessageProperties properties, String contentType, String charset, String acceptType, String soapAction) throws Exception {
        assertNotNull(properties);
        assertEquals("SOAPAction: ", soapAction, properties.getProperty("SOAPAction"));
        assertEquals("ContentType: ", new MediaType(contentType), properties.getContentType());
        assertEquals("Charset: ", charset, properties.getCharset());
        assertEquals("Accept types length: ", 1, properties.getAcceptTypes().length);
        assertEquals("Accept: ", new MediaType(acceptType), properties.getAcceptTypes()[0]);
	}
    
    /**
     * Tests that {@link SoapClient#call(Object)} unmarshalls the SOAP response correctly.
     * 
     * @throws Throwable
     */
    public void testCallUnmarshalsSOAPResponse() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        Customer result = invokeCall();

        assertEquals("Response Object:", TestObjects.RESPONSE_OBJECT, result);
    }

    /**
     * Tests that {@link SoapClient#call(Object)} unmarshalls an empty body as a null object.
     * 
     * @throws Throwable
     */
    public void testCallUnmarshalsEmptySoapBody() throws Throwable {
        StubbedChannel.setInput(ENVELOPE_WITH_EMPTY_BODY);
        
        Customer result = invokeCall();

        assertEquals("Response Object:", null, result);
    }

    /**
     * Tests that {@link SoapClient#call(Object)} unmarshalls the SOAP header correctly.
     * 
     * @throws Throwable
     */
    public void testCallUnmarshalsSOAPHeader() throws Throwable {
        StubbedChannel.setInput(TestHeaderObjects.RESPONSE_SOAP);

        UnmarshallingInHandler headerReader = new UnmarshallingInHandler(Locale.class);
        m_soapClient.addInHeaderHandler(headerReader);
        Customer result = invokeCall();

        Object header = headerReader.getPayload();
        assertEquals("Header object", TestHeaderObjects.EN_US, header);

        assertEquals("Response Object:", TestObjects.RESPONSE_OBJECT, result);
    }
    
    /**
     * Tests that {@link SoapClient#call(Object)} throws a {@link WsNotUnderstoodException} if a SOAP
     * header has the SOAP mustUnderstand attribute set, and no handler is defined for the header.
     * 
     * @throws Throwable
     */
    public void testCallThrowsNotUnderstoodExceptionOnSOAPHeaderWithoutHandler() throws Throwable {
        StubbedChannel.setInput(TestHeaderObjects.RESPONSE_SOAP_MUST_UNDERSTAND);

        try {
            invokeCall();
            fail("Expected WsNotUnderstoodException to be thrown");
        } catch (WsNotUnderstoodException expected) {
            
        }
    }
    
    /**
     * Tests that {@link SoapClient#call(Object)} throws a {@link ServerException} when a SOAP fault is
     * returned.
     * 
     * @throws Throwable
     */
    public void testCallThrowsSoapFaultExceptionOnSOAPFault() throws Throwable {
        StubbedChannel.setInput(TestObjects.SIMPLE_SOAP_FAULT);

        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));

        try {
            m_soapClient.call(TestObjects.REQUEST_OBJECT);
            fail("Expected SOAPFault to throw SoapFaultException");
        } catch (SoapFaultException e) {
            assertEquals("Expected SOAPFault exception message to match faultstring", 
                TestObjects.FAULT_RESPONSE_FAULTSTRING, e.getMessage());
            assertEquals("SoapFaultException.getFault().getFaultCode()", SoapFault.FAULT_CODE_SERVER, 
                e.getFault().getFaultCode());
        }
    }
    
    /**
     * Checks that the SoapClient throws an {@link IllegalStateException} if no service location has been specified.
     * @throws Throwable
     */
    public void testSoapClientThrowsNPEfNoServiceLocation() throws Throwable {
        try {
            new SoapClient(null, BindingDirectory.getFactory(Person.class));
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertTrue("Expected exception message to contain the word \"location\"", 
                expected.getMessage().contains("location"));
        }
    }

    /**
     * Checks that the SoapClient throws an {@link WsException} if bindings haven't been defined for the outbound 
     * SOAP body.
     * @throws Throwable
     */
    public void testSoapClientThrowsWSEIfRequestObjectNotBound() throws Throwable {
        try {
            m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
            m_soapClient.call(new java.util.Date());
            fail("Expected WsException");
        } catch (WsException expected) {
            assertTrue(
                "Expected exception message to contain the word \"mapping\" but got '" + expected.getMessage() + "'", 
                expected.getMessage().contains("mapping"));
        }
    }

    /**
     * Checks that the SoapClient throws a {@link WsException} if no handlers have been defined for the inbound 
     * SOAP body.
     * @throws Throwable
     */
    public void testSoapClientThrowsWSEIfNoHandlerForResponseBody() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));

        try {
            m_soapClient.call(TestObjects.REQUEST_OBJECT);
        } catch (WsException expected) {
            assertTrue("Expected root cause exception message to contain the word \"No handlers\"", 
                expected.getMessage().contains("No handlers"));
        }
    }
    
    /**
     * Tests that {@link SoapClient#call(Object)} creates a valid SOAP request with no indentation.
     * 
     * @throws Throwable
     */
    public void testDefaultOutputFormatContainsNoIndentation() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        invokeCall();

        String expectedRequest = EXPECTED_REQUEST_XML_UNFORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }
    
    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the output format correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsChangesOutputFormat() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(' ', 4, "\n"));
        invokeCall();

        String expectedRequest = EXPECTED_REQUEST_XML_FORMATTED;
        
        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }    

    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the XML declaration correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsSetsXMLEncodingAndStandaloneDeclaration() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(XmlEncoding.UTF_8, true, Boolean.TRUE));
        invokeCall();

        String expectedRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" 
            + EXPECTED_REQUEST_XML_UNFORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }

    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the XML declaration correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsSetsXMLEncodingAndNotStandaloneDeclaration() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(XmlEncoding.UTF_8, true, Boolean.FALSE));
        invokeCall();

        String expectedRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + EXPECTED_REQUEST_XML_UNFORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }
    
    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the XML declaration correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsSetsStandaloneDeclaration() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(XmlEncoding.UTF_8, false, Boolean.FALSE));
        invokeCall();

        String expectedRequest = "<?xml version=\"1.0\" standalone=\"no\"?>"
            + EXPECTED_REQUEST_XML_UNFORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }


    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the XML declaration correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsSetsXMLEncoding() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(XmlEncoding.UTF_8, true, null));
        invokeCall();

        String expectedRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + EXPECTED_REQUEST_XML_UNFORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }

    /**
     * Tests that {@link SoapClient#setMessageOptions(MessageOptions)} changes the output format correctly.
     * @throws Throwable
     */
    public void testSetMessageOptionsChangesOutputFormatAndXmlEncoding() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setMessageOptions(new MessageOptions(XmlEncoding.UTF_8, true, Boolean.TRUE, ' ', 4, "\n"));
        invokeCall();

        String expectedRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" 
            + EXPECTED_REQUEST_XML_FORMATTED;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }    

    /**
     * Tests that {@link SoapClient#setSoapEncodingStyle(String)} adds a soap:encodingStyle attribute to the 
     * SOAP Envelope.
     * @throws Throwable
     */
    public void testSetSoapEncodingStyleAddsAttributeToSoapEnvelope() throws Throwable {
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);

        m_soapClient.setSoapEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
        invokeCall();

        String expectedRequest = EXPECTED_REQUEST_XML_WITH_ENCODING_STYLE;

        assertEquals("SOAP Request: ", expectedRequest, StubbedChannel.getOutput());
    }
    
    private Customer invokeCall() throws JiBXException, IOException, WsException {
        m_soapClient.setOutBodyBindingFactory(BindingDirectory.getFactory(Person.class));
        m_soapClient.setInBodyBindingFactory(BindingDirectory.getFactory(Customer.class));
        m_soapClient.setOperationName("testSoapClientAction");
        Customer result = (Customer) m_soapClient.call(TestObjects.REQUEST_OBJECT);
        return result;
    }
}
