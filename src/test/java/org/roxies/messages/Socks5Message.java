/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.roxies.messages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author ravigu
 */



class Socks5Message{
   /** Address type of given message*/
   public int addrType;
   public int command;

   byte[] data;
    private String host;
    private int version;
    private InetAddress ip;
    private int port;

   /**
    Server error response.
    @param cmd Error code.
   */
   public Socks5Message(int cmd){
      data = new byte[3];
      data[0] = SOCKS_VERSION; //Version.
      data[1] = (byte)cmd;     //Reply code for some kind of failure.
      data[2] = 0;             //Reserved byte.
   }

   /**
    Construct client request or server response.
    @param cmd - Request/Response code.
    @param ip  - IP field.
    @paarm port - port field.
   */
   public Socks5Message(int cmd,InetAddress ip,int port){
      this.host = ip==null?"0.0.0.0":ip.getHostName();
      this.version = SOCKS_VERSION;

      byte[] addr;

      if(ip == null){
         addr = new byte[4];
         addr[0]=addr[1]=addr[2]=addr[3]=0;
      }else
         addr = ip.getAddress();

      addrType = addr.length == 4 ? SOCKS_ATYP_IPV4
                                  : SOCKS_ATYP_IPV6;
 
      data = new byte[6+addr.length];
      data[0] = (byte) SOCKS_VERSION;		//Version
      data[1] = (byte) this.command;			//Command
      data[2] = (byte) 0;			//Reserved byte
      data[3] = (byte) addrType;		//Address type
 
      //Put Address
      System.arraycopy(addr,0,data,4,addr.length);
      //Put port
      data[data.length-2] = (byte)(port>>8);
      data[data.length-1] = (byte)(port);
   }


   /**
    Construct client request or server response.
    @param cmd - Request/Response code.
    @param hostName  - IP field as hostName, uses ADDR_TYPE of HOSTNAME.
    @paarm port - port field.
   */
   public Socks5Message(int cmd,String hostName,int port){
      this.host = hostName;
      this.version = SOCKS_VERSION;

      //System.out.println("Doing ATYP_DOMAINNAME");

      addrType = SOCKS_ATYP_DOMAINNAME;
      byte addr[] = hostName.getBytes();
 
      data =new byte[7+addr.length];
      data[0] = (byte) SOCKS_VERSION;		//Version
      data[1] = (byte) command;			//Command
      data[2] = (byte) 0;			//Reserved byte
      data[3] = (byte) SOCKS_ATYP_DOMAINNAME;	//Address type
      data[4] = (byte) addr.length;		//Length of the address

      //Put Address
      System.arraycopy(addr,0,data,5,addr.length);
      //Put port
      data[data.length-2] = (byte)(port >>8);
      data[data.length-1] = (byte)(port);
   }

   /**
     Initialises Message from the stream. Reads server response from
     given stream.
     @param in Input stream to read response from.
     @throws SocksException If server response code is not SOCKS_SUCCESS(0), or
     if any error with protocol occurs.
     @throws IOException If any error happens with I/O.
   */
   public Socks5Message(InputStream in) throws IOException{
      this(in,true);
   }

   /**
     Initialises Message from the stream. Reads server response or client 
     request from given stream.
     
     @param in Input stream to read response from.
     @param clinetMode If true read server response, else read client request.
     @throws SocksException If server response code is not SOCKS_SUCCESS(0) and
     reading in client mode, or if any error with protocol occurs.
     @throws IOException If any error happens with I/O.
   */
   public Socks5Message(InputStream in,boolean clientMode) throws IOException{
      read(in,clientMode);
   }


   /**
     Initialises Message from the stream. Reads server response from
     given stream.
     @param in Input stream to read response from.
     @throws SocksException If server response code is not SOCKS_SUCCESS(0), or
     if any error with protocol occurs.
     @throws IOException If any error happens with I/O.
   */
   public void read(InputStream in) throws IOException{
       read(in,true);
   }


   /**
     Initialises Message from the stream. Reads server response or client 
     request from given stream.
     
     @param in Input stream to read response from.
     @param clinetMode If true read server response, else read client request.
     @throws SocksException If server response code is not SOCKS_SUCCESS(0) and
     reading in client mode, or if any error with protocol occurs.
     @throws IOException If any error happens with I/O.
   */
   public void read(InputStream in,boolean clientMode) throws IOException{
      data = null;
      ip = null;

      DataInputStream di = new DataInputStream(in);

      version = di.readUnsignedByte();
      command = di.readUnsignedByte();
      if(clientMode && command != 0)
        throw new RuntimeException("command!=0");

      int reserved = di.readUnsignedByte();
 
   }

   /**
    Writes the message to the stream.
    @param out Output stream to which message should be written.
   */
   public void write(OutputStream out)throws IOException{
     if(data == null){
       Socks5Message msg;

       if(addrType == SOCKS_ATYP_DOMAINNAME)
          msg = new Socks5Message(command,host,port);
       else{
          if(ip == null){
             try{
               ip = InetAddress.getByName(host);
             }catch(UnknownHostException uh_ex){
               throw new RuntimeException("Proxy.SOCKS_JUST_ERROR");
             }
          }
          msg = new Socks5Message(command,ip,port);
       }
       data = msg.data;
     }
     out.write(data);
   }

   /**
    Returns IP field of the message as IP, if the message was created
    with ATYP of HOSTNAME, it will attempt to resolve the hostname,
    which might fail.
    @throws UnknownHostException if host can't be resolved.
   */
   public InetAddress getInetAddress() throws UnknownHostException{
     if(ip!=null) return ip;

     return (ip=InetAddress.getByName(host));
   }

   /**
     Returns string representation of the message.
   */
   public String toString(){
      String s=
        "Socks5Message:"+"\n"+
        "VN   "+version+"\n"+
        "CMD  "+command+"\n"+
        "ATYP "+addrType+"\n"+
        "ADDR "+host+"\n"+
        "PORT "+port+"\n";
      return s;
   }
            

   /**
    *Wether to resolve hostIP returned from SOCKS server
    *that is wether to create InetAddress object from the
    *hostName string
    */
   static public boolean resolveIP(){ return doResolveIP;}

   /**
    *Wether to resolve hostIP returned from SOCKS server
    *that is wether to create InetAddress object from the
    *hostName string
    *@param doResolve Wether to resolve hostIP from SOCKS server.
    *@return Previous value.
    */
   static public boolean resolveIP(boolean doResolve){
      boolean old = doResolveIP;
      doResolveIP = doResolve;
      return old;
   }

   /*
   private static final void debug(String s){
      if(DEBUG)
         System.out.print(s);
   }
   private static final boolean DEBUG = false;
   */
   
  static final String bytes2IPV4(byte[] addr,int offset){
      String hostName = ""+(addr[offset] & 0xFF);
      for(int i = offset+1;i<offset+4;++i)
        hostName+="."+(addr[i] & 0xFF);
      return hostName;
   }

   static final String bytes2IPV6(byte[] addr,int offset){
     //Have no idea how they look like!
     return null;
   }

   //SOCKS5 constants
   public static final int SOCKS_VERSION		=5;

   public static final int SOCKS_ATYP_IPV4		=0x1; //Where is 2??
   public static final int SOCKS_ATYP_DOMAINNAME	=0x3; //!!!!rfc1928
   public static final int SOCKS_ATYP_IPV6		=0x4;

   public static final int SOCKS_IPV6_LENGTH		=16;

   static boolean doResolveIP = true;

}

