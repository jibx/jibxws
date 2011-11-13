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

package org.jibx.ws.transport;

import java.io.IOException;

import org.jibx.runtime.IXMLReader;
import org.jibx.ws.WsException;

/**
 * Represents a one way connection used for receiving a message.
 * 
 * @author Nigel Charman
 */
public interface InConnection extends InMessageAttributes 
{
    /**
     * Prepares the connection for use. Properties and other information will generally not be available until after
     * this method is called.
     * 
     * @throws IOException on I/O error
     * @throws WsException on web service error
     */
    void init() throws IOException, WsException;
    
    /**
     * Get a reader for received XML data.
     * 
     * @return reader
     * @throws IOException on I/O error
     * @throws WsException on web service error
     */
    IXMLReader getReader() throws IOException, WsException;
    
    /**
     * Returns whether an error has been set on the connection (eg. HTTP error code for HTTP connection). 
     *
     * @return <code>true</code> if error has been set, <code>false</code> otherwise
     * @throws IOException on I/O error
     */
    boolean hasError() throws IOException;
    
    /**
     * Returns the error message from the connection.  
     *
     * @return error message, or <code>null</code> if there is no error
     * @throws IOException on I/O error
     */
    String getErrorMessage() throws IOException;
    
    /**
     * Indicates that the reading of the input is complete.
     */
    void inputComplete();

    /**
     * End the reader usage and release resources.
     * 
     * @throws IOException on I/O error
     */
    void close() throws IOException;

    /**
     * Get the specified property from the connection.
     *
     * @param name the name of the property
     * @return the property value, or <code>null</code> if the property was not set on the request
     */
    String getProperty(String name);
}
