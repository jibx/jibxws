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

package org.jibx.ws.io;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.IXMLReaderFactory;

/**
 * Utility methods for working with an {@link org.jibx.runtime.IXMLReader} instance. 
 * In JiBX 2.0 these methods will probably become part of the reader interface.
 * 
 * @author Dennis M. Sosnoski
 */
// D-TODO move to reader interface in JiBX 2.0.
public final class XmlReaderWrapper
{
    /** Factory for creating XML readers. */
    private static final IXMLReaderFactory s_readerFactory;
    static {
        String prop = System.getProperty("org.jibx.runtime.impl.parser");
        if (prop == null) {

            // try XMLPull parser factory first
            IXMLReaderFactory fact = null;
            try {
                fact = createReaderFactory("org.jibx.runtime.impl.XMLPullReaderFactory");
            } catch (Throwable e) {
                fact = createReaderFactory("org.jibx.runtime.impl.StAXReaderFactory");
            }
            s_readerFactory = fact;

        } else {

            // try loading factory class specified by property
            s_readerFactory = createReaderFactory(prop);
        }
    }

    /** The reader that is being wrapped. */
    private IXMLReader m_reader;

    /**
     * Constructor.
     * 
     * @param reader wrapped reader
     */
    private XmlReaderWrapper(IXMLReader reader) {
        m_reader = reader;
    }
    
