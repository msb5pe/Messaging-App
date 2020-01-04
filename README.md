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
