/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;

/**
 *
 * @author ravigu
 */
public class EchoServer {

    private ServerSocket echoSocket;

    public void start() {
        try {
            echoSocket = ServerSocketFactory.getDefault().createServerSocket(9090, 0);
            while (!echoSocket.isClosed()) {
                Socket soc = echoSocket.accept();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(" Echoing : " + line);
                    bw.write(line);
                    bw.write("\n");
                    bw.flush();
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void stop() {

        try {
            if (echoSocket != null) {
                echoSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
