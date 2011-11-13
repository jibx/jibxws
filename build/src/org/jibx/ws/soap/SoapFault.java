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

package org.jibx.ws.soap;

import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.QName;
import org.jibx.ws.WsException;
import org.jibx.ws.io.handler.OutHandler;

/**
 * SOAP fault object class. 
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public final class SoapFault
{
    /** The standard SOAP Client fault code. */
    public static final QName FAULT_CODE_CLIENT = new QName(SoapConstants.SOAP_URI, "Client");
    
    /** The standard SOAP Server fault code. */
    public static final QName FAULT_CODE_SERVER = new QName(SoapConstants.SOAP_URI, "Server");

    /** The standard SOAP MustUnderstand fault code. */
    public static final QName FAULT_CODE_MUST_UNDERSTAND = new QName(SoapConstants.SOAP_URI, "MustUnderstand");

    /** The standard SOAP VersionMismatch fault code. */
    public static final QName FAULT_CODE_VERSION_MISMATCH = new QName(SoapConstants.SOAP_URI, "VersionMismatch");
    
    /** Code for type of fault. */
    private final QName m_faultCode;
    
    /** Readable description of fault condition. */
    private final String m_faultString;
    
    /** URI for fault origination (optional, may be <code>null</code>). */
    private final String m_faultActor;
    
    /** A list of detail items. Note this is only intended for incoming faults.  Outgoing faults must set detail
     *  writers in order to have the fault written.
     */
    private final List m_details = new ArrayList();
    
    /** A list of {@link OutHandler}s, which will be called sequentially to write the SOAP Fault details. */
    private final List m_detailWriters = new ArrayList();

    /**
     * @param faultCode Code for type of fault
     * @param faultString Readable description of fault condition
     * @param faultActor URI for fault origination (optional, may be <code>null</code>).
     * @throws WsException if SOAP Fault details violate the SOAP specification
     */
    public SoapFault(QName faultCode, String faultString, String faultActor) throws WsException {
        if (faultCode == null || faultString == null) {
            throw new WsException("faultCode and faultString must be non-null");
        }
        if (faultCode.getUri() == null) {
            throw new WsException("faultCode URI must be non-null");
        }
        if (!faultCode.getUri().equals(SoapConstants.SOAP_URI)) {
            if (faultCode.getPrefix() == null) {
                throw new WsException("faultCode prefix must be non-null for custom URIs");
            }
        }
        m_faultCode = faultCode;
        m_faultString = faultString;
        m_faultActor = faultActor;
    }

    /**
     * Add detail item for a body-related fault condition. This method is only intended for unmarshalling code.  Code
     * that is creating a fault for marshalling must use the {@link #addDetailWriter(OutHandler)} method. 
     *
     * @param item the item to add
     */
    void addDetail(Object item) {
        m_details.add(item);
    }
    
    /**
     * Add writer for detail item for a body-related fault condition.
     *
     * @param writer the handler that will write the detail item
     */
    public void addDetailWriter(OutHandler writer) {
        m_detailWriters.add(writer);
    }

    /**
     * Get faultCode.
     *
     * @return faultCode
     */
    public QName getFaultCode() {
        return m_faultCode;
    }

    /**
     * Get faultString.
     *
     * @return faultString
     */
    public String getFaultString() {
        return m_faultString;
    }

    /**
     * Get faultActor.
     *
     * @return faultActor
     */
    public String getFaultActor() {
        return m_faultActor;
    }

    /**
     * Get details.
     *
     * @return details
     */
    public List getDetails() {
        return m_details;
    }
    
    /**
     * Get the list of {@link OutHandler}s, which will be called sequentially to write the SOAP Fault details. 
     *
     * @return detail writers
     */
    public List getDetailWriters() {
        return m_detailWriters;
    }

    /**
     *  {@inheritDoc}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getFaultCode().getPrefix() != null) {
            sb.append(getFaultCode().getPrefix()).append(":");
        } else if (SoapConstants.SOAP_URI.equals(getFaultCode().getUri())) {
            sb.append(SoapConstants.SOAP_PREFIX).append(":");
        } else if (getFaultCode().getUri() != null) {
            sb.append('{').append(getFaultCode().getUri()).append('}');
        }
        if (getFaultCode().getName() != null) {
            sb.append(getFaultCode().getName());
        }
        sb.append(" - ").append(getFaultString());
        
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_details == null) ? 0 : m_details.hashCode());
        result = prime * result + ((m_faultActor == null) ? 0 : m_faultActor.hashCode());
        result = prime * result + ((m_faultCode == null) ? 0 : m_faultCode.hashCode());
        result = prime * result + ((m_faultString == null) ? 0 : m_faultString.hashCode());
        return result;
    }

    /**
     * Compares two objects for equality.
     * SoapFaults are considered equals if the faultCode, faultString, faultActor and details match. 
     * DetailWriters are ignored in the comparison.
     *
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SoapFault other = (SoapFault) obj;
        if (m_details == null) {
            if (other.m_details != null) {
                return false;
            }
        } else if (!m_details.equals(other.m_details)) {
            return false;
        }
        if (m_faultActor == null) {
            if (other.m_faultActor != null) {
                return false;
            }
        } else if (!m_faultActor.equals(other.m_faultActor)) {
            return false;
        }
        if (m_faultCode == null) {
            if (other.m_faultCode != null) {
                return false;
            }
        } else if (!m_faultCode.equals(other.m_faultCode)) {
            return false;
        }
        if (m_faultString == null) {
            if (other.m_faultString != null) {
                return false;
            }
        } else if (!m_faultString.equals(other.m_faultString)) {
            return false;
        }
        return true;
    }
    
    
}
