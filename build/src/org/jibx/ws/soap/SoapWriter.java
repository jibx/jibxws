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

import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.ws.WsException;
import org.jibx.ws.transport.OutConnection;

/**
 * SOAP writer class. This handles all SOAP message generation.
 * 
 * @author Dennis Sosnoski
 */
final class SoapWriter
{
    private static final int NO_NS_IDX = 0;
    
    /** The namespaces required to construct an XML writer for SOAP messages. */
    private static final String[] SOAP_NS = new String[] {"", MarshallingContext.XML_NAMESPACE, SoapConstants.SOAP_URI};
    
    /** The index of the SOAP_URI in SOAP_NAMESPACES. */
    private static final int SOAP_NS_IDX = 2;
    
    /** The client's transport connection to the SOAP service. */
    private OutConnection m_connection;
    
    /** Writer instance obtained from connection. */
    private IXMLWriter m_writer;

    private boolean m_customFaultCodeNS;
    
    /**
     * Constructor. This sets the connection and output options to be used for the life of the writer.
     * @param conn the connection to write the SOAP message to
     * @throws IOException on any error creating the writer 
     * @throws WsException on configuration error
     */
    public SoapWriter(OutConnection conn) throws IOException, WsException {
        m_connection = conn;
        m_writer = conn.getNormalWriter(SOAP_NS);
    }

    /**
     * Get the XML writer instance.
     * 
     * @return writer
     */
    public IXMLWriter getWriter() {
        return m_writer;
    }

    /**
     * Writes the XML prolog and the SOAP start tag.  No encodingStyle attribute is added to the SOAP envelope.
     * 
     * @throws IOException on any write error   
     */
    public void startMessage() throws IOException {
        startMessage(null);
    }

    /**
     * Writes the XML prolog and the SOAP start tag.
     * @param encodingStyle the optional 
     * <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383495">SOAP encodingStyle</a>
     * attribute on the SOAP envelope.  If set to <code>null</code>, no encodingStyle attribute is added.
     * 
     * @throws IOException on any write error   
     */
    public void startMessage(String encodingStyle) throws IOException {
        m_writer.startTagNamespaces(SOAP_NS_IDX, SoapConstants.SOAP_ENVNAME, new int[] { SOAP_NS_IDX },
            SoapConstants.SOAP_PREFIX_ARRAY);
        if (encodingStyle != null) {
            m_writer.addAttribute(SOAP_NS_IDX, SoapConstants.ENCODING_STYLE, encodingStyle);
        }
        m_writer.closeStartTag();
    }
    
    /**
     * Writes the SOAP header start tag.
     * 
     * @throws IOException on any write error   
     */
    public void startHeader() throws IOException {
        m_writer.startTagClosed(SOAP_NS_IDX, SoapConstants.SOAP_HEADERNAME);
    }
    
    /**
     * Writes the SOAP header end tag.
     * 
     * @throws IOException on any write error   
     */
    public void endHeader() throws IOException {
        m_writer.endTag(SOAP_NS_IDX, SoapConstants.SOAP_HEADERNAME);
    }
    
    /**
     * Writes the SOAP body start tag.
     * 
     * @throws IOException on any write error   
     */
    public void startBody() throws IOException {
        m_writer.startTagClosed(SOAP_NS_IDX, SoapConstants.SOAP_BODYNAME);
    }
    
    /**
     * Writes the SOAP body end tag.
     * 
     * @throws IOException on any write error   
     */
    public void endBody() throws IOException {
        m_writer.endTag(SOAP_NS_IDX, SoapConstants.SOAP_BODYNAME);
        m_writer.endTag(SOAP_NS_IDX, SoapConstants.SOAP_ENVNAME);
    }
    
    /**
     * Writes the start tag, the fault code, the fault string, and, if present, the fault actor of a SOAP fault. 
     * 
     * @param fault the fault to be written
     * @throws IOException on any write error   
     */
    public void startFault(SoapFault fault) throws IOException {
        
        String faultCode = null;
        String uri = fault.getFaultCode().getUri();
        String prefix = fault.getFaultCode().getPrefix();
    
        if (SoapConstants.SOAP_URI.equals(uri)) {
	        m_customFaultCodeNS = false;
            m_writer.startTagClosed(SOAP_NS_IDX, SoapConstants.SOAP_FAULTNAME);
            faultCode = SoapConstants.SOAP_PREFIX + ":" + fault.getFaultCode().getName();
        } else {
            // it's a custom fault code with a namespace other than the SOAP namespace 
            m_customFaultCodeNS = true;
            int base = m_writer.getNamespaceCount();
            int[] indexes = new int[] { base };
            String[] uris = new String[] { uri };
            String[] prefs = new String[] { prefix };
            
            // add the namespace declarations to current element
            m_writer.pushExtensionNamespaces(uris);
            
            m_writer.startTagNamespaces(SOAP_NS_IDX, SoapConstants.SOAP_FAULTNAME, indexes, prefs);
            m_writer.closeStartTag();
            faultCode = prefix + ":" + fault.getFaultCode().getName();
        }
        writeTextElement(NO_NS_IDX, SoapConstants.FAULTCODE_NAME, faultCode);
        writeTextElement(NO_NS_IDX, SoapConstants.FAULTSTRING_NAME, fault.getFaultString());
        if (fault.getFaultActor() != null) {
            writeTextElement(NO_NS_IDX, SoapConstants.FAULTACTOR_NAME, fault.getFaultActor());
        }
    }
    
    /**
     * Writes the SOAP fault detail start tag.  
     * 
     * @throws IOException on any write error   
     */
    public void startFaultDetail() throws IOException {
        m_writer.startTagClosed(NO_NS_IDX, SoapConstants.FAULTDETAIL_NAME);
    }
    
    /**
     * Writes the SOAP fault detail end tag.  
     * 
     * @throws IOException on any write error   
     */
    public void endFaultDetail() throws IOException {
        m_writer.endTag(NO_NS_IDX, SoapConstants.FAULTDETAIL_NAME);
    }
    
    /**
     * Writes the SOAP fault end tag.  
     * 
     * @throws IOException on any write error   
     */
    public void endFault() throws IOException {
        m_writer.endTag(SOAP_NS_IDX, SoapConstants.SOAP_FAULTNAME);
        if (m_customFaultCodeNS) {
            m_writer.popExtensionNamespaces();
        }
    }

    /**
     * Send the message completely. This commits the message and waits until it has been completely sent before
     * returning.
     * 
     * @throws IOException on any error sending the message   
     */    
    public void sendMessageCompletely() throws IOException {
//        m_writer.close();
        m_connection.close();
    }
    
    /**
     * Abort the message. This closes the XML Writer without necessarily completing the send of the message.
     * 
     * @throws IOException on any error aborting the message   
     */
    public void abortMessage() throws IOException {
//        m_writer.close();
        m_connection.close();
    }
    
    /**
     * Writes a simple text element to the xmlWriter.
     * 
     * @param nsi the namespace index
     * @param elementName the name of the element
     * @param text the element text
     * @throws IOException
     */
    private void writeTextElement(int nsi, String elementName, String text) throws IOException {
        m_writer.startTagClosed(nsi, elementName);
        m_writer.writeTextContent(text);
        m_writer.endTag(nsi, elementName);
    }
}
