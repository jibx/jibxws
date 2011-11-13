/*
 * Copyright (c) 2008, Sosnoski Software Associates Limited All rights reserved.
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
package org.jibx.ws.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsTestHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link ServiceDefinition}.
 */

public class ServiceDefinitionTest
{
    private static IBindingFactory m_factory;
    private static IUnmarshallingContext m_ctx;

    static {
        WsTestHelper.loadBindings();
    }

    @BeforeClass
    public static void init() throws Exception {
        m_factory = BindingDirectory.getFactory(ServiceDefinition.class);
        m_ctx = m_factory.createUnmarshallingContext();
    }
    
    @Test
    public final void givenValidServiceAndOperationMethodShouldReturnServiceClassAndMethodName() throws Exception {
        StringReader sdxml = new StringReader("<service name=\"jibxws.sdtest\">"
            + "<service-class>java.lang.Integer</service-class>"
            + "<operation method=\"toString\"/>" + "</service>");
        ServiceDefinition sdef = (ServiceDefinition) m_ctx.unmarshalDocument(sdxml, null);
//        assertThat(sdef.getServiceName(), is("jibxws.sdtestService"));
        assertThat(sdef.getServiceClass().getName(), is(java.lang.Integer.class.getName()));
        assertThat(sdef.getOperationDefinitions().size(), is(1));
        OperationDefinition opdef = (OperationDefinition) sdef.getOperationDefinitions().get(0);
        assertThat(opdef.getMethodName(), is("toString"));
    }

    @Test
    public final void givenJibxBindingShouldReturnBindingFactoryForBothInBodyAndOutBody() throws Exception {
        StringReader sdxml = new StringReader("<service>"
            + "<service-class>java.lang.Integer</service-class>"
            + "<jibx-binding binding-name=\"test1_binding\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "</service>");
        ServiceDefinition sdef = (ServiceDefinition) m_ctx.unmarshalDocument(sdxml, null);
        assertThat(sdef.getInBodyBindingFactory().getBindingName(), is("test1_binding"));
        assertThat(sdef.getOutBodyBindingFactory().getBindingName(), is("test1_binding"));
    }

    @Test
    public final void givenJibxInBindingShouldReturnBindingFactoryForOnlyInBody() throws Exception {
        StringReader sdxml = new StringReader("<service>"
            + "<service-class>java.lang.Integer</service-class>"
            + "<jibx-in-binding binding-name=\"test1_binding\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "</service>");
        ServiceDefinition sdef = (ServiceDefinition) m_ctx.unmarshalDocument(sdxml, null);
        assertThat(sdef.getInBodyBindingFactory().getBindingName(), is("test1_binding"));
        assertThat(sdef.getOutBodyBindingFactory(), is(nullValue()));
    }

    @Test
    public final void givenJibxOutBindingShouldReturnBindingFactoryForOnlyOutBody() throws Exception {
        StringReader sdxml = new StringReader("<service>"
            + "<service-class>java.lang.Integer</service-class>"
            + "<jibx-out-binding binding-name=\"test1_binding\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "</service>");
        ServiceDefinition sdef = (ServiceDefinition) m_ctx.unmarshalDocument(sdxml, null);
        assertThat(sdef.getInBodyBindingFactory(), is(nullValue()));
        assertThat(sdef.getOutBodyBindingFactory().getBindingName(), is("test1_binding"));
    }

    @Test
    public final void givenDifferentJibxInAndOutBindingsShouldReturnCorrectBindingFactoryForInAndOutBody() throws Exception {
        StringReader sdxml = new StringReader("<service>"
            + "<service-class>java.lang.Integer</service-class>"
            + "<jibx-in-binding binding-name=\"test_error_binding\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "<jibx-out-binding binding-name=\"test1_binding\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "</service>");
        ServiceDefinition sdef = (ServiceDefinition) m_ctx.unmarshalDocument(sdxml, null);
        assertThat(sdef.getInBodyBindingFactory().getBindingName(), is("test_error_binding"));
        assertThat(sdef.getOutBodyBindingFactory().getBindingName(), is("test1_binding"));
    }

    @Test
    public final void givenNonExistentBindingNameShouldThrowJiBXException() throws Exception {
        StringReader sdxml = new StringReader("<service>"
            + "<service-class>java.lang.Integer</service-class>"
            + "<jibx-binding binding-name=\"non_existent\" package-name=\"org.jibx.ws.soap.testdata.basic\" />"
            + "</service>");
        try {
            m_ctx.unmarshalDocument(sdxml, null);
            Assert.fail("Expected JiBXException");
        } catch(JiBXException e) {
            assertThat(e.getRootCause().getClass().getName(), is(WsBindingException.class.getName()));
        }
    }
}
