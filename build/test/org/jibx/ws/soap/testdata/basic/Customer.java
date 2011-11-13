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

package org.jibx.ws.soap.testdata.basic;

/**
 * A customer object for use in testing.
 * 
 * @author Nigel Charman
 */
public class Customer
{
    public Person person;

    public String street;

    public String city;

    public String state;

    public Integer zip;

    public String phone;

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Customer other = (Customer)obj;
        if (city == null) {
            if (other.city != null) {
                return false;
            }
        } else if (!city.equals(other.city)) {
            return false;
        }
        if (person == null) {
            if (other.person != null) {
                return false;
            }
        } else if (!person.equals(other.person)) {
            return false;
        }
        if (phone == null) {
            if (other.phone != null) {
                return false;
            }
        } else if (!phone.equals(other.phone)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (street == null) {
            if (other.street != null) {
                return false;
            }
        } else if (!street.equals(other.street)) {
            return false;
        }
        if (zip == null) {
            if (other.zip != null) {
                return false;
            }
        } else if (!zip.equals(other.zip)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((city == null) ? 0 : city.hashCode());
        result = PRIME * result + ((person == null) ? 0 : person.hashCode());
        result = PRIME * result + ((phone == null) ? 0 : phone.hashCode());
        result = PRIME * result + ((state == null) ? 0 : state.hashCode());
        result = PRIME * result + ((street == null) ? 0 : street.hashCode());
        result = PRIME * result + ((zip == null) ? 0 : zip.hashCode());
        return result;
    }
}
