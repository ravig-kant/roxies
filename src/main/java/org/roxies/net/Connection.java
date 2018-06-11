/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author ravigu
 */
public interface Connection extends Runnable {

    public void connect();

    public void use(Socket socket);

    public void setlClientStream(InputStream stream);

    public InputStream getBackendStream();
    
    public void setFrontendConnection(Connection conn);

    public static Connection getClientConnection(Socket socket) {
        return new FrontendConnection(socket);
    }
}
