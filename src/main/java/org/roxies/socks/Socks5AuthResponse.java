/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.socks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ravigu
 */
public class Socks5AuthResponse {
    
    private int ver ;
    private int method ;
    
    public Socks5AuthResponse(DataInputStream dis){
        try {
            ver = dis.readUnsignedByte();
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "Version received " + ver);
            method = dis.readUnsignedByte();
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "Auth method accepted " + method);
        } catch (IOException ex) {
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Socks5AuthResponse() {
        ver = 5;
        method = 0;
    }
    
    public void write(OutputStream outputStream) {
        try {
            DataOutputStream dos = new DataOutputStream(outputStream);
            dos.writeByte(ver);
            dos.writeByte(method);
//            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Socks5Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
