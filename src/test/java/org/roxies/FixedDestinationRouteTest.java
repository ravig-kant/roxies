/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roxies.net.Server;
import org.roxies.route.Route;
import org.roxies.route.Routes;
import org.roxies.util.EchoServer;
import socks.ProxyServer;
import socks.server.ServerAuthenticator;
import socks.server.ServerAuthenticatorNone;

/**
 *
 * @author ravigu
 */
public class FixedDestinationRouteTest {

    private static EchoServer testServer = null;
    private static Server proxyServer = null;

    @BeforeClass
    public static void setup() {
        Executor echoServerThread = Executors.newFixedThreadPool(2);
        echoServerThread.execute(new Runnable() {
            @Override
            public void run() {
                testServer = new EchoServer();
                testServer.start();
            }
        }
        );

        echoServerThread.execute(new Runnable() {
            public void run() {
                Route route = Routes.newFixDestinationRoute("127.0.0.1", 9090);
                proxyServer = new Server();
                proxyServer.start(8081,null);

            }
        });

//        ServerAuthenticator auth = new ServerAuthenticatorNone();
//
//        ProxyServer pServer = new ProxyServer(auth);
//
//        pServer.setLog(System.out);
//
//        pServer.start(50352);
    }

    @AfterClass
    public static void teardown() {
        if (testServer != null) {
            testServer.stop();
        }
    }

    @Test
    public void routeToEchoServer() {
        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(8081));

            
            Socket client = new Socket(proxy);
            client.connect(new InetSocketAddress(9090));
//            ProxySelector.setDefault(old);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            bw.write("Hello Server\n");
            bw.flush();
            String line = br.readLine();
            System.out.println("Got back from server :" + line);

            client.close();
        } catch (IOException ex) {
            Logger.getLogger(FixedDestinationRouteTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
