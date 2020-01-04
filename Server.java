/*
 * Threaded UDPServer
 * Compile: javac Server.java
 * Run: java Server server_port block_duration timeout
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.*;
import java.util.Timer;

public class Server extends Thread{
    static ServerSocket welcomeSocket;
    static Socket connectionSocket;
    static int UPDATE_INTERVAL = 1000;//milliseconds
    static ReentrantLock syncLock = new ReentrantLock();
    int blockDuration;
    int timeout;
    //static List<String> users = new ArrayList<String>();
    private static ArrayList<ServerHelper> clients = new ArrayList<ServerHelper>();
    protected static ArrayList<OnlineUser> onlineUsers = new ArrayList<OnlineUser>();
    protected static ArrayList<OfflineMessage> offlineMessages = new ArrayList<OfflineMessage>();
    
    
    
	public static void main(String[] args)throws Exception {
		
		//Assign Port number to newly created welcome socket
        int serverPort = Integer.parseInt(args[0]);
        int blocked = Integer.parseInt(args[1]);
        int time = Integer.parseInt(args[2]);
        // takes in block_duration and timeout values for constructor/////////////////
        ServerSocket welcomeSocket = new ServerSocket(serverPort);
        
        // Creates list of all users in credentials and if theyre online
    	BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"));
    	String line = reader.readLine().trim();
    	while(line!=null) {
    		String user = line.substring(0, line.indexOf(' '));
    		onlineUsers.add(new OnlineUser(user, false));
    		line = reader.readLine();
    	}
        
        
        System.out.println("Server is ready :");
        
        Socket connSocket;
        while (true){
        	// accept connection from connection queuer
        	connSocket = welcomeSocket.accept();
        	System.out.println("New Connection Accepted");
        	
        	//get lock
            syncLock.lock();
            
            
            // New serverHelper object per client
            ServerHelper s = new ServerHelper(connSocket, blocked, time, clients, onlineUsers, offlineMessages);
            clients.add(s);
            Thread t = new Thread(s);
            t.start();
            
            
            syncLock.unlock();
        } // end of while (true)
        
	} // end of main()
} 

class ServerHelper extends Server{
	Socket connnectionSocket;
    final int blockDuration;
    final int timeout;
	boolean isLoggedIn;
	DataInputStream inFromClient;
	DataOutputStream outToClient;
	static ReentrantLock syncLock = new ReentrantLock();
	private ArrayList<ServerHelper> clients = new ArrayList<ServerHelper>();
	String username;
	//private ArrayList<String> blockedList = new ArrayList<String>();
	private ArrayList<OnlineUser> onlineUsers = new ArrayList<OnlineUser>();
	private ArrayList<OfflineMessage> offlineMessages = new ArrayList<OfflineMessage>();
    
    public ServerHelper(Socket s, int block, int time, ArrayList<ServerHelper> clients, 
    		ArrayList<OnlineUser> onlineUsers, ArrayList<OfflineMessage> offlineMessages) throws Exception{
    	this.connnectionSocket = s;
    	this.clients = clients;
    	this.blockDuration = block;
    	this.timeout = time;
    	this.isLoggedIn = false;
    	inFromClient = new DataInputStream(s.getInputStream());
    	outToClient = new DataOutputStream(s.getOutputStream());
    	this.onlineUsers = onlineUsers;
    	this.offlineMessages = offlineMessages;
    	
    }
    
    
    @Override
    public void run() {
    	
    	String message;
    	while(isLoggedIn == false) {
    		isLoggedIn = login();
    		if(isLoggedIn == false) {
    			try{
    				Thread.sleep(blockDuration * 1000);//in milliseconds
    			} catch (InterruptedException e){
    				System.out.println(e);
    			}
    		}
    		else {
    			this.isLoggedIn = true;
    			break;
    		}
    	}
    	
    	if(this.isLoggedIn) {
    		for(OnlineUser us: onlineUsers) {
    			if(this.username.equals(us.getUser())) {
    				us.makeOnline();
    				break;
    			}
    		}
    		String offlineRet = "";
    		int counter = 1;
    		for(OfflineMessage m: offlineMessages) {
    			if(m.getIntendedUser().trim().equals(this.username.trim())) {
    				offlineRet = offlineRet + "Offline message " + counter + " from " + m.getFromUser() + ": " + m.getOfflineMessage() + "\n";
    				counter++;
    			}
    		}
    		if(offlineMessages.size() > 0) {
    			for(int i = offlineMessages.size()-1; i >= 0; i--) {
    				if(offlineMessages.get(i).getIntendedUser().trim().equals(this.username.trim())) {
    					offlineMessages.remove(i);
    				}
    			}
    		}
    		if(offlineRet.equals("")) {
    			offlineRet = "No messages while offline";
    		}
    		try {
    			outToClient.writeUTF(offlineRet.trim());
    		} catch(IOException e) {e.printStackTrace();}
    	}
    	
    	//this.connnectionSocket.setSoTimeout(timeout * 1000);
    	try {
    		while(true) {
    			// Try to receive a message
    		//this.connectionSocket.setSoTimeout(timeout * 1000);
    			message = inFromClient.readUTF();
    			System.out.println(message);
    			
    			// Message command
    			int spaceIndex = message.indexOf(' ');
    			if(spaceIndex == -1) {
    				spaceIndex = message.length();
    			}
    			if(message.substring(0, spaceIndex).toUpperCase().equals("MESSAGE")) {
    				String noCommand = message.substring(message.indexOf(' ') + 1, message.length());
    				String toUser = noCommand.substring(0, noCommand.indexOf(" "));
    				String content = noCommand.substring(noCommand.indexOf(' ') + 1, noCommand.length());
    				boolean blocked = false;
    				boolean on = false;
    				for(OnlineUser us: this.onlineUsers) {
    					if(toUser.trim().equals(us.getUser().trim())) {
    						on = us.isOnline();
    						for(String str: us.blockedList) {
    							System.out.println(str);
    							if(str.trim().equals(this.username.trim())) {
    								blocked = true;
    								break;
    							}
    						}
    						break;
    					}
    				}
    				
    				if(blocked) {
    					outToClient.writeUTF("Cannot send message, " + toUser + " has blocked you");
    				}
    				else if(!blocked && !on) {
    					outToClient.writeUTF("User is offline, will receive message once they log in");
    					offlineMessages.add(new OfflineMessage(toUser, content, this.username));
    				}
    				else if(!blocked && on) {
    					sendMessage(toUser, content);
    				}
    			}
    			
    			// Broadcast command
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("BROADCAST")) {
    				String content = message.substring(message.indexOf(" ") + 1, message.length());
    				broadcast(content, this.username);
    			}
    			
    			// Whoelse command
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("WHOELSE")) {
    				String who = "";
    				if(clients.size() > 1) {
    					for(ServerHelper client: clients) {
    						if(!(client.getUsername().equals(this.username)) && client.isOnline()) {
    							who = who + client.getUsername() + ", ";
    						}
    					}
    				}
    				outToClient.writeUTF(who);
    			}
    			
    			// Logs user out if command given
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("LOGOUT")) {
    				this.isLoggedIn = false;
    				clients.remove(this); ///////////////////////////////////////////// unsure
    				for(OnlineUser us: onlineUsers) {
    					if(this.getUsername().equals(us.getUser())) {
    						us.makeOffline();
    					}
    				}
    				this.connectionSocket.close();
    			}
    			
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("BLOCK")) {
    				String blockedUser = message.substring(message.indexOf(" ") + 1, message.length()).trim();
    				System.out.println(blockedUser + "!");
    				for(OnlineUser us: this.onlineUsers) {
    					if(us.getUser().equals(this.username)) {
    						System.out.println("here 1");
    						if(us.blockedList.size() == 0) {
    							us.blockedList.add(blockedUser);
    							System.out.println("here 2");
    						}
    						else {
    							boolean isBlocked = false;
    							for(String str: us.blockedList) {
    								if(str.trim().equals(blockedUser)) {
    									outToClient.writeUTF(blockedUser + " already blocked");
    									isBlocked = true;
    									break;
    								}
    							}
    							if(!isBlocked) {
    								us.blockedList.add(blockedUser);
    								outToClient.writeUTF(blockedUser + " blocked");
    							}
						
    						}
    						break;
    					}
    				}
    			}
    			
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("UNBLOCK")) {
    				String blockedUser = message.substring(message.indexOf(" ") + 1, message.length()).trim();
    				for(OnlineUser us: this.onlineUsers) {
    					if(us.getUser().equals(this.username)) {
    						if(us.blockedList.size() == 0) {
    							outToClient.writeUTF("No users blocked");
    						}
    						else {
    							boolean unblocked = false;
    							for(int i = 0; i < us.blockedList.size(); i++) {
    								if(us.blockedList.get(i).equals(blockedUser)) {
    									outToClient.writeUTF(blockedUser +" unblocked");
    									us.blockedList.remove(i);
    									unblocked = true;
    									break;
    								}
    							}
    							if(!unblocked) {
    								outToClient.writeUTF(blockedUser + " was never blocked");
    							}
    						}
        					break;
    					}
    				}
    			}
    			
    			else if(message.substring(0, spaceIndex).toUpperCase().equals("BLOCKEDLIST")){
    				String retStr = "";
    				for(OnlineUser us: this.onlineUsers) {
    					if(us.getUser().equals(this.username)) {
    						if(us.blockedList.size() == 0) {
    							outToClient.writeUTF("No users blocked");
    						}
    						else {
    							for(String str: us.blockedList) {
    								retStr = retStr + str + ", ";
    							}
    							outToClient.writeUTF(retStr);
    							System.out.println(retStr);
    						}
    						break;
    					}
    				}
    			}
    			
    			else {
    				outToClient.writeUTF("Invalid Command");
    			}
    			
    		
    		}
    	}catch(IOException e) {e.printStackTrace();}
    	
    }
    
    
    public boolean login() /*throws IOException*/ {
    	int attempts = 0;
    	try {
    		///////////////////////////syncLock.lock();
        // Receives input from user
    	// Reader to read file line by line
		BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"));
    	String line = "----";
    	boolean correctUser = false;
    	boolean a = false;
    	while(!correctUser && attempts < 3) {
    		outToClient.writeUTF("Enter username: ");
            String user = inFromClient.readUTF().trim();
            outToClient.writeUTF("Enter password: ");
            String pass = inFromClient.readUTF().trim();
            String combo = user + " " + pass;
    		line = reader.readLine();
    		while(line != null) {
    			//System.out.println(line);
    			if(combo.equals(line.trim())) {
    				correctUser = true;
    				reader.close();
					///////////////////////////////////syncLock.unlock();
					outToClient.writeUTF("Login successful");
					setUsername(user);
					//tempUser.makeOnline();
    				return true;
    			}
    			line = reader.readLine();
    			
    		}
    		if (!correctUser) {
    			outToClient.writeUTF("Wrong username or password");
    			attempts++;
    			reader = new BufferedReader(new FileReader("credentials.txt"));
    		}
    	}
		outToClient.writeUTF("You've been locked out for " + blockDuration + " seconds");
    	reader.close();
    	////////////////////////////////////////////////////syncLock.unlock();
    	return false;
    	
    	
    	
    }catch(IOException e) {e.printStackTrace();
    	return false;} 
    }
    
    public void broadcast(String message, String user) {
    	try {
    		if(clients.size()>1) {
    			for(ServerHelper client: clients) {
    				if(!(user.equals(client.getUsername())) && client.isOnline())
    					client.outToClient.writeUTF("Broadcast from " + this.getUsername() + ": " + message);
    			}
    		}
    		
    	}catch (IOException e) {e.printStackTrace();}
    }
    
    public Socket getSocket() {
    	return connectionSocket;
    }
    
    public void setUsername(String user) {
    	this.username = user;
    }
    
    public String getUsername() {
    	return this.username;
    }
    
    public boolean isOnline() {
    	return this.isLoggedIn;
    }
    
    public void sendMessage(String user, String message) throws IOException {
    	String content = message.substring(message.indexOf(" ") + 1, message.length());
		ServerHelper recip = null;
		boolean flag = false;
		for(ServerHelper client: clients) {
			if(client.getUsername().equals(user) && client.isOnline()) {
				recip = client;
				flag = true;
			}	
			else if(client.getUsername().equals(user) && !(client.isOnline())) {
				flag = true;
				outToClient.writeUTF("User offline");
			}
		}
		if(!flag) {
			outToClient.writeUTF("User not found");
		}
		else if(flag && recip!= null) {
			recip.outToClient.writeUTF(this.getUsername() + ": " + content);
		}
    }
    
}

class OfflineMessage{
	String message;
	String intendedUser;
	String fromUser;
	
	public OfflineMessage(String intendedUser, String message, String fromUser) {
		this.message = message;
		this.intendedUser = intendedUser;
		this.fromUser = fromUser;
	}
	
	public String getOfflineMessage() {
		return message;
	}
	
	public String getIntendedUser() {
		return intendedUser;
	}
	
	public String getFromUser() {
		return fromUser;
	}
	
}

class OnlineUser{
	boolean online;
	String user;
	ArrayList<String> blockedList = new ArrayList<String>();
	
	public OnlineUser(String u, boolean on) {
		this.user = u;
		this.online = on;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public boolean isOnline() {
		return this.online;
	}
	
	public void makeOnline() {
		this.online = true;
	}
	
	public void makeOffline() {
		this.online = false;
	}
}






