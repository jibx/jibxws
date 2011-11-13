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

import javax.servlet.http.HttpServletRequest;

import org.jibx.ws.WsException;
import org.jibx.ws.protocol.ProtocolDirectory;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceFactory;
import org.jibx.ws.server.ServicePool;

/**
 * Maps all requests to a single service.
 * 
 * @author Nigel Charman
 */
public final class SingleServiceMapper implements ServiceMapper
{
    private static final long serialVersionUID = -8754981981832028644L;
    
    private ServiceDefinition m_sdef;

    /** {@inheritDoc} */
    public Service getServiceInstance(HttpServletRequest req) throws WsException {
        ServiceFactory serviceFactory = ProtocolDirectory.getProtocol(m_sdef.getProtocolName()).getServiceFactory();
        return ServicePool.getInstance(serviceFactory, m_sdef);
    }

    /**
     * Gets the service definition for the service.
     * 
     * @return service definition
     */
    public ServiceDefinition getServiceDefinition() {
        return m_sdef;
    }

    /**
     * Sets the service definition for the service.
     * 
     * @param sdef service definition
     */
    public void setServiceDefinition(ServiceDefinition sdef) {
        m_sdef = sdef;
    }
}
