package Server.TCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Server.Common.Middleware;

public class TCPMiddleware extends Middleware {

	private static String s_serverName = "Server";
	
	private static int port = 33303;

	public static void main(String args[]) {
		
		ExecutorService threadPool = null;
		ServerSocket serverSocket = null;
			
		// Create a server socket to listen for incoming connections
		try {
			// Create a new Server object
			TCPMiddleware middleware = new TCPMiddleware(s_serverName, args[0], args[1], args[2]);
			
			// Set up a server socket to listen for connections
			serverSocket = new ServerSocket(port);
			threadPool = Executors.newFixedThreadPool(50); // Maximum of 50 concurrent connections
			
			System.out.println("'" + s_serverName + "' middleware ready to receive TCP connections on port " + port);
			
			while (true) {
				// Receive a client connection request and dispatch it to a new thread
				Socket clientSocket = serverSocket.accept();
				Runnable clientThread = new TCPMiddlewareConnectionHandler(clientSocket, middleware);
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
	
	public TCPMiddleware(String p_name, String flightsHost, String carsHost, String roomsHost) {
		super(p_name, flightsHost, carsHost, roomsHost);
	}
}
