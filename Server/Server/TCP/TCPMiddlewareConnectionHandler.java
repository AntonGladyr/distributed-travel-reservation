package Server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Server.Common.Car;
import Server.Common.Customer;
import Server.Common.Flight;
import Server.Common.Middleware;
import Server.Common.RMHashMap;
import Server.Common.ReservedItem;
import Server.Common.Room;
import Server.Common.Trace;
import Server.Interface.InvalidTransactionException;
import Server.Interface.MessageType;
import Server.Interface.TCPMessage;
import Server.Interface.TransactionAbortedException;

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
	
	// Used in the bundle request
	private static final int MAX_THREADS = 3;
	private static final int WAIT_RESPONSE = 5;  // Seconds

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
	private TCPMessage handleQueryCustomerInfo(TCPMessage r) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
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
		Trace.info("MW::Received DELETE_CUSTOMER(" + r.id + ", " + r.customerID + ") request from [" + clientHost + ":" + clientPort + "], processing locally");

		Customer customer = middleware.getCustomer(r.id, r.customerID);
		if (customer == null)
		{
			Trace.warn("MW::deleteCustomer(" + r.id + ", " + r.customerID + ") failed--customer doesn't exist");
			r.booleanResult = false;
			return r;
		}

		// Increase the reserved numbers of all reservable items which the customer reserved. 
		RMHashMap reservations = customer.getReservations();
		
		// Hashmap for storing keys and number of reservations
		HashMap<String, Integer> reservationsMap = new HashMap<String, Integer>(); // <key, count>
		
		for (String reservedKey : reservations.keySet())
		{
			ReservedItem reserveditem = customer.getReservedItem(reservedKey);
			reservationsMap.put(reserveditem.getKey(), reserveditem.getCount());	
		}
		
		// Cancel item reservations in all resource managers. Return false if failed
		if (!cancelItemSet(r.id, reservationsMap)) {
			r.booleanResult = false;
			return r;
		}
		
		// Remove the customer from the storage
		middleware.removeData(r.id, customer.getKey());
		Trace.info("MW::deleteCustomer(" + r.id + ", " + r.customerID + ") succeeded");
		
		r.booleanResult = true;
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
				
				customer.reserve(Flight.getKey(r.flightNum), String.valueOf(r.flightNum), response.intResult);
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
				
				customer.reserve(Car.getKey(r.location), r.location, response.intResult);
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
				
				customer.reserve(Room.getKey(r.location), r.location, response.intResult);
				middleware.writeData(r.id, customer.getKey(), customer);
			}
			
			return response;
		}
	}

	// Handles messages of type BUNDLE
	private TCPMessage handleBundle(TCPMessage r) {
		Trace.info("MW::Received BUNDLE request from [" + clientHost + ":" + clientPort + "]");
		
		if (r.flightNumbers.isEmpty()) {
			r.booleanResult = false;
			return r;
		}
		
		boolean available = checkItemsAvailability(r);
		
		if (available) r.booleanResult = reserveItemsBundle(r);
		else r.booleanResult = false;
		return r;
	}
	
	// Checks if the items in a BUNDLE request are available
	private boolean checkItemsAvailability (TCPMessage r) {
		
		Trace.info("MW::Processing BUNDLE[checkItemsAvailability](" + r.id + ", " + r.customerID + ", " +
					   r.flightNumbers.toString() + "," + r.location + ", " + r.car + ", " + r.room + ")");
		boolean flightsResult = true;
		boolean carResult = true;
		boolean roomResult = true;
		
		// Threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		Future<Boolean> flightsFuture = null;
		Future<Boolean> carFuture = null;
		Future<Boolean> roomFuture = null;

		// If customer does not exist, return false
		Customer customer = middleware.getCustomer(r.id, r.customerID);
		if (customer == null) return false;
		
		// Send asynchronous requests to the resource managers
		// Check the flights' availability
		if (!r.flightNumbers.isEmpty()) {
			
			Callable<Boolean> checkFlightsCallback = () -> {	
				Trace.info("MW::BUNDLE forwarding checkFlightList(" + r.id + ", " + r.flightNumbers + ", " + r.location + ") to flightsHost");
				
				// Send a CHECK_FLIGHT_LIST request to the flights server
				TCPMessage checkRequest = TCPMessage.newCheckFlightList(r.id, r.flightNumbers, r.location);
				TCPMessage checkResponse = forwardToFlights(checkRequest);
				return checkResponse.booleanResult;
			};

			flightsFuture = executor.submit(checkFlightsCallback);
		}
		
		// Check the car availability
		if (r.car) {
			
			Callable<Boolean> checkCarCallback = () -> {
				Trace.info("MW::BUNDLE forwarding queryCars(" + r.id + ", " + r.location + ") to carsHost");
				
				// Send a QUERY_CARS request to the cars server
				TCPMessage checkRequest = TCPMessage.newQueryCars(r.id, r.location);
				TCPMessage checkResponse = forwardToCars(checkRequest);
				return checkResponse.intResult > 0;
			};
			
			carFuture = executor.submit(checkCarCallback);
		}

		// Check the room availability
		if (r.room) {
			
			Callable<Boolean> checkRoomCallback = () -> {
				Trace.info("MW::BUNDLE forwarding queryRooms(" + r.id + ", " + r.location + ") to roomsHost");
				
				// Send a QUERY_ROOMS request to the rooms server
				TCPMessage checkRequest = TCPMessage.newQueryRooms(r.id, r.location);
				TCPMessage checkResponse = forwardToRooms(checkRequest);
				return checkResponse.intResult > 0;
			};
			
			roomFuture = executor.submit(checkRoomCallback);
		}	

		// Wait for previously submitted tasks to execute, and then terminate the executor
		executor.shutdown();

		// Get the results
		if (!r.flightNumbers.isEmpty()) flightsResult = handleThreadResult(flightsFuture);
		if (r.car) carResult = handleThreadResult(carFuture);
		if (r.room) roomResult = handleThreadResult(roomFuture);	
		
		return flightsResult && carResult && roomResult;
	}
	
	// Extracts the result from a Future<Boolean> object
	private boolean handleThreadResult(Future<Boolean> itemsFuture) {
		try {
			// Limit the thread response time
			return itemsFuture.get(WAIT_RESPONSE, TimeUnit.SECONDS);
		}
		catch(Exception e) {
			Trace.warn("RM::Thread exception: " + e.getMessage());
			itemsFuture.cancel(true);
			return false;
		}
	}

	// Reserves the items in a BUNDLE request
	private boolean reserveItemsBundle(TCPMessage r) {

		Trace.info("MW::Processing BUNDLE[reserveItemsBundle](" + r.id + ", " + r.customerID + ", " +
					   r.flightNumbers.toString() + "," + r.location + ", " + r.car + ", " + r.room + ")");
		
		boolean flightsResult = true;
		boolean carResult = true;
		boolean roomResult = true;

		// Threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		Future<Boolean> flightsFuture = CompletableFuture.completedFuture(true);
		Future<Boolean> carFuture = CompletableFuture.completedFuture(true);
		Future<Boolean> roomFuture = CompletableFuture.completedFuture(true);

		// If customer does not exist, return false
		Customer customer = middleware.getCustomer(r.id, r.customerID);
		if (customer == null) return false;

		// Send asynchronous requests to the resource managers
		// Reserve flights
		if (!r.flightNumbers.isEmpty()) {
			Callable<Boolean> reserveFlightsCallback = () -> {
				
				Trace.info("MW::BUNDLE forwarding reserveFlights(" + r.id + ", " + r.customerID + ", " + r.flightNumbers + ", " + r.location + ") to flightsHost");
				
				// Send a RESERVE_FLIGHT_LIST request to the flights server
				TCPMessage request = TCPMessage.newReserveFlightList(r.id, r.customerID, r.flightNumbers, r.location);
				TCPMessage response = forwardToFlights(request);
				
				// Check the result
				Vector<Integer> prices = response.vectorIntResult;
				if (prices.isEmpty()) return false;
				
				// Reserve the flights in the customer database
				for (int i = 0; i < prices.size(); i++) {
					int flightNum = Integer.parseInt(r.flightNumbers.get(i));
					customer.reserve(Flight.getKey(flightNum), String.valueOf(flightNum), prices.get(i));
					middleware.writeData(r.id, customer.getKey(), customer);
				}
				
				return true;
			};

			flightsFuture = executor.submit(reserveFlightsCallback);
		}
		
		// Reserve car
		if (r.car) {	
			Callable<Boolean> reserveCarCallback = () -> {
				
				Trace.info("MW::BUNDLE forwarding reserveCar(" + r.id + ", " + r.customerID + ", " + r.location + ") to carsHost");
				
				// Send a RESERVE_CAR request to the cars server
				TCPMessage request = TCPMessage.newReserveCar(r.id, r.customerID, r.location);
				TCPMessage response = forwardToCars(request);
				
				// Check the result
				if (response.intResult == -1) return false;
				
				// Reserve the car in the customer database
				customer.reserve(Car.getKey(r.location), r.location, response.intResult);
				middleware.writeData(r.id, customer.getKey(), customer);
				return true;
			};
			
			carFuture = executor.submit(reserveCarCallback);
		}
		
		// Reserve room
		if (r.room) {	
			Callable<Boolean> reserveRoomCallback = () -> {
				
				Trace.info("MW::BUNDLE forwarding reserveRoom(" + r.id + ", " + r.customerID + ", " + r.location + ") to roomsHost");
				
				// Send a RESERVE_ROOM request to the rooms server
				TCPMessage request = TCPMessage.newReserveRoom(r.id, r.customerID, r.location);
				TCPMessage response = forwardToRooms(request);
				
				// Check the result
				if (response.intResult == -1) return false;
				
				// Reserve the room in the customer database
				customer.reserve(Room.getKey(r.location), r.location, response.intResult);
				middleware.writeData(r.id, customer.getKey(), customer);
				return true;
			};
			
			roomFuture = executor.submit(reserveRoomCallback);
		}
		
		// Wait for previously submitted tasks to execute, and then terminate the executor
		executor.shutdown();

		// Get the results
		if (!r.flightNumbers.isEmpty()) flightsResult = handleThreadResult(flightsFuture);
		if (r.car) carResult = handleThreadResult(carFuture);
		if (r.room) roomResult = handleThreadResult(roomFuture);	
		
		return flightsResult && carResult && roomResult;
	}
	
	// Used by DELETE_CUSTOMER to cancel all of a customer's reservations
	private boolean cancelItemSet(int xid, HashMap<String, Integer> reservationsMap) {
		// Threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		
		TCPMessage request = TCPMessage.newCancelItemReservations(xid, reservationsMap);
		
		// Cancel all reservations
		Callable<Boolean> cancelFlights = () -> {
			
			// Send a CANCEL_ITEM_RESERVATIONS request to the flights server
			TCPMessage response = forwardToFlights(request);
			return response.booleanResult;
		};
		
		Callable<Boolean> cancelCars = () -> {
			
			// Send a CANCEL_ITEM_RESERVATIONS request to the cars server
			TCPMessage response = forwardToCars(request);
			return response.booleanResult;
		};
		
		Callable<Boolean> cancelRooms = () -> {
			
			// Send a CANCEL_ITEM_RESERVATIONS request to the rooms server
			TCPMessage response = forwardToRooms(request);
			return response.booleanResult;
		};
		
		// Submit value-returning tasks for execution in separate threads
		Future<Boolean> flightsFuture = executor.submit(cancelFlights);
		Future<Boolean> carsFuture = executor.submit(cancelCars);
		Future<Boolean> roomsFuture = executor.submit(cancelRooms);
		
		// Wait for previously submitted tasks to execute, and then terminate the executor
		executor.shutdown();
	
		// Get the results
		boolean flightsResult = handleThreadResult(flightsFuture);
		boolean carResult = handleThreadResult(carsFuture);
		boolean roomResult = handleThreadResult(roomsFuture);

		return flightsResult && carResult && roomResult;
	}
}
