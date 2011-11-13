package org.jibx.ws.soap;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.runtime.QName;
import org.jibx.ws.WsException;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.io.handler.MarshallingOutHandler;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.ErrorMessage;
import org.jibx.ws.soap.testdata.basic.ErrorType;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.transport.test.StubbedChannel;
import org.jibx.ws.transport.test.StubbedInboundConnection;

public class SoapProcessorTest extends TestCase
{
    static {
        WsTestHelper.loadBindings();
    }
    
    private MarshallingPayloadWriter m_outBodyHandler;
    private SoapProcessor m_OutInProcessor;
    private SoapProcessor m_InOnlyProcessor;
    private InContext m_inCtx;

    protected void setUp() throws Exception {
        StubbedChannel.getInstance().close();
        m_outBodyHandler = new MarshallingPayloadWriter(Person.class);
        OutContext outCtx = new OutContext();
        outCtx.setBodyWriter(m_outBodyHandler);

        UnmarshallingPayloadReader inBodyHandler = new UnmarshallingPayloadReader(Customer.class);
        m_inCtx = new InContext();
        m_inCtx.setBodyReader(inBodyHandler);
        m_OutInProcessor = (SoapProcessor) SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createOutInExchange(outCtx, m_inCtx));
        m_InOnlyProcessor = (SoapProcessor) SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createInOnlyExchange(m_inCtx));
    }

    public void testMessageAttributesFromTransport() throws Exception {
        StubbedInboundConnection conn = StubbedChannel.getInConnection();
        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
        conn.setCharacterEncoding("ENC");
        conn.setContentType("CT");
        conn.setOperationName("OPNM");
        m_InOnlyProcessor.receiveMessage(conn);
        assertEquals("Character Encoding", conn.getCharacterEncoding(), "ENC");
    }

    public void testSendFaultMessageWithCustomHandlers() throws Exception {
        SoapFault fault = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", "http://example.org/someactor");
        MarshallingOutHandler detail1 = new MarshallingOutHandler(new ErrorMessage("There were lots of elements in the message that I did not understand"));
        MarshallingOutHandler detail2 = new MarshallingOutHandler(new ErrorType("Severe"));
        fault.addDetailWriter(detail1);
        fault.addDetailWriter(detail2);
        m_OutInProcessor.sendFaultMessage(fault, StubbedChannel.getOutConnection());

        XMLUnit.setIgnoreWhitespace(true);
        try {
            XMLAssert.assertXMLEqual("SOAP Fault message", TestObjects.DETAILED_SOAP_FAULT, 
                StubbedChannel.getOutput());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    /**
     * Tests WS-I R1004 When an ENVELOPE contains a faultcode element, the content of that element SHOULD be either ...,
     * or a Qname whose namespace is controlled by the fault's specifying authority.
     * Uses the example SOAP fault provided the WS-I R1004 requirement.
     * 
     * @throws Exception
     */
    public void testSendFaultMessageWithCustomFaultCode() throws Exception {
        QName faultCode = new QName("http://example.org/faultcodes", "c", "ProcessingError");
        SoapFault fault = new SoapFault(faultCode, "An error occured while processing the message", null);
        m_OutInProcessor.sendFaultMessage(fault, StubbedChannel.getOutConnection());

        String expected = SoapMaker.envelope(SoapMaker.body(
                  "<soap:Fault xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'\n"
                + "xmlns:c='http://example.org/faultcodes' >\n" 
                + "<faultcode>c:ProcessingError</faultcode>"
                + "<faultstring>An error occured while processing the message\n</faultstring>\n" 
                + "</soap:Fault>"));

        XMLUnit.setIgnoreWhitespace(true);
        try {
            XMLAssert.assertXMLEqual("SOAP Fault message", expected, StubbedChannel.getOutput());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    /**
     * Tests that a prefix must be specified if a custom fault code is specified. 
     *
     * @throws Exception//    private static final SoapProtocol SOAP1_2 = SoapProtocol.SOAP1_2;
//
//    @Test
//    public final void getMediaTypeFor_givenTextCode_shouldReturnApplicationSoapXml() throws Exception {
//        assertThat(SOAP1_2.getMediaTypeMapper().getMediaTypeFor("xml").toString(), is("application/soap+xml"));
//    }
//
//    @Test
//    public final void getMediaTypeFor_givenXbisCode_shouldReturnTextApplicationSoapXXbis() throws Exception {
//        assertThat(SOAP1_2.getMediaTypeMapper().getMediaTypeFor("x-xbis").toString(), is("application/soap+x-xbis"));
//    }

     */
    public void testSendFaultMessageWithCustomFaultCodeWithNoPrefix() throws Exception {
        try {
            QName faultCode = new QName("http://example.org/faultcodes", "ProcessingError");
            new SoapFault(faultCode, "An error occured while processing the message", null);
            fail("Expected WsException to be thrown if prefix is null");
        } catch (WsException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests that a URI must be specified with a fault code. 
     *
     * @throws Exception
     */
    public void testSendFaultMessageWithFaultCodeWithNoURI() throws Exception {
        try {
            QName faultCode = new QName("ProcessingError");
            new SoapFault(faultCode, "An error occured while processing the message", null);
            fail("Expected WsException to be thrown if URI is null");
        } catch (WsException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithoutErrorDetail() throws Exception {
    	StubbedChannel.setInput(TestObjects.SIMPLE_SOAP_FAULT);
        m_InOnlyProcessor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_SERVER, TestObjects.FAULT_RESPONSE_FAULTSTRING, null);
        assertEquals(expected, m_inCtx.getBody());
    }

    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithErrorDetailWithoutHandlers() throws Exception {
        StubbedChannel.setInput(TestObjects.DETAILED_SOAP_FAULT);
        m_InOnlyProcessor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", "http://example.org/someactor");
        assertEquals(expected, m_inCtx.getBody());
    }

    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithErrorDetailWithOneHandler() throws Exception {
        StubbedChannel.setInput(TestObjects.DETAILED_SOAP_FAULT);
        m_InOnlyProcessor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", "http://example.org/someactor");
        assertEquals(expected, m_inCtx.getBody());
    }
}
