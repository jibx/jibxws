/*
Copyright (c) 2007, Sosnoski Software Associates Limited. 
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

package org.jibx.ws.soap;

/**
 * An exception that can indicate that a {@link SoapFault} has occurred.  
 * The message of the exception will contain the faultstring from the SOAP fault.
 * 
 * @author Nigel Charman
 */
public class SoapFaultException extends RuntimeException
{
    private static final long serialVersionUID = -3993930349974183605L;
    
    private SoapFault m_fault;

    /**
     * Create the exception based on the specified fault.
     * 
     *  @param fault the SoapFault that caused this exception to be created.
     */
    public SoapFaultException(SoapFault fault) {
        super(fault.getFaultString());
        m_fault = fault;
    }


    /**
     * Return the {@link SoapFault} that caused this exception to be created.
     *
     * @return fault
     */
    public SoapFault getFault() {
        return m_fault;
    }
    
    /** {@inheritDoc} */
    public String getMessage() {
        return getFault() == null ? null : getFault().getFaultString();
    }
}
