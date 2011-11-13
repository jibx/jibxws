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

package org.jibx.ws.codec;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Directory of codecs. This uses a property file to relate format names with specific {@link CodecFactory} classes,
 * which are then loaded during class initialization and accessed as needed to handle connections.
 *
 * @author Dennis M. Sosnoski
 */
public final class CodecDirectory
{
    /** Media type short code used for text XML. */
    public static final String TEXT_MEDIA_CODE = "xml";

    /** Media type for "text/xml". */
    public static final MediaType TEXT_XML_MEDIA_TYPE = new MediaType("text", "xml");
    
    /** Properties file path. */
    private static final String CODEC_PROPERTIES_PATH = "/codec.properties";

    /** List of media types in decreasing preference order (nonmodifiable, so safe to pass).*/
    private static final List s_orderedMediaTypes;
    
    /** Map from encoding name to implementation class. */
    private static final Map s_mediaMap;

    /** Hide constructor for utility class. */
    private CodecDirectory() {
    }
    
    // D-TODO: convert codec and transport versions to common
    static {
        InputStream in = null;
        try {
            final List mediaTypes = new ArrayList();
            Properties props = new Properties() {

                // add keys in order processed to media list
                public synchronized Object put(Object arg0, Object arg1) {
                    try {
                        mediaTypes.add(new MediaType((String)arg0).freeze());
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Invalid media type '" + arg0 + "' in properties file '" 
                            + CODEC_PROPERTIES_PATH + "'");
                    }
                    return super.put(arg0, arg1);
                }

            };
            in = CodecDirectory.class.getResourceAsStream(CODEC_PROPERTIES_PATH);
            if (in == null) {
                throw new RuntimeException("Unable to load required properties file '" + CODEC_PROPERTIES_PATH + '\'');
            }
            props.load(in);
            s_orderedMediaTypes = Collections.unmodifiableList(mediaTypes);
            s_mediaMap = new HashMap();
            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String mediatype = (String)iter.next();
                String classname = props.getProperty(mediatype);
                try {
                    Class clas = CodecDirectory.class.getClassLoader().loadClass(classname);
                    if (CodecFactory.class.isAssignableFrom(clas)) {
                        s_mediaMap.put(mediatype.toLowerCase(), clas.newInstance());
                    } else {
                        throw new IllegalStateException("Class " + classname + ", specified for format '" + mediatype
                            + "', is not an org.jibx.ws.encoding.CodecFactory implementation");
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Unable to load encoding class " + classname);
                } catch (InstantiationException e) {
                    throw new IllegalStateException("Error creating an instance of encoding class " + classname);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to create an instance of encoding class " + classname);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load required properties file '" + CODEC_PROPERTIES_PATH + '\'');
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
    };

    /**
     * Get the list of media types in the directory. The returned list is unmodifiable.
     *
     * @return list of {@link MediaType}
     */
    public static List getMediaTypes() {
        return s_orderedMediaTypes;
    }

    /**
     * Get codec instance.
     *
     * @param mediaType media type
     * @return instance, or <code>null</code> if not found
     */
    public static XmlCodec getCodec(MediaType mediaType) {
        String key = getCodecKey(mediaType);
        CodecFactory factory = (CodecFactory)s_mediaMap.get(key);
        if (factory == null) {
            return null;
        } else {
            return factory.createInstance(mediaType);
        }
    }

    /**
     * Returns a key that can be used to uniquely identify a codec. Currently, this strips the parameter name from
     * the media type.  The result of this method may change in the future if different codecs are implemented 
     * dependent on the media type parameters.
     *
     * @param mediaType media type, must be non-null
     * @return key to identify codec
     */
    public static String getCodecKey(MediaType mediaType) {
        return mediaType.getBaseType();
    }

    /**
     * Returns whether there is a codec available for the specified media type, ignoring any parameters of the
     * MediaType.
     * 
     * @param type media type
     * @return <code>true</code> if a codec is available, <code>false</code> otherwise
     */
    public static boolean hasCodecFor(MediaType type) {
        return s_mediaMap.containsKey(type.getBaseType());
    }

    /**
     * Finds a media type that is supported for the accept types in the given <code>acceptString</code>.
     * <p>  
     * If <code>acceptString</code> is <code>null</code>, then it is assumed that the client accepts all media types 
     * (as per RFC2616) and this method returns:
     * <ul>
     * <li>the <code>contentType</code> of the request, assuming there is a codec available for the 
     * <code>contentType</code>,</li>
     * <li>otherwise the <i>most preferred</i> media type is returned.</li>
     * </ul>
     * <p>
     * Otherwise, <code>acceptString</code> contains one or more media-ranges that are comma separated, in which case 
     * the media-ranges are checked in the following order:
     * <ol>
     * <li>All media-ranges that reference specific media types ("type/subtype") are checked for matches with supported 
     * media types. The <i>most preferred</i> media type that matches any of the media-ranges is returned.</li>
     * <li>Otherwise, all media-ranges that specify all subtypes of a particular type ("type/*") are checked for 
     * matches with supported media types. The <i>most preferred</i> media type that matches any of the media-ranges is 
     * returned.</li>
     * <li>Otherwise, if a media-range specifies all media types (*<!-- -->/*), this method returns:
     *   <ul>
     *   <li>the <code>contentType</code> of the request, assuming there is a codec available for the 
     *   <code>contentType</code>,</li>
     *   <li>otherwise the <i>most preferred</i> media type is returned.</li>
     *   </ul>
     * </li>
     * <li>If no matches are found, <code>null</code> is returned.</li>
     * </ol> 
     * <p>
     * The <i>most preferred</i> type is determined by checking against the list of supported media types in 
     * decreasing preference order.   
     * <p>
     * Note that all media type parameters are ignored.  In particular the quality factor "q" parameter is ignored. 
     *
     * @param acceptString media type string (e.g.  "application/soap+xml" or "text/xml; charset=utf-8, text/*, 
     * *<!-- -->/*"")
     * @param contentType the media type that has the highest preference 
     * @return the most preferred media type that is allowed by the <code>acceptString</code> argument, or 
     * <code>null</code> if no media types are allowed by the <code>acceptString</code> argument  
     * @throws ParseException if <code>acceptString</code> cannot be parsed
     */
    public static MediaType getAcceptableMediaType(String acceptString, MediaType contentType) throws ParseException {
        if (acceptString == null) {
            if (CodecDirectory.hasCodecFor(contentType)) {
                return contentType;
            }
            return (MediaType) s_orderedMediaTypes.get(0);
        }
        
        MediaType[] acceptTypes = parseAcceptString(acceptString);

        MediaType match = findSpecificTypeMatch(acceptTypes);
        if (match != null) {
            return match;
        }

        match = findTypeRangeMatch(acceptTypes);
        if (match != null) {
            return match;
        }

        if (containsMatchAll(acceptTypes)) {
            if (CodecDirectory.hasCodecFor(contentType)) {
                return contentType;
            }
            return (MediaType) s_orderedMediaTypes.get(0);
        }
        
        return null;
    }

    private static MediaType[] parseAcceptString(String acceptString) throws ParseException {
        StringTokenizer toke = new StringTokenizer(acceptString, ",");
        MediaType[] acceptTypes = new MediaType[toke.countTokens()];
        int i = 0;
        while (toke.hasMoreTokens()) {
            acceptTypes[i++] = new MediaType(toke.nextToken(), true);
        }
        return acceptTypes;
    }

    private static MediaType findSpecificTypeMatch(MediaType[] acceptTypes) {
        for (Iterator iterator = s_orderedMediaTypes.iterator(); iterator.hasNext();) {
            MediaType mediaType = (MediaType) iterator.next();
            for (int i = 0; i < acceptTypes.length; i++) {
                MediaType acceptType = acceptTypes[i];
                if (!acceptType.getSubType().equals("*")) {
                    if (acceptType.getSubType().equals(mediaType.getSubType()) 
                        && acceptType.getPrimaryType().equals(mediaType.getPrimaryType())) {
                        return mediaType;
                    }
                }
            }
        }
        return null;
    }

    private static MediaType findTypeRangeMatch(MediaType[] acceptTypes) {
        for (Iterator iterator = s_orderedMediaTypes.iterator(); iterator.hasNext();) {
            MediaType mediaType = (MediaType) iterator.next();
            for (int i = 0; i < acceptTypes.length; i++) {
                MediaType acceptType = acceptTypes[i];
                if (acceptType.getSubType().equals("*") && !acceptType.getPrimaryType().equals("*")) {
                    if (acceptType.getPrimaryType().equals(mediaType.getPrimaryType())) {
                        return mediaType;
                    }
                }
            }
        }
        return null;
    }

    private static boolean containsMatchAll(MediaType[] acceptTypes) {
        for (int i = 0; i < acceptTypes.length; i++) {
            MediaType acceptType = acceptTypes[i];
            if (acceptType.getSubType().equals("*") && acceptType.getPrimaryType().equals("*")) {
                return true;
            }
        }
        return false;
    }
}
