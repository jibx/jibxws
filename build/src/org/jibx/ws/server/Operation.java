/*
 * Copyright (c) 2008, Sosnoski Software Associates Limited. All rights reserved.
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
 * INCLUDING, BUT NOT Lm_outputClassIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jibx.ws.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jibx.runtime.JiBXException;
import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.WsException;
import org.jibx.ws.context.InContext;
import org.jibx.ws.context.MessageContext;
import org.jibx.ws.context.OutContext;
import org.jibx.ws.process.Processor;

/**
 * An operation that can be invoked by a service. Each {@link Service} is associated with a Java class. Each
 * {@link Operation} for the <code>Service</code> corresponds to a method of that Java class.
 * <p>
 * The static {@link #newInstance(Class, OperationDefinition)} method constructs the <code>Operation</code> by
 * determining which method within the service class matches the specified {@link OperationDefinition}.
 * 
 * @author Nigel Charman
 */
public final class Operation
{
//    private final String m_operationName;
//    private final String m_inputMessageName;
//    private final String m_outputMessageName;
    private final Method m_method;
    private final Class m_inputClass;
    private final Class m_outputClass;
    private final ParamIndices m_idxs;

    /**
     * Constructs an {@link Operation} that corresponds to the method of the specified <code>serviceClass</code> that
     * matches the specified {@link OperationDefinition}.
     * <p>
     * The operation is linked to the method in the service class where:
     * <ul>
     * <li>the method name is the same as {@link OperationDefinition#getMethodName()}, and</li>
     * <li>if {@link OperationDefinition#getInputClassName()} is <code>non-null</code>, the type of the first
     * parameter of the method must match this value, and</li>
     * <li>if {@link OperationDefinition#getOutputClassName()} is <code>non-null</code>, the return type of the
     * method must match this value.</li>
     * <li>The method may have an additional parameter of type {@link InContext}, or</li>
     * <li>The method may have 2 additional parameters of type {@link InContext} and {@link OutContext}.</li>
     * </ul>
     * 
     * @param serviceClass the class that contains the method to be associated with the <code>Operation</code>
     * @param opdef the definition of the <code>Operation</code>
     * @return a new <code>Operation</code>
     * @throws WsConfigurationException if no method in the <code>serviceClass</code> matches the <code>opdef</code>
     */
    public static Operation newInstance(Class serviceClass, OperationDefinition opdef) throws WsConfigurationException {
        String methodName = opdef.getMethodName();
        if (methodName == null) {
            throw new IllegalArgumentException("Method Name must be set on operation definition");
        }

        Method[] methods = serviceClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methodName.equals(methods[i].getName())) {
                Method method = methods[i];
                ParamIndices idxs = matchSignature(method, opdef.getInputClassName(), opdef.getOutputClassName());
                if (idxs != null) {
                    return new Operation(opdef, method, idxs);
                }
            }
        }

        throw new WsConfigurationException("Method " + methodName + " not found in " + serviceClass.getName()
            + " with expected signature");
    }

    /**
     * Construct the Operation.
     * 
     * @param opdef the definition of the operation
     * @param method the method to be associated with the Operation
     * @param idxs specifies the indices of the method parameters
     */
    private Operation(OperationDefinition opdef, Method method, ParamIndices idxs) {
        m_method = method;
        m_idxs = idxs;

        Class[] params = method.getParameterTypes();
        m_inputClass = (params.length == 0) ? null : params[0];

        Class result = method.getReturnType();
        m_outputClass = (result == void.class) ? null : result;

//        m_operationName = (opdef.getOperationName() != null) ? opdef.getOperationName() : method.getName();
//
//        if (opdef.getInputMessageName() != null) {
//            m_inputMessageName = opdef.getInputMessageName();
//        } else {
//            if (m_inputClass != null) {
//                m_inputMessageName = splitClassName(m_inputClass.getName());
//            } else {
//                m_inputMessageName = null;
//            }
//        }
//        if (opdef.getOutputMessageName() != null) {
//            m_outputMessageName = opdef.getOutputMessageName();
//        } else {
//            if (m_outputClass != null) {
//                m_outputMessageName = splitClassName(m_outputClass.getName());
//            } else {
//                m_outputMessageName = null;
//            }
//        }
    }

    /**
     * Invokes the method that is associated with this operation, passing the optional payload as a parameter. If the
     * method definition includes {@link InContext} or {@link OutContext} parameters, the current contexts are retrieved
     * from the specified {@link Processor}.
     * 
     * @param serviceObj the current Service object, on which to invoke the method. If the method is static, this
     * parameter may be set to <code>null</code>. For non-static methods, the parameter must be <code>non-null</code>.
     * @param payload the object to pass as an input parameter, or <code>null</code> if the method does not have an
     * input parameter.
     * @param processor the current processor. Must be <code>non-null</code> if the method definition includes
     * {@link InContext} or {@link OutContext} parameters.
     * @return the return value from the method, or <code>null</code> for void methods.
     * @throws InvocationTargetException wraps an exception thrown by the method that was invoked
     * @throws WsException if the processor is in an invalid state to provide <code>InContext</code> or
     * <code>OutContext</code> parameters
     */
    public Object invoke(Object serviceObj, Object payload, Processor processor) throws InvocationTargetException,
        WsException {

        Object[] args = null;

        if (m_idxs.m_paramCount > 0) {
            args = new Object[m_idxs.m_paramCount];
            if (m_idxs.m_payloadIndex != -1) {
                args[m_idxs.m_payloadIndex] = payload;
            }
            if (m_idxs.m_inContextIndex != -1) {
                MessageContext currentContext = processor.getCurrentMessageContext();
                if (!(currentContext instanceof InContext)) {
                    throw new WsException(
                        "Error - attempt to invoke operation when current context is not an InContext");
                }
                args[m_idxs.m_inContextIndex] = currentContext;
            }
            if (m_idxs.m_outContextIndex != -1) {
                MessageContext nextContext = processor.getNextMessageContext();
                if (!(nextContext instanceof OutContext)) {
                    throw new WsException(
                        "Error - attempt to invoke 2-way operation when next message context is not an OutContext");
                }
                args[m_idxs.m_outContextIndex] = nextContext;
            }
        }

        try {
            return m_method.invoke(serviceObj, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Operation does not have access to method that was visible on construction - " + e.getMessage());
        }
    }

    /**
     * Check for signature match with method. If the signature matches, this saves the method information.
     * 
     * @throws JiBXException on nesting error
     */
    private static ParamIndices matchSignature(Method method, String inputClassName, String outputClassName) {
        ParamIndices idxs = new ParamIndices();
        Class[] params = method.getParameterTypes();
        if (params.length == 0) {
            if (inputClassName != null) {
                return null;
            }
        } else {
            if (inputClassName == null || inputClassName.equals(params[0].getName())) {
                idxs.m_payloadIndex = 0;
            } else {
                return null;
            }
            if (params.length > 1) {
                if (params[1] == InContext.class) {
                    idxs.m_inContextIndex = 1;
                } else {
                    return null;
                }
                if (params.length > 2) {
                    if (params[2] == OutContext.class) {
                        idxs.m_outContextIndex = 2;
                    } else {
                        return null;
                    }
                    if (params.length > 3) {
                        return null;
                    }
                }
            }

        }
        if (outputClassName != null) {
            if (!outputClassName.equals(method.getReturnType().getName())) {
                return null;
            }
        }
        idxs.m_paramCount = params.length;

        return idxs;
    }

//    /**
//     * Get simple class name from fully-qualified class name.
//     * 
//     * @param full fully-qualified class name
//     * @return class name without package information
//     */
//    private String splitClassName(String full) {
//        int split = full.lastIndexOf('.');
//        if (split >= 0) {
//            return full.substring(split + 1);
//        } else {
//            return full;
//        }
//    }

    /**
     * Returns the class of the input parameter to the method, or <code>null</code> if there is no input parameter.
     * The <code>payload</code> passed to the {@link #invoke(Object, Object, Processor)} method must be of this class.
     * 
     * @return inputClass
     */
    public Class getInputClass() {
        return m_inputClass;
    }

    /**
     * Returns the class of the return value of the method, or <code>null</code> if it has a <code>void</code>
     * return type. The {@link #invoke(Object, Object, Processor)} method will return an object of this type.
     * 
     * @return outputClass
     */
    public Class getOutputClass() {
        return m_outputClass;
    }

//    /**
//     * Returns the name of the operation. This will be set to the value from
//     * {@link OperationDefinition#getOperationName()} if <code>non-null</code>, or to the method name otherwise.
//     * 
//     * @return operation name
//     */
//    public String getOperationName() {
//        return m_operationName;
//    }
//
//    /**
//     * Returns the name of the input message. This will be set to the value from
//     * {@link OperationDefinition#getInputMessageName()} if <code>non-null</code>, or to the class name of the input
//     * parameter otherwise (omitting the package name). If there is no input parameter, this method will return
//     * <code>null</code>.
//     * 
//     * @return input message name
//     */
//    public String getInputName() {
//        return m_inputMessageName;
//    }
//
//    /**
//     * Returns the name of the output message. This will be set to the value from
//     * {@link OperationDefinition#getOutputMessageName()} if <code>non-null</code>, or to the class name of the
//     * method's return value otherwise (omitting the package name). If the method has a <code>void</code> return type,
//     * this method will return <code>null</code>.
//     * 
//     * @return output message name
//     */
//    public String getOutputName() {
//        return m_outputMessageName;
//    }

    /**
     * Returns whether the associated method is static.
     *
     * @return <code>true</code> if the method is static, <code>false</code> otherwise.
     */
    public boolean isStaticMethod() {
        int mods = m_method.getModifiers();
        return Modifier.isStatic(mods);
    }

    /** {@inheritDoc} */
    public String toString() {
        return m_method.toString();
    }
    
    /** Stores the indices of the parameters to the method. */
    private static final class ParamIndices
    {
        private ParamIndices() {
        }
        
        /** The number of parameters to the method. */
        private int m_paramCount;
        
        /** The index of the payload (input) parameter, or -1 if there is no payload parameter. */
        private int m_payloadIndex = -1;

        /** The index of the {@link InContext} parameter, or -1 if there is no {@link InContext} parameter. */
        private int m_inContextIndex = -1;

        /** The index of the {@link OutContext} parameter, or -1 if there is no {@link OutContext} parameter. */
        private int m_outContextIndex = -1;
    }
}
