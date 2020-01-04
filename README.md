# Messaging-App
Matthew Burkher
COMP3331 at University of New South Wales 2019
*Java 9*

## Running
Server run command:

	java Server <server_port> <block_duration> <timeout>
Where server_port is the port number which the server is hosted on, block_duration is the time in milliseconds which a user is locked out after 3 consecutive failed login attempts, and timeout is the amount of time in milliseconds of inactivity before a user is automatically logged out.
  
Client run command:

 	java Client <server_IP> <server_port>
Where server_IP is the IP address the server is hosted on and server_port is the port number which the server is hosted on.

## Server Responsiblities

**User Authentication** - The server prompts a new client to input the username and password and authenticate the user. Username and password combinations are stored in a file called credentials.txt which will be in the same directory as the server program. Username and passwords are case-sensitive. Assume that each username and password will be on a separate line and that there will be one white space between the two. If the credentials are correct, the client is considered to be logged in and online, and a welcome message is displayed. On entering invalid credentials, the user is prompted to retry. After 3 consecutive failed attempts, the user is blocked for a duration of block_duration seconds and cannot login during this duration (even from another IP address). While a user is online, if someone uses the same username/password to log in (even from another IP address), then this new login attempt is denied.

**Timeout** - The server keeps track of all online users. If the server does not receive any commands from a user for a period of timeout seconds (timeout is a command line argument supplied to the server), then the server should automatically log this user out. Note that, to be considered active, a user must actively issue a command. The receipt of a message does not count.

**Presence Broadcasts** - The server sends a broadcast notification to all online users when a user logs in and logs out. 

**List of online users** - The server provides a list of users that are currently online in response to such a query from a user.

**Online history** – The sever provides a list of users that logged in for a user specified time in the past (e.g. users who logged in within the past 15 minutes).

**Message Forwarding** - The server should forward each instant message to the correct recipient assuming they are online.

**Offline Messaging** - When the recipient of a message is not logged in, the message will be saved by the server. When the recipient logs in next, the server will send all the unread messages stored for that user.

**Message Broadcast** – The server allows a user to broadcast a message to all online users.

**Blacklisting** - The server allows a user to block / unblock any other user.

## Client Commands

**message**  *user message* - Send *message* to *user* through the server. If the user is online then deliver the message immediately, else store the message for offline delivery. If <user> has blocked A, then a message saying that A has been blocked. If user is not listed in credentials file, then error message is displayed.
	
**broadcast** *message* - User A sends *message* to all users except A and users who have blocked A. Informs A that message couldn't be sent to some users.

**whoelse** - Displays list of all users currently online.

**whoeelsesince** *time* - Dsiplays list of all users currently online, and those who were logged on since a certain time ago, including users that may currently be offline.
	
**block** *user* - Blocks *user* from sending messages or broadcasts to user A. If user is already blocked or does not exist, appropriate error message is displayed.
	
**unblock** *user* - Unblocks *user*. If user is not blocked or does not exist, appropriate error message is displayed. 
	
**logout** - Logs user A out.
