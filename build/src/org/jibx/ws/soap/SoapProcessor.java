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

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.ExchangeContext;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.MessageContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.io.XmlReaderWrapper;
import org.jibx.ws.io.handler.OutHandler;
import org.jibx.ws.process.Processor;
import org.jibx.ws.transport.InConnection;
import org.jibx.ws.transport.OutConnection;
import org.jibx.ws.transport.WsTransportException;

/**
 * Processor for SOAP messages. The sequence of message exchanges is defined by the {@link ExchangeContext}, which
 * contains a list of {@link MessageContext}s, corresponding to the expected sequence of outbound and inbound messages.
 * Each <code>MessageContext</code> is configured with appropriate handlers to handle the content of the message.
 * <p>
 * The processor can be invoked using the {@link #invoke(OutConnection, InConnection)} method, which sequentially sends
 * and receives messages according to the {@link MessageContext}.
 * <p>
 * Alternatively, the caller can send and receive messages using the individual send and receive methods.
 *
 * @author Nigel Charman
 */
public final class SoapProcessor implements Processor
{
    private static final Log logger = LogFactory.getLog(SoapProcessor.class);

//    private final SoapVersion m_soapVersion;

    private ExchangeContext m_exchangeCtx;

    /** The optional SOAP encoding style to set in the SOAP message. */
    private String m_encodingStyle;

    /**
     * Creates a SoapProcessor with the specified SoapVersion. The transport defined in the outbound context is used for
     * both the outbound and inbound messages. The outbound message is formatted according to the XML formatting options
     * in the outbound context.
     *
     * @param soapVersion the version of the SOAP standard to use
     */
    public SoapProcessor(SoapVersion soapVersion) {
//        m_soapVersion = soapVersion;
    }
    
    /**
     * Creates a SoapProcessor with the specified SoapVersion. The transport defined in the outbound context is used for
     * both the outbound and inbound messages. The outbound message is formatted according to the XML formatting options
     * in the outbound context.
     * <p>
     * Each phase of the message processing calls the commands and handlers associated with that phase.
     *
     * @param soapVersion the version of the SOAP standard to use
     * @param exchangeContext the context of the message exchange. Contains the outbound and inbound contexts.
     */
    public SoapProcessor(SoapVersion soapVersion, ExchangeContext exchangeContext) {
//        m_soapVersion = soapVersion;
        m_exchangeCtx = exchangeContext;
    }

    /**
     * Sets the optional <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383495">SOAP encodingStyle</a>
     *  attribute on the SOAP envelope.  If this method is not called, or the encodingStyle parameter is set to 
     *  <code>null</code>, then no encodingStyle attribute is added.
     *
     * @param encodingStyle value to set in encodingStyle attribute
     */
    public void setSoapEncodingStyle(String encodingStyle) {
        m_encodingStyle = encodingStyle;
    }
    
