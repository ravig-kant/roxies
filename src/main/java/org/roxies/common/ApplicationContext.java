/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.roxies.tunnel.FixedDestinationComponentFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import org.roxies.chain.ChainProxyFactory;
import org.roxies.net.Connection;

/**
 *
 * @author ravigu
 */
public class ApplicationContext {

    public static ApplicationContext context = new ApplicationContext();

    private Executor m_clientexecutor = null;
    private Executor m_backendexecutor = null;
    private ServerSocket m_serverSocket = null;

    private static ProxyComponentFactory factory = null;

    public static void initialise(int port, boolean isSecure) {
        String factoryType = System.getProperty("roxies.type");
        if (factory == null) {
            factory = ("ChainedProxyOriginator".equals(factoryType)) ? new ChainProxyFactory()
                    : new FixedDestinationComponentFactory();
        }

        if (isSecure) {
            context.m_serverSocket = createSecureServer(port);
        } else {
            context.m_serverSocket = createServer(port);
        }

    }

    public ProxyComponentFactory getFactory() {
        return factory;
    }

    public Connection getBackendConnection() {
        return factory.getBackendConnection();
    }

    public static ApplicationContext currentContext() {
        return context;
    }

    public Executor getClientThreadPool() {
        if (m_clientexecutor == null) {
            m_clientexecutor = Executors.newFixedThreadPool(10, new ProxyThreadFacory("ClientPool"));

        }

        return m_clientexecutor;
    }

    public Executor getBackendConnectionPool() {
        if (m_backendexecutor == null) {
            m_backendexecutor = Executors.newFixedThreadPool(10, new ProxyThreadFacory("BackendPool"));

        }

        return m_backendexecutor;
    }

    static class ProxyThreadFacory implements ThreadFactory {

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private ProxyThreadFacory(String poolName) {
            namePrefix = poolName
                    + poolNumber.getAndIncrement()
                    + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = defaultFactory.newThread(r);
            t.setName(namePrefix + threadNumber.getAndIncrement());
            return t;
        }
    }
    
    public ServerSocket getServerSocket(){
        return m_serverSocket;
    }

    private static ServerSocket createSecureServer(int port) {
        String ksLocation = System.getProperty("roxies.keystore");
        try (FileInputStream fis = (FileInputStream) Files.newInputStream(Paths.get(ksLocation))) {

            String ksFormat = System.getProperty("roxies.keystore.type");
            if (ksFormat == null) {
                ksFormat = KeyStore.getDefaultType();
            }

            KeyStore ks = KeyStore.getInstance(ksFormat);
            ks.load(fis, "welcome".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
            kmf.init(ks, "".toCharArray());
            SSLContext sslContext = SSLContext.getDefault();
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory sslSocketFactory = sslContext.getServerSocketFactory();
            return sslSocketFactory.createServerSocket(port, 0);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            Logger.getLogger(ApplicationContext.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static ServerSocket createServer(int port) {
        try {
            ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
            return serverSocketFactory.createServerSocket(port, 0);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

}
