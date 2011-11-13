/*
 * Copyright (c) 2008, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.http.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.protocol.ProtocolDirectory;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.server.ServicePool;

/**
 * Maps a servlet request to a {@link Service} instance, based on the servlet path. This attempts to match the request
 * to a {@link ServiceDefinition} defined in the init-params for this servlet. The request is matched using:
 * <ol>
 * <li>The extra path information between the servlet path and query string, or</li>
 * <li>If no extra path information present, the servlet path.</li>
 * </ol>
 * 
 * @author Dennis M. Sosnoski
 */
public final class PathBasedServiceMapper implements ServiceMapper
{
    private static final long serialVersionUID = -4161361963133295719L;

    private static final Log logger = LogFactory.getLog(PathBasedServiceMapper.class);

    /** Singleton map from context+servlet names to service definition maps. */
    private static HashMap s_servletMap = new HashMap();

    /** Map from path to {@link ServiceDefinition} for the services. */
    private Map m_serviceDefnMap;

    /**
     * Constructs the mapper. When the first instance of a servlet with a particular name within a particular context is
     * initialized it reads the service definitions associated with that name, which are then used to create the actual
     * service objects as needed to process received requests.
     * 
     * @param servlet the servlet to create the mapper for
     * @throws UnavailableException on any initialization error that causes the servlet to be unavailable
     */
    PathBasedServiceMapper(HttpServlet servlet) throws UnavailableException {
        // build key string from combination of context and servlet name
        String sname = servlet.getServletName();
        if (sname.equals(servlet.getClass().getName())) {
            sname = "";
        }
        String key = servlet.getServletContext().getServletContextName();
        if (key == null) {
            key = "";
        }
        key += "|" + sname;

        // check for existing service definition map
        synchronized (s_servletMap) {
            m_serviceDefnMap = (HashMap) s_servletMap.get(key);
            if (m_serviceDefnMap == null) {
                // need a new one, create it now with all service definitions
                m_serviceDefnMap = createServiceDefinitions(servlet);
                s_servletMap.put(key, m_serviceDefnMap);
            }
        }
    }

    /**
     * Initialize service configuration information. This uses the servlet initialization parameters as the list of
     * service definitions to be supported, reading and validating the actual service configurations from XML definition
     * files.
     * 
     * @return hash map from paths to service definitions
     * @param servlet the servlet to create the mapper for
     * @throws UnavailableException if any error occurs that stops the mapper from being created
     */
    private Map createServiceDefinitions(HttpServlet servlet) throws UnavailableException {

        // set up JiBX unmarshalling for service configuration files
        IUnmarshallingContext ctx = null;
        try {
            IBindingFactory factory = BindingDirectory.getFactory(ServiceDefinition.class);
            ctx = factory.createUnmarshallingContext();
        } catch (JiBXException e) {
            logger.error("Unable to initialize unmarshalling", e);
            throw new UnavailableException("Unable to initialize unmarshalling. \n" + getErrorDetails(e));
        }

        // read and validate service definitions
        String path = null;
        String file = null;
        try {

            // loop through all initialization parameter pairs
            HashMap map = new HashMap();
            Enumeration pnum = servlet.getInitParameterNames();
            ServletContext serv = servlet.getServletContext();
            while (pnum.hasMoreElements()) {

                // parameter name is path and value is service definition file
                path = (String) pnum.nextElement();
                file = "/WEB-INF/" + servlet.getInitParameter(path);
                InputStream is = null;
                try {
                    is = serv.getResourceAsStream(file);
                    if (is == null) {
                        logger.error("Service definition not found for service " + path + " at " + file);
                        throw new UnavailableException("Service definition not found for service " + path + " at "
                            + file + ". Check configuration of servlet " + servlet.getServletName()
                            + " in WEB-INF/web.xml.");
                    }
                    ServiceDefinition sdef = (ServiceDefinition) ctx.unmarshalDocument(is, null);
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    map.put(path, sdef);

                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }

            // return map with all services entered
            return map;

        } catch (JiBXException e) {
            logger.error("Error reading service definition " + file + " for service " + path, e);
            throw new UnavailableException("Error reading service definition " + file + " for service " + path + ".\n"
                + getErrorDetails(e));
        }
    }

    /**
     * Get service definition for servlet request. This attempts to match the request to a service definition defined in
     * the init-params for this servlet. The request is matched using:
     * <ol>
     * <li>The extra path information between the servlet path and query string, or</li>
     * <li>If no extra path information present, the servlet path.</li>
     * </ol>
     * 
     * @param req servlet request
     * @return definition, or <code>null</code> if no service definition found for path
     */
    private ServiceDefinition getServiceDefinition(HttpServletRequest req) {
        String servicePath = req.getPathInfo();
        if (servicePath == null) {
            servicePath = req.getServletPath();
        }
        ServiceDefinition defn = (ServiceDefinition) m_serviceDefnMap.get(servicePath);
        if (defn == null && logger.isWarnEnabled()) {
            logger.warn("No service definition for service path '" + servicePath + "' based on "
                + (req.getPathInfo() != null ? "path info" : "servlet path") + " of request '" + req.getRequestURI()
                + "'");
        }
        return defn;
    }

    /** {@inheritDoc} */
    public Service getServiceInstance(HttpServletRequest req) throws WsException {
        ServiceDefinition sdef = getServiceDefinition(req);
        if (sdef == null) {
            return null;
        }
        ServiceFactory serviceFactory = ProtocolDirectory.getProtocol(sdef.getProtocolName()).getServiceFactory();
        return ServicePool.getInstance(serviceFactory, sdef);
    }

    /**
     * Creates a string containing the exception message and the message of the root cause, if one exists.
     * 
     * @param e exception
     * @return error details string
     */
    private String getErrorDetails(JiBXException e) {
        String errorDetails = "Error details: " + e.getMessage();
        if (e.getRootCause() != null) {
            errorDetails += "\nRoot cause: " + e.getRootCause().getMessage();
            logger.error("Root cause: ", e.getRootCause());
        }
        return errorDetails;
    }
}
