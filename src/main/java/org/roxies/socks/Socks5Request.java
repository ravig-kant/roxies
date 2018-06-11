/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.socks;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ravigu
 */
public class Socks5Request {

    public int addrType;
    public int command = 1;

    byte[] headerData;
    private String host;
    private int version;
    public InetAddress ip;
    public int port;

    public static final int SOCKS_VERSION = 5;

    public static final int SOCKS_ATYP_IPV4 = 0x1; //Where is 2??
    public static final int SOCKS_ATYP_DOMAINNAME = 0x3; //!!!!rfc1928
    public static final int SOCKS_ATYP_IPV6 = 0x4;

    public static final int SOCKS_IPV6_LENGTH = 16;

    public Socks5Request(int cmd, InetAddress ip, int port) {
        this.host = ip == null ? "0.0.0.0" : ip.getHostName();
        this.version = SOCKS_VERSION;

        byte[] addr;

        if (ip == null) {
            addr = new byte[4];
            addr[0] = addr[1] = addr[2] = addr[3] = 0;
        } else {
            addr = ip.getAddress();
        }

        addrType = addr.length == 4 ? SOCKS_ATYP_IPV4
                : SOCKS_ATYP_IPV6;

        headerData = new byte[6 + addr.length];
        headerData[0] = (byte) 5;		//Version
        headerData[1] = (byte) this.command;			//Command
        headerData[2] = (byte) 0;			//Reserved byte
        headerData[3] = (byte) addrType;		//Address type

        //Put Address
        System.arraycopy(addr, 0, headerData, 4, addr.length);
        //Put port
        headerData[headerData.length - 2] = (byte) (port >> 8);
        headerData[headerData.length - 1] = (byte) (port);
    }

    public void write(OutputStream outputStream) {
        try {
            outputStream.write(headerData);
            outputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(Socks5Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
