/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.common;

import org.roxies.net.Connection;
import org.roxies.route.Route;

/**
 *
 * @author ravigu
 */
public interface ProxyComponentFactory {
    
    public Connection getBackendConnection();
    
    public Route getDefaultRoute();
    
    public void addRoute(String name, Route route);
    
}
