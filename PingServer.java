import java.io.*;
import java.net.*;
import java.util.*;
//Class Extends Thread in order to use parallel
public class PingServer extends Thread {//modified
 private static final double LOSS_RATE = 0.3;
 private static final int AVERAGE_DELAY = 1000;
 
 //modified
 //multithreading needs static variables
 public static InetAddress clientAddress;
 public static byte[] buffer;
 public static DatagramSocket socket; 
 public static int clientPort;
 public void run(){
  Random random   = new Random();

  //decide whether to reply or simulate packet loss
  if(random.nextDouble()< LOSS_RATE){
   System.out.println("Reply not sent.");
   return;
  }

    //modified
  //simulate network delay
  try {
   Thread.sleep((int)(random.nextDouble()*2*AVERAGE_DELAY));
  }
  catch (InterruptedException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  
  //Send reply
  DatagramPacket reply = new DatagramPacket(buffer, buffer.length,clientAddress,clientPort);
  try {
   socket.send(reply);
  }
  //modified
  catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  
  System.out.println("  Reply sent");
 }
 
 public static void main(String[] args) throws Exception{
  // TODO Auto-generated method stub
  
  //get command line argument
  if(args.length!=1){
   System.out.println("  Required arguments: port");
   return;
  }
  int port = Integer.parseInt(args[0]);
  
  //create random number generator for use in simulating
  //packet loss and network delay
  Random random   = new Random();
  
  //Create a datagram socket for receiving and sending UDP packets
  //through the poet specified on the command line
  socket  = new DatagramSocket(port);
  
  while(true){
   //Create a datagram packet to hold incomming UDP packet
   DatagramPacket request  = new DatagramPacket(new byte[1024],1024);
  
   //block until the host receives a UDP packet
   socket.receive(request);
   
   //modified
   //when ping is received, make a new thread
   PingServer multiServer= new PingServer();
    multiServer.start();
         
    clientAddress = request.getAddress();
    clientPort = request.getPort();
    buffer = request.getData();
   //print the recieved data
   printData(request);
   
   
   
  }
  
 }
 
 //print data method
 private static void printData(DatagramPacket request) throws Exception{
 
  //obtain references to the packet;s array of bytes.
  byte[] buffer = request.getData();
  
  ByteArrayInputStream bias = new ByteArrayInputStream(buffer);
  
  InputStreamReader isr = new InputStreamReader(bias);
  
  BufferedReader br = new BufferedReader(isr);
  
  String line = br.readLine();
  System.out.println(
    "Received from" +
  request.getAddress().getHostAddress() +
  ":"+
  new String(line)
  );
  
 }
}