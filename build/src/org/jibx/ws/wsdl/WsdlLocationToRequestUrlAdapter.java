/*
 * Copyright (c) 2009, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.wsdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jibx.ws.WsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Modifies the location address of WSDL services based on the URL of the incoming servlet request.
 * <p>
 * The content of the <code>location</code> attribute of each <code>&lt;soap:address&gt;</code> element in the provided
 * WSDL is modified as follows:
 * <ul>
 * <li>if the location is a "partial" URL that starts with a <code>/</code>, it is appended to the base request URL</li>
 * <li>if the location is a "full" URL starts with a <code>/</code>, it is modified to start with the base request URL</li>
 * <li>otherwise the location is left as-is</li>
 * </ul>
 * , where "base request URL" means the <code>&lt;scheme>://&lt;server>:&lt;port></code> part of the request URL, for
 * example <code>http://localhost:8080</code>.
 *
 * @author Nigel Charman
 */
public class WsdlLocationToRequestUrlAdapter implements WsdlProvider
{
    private static final String WSDLSOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    private final WsdlProvider m_wsdlProvider;
    private HashMap m_wsdlMap = new HashMap();  // from location base to adapted WSDL

    /**
     * Constructor.
     * 
     * @param wsdlProvider supplies the WSDL to be adapted
     */
    public WsdlLocationToRequestUrlAdapter(WsdlProvider wsdlProvider) {
        m_wsdlProvider = wsdlProvider;
    }

    /**
     * {@inheritDoc} Writes the adapted WSDL.
     */
    public void writeWSDL(OutputStream outputStream, HttpServletRequest req) throws IOException, WsException {
        outputStream.write(getAdaptedWsdl(req));
    }
    
    /**
     * {@inheritDoc} Returns the adapted WSDL.
     */
    public InputStream getWSDL(HttpServletRequest req) throws IOException, WsException {
        return new ByteArrayInputStream(getAdaptedWsdl(req));
    }

    /**
     * Returns the base URL (that is, <code>&lt;scheme>://&lt;server>:&lt;port></code>)
     *
     * @param req request to derive base URL from 
     * @return the base URL
     */
    private String getLocationBase(HttpServletRequest req) {
        StringBuffer locationBaseBuf = new StringBuffer(64);
        locationBaseBuf.append(req.getScheme()).append("://").append(req.getServerName()).append(":").append(
            req.getServerPort());
        String locationBase = locationBaseBuf.toString();
        return locationBase;
    }

    /**
     * Returns the WSDL with the location adapted to start with the <code>locationBase</code> of the given 
     * request. It is assumed that the underlying WSDL provider returns the same WSDL for each request that has the
     * same <code>locationBase</code>, and the adapted WSDL is cached.
     *
     * @param req request for WSDL 
     * @return adapted WSDL
     * @throws IOException on I/O error with WSDL
     * @throws WsException on error with WSDL content
     */
    private synchronized byte[] getAdaptedWsdl(HttpServletRequest req) throws IOException, WsException {
        String locationBase = getLocationBase(req);
        
        byte[] wsdl = (byte[]) m_wsdlMap.get(locationBase);
        if (wsdl == null) {
            wsdl = adaptWsdl(req, locationBase);
            m_wsdlMap.put(locationBase, wsdl);
        }
        return wsdl;
    }

    /* Not thread safe. Calling method must ensure all calls to this method are synchronized. */
    private byte[] adaptWsdl(HttpServletRequest req, String locationBase) throws IOException, WsException {
        Document doc;
        try {
            // read the schema to DOM representation
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            DocumentBuilder bldr = fact.newDocumentBuilder();
            doc = bldr.parse(m_wsdlProvider.getWSDL(req));
            Element schema = doc.getDocumentElement();
            NodeList services = schema.getElementsByTagNameNS(WSDLSOAP_NAMESPACE, "address");
            for (int i = 0; i < services.getLength(); i++) {
                Node node = services.item(i).getAttributes().getNamedItem("location");
                if (node != null) {
                    String location = node.getTextContent();
                    node.setTextContent(adaptLocation(location, locationBase));
                }
            }
        } catch (ParserConfigurationException e) {
            throw new WsException("Unable to configure parser for WSDL adapter: " + e.getMessage());
        } catch (SAXException e) {
            throw new WsException("Error parsing supplied WSDL: " + e.getMessage());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(baos);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new WsException("Error transforming WSDL: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    /**
     * Adapts the location of the service.
     * 
     * @param initialLocation the service location in the supplied WSDL
     * @param locationBase the base of the request URI
     *
     * @return adapted location
     */
    private String adaptLocation(String initialLocation, String locationBase) {
        String location = initialLocation;
        if (initialLocation.startsWith("/")) {
            location = locationBase + initialLocation;
        } else {
            int idx = initialLocation.indexOf("://");
            if (idx != -1) {
                idx = initialLocation.indexOf("/", idx + 3);
                if (idx != -1) {
                    location = locationBase + initialLocation.substring(idx);
                }
            }
        }
        return location;
    }
}
