/*
 *
 *  UDPClient
 *  * Compile: java UDPClient.java
 *  * Run: java UDPClient localhost PortNo
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws Exception {
        if(args.length != 2){
            System.out.println("Usage: java UDPClinet localhost PortNo");
            System.exit(1);
        }
		// Define socket parameters, address and Port No
        InetAddress IPAddress = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);
        Scanner scan = new Scanner(System.in); 
		
		// create socket which connects to server
        Socket clientSocket = new Socket(IPAddress, serverPort);
        
        
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        // Must create two threads: one for listening, one for sending,
        // as order of messages sent or received will not be known
        
        // Sending thread
        Thread sendThread = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		
        		try {
        			while (true) {
        				String message = scan.nextLine();
        				outToServer.writeUTF(message);
        			}
        		} catch (IOException e) { e.printStackTrace(); }
        	}
        });
        
        // CHANGE WAY OF DOING THREADS??? ////////////////////////////////////////////////////////////
        
        // Receiving thread
        Thread receiveThread = new Thread(new Runnable() {
        	@Override
        	public void run() {
    			try {
    				while (true) {
        				DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
        				System.out.println(">" + inFromServer.readUTF());
    				}
    			} catch (IOException e) {e.printStackTrace();}
        	}
        });
        
        sendThread.start();
        receiveThread.start();
        
        
        
        
        //clientSocket.close();
		
	} // end of main
    
} // end of class UDPClient