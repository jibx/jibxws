/*
Copyright (c) 2009, Sosnoski Software Associates Limited. 
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

package org.jibx.ws.soap.server;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.ws.io.handler.ExceptionWriter;
import org.jibx.ws.process.Processor;
import org.jibx.ws.server.ServiceExceptionHandler;
import org.jibx.ws.soap.SoapFault;
import org.jibx.ws.soap.SoapFaultException;
import org.jibx.ws.soap.SoapProcessor;
import org.jibx.ws.soap.WsNotUnderstoodException;
import org.jibx.ws.transport.OutServerConnection;

/**
 * Sends a SOAP Fault message.
 * 
 * @author Nigel Charman
 */
public final class SoapFaultHandler implements ServiceExceptionHandler
{
    private static final Log logger = LogFactory.getLog(SoapFaultHandler.class);
    private boolean m_includeStackTrace;

    /**
     * Constructor.
     * 
     * @param includeStackTrace <code>true</code> to include exception stack trace in fault
     */
    public SoapFaultHandler(boolean includeStackTrace) {
        m_includeStackTrace = includeStackTrace;
    }


    /**
     * Handle an error that occurred during processing.
     * @param e the error
     * @param processor the current processor
     * @param outConn the outbound connection
     */
    public void handleException(Throwable e, Processor processor, OutServerConnection outConn) {
        // generate fault response with error information
        logger.warn("Error processing request. Generating SOAP Fault response.", e);
    
        outConn.setInternalServerError();   
        try {
            if (e instanceof InvocationTargetException) {
                Throwable wrapped = ((InvocationTargetException) e).getTargetException();
                if (wrapped != null) {
                    e = wrapped;
                }
            }
    
            SoapProcessor soapProcessor = ((SoapProcessor) processor);
            SoapFault fault;
            if (e instanceof SoapFaultException) {
                fault = ((SoapFaultException) e).getFault();
            } else if (e instanceof WsNotUnderstoodException) {
                fault = new SoapFault(SoapFault.FAULT_CODE_MUST_UNDERSTAND, e.getMessage(), null);
            } else {
                fault = new SoapFault(SoapFault.FAULT_CODE_SERVER, e.getMessage(), null);
                if (m_includeStackTrace) {
                    fault.addDetailWriter(new ExceptionWriter(e, m_includeStackTrace));
                }
            }
            soapProcessor.sendFaultMessage(fault, outConn);
        } catch (Throwable ex) {
            logger.error("Error while processing prior error", ex);
        }
    }

}
