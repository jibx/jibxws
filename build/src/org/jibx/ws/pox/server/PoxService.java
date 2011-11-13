/*
 * Copyright (c) 2007-2009, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.pox.server;

import org.jibx.ws.WsException;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.process.Processor;
import org.jibx.ws.server.MediaTypeMapper;
import org.jibx.ws.server.Service;
import org.jibx.ws.server.ServiceDefinition;
import org.jibx.ws.server.ServiceExceptionHandlerFactory;
import org.jibx.ws.wsdl.WsdlProvider;

/**
 * Service implementation for a web service using POX. 
 * 
 * @author Nigel Charman
 */
public final class PoxService extends Service 
{
    /**
     * Create service from definition.
     * 
     * @param sdef service definition information
     * @param processor for processing the message
     * @param mediaTypeMapper to map media type code to media type
     * @param defaultExceptionHandlerFactory for creating exception handler if none defined in service definition
     * @throws WsException on error creating the service
     */
    public PoxService(ServiceDefinition sdef, Processor processor, MediaTypeMapper mediaTypeMapper, 
            ServiceExceptionHandlerFactory defaultExceptionHandlerFactory) throws WsException {
        
        super(sdef, processor, mediaTypeMapper, defaultExceptionHandlerFactory);

        processor.setExchangeContext(createExchangeContext());
    }

    /**
     * Creates an ExchangeContext instance from the supplied ServiceDefinition.
     * 
     * @return exchange context based on the service definition
     * @throws WsException on error creating exchange context, for example handler object cannot be created
     */
    private ExchangeContext createExchangeContext() throws WsException {
        InContext inCtx = new InContext();
        OutContext outCtx = new OutContext();

        createBodyHandlers(inCtx, outCtx);
        setContextOnTransportOptions(inCtx, outCtx);

        return ExchangeContext.createInOutExchange(inCtx, outCtx);
    }

    /**
     * Not supported for PoxService.
     * {@inheritDoc}
     */
    public void setWsdlProvider(WsdlProvider wsdlProvider) {
        throw new UnsupportedOperationException("WSDL not supported for POX protocoll");
    }

    /**
     * Not supported for PoxService.
     * {@inheritDoc}
     */
    public WsdlProvider getWsdlProvider() {
        throw new UnsupportedOperationException("WSDL not supported for POX protocoll");
    }
}

