package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Vector;

import Server.Interface.*;

/*
 * Uses TCP sockets to implement the same remote commands as those offered using RMI.
 * 
 * Sockets usage based on the following Java tutorial:
 * https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/SocketProgramming/SocketProgram.html#overview
 */
public class TCPResourceManager implements IResourceManager {
	
	private String server;
	private int port;
	
	private String communicationError = "Communication error; no response received.";
	private String errorResponse = "Issue on the server; error response recieved.";

	// Constructor
	public TCPResourceManager(String server, int port) {
		this.server = server;
		this.port = port;
		System.out.println("Created TCPResourceManager for [" + this.server + ":" + this.port + "]");
	}
	
	// Sends a message to the server on the specified port using sockets
	// Receives and returns a message in response
	public TCPMessage sendMessage(Socket clientSocket, TCPMessage outgoingMessage) throws IOException, ClassNotFoundException {
		
		// Send the message to the server
		ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
		output.writeObject(outgoingMessage);
		
		// Receive a response from the server
		ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
		
		return (TCPMessage) input.readObject();
	}
	
	// Sends the message. If any errors occur, they are caught, printed, and null is returned.
	public TCPMessage sendMessageWithErrorHandling(TCPMessage outgoingMessage) {
		
		Socket clientSocket = null;
		
		try {
			clientSocket = new Socket(server, port);
			return sendMessage(clientSocket, outgoingMessage);
		}
		catch (ClassNotFoundException e) {
			System.err.println("Failed to establish connection with the server; invalid response received: " + e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Failed to establish connection with the server: " + e.getMessage());
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
		return null;
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {

		TCPMessage message = TCPMessage.newAddFlight(id, flightNum, flightSeats, flightPrice);
		
		TCPMessage response = sendMessageWithErrorHandling(message);
		
		if (response == null) throw new RemoteException(communicationError);
		else if (response.type == MessageType.ERROR) throw new RemoteException(errorResponse);
		else return response.booleanResult;
		
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int carPrice) throws RemoteException {
		TCPMessage message = TCPMessage.newAddCars(id, location, numCars, carPrice);

		TCPMessage response = sendMessageWithErrorHandling(message);
		
		if (response == null) throw new RemoteException(communicationError);
		else if (response.type == MessageType.ERROR) throw new RemoteException(errorResponse);
		else return response.booleanResult;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet.");
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean deleteCustomer(int id, int customerID) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public String queryCustomerInfo(int id, int customerID) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean reserveFlight(int id, int customerID, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean reserveCar(int id, int customerID, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean reserveRoom(int id, int customerID, String location) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}

	@Override
	public String getName() throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet");
	}
}
