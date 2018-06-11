/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.route;

import java.net.InetSocketAddress;
import java.util.List;

/**
 *
 * @author ravigu
 */
public interface Route{
        
    public InetSocketAddress destination();
    
    public Route addStop(InetSocketAddress stop);
    
    public List<InetSocketAddress> getStops();
     
}
