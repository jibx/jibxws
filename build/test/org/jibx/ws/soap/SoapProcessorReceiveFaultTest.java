package org.jibx.ws.soap;

import junit.framework.TestCase;

import org.jibx.ws.WsTestHelper;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.io.handler.UnmarshallingInHandler;
import org.jibx.ws.process.Processor;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.ErrorMessage;
import org.jibx.ws.soap.testdata.basic.ErrorType;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.transport.test.StubbedChannel;

/**
 * Test that SoapProcessor receives SOAP Faults correctly.
 */
public class SoapProcessorReceiveFaultTest extends TestCase
{
    static {
        WsTestHelper.loadBindings();
    }
    
    private InContext m_inCtx;

    protected void setUp() throws Exception {
        StubbedChannel.getInstance().close();
        UnmarshallingPayloadReader inBodyHandler = new UnmarshallingPayloadReader(Customer.class);
        m_inCtx = new InContext();
        m_inCtx.setBodyReader(inBodyHandler);
    }
    
    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithoutErrorDetail() throws Exception {
        Processor processor = SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createInOnlyExchange(m_inCtx));
        StubbedChannel.setInput(TestObjects.SIMPLE_SOAP_FAULT);
        processor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_SERVER, TestObjects.FAULT_RESPONSE_FAULTSTRING, null);
        assertEquals(expected, m_inCtx.getBody());
    }

    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithErrorDetailWithoutHandlers() throws Exception {
        Processor processor = SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createInOnlyExchange(m_inCtx));
        StubbedChannel.setInput(TestObjects.DETAILED_SOAP_FAULT);
        processor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", "http://example.org/someactor");
        assertEquals(expected, m_inCtx.getBody());
    }

    /**
     * Tests that a SOAP fault without SOAP details is received correctly.
     * @throws Exception
     */
    public void testReceiveFaultMessageWithErrorDetailWithHandler() throws Exception {
        m_inCtx.addHandler(SoapPhase.BODY_FAULT, new UnmarshallingInHandler(ErrorMessage.class));
        Processor processor = SoapProtocol.SOAP1_1.createProcessor(ExchangeContext.createInOnlyExchange(m_inCtx));
        StubbedChannel.setInput(TestObjects.DETAILED_SOAP_FAULT);
        processor.receiveMessage(StubbedChannel.getInConnection());
        SoapFault expected = new SoapFault(SoapFault.FAULT_CODE_CLIENT, "Invalid message format", "http://example.org/someactor");
        expected.addDetail(new ErrorMessage("There were lots of elements in the message that I did not understand"));
        expected.addDetail(new ErrorType("Severe"));
        
        assertEquals(expected, m_inCtx.getBody());
    }
}
