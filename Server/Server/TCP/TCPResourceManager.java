// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.TCP;

import Server.Interface.*;
import Server.Common.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Sockets usage based on the following Java Sockets tutorial:
 * https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/SocketProgramming/SocketProgram.html#overview
 */
public class TCPResourceManager extends ResourceManager 
{
	private static String s_serverName = "Server";
	
	private static int port = 33303;

	public static void main(String args[])
	{
		if (args.length > 0)
		{
			s_serverName = args[0];
		}
		
		ExecutorService threadPool = null;
		ServerSocket serverSocket = null;
			
		// Create a server socket to listen for incoming connections
		try {
			// Create a new Server object
			TCPResourceManager server = new TCPResourceManager(s_serverName);
			
			// Set up a server socket to listen for connections
			serverSocket = new ServerSocket(port);
			threadPool = Executors.newFixedThreadPool(50); // Maximum of 50 concurrent connections
			
			System.out.println("'" + s_serverName + "' resource manager server ready to receive TCP connections on port " + port);
			
			while (true) {
				// Receive a client connection request and dispatch it to a new thread
				Socket clientSocket = serverSocket.accept();
				Runnable clientThread = new TCPConnectionHandler(clientSocket);
				threadPool.execute(clientThread);
			}
		}
		catch (IOException e) {
			System.err.println((char)27 + "[31;1mSocket IOException: " + (char)27 + "[0m");
			e.printStackTrace();
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
		}
		finally {
			if (threadPool != null) threadPool.shutdown();
			
			if (serverSocket != null) {
				try {
					serverSocket.close();
				}
				catch (IOException e) {
					System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mIOException while closing ServerSocket");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	public TCPResourceManager(String name)
	{
		super(name);
	}
}
