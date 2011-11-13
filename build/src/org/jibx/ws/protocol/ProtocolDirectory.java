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

package org.jibx.ws.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.jibx.ws.WsConfigurationException;


/**
 * Directory of protocols. For now this uses a property file to relate protocols with specific {@link Protocol} classes,
 * which are then loaded during class initialization and accessed as needed.
 *
 * @author Nigel Charman
 */
public final class ProtocolDirectory
{
    /** Properties file path. */
    private static final String PROTOCOL_PROPERTIES_PATH = "/protocol.properties";

    /** Separates class and field name in the properties file. */
    private static final char INSTANCE_SEPARATOR = '#';

    /** Map from protocol name to protocol class. */
    private static final Map s_protocolMap;

    static {
        s_protocolMap = new HashMap();

        loadProperties(PROTOCOL_PROPERTIES_PATH);
    }

    /** Prevent class from being instantiated. */
    private ProtocolDirectory() {
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
            stream = ProtocolDirectory.class.getResourceAsStream(path);
            if (stream == null) {
                throw new RuntimeException("Unable to load required properties file '" + path + '\'');
            }
            props.load(stream);

            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String objectName = props.getProperty(name);
                Protocol inst = loadProtocol(objectName);
                s_protocolMap.put(name, inst);
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
    }

    /**
     * Load the specified protocol class.
     *
     * @param objectName May be either the name of a class, or an instance of a class. For an instance, separate the
     * class name and instance name with a '#' character. For example,
     * <code>org.jibx.ws.soap.SoapProtocol#SOAP1_1</code>.
     * @return protocol object
     * @throws IllegalStateException if protocol object cannot be found or loaded
     */
    static Protocol loadProtocol(String objectName) {
        String classname;
        String fieldname = null;
        int index = objectName.indexOf(INSTANCE_SEPARATOR);
        if (index >= 0) {
            classname = objectName.substring(0, index);
            fieldname = objectName.substring(index + 1);
        } else {
            classname = objectName;
        }
        try {
            Class clas = ProtocolDirectory.class.getClassLoader().loadClass(classname);
            Object inst = null;
            if (fieldname != null) {
                inst = clas.getField(fieldname).get(null);
            } else {
                inst = clas.newInstance();
            }
            if (inst instanceof Protocol) {
                return (Protocol) inst;
            } else {
                throw new IllegalStateException("Class " + classname + ", specified for protocol '" + objectName
                    + "', is not an org.jibx.ws.Protocol implementation");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load protocol class " + classname);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Unable to access field " + fieldname + " of class " + classname);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Error creating an instance of protocol class " + classname + ": " 
                + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to create an instance of protocol class " + classname);
        }
    }

    /**
     * Return the protocol for the specified protocol name.
     *
     * @param protocolName the name of the protocol, for example <code>SOAP1_1</code>
     * @return protocol, or <code>null</code> if none found
     * @throws WsConfigurationException if protocolName is unknown
     */
    public static Protocol getProtocol(String protocolName) throws WsConfigurationException {
        Protocol protocol = (Protocol) s_protocolMap.get(protocolName);
        if (protocol == null) {
            throw new WsConfigurationException("Unknown protocol '" + protocolName);
        }
        return protocol;
    };
}
