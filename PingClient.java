import java.util.*;
import java.util.concurrent.TimeUnit;//Needed to use timer
import java.io.*;
import java.net.*;

public class PingClient extends Thread{
 
 //set client ping packet size to 1024 since Server says so
 private final static int PACKETSIZE = 1024;
 
 //make a timer to use
 Timer t = new Timer(true);         
  
 //set arguments used to send packets
 public static String ServerHost;
 public static int ServerPort;
 //timeout 1 second. Can be changed
 public static int timeout = 1000;
 
 //array used to save time for each threads
 //used to make average RTT etc afterwards
 //Individual Thread to use for each Ping. Thus, 10 Threads needed to do this job.
 static double[] recvtime = {0,0,0,0,0,0,0,0,0,0};
 static double[] sendtime = {0,0,0,0,0,0,0,0,0,0};
 
 //static variable declared here for multithreading
 //public static DatagramSocket socket;
 public static InetAddress Address;
 
 //used to determine which Thread is sending
 public static int Threadnumber= -1;
 
 public void run() {
  //originally was -1. Each time one is sent, +1. So at the begining it gets 0.
  Threadnumber++;
  
 
  //make message according to given format
  //message send time is stored
  long SendTime = System.currentTimeMillis();
  sendtime[Threadnumber] = SendTime;
  System.out.println("Send: PING "+Threadnumber);
  String Message = "Ping "+ Threadnumber + " " + SendTime + "\r\n";
  
  try{
   
   //create datagram socket   
   DatagramSocket socket = new DatagramSocket();
   //create datagram packet
   DatagramPacket request =
   new DatagramPacket(Message.getBytes(), Message.length(),Address,ServerPort );
   
   //send data
   socket.send(request);
   
   //block until reply comes
   DatagramPacket reply =
   new DatagramPacket(new byte[1024], 1024);
   //set socket timeout which is originally 1000mills and can be modified using -w
   socket.setSoTimeout(timeout);
  
   //if socket data received, print data and store recv. time
   socket.receive(reply);
   printData(reply);
    
  }
  catch(SocketTimeoutException e){
 //find which ping has timeout
 //the smallest number of ping which has recvtime zero(that is, reply never received)
 // have to use a loop each time searching for it
 int i;
 for(i = 0;i<9;i++){
   if(recvtime[i] == 0) break;
 }
 //a specific random number -1 is used to mark that it is timeout.
 //used afterwards when making average
 recvtime[i] = -1;
 System.out.println("PING "+i+" Timeout!");
  }
  catch(Exception e){
  }
 }

  //printdata like in server
 private static void printData(DatagramPacket request) throws Exception{
  
  //get the bytes to use
  byte[] buf = request.getData();
  
  ByteArrayInputStream bias = new ByteArrayInputStream(buf);
  InputStreamReader isr = new InputStreamReader(bias);
  BufferedReader br = new BufferedReader(isr);
  
  String line = br.readLine();
  line = line.substring(0,6);
  
  int seq = Integer.parseInt(line.substring(5));
  
  long timerec = System.currentTimeMillis();
  recvtime[seq] = timerec;
  int RTT = (int) (recvtime[seq]-sendtime[seq]);
  System.out.println(
    "Received from " +
  request.getAddress().getHostAddress() +
  ":"+
  (line)+" "+RTT+"ms"
  );
  
 }


 public static void main(String[] args) throws Exception {
  
  if(args.length == 2 ){
   //we have ip and port in order so use it
   ServerHost = (args[0]);
   ServerPort = Integer.parseInt(args[1]);
  }
  else if(args.length == 4){
   //if, args's length is 4, the format would be
   //ip port -w timeout's length
   ServerHost = (args[0]);
   ServerPort = Integer.parseInt(args[1]);
   String w = args[2];
   //change timeout to specific input
   timeout = Integer.parseInt(args[3]);
   
   //check if -w is correctly written
   if(!w.equals("-w")){
    System.out.println("  check option command. Should be -w");
    return;
   }
  }
  else{
   System.out.println("  Improper Arguments");
   return;
  }
  Address =InetAddress.getByName(ServerHost);
// now all arguments are set
  
  
  
  //use one thread per ping
  PingClient thread0 = new PingClient();
  PingClient thread1 = new PingClient();
  PingClient thread2 = new PingClient();
  PingClient thread3 = new PingClient();
  PingClient thread4 = new PingClient();
  PingClient thread5 = new PingClient();
  PingClient thread6 = new PingClient();
  PingClient thread7 = new PingClient();
  PingClient thread8 = new PingClient();
  PingClient thread9 = new PingClient();
  //wait 1sec before generating new thread and wait enough time afterwards
  thread0.start(); Thread.sleep(1000);
  thread1.start(); Thread.sleep(1000);
  thread2.start(); Thread.sleep(1000);
  thread3.start(); Thread.sleep(1000);
  thread4.start(); Thread.sleep(1000);
  thread5.start(); Thread.sleep(1000);
  thread6.start(); Thread.sleep(1000);
  thread7.start(); Thread.sleep(1000);
  thread8.start(); Thread.sleep(1000);
  thread9.start(); Thread.sleep(1000);
  Thread.sleep(timeout+10);

  //analyze the packet
  int Received = 0;
  int minRTT = timeout;
  int maxRTT = 0;
  int sumRTT = 0;
  int averageRTT = 0;
  

  for(int i = 0;i<10;i++){
    
   if(recvtime[i] != -1){
    int thisRTT = (int) (recvtime[i]-sendtime[i]);
    sumRTT += thisRTT;
    //update max and min RTT
    if(thisRTT>maxRTT) maxRTT = thisRTT;
    if(thisRTT<minRTT) minRTT = thisRTT;
    //when rectime is -1 it means timeout so sum of the rest
    Received ++;
   }
  }
  averageRTT = sumRTT/Received;
  int lossRate = 10*(10-Received);
  System.out.println();
  System.out.println("Ping Statistics for "+Address+":");
  System.out.println("     Sent: 10, Received: "+Received+", Lost: "+(lossRate/10)+" "+"("+lossRate+"% loss)");

  //calculate RTT only when at least one packet is received (if 0, cannot make average)
  if(sumRTT != 0){
   System.out.println("Approximate round trip times in milli-seconds:");
   System.out.println("     Minimum: "+minRTT+"ms, Maximum: "+maxRTT+"ms, Average: "+averageRTT+"ms");
   
  }
 }
 

}