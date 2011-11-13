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

package org.jibx.ws.soap.client;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.MarshallingPayloadWriter;
import org.jibx.ws.io.UnmarshallingPayloadReader;
import org.jibx.ws.io.handler.MarshallingOutHandler;
import org.jibx.ws.io.handler.UnmarshallingInHandler;
import org.jibx.ws.process.Processor;
import org.jibx.ws.soap.SoapPhase;
import org.jibx.ws.soap.SoapProcessor;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.soap.testdata.header.Locale;
import org.jibx.ws.soap.testdata.header.TestHeaderObjects;
import org.jibx.ws.transport.test.StubbedChannel;

/**
 * Tests that the {@link SoapProcessor} works correctly for writing and reading headers using a dummy transport for SOAP 1.2.
 *
 * @author Nigel Charman
 */
public final class Soap12ClientHeaderTest extends TestCase
{
    /** Remove this and uncomment tests when SOAP 1.2 implemented. */
    public void testNothing() {
    }
//    static {
//        WsTestHelper.loadBindings();
//    }
//    
//    private Processor m_processor;
//    private MarshallingPayloadWriter m_outBodyHandler;
//    private MarshallingOutHandler m_outHeaderHandler;
//    private OutContext m_outCtx;
//    private UnmarshallingInHandler m_headerUnmarshaller;
//
//	/**
//	 * Constructs the {@link SoapProcessor} using the dummy transport.
//	 *
//	 * {@inheritDoc}
//	 */
//	protected void setUp() throws Exception {
//        StubbedChannel.getInstance().close();
//        m_outBodyHandler = new MarshallingPayloadWriter(Person.class);
//        m_outHeaderHandler = new MarshallingOutHandler(TestHeaderObjects.EN);
//        m_outCtx = new OutContext();
//        m_outCtx.setBodyWriter(m_outBodyHandler);
//        m_outCtx.setBody(TestObjects.REQUEST_OBJECT);
//        m_outCtx.addHandler(SoapPhase.HEADER, m_outHeaderHandler);
//        
//        UnmarshallingPayloadReader inBodyHandler = new UnmarshallingPayloadReader(Customer.class);
//        m_headerUnmarshaller = new UnmarshallingInHandler(Locale.class);
//        InContext inCtx = new InContext();
//        inCtx.setBodyReader(inBodyHandler);
//        inCtx.addHandler(SoapPhase.HEADER, m_headerUnmarshaller);
//
//        m_processor = SoapProtocol.SOAP1_2.createProcessor(ExchangeContext.createOutInExchange(m_outCtx, inCtx));
//	}
//	
//    /**
//     * Tests that a SOAP header is constructed correctly.
//     * 
//     * @throws Throwable
//     */
//    public void testProcessorCreatesSOAPHeader() throws Throwable {
//        StubbedChannel.setInput(TestObjects.RESPONSE_SOAP);
//        m_processor.invoke(StubbedChannel.getOutConnection(), StubbedChannel.getInConnection());
//        
//        XMLUnit.setIgnoreWhitespace(true);
//        try {
//            XMLAssert.assertXMLEqual("SOAP Request: ", TestHeaderObjects.REQUEST_SOAP, StubbedChannel.getOutput());
//        } finally {
//            XMLUnit.setIgnoreWhitespace(false);
//        }
//    }
//
//    /**
//     * Tests that {@link SoapClient#call(Object)} unmarshalls the SOAP m_response correctly.
//     * 
//     * @throws Throwable
//     */
//    public void testProcessorUnmarshalsSOAPHeader() throws Throwable {
//        StubbedChannel.setInput(TestHeaderObjects.RESPONSE_SOAP);
//        m_processor.invoke(StubbedChannel.getOutConnection(), StubbedChannel.getInConnection());
//        
//        assertEquals("Response Header Object:", TestHeaderObjects.EN_US, m_headerUnmarshaller.getPayload());
//    }
//    
//    
////    /**
////     * Tests that {@link WsClient#call(Object, String)} correctly sets the SOAPAction.
////     * 
////     * @throws Throwable
////     */
////	public void testSOAPCallSetsSOAPAction() throws Throwable {
////        StubbedChannel.setInData(TestObjects.RESPONSE_SOAP);
////        m_soapClient.call(TestObjects.REQUEST_OBJECT, "testSoapClientAction");
////        
////        List properties = m_helper.getObservedProperties();
////        assertEquals("Properties size ", 1, properties.size());
////        assertEquals("ContentType: ", new Property("Content-type", "text/xml"), (Property) properties.get(0));
////        assertEquals("SOAPAction: ", new Property("SOAPAction", "\"testSoapClientAction\""), (Property) properties.get(1));
////    }
////
////    /**
////     * Tests that {@link WsClient#call(Object, String)} correctly sets an empty SOAPAction.
////     * 
////     * @throws Throwable
////     */
////    public void testSOAPCallSetsEmptySOAPAction() throws Throwable {
////        StubbedChannel.setInData(TestObjects.RESPONSE_SOAP);
////        m_soapClient.call(TestObjects.REQUEST_OBJECT, "");
////        
////        List properties = m_helper.getObservedProperties();
////        assertEquals("Properties size ", 1, properties.size());
////        assertEquals("ContentType: ", new Property("Content-type", "text/xml"), (Property) properties.get(0));
////        assertEquals("SOAPAction: ", new Property("SOAPAction", "\"\""), (Property) properties.get(1));
////    }
////
////    /**
////     * Tests that {@link WsClient#call(Object, String)} correctly sets a blank SOAPAction.
////     * 
////     * @throws Throwable
////     */
////    public void testSOAPCallSetsBlankSOAPAction() throws Throwable {
////        StubbedChannel.setInData(TestObjects.RESPONSE_SOAP);
////        m_soapClient.call(TestObjects.REQUEST_OBJECT, null);
////        
////        List properties = m_helper.getObservedProperties();
////        assertEquals("Properties size ", 1, properties.size());
////        assertEquals("ContentType: ", new Property("Content-type", "text/xml"), (Property) properties.get(0));
////        assertEquals("SOAPAction: ", new Property("SOAPAction", ""), (Property) properties.get(1));
////    }
}
