/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.BeforeClass;
import org.roxies.net.Server;
import org.roxies.route.Route;
import org.roxies.route.Routes;
import org.roxies.util.CryptoMaterial;
import org.roxies.util.EchoServer;

/**
 *
 * @author ravigu
 */
public class SecureFixDestinationProxyTest {

    private static EchoServer testServer = null;
    private static Server proxyServer = null;

    @BeforeClass
    public static void setup() {
        
        System.setProperty("roxies.keystore", CryptoMaterial.getKeyStore());
        System.setProperty("roxies.keystore.type", "JKS");
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
                proxyServer.start(8081, null);

            }
        });
    }
}    
