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

import java.util.HashMap;

import org.jibx.ws.WsException;
import org.jibx.ws.util.ExpandingPool;

/**
 * Pools {@link Service}s. The {@link #getInstance(ServiceFactory, ServiceDefinition)} method returns a Service object
 * from the pool (creating one if necessary), and calls the {@link Service#setOwningPool(ExpandingPool)} method on the 
 * service. Once the service has completed processing, it must call {@link ExpandingPool#releaseInstance(Object)} on its
 * owning pool to release itself.
 * 
 * @author Dennis Sosnoski
 */
public final class ServicePool
{
    /** Hide constructor. */
    private ServicePool() {
    }
    
    /** Map from service definition to service pool. */
    private static HashMap s_factoryMap = new HashMap();

    /**
     * Get a service instance for a specific service. This will either get an available instance from the pool, or
     * create a new instance if none are currently available.
     * 
     * @param sfac factory for creating Service
     * @param sdef service definition for mapper to be returned
     * @return SOAP mapper instance for service
     * @throws WsException on error creating the service
     */
    public static Service getInstance(final ServiceFactory sfac, final ServiceDefinition sdef) throws WsException {
        ExpandingPool pool;
        synchronized (s_factoryMap) {
            pool = (ExpandingPool) s_factoryMap.get(sdef);
            if (pool == null) {
                pool = new ExpandingPool() {
                    protected Object createInstance() throws WsException {
                        Service service = sfac.createInstance(sdef);
                        service.setOwningPool(this);
                        return service;
                    }
                };
                s_factoryMap.put(sdef, pool);
            }
        }
        synchronized (pool) {
            return (Service) pool.getInstance();
        }
    }
}
