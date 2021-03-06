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

package org.jibx.ws.example.custom.exception.server;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.ws.WsException;
import org.jibx.ws.example.custom.exception.common.ServiceError;
import org.jibx.ws.process.Processor;
import org.jibx.ws.server.ServiceExceptionHandler;
import org.jibx.ws.transport.OutServerConnection;

/**
 * Sends a custom error message.
 * 
 * @author Nigel Charman
 */
public class ExceptionHandler implements ServiceExceptionHandler
{
    private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

    /**
     * Constructor.
     */
    public ExceptionHandler() {
    }

    /**
     * Handle an error that occurred during processing.
     * 
     * @param t the error
     * @param processor the current processor
     * @param outConn the outbound connection
     */
    public void handleException(Throwable t, Processor processor, OutServerConnection outConn) {
        logger.error("Error processing request", t);

        try {
            processor.getCurrentMessageContext().setBody(new ServiceError(t.getClass().getName(), t.getMessage()));
            processor.sendMessage(outConn);
        } catch (IOException e) {
            logger.error(e);
        } catch (WsException e) {
            logger.error(e);
        }
    }
}
