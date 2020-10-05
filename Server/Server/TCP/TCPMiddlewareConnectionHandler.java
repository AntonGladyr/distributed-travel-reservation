package Server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import Server.Common.Car;
import Server.Common.Customer;
import Server.Common.Flight;
import Server.Common.Middleware;
import Server.Common.Room;
import Server.Common.Trace;
import Server.Interface.MessageType;
import Server.Interface.TCPMessage;

public class TCPMiddlewareConnectionHandler implements Runnable {

	private Socket clientSocket;

	// The remote host name and port attached to the clientSocket
	private String clientHost;
	private int clientPort;
	
	// The hosts and port number to use for the resource managers
	private String flightsHost;
	private String carsHost;
	private String roomsHost;
	private int rmPort;

	// The middleware which will handle customer-specific operations
	Middleware middleware;

	public TCPMiddlewareConnectionHandler(Socket clientSocket, Middleware middleware) {
		this.clientSocket = clientSocket;
		this.middleware = middleware;
		this.clientHost = clientSocket.getInetAddress().getHostName();
		this.clientPort = clientSocket.getPort();

		// Truncate the ".CS.McGill.CA" part of the hostname
		int indexOfSuffix = this.clientHost.indexOf(".CS");
		if (indexOfSuffix != -1)
			this.clientHost = this.clientHost.substring(0, indexOfSuffix);
		
		// Get the resource manager hosts and port
		flightsHost = middleware.getFlightsHost();
		carsHost = middleware.getCarsHost();
		roomsHost = middleware.getRoomsHost();
		rmPort = middleware.getPortNum();
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
				case DELETE_FLIGHT:
				case QUERY_FLIGHT:
				case QUERY_FLIGHT_PRICE:
					response = forwardToFlights(request);
					break;
				case ADD_CARS:
				case DELETE_CARS:
				case QUERY_CARS:
				case QUERY_CARS_PRICE:
					response = forwardToCars(request);
					break;
				case ADD_ROOMS:
				case DELETE_ROOMS:
				case QUERY_ROOMS:
				case QUERY_ROOMS_PRICE:
					response = forwardToRooms(request);
					break;
				case NEW_CUSTOMER:
					response = handleAddNewCust(request);
					break;
				case NEW_CUSTOMER_ID:
					response = handleAddNewCustID(request);
					break;
				case DELETE_CUSTOMER:
					response = handleDeleteCustomer(request);
					break;
				case QUERY_CUSTOMER_INFO:
					response = handleQueryCustomerInfo(request);
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
			sendMessage(clientSocket, response);
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
							"Error closing client socket connection to server [" + clientHost + ":" + clientPort + "]");
				}
			}
		}
	}

	// Sends a message to the server on the specified port using sockets
	public void sendMessage(Socket recipient, TCPMessage outgoingMessage) throws IOException {

		// Send the message to the server
		ObjectOutputStream output = new ObjectOutputStream(recipient.getOutputStream());
		output.writeObject(outgoingMessage);
	}
	
	// Sends a message to the specified host using sockets, and awaits a response
	public TCPMessage sendMessageWithResponse(String host, int port, TCPMessage outgoingMessage) {
		
		// Create a socket
		Socket socket = null;
		
		try {
			socket = new Socket(host, port);
			
			// Send the message to the server
			sendMessage(socket, outgoingMessage);
			
			// Receive a response
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			TCPMessage response = (TCPMessage) input.readObject();
			
			// Check that the response is valid
			if (response != null & response.type != MessageType.ERROR) {
				return response;
			}
			else {
				return null;
			}
		}
		catch (ClassNotFoundException e) {
			System.err.println("Failed to establish connection with the server; invalid response received: " + e.getMessage());
			return null;
		}
		catch (IOException e) {
			System.out.println("Failed to connect to server using TCP [" + host + ":" + port + "]");
			return null;
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			return null;
		}
		finally {
			if (socket != null) {
				try {
					socket.close();
				}
				catch (IOException e) {
					System.err.println("Error closing RM socket connection to server [" + host + ":" + port + "]");
				}
			}
		}
	}

	// Handles messages of type "HELLO"
	private TCPMessage handleHello() {
		Trace.info("MW::Received connection request from [" + clientHost + ":" + clientPort + "]");

		// Send a hello message in response
		return new TCPMessage(MessageType.HELLO);
	}
	
	// Forwards the request to the flights resource manager and returns its response
	private TCPMessage forwardToFlights(TCPMessage r) {
		Trace.info("MW::Received " + r.type + " request from [" + clientHost + ":" + clientPort + "], forwarding to flightsHost");
		return sendMessageWithResponse(flightsHost, rmPort, r);
	}
	
	// Forwards the request to the cars resource manager and returns its response
	private TCPMessage forwardToCars(TCPMessage r) {
		Trace.info("MW::Received " + r.type + " request from [" + clientHost + ":" + clientPort + "], forwarding to carsHost");
		return sendMessageWithResponse(carsHost, rmPort, r);
	}
	
	// Forwards the request to the rooms resource manager and returns its response
	private TCPMessage forwardToRooms(TCPMessage r) {
		Trace.info("MW::Received " + r.type + " request from [" + clientHost + ":" + clientPort + "], forwarding to roomsHost");
		return sendMessageWithResponse(roomsHost, rmPort, r);
	}

	// Handles messages of type QUERY_CUSTOMER_INFO
	private TCPMessage handleQueryCustomerInfo(TCPMessage r) throws RemoteException {
		Trace.info("MW::Received QUERY_CUSTOMER_INFO request from [" + clientHost + ":" + clientPort + "], processing locally");

		r.stringResult = middleware.queryCustomerInfo(r.id, r.customerID);
		return r;
	}
	
	// Handles messages of type NEW_CUSTOMER
	private TCPMessage handleAddNewCust(TCPMessage r) throws RemoteException {
		Trace.info("MW::Received NEW_CUSTOMER request from [" + clientHost + ":" + clientPort + "], processing locally");

		r.intResult = middleware.newCustomer(r.id);
		return r;
	}

	// Handles messages of type NEW_CUSTOMER_ID
	private TCPMessage handleAddNewCustID(TCPMessage r) throws RemoteException {
		Trace.info("MW::Received NEW_CUSTOMER_ID request from [" + clientHost + ":" + clientPort + "], processing locally");

		r.booleanResult = middleware.newCustomer(r.id, r.cid);
		return r;
	}

	// Handles messages of type DELETE_CUSTOMER
	private TCPMessage handleDeleteCustomer(TCPMessage r) throws RemoteException {
		Trace.info("MW::Received DELETE_CUSTOMER request from [" + clientHost + ":" + clientPort + "], processing locally");

		r.booleanResult = middleware.deleteCustomer(r.id, r.customerID);
		return r;
	}

	// Handles messages of type RESERVE_FLIGHT
	private TCPMessage handleReserveFlight(TCPMessage r) {
		Trace.info("MW::Received RESERVE_FLIGHT request from [" + clientHost + ":" + clientPort + "], checking customer and forwarding to flightsHost");

		Customer customer = middleware.getCustomer(r.id, r.customerID);
		
		// If customer does not exist, return -1
		if (customer == null) {
			r.intResult = -1;
			return r;
		}
		
		else {
			// Reserve the flight on the flight server
			TCPMessage response = forwardToFlights(r);
			
			// Reserve the flight in the customer database
			if (response != null && response.type != MessageType.ERROR && response.intResult != -1) {
				
				customer.reserve(Flight.getKey(r.flightNum), String.valueOf(r.flightNum), r.flightPrice);
				middleware.writeData(r.id, customer.getKey(), customer);
			}
			
			return response;
		}
	}

	// Handles messages of type RESERVE_CAR
	private TCPMessage handleReserveCar(TCPMessage r) {
		Trace.info("MW::Received RESERVE_CAR request from [" + clientHost + ":" + clientPort + "], checking customer and forwarding to carsHost");

		Customer customer = middleware.getCustomer(r.id, r.customerID);
		
		// If customer does not exist, return -1
		if (customer == null) {
			r.intResult = -1;
			return r;
		}
		
		else {
			// Reserve the car on the car server
			TCPMessage response = forwardToCars(r);
			
			// Reserve the car in the customer database
			if (response != null && response.type != MessageType.ERROR && response.intResult != -1) {
				
				customer.reserve(Car.getKey(r.location), r.location, r.intResult);
				middleware.writeData(r.id, customer.getKey(), customer);
			}
			
			return response;
		}
	}

	// Handles messages of type RESERVE_ROOM
	private TCPMessage handleReserveRoom(TCPMessage r) {
		Trace.info("MW::Received RESERVE_ROOM request from [" + clientHost + ":" + clientPort + "], checking customer and forwarding to roomsHost");

		Customer customer = middleware.getCustomer(r.id, r.customerID);
		
		// If customer does not exist, return -1
		if (customer == null) {
			r.intResult = -1;
			return r;
		}
		
		else {
			// Reserve the room on the room server
			TCPMessage response = forwardToRooms(r);
			
			// Reserve the room in the customer database
			if (response != null && response.type != MessageType.ERROR && response.intResult != -1) {
				
				customer.reserve(Room.getKey(r.location), r.location, r.intResult);
				middleware.writeData(r.id, customer.getKey(), customer);
			}
			
			return response;
		}
	}

	// Handles messages of type BUNDLE
	private TCPMessage handleBundle(TCPMessage r) {
		Trace.info("MW::Received BUNDLE request from [" + clientHost + ":" + clientPort + "]");
		Trace.warn("MW::BUNDLE NOT IMPLEMENTED YET");

		r.booleanResult = false;
		return r;
	}
}
