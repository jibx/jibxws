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
package org.jibx.ws.server;

import org.jibx.ws.context.InContext;
import org.jibx.ws.context.OutContext;

/**
 * Defines an operation that is available for a service. <!--In addition to defining the method that is to be invoked, 
 * this
 * class includes optional properties that are used for WSDL generation. -->Operations to be used for a service are set
 * using {@link ServiceDefinition#setOperationDefinitions(java.util.List)}.
 * <p>
 * The method name supplied in this definition must match the name of an accessible method of the service class (which
 * is defined in the enclosing {@link ServiceDefinition}). If multiple methods are defined with this method name, the
 * names of the input and/or output classes must be supplied to differentiate the method.
 * <p>
 * Presently, at most one input class is supported, since wrapped document-literal services are not supported.
 * <p>
 * The method may optionally have an {@link InContext} parameter as the last parameter, or {@link InContext} and
 * {@link OutContext} parameters as the last 2 parameters. These additional parameters must not be defined in the 
 * OperationDefinition.
 * <p>
 * The optional input and output classes of the method must have bindings defined so that they can be unmarshalled and
 * marshalled (respectively) by JiBX.
 * <p>
 * When using JiBX to configure the service, this object is populated from the XML service definition document by JiBX
 * unmarshalling.
 * 
 * @author Dennis M. Sosnoski
 */
public final class OperationDefinition
{
//    /** SOAP action name (optional, empty if not defined). */
//    private String m_soapAction = "";

    /** Method name. */
    private String m_methodName;

//    /** Operation name (optional, will be set to the same as method name if not defined). */
//    private String m_operationName;

    /** Input message class name, will be determined from method signature if not supplied. */
    private String m_inputClassName;

//    /** Input message name, will be derived from class name if not supplied. */
//    private String m_inputMessageName;

    /** Output message class name, will be determined from method signature if not supplied. */
    private String m_outputClassName;

//    /** Output message name, will be derived from class name if not supplied. */
//    private String m_outputMessageName;

//    /**
//     * Sets the value of the SOAP action header for this operation. Optional. If a SOAPAction header is present on a
//     * request it must match this value or an error is returned. By default, SOAPAction values are left empty and are
//     * not included in generated WSDL, since they effectively add no useful information for doc/lit services.
//     * 
//     * @param soapAction the SOAPAction header value to match against
//     */
//    public void setSoapAction(String soapAction) {
//        m_soapAction = soapAction;
//    }
//
//    /**
//     * Get SOAP action.
//     * 
//     * @return SOAP action
//     */
//    public String getSoapAction() {
//        return m_soapAction;
//    }

    /**
     * Sets the name of the method that is to be invoked for this operation.
     * 
     * @param methodName the name of the method
     */
    public void setMethodName(String methodName) {
        m_methodName = methodName;
    }

    /**
     * Get methodName.
     *
     * @return methodName
     */
    public String getMethodName() {
        return m_methodName;
    }

//    /**
//     * Set the name of the operation for use in the WSDL definition. If not set, this will default to the method name.
//     * 
//     * @param operationName operation name
//     */
//    public void setOperationName(String operationName) {
//        m_operationName = operationName;
//    }
//
//    /**
//     * Get operationName.
//     *
//     * @return operationName
//     */
//    public String getOperationName() {
//        return m_operationName;
//    }

    /**
     * Sets the name of the input class. This method only needs to be called if multiple methods exist with the
     * specified method name.
     * 
     * @param inputClassName the name of the input class
     */
    public void setInputClassName(String inputClassName) {
        m_inputClassName = inputClassName;
    }

    /**
     * Get input class name.
     * 
     * @return input class name
     */
    public String getInputClassName() {
        return m_inputClassName;
    }

//    /**
//     * Set the name of the input message for use in the WSDL definition. If not set, this will default to the class 
//     * name of the input class, with package prefixes removed.
//     * 
//     * @param inputMessageName the input message name
//     */
//    public void setInputMessageName(String inputMessageName) {
//        m_inputMessageName = inputMessageName;
//    }
//
//    /**
//     * Get inputMessageName.
//     *
//     * @return inputMessageName
//     */
//    public String getInputMessageName() {
//        return m_inputMessageName;
//    }
//
    /**
     * Sets the name of the output (return) class. This method only needs to be called if multiple methods exist with
     * the specified method name.
     * 
     * @param outputClassName the name of the output class
     */
    public void setOutputClassName(String outputClassName) {
        m_outputClassName = outputClassName;
    }

    /**
     * Get output class name.
     * 
     * @return output class name
     */
    public String getOutputClassName() {
        return m_outputClassName;
    }

//    /**
//     * Set the name of the output message for use in the WSDL definition. If not set, this will default to the class
//     * name of the output class, with package prefixes removed.
//     * 
//     * @param outputMessageName the output message name
//     */
//    public void setOutputMessageName(String outputMessageName) {
//        m_outputMessageName = outputMessageName;
//    }
//
//    /**
//     * Get outputMessageName.
//     *
//     * @return outputMessageName
//     */
//    public String getOutputMessageName() {
//        return m_outputMessageName;
//    }
}
