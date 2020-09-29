package Server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Server.Common.ResourceManager;
import Server.Interface.MessageType;
import Server.Interface.TCPMessage;

public class TCPConnectionHandler implements Runnable {
	
	private Socket clientSocket;
	
	// The remote host name and port attached to the clientSocket
	private String hostName;
	private int port;
	
	// The resource manager which will process all client requests
	ResourceManager resourceManager;
	
	public TCPConnectionHandler(Socket clientSocket, ResourceManager resourceManager) {
		this.clientSocket = clientSocket;
		this.resourceManager = resourceManager;
		this.hostName = clientSocket.getInetAddress().getHostName();
		this.port = clientSocket.getPort();

		// Truncate the ".CS.McGill.CA" part of the hostname
		int indexOfSuffix = this.hostName.indexOf(".CS");
		if (indexOfSuffix != -1) this.hostName = this.hostName.substring(0, indexOfSuffix);
	}

	@Override
	public void run() {
		
		// TCPMessage to send in response (either a success or an error)
		TCPMessage response = null;
		
		// Read and handle the message sent by the client
		try {
			ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
			TCPMessage incomingMessage = (TCPMessage) input.readObject();
			
			if (incomingMessage != null) {
				
				switch (incomingMessage.type) {
				case HELLO:
					response = handleHello();
					break;
				default:
					throw new IOException("Unrecognized TCPMessage.type: " + incomingMessage.type);
				}
			}
			else {
				throw new IOException("Invalid TCPMessage: " + incomingMessage);
			}
		}
		catch (Exception e) {
			// In the case of an exception, prepare to send an error response
			response = new TCPMessage(MessageType.ERROR);
			
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mError handling incoming TCP message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		// Send a response
		try {
			sendMessage(response);
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mError sending an outgoing TCP message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		// Close the connection
		finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				}
				catch (IOException e) {
					System.err.println("Error closing client socket connection to server [" + hostName + ":" + port + "]");
				}
			}
		}
	}
	
	// Sends a message to the server on the specified port using sockets
	public void sendMessage(TCPMessage outgoingMessage) throws IOException{
		
		// Send the message to the server
		ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
		output.writeObject(outgoingMessage);
	}
	
	// Handles messages of type "HELLO"
	private TCPMessage handleHello() {
		System.out.println("Received connection request from [" + hostName + ":" + port + "]");
		
		// Send a hello message in response
		return new TCPMessage(MessageType.HELLO);
	}
}
