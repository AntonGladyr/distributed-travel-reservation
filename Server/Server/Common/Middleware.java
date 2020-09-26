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

public class Middleware implements IResourceManager
{	
	private IResourceManager m_resourceManager;

	// group number as unique identifier
	private final String s_rmiPrefix = "group_03_";
	
	private String flightsHost;
	private String carsHost;
	private String roomsHost;
	private final String flightsServerName = "Flights";
	private final String carsServerName = "Cars";
	private final String roomsServerName = "Rooms";
	private final int portNum = 33303;
	
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
	
	private void connectServer(String server, int port, String name) {
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
			e.printStackTrace();
			System.exit(1);
		}	
	}

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
	protected void writeData(int xid, String key, RMItem value)
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
	protected Customer getCustomer(int xid, int customerID, String key, String location)
	{
		Trace.info("RM::getCustomer(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::getCustomer(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");	
		}

		return customer;
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{	
		connectServer(flightsHost, portNum, flightsServerName); 
		Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
		return m_resourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) throws RemoteException
	{
		connectServer(carsHost, portNum, carsServerName);
		Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		return m_resourceManager.addCars(xid, location, count, price);
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
	{
		connectServer(roomsHost, portNum, roomsServerName);
		Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		return m_resourceManager.addRooms(xid, location, count, price);
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) throws RemoteException
	{
		connectServer(flightsHost, portNum, flightsServerName);
		Trace.info("RM::deleteFlight(" + xid + ", " + flightNum + ") called");
		return m_resourceManager.deleteFlight(xid, flightNum);
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws RemoteException
	{
		connectServer(carsHost, portNum, carsServerName);
		Trace.info("RM::deleteCars(" + xid + ", " + location + ") called");
		return m_resourceManager.deleteCars(xid, location);
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws RemoteException
	{
		connectServer(roomsHost, portNum, roomsServerName);
		Trace.info("RM::deleteRooms(" + xid + ", " + location + ") called");
		return m_resourceManager.deleteRooms(xid, location);
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) throws RemoteException
	{
		connectServer(flightsHost, portNum, flightsServerName);
		Trace.info("RM::queryFlight(" + xid + ", " + flightNum + ") called");
		return m_resourceManager.queryFlight(xid, flightNum);
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws RemoteException
	{
		connectServer(carsHost, portNum, carsServerName);
		Trace.info("RM::queryCars(" + xid + ", " + location + ") called");
		return m_resourceManager.queryCars(xid, location);
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws RemoteException
	{
		connectServer(roomsHost, portNum, roomsServerName);
		Trace.info("RM::queryRooms(" + xid + ", " + location + ") called");
		return m_resourceManager.queryRooms(xid, location);
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) throws RemoteException
	{
		connectServer(flightsHost, portNum, flightsServerName);
		Trace.info("RM::queryFlightPrice(" + xid + ", " + flightNum + ") called");
		return m_resourceManager.queryFlightPrice(xid, flightNum);
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws RemoteException
	{
		connectServer(carsHost, portNum, carsServerName);
		Trace.info("RM::queryCarsPrice(" + xid + ", " + location + ") called");
		return m_resourceManager.queryCarsPrice(xid, location);
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws RemoteException
	{
		connectServer(roomsHost, portNum, roomsServerName);
		Trace.info("RM::queryRoomsPrice(" + xid + ", " + location + ") called");
		return m_resourceManager.queryRoomsPrice(xid, location);
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
			for (String reservedKey : reservations.keySet())
			{        
				ReservedItem reserveditem = customer.getReservedItem(reservedKey);
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
				ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
				item.setReserved(item.getReserved() - reserveditem.getCount());
				item.setCount(item.getCount() + reserveditem.getCount());
				writeData(xid, item.getKey(), item);
			}

			// Remove the customer from the storage
			removeData(xid, customer.getKey());
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
			return true;
		}
	}

	// Adds flight reservation to this customer
	public int reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
	{
		Customer customer = getCustomer(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
		// if customer does not exist, return false	
		if (customer == null) return -1;
		
		connectServer(flightsHost, portNum, flightsServerName);
		Trace.info("RM::reserveFlight(" + xid + ", " + customerID + ", " + flightNum + ") called");
		
		// if a flight is successfully reserved
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
		Customer customer = getCustomer(xid, customerID, Car.getKey(location), location);
		// if customer does not exist, return false
		if (customer == null) return -1;
		
		connectServer(carsHost, portNum, carsServerName);
		Trace.info("RM::reserveCar(" + xid + ", " + customerID + ", " + location + ") called");
		
		// if a car is successfully reserved
		int carPrice = m_resourceManager.reserveCar(xid, customerID, location);
		if (carPrice != -1) {
			customer.reserve(Car.getKey(location), location, carPrice);
			writeData(xid, customer.getKey(), customer);
			return 0;
		}
		
		return -1;
	}

	// Adds room reservation to this customer
	public int reserveRoom(int xid, int customerID, String location) throws RemoteException
	{
		Customer customer = getCustomer(xid, customerID, Car.getKey(location), location);
		// if customer does not exist, return false
		if (customer == null) return -1;
		
		connectServer(roomsHost, portNum, roomsServerName);
		Trace.info("RM::reserveRoom(" + xid + ", " + customerID + ", " + location + ") called");
		
		// if a room is successfully reserved
		int roomPrice = m_resourceManager.reserveRoom(xid, customerID, location);
		if (roomPrice != -1) {
			customer.reserve(Room.getKey(location), location, roomPrice);
			writeData(xid, customer.getKey(), customer);
			return 0;
		}

		return -1;
	}

	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		return false;
	}

	public String getName() throws RemoteException
	{
		return m_name;
	}
}
 
