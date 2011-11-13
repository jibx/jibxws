package org.jibx.ws.soap;

import java.util.List;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.runtime.QName;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.io.handler.MarshallingOutHandler;
import org.jibx.ws.soap.server.SoapFaultHandler;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.ErrorMessage;
import org.jibx.ws.soap.testdata.basic.ErrorType;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.transport.test.StubbedChannel;

/**
 * Test that SoapProcessor sends SOAP Faults correctly.
 */
public class SoapProcessorSendFaultTest extends TestCase
{
    static {
        WsTestHelper.loadBindings();
    }
    
    private MarshallingPayloadWriter m_outBodyHandler;
    private SoapProcessor m_processor;

    protected void setUp() throws Exception {
        StubbedChannel.getInstance().close();
        m_outBodyHandler = new MarshallingPayloadWriter(Person.class);
        OutContext outCtx = new OutContext();
        outCtx.setBodyWriter(m_outBodyHandler);

        UnmarshallingPayloadReader inBodyHandler = new UnmarshallingPayloadReader(Customer.class);
        InContext inCtx = new InContext();
        inCtx.setBodyReader(inBodyHandler);

        m_processor = (SoapProcessor) SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createOutInExchange(outCtx, inCtx));
    }

    /**
     * Tests that SOAP Fault details are correctly written using custom handlers.
     *
     * @throws Exception
     */
    public void testSendFaultMessageWithCustomHandlers() throws Exception {
        SoapFault fault = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", 
            "http://example.org/someactor");
        MarshallingOutHandler detail1 = new MarshallingOutHandler(new ErrorMessage("There were lots of elements in the message that I did not understand"));
        MarshallingOutHandler detail2 = new MarshallingOutHandler(new ErrorType("Severe"));
        fault.addDetailWriter(detail1);
        fault.addDetailWriter(detail2);
        m_processor.sendFaultMessage(fault, StubbedChannel.getOutConnection());

        XMLUnit.setIgnoreWhitespace(true);
        try {
            XMLAssert.assertXMLEqual("SOAP Fault message", TestObjects.DETAILED_SOAP_FAULT, 
                StubbedChannel.getOutput());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    /**
     * Tests custom faultcode compliance with WS-I R1004. WS-I R1004 states "When an ENVELOPE contains a faultcode
     * element, the content of that element SHOULD be either one of the fault codes defined in SOAP 1.1 (...), or a
     * Qname whose namespace is controlled by the fault's specifying authority." This tests the 2nd case. Uses the
     * example SOAP fault provided the WS-I R1004 requirement.
     * 
     * @throws Exception
     */
    public void testSendFaultMessageWithCustomFaultCode() throws Exception {
        QName faultCode = new QName("http://example.org/faultcodes", "c", "ProcessingError");
        SoapFault fault = new SoapFault(faultCode, "An error occured while processing the message", null);
        m_processor.sendFaultMessage(fault, StubbedChannel.getOutConnection());

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
     * Tests that sending an exception with a stack trace creates correct output.
     * 
     * @throws Exception
     */
    public void testSendFaultOnExceptionWithStackTrace() throws Exception {
        
        SoapFaultHandler faultHandler = new SoapFaultHandler(true);
        
        try {
            throw new IllegalStateException("Dummy Exception");
        } catch (Throwable t) {
            faultHandler.handleException(t, m_processor, StubbedChannel.getOutConnection());
        }
        String expected = SoapMaker.envelope(SoapMaker.body("<SOAP:Fault>\n" + "<faultcode>SOAP:Server</faultcode>\n"
            + "<faultstring>Dummy Exception</faultstring>\n" + "<detail>\n"
            + "<Exception>java.lang.IllegalStateException: Dummy Exception\n\tat" + "</Exception>\n" + "</detail>\n"
            + "</SOAP:Fault>\n"));
        XMLUnit.setIgnoreWhitespace(true);
        try {
            DetailedDiff diffs = new DetailedDiff(XMLUnit.compareXML(expected, StubbedChannel.getOutput()));
            List allDiffs = diffs.getAllDifferences();
            assertEquals("Only expected 1 difference", 1, allDiffs.size());
            Difference diff = (Difference) allDiffs.get(0);
            String expectedDiffPath = "/Envelope[1]/Body[1]/Fault[1]/detail[1]/Exception[1]/text()[1]";
            assertEquals("Expected difference to be at " + expectedDiffPath, expectedDiffPath, diff.getTestNodeDetail()
                .getXpathLocation());
            String expectedText = diff.getControlNodeDetail().getValue();
            String actualText = diff.getTestNodeDetail().getValue();
            assertTrue("Expected <Exception> element text to start with " + expectedText, actualText
                .startsWith(expectedText));
            String methodName = "testSendFaultOnExceptionWithStackTrace";
            assertTrue("Expected <Exception> element text to include " + methodName + " in stack trace", actualText
                .indexOf(methodName) != -1);
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

    /**
     * Tests that sending an error without a stack trace creates correct output.
     * @throws Exception
     */
    public void testSendFaultOnErrorWithoutStackTrace() throws Exception {
        SoapFaultHandler faultHandler = new SoapFaultHandler(false);
        try {
            throw new AssertionError("Dummy Assertion Error <99>");
        } catch (Throwable t) {
            faultHandler.handleException(t, m_processor, StubbedChannel.getOutConnection());
        }

        String expected = SoapMaker.envelope(SoapMaker.body("<SOAP:Fault>\n" + "<faultcode>SOAP:Server</faultcode>\n"
            + "<faultstring>Dummy Assertion Error &lt;99></faultstring>\n" 
            + "</SOAP:Fault>\n"));

        XMLUnit.setIgnoreWhitespace(true);
        try {
            XMLAssert.assertXMLEqual("SOAP Fault message", expected, StubbedChannel.getOutput());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }
}
