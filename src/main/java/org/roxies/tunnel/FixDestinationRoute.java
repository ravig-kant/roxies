/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.tunnel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.roxies.route.Route;

/**
 *
 * @author ravigu
 */
public class FixDestinationRoute implements Route {
    
    private InetSocketAddress  m_destAddr;

    public FixDestinationRoute(InetSocketAddress destAddr) {
        m_destAddr = destAddr;
    }

    @Override
    public InetSocketAddress destination() {
        return m_destAddr;
    }

    @Override
    public Route addStop(InetSocketAddress stop) {
        throw new UnsupportedOperationException("Fixed destination can't be used to add stops");
    }

    @Override
    public List<InetSocketAddress> getStops() {
        List<InetSocketAddress> stops = new ArrayList<>();
        stops.add(m_destAddr);
        return stops;
    }
    
}
