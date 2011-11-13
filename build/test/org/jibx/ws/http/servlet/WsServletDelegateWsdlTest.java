/*
Copyright (c) 2009, Sosnoski Software Associates Limited. 
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
package org.jibx.ws.http.servlet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.wsdl.InputStreamWsdlProvider;
import org.jibx.ws.wsdl.WsdlLocationToRequestUrlAdapter;
import org.jibx.ws.wsdl.WsdlProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for WSDL generation through WsServletDelegate. 
 */
public class WsServletDelegateWsdlTest
{
    private static final String WSDL_FILE_PATH = "build/test/MyService.wsdl";
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private WsServletDelegate servlet;
    private ServiceDefinition sdef;
    private ServiceMapper mapper;
    private Service service;

    @Before
    public void setUp() throws Exception {
        request = new MockHttpServletRequest("GET", "http://localhost/myService");
        request.setQueryString("wsdl");
        response = new MockHttpServletResponse();
        servlet = new WsServletDelegate();
        service = mock(Service.class);
        mapper = mock(ServiceMapper.class);
        when(mapper.getServiceInstance(request)).thenReturn(service);
        doNothing().when(service).releaseInstance();
        servlet.setServiceMapper(mapper);
    }
    
    @Test
    public void givenNoWsdlProvider_whenWsdlIsRequested_shouldResultInHttp405() throws Exception {  
        servlet.doGet(null, request, response);
        assertThat(response.getStatus(), is(405));
    }

    @Test
    public void givenWsdlProvider_whenWsdlIsRequested_shouldRespondWithWsdl() throws Exception {
        File wsdlFile = new File(WSDL_FILE_PATH);
        assertThat("Test file '" + wsdlFile.getAbsolutePath() + "' exists", wsdlFile.exists(), is(true));
        
        InputStreamWsdlProvider wsdlProvider = new InputStreamWsdlProvider(FileUtils.openInputStream(wsdlFile));
        when(service.getWsdlProvider()).thenReturn(wsdlProvider);
        servlet.doGet(null, request, response);

        String wsdl = FileUtils.readFileToString(wsdlFile);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getContentAsString(), is(wsdl));
    }

    @Test
    public void givenWsdlAdapter_whenWsdlIsRequested_shouldRespondWithAdaptedWsdl() throws Exception {
        File wsdlFile = new File(WSDL_FILE_PATH);
        String initialWsdl = FileUtils.readFileToString(wsdlFile);
        initialWsdl = initialWsdl.replace("$serviceLocation$", "/example/service");
        InputStreamWsdlProvider wsdlProvider = new InputStreamWsdlProvider(new ByteArrayInputStream(initialWsdl.getBytes()));
        WsdlProvider adapter = new WsdlLocationToRequestUrlAdapter(wsdlProvider); 
        when(service.getWsdlProvider()).thenReturn(adapter);
        request.setScheme("http");
        request.setServerName("test.example.com");
        request.setServerPort(80);
        
        servlet.doGet(null, request, response);

        String expectedWsdl = FileUtils.readFileToString(wsdlFile);
        expectedWsdl = expectedWsdl.replace("$serviceLocation$", "http://test.example.com:80/example/service");
        assertThat(response.getStatus(), is(200));
        XMLAssert.assertXMLEqual(expectedWsdl, response.getContentAsString());
    }
}