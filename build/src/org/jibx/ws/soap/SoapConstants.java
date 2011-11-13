/*
Copyright (c) 2007, Sosnoski Software Associates Limited
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

package org.jibx.ws.soap;

/**
 * Defines constants for SOAP 1.1.
 *
 * @author Nigel Charman
 */
final class SoapConstants
{
    private SoapConstants() {
    }
    
    /** Unqualified SOAP mustUnderstand attribute name. */
    public static final String MUSTUNDERSTAND_NAME = "mustUnderstand";
    /** Value to indicate SOAP mustUnderstand attribute is true. */
    public static final String MUSTUNDERSTAND_TRUE = "1";
    /** Unqualified SOAP Body name. */
    public static final String SOAP_BODYNAME = "Body";
    /** Unqualified SOAP Header name. */
    public static final String SOAP_HEADERNAME = "Header";
    /** Unqualified SOAP Envelope name. */
    public static final String SOAP_ENVNAME = "Envelope";
    /** Array containing single SOAP prefix. */
    public static final String[] SOAP_PREFIX_ARRAY = { SoapConstants.SOAP_PREFIX };
    /** SOAP prefix. */
    public static final String SOAP_PREFIX = "SOAP";
    /** Array containing single SOAP UR!. */
    public static final String[] SOAP_URI_ARRAY = { SoapConstants.SOAP_URI };
    /** SOAP UR! (needs modifying for SOAP 1.2). */
    public static final String SOAP_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    /** Unqualified SOAP Fault name. */
    public static final String SOAP_FAULTNAME = "Fault";
    /** Name of faultcode subelement of SOAP Fault. */
    public static final String FAULTCODE_NAME = "faultcode";
    /** Name of faultstring subelement of SOAP Fault. */
    public static final String FAULTSTRING_NAME = "faultstring";
    /** Name of faultactor subelement of SOAP Fault. */
    public static final String FAULTACTOR_NAME = "faultactor";
    /** Name of detail subelement of SOAP Fault. */
    public static final String FAULTDETAIL_NAME = "detail";
    /** Name of encodingStyle attribute. */
    public static final String ENCODING_STYLE = "encodingStyle";
}
