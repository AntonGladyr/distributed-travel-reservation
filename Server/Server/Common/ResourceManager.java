// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;
import Server.RMI.ShutdownThread;

import java.util.*;
import java.rmi.RemoteException;

import Transactions.TransactionNode;

public class ResourceManager implements IResourceManager, DataStore {
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();

	private static final String notImplementedHere = "Operation not implemented in ResourceManagers";

	public ResourceManager(String p_name) {
		m_name = p_name;

		// Register self with the transaction node
		TransactionNode.dataStore = this;
	}

	// Reads a data item
	protected RMItem readData(int xid, String key) {
		synchronized (m_data) {

			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem) item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	public void writeData(int xid, String key, RMItem value) {
		synchronized (m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key) {
		synchronized (m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key) {
		Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");

		ReservableItem curObj = (ReservableItem) readData(xid, key);

		TransactionNode.beforeWriting(xid, key, curObj);

		// Check if there is such an item in the storage
		if (curObj == null) {
			Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
			return false;
		} else {
			if (curObj.getReserved() == 0) {
				removeData(xid, curObj.getKey());

				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
				return true;
			} else {
				Trace.info("RM::deleteItem(" + xid + ", " + key
						+ ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(int xid, String key) {
		Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem) readData(xid, key);
		int value = 0;
		if (curObj != null) {
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
		return value;
	}

	// Query the price of an item
	protected int queryPrice(int xid, String key) {
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem) readData(xid, key);
		int value = -1;
		if (curObj != null) {
			value = curObj.getPrice();
		}

		Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
		return value;
	}

	// Reserve an item
	protected int reserveItem(int xid, int customerID, String key, String location) {
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called");

		// Check if the item is available
		ReservableItem item = (ReservableItem) readData(xid, key);

		TransactionNode.beforeWriting(xid, key, item);

		if (item == null) {
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location
					+ ") failed--item doesn't exist");
			return -1;

		} else if (item.getCount() == 0) {
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location
					+ ") failed--No more items");
			return -1;
		} else {
			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(xid, item.getKey(), item);
			
			Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
			return queryPrice(xid, key);
		}
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its
	// current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");

		String key = Flight.getKey(flightNum);
		Flight curObj = (Flight) readData(xid, key);

		TransactionNode.beforeWriting(xid, key, curObj);

		if (curObj == null) {
			// Doesn't exist yet, add it
			Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats
					+ ", price=$" + flightPrice);
		} else {
			// Add seats to existing flight and update the price if greater than zero
			curObj.setCount(curObj.getCount() + flightSeats);
			if (flightPrice > 0) {
				curObj.setPrice(flightPrice);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats="
					+ curObj.getCount() + ", price=$" + flightPrice);
		}
		return true;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current
	// price
	public boolean addCars(int xid, String location, int count, int price) throws RemoteException {
		Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");

		String key = Car.getKey(location);
		Car curObj = (Car) readData(xid, key);

		TransactionNode.beforeWriting(xid, key, curObj);

		if (curObj == null) {
			// Car location doesn't exist yet, add it
			Car newObj = new Car(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$"
					+ price);
		} else {
			// Add count to existing car location and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0) {
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count="
					+ curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its
	// current price
	public boolean addRooms(int xid, String location, int count, int price) throws RemoteException {
		Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");

		String key = Room.getKey(location);
		Room curObj = (Room) readData(xid, key);

		TransactionNode.beforeWriting(xid, key, curObj);

		if (curObj == null) {
			// Room location doesn't exist yet, add it
			Room newObj = new Room(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addRooms(" + xid + ") created new room location " + location + ", count=" + count
					+ ", price=$" + price);
		} else {
			// Add count to existing object and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0) {
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addRooms(" + xid + ") modified existing location " + location + ", count="
					+ curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		// beforeWriting is in deleteItem
		return deleteItem(xid, Flight.getKey(flightNum));
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws RemoteException {
		// beforeWriting is in deleteItem
		return deleteItem(xid, Car.getKey(location));
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws RemoteException {
		// beforeWriting is in deleteItem
		return deleteItem(xid, Room.getKey(location));
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		return queryNum(xid, Flight.getKey(flightNum));
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws RemoteException {
		return queryNum(xid, Car.getKey(location));
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws RemoteException {
		return queryNum(xid, Room.getKey(location));
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum)
			throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		return queryPrice(xid, Flight.getKey(flightNum));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws RemoteException {
		return queryPrice(xid, Car.getKey(location));
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws RemoteException {
		return queryPrice(xid, Room.getKey(location));
	}

	public String queryCustomerInfo(int xid, int customerID) throws RemoteException {
		throw new RemoteException(notImplementedHere);
	}

	public int newCustomer(int xid) throws RemoteException {
		throw new RemoteException(notImplementedHere);
	}

	public boolean newCustomer(int xid, int customerID) throws RemoteException {
		throw new RemoteException(notImplementedHere);
	}

	public boolean deleteCustomer(int xid, int customerID) throws RemoteException {
		throw new RemoteException(notImplementedHere);
	}

	// Adds flight reservation to this customer
	public int reserveFlight(int xid, int customerID, int flightNum) throws RemoteException {
		// beforeWriting is in reserveItem
		return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}

	// Adds car reservation to this customer
	public int reserveCar(int xid, int customerID, String location) throws RemoteException {
		// beforeWriting is in reserveItem
		return reserveItem(xid, customerID, Car.getKey(location), location);
	}

	// Adds room reservation to this customer
	public int reserveRoom(int xid, int customerID, String location) throws RemoteException {
		// beforeWriting is in reserveItem
		return reserveItem(xid, customerID, Room.getKey(location), location);
	}

	// Reserve bundle
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car,
			boolean room) throws RemoteException {
		return false;
	}

	// Check if the flight list is available
	public boolean checkFlightList(int xid, Vector<String> flightNumbers, String location) throws RemoteException {
		Trace.info("RM::checkFlightList(" + xid + ", " + "[flightNumbers]" + ", " + location + ") called");

		boolean isAvailable = true;
		// hashmap to check if we are trying to reserve more seats than available in the
		// same flight
		HashMap<Integer, Integer> availableSeatsMap = new HashMap<Integer, Integer>(); // <flight number, number of
																						// seats>

		// iterate through each flight number
		for (String flightNum : flightNumbers) {
			int availableSeats = queryNum(xid, Flight.getKey(Integer.parseInt(flightNum)));
			// return false if there is no available seats
			if (availableSeats == 0) {
				isAvailable = false;
				break;
			}

			// if the key is not in the hashmap, add <flight number, number of seats>
			if (availableSeatsMap.get(Integer.parseInt(flightNum)) == null) {
				availableSeatsMap.put(Integer.parseInt(flightNum), availableSeats - 1);
			} else { // otherwise decrease the number of seats
				int seats = availableSeatsMap.get(Integer.parseInt(flightNum));
				availableSeatsMap.put(Integer.parseInt(flightNum), seats - 1);
			}
		}

		// iterate through the whole hashmap. If there is a negative value, a flight
		// does not have enough seats
		for (int value : availableSeatsMap.values()) {
			if (value < 0) {
				isAvailable = false;
				break;
			}
		}

		Trace.info("RM::checkFlightList RESULT: " + isAvailable);
		return isAvailable;
	}

	public Vector<Integer> reserveFlightList(int xid, int customerId, Vector<String> flightNumbers, String location)
			throws RemoteException {
		Trace.info("RM::reserveFlightList(" + xid + ", " + customerId + ", " + "[flightNumbers]" + ", " + location
				+ ") called");

		Vector<Integer> flightPrices = new Vector<Integer>();

		// iterate through each flight number and make a reservation
		for (String flightNum : flightNumbers) {
			int price = reserveFlight(xid, customerId, Integer.parseInt(flightNum));
			if (price == -1) {
				flightPrices = null;
				break;
			}

			flightPrices.add(price);
		}

		// return a list of prices for corresponding flights
		return flightPrices;
	}

	public boolean cancelItemReservations(int xid, HashMap<String, Integer> reservedKeysMap) throws RemoteException {
		Trace.info("RM::cancelItemReservations(" + xid + ", HashMap<String, Integer>) called");

		for (Map.Entry<String, Integer> entry : reservedKeysMap.entrySet()) {

			ReservableItem item = (ReservableItem) readData(xid, entry.getKey());

			TransactionNode.beforeWriting(xid, entry.getKey(), item);

			if (item == null)
				continue;
			item.setReserved(item.getReserved() - entry.getValue());
			item.setCount(item.getCount() + entry.getValue());
			writeData(xid, item.getKey(), item);
		}

		return true;
	}

	public String getName() throws RemoteException {
		return m_name;
	}

	@Override
	public int start() throws RemoteException {
		throw new RemoteException(notImplementedHere);
	}

	@Override
	public boolean commit(int xid) throws RemoteException {
		TransactionNode.commit(xid);
		return true;
	}

	@Override
	public boolean abort(int xid) throws RemoteException {
		TransactionNode.abort(xid);
		return true;
	}

	@Override
	public void shutdown(String name) throws RemoteException{
		
		Trace.info("Shutting down " + this.m_name);
		
		ShutdownThread thread = new ShutdownThread();
		thread.start();
		
//		try {
//			Trace.info("Naming.unbind param is: group_3_" + name);
//			Naming.unbind("group_3_" + m_name);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NotBoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Trace.info("here");
//		UnicastRemoteObject.unexportObject(this, true);
//		Trace.info("here 2");
		
		
	}
}
