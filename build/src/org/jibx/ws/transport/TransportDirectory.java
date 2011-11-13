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

package org.jibx.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Directory of client transports. For now this uses a property file to relate URL protocols with specific {@link
 * Transport} classes, which are then loaded during class initialization and accessed as needed to establish
 * connections.
 * 
 * @author Dennis M. Sosnoski
 */
public final class TransportDirectory
{
    /** Properties file path. */
    private static final String TRANSPORT_PROPERTIES_PATH = "/transport.properties";

    /** Map from protocol to transport connector class. */
    private static final Map s_protocolMap;

    static {
        s_protocolMap = new HashMap();

        loadProperties(TRANSPORT_PROPERTIES_PATH);
    }

    /** Hide constructor for utility class. */
    private TransportDirectory() {
    }
    
    /**
     * Load protocols from a properties file.
     * 
     * @param path path to the properties file relative to the classpath
     */
    static void loadProperties(String path) {
        InputStream stream = null;
        try {
            Properties props = new Properties();
            stream = TransportDirectory.class.getResourceAsStream(path);
            if (stream == null) {
                throw new RuntimeException("Unable to load required properties file '" + path + '\'');
            }
            props.load(stream);

            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String protocol = (String) iter.next();
                String classname = props.getProperty(protocol);
                try {
                    Class clas = TransportDirectory.class.getClassLoader().loadClass(classname);
                    Object inst = clas.newInstance();
                    if (inst instanceof Transport) {
                        s_protocolMap.put(protocol.toLowerCase(), inst);
                    } else {
                        throw new IllegalStateException("Class " + classname + ", specified for protocol '" + protocol
                            + "', is not an org.jibx.ws.transport.Transport implementation");
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Unable to load transport class " + classname);
                } catch (InstantiationException e) {
                    throw new IllegalStateException("Error creating an instance of transport class " + classname + ": "
                        + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to create an instance of transport class " + classname);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load required properties file '" + path + '\'');
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
    };

    /**
     * Get transport used to communicate with the target endpoint. The endpoint must be in URL format, with the
     * "protocol" portion of the URL used to select the transport. It does not need to be a valid URL in terms of the
     * standard <code>java.net.URL</code> class.
     * 
     * @param endpoint the target endpoint
     * @return transport
     * @throws IllegalArgumentException if no protocol implementation available for endpoint
     */
    public static Transport getTransport(String endpoint) {
        Transport transport = null;
        int split = endpoint.indexOf(':');
        if (split > 0) {
            String protocol = endpoint.substring(0, split);
            transport = (Transport) s_protocolMap.get(protocol.toLowerCase());
            if (transport == null) {
                throw new IllegalArgumentException("No transport defined for protocol '" + protocol + "' of endpoint '"
                    + endpoint + "'");
            }
        } else {
            throw new IllegalArgumentException("Unrecognized endpoint '" + endpoint + "' - missing required protocol");
        }
        return transport;
    }

    /**
     * Get transport settings for customizing the target endpoint. The endpoint must be in URL format, with the
     * "protocol" portion of the URL used to select the transport. It does not need to be a valid URL in terms of the
     * standard <code>java.net.URL</code> class.
     * 
     * @param endpoint the target endpoint
     * @return default transport settings
     * @throws IllegalArgumentException if no protocol implementation available for endpoint
     */
    public static TransportOptions newTransportOptions(String endpoint) {
        return getTransport(endpoint).newTransportOptions();
    }

    // TODO: add separate inbound-only and outbound-only calls, and separate response endpoint variation?
}
