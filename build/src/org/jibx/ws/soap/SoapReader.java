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

package org.jibx.ws.soap;

import java.io.IOException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.ws.WsException;
import org.jibx.ws.io.XmlReaderWrapper;
import org.jibx.ws.transport.InConnection;

/**
 * SOAP reader class. This handles all SOAP message decoding.
 *
 * @author Nigel Charman
 */
public final class SoapReader
{
    private XmlReaderWrapper m_wrapper;
    private IXMLReader m_reader;
    private InConnection m_conn;

    /**
     * Constructor to read from the specified connection.
     * @param conn the connection to read the SOAP message from
     * @throws IOException on error creating a XML reader from the connection
     * @throws WsException on configuration error
     */
    SoapReader(InConnection conn) throws IOException, WsException {
        m_conn = conn;
        m_conn.init();
        m_reader = conn.getReader();
        m_wrapper = XmlReaderWrapper.createXmlReaderWrapper(m_reader);
    }

    /**
     * Get the XML reader instance.
     *
     * @return reader
     */
    public IXMLReader getReader() {
        return m_reader;
    }

    /**
     * Checks that the reader is at a SOAP envelope start tag and, if so, reads past the tag.
     * @throws WsException on any other error reading the envelope start
     */
    public void startMessage() throws WsException {
        try {
            m_wrapper.toStart();
            if (!m_wrapper.isAtStart(SoapConstants.SOAP_URI, SoapConstants.SOAP_ENVNAME)) {
                throw new WsException("Message does not begin with SOAP Envelope");
            }
            m_wrapper.parsePastStartTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_ENVNAME);
        } catch (JiBXException e) {
            throw new WsException("Error reading start of SOAP message.", e);
        }
    }

    /**
     * Determines whether the reader is at a non-empty SOAP header start tag and, if so, reads past the tag. If the
     * reader is at an empty SOAP header (eg. <SOAP:header/>), the header is skipped and this method returns false.
     *
     * @return <code>true</code> if a SOAP header start tag was present and the SOAP header was non-empty
     * @throws WsException on any error reading the header
     */
    public boolean hasHeaders() throws WsException {
        boolean isStartHeader;
        try {
            isStartHeader = m_wrapper.isAtStart(SoapConstants.SOAP_URI, SoapConstants.SOAP_HEADERNAME);
            if (isStartHeader) {
                m_wrapper.parsePastStartTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_HEADERNAME);
                if (endHeader()) {
                    isStartHeader = false;
                } else {
                    m_wrapper.toStart();
                }
            }
        } catch (JiBXException e) {
            throw new WsException("Error reading start of header.", e);
        }

        return isStartHeader;
    }

    /**
     * Determines whether the reader is at a SOAP header end tag and, if so, reads past the tag.
     *
     * @return <code>true</code> if a SOAP header end tag was present
     * @throws WsException on any error reading the header end tag
     */
    public boolean endHeader() throws WsException {
        boolean isEndHeader;
        try {
            isEndHeader = m_wrapper.isAtEnd(SoapConstants.SOAP_URI, SoapConstants.SOAP_HEADERNAME);
            if (isEndHeader) {
                m_wrapper.parsePastEndTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_HEADERNAME);
            }
        } catch (JiBXException e) {
            throw new WsException("Error reading end of header.", e);
        }

        return isEndHeader;
    }

    /**
     * Checks whether the SOAP body element is empty. If non empty, will position the parser at the start of the 
     * first child element in the SOAP body.
     *
     * @return true if the message has a body, false otherwise
     * @throws WsException on any error checking the SOAP Body 
     */
    public boolean hasNonEmptyBody() throws WsException {
        try {
            if (m_wrapper.isAtEnd(SoapConstants.SOAP_URI, SoapConstants.SOAP_BODYNAME)) {
                return false;
            }
            m_wrapper.toStart();
        } catch (JiBXException e) {
            throw new WsException("Error checking for empty SOAP Body.", e);
        }
        return true;
    }

    /**
     * Reads past the SOAP Body start tag.
     *
     * @throws WsException on any error reading the body start tag
     */
    public void startBody() throws WsException {
        try {
            m_wrapper.parsePastStartTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_BODYNAME);
        } catch (JiBXException e) {
            throw new WsException("Error reading start of SOAP Body.", e);
        }
    }

    /**
     * Reads past the end tags for the SOAP body.
     *
     * @throws WsException on any error reading the body end tag
     */
    public void endBody() throws WsException {
        try {
            m_wrapper.parsePastEndTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_BODYNAME);
        } catch (JiBXException e) {
            throw new WsException("Expected end tag for SOAP body.", e);
        }
    }

    /**
     * Reads the start tag, the fault code, the fault string, and, if present, the fault actor of a SOAP fault.
     *
     * @return a SOAP fault with the fault code, fault string and fault actor set to the values in the SOAP fault
     * elements.
     * @throws WsException on any error reading the SOAP fault
     */
    public SoapFault startFault() throws WsException {
        try {
            m_wrapper.parsePastStartTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_FAULTNAME);
            QName code = createQName(m_wrapper.parseElementText("", SoapConstants.FAULTCODE_NAME));
            String fault = m_wrapper.parseElementText("", SoapConstants.FAULTSTRING_NAME);
            String actor = null;
            if (m_wrapper.isAtStart("", SoapConstants.FAULTACTOR_NAME)) {
                actor = m_wrapper.parseElementText("", SoapConstants.FAULTACTOR_NAME);
            }

            return new SoapFault(code, fault, actor);
        } catch (JiBXException e) {
            throw new WsException("Error reading start of fault.", e);
        }
    }

    /**
     * Determines whether the reader is at a SOAP fault detail start tag and, if so, reads past the tag.
     *
     * @return <code>true</code> if a SOAP fault detail start tag was present
     * @throws WsException on any error reading the fault detail start
     */
    public boolean startFaultDetail() throws WsException {
        try {
            if (m_wrapper.isAtStart("", SoapConstants.FAULTDETAIL_NAME)) {
                m_wrapper.parsePastStartTag("", SoapConstants.FAULTDETAIL_NAME);
                return true;
            }
            return false;
        } catch (JiBXException e) {
            throw new WsException("Error reading start of fault detail.", e);
        }

    }

    /**
     * Reads past the fault detail end tag.
     *
     * @throws WsException on any error reading the fault detail end tag
     */
    public void endFaultDetail() throws WsException {
        try {
            m_wrapper.parsePastEndTag("", SoapConstants.FAULTDETAIL_NAME);
        } catch (JiBXException e) {
            throw new WsException("Error reading end of fault detail.", e);
        }
    }

    /**
     * Reads past the fault end tag.
     *
     * @throws WsException on any error reading the fault end tag
     */
    public void endFault() throws WsException {
        try {
            m_wrapper.parsePastEndTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_FAULTNAME);
        } catch (JiBXException e) {
            throw new WsException("Error reading end of fault.", e);
        }
    }

    /**
     * Reads past the end tags for the SOAP envelope.
     *
     * @throws WsException on any error reading the envelope end tag
     */
    public void endMessage() throws WsException {
        try {
            m_wrapper.parsePastEndTag(SoapConstants.SOAP_URI, SoapConstants.SOAP_ENVNAME);
        } catch (JiBXException e) {
            throw new WsException("Expected end tag for SOAP envelope.", e);
        }
    }

    /**
     * Returns whether the SOAP body is a SOAP fault.
     *
     * @return <code>true</code> if the body is a SOAP fault, <code>false</code> otherwise
     * @throws WsException on any error checking the SOAP Fault tag
     */
    public boolean isBodyFault() throws WsException {
        try {
            // Determine if first body element is SOAP Fault
            return m_wrapper.isAtStart(SoapConstants.SOAP_URI, SoapConstants.SOAP_FAULTNAME);
        } catch (JiBXException e) {
            throw new WsException("Error checking for start of SOAP Fault.", e);
        }
    }


    private QName createQName(String str) {
        QName qname = null;
        if (str != null) {
            int i = str.indexOf(':');
            if (i == -1) {
                qname = new QName(str);
            } else {
                String prefix = str.substring(0, i);
                String name = str.substring(i + 1);
                String uri = m_wrapper.getReader().getNamespace(prefix);
                qname = new QName(uri, prefix, name);
            }
        }
        return qname;
    }
}
