// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.rmi.NotBoundException;
import java.io.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;


public class Middleware implements IResourceManager
{		
	// group number as unique identifier
	private static final String s_rmiPrefix = "group_03_";
	private static final int MAX_THREADS = 3;
	private static final int WAIT_RESPONSE = 3;
	
	private String flightsHost;
	private String carsHost;
	private String roomsHost;
	private final String flightsServerName = "Flights";
	private final String carsServerName = "Cars";
	private final String roomsServerName = "Rooms";
	protected final int portNum = 33303;
	
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();

	public Middleware(
		String p_name,
		String flightsHost,
		String carsHost,
		String roomsHost
	) {
		m_name = p_name;
		this.flightsHost = flightsHost;
		this.carsHost = carsHost;
		this.roomsHost = roomsHost;

		// Set the security policy
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}
	
	public String getFlightsHost() { return flightsHost; }
	public String getCarsHost() { return carsHost; }
	public String getRoomsHost() { return roomsHost; }
	public int getPortNum() { return portNum; }
	
	// Reads a data item
	protected RMItem readData(int xid, String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	public void writeData(int xid, String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}
	
	// Check if customer exists
	public Customer getCustomer(int xid, int customerID)
	{
		Trace.info("RM::getCustomer(" + xid + ", customer=" + customerID + ") called" );
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::getCustomer(" + xid + ", " + customerID + ")  failed--customer doesn't exist");	
		}

		return customer;
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{	
		Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
		IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
		}
		else {
			Trace.warn("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ")" + 
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		IResourceManager m_resourceManager = connectServer(carsHost, portNum, carsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.addCars(xid, location, count, price);
		}
		else {
			Trace.warn("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ")" +
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		IResourceManager m_resourceManager = connectServer(roomsHost, portNum, roomsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.addRooms(xid, location, count, price);
		}
		else {
			Trace.warn("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ")" + 
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) throws RemoteException
	{
		Trace.info("RM::deleteFlight(" + xid + ", " + flightNum + ") called");
		IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.deleteFlight(xid, flightNum);
		}
		else {
			Trace.warn("RM::deleteFlight(" + xid + ", " + flightNum + ")" +
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws RemoteException
	{
		Trace.info("RM::deleteCars(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(carsHost, portNum, carsServerName);
		if (m_resourceManager != null) {
			return m_resourceManager.deleteCars(xid, location);
		}
		else {
			Trace.warn("RM::deleteCars(" + xid + ", " + location + ")" +
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws RemoteException
	{
		Trace.info("RM::deleteRooms(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(roomsHost, portNum, roomsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.deleteRooms(xid, location);
		}
		else {
			Trace.warn("RM::deleteRooms(" + xid + ", " + location + ")" + 
				   "  could not connect to the resource manager server.");
			return false;
		}
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) throws RemoteException
	{
		Trace.info("RM::queryFlight(" + xid + ", " + flightNum + ") called");
		IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
		if (m_resourceManager != null) {
			return m_resourceManager.queryFlight(xid, flightNum);
		}
		else {
			Trace.warn("RM::queryFlight(" + xid + ", " + flightNum + ")" + 
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws RemoteException
	{
		Trace.info("RM::queryCars(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(carsHost, portNum, carsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.queryCars(xid, location);
		}
		else {
			Trace.warn("RM::queryCars(" + xid + ", " + location + ")" +
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws RemoteException
	{
		Trace.info("RM::queryRooms(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(roomsHost, portNum, roomsServerName);
		if (m_resourceManager != null) {			
			return m_resourceManager.queryRooms(xid, location);
		}
		else {
			Trace.warn("RM::queryRooms(" + xid + ", " + location + ")" + 
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) throws RemoteException
	{
		Trace.info("RM::queryFlightPrice(" + xid + ", " + flightNum + ") called");
		IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.queryFlightPrice(xid, flightNum);
		}
		else {
			Trace.warn("RM::queryFlightPrice(" + xid + ", " + flightNum + ")" + 
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws RemoteException
	{
		Trace.info("RM::queryCarsPrice(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(carsHost, portNum, carsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.queryCarsPrice(xid, location);
		}
		else {
			Trace.warn("RM::queryCarsPrice(" + xid + ", " + location + ")" +
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws RemoteException
	{
		Trace.info("RM::queryRoomsPrice(" + xid + ", " + location + ") called");
		IResourceManager m_resourceManager = connectServer(roomsHost, portNum, roomsServerName);
		if (m_resourceManager != null) {	
			return m_resourceManager.queryRoomsPrice(xid, location);
		}
		else {
			Trace.warn("RM::queryRoomsPrice(" + xid + ", " + location + ")" +
				   "  could not connect to the resource manager server.");
			return -1;
		}
	}

	public String queryCustomerInfo(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			// NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			return "";
		}
		else
		{
			Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
			System.out.println(customer.getBill());
			return customer.getBill();
		}
	}

	public int newCustomer(int xid) throws RemoteException
	{
        	Trace.info("RM::newCustomer(" + xid + ") called");
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt(String.valueOf(xid) +
			String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
			String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer customer = new Customer(cid);
		writeData(xid, customer.getKey(), customer);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
		return cid;
	}

	public boolean newCustomer(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			customer = new Customer(customerID);
			writeData(xid, customer.getKey(), customer);
			Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
			return true;
		}
		else
		{
			Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
			return false;
		}
	}
		
	public boolean deleteCustomer(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			return false;
		}
		else
		{            
			// Increase the reserved numbers of all reservable items which the customer reserved. 
 			RMHashMap reservations = customer.getReservations();
			// hashmap for storing keys and number of reservations
			HashMap<String, Integer> reservationsMap = new HashMap<String, Integer>(); //<key, count>
			//Vector<String> reservedKeyList;
			
			for (String reservedKey : reservations.keySet())
			{        
				ReservedItem reserveditem = customer.getReservedItem(reservedKey);
				reservationsMap.put(reserveditem.getKey(), reserveditem.getCount());	
			}
			
			// cancel item reservations in all resource managers. Return false if failed
			if (cancelItemSet(xid, reservationsMap) == false) return false;
			
			// Remove the customer from the storage
			removeData(xid, customer.getKey());
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
			return true;
		}
	}

	// Adds flight reservation to this customer
	public int reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
	{
		Trace.info("RM::reserveFlight(" + xid + ", " + customerID + ", " + flightNum + ") called");
		Customer customer = getCustomer(xid, customerID);
		// if customer does not exist, return -1
		if (customer == null) return -1;
		
		// return -1 if there is no connection to the resource manager
		IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);	
		if (m_resourceManager == null) return -1;
		
		// if a flight is successfully reserved return 0, otherwise -1
		int flightPrice = m_resourceManager.reserveFlight(xid, customerID, flightNum);
		if (flightPrice != -1) {
			customer.reserve(Flight.getKey(flightNum), String.valueOf(flightNum), flightPrice);
			writeData(xid, customer.getKey(), customer);
			return 0;
		}

		return -1;
	}

	// Adds car reservation to this customer	
	public int reserveCar(int xid, int customerID, String location) throws RemoteException
	{
		Trace.info("RM::reserveCar(" + xid + ", " + customerID + ", " + location + ") called");
		Customer customer = getCustomer(xid, customerID);
		// if customer does not exist, return false
		if (customer == null) return -1;	
		
		// return -1 if there is no connection to the resource manager
		IResourceManager m_resourceManager = connectServer(carsHost, portNum, carsServerName);
		if (m_resourceManager == null) return -1;	
		
		// if a car is successfully reserved return 0, otherwise -1
		int carPrice = m_resourceManager.reserveCar(xid, customerID, location);	
		
		if (carPrice != -1) {
			customer.reserve(Car.getKey(location), location, carPrice);
			writeData(xid, customer.getKey(), customer);
			return carPrice;
		}
		
		return -1;
	}

	// Adds room reservation to this customer
	public int reserveRoom(int xid, int customerID, String location) throws RemoteException
	{
		Trace.info("RM::reserveRoom(" + xid + ", " + customerID + ", " + location + ") called");
		Customer customer = getCustomer(xid, customerID);
		// if customer does not exist, return false
		if (customer == null) return -1;

		// return -1 if there is no connection to the resource manager
		IResourceManager m_resourceManager = connectServer(roomsHost, portNum, roomsServerName);
		if (m_resourceManager == null) return -1;
		
		// if a room is successfully reserved return 0, otherwise -1
		int roomPrice = m_resourceManager.reserveRoom(xid, customerID, location);
		if (roomPrice != -1) {
			customer.reserve(Room.getKey(location), location, roomPrice);
			writeData(xid, customer.getKey(), customer);
			return roomPrice;
		}

		return -1;
	}

	// Reserve bundle 
	public boolean bundle(
		int xid,
		int customerId,
		Vector<String> flightNumbers,
		String location,
		boolean car,
		boolean room
	) throws RemoteException {
		Trace.info("RM::bundle(" + xid + ", " + customerId + ", " +
					   flightNumbers.toString() + "," + location + ", " + car + ", " + room + ") called");
		if (flightNumbers.isEmpty()) return false;
			
		boolean availabe = checkItemsAvailability(xid, customerId, flightNumbers, location, car, room);
		if (availabe) return reserveItemsBunlde(xid, customerId, flightNumbers, location, car, room);
		return false;
	}

	public boolean checkFlightList(int xid, Vector<String> flightNumbers, String location) throws RemoteException {
		return false;
	}

	public Vector<Integer> reserveFlightList(
		int xid,
		int customerId,
		Vector<String> flightNumbers,
		String location
	) throws RemoteException {
		return null;
	}

	public boolean cancelItemReservations(int xid, HashMap<String, Integer> reservedKeysMap) throws RemoteException {
		return false;
	}


	public String getName() throws RemoteException
	{
		return m_name;
	}

	
	// check if items are available
	private boolean checkItemsAvailability(
		int xid, 
		int customerId,
		Vector<String> flightNumbers,
		String location,
		boolean car, 
		boolean room
	) throws RemoteException {
		Trace.info("RM::bundle[checkItemsAvailability](" + xid + ", " + customerId + ", " +
					   flightNumbers.toString() + "," + location + ", " + car + ", " + room + ") called");
		boolean flightsResult = true;
		boolean carResult = true;
		boolean roomResult = true;
		
		// threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		Future<Boolean> flightsFuture = null;
		Future<Boolean> carFuture = null;
		Future<Boolean> roomFuture = null;

		// if customer does not exist, return false
		Customer customer = getCustomer(xid, customerId);
		if (customer == null) return false;
		
		// send asynchronous requests to the resource managers
		
		if (!flightNumbers.isEmpty()) {
			Callable<Boolean> checkFlightsCallback = () -> {	
				Trace.info("RM::checkFlightList(" + xid + ", " + flightNumbers + ", " + location + ") called");
				IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
				if (m_resourceManager == null) return false;	
				return m_resourceManager.checkFlightList(xid, flightNumbers, location);
			};
			
			// create thread for reserving list flight
			flightsFuture = executor.submit(checkFlightsCallback);
		}
		
		if (car) {	
			// thread for reserving a car at a location
			Callable<Boolean> checkCarCallback = () -> {
				int price = queryCars(xid, location);
				return price >= 0;	
			};
			
			// thread for reserving a car at a location
			carFuture = executor.submit(checkCarCallback);
		}

		if (room) {
			// thread for reserving a  at a location
			Callable<Boolean> checkRoomCallback = () -> {
				int price = queryRooms(xid, location);
				return price >= 0;	
			};
			
			// thread for reserving a  at a location
			roomFuture = executor.submit(checkRoomCallback);
		}	

		// wait for previously submitted tasks to execute, and then terminate the executor
		executor.shutdown();

		// get the results
		if (!flightNumbers.isEmpty()) flightsResult = handleThreadResult(flightsFuture);
		if (car) carResult = handleThreadResult(carFuture);
		if (room) roomResult = handleThreadResult(roomFuture);	
		
		return flightsResult && carResult && roomResult;
	}
	
	// reserve items
	private boolean reserveItemsBunlde(
		int xid,
		int customerId,
		Vector<String> flightNumbers,
		String location,
		boolean car, 
		boolean room
	) throws RemoteException {
		Trace.info("RM::bundle[reserveItemsBunlde](" + xid + ", " + customerId + ", " +
					   flightNumbers.toString() + "," + location + ", " + car + ", " + room + ") called");
		
		boolean flightsResult = true;
		boolean carResult = true;
		boolean roomResult = true;

		// threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		Future<Boolean> flightsFuture = CompletableFuture.completedFuture(true);
		Future<Boolean> carFuture = CompletableFuture.completedFuture(true);
		Future<Boolean> roomFuture = CompletableFuture.completedFuture(true);

		// if customer does not exist, return false
		Customer customer = getCustomer(xid, customerId);
		if (customer == null) return false;

		// send asynchronous requests to the resource managers
	
		// check if the vector is empty	
		if (!flightNumbers.isEmpty()) {	
			Callable<Boolean> reserveFlightsCallback = () -> {
				Trace.info("RM::reserveFlightList(" + xid + ", " + customerId +
					   ", " + flightNumbers + ", " + location + ") called");
				
				IResourceManager m_resourceManager = connectServer(flightsHost, portNum, flightsServerName);
				if (m_resourceManager == null) return false;
				
				Vector<Integer> prices = m_resourceManager.reserveFlightList(xid, customerId, flightNumbers, location);
				if (prices.isEmpty()) return false;				

				for (int i = 0; i < prices.size(); i++) {
					int flightNum = Integer.parseInt(flightNumbers.get(i));
					customer.reserve(Flight.getKey(flightNum), String.valueOf(flightNum), prices.get(i));
					writeData(xid, customer.getKey(), customer);
				}
				
				return true;
			};

			// create thread for reserving list flight
			flightsFuture = executor.submit(reserveFlightsCallback);
		}
		
		if (car) {	
			Callable<Boolean> reserveCarCallback = () -> {
				Trace.info("RM::reserveCar(" + xid + ", " + customerId + ", " + location + ") called");
						
				int price = reserveCar(xid, customerId, location);
				if (price == -1) return false;
				
				customer.reserve(Car.getKey(location), location, price);
				writeData(xid, customer.getKey(), customer);
				return true;
			};
			
			// thread for reserving a car at a location
			carFuture = executor.submit(reserveCarCallback);
		}

		if (room) {	
			Callable<Boolean> reserveRoomCallback = () -> {
				Trace.info("RM::reserveRoom(" + xid + ", " + customerId + ", " + location + ") called");
					
				int price = reserveRoom(xid, customerId, location);
				if (price == -1) return false;
				customer.reserve(Room.getKey(location), location, price);
				writeData(xid, customer.getKey(), customer);
				return true;
			};
			
			// thread for reserving a room at a location
			roomFuture = executor.submit(reserveRoomCallback);
		}
		
		// wait for previously submitted tasks to execute, and then terminate the executor
		executor.shutdown();

		// get the results
		if (!flightNumbers.isEmpty()) flightsResult = handleThreadResult(flightsFuture);
		if (car) carResult = handleThreadResult(carFuture);
		if (room) roomResult = handleThreadResult(roomFuture);
	
		return flightsResult && carResult && roomResult;
	}

	private boolean cancelItemSet(int xid, HashMap<String, Integer> reservationsMap) {
		// threads executor
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
		
		// get the references to the remote objects
		IResourceManager flightsManager = connectServer(flightsHost, portNum, flightsServerName);
		if (flightsManager == null) return false;
		
		IResourceManager carsManager = connectServer(carsHost, portNum, carsServerName);
		if (carsManager == null) return false;

		IResourceManager roomsManager = connectServer(roomsHost, portNum, roomsServerName);
		if (roomsManager == null) return false;
		
		// 
		Callable<Boolean> cancelFlights = () -> {
			return flightsManager.cancelItemReservations(xid, reservationsMap);
		};
		
		Callable<Boolean> cancelCars = () -> {
			return carsManager.cancelItemReservations(xid, reservationsMap);
		};
		
		Callable<Boolean> cancelRooms = () -> {
			return roomsManager.cancelItemReservations(xid, reservationsMap);
		};
		
		// Submit a value-returning tasks for execution in separate threads
		Future<Boolean> flightsFuture = executor.submit(cancelFlights);
		Future<Boolean> carsFuture = executor.submit(cancelCars);
		Future<Boolean> roomsFuture = executor.submit(cancelRooms);
		
		// execute threads
		executor.shutdown();
	
		// get the results
		boolean flightsResult = handleThreadResult(flightsFuture);
		boolean carResult = handleThreadResult(carsFuture);
		boolean roomResult = handleThreadResult(roomsFuture);

		return flightsResult && carResult && roomResult;
	}

	private boolean handleThreadResult(Future<Boolean> itemsFuture) {
		boolean itemsResult = false;
		
		// submit lambda function to a new thread	
		try {
			// limit the thread response time by 3 seconds
			itemsResult = itemsFuture.get(WAIT_RESPONSE, TimeUnit.SECONDS);
		}
		catch(TimeoutException e) {
			Trace.warn("RM::Thread timeout exception");
			itemsFuture.cancel(true);
			return false;
		}
		catch(InterruptedException e) {
			Trace.warn("RM::Thread interrupted exception");
			itemsFuture.cancel(true);
			return false;
		}
		catch(ExecutionException e) {
			Trace.warn("RM::Thread execution exception");
			itemsFuture.cancel(true);
			return false;
		}
		catch(CancellationException e) {
			Trace.warn("RM::Thread cancellation exception");
			return false;
		}
		
		return itemsResult;
	}

	
	private IResourceManager connectServer(String server, int port, String name) {
		IResourceManager m_resourceManager = null;

		try {
			boolean first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(server, port);
					m_resourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + name);
					Trace.info("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					if (first) {
						Trace.warn("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			Trace.error((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			//e.printStackTrace();	
		}

		return m_resourceManager;
	}
}
