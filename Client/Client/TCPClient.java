package Client;

import Server.Interface.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * Sockets usage based on the following Java Sockets tutorial:
 * https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/SocketProgramming/SocketProgram.html#overview
 */
public class TCPClient extends Client
{
	private static String s_serverHost = "localhost";
	private static int s_serverPort = 33303;
	private static String s_serverName = "Server";

	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			s_serverName = args[1];
		}
		if (args.length > 2)
		{
		  // TODO remove reference to rmi
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.TCPClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		try {
			TCPClient client = new TCPClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPClient()
	{
		super();
	}

	public void connectServer()
	{
		connectServer(s_serverHost, s_serverPort, s_serverName);
	}

	/*
	 * Attempts to connect to the server using a TCP socket and instantiates an object that can handle
	 * the same commands as the RMI, but using TCP instead.
	 */
	public void connectServer(String server, int port, String name)
	{
		Socket clientSocket = null;
		
		try {
			boolean first = true;
			while (true) {
				try {
					clientSocket = new Socket(server, port);
					
					// Create a hello message
					TCPMessage outgoingMessage = new TCPMessage(MessageType.HELLO);
					
					// Send the hello message to the server to test the connection
					// This is done to establish whether further communication will be possible (to catch errors early)
					ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
					output.writeObject(outgoingMessage);
					
					// Receive a response from the server
					ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
					TCPMessage incomingMessage = null;
					
					incomingMessage = (TCPMessage) input.readObject();
					
					// Check that the response is valid
					if (incomingMessage != null & incomingMessage.type == MessageType.HELLO) {
						
						// Create a new TCP Resource Manager to handle Client requests made in the interactive interface
						m_resourceManager = (IResourceManager) new TCPResourceManager(server, port);

						System.out.println("Connected to '" + name + "' server using TCP [" + server + ":" + port + "]");
						break;
					}
					else {
						throw new IOException(String.valueOf(incomingMessage));
					}
				}
				catch (ClassNotFoundException e) {
					System.err.println("Failed to establish connection with the server; invalid response received: " + e.getMessage());
					break;
				}
				catch (IOException e) {
					if (first) {
						System.out.println("Waiting for '" + name + "' server using TCP [" + server + ":" + port + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				}
				catch (IOException e) {
					System.err.println("Error closing client socket connection to server [" + server + ":" + port + "]");
				}
			}
		}
	}
}

