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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jibx.ws.server.Service;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * JiBX/WS web service request handler servlet that is configured using the <a
 * href="http://www.springframework.org">Spring Framework</a>.
 * <p>
 * This servlet expects a {@link ServiceMapper} object to be available with the bean name defined by
 * {@link #DEFAULT_SERVICE_MAPPER_BEAN_NAME}. This bean name can be overridden using the method
 * {@link #setServiceMapperBeanName(String)}. The <code>ServiceMapper</code> class maps the servlet request to a
 * {@link Service} object.
 * <p>
 * As a special case, the request parameter "?WSDL" is recognized as a request for the WSDL service description.
 * 
 * @author Dennis M. Sosnoski
 */
public class WsSpringServlet extends FrameworkServlet
{
    private WsServletDelegate m_delegate = new WsServletDelegate();

    /** Well-known name for the {@link ServiceMapper} bean in the bean factory for this namespace. */
    public static final String DEFAULT_SERVICE_MAPPER_BEAN_NAME = "serviceMapper";
    private String m_serviceMapperBeanName = DEFAULT_SERVICE_MAPPER_BEAN_NAME;

    /**
     * Returns the bean name used to lookup a {@link ServiceMapper}.
     * 
     * @return bean name
     */
    public String getServiceMapperBeanName() {
        return m_serviceMapperBeanName;
    }

    /**
     * Sets the bean name used to lookup a {@link ServiceMapper}. Defaults to {@link #DEFAULT_SERVICE_MAPPER_BEAN_NAME}.
     * 
     * @param serviceMapperBeanName the new name for the service mapper bean
     */
    public void setServiceMapperBeanName(String serviceMapperBeanName) {
        this.m_serviceMapperBeanName = serviceMapperBeanName;
    }

    /** {@inheritDoc} */
    protected void onRefresh(ApplicationContext context) throws BeansException {
        ServiceMapper serviceMapper = (ServiceMapper) context.getBean(getServiceMapperBeanName(), ServiceMapper.class);
        m_delegate.setServiceMapper(serviceMapper);
    }

    /** {@inheritDoc} */
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String method = request.getMethod();
        if ("POST".equals(method)) {
            m_delegate.doPost(request, response);
        } else if ("GET".equals(method)) {
            m_delegate.doGet(getServletContext(), request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
}
