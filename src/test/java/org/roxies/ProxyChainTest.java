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
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roxies.chain.ChainedRoute;
import org.roxies.common.ApplicationContext;
import org.roxies.net.Server;
import org.roxies.route.Route;
import org.roxies.util.EchoServer;
import socks.ProxyServer;
import socks.server.ServerAuthenticator;
import socks.server.ServerAuthenticatorNone;

/**
 *
 * @author ravigu
 */
public class ProxyChainTest {

    private static EchoServer testServer = null;
    private static Server proxyServer = null;
    private static ProxyServer pServer1 = null;
    private static ProxyServer pServer2 = null;

    @BeforeClass
    public static void setup() {
        System.setProperty("roxies.type", "ChainedProxyOriginator");

        Logger logger = Logger.getGlobal();
        // LOG this level to the log
        logger.setLevel(Level.FINE);

        ConsoleHandler handler = new ConsoleHandler();
        // PUBLISH this level
        handler.setLevel(Level.FINE);
        logger.addHandler(handler);
        Executor echoServerThread = Executors.newFixedThreadPool(4);
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
                Route route = new ChainedRoute();
                route.addStop(new InetSocketAddress("127.0.0.1", 50352));
                route.addStop(new InetSocketAddress("127.0.0.1", 50353));
                ApplicationContext.currentContext().getFactory().addRoute("defaultRoute", route);
                proxyServer = new Server();
                proxyServer.start(8081,null);

            }
        });

        ServerAuthenticator auth = new ServerAuthenticatorNone();

        pServer1 = new ProxyServer(auth);
        pServer1.setIddleTimeout(0);
        pServer2 = new ProxyServer(auth);
        pServer2.setIddleTimeout(0);
        pServer1.setLog(System.out);
        pServer2.setLog(System.out);

        echoServerThread.execute(new Runnable() {
            public void run() {
                pServer1.start(50352);
            }
        });

        echoServerThread.execute(new Runnable() {
            public void run() {
                pServer1.start(50353);
            }
        });

    }

    @AfterClass
    public static void tearDown() {
        pServer2.stop();
        pServer1.stop();
        proxyServer.stop();
        testServer.stop();
    }

    @Test
    public void testChainingProxy() {
//            ProxySelector selector = new ProxySelector() {
//                @Override
//                public List<Proxy> select(URI uri) {
//                    List<Proxy> ret = new ArrayList<>();
//                    ret.add(proxy);
//                    return ret;
//                }
//
//                @Override
//                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//            };
//            ProxySelector old = ProxySelector.getDefault();
//            ProxySelector.setDefault(selector);

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
