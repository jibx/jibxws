/*
Copyright (c) 2007, Sosnoski Software Associates Limited. 
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

package org.jibx.ws;

import org.jibx.ws.transport.TestTransportLoader;
import org.jibx.ws.transport.TransportDirectory;


/**
 * A helper class for JiBX/WS test cases.
 */
public final class WsTestHelper 
{
    private static boolean s_transportLoaded = false;
	
    // Prevent utility class from being constructed
    private WsTestHelper() {
    }
    
    /**
     * Loads the binding definitions for the JiBX/WS test cases. This ensures that the test cases will run even if 
     * they the binding compiler has not been run.
     * <p>
     * This will slow down the test cases slightly.  If you do not want the binding definitions to be loaded, set
     * the system property <code>org.jibx.ws.test.loadBindings</code> to false.
     */
    public static void loadBindings() {
        String loadBindings = System.getProperty("org.jibx.ws.test.loadBindings", "true");
        if (!loadBindings.equalsIgnoreCase("false")) {
            JiBXClassBinder.loadBinding(new String[] {
                "org/jibx/ws/soap/testdata/basic/test-error-binding.xml",
                "org/jibx/ws/soap/testdata/basic/test1-binding.xml",
                "org/jibx/ws/soap/testdata/header/test2-binding.xml",
                "service-mapping.xml"
            });
        }
    }
    
    /** 
     * Loads the test transports into the {@link TransportDirectory}.
     */
    public static void loadTestTransport() {
    	if (!s_transportLoaded) {
    		TestTransportLoader.init();
    	}
    	s_transportLoaded = true;
    }
}
