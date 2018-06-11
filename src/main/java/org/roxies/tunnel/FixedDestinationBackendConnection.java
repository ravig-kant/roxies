/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import org.roxies.common.ApplicationContext;
import org.roxies.net.Connection;
import org.roxies.net.Pipe;

/**
 *
 * @author ravigu
 */
public class FixedDestinationBackendConnection implements Connection {

    private InputStream m_inputStream;
    private InputStream m_backendStream;
    private Connection m_frontEndConnection;
    private static final int BUFFER_SIZE = 8192;

    @Override
    public void connect() {
        Executor executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetSocketAddress destination = ApplicationContext.currentContext().
                                                        getFactory().getDefaultRoute().
                                                        destination();
                    Socket backend = SocketFactory.getDefault().createSocket(destination.getAddress(), destination.getPort());
                    m_backendStream = backend.getInputStream();
                    Pipe forwardPipe = new Pipe(m_inputStream, backend.getOutputStream());
                    forwardPipe.flow();
                } catch (IOException ex) {
                    Logger.getLogger(FixedDestinationBackendConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

    }

    @Override
    public void use(Socket socket) {
        throw new UnsupportedOperationException("For fixed destination it is not required");
    }

    @Override
    public void run() {
        connect();
    }

    @Override
    public void setlClientStream(InputStream stream) {
        m_inputStream = stream;
    }

    @Override
    public InputStream getBackendStream() {
        return m_backendStream;
    }

    private int receiveBytes(List<byte[]> bufferArray) {
        bufferArray = new ArrayList<>();
        int size = 0;
        try {
            while (true) {
                byte[] lbuffer = new byte[BUFFER_SIZE];
                int bytesRead = m_inputStream.read(lbuffer);
                if (bytesRead == -1) {
                    break; // End of stream is reached --> exit 
                }
                size += bytesRead;
                boolean out = bytesRead < BUFFER_SIZE ? bufferArray.add(Arrays.copyOf(lbuffer, bytesRead))
                        : bufferArray.add(lbuffer);
            }
        } catch (IOException ex) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        return size == 0 ? -1 : size;
    }

    @Override
    public void setFrontendConnection(Connection conn) {
        m_frontEndConnection = conn;
    }

}
