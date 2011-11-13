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

import java.util.Arrays;

import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.WsTestHelper;
import org.jibx.ws.protocol.ProtocolDirectory;
import org.jibx.ws.server.HandlerDefinition;
import org.jibx.ws.server.OperationDefinition;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.server.ServicePool;
import org.jibx.ws.soap.testdata.basic.Customer;
import org.jibx.ws.soap.testdata.basic.Person;
import org.jibx.ws.soap.testdata.basic.TestObjects;

/**
 * Provides helper methods for SoapService tests.
 */
public class SoapServiceTestHelper
{
    static {
        WsTestHelper.loadBindings();
    }

    static Service createSoapService(String serviceMethodName) throws NoSuchMethodException, JiBXException, 
            WsException {

        ServiceDefinition sdef = getServiceDefinition(serviceMethodName);
        sdef.init();

        ServiceFactory serviceFactory = ProtocolDirectory.getProtocol(sdef.getProtocolName()).getServiceFactory();
        return ServicePool.getInstance(serviceFactory, sdef);
    }

    static Service createSoapServiceWithHandler(String handlerMethodName, String handlerId, Class handlerClass)
        throws NoSuchMethodException, JiBXException, WsException {

        ServiceDefinition sdef = getServiceDefinition(handlerMethodName);
        HandlerDefinition hdef = new HandlerDefinition();
        hdef.setClassName(handlerClass.getName());
        sdef.setHandlerDefinitions(Arrays.asList(new HandlerDefinition[] { hdef }));
        sdef.init();

        ServiceFactory serviceFactory = ProtocolDirectory.getProtocol(sdef.getProtocolName()).getServiceFactory();
        return ServicePool.getInstance(serviceFactory, sdef);
    }

    /**
     * Returns a stubbed service definition which includes a body handler that will invoke the method named
     * <code>serviceMethodName</code>.
     * 
     * @param serviceMethodName the name of the method that the body handler is to invoke (within this class)
     * @return service definition for testing
     * @throws NoSuchMethodException if the method doesn't exist
     * @throws JiBXException on any error accessing JiBX bindings or error in configuration
     * @throws WsConfigurationException
     */
    private static ServiceDefinition getServiceDefinition(String serviceMethodName) throws NoSuchMethodException,
        JiBXException, WsConfigurationException {
        OperationDefinition odef = new OperationDefinition();
        odef.setMethodName(serviceMethodName);
        odef.setInputClassName(Person.class.getName());
        odef.setOutputClassName(Customer.class.getName());

        ServiceDefinition sdef = new ServiceDefinition();
        // sdef.setBindingFactory(BindingDirectory.getFactory(Customer.class));
        sdef.setServiceClassName(SoapServiceTestHelper.class.getName());
        sdef.setOperationDefinitions(Arrays.asList(new OperationDefinition[] { odef }));
        sdef.setProtocolName("SOAP1.1");

        return sdef;
    }

    // ********************************************************************
    // Methods for processing requests. These are accessed via reflection.
    // ********************************************************************
    /**
     * Always returns {@link TestObjects#RESPONSE_OBJECT}.
     * 
     * @param p ignored
     * 
     * @return preset m_response
     */
    public static Customer findCustomer(Person p) {
        return (Customer) TestObjects.RESPONSE_OBJECT;
    }

    /**
     * Always throws {@link IllegalArgumentException}.
     * 
     * @param p ignored
     * 
     * @return never returns - always throws exception.
     */
    public static Customer throwIllegalArgumentException(Person p) {
        throw new IllegalArgumentException("Dummy IAE");
    }
}