    /**
     * Parser factory class loader method. This is used during initialization to check that a particular factory class
     * is usable.
     * 
     * @param cname class name
     * @return reader factory instance
     * @throws RuntimeException on error creating class instance
     */
    private static IXMLReaderFactory createReaderFactory(String cname) {

        // try loading factory class from context loader
        Class clas = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                clas = loader.loadClass(cname);
            } catch (ClassNotFoundException e) { /* deliberately empty */
            }
        }
        if (clas == null) {

            // next try the class loader that loaded the unmarshaller interface
            try {
                loader = IUnmarshallingContext.class.getClassLoader();
                clas = loader.loadClass(cname);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to specified parser factory class " + cname);
            }
        }
        if (!(IXMLReaderFactory.class.isAssignableFrom(clas))) {
            throw new RuntimeException("Specified parser factory class " + cname
                + " does not implement IXMLReaderFactory interface");
        }

        // use static method to create parser factory class instance
        try {
            Method meth = clas.getMethod("getInstance", null);
            return (IXMLReaderFactory)meth.invoke(null, null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Specified parser factory class " + cname
                + " does not define static getInstance() method");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error on parser factory class " + cname + " getInstance() method call: "
                + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error on parser factory class " + cname + " getInstance() method call: "
                + e.getMessage());
        }
    }
    
    /**
     * Get the reader factory in use.
     * 
     * @return factory
     */
    public static IXMLReaderFactory getFactory() {
        return s_readerFactory;
    }

    /**
     * Create an IXMLReaderWrapper.
     * 
     * @param ins stream supplying document data
     * @param enc document input encoding, or <code>null</code> if to be determined by parser
     * @return reader
     * @throws JiBXException if error creating parser
     */
    public static XmlReaderWrapper createXmlReaderWrapper(InputStream ins, String enc) throws JiBXException {
        return new XmlReaderWrapper(s_readerFactory.createReader(ins, null, enc, true));
    }

    /**
     * Create an IXMLReaderWrapper.
     * 
     * @param reader the reader to wrap 
     * 
     * @return reader wrapper
     */
    public static XmlReaderWrapper createXmlReaderWrapper(IXMLReader reader) {
        return new XmlReaderWrapper(reader);
    }
    
    /**
     * Build name with optional namespace. Just returns the appropriate name format.
     * 
     * @param ns namespace URI of name
     * @param name local name part of name
     * @return formatted name string
     */
    public static String buildNameString(String ns, String name) {
        if (ns == null || "".equals(ns)) {
            return "\"" + name + "\"";
        } else {
            return "\"{" + ns + "}" + name + "\"";
        }
    }

    /**
     * Build current element name, with optional namespace.
     * 
     * @return formatted name string
     */
    public String currentNameString() {
        return buildNameString(m_reader.getNamespace(), m_reader.getName());
    }

    /**
     * Build current parse input position description.
     * 
     * @return text description of current parse position
     */
    public String buildPositionString() {
        return m_reader.buildPositionString();
    }

    /**
     * Verify namespace. This is a simple utility method that allows multiple representations for the empty namespace as
     * a convenience for generated code.
     * 
     * @param ns namespace URI expected (may be <code>null</code> or the empty string for the empty namespace)
     * @return <code>true</code> if the current namespace matches that expected, <code>false</code> if not
     */
    public boolean verifyNamespace(String ns) {
        if (ns == null || "".equals(ns)) {
            return m_reader.getNamespace().length() == 0;
        } else {
            return ns.equals(m_reader.getNamespace());
        }
    }

    /**
     * Parse to start tag. Ignores character data seen prior to a start tag, but throws exception if an end tag or the
     * end of the document is seen before a start tag. Leaves the parser positioned at the start tag.
     * 
     * @return element name of start tag found
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public String toStart() throws JiBXException {
        if (m_reader.getEventType() == IXMLReader.START_TAG) {
            return m_reader.getName();
        }
        while (true) {
            switch (m_reader.next()) {

                case IXMLReader.START_TAG:
                    return m_reader.getName();

                case IXMLReader.END_TAG:
                    throw new JiBXException("Expected start tag, found end tag " + currentNameString() + " "
                        + buildPositionString());

                case IXMLReader.END_DOCUMENT:
                    throw new JiBXException("Expected start tag, found end of document " + buildPositionString());

            }
        }
    }

    /**
     * Parse to end tag. Ignores character data seen prior to an end tag, but throws exception if a start tag or the end
     * of the document is seen before an end tag. Leaves the parser positioned at the end tag.
     * 
     * @return element name of end tag found
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public String toEnd() throws JiBXException {
        if (m_reader.getEventType() == IXMLReader.END_TAG) {
            return m_reader.getName();
        }
        while (true) {
            switch (m_reader.next()) {

                case IXMLReader.START_TAG:
                    throw new JiBXException("Expected end tag, found start tag " + currentNameString() + " "
                        + buildPositionString());

                case IXMLReader.END_TAG:
                    return m_reader.getName();

                case IXMLReader.END_DOCUMENT:
                    throw new JiBXException("Expected end tag, found end of document " + buildPositionString());

            }
        }
    }

    /**
     * Parse to start or end tag. If not currently positioned at a start or end tag this first advances the parse to the
     * next start or end tag. Throws an exception if the end of document is seen before a start or end tag.
     * 
     * @return parser event type for start tag or end tag
     * @throws JiBXException on any error (possibly wrapping other exception)
     * @see org.jibx.runtime.IXMLReader
     */ 
    public int toTag() throws JiBXException {
        int type = m_reader.getEventType();
        while (type != IXMLReader.START_TAG && type != IXMLReader.END_TAG) {
            if (type == IXMLReader.END_DOCUMENT) {
                throw new JiBXException("Expected tag, found end of document " + buildPositionString());
            } else {
                type = m_reader.next();
            }
        }
        return type;
    }

    /**
     * Check if next tag is start of element. If not currently positioned at a start or end tag this first advances the
     * parse to the next start or end tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @return <code>true</code> if at start of element with supplied name, <code>false</code> if not
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public boolean isAtStart(String ns, String name) throws JiBXException {
        return toTag() == IXMLReader.START_TAG && m_reader.getName().equals(name) && verifyNamespace(ns);
    }

    /**
     * Check if next tag is end of element. If not currently positioned at a start or end tag this first advances the
     * parse to the next start or end tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @return <code>true</code> if at end of element with supplied name, <code>false</code> if not
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public boolean isAtEnd(String ns, String name) throws JiBXException {
        return toTag() == IXMLReader.END_TAG && m_reader.getName().equals(name) && verifyNamespace(ns);
    }
    
    /**
     * Parse to start of element. Ignores character data to next start or end tag, but throws exception if an end tag is
     * seen before a start tag, or if the start tag seen does not match the expected name. Leaves the parse positioned
     * at the start tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public void parseToStartTag(String ns, String name) throws JiBXException {
        if (toTag() == IXMLReader.START_TAG) {
            if (!m_reader.getName().equals(name) || !verifyNamespace(ns)) {
                throw new JiBXException("Expected " + buildNameString(ns, name) + " start tag, found "
                    + currentNameString() + " start tag " + buildPositionString());
            }
        } else {
            throw new JiBXException("Expected " + buildNameString(ns, name) + " start tag, found "
                + currentNameString() + " end tag " + buildPositionString());
        }
    }

    /**
     * Parse past start of element. Ignores character data to next start or end tag, but throws exception if an end tag
     * is seen before a start tag, or if the start tag seen does not match the expected name. Leaves the parse
     * positioned following the start tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public void parsePastStartTag(String ns, String name) throws JiBXException {
        parseToStartTag(ns, name);
        m_reader.nextToken();
    }

    /**
     * Parse past start of expected element. If not currently positioned at a start or end tag this first advances the
     * parser to the next tag. If the expected start tag is found it is skipped and the parse is left positioned
     * following the start tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @return <code>true</code> if start tag found, <code>false</code> if not
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public boolean parseIfStartTag(String ns, String name) throws JiBXException {
        if (isAtStart(ns, name)) {
            m_reader.nextToken();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parse past current end of element. Ignores character data to next start or end tag, but throws exception if a
     * start tag is seen before a end tag, or if the end tag seen does not match the expected name. Leaves the parse
     * positioned following the end tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public void parsePastCurrentEndTag(String ns, String name) throws JiBXException {

        // check for match on expected end tag
        if (toTag() == IXMLReader.END_TAG) {
            if (m_reader.getName().equals(name) && verifyNamespace(ns)) {
                m_reader.nextToken();
            } else {
                throw new JiBXException("Expected " + buildNameString(ns, name) + " end tag, found "
                    + currentNameString() + " end tag " + buildPositionString());
            }
        } else {
            throw new JiBXException("Expected " + buildNameString(ns, name) + " end tag, found "
                + currentNameString() + " start tag " + buildPositionString());
        }
    }

    /**
     * Parse past end of element. If currently at a start tag parses past that start tag, then ignores character data to
     * next start or end tag, and throws exception if a start tag is seen before a end tag, or if the end tag seen does
     * not match the expected name. Leaves the parse positioned following the end tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public void parsePastEndTag(String ns, String name) throws JiBXException {

        // most past current tag if start
        if (m_reader.getEventType() == IXMLReader.START_TAG) {
            m_reader.nextToken();
        }

        // handle as current tag
        parsePastCurrentEndTag(ns, name);
    }

    /**
     * Parse past end of named element. Will skip all content between the current position and the end of the element.
     * Leaves the parse positioned following the end tag.
     * 
     * @param ns namespace URI for expected element (may be <code>null</code> or the empty string for the empty
     * namespace)
     * @param name element name expected
     * @throws JiBXException on any error (possibly wrapping other exception), including end of document reached 
     * without finding the end tag
     */
    public void skipPastEndTag(String ns, String name) throws JiBXException {
        boolean found = false;
        while (!found) {
            if (toTag() == IXMLReader.END_TAG && m_reader.getName().equals(name) && verifyNamespace(ns)) {
                found = true;
            }
            m_reader.nextToken();
        }
    }

    /**
     * Check if next tag is a start tag. If not currently positioned at a start or end tag this first advances the parse
     * to the next start or end tag.
     * 
     * @return <code>true</code> if at start of element, <code>false</code> if at end
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public boolean isStart() throws JiBXException {
        return toTag() == IXMLReader.START_TAG;
    }

    /**
     * Check if next tag is an end tag. If not currently positioned at a start or end tag this first advances the parse
     * to the next start or end tag.
     * 
     * @return <code>true</code> if at end of element, <code>false</code> if at start
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public boolean isEnd() throws JiBXException {
        return toTag() == IXMLReader.END_TAG;
    }

    /**
     * Skip past the end of the current element. This cannot be used when the parser is positioned on the start tag or
     * end tag for a child element of the element to be skipped (because it'd just skip that child element). It's okay
     * to use when the parser is on the start tag or end tag for the element to be skipped. The parse is returned
     * positioned after the end tag.
     * 
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    public void skipCurrent() throws JiBXException {
        if (m_reader.getEventType() != IXMLReader.END_TAG) {

            // loop until end tag for current start tag reached
            int depth = 1;
            while (depth > 0) {
                int event = m_reader.next();
                if (event == IXMLReader.END_TAG) {
                    depth--;
                } else if (event == IXMLReader.START_TAG) {
                    depth++;
                }
                // Note END_DOCUMENT cannot happen, since the parser will report the document is malformed
            }
        }
        m_reader.nextToken();
    }

    /**
     * Accumulate text content to next start or end tag. If the parse is initially positioned on a start tag, this first
     * moves past the start tag. It then consolidates all text found before the next start or end tag to a single
     * string.
     * 
     * @return consolidated text string (empty string if no text components)
     * @exception JiBXException on error in unmarshalling
     */
    public String accumulateText() throws JiBXException {
        String text = null;
        StringBuffer buff = null;
        loop: while (true) {
            switch (m_reader.getEventType()) {

                case IXMLReader.ENTITY_REF:
                    if (m_reader.getText() == null) {
                        throw new JiBXException("Unexpanded entity reference in text at " + buildPositionString());
                    }
                    // fall through into text accumulation

                case IXMLReader.CDSECT:
                case IXMLReader.TEXT:
                    if (text == null) {
                        text = m_reader.getText();
                    } else {
                        if (buff == null) {
                            buff = new StringBuffer(text);
                        }
                        buff.append(m_reader.getText());
                    }
                    break;

                case IXMLReader.END_TAG:
                case IXMLReader.START_TAG:
                    break loop;

                default:
                    break;

            }
            m_reader.nextToken();
        }
        if (buff == null) {
            return (text == null) ? "" : text;
        } else {
            return buff.toString();
        }
    }

    /**
     * Returns the underlying reader that this wrapper is using.
     *
     * @return reader
     */
    public IXMLReader getReader() {
        return m_reader;
    }

    /**
     * Throw exception for expected element end tag not found.
     *
     * @param ns namespace URI of name
     * @param name local name part of name
     * @exception JiBXException always thrown
     */
    public void throwEndTagNameError(String ns, String name)
        throws JiBXException {
        throw new JiBXException("Expected " + buildNameString(ns, name) 
            + " end tag, found " + currentNameString() + " end tag " 
            + buildPositionString());
    }
    
    /**
     * Parse past end of element, returning optional text content. Assumes
     * you've already parsed past the start tag of the element, so it just looks
     * for text content followed by the end tag, and returns with the parser
     * positioned after the end tag.
     *
     * @param ns namespace URI for expected element (may be <code>null</code>
     * or the empty string for the empty namespace)
     * @param tag element name expected
     * @return content text from element
     * @throws JiBXException on any error (possible wrapping other exception)
     */
    public String parseContentText(String ns, String tag)
        throws JiBXException {
        String text = accumulateText();
        switch (m_reader.getEventType()) {

            case IXMLReader.END_TAG:
                if (m_reader.getName().equals(tag) 
                    && verifyNamespace(ns)) {
                    m_reader.nextToken();
                    return text;
                } else {
                    throwEndTagNameError(ns, tag);
                }

            case IXMLReader.START_TAG:
                throw new JiBXException("Expected " 
                    + buildNameString(ns, tag) + " end tag, " 
                    + "found " + currentNameString() + " start tag " 
                    + buildPositionString());

            case IXMLReader.END_DOCUMENT:
                throw new JiBXException("Expected " 
                    + buildNameString(ns, tag) + " end tag, " 
                    + "found end of document " + buildPositionString());

        }
        return null;
    }
    
    /**
     * Parse entire element, returning text content.
     * Expects to find the element start tag, text content, and end tag,
     * in that order, and returns with the parser positioned following
     * the end tag.
     *
     * @param ns namespace URI for expected element (may be <code>null</code>
     * or the empty string for the empty namespace)
     * @param tag element name expected
     * @return content text from element
     * @throws JiBXException on any error (possible wrapping other exception)
     */
    public String parseElementText(String ns, String tag) throws JiBXException {
        parsePastStartTag(ns, tag);
        return parseContentText(ns, tag);
    }
}
