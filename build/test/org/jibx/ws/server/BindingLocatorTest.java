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
import static org.junit.Assert.assertThat;

import org.jibx.runtime.IBindingFactory;
import org.jibx.ws.WsBindingException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsTestHelper;
import org.junit.Test;

/**
 * Tests that the {@link BindingLocator} locates {@link IBindingFactory}s correctly. To do this, it needs the test
 * bindings to be compiled. If not already compiled, the call to WsTestHelper in the static initializer will attempt to
 * load them.
 * 
 * @author Nigel Charman
 */
public class BindingLocatorTest
{
    static {
        WsTestHelper.loadBindings();
    }

    @Test
    public void whenPackageAndBindingNameSpecifiedBindingLocatorShouldReturnBinding() throws Exception {
        BindingLocator locator = new BindingLocator();
        locator.setPackageName("org.jibx.ws.soap.testdata.basic");
        locator.setBindingName("test1_binding");
        assertThat(locator.getBindingFactory().getBindingName(), is("test1_binding"));
    }

    @Test(expected = WsConfigurationException.class)
    public void whenPackageNameNotSpecifiedBindingLocatorShouldThrowWsConfigurationException() throws Exception {
        BindingLocator locator = new BindingLocator();
        locator.setBindingName("test1_binding");
        locator.getBindingFactory();
    }

    @Test(expected = WsConfigurationException.class)
    public void whenBindingNameNotSpecifiedBindingLocatorShouldThrowWsConfigurationException() throws Exception {
        BindingLocator locator = new BindingLocator();
        locator.setPackageName("org.jibx.ws.soap.testdata.basic");
        locator.getBindingFactory();
    }

    @Test(expected = WsBindingException.class)
    public void whenNonExistentBindingNameSpecifiedBindingLocatorShouldThrowWsBindingException() throws Exception {
        BindingLocator locator = new BindingLocator();
        locator.setPackageName("org.jibx.ws.soap.testdata.basic");
        locator.setBindingName("non_existent");
        locator.getBindingFactory();
    }
}
