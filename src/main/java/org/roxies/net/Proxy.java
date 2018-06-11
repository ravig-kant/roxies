/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.net;

import java.util.concurrent.Executor;

/**
 *
 * @author ravigu
 */
public interface Proxy {
    
    public void start(int port, Boolean isSecure);
       
    public void stop();
    
}
