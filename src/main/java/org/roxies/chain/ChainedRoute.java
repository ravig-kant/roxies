/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.chain;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.roxies.route.Route;
import org.roxies.socks.Socks5Request;

/**
 *
 * @author ravigu
 */
public class ChainedRoute implements Route{
    
    //TODO change the list to InetSocketAddress
    private List<Socks5Request> m_proxies = new ArrayList<>();
    private InetSocketAddress destination = null;

    @Override
    public InetSocketAddress destination() {
        return destination;
    }
    
    
    public List<Socks5Request> proxyChain(){
        return m_proxies;
    }
    
    @Override
    public Route addStop(InetSocketAddress ip){
        if(destination == null)
            destination = ip;
        else
            m_proxies.add(new Socks5Request(0,ip.getAddress(),ip.getPort()));
        
        return this;
    }

    @Override
    public List<InetSocketAddress> getStops() {
        List<InetSocketAddress> stops = new ArrayList<>();
        
        for(Socks5Request aProxy : m_proxies)
            stops.add(new InetSocketAddress(aProxy.ip, aProxy.port));
        
        return stops;
    }
}
