/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import org.roxies.common.ApplicationContext;

/**
 *
 * @author ravigu
 */
public class Server implements Proxy {

    private ServerSocket m_server;

    @Override
    public void start(int port, Boolean secure) {
        try {
            if(secure == null)
                secure=false;
            
            ApplicationContext.initialise(port,secure);
            Executor executor = ApplicationContext.currentContext().getClientThreadPool();

            m_server = ApplicationContext.currentContext().getServerSocket();
            //TODO read timeout from configuration file
            m_server.setSoTimeout(5000);
            Socket socket = null;
            while (!m_server.isClosed()) {
                try {
                    socket = m_server.accept();
                    Connection frontendConn = Connection.getClientConnection(socket);
                    executor.execute(frontendConn);
                } catch (SocketTimeoutException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.FINE, "Socket timed out after 5 seconds");
                }
            }
        } catch (SocketException ex) {
            if (!m_server.isClosed()) //Log only if it's not legitmitate shut down
            {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        try {
            m_server.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error while shutdown", ex);
        }
    }

}
