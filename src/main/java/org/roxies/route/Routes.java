/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.route;

import org.roxies.tunnel.FixDestinationRoute;
import java.net.InetSocketAddress;
import java.util.List;
import org.roxies.chain.ChainedRoute;
import org.roxies.common.ApplicationContext;
import org.roxies.common.ProxyComponentFactory;

/**
 *
 * @author ravigu
 */
public class Routes {
    
    public static Route newFixDestinationRoute(String host, int port){
        ProxyComponentFactory factory = ApplicationContext.currentContext().getFactory();
        if(factory.getDefaultRoute() != null)
            throw new RuntimeException("Route can't be changed dynamically. Use command line argument and restart the proxy");
        
        InetSocketAddress destAddr = new InetSocketAddress(host,port);
        factory.addRoute("FixedRoute", new FixDestinationRoute(destAddr));
        return factory.getDefaultRoute();
    }
    
    public static void addChainedRoute(String name, List<InetSocketAddress> routes){
        ProxyComponentFactory factory = ApplicationContext.currentContext().getFactory();
        
        Route route = new ChainedRoute();
        for(InetSocketAddress address : routes)
            route.addStop(address);
        
        factory.addRoute(name, route);
    }
    
}
