/*
 * Copyright (c) 2007, Sosnoski Software Associates Limited. All rights reserved.
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

package org.jibx.ws.tcp.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.ws.server.ServiceDefinition;

/**
 * Server implementation for TCP protocol using DIME message exchange. A separate instance of this class is created for
 * each service. As connections are made to the service socket an instance of the {@link SocketRunner} class and an
 * associated runner thread is created for each accepted connection.
 *
 * @author Dennis M. Sosnoski
 */
public final class TcpServer extends Thread
{
    private static final Log s_logger = LogFactory.getLog(TcpServer.class);

    /** Socket to be handled. */
    private final ServerSocket m_socket;

    /** Service accessed by this socket. */
    private final ServiceDefinition m_service;

    /** Service exit flag. */
    private boolean m_exit;

    /** First spawned runner (<code>null</code> if none). */
    private SocketRunner m_head;

    /** Last spawned runner (<code>null</code> if none). */
    private SocketRunner m_tail;

    /**
     * Constructor.
     *
     * @param socket
     * @param service
     */
    private TcpServer(ServerSocket socket, ServiceDefinition service) {
        m_socket = socket;
        m_service = service;
    }

    /**
     * Thread execution method. The execution loop is simple, consisting of the thread waiting for an incoming
     * connection and then spawning off a separate thread and a {@link SocketRunner} instance for each accepted
     * connection.
     */
    public void run() {
        while (true) {
            try {
                SocketRunner runner = new SocketRunner(m_socket.accept(), m_service, this);
                synchronized (this) {
                    if (m_head == null) {
                        m_head = m_tail = runner;
                    } else {
                        runner.m_last = m_tail;
                        m_tail.m_next = runner;
                        m_tail = runner;
                    }
                }
                Thread thread = new Thread(runner);
                thread.start();
            } catch (IOException e) {
                // log and ignore, unless it is expected
                if (m_exit) {
                    break;
                } else {
                    s_logger.error("Error on server socket for service " + m_service.getServiceName(), e);
                }
            }
        }
        synchronized (this) {
            while (!m_exit) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // expected
                }
            }
            for (SocketRunner runner = m_head; runner != null; runner = runner.m_next) {
                runner.setExit(true);
            }
        }
    }

    /**
     * Set thread exit flag. This also terminates the socket connection if called with value <code>true</code>.
     *
     * @param exit <code>true</code> if thread is to exit, <code>false</code> otherwise
     */
    private synchronized void setExit(boolean exit) {
        m_exit = exit;
        if (exit) {
            try {
                m_socket.close();
            } catch (IOException e) {
                // nothing to be done if this fails
            }
            notify();
        }
    }

    /**
     * Unlink a runner from the active list. This should only be called when the associated socket has been closed and
     * the runner is existing.
     *
     * @param runner runner to unlink
     */
    synchronized void unlink(SocketRunner runner) {
        if (runner == m_head) {
            if (runner == m_tail) {
                m_head = m_tail = null;
            } else {
                m_head = runner.m_next;
                m_head.m_last = null;
            }
        } else {
            runner.m_last.m_next = runner.m_next;
            if (runner == m_tail) {
                m_tail = runner.m_last;
            } else {
                runner.m_next.m_last = runner.m_last;
            }
        }
    }

    /**
     * Main method used to run the server. This takes arguments of the form 'def-path=port#', where 'def-path' is the
     * path to the service definition file and 'port#' is the corresponding service port number.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        
        // make sure there's at least one port specified
        if (args.length == 0) {
            System.err.println("Need at least one service definition and port number parameter");
            System.exit(1);
        }

        // set up JiBX unmarshalling for service configuration files
        IUnmarshallingContext ctx = null;
        try {
            ctx = BindingDirectory.getFactory(ServiceDefinition.class).createUnmarshallingContext();
        } catch (JiBXException e) {
            System.err.println("Unable to initialize unmarshalling: " + e.getMessage());
        }

        // parse all command line arguments and unmarshal the service definitions
        ServiceDefinition[] sdefs = new ServiceDefinition[args.length];
        int[] ports = new int[args.length];
        boolean valid = true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            int split = arg.indexOf('=');
            String path = arg.substring(0, split);
            if (split > 0) {
                InputStream is = null;
                try {
                    ports[i] = Integer.parseInt(arg.substring(split + 1));
                    is = new FileInputStream(path);
                    sdefs[i] = (ServiceDefinition)ctx.unmarshalDocument(is, null);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing port number in argument " + i + ": " + arg);
                    valid = false;
                } catch (FileNotFoundException e) {
                    System.err.println("Service definition file not found for argument " + i + ": " + path);
                    valid = false;
                } catch (JiBXException e) {
                    System.err.println("Error unmarshalling service definition " + i + " (" + path + "): " 
                        + e.getMessage());
                    valid = false;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            } else {
                System.err.println("Missing required '=' in argument " + i + ": " + arg);
                valid = false;
            }
        }
        if (valid) {

            // start the actual server sockets and threads
            TcpServer[] servers = new TcpServer[ports.length];
            for (int i = 0; i < ports.length; i++) {
                try {
                    ServerSocket socket = new ServerSocket(ports[i]);
                    servers[i] = new TcpServer(socket, sdefs[i]);
                    Thread thread = new Thread(servers[i]);
                    thread.start();
                } catch (IOException e) {
                    System.err.println("Error opening socket on port " + ports[i] + " for service " 
                        + sdefs[i].getServiceName());
                }
            }

            // wait for user input to trigger shutdown
            System.out.println("Type enter when ready to shutdown servers.");
            try {
                System.in.read();
            } catch (IOException ex) {
            }

            // close all connections and shutdown all services
            for (int i = 0; i < servers.length; i++) {
                if (servers[i] != null) {
                    servers[i].setExit(true);
                }
            }
        } else {
            System.exit(2);
        }
    }
}
