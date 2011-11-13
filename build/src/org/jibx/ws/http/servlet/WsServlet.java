/*
 * Copyright (c) 2007-2008, Sosnoski Software Associates Limited. All rights reserved.
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

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * JiBX/WS web service request handler servlet that is configured using JiBX. The configuration information for this
 * servlet is obtained from one or more service definition files located with the WEB-INF directory of the web
 * application. The particular service definition files handled by an instance of this servlet are configured as
 * initialization parameters for the servlet. If the servlet is invoked with any path information in the request the
 * path information is used to identify the particular service being requested. 
 * <!--As a special case, the request parameter
 * "?WSDL" is recognized as a request for the WSDL service description.-->
 * 
 * @author Dennis M. Sosnoski
 */
public final class WsServlet extends HttpServlet
{
    private WsServletDelegate m_delegate = new WsServletDelegate();

    /**
     * Get servlet description.
     * 
     * @return description string
     */
    public String getServletInfo() {
        return "JiBX/WS service servlet";
    }

    /**
     * Initialize servlet. When the first instance of a servlet with a particular name within a particular context is
     * initialized it reads the service definitions associated with that name, which are then used to create the actual
     * services as needed to process received requests.
     * 
     * @throws UnavailableException on any initialization error that causes the servlet to be unavailable
     */
    public void init() throws UnavailableException {
        m_delegate.setServiceMapper(new PathBasedServiceMapper(this));
    }

    /**
     * POST request handler. This processes the incoming request message and generates the response.
     * 
     * @param req servlet request information
     * @param rsp servlet response information
     * @exception ServletException on message content or operational error
     * @exception IOException on error reading or writing
     */
    public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        m_delegate.doPost(req, rsp);
    }

    /**
     * GET request handler. The only type of GET request supported is one to get the WSDL for a service.
     * 
     * @param req servlet request information
     * @param rsp servlet response information
     * @exception ServletException on message content or operational error
     * @exception IOException on error reading or writing
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        m_delegate.doGet(getServletContext(), req, rsp);
    }

    /** {@inheritDoc} */
    protected void doDelete(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        rsp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** {@inheritDoc} */
    protected void doHead(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        rsp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** {@inheritDoc} */
    protected void doOptions(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        rsp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** {@inheritDoc} */
    protected void doPut(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        rsp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
