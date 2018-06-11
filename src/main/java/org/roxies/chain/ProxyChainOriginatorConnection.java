/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.chain;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import org.roxies.common.ApplicationContext;
import org.roxies.net.Connection;
import org.roxies.tunnel.FixedDestinationBackendConnection;
import org.roxies.net.FrontendConnection;
import org.roxies.net.Pipe;
import org.roxies.route.Route;
import org.roxies.socks.Socks5AuthResponse;
import org.roxies.socks.Socks5Request;
import org.roxies.socks.Socks5Response;

/**
 *
 * @author ravigu
 */
public class ProxyChainOriginatorConnection implements Connection {

    private InputStream m_inputStream = null;
    private OutputStream m_clientOutputStream = null;
    private InputStream m_backendStream = null;
    private FrontendConnection m_frontEndConn = null;

    @Override
    public void connect() {
        try {
            Route route = ApplicationContext.currentContext().getFactory().getDefaultRoute();
            if (!(route instanceof ChainedRoute)) {
                throw new RuntimeException("Invalid Route");
            }

            ChainedRoute chainedRoute = (ChainedRoute) route;

            InetSocketAddress destination = chainedRoute.destination();
            Socket backend = SocketFactory.getDefault().createSocket(destination.getAddress(), destination.getPort());
            m_backendStream = backend.getInputStream();
            DataInputStream dis = new DataInputStream(m_backendStream);
            for (Socks5Request aProxy : chainedRoute.proxyChain()) {
                writeAuthData(backend.getOutputStream());
                aProxy.write(backend.getOutputStream());
                Socks5AuthResponse authResponse = new Socks5AuthResponse(dis);
                Socks5Response proxyResp = new Socks5Response(dis);
            }
//            Not needed because the response from backend contains socks response            
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            Socks5AuthResponse authResponse = new Socks5AuthResponse();
//            Socks5Response response = new Socks5Response();
//            authResponse.write(baos);
//            response.write(baos);
//            byte[] finalResponse = baos.toByteArray();
//            System.out.println("-- Start of response --");
//            for(int i =0 ; i < finalResponse.length ; ++i)
//                System.out.printf("%02X", finalResponse[i]);
//            System.out.println("\n-- End of response --");
            
//            m_frontEndConn.writeConnectionResponse(finalResponse);
            
            Executor executor = ApplicationContext.currentContext().getBackendConnectionPool();
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        Pipe forwardPipe = new Pipe(m_inputStream, backend.getOutputStream());
                        forwardPipe.flow();
                    } catch (IOException ex) {
                        Logger.getLogger(ProxyChainOriginatorConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

        } catch (IOException ex) {
            Logger.getLogger(FixedDestinationBackendConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void use(Socket socket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setlClientStream(InputStream stream) {
        m_inputStream = stream;
    }

    @Override
    public InputStream getBackendStream() {
        return m_backendStream;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void writeAuthData(OutputStream out) {
        byte[] authData = new byte[3];
        authData[0] = (byte) 5;
        authData[1] = (byte) 1;
        authData[2] = (byte) 0;

        try {
            out.write(authData);
        } catch (IOException ex) {
            Logger.getLogger(ProxyChainOriginatorConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setFrontendConnection(Connection conn) {
        m_frontEndConn = (FrontendConnection) conn;
    }

}
