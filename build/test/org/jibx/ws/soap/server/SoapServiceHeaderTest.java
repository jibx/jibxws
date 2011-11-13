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

package org.jibx.ws.soap.server;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.server.Service;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.soap.testdata.header.LocaleMarshaller;
import org.jibx.ws.soap.testdata.header.LocaleUnmarshaller;
import org.jibx.ws.soap.testdata.header.TestHeaderObjects;
import org.jibx.ws.transport.test.StubbedChannel;
import org.jibx.ws.transport.test.StubbedDuplexServerConnection;
import org.jibx.ws.transport.test.StubbedInboundConnection;
import org.jibx.ws.transport.test.StubbedOutboundServerConnection;

/**
 * Tests that SoapService header handlers work correctly using a dummy transport. See also {@link SoapServiceTest}.
 * 
 * @author Nigel Charman
 */
public final class SoapServiceHeaderTest extends TestCase
{
    private StubbedOutboundServerConnection m_outbound;
    private StubbedInboundConnection m_inbound;

    static {
        WsTestHelper.loadBindings();
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        StubbedChannel.getInstance().close();
        StubbedDuplexServerConnection conn = new StubbedDuplexServerConnection();
        m_inbound = (StubbedInboundConnection) conn.getInbound();
        m_outbound = (StubbedOutboundServerConnection) conn.getOutbound();
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        XMLUnit.setIgnoreWhitespace(false);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * reads SOAP headers and invokes a header handler.
     * 
     * @throws Exception e
     */
    public void testProcessRequestReadsSoapHeader() throws Exception {
        m_inbound.setInBytes(TestHeaderObjects.REQUEST_SOAP.getBytes());
        Service service = SoapServiceTestHelper.createSoapServiceWithHandler("findCustomer", "org.jibx.ws.test.locale",
            LocaleUnmarshaller.class);
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        // assertEquals(false, m_outbound.isInternalServerError());

        // Check the header
        assertEquals("Unmarshalled header", TestHeaderObjects.EN, LocaleUnmarshaller.getLocale());

        // Check the body
        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(TestObjects.RESPONSE_SOAP, responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * ignores empty SOAP headers.
     * 
     * @throws Exception e
     */
    public void testProcessRequestIgnoresEmptySoapHeader() throws Exception {
        m_inbound.setInBytes(SoapMaker.envelope("<SOAP:Header/>", TestObjects.REQUEST_SOAP_BODY).getBytes());
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        // assertEquals(false, m_outbound.isInternalServerError());

        // Check the body
        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(TestObjects.RESPONSE_SOAP, responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * ignores empty SOAP headers.
     * 
     * @throws Exception e
     */
    public void testProcessRequestIgnoresEmptySoapHeader2() throws Exception {
        m_inbound.setInBytes(SoapMaker.envelope("<SOAP:Header></SOAP:Header>", TestObjects.REQUEST_SOAP_BODY)
            .getBytes());
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        // assertEquals(false, m_outbound.isInternalServerError());

        // Check the body
        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(TestObjects.RESPONSE_SOAP, responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * creates a MustUnderstand SOAP fault if it cannot understand a header with the mustUnderstand attribute set.
     * 
     * @throws Exception e
     */
    public void testProcessRequestCreatesFaultIfHeaderNotUnderstood() throws Exception {
        m_inbound.setInBytes(TestHeaderObjects.REQUEST_SOAP_MUST_UNDERSTAND.getBytes());
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        assertEquals(true, m_outbound.isInternalServerError());

        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(SoapMaker.soapFault(SoapMaker.MUSTUNDERSTAND_FAULTCODE, ""), responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * writes SOAP headers by invoking a header handler.
     * 
     * @throws Exception e
     */
    public void testProcessRequestWritesSoapHeader() throws Exception {
        m_inbound.setInBytes(TestObjects.REQUEST_SOAP.getBytes());
        // Set the header
        LocaleMarshaller.setLocale(TestHeaderObjects.EN_US);
        Service service = SoapServiceTestHelper.createSoapServiceWithHandler("findCustomer", "org.jibx.ws.test.locale",
            LocaleMarshaller.class);
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        assertEquals(false, m_outbound.isInternalServerError());

        // Check the response includes the header
        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(TestHeaderObjects.RESPONSE_SOAP, responseXML);
    }
}
