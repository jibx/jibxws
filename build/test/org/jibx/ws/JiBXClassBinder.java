/*
//Copyright (c) 2007-2008, Sosnoski Software Associates Limited. 
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.jibx.binding.Loader;
import org.jibx.binding.Utility;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;

/**
 * Dynamically processes JiBX binding definitions and outputs the modified class files.
 */
public final class JiBXClassBinder
{
    private static boolean s_bound = false;
    
    // Prevent utility class from being constructed
    private JiBXClassBinder() {
    }
    
    /**
     * Processes the specified binding definition files and outputs the modified class files.  So long as the bound
     * classes have not already been loaded, they will contain the binding information when subsequently used.
     *
     * @param paths the paths to the binding files relative to the classpath
     */
    public static void loadBinding(String[] paths) {
        
        if (!s_bound) {
            try {
                // set paths to be used for loading referenced classes
                URL[] urls = Loader.getClassPaths();
                String[] classpaths = new String[urls.length];
                for (int i = 0; i < urls.length; i++) {
                    classpaths[i] = urls[i].getFile();
                }
                ClassCache.setPaths(classpaths);
                ClassFile.setPaths(classpaths);
                
                // process the binding
                BoundClass.reset();
                MungedClass.reset();
                BindingDefinition.reset();
                
                // find the binding definition(s)
                ClassLoader loader = JiBXClassBinder.class.getClassLoader();
                ArrayList bindings = new ArrayList();
                for (int i = 0; i < paths.length; i++) {
                    String path = paths[i];
                    bindings.add(loadBinding(path, loader));
                }
                
                // get the lists of class names modified, kept unchanged, and unused
                ClassFile[][] lists = MungedClass.fixDispositions();
                
                // add class used list to each binding factory and output files
                for (int i = 0; i < bindings.size(); i++) {
                    ((BindingDefinition)bindings.get(i)).addClassList(lists[0], lists[1]);
                }
                MungedClass.writeChanges();
                s_bound = true;
            } catch (JiBXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static BindingDefinition loadBinding(String path, ClassLoader loader) throws JiBXException, IOException {
        InputStream is = loader.getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Schema binding definition not found");
        }
        
        // extract basic name of binding file from path
        String fname = Utility.fileName(path);
        String sname = fname;
        int split = sname.indexOf('.');
        if (split > 0) {
            sname = sname.substring(0, split);
        }
        sname = BindingDirectory.convertName(sname);
        
        BindingDefinition def = Utility.loadBinding(fname, sname, is, null, false);
        def.generateCode(false);
        return def;
    }
}
