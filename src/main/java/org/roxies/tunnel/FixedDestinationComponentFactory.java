/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.tunnel;

import java.net.InetSocketAddress;
import org.roxies.common.ProxyComponentFactory;
import org.roxies.net.Connection;
import org.roxies.route.Route;

/**
 *
 * @author ravigu
 */
public class FixedDestinationComponentFactory implements ProxyComponentFactory {

    static Route m_installedRoute=null;
    static String m_host; 
    static int m_port;
    
    @Override
    public Connection getBackendConnection() {
        return new FixedDestinationBackendConnection();
    }

    @Override
    public Route getDefaultRoute() {
        if (m_installedRoute != null) {
            return m_installedRoute;
        }

        InetSocketAddress destAddr = new InetSocketAddress(m_host, m_port);
        m_installedRoute = new FixDestinationRoute(destAddr);
        return m_installedRoute;
    }
    
    
    
    public void setConnectionDetails(String host, int port){
        m_host = host;
        m_port = port;
    }

    @Override
    public void addRoute(String name, Route route) {
        if(m_installedRoute != null) 
            throw new RuntimeException("Route already set");
        
        m_installedRoute = route;
    }

}
