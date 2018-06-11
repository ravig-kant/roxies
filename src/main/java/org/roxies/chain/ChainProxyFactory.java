/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.chain;

import java.util.HashMap;
import java.util.Map;
import org.roxies.common.ProxyComponentFactory;
import org.roxies.net.Connection;
import org.roxies.route.Route;

/**
 *
 * @author ravigu
 */
public class ChainProxyFactory implements ProxyComponentFactory{

    Route m_default = null;
    Map<String,Route> routeMap = new HashMap<>();
    @Override
    public Connection getBackendConnection() {  
        return new ProxyChainOriginatorConnection();
    }

    @Override
    public Route getDefaultRoute() {
        return m_default;
    }

    @Override
    public void addRoute(String name, Route route) {
        if(m_default == null)
            m_default = route;
        
        routeMap.put(name, route);
    }
    
}
