/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.socks;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ravigu
 */
public class Socks5Response {

    private int version;
    private int response;
    private int reservered;
    private int addrType;
    private InetAddress addr;
    private int port;
    private String host;

    public static final int SOCKS_ATYP_IPV4 = 0x1; //Where is 2??
    public static final int SOCKS_ATYP_DOMAINNAME = 0x3; //!!!!rfc1928
    public static final int SOCKS_ATYP_IPV6 = 0x4;

    public static final int SOCKS_IPV6_LENGTH = 16;

    public Socks5Response(DataInputStream dis) {
        try {
            version = dis.readUnsignedByte();
            Logger.getLogger(Socks5Response.class.getName()).log(Level.INFO, "Version in response header " + version);
            response = dis.readUnsignedByte();
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "Response received " + response);
            //TODO proper error handling as per the code
//            if(response[0] != 0x00)
//                throw new RuntimeException("Error while processing request. Got response " + response);

            reservered = dis.readUnsignedByte();
            addrType = dis.readUnsignedByte();
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "Address type received " + addrType);
            byte addr[];

            switch (addrType) {
                case SOCKS_ATYP_IPV4:
                    addr = new byte[4];
                    dis.readFully(addr);
                    host = bytes2IPV4(addr, 0);
                    break;
                case SOCKS_ATYP_IPV6:
                    addr = new byte[SOCKS_IPV6_LENGTH];//I believe it is 16 bytes,huge!
                    dis.readFully(addr);
                    host = bytes2IPV6(addr, 0);
                    break;
                case SOCKS_ATYP_DOMAINNAME:
                    //System.out.println("Reading ATYP_DOMAINNAME");
                    addr = new byte[dis.readUnsignedByte()];//Next byte shows the length
                    dis.readFully(addr);
                    host = new String(addr);
                    break;
                default:
                    throw (new RuntimeException("Proxy.SOCKS_JUST_ERROR"));
            }
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "IP received " + host);
            port = dis.readUnsignedShort();
            Logger.getLogger(Socks5AuthResponse.class.getName()).log(Level.INFO, "Port received " + port);
        } catch (IOException ex) {
            Logger.getLogger(Socks5Response.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    static final String bytes2IPV4(byte[] addr, int offset) {
        String hostName = "" + (addr[offset] & 0xFF);
        for (int i = offset + 1; i < offset + 4; ++i) {
            hostName += "." + (addr[i] & 0xFF);
        }
        return hostName;
    }

    static final String bytes2IPV6(byte[] addr, int offset) {
        //Have no idea how they look like!
        return null;
    }

    public Socks5Response() {
        version = 5;
        response = 0;
        reservered = 0;
        addrType = 1;
        addr = InetAddress.getLoopbackAddress();
        port = 8081;
        host = "127.0.0.1";
    }

    public void write(OutputStream os) {
        try {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeByte(version);
            dos.writeByte(response);
            dos.writeByte(reservered);
            dos.writeByte(addrType);
            dos.write(addr.getAddress());
            dos.writeByte(port);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Socks5Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
