package Server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

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
		if (indexOfSuffix != -1)
			this.hostName = this.hostName.substring(0, indexOfSuffix);
	}

	@Override
	public void run() {

		// TCPMessage to send in response (either a success or an error)
		TCPMessage response = null;

		// Read and handle the message sent by the client
		try {
			ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
			TCPMessage request = (TCPMessage) input.readObject();

			if (request != null) {

				switch (request.type) {
				case HELLO:
					response = handleHello();
					break;
				case ADD_FLIGHT:
					response = handleAddFlight(request);
					break;
				case ADD_CARS:
					response = handleAddCars(request);
					break;
				case ADD_ROOMS:
					response = handleAddRooms(request);
					break;
				case NEW_CUSTOMER:
					response = handleAddNewCust(request);
					break;
				case NEW_CUSTOMER_ID:
					response = handleAddNewCustID(request);
					break;
				case DELETE_FLIGHT:
					response = handleDeleteFlight(request);
					break;
				case DELETE_CARS:
					response = handleDeleteCars(request);
					break;
				case DELETE_ROOMS:
					response = handleDeleteRooms(request);
					break;
				case DELETE_CUSTOMER:
					response = handleDeleteCustomer(request);
					break;
				case QUERY_FLIGHT:
					response = handleQueryFlight(request);
					break;
				case QUERY_CARS:
					response = handleQueryCars(request);
					break;
				case QUERY_ROOMS:
					response = handleQueryRooms(request);
					break;
				case QUERY_CUSTOMER_INFO:
					response = handleQueryCustomerInfo(request);
					break;
				case QUERY_FLIGHT_PRICE:
					response = handleQueryFlightPrice(request);
					break;
				case QUERY_CARS_PRICE:
					response = handleQueryCarsPrice(request);
					break;
				case QUERY_ROOMS_PRICE:
					response = handleQueryRoomsPrice(request);
					break;
				case RESERVE_FLIGHT:
					response = handleReserveFlight(request);
					break;
				case RESERVE_CAR:
					response = handleReserveCar(request);
					break;
				case RESERVE_ROOM:
					response = handleReserveRoom(request);
					break;
				case BUNDLE:
					response = handleBundle(request);
					break;
				default:
					throw new IOException("Unrecognized TCPMessage.type: " + request.type);
				}
			} else {
				throw new IOException("Invalid TCPMessage: " + request);
			}
		} catch (Exception e) {
			// In the case of an exception, prepare to send an error response
			response = new TCPMessage(MessageType.ERROR);

			System.err.println(
					(char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mError handling incoming TCP message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		// Send a response
		try {
			sendMessage(response);
		} catch (Exception e) {
			System.err.println(
					(char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mError sending an outgoing TCP message");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		// Close the connection
		finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.err.println(
							"Error closing client socket connection to server [" + hostName + ":" + port + "]");
				}
			}
		}
	}

	// Sends a message to the server on the specified port using sockets
	public void sendMessage(TCPMessage outgoingMessage) throws IOException {

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

	// Handles messages of type "ADD_FLIGHT"
	private TCPMessage handleAddFlight(TCPMessage r) throws RemoteException {
		System.out.println("Received ADD_FLIGHT request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.addFlight(r.id, r.flightNum, r.flightSeats, r.flightPrice);

		return r;
	}

	// Handles messages of type "ADD_CARS"
	private TCPMessage handleAddCars(TCPMessage r) throws RemoteException {
		System.out.println("Received ADD_CARS request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.addCars(r.id, r.location, r.numCars, r.price);
		return r;
	}

	// Handles messages of type ADD_ROOMS
	private TCPMessage handleAddRooms(TCPMessage r) throws RemoteException {
		System.out.println("Received ADD_ROOMS request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.addRooms(r.id, r.location, r.numRooms, r.price);
		return r;
	}

	// Handles messages of type NEW_CUSTOMER
	private TCPMessage handleAddNewCust(TCPMessage r) throws RemoteException {
		System.out.println("Received NEW_CUSTOMER request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.newCustomer(r.id);
		return r;
	}

	// Handles messages of type NEW_CUSTOMER_ID
	private TCPMessage handleAddNewCustID(TCPMessage r) throws RemoteException {
		System.out.println("Received NEW_CUSTOMER_ID request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.newCustomer(r.id, r.cid);
		return r;
	}

	// Handles messages of type DELETE_FLIGHT
	private TCPMessage handleDeleteFlight(TCPMessage r) throws RemoteException {
		System.out.println("Received DELETE_FLIGHT request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.deleteFlight(r.id, r.flightNum);
		return r;
	}

	// Handles messages of type DELETE_CARS
	private TCPMessage handleDeleteCars(TCPMessage r) throws RemoteException {
		System.out.println("Received DELETE_CARS request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.deleteCars(r.id, r.location);
		return r;
	}

	// Handles messages of type DELETE_ROOMS
	private TCPMessage handleDeleteRooms(TCPMessage r) throws RemoteException {
		System.out.println("Received DELETE_ROOMS request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.deleteRooms(r.id, r.location);
		return r;
	}

	// Handles messages of type DELETE_CUSTOMER
	private TCPMessage handleDeleteCustomer(TCPMessage r) throws RemoteException {
		System.out.println("Received DELETE_CUSTOMER request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.deleteCustomer(r.id, r.customerID);
		return r;
	}

	// Handles messages of type QUERY_FLIGHT
	private TCPMessage handleQueryFlight(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERY_FLIGHT request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryFlight(r.id, r.flightNum);
		return r;
	}

	// Handles messages of type QUERY_CARS
	private TCPMessage handleQueryCars(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERY_CARS request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryCars(r.id, r.location);
		return r;
	}

	// Handles messages of type QUERY_ROOMS
	private TCPMessage handleQueryRooms(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERY_ROOMS request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryRooms(r.id, r.location);
		return r;
	}

	// Handles messages of type QUERY_CUSTOMER_INFO
	private TCPMessage handleQueryCustomerInfo(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERY_CUSTOMER_INFO request from [" + hostName + ":" + port + "]");

		r.stringResult = resourceManager.queryCustomerInfo(r.id, r.customerID);
		return r;
	}

	// Handles messages of type QUERY_FLIGHT_PRICE
	private TCPMessage handleQueryFlightPrice(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERRY_FLIGHT_PRICE request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryFlightPrice(r.id, r.flightNum);
		return r;
	}

	// Handles messages of type QUERY_CARS_PRICE
	private TCPMessage handleQueryCarsPrice(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERY_CARS_PRICE request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryCarsPrice(r.id, r.location);
		return r;
	}

	// Handles messages of type QUERRY_ROOMS_PRICE
	private TCPMessage handleQueryRoomsPrice(TCPMessage r) throws RemoteException {
		System.out.println("Received QUERRY_ROOMS_PRICE request from [" + hostName + ":" + port + "]");

		r.intResult = resourceManager.queryRoomsPrice(r.id, r.location);
		return r;
	}

	// Handles messages of type RESERVE_FLIGHT
	private TCPMessage handleReserveFlight(TCPMessage r) throws RemoteException {
		System.out.println("Received RESERVE_FLIGHT request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.reserveFlight(r.id, r.customerID, r.flightNum);
		return r;
	}

	// Handles messages of type RESERVE_CAR
	private TCPMessage handleReserveCar(TCPMessage r) throws RemoteException {
		System.out.println("Received RESERVE_CAR request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.reserveCar(r.id, r.customerID, r.location);
		return r;
	}

	// Handles messages of type RESERVE_ROOM
	private TCPMessage handleReserveRoom(TCPMessage r) throws RemoteException {
		System.out.println("Received RESERVE_ROOM request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.reserveRoom(r.id, r.customerID, r.location);
		return r;
	}

	// Handles messages of type BUNDLE
	private TCPMessage handleBundle(TCPMessage r) throws RemoteException {
		System.out.println("Received BUNDLE request from [" + hostName + ":" + port + "]");

		r.booleanResult = resourceManager.bundle(r.id, r.customerID, r.flightNumbers, r.location, r.car, r.room);
		return r;
	}

}
