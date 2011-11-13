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

package org.jibx.ws;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An unhandled service exception.  The {@link #wrap(Throwable)} method wraps any checked exception as an 
 * {@link UnhandledWsException} and returns {@link RuntimeException}s as is.
 * 
 * @author Nigel Charman
 */
public final class UnhandledWsException extends RuntimeException
{
    private static final long serialVersionUID = 5426854285737675647L;
    
    private Throwable m_throwable;
    
    private UnhandledWsException(Throwable t) {
        this.m_throwable = t;
    }
    
    /** Wraps a non-RuntimeException in an UnhandledWsException.
     * @param t the exception to wrap
     * @return the original exception, if it was a {@link RuntimeException}, or a new {@link UnhandledWsException}
     * otherwise.
     */
    public static RuntimeException wrap(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new UnhandledWsException(t);
    }

    /** {@inheritDoc} */
// JDK 1.4 or higher only    
//    public Throwable getCause() {
//        return m_throwable.getCause();
//    }

    /** {@inheritDoc} */
    public String getLocalizedMessage() {
        return m_throwable.getLocalizedMessage();
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return m_throwable.getMessage();
    }

//    StackTraceElement non-existent in JDK 1.3
//    /** {@inheritDoc} */
//    public StackTraceElement[] getStackTrace() {
//        return m_throwable.getStackTrace();
//    }

    /** {@inheritDoc} */
    public void printStackTrace() {
        m_throwable.printStackTrace();
    }

    /** {@inheritDoc} */
    public void printStackTrace(PrintStream err) {
        m_throwable.printStackTrace(err);
    }

    /** {@inheritDoc} */
    public void printStackTrace(PrintWriter err) {
        m_throwable.printStackTrace(err);
    }

    /** {@inheritDoc} */
    public String toString() {
        return m_throwable.toString();
    }
}
