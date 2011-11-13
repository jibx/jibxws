/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited. All rights reserved.
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServicePool;
import org.jibx.ws.soap.SoapProtocol;
import org.jibx.ws.soap.testdata.SoapMaker;
import org.jibx.ws.soap.testdata.basic.TestObjects;
import org.jibx.ws.transport.test.StubbedDuplexServerConnection;
import org.jibx.ws.transport.test.StubbedInboundConnection;
import org.jibx.ws.transport.test.StubbedOutboundServerConnection;
import org.jibx.ws.wsdl.InputStreamWsdlProvider;
import org.jibx.ws.wsdl.WsdlLocationToRequestUrlAdapter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the basic SoapService works correctly using a dummy transport. See also {@link SoapServiceHeaderTest}.
 * 
 * @author Nigel Charman
 */
public final class SoapServiceTest extends TestCase
{
    private static final String WSDL_FILE_PATH = "build/test/MyService.wsdl";
    private StubbedOutboundServerConnection m_outbound;
    private StubbedInboundConnection m_inbound;

    static {
        WsTestHelper.loadBindings();
    }

    /**
     * Constructs the dummy transport, and assigns {@link TestObjects#REQUEST_SOAP} to be the incoming request for the
     * server.
     * 
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        StubbedDuplexServerConnection conn = new StubbedDuplexServerConnection();
        m_inbound = (StubbedInboundConnection) conn.getInbound();
        m_inbound.setInBytes(TestObjects.REQUEST_SOAP.getBytes());
        m_outbound = (StubbedOutboundServerConnection) conn.getOutbound();
        XMLUnit.setIgnoreWhitespace(true);
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        XMLUnit.setIgnoreWhitespace(false);
    }

    // **********
    // Test Cases
    // **********
    /**
     * Tests that {@link ServicePool#getInstance(org.jibx.ws.server.ServiceFactory, ServiceDefinition)} returns a
     * <code>SoapService</code>.
     * 
     * @throws Exception e
     */
    public void testGetInstanceReturnsSoapService() throws Exception {
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");

        assertNotNull(service);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * returns a valid m_response when the service completes successfully.
     * 
     * @throws Exception e
     */
    public void testProcessRequestReturnsValidResponseWhenServiceSuccessful() throws Exception {
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");

        service.processRequest(m_inbound, m_outbound);

        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(TestObjects.RESPONSE_SOAP, responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * logs no errors when the service completes successfully.
     * 
     * @throws Exception e
     */
    public void testProcessRequestLogsNoErrorsWhenServiceSuccessful() throws Exception {
        Service service = SoapServiceTestHelper.createSoapService("findCustomer");

        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        assertEquals(false, m_outbound.isInternalServerError());
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * returns a SOAPFault when the service throws an exception.
     * 
     * @throws Exception e
     */
    public void testProcessRequestReturnsSOAPFaultWhenServiceThrowsException() throws Exception {
        Service service = SoapServiceTestHelper.createSoapService("throwIllegalArgumentException");
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        assertEquals(true, m_outbound.isInternalServerError());
        String responseXML = new String(m_outbound.getOutBytes());
        XMLAssert.assertXMLEqual(SoapMaker.soapServerFault("Dummy IAE"), responseXML);
    }

    /**
     * Tests that
     * {@link SoapService#processRequest(org.jibx.ws.transport.InConnection, org.jibx.ws.transport.OutServerConnection)}
     * logs the fault when the service throws an exception.
     * 
     * @throws Exception e
     */
    public void testProcessRequestLogsErrorWhenServiceThrowsException() throws Exception {
        Service service = SoapServiceTestHelper.createSoapService("throwIllegalArgumentException");
        service.processRequest(m_inbound, m_outbound);

        assertEquals(false, m_outbound.isNotFoundError());
        assertEquals(true, m_outbound.isInternalServerError());

        // assertEquals("Log size", 1, m_logger.getLogEntries().size());
        // StubbedLogger.LogEntry log = (StubbedLogger.LogEntry)m_logger.getLogEntries().get(0);
        // assertEquals("Log message:", "Error processing request", log.message);
        // assertEquals("Log error class:", InvocationTargetException.class, log.error.getClass());
        // Throwable wrapped = ((InvocationTargetException)log.error).getTargetException();
        // assertEquals("Wrapped error class:", IllegalArgumentException.class, wrapped.getClass());
        // assertEquals("Wrapped error message:", "Dummy IAE", wrapped.getMessage());
    }
    
    @Test
    public void givenWsdlFileInServiceDefinition_SoapServiceShouldBeConfiguredWithInputStreamWsdlProvider() 
            throws Exception {
        ServiceDefinition sdef = new ServiceDefinition();
        sdef.setOperationDefinitions(Collections.EMPTY_LIST);
        sdef.setWsdlFilepath(WSDL_FILE_PATH);
        Service soapService = SoapProtocol.SOAP1_1.getServiceFactory().createInstance(sdef);
        assertThat(soapService.getWsdlProvider(), instanceOf(InputStreamWsdlProvider.class));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ((InputStreamWsdlProvider)soapService.getWsdlProvider()).writeWSDL(baos, null);
        String wsdl = FileUtils.readFileToString(new File(WSDL_FILE_PATH));
        assertThat(baos.toString(), is(wsdl));
    }

    @Test
    public void givenInvalidWsdlFileInServiceDefinition_SoapServiceShouldThrowWsException() throws Exception {
        ServiceDefinition sdef = new ServiceDefinition();
        sdef.setOperationDefinitions(Collections.EMPTY_LIST);
        sdef.setWsdlFilepath(WSDL_FILE_PATH + "nonexistent");
        try {
            SoapProtocol.SOAP1_1.getServiceFactory().createInstance(sdef);
            Assert.fail("Expected WsException");
        } catch (WsConfigurationException e) {
            assertThat(e.getMessage(), containsString(WSDL_FILE_PATH));
        }
    }

    @Test
    public void givenWsdlTransformInServiceDefinition_SoapServiceShouldBeConfiguredWithWsdlAdapter() 
            throws Exception {
        ServiceDefinition sdef = new ServiceDefinition();
        sdef.setOperationDefinitions(Collections.EMPTY_LIST);
        sdef.setWsdlFilepath(WSDL_FILE_PATH);
        sdef.setWsdlLocationTransform(true);
        Service soapService = SoapProtocol.SOAP1_1.getServiceFactory().createInstance(sdef);
        assertThat(soapService.getWsdlProvider(), instanceOf(WsdlLocationToRequestUrlAdapter.class));
    }
}
