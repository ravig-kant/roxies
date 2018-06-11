/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ravigu
 */
public class Pipe {

    private static final int BUFFER_SIZE = 8192;
    private InputStream m_startpoint;
    private OutputStream m_endpoint;

    public void flow() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                int bytesRead = m_startpoint.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream is reached --> exit
                }
                m_endpoint.write(buffer, 0, bytesRead);
                m_endpoint.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(Pipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Pipe(InputStream start, OutputStream end) {
        m_startpoint = start;
        m_endpoint = end;
    }

}
