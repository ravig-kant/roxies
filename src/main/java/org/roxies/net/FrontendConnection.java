/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.roxies.common.ApplicationContext;

/**
 *
 * @author ravigu
 */
public class FrontendConnection implements Connection{
    
    private Socket m_client_socket = null;

    FrontendConnection(Socket socket) {
        m_client_socket = socket;
    }

    @Override
    public void run() {
        connect();
    }

    @Override
    public void connect() {
        OutputStream cos = null;
        try {
            if(m_client_socket == null)
                throw new IllegalStateException("Provide socket before connecting");
            
            Connection backendConnection = ApplicationContext.currentContext().getBackendConnection();
            backendConnection.setlClientStream(m_client_socket.getInputStream());
            backendConnection.setFrontendConnection(this);
            backendConnection.connect();
            cos = m_client_socket.getOutputStream();
            Pipe backwardPipe = new Pipe(backendConnection.getBackendStream(),cos);
            backwardPipe.flow();
        } catch (IOException ex) {
            Logger.getLogger(FrontendConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                cos.close();
            } catch (IOException ex) {
                Logger.getLogger(FrontendConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void use(Socket socket) {
        m_client_socket = socket;
    }

    public void writeConnectionResponse(byte[] output){
        try {
            m_client_socket.getOutputStream().write(output);
        } catch (IOException ex) {
            Logger.getLogger(FrontendConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public void setlClientStream(InputStream stream) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream getBackendStream() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFrontendConnection(Connection conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
