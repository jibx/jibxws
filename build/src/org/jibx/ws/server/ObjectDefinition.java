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

package org.jibx.ws.server;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.jibx.ws.WsConfigurationException;
import org.jibx.ws.io.handler.InHandler;
import org.jibx.ws.io.handler.OutHandler;
import org.jibx.ws.util.Utility;

/**
 * A base class for all definition classes that require an object to be instantiated.  Two options are provided for
 * defining the object:
 * <ul>
 * <li>use {@link #setClassName(String)} and {@link #setArgs(String[])} for declarative definition, or</li>
 * <li>use {@link #setDefinedObject(Object)} for programatic definition (or when using dependency injection framework)
 * </li>
 * </ul>
 * <p>
 * If the declarative definition is used, the {@link #init()} method must be called after the arguments have been set. 
 * 
 * @author Nigel Charman
 */
public abstract class ObjectDefinition
{
    /** Defined object. If this is set, the defined class and args are ignored. */
    private Object m_definedObject;
    /** Defined class name. */
    private String m_className;
    /** Constructor arguments for defined class. */
    private String[] m_args;
    
    /** A description of the type of object, eg "handler". */
    private String m_typeDesc;

    /** The class object for m_className. */
    private Class m_definedClass;
    /** The constructor required to construct m_className with arguments m_args. */
    private Constructor m_constructor;

    /**
     * Constructor.
     * 
     * @param typeDesc a description of the type of object, eg "handler"
     */
    ObjectDefinition(String typeDesc) {
        m_typeDesc = typeDesc;
    }
    
    /**
     * Method called after configuration is complete. This validates that either an object has been defined (using
     * {@link #setDefinedObject(Object)}), or that the defined class is available with  a constructor matching the 
     * defined arguments.  Only string arguments are supported for this constructor, and the number of arguments must
     * match the length of the array passed to {@link #setArgs(String[])}.
     * 
     * @throws WsConfigurationException if the class is not accessible or is not a handler class
     */
    public final void init() throws WsConfigurationException {
        if (m_definedObject == null) {
            m_definedClass = loadClass(m_className);
            m_constructor = getConstructor(m_definedClass, m_args);
        }
        
        postInit();
    }

    private Class loadClass(String className) throws WsConfigurationException {
        if (className == null) {
            throw new WsConfigurationException("Definition must contain the " + m_typeDesc + " class name.");
        }
        Class clazz = Utility.loadClass(className);
        if (clazz == null) {
            throw new WsConfigurationException("Class " + className + " not found in classpath");
        }
        return clazz;
    }

    private Constructor getConstructor(Class clazz, String[] args) throws WsConfigurationException {
        int argCount = (args == null) ? 0 : args.length;
        Class[] paramTypes = new Class[argCount];
        Arrays.fill(paramTypes, String.class);
        
        try {
            return clazz.getConstructor(paramTypes);
        } catch (SecurityException e) {
            throw new WsConfigurationException("Error getting constructor for " + m_typeDesc + " class '" 
                + clazz + "' with arguments " + Utility.toString(args) + ".", e);
        } catch (NoSuchMethodException e) {
            throw new WsConfigurationException("Unable to find constructor for " + m_typeDesc + " class '" 
                + clazz + "' with arguments " + Utility.toString(args) + ".", e);
        }
    }
    /**
     * Perform any post-initialization tasks specific to sub classes. 
     * @throws WsConfigurationException on configuration error 
     */
    protected abstract void postInit() throws WsConfigurationException;

    
    /**
     * Sets an object to be used, rather than defining a class and arguments. 
     * <p>
     * If this is set to a non <code>null</code> value, the handler class and args do not need
     * to be set, and will be ignored. If set, the object must be thread safe.
     * <p>
     * 
     * @param obj object
     */
    protected final void setDefinedObject(Object obj) {
        m_definedObject = obj;
    }

    /**
     * Get object to be used.
     * 
     * @return handler object, or <code>null</code> if no handler object set
     */
    protected final Object getDefinedObject() {
        return m_definedObject;
    }

    /**
     * Set handler class name. Only required if {@link #setDefinedObject(Object)} has not been called. The specified
     * class must implement either the {@link InHandler} or {@link OutHandler} interface. A new instance of the
     * specified handler class will be constructed for each service instance. To pass arguments to the construction of
     * this class call the {@link #setArgs(String[])} method.
     * 
     * @param className handler class name
     */
    public final void setClassName(String className) {
        m_className = className;
    }

    /**
     * Get handler class name.
     * 
     * @return handler class name
     */
    protected final String getClassName() {
        return m_className;
    }

    /**
     * Set arguments for construction of the specified handler class. If this method is not called, the handler will be
     * constructed using a no-args constructor of the specified handler class.
     * 
     * @param args arguments to construct handler with
     */
    public final void setArgs(String[] args) {
        m_args = args;
    }

    /**
     * Returns the defined object (if one has been defined), or otherwise a new instance of the declared class using
     * the declared arguments.
     *
     * @return object 
     * @throws WsConfigurationException if a new instance cannot be created 
     */
    public final Object getObject() throws WsConfigurationException {
        if (getDefinedObject() != null) {
            return getDefinedObject();
        } 
        
        try {
            return m_constructor.newInstance(m_args);
        } catch (Exception e) {
            throw new WsConfigurationException("Error constructing " + m_typeDesc + " class '" 
                + m_className + "' with arguments " + Utility.toString(m_args) + ".", e);
        }
        
    }

    /**
     * Gets the Class object for the class that has been defined using {@link #setClassName(String)}. 
     *
     * @return definedClass the class 
     */
    protected final Class getDefinedClass() {
        return m_definedClass;
    }
}
