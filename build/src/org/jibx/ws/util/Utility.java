/*
 * Copyright (c) 2004-2007, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.util;

/**
 * Utility methods.
 * 
 * @author Dennis M. Sosnoski
 */
public final class Utility
{
    /**
     * Private constructor, since class is static methods only.
     */
    private Utility() {
    }

    /**
     * Load class. This first tries using the classloader set on the current thread, then tries the classloader that
     * loaded this class.
     * 
     * @param name full package and class name of class to be loaded
     * @return loaded class, or <code>null</code> if class not found
     */
    public static Class loadClass(String name) {
        Class clas = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                clas = loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                /* deliberately empty */
            }
        }
        if (clas == null) {
            try {
                clas = Utility.class.getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                /* deliberately empty */
            }
        }
        return clas;
    }

    /**
     * Returns a string representation of the contents of the specified array. Can be replaced by
     * <code>Arrays.sort(Object[]) </code>, when JiBX/WS is updated to JDK 1.5.
     * 
     * @param objs array to return string representation of
     * @return string representation of array, for example calling this method with an array with 2 elements whose
     * toString() method return "a" and "b" respectively, would return "[a, b]".  If array is null, returns the string
     * "null".
     */
    public static String toString(Object[] objs) {
        if (objs == null) {
            return "null";
        }

        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < objs.length; i++) {
            Object obj = objs[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(obj.toString());
        }
        sb.append(']');
        return sb.toString();
    }
}
