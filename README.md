# Messaging-App
Matthew Burkher
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
**Timeout** - The server should keep track of all online users. If the server does not receive any commands from a user for a period of timeout seconds (timeout is a command line argument supplied to the server), then the server should automatically log this user out. Note that, to be considered active, a user must actively issue a command. The receipt of a message does not count.
**Presence Broadcasts** - The server should notify the presence/absence of other users logged into the server, i.e. send a broadcast notification to all online users when a user logs in and logs out. List of online users - The server should provide a list of users that are currently online in response to such a query from a user.
**Online history** – The sever should provide a list of users that logged in for a user specified time in the past (e.g. users who logged in within the past 15 minutes).
**Message Forwarding** - The server should forward each instant message to the correct recipient assuming they are online.
**Offline Messaging** - When the recipient of a message is not logged in (i.e. is offline), the message will be saved by the server. When the recipient logs in next, the server will send all the unread messages stored for that user (timestamps are not required).
**Message Broadcast** – The server should allow a user to broadcast a message to all online users. Offline messaging is not required for broadcast messages.
**Blacklisting** - The server should allow a user to block / unblock any other user. For example, if user A has blocked user B, B can no longer send messages to A i.e. the server should intercept such messages and inform B that the message cannot be forwarded. Blocked users also do not get presence notifications i.e. B will not be informed each time A logs in or logs out. 
