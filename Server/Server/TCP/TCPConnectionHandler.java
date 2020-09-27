package Server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Server.Interface.MessageType;
import Server.Interface.TCPMessage;

public class TCPConnectionHandler implements Runnable {
	
	private Socket clientSocket;
	
	// The remote host name and port attached to the clientSocket
	private String hostName;
	private int port;
	
	public TCPConnectionHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.hostName = clientSocket.getInetAddress().getHostName();
		this.port = clientSocket.getPort();

		// Truncate the ".CS.McGill.CA" part of the hostname
		int indexOfSuffix = this.hostName.indexOf(".CS");
		if (indexOfSuffix != -1) this.hostName = this.hostName.substring(0, indexOfSuffix);
	}

	@Override
	public void run() {
		
		try {
			// Read the message sent by the client
			ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
			TCPMessage incomingMessage = (TCPMessage) input.readObject();
			
			if (incomingMessage != null) {
				switch (incomingMessage.type) {
				case HELLO:
					handleHello();
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
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mError handling incoming TCP message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * Handles messages of type "HELLO"
	 */
	private void handleHello() {
		System.out.println("Received connection request from [" + hostName + ":" + port + "]");
		try {
			// Send a hello message in response
			ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
			TCPMessage outgoingMessage = new TCPMessage(MessageType.HELLO);
			output.writeObject(outgoingMessage);
			System.out.println("Sent a response to accept connection request from [" + hostName + ":" + port + "]");
		}
		catch (IOException e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mFailed to send hello message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