    /**
     * {@inheritDoc}
     */
    public void invoke(OutConnection oconn, InConnection iconn) throws IOException, WsException {
        MessageContext msgCtx = m_exchangeCtx.getCurrentMessageContext();
        while (msgCtx != null) {
            if (msgCtx.isOutbound()) {
                sendMessage(oconn);
            } else {
                receiveMessage(iconn);
            }
            m_exchangeCtx.switchMessageContext();
            msgCtx = m_exchangeCtx.getCurrentMessageContext();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(OutConnection conn) throws IOException, WsException {
        if (m_exchangeCtx.getCurrentMessageContext() == null) {
            throw new IllegalStateException("No message context available for sending message");
        }
        if (!m_exchangeCtx.getCurrentMessageContext().isOutbound()) {
            throw new IllegalStateException("Cannot send message when current message context is inbound");
        }
        OutContext context = (OutContext) m_exchangeCtx.getCurrentMessageContext();
       
        SoapWriter soapWriter = new SoapWriter(conn);

        boolean requestCompleted = false;
        try {
            logger.debug("Starting send message");
            soapWriter.startMessage(m_encodingStyle);
            IXMLWriter xmlWriter = soapWriter.getWriter();
            if (context.hasHandlers(SoapPhase.HEADER)) {
                soapWriter.startHeader();
                context.invokeHandlers(SoapPhase.HEADER, xmlWriter);
                soapWriter.endHeader();
            }
            soapWriter.startBody();
            context.invokeBodyWriter(xmlWriter);
            logger.debug("Ending send message body");
            soapWriter.endBody();
            soapWriter.sendMessageCompletely();
            logger.debug("Message sent");
            requestCompleted = true;
        } finally {
            conn.outputComplete();
            
            // Abort the request on error
            if (!requestCompleted) {
                try {
                    logger.debug("Aborting send");
                    soapWriter.abortMessage();
                } catch (IOException e) {
                    logger.error("Error aborting send", e);
                }
            }
        }
    }

    /**
     * Send a SOAP fault message. 
     * 
     * @param error the error message to send
     * @param conn transport connection
     * @throws IOException on an I/O error, for example unable to connect to server
     * @throws WsException on any error other than I/O, for example invalid format of the response message
     */
    public void sendFaultMessage(SoapFault error, OutConnection conn) throws IOException, WsException {
        if (m_exchangeCtx.getCurrentMessageContext() == null) {
            throw new IllegalStateException("No message context available for sending fault message");
        }
        if (!m_exchangeCtx.getCurrentMessageContext().isOutbound()) {
            throw new IllegalStateException("Cannot send fault message when current message context is inbound");
        }
        OutContext context = (OutContext) m_exchangeCtx.getCurrentMessageContext();

        SoapWriter soapWriter = new SoapWriter(conn);

        boolean requestCompleted = false;

        try {
            soapWriter.startMessage(m_encodingStyle);
            IXMLWriter xmlWriter = soapWriter.getWriter();

            logger.debug("Starting send fault");
            soapWriter.startBody();

            SoapFault fault = (SoapFault) error;

            soapWriter.startFault(fault);

            List detailWriters = fault.getDetailWriters();

            if (detailWriters != null && detailWriters.size() > 0) {
                soapWriter.startFaultDetail();
                for (int i = 0; i < detailWriters.size(); i++) {
                    ((OutHandler) detailWriters.get(i)).invoke(context, xmlWriter);
                }
                soapWriter.endFaultDetail();
            }
            soapWriter.endFault();

            logger.debug("Ending send fault body");
            soapWriter.endBody();
            soapWriter.sendMessageCompletely();
            logger.debug("Fault sent");

            requestCompleted = true;
        } finally {
            conn.outputComplete();
            // Abort the request on error
            if (!requestCompleted) {
                try {
                    logger.debug("Aborting send fault");
                    soapWriter.abortMessage();
                } catch (IOException e) {
                    logger.error("Error aborting send fault", e);
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public void receiveMessage(InConnection conn) throws IOException, WsException {
        if (m_exchangeCtx.getCurrentMessageContext() == null) {
            throw new IllegalStateException("No message context available for receiving message");
        }
        if (m_exchangeCtx.getCurrentMessageContext().isOutbound()) {
            throw new IllegalStateException("Cannot receive message when current message context is outbound");
        }
        InContext context = (InContext) m_exchangeCtx.getCurrentMessageContext();

        SoapReader soapReader = new SoapReader(conn);

        try {
            logger.debug("Starting receive message");
            soapReader.startMessage();
            IXMLReader xmlReader = soapReader.getReader();
    
            if (soapReader.hasHeaders()) {
                readSoapHeaders(xmlReader, context);
                soapReader.endHeader();
            }

            soapReader.startBody();
            if (soapReader.hasNonEmptyBody()) {
                if (soapReader.isBodyFault()) {
                    // Handle SOAP Faults. Will need changes for SOAP 1.2
                    SoapFault fault = soapReader.startFault();
                    if (soapReader.startFaultDetail()) {
                        readSoapFaultDetail(xmlReader, context, fault);
                        soapReader.endFaultDetail();
                    }
                    soapReader.endFault();
                    context.setBody(fault);
                } else {
                    // Handle SOAP body
                    context.invokeBodyReader(xmlReader);
                    if (context.getBody() == null) {
                        throw new WsException("No handlers could be found for unmarshalling the SOAP body payload");
                    }
                }
            }
            soapReader.endBody();
            soapReader.endMessage();
        } catch (WsException e) {
            if (conn.hasError()) {
                throw new WsTransportException(conn.getErrorMessage());
            } else {
                throw e;
            }
        } finally {
            conn.inputComplete();
        }
    }

    /**
     * Reads SOAP fault details from the xmlReader. For each element in the SOAP fault details, any handlers that have
     * been defined for the bodyFaultPhase are given a chance to read the fault details. If no handlers read the
     * details, the element is skipped.
     *
     * @param xmlReader the reader to read the SOAP fault from
     * @param context
     * @param fault the SOAP fault to add the details to
     * @throws IOException
     * @throws WsException
     */
    private void readSoapFaultDetail(IXMLReader xmlReader, InContext context, SoapFault fault) throws IOException,
        WsException {
        try {
            XmlReaderWrapper wrpr = XmlReaderWrapper.createXmlReaderWrapper(xmlReader);
            while (wrpr.toTag() == IXMLReader.START_TAG) {
                Object detail = null;
                /**
                 * If handlers are defined on the body fault phase, they have the opportunity to add details to the
                 * SoapFault. Otherwise the SOAP details will be skipped.
                 */
                if (context.hasHandlers(SoapPhase.BODY_FAULT)) {
                    detail = context.invokeInHandlers(SoapPhase.BODY_FAULT, xmlReader);
                }
                if (detail != null) {
                    fault.addDetail(detail);
                } else {
                    wrpr.skipPastEndTag(xmlReader.getNamespace(), xmlReader.getName());
                }
            }
        } catch (JiBXException e) {
            throw new WsException("Error while unmarshalling SOAP fault details", e);
        }
    }

    /**
     * Reads SOAP header from the xmlReader. For each element in the SOAP header, all handlers that have been defined
     * for the header are sequentially given a chance to read the fault details. If no handlers read the details, the
     * element is skipped, unless the SOAP mustUnderstand attribute is set, when a WsNotUnderstoodException is raised.
     *
     * @param xmlReader the reader to read the SOAP fault from
     * @param context
     * @throws IOException
     * @throws WsException
     */
    private void readSoapHeaders(IXMLReader xmlReader, InContext context) throws IOException, WsException {
        try {
            XmlReaderWrapper wrpr = XmlReaderWrapper.createXmlReaderWrapper(xmlReader);
            while (wrpr.toTag() == IXMLReader.START_TAG) {
                /**
                 * If handlers are defined on the headers, they have the opportunity to read the header. Otherwise the
                 * header will be skipped.
                 */
                Object header = context.invokeInHandlers(SoapPhase.HEADER, xmlReader);
                if (header == null) {
                    String mustUnderstand = xmlReader.getAttributeValue(SoapConstants.SOAP_URI,
                        SoapConstants.MUSTUNDERSTAND_NAME);
                    if (SoapConstants.MUSTUNDERSTAND_TRUE.equals(mustUnderstand)) {
                        throw new WsNotUnderstoodException("");
                    }
                    wrpr.skipPastEndTag(xmlReader.getNamespace(), xmlReader.getName());
                }
            }
        } catch (JiBXException e) {
            throw new WsException("Error while unmarshalling SOAP header details", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setExchangeContext(ExchangeContext exchangeCtx) {
        m_exchangeCtx = exchangeCtx;
    }
    
    /**
     * {@inheritDoc}
     */
    public void reset() {
        m_exchangeCtx.reset();
    }

    /**
     * {@inheritDoc}
     */
    public MessageContext getCurrentMessageContext() {
        return m_exchangeCtx.getCurrentMessageContext();
    }

    /**
     * {@inheritDoc}
     */
    public MessageContext getNextMessageContext() {
        return m_exchangeCtx.getNextMessageContext();
    }

    /**
     * {@inheritDoc}
     */
    public void switchMessageContext() {
        m_exchangeCtx.switchMessageContext();
    }
}
