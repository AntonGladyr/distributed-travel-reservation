package Server.Interface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

public class TCPMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	// Indicates the type of request this message pertains to
	public MessageType type;
	
	// Message request parameters (different parameters used depending on the message type)
	public int id;
	public int flightNum;
	public int flightSeats;
	public int flightPrice;
	public String location;
	public int numCars;
	//public int carPrice;
	public int price;
	public int numRooms;
	//public int roomPrice;
	public int cid;
	public int customerID;
	public int flightNumber;
	public Vector<String> flightNumbers;
	public boolean car;
	public boolean room;
	public HashMap<String,Integer> reservedKeysMap;
	
	// Message response result (different result type depending on the message type)
	public boolean booleanResult;
	public int intResult;
	public String stringResult;
	public Vector<Integer> vectorIntResult;
	
	// Constructor
	public TCPMessage(MessageType type) {
		this.type = type;
	}
	
	// ========== Factory methods to create messages ==========
	
	public static TCPMessage newAddFlight(int id, int flightNum, int flightSeats, int flightPrice) {
		TCPMessage message = new TCPMessage(MessageType.ADD_FLIGHT);
		message.id = id;
		message.flightNum = flightNum;
		message.flightSeats = flightSeats;
		message.flightPrice = flightPrice;
		return message;
	}
	public static TCPMessage newAddCars(int id, String location, int numCars, int price) {
		TCPMessage message = new TCPMessage(MessageType.ADD_CARS); 
		message.id = id;
		message.location = location;
		message.numCars = numCars;
		message.price = price;
		return message;
	}
	public static TCPMessage newAddRooms(int id, String location, int numRooms, int price) {
		TCPMessage message = new TCPMessage(MessageType.ADD_ROOMS); 
		message.id = id;
		message.location = location;
		message.numRooms = numRooms;
		message.price = price;
		return message;
	}	
	public static TCPMessage newNewCustomer(int id) {
		TCPMessage message = new TCPMessage(MessageType.NEW_CUSTOMER); 
		message.id = id;
		return message;
	}
	public static TCPMessage newNewCustomerID(int id, int cid) {
		TCPMessage message = new TCPMessage(MessageType.NEW_CUSTOMER_ID); 
		message.id = id;
		message.cid = cid;
		return message;
	}
	public static TCPMessage newDeleteFlight(int id, int flightNum) {
		TCPMessage message = new TCPMessage(MessageType.DELETE_FLIGHT); 
		message.id = id;
		message.flightNum = flightNum;
		return message;
	}
	public static TCPMessage newDeleteCars(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.DELETE_CARS); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newDeleteRooms(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.DELETE_ROOMS); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newDeleteCustomer(int id, int customerID) {
		TCPMessage message = new TCPMessage(MessageType.DELETE_CUSTOMER); 
		message.id = id;
		message.customerID = customerID;
		return message;
	}
	public static TCPMessage newQueryFlight(int id, int flightNumber) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_FLIGHT); 
		message.id = id;
		message.flightNum = flightNumber;
		return message;
	}
	public static TCPMessage newQueryCars(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_CARS); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newQueryRooms(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_ROOMS); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newQueryCustomerInfo(int id, int customerID) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_CUSTOMER_INFO); 
		message.id = id;
		message.customerID = customerID;
		return message;
	}
	public static TCPMessage newQueryFlightPrice(int id, int flightNumber) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_FLIGHT_PRICE); 
		message.id = id;
		message.flightNumber = flightNumber;
		return message;
	}
	public static TCPMessage newQueryCarsPrice(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_CARS_PRICE); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newQueryRoomsPrice(int id, String location) {
		TCPMessage message = new TCPMessage(MessageType.QUERY_ROOMS_PRICE); 
		message.id = id;
		message.location = location;
		return message;
	}
	public static TCPMessage newReserveFlight(int id, int customerID, int flightNumber) {
		TCPMessage message = new TCPMessage(MessageType.RESERVE_FLIGHT); 
		message.id = id;
		message.customerID = customerID;
		message.flightNum = flightNumber;
		return message;
	}
	public static TCPMessage newReserveCar(int id, int customerID, String location) {
		TCPMessage message = new TCPMessage(MessageType.RESERVE_CAR); 
		message.id = id;
		message.customerID = customerID;
		message.location = location;
		return message;
	}
	public static TCPMessage newReserveRoom(int id, int customerID, String location) {
		TCPMessage message = new TCPMessage(MessageType.RESERVE_ROOM); 
		message.id = id;
		message.customerID = customerID;
		message.location = location;
		return message;
	}
	public static TCPMessage newBundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car,
			boolean room) {
		TCPMessage message = new TCPMessage(MessageType.BUNDLE); 
		message.id = id;
		message.customerID = customerID;
		message.flightNumbers = flightNumbers;
		message.location = location;
		message.car = car;
		message.room = room;
		
		return message;
	}
	public static TCPMessage newCheckFlightList(int id, Vector<String> flightNumbers, String location) {
		TCPMessage message = new TCPMessage(MessageType.CHECK_FLIGHT_LIST); 
		message.id = id;
		message.flightNumbers = flightNumbers;
		message.location = location;
		
		return message;
	}
	public static TCPMessage newReserveFlightList(int id, int customerID, Vector<String> flightNumbers, String location) {
		TCPMessage message = new TCPMessage(MessageType.RESERVE_FLIGHT_LIST); 
		message.id = id;
		message.customerID = customerID;
		message.flightNumbers = flightNumbers;
		message.location = location;
		
		return message;
	}
	public static TCPMessage newCancelItemReservations(int id, HashMap<String,Integer> reservedKeysMap) {
		TCPMessage message = new TCPMessage(MessageType.CANCEL_ITEM_RESERVATIONS); 
		message.id = id;
		message.reservedKeysMap = reservedKeysMap;
		
		return message;
	}
	
//	public static TCPMessage newGetName() {
//		TCPMessage message = new TCPMessage(MessageType.GET_NAME); 
//		message.id = id;
//		
//		
//		return message;
//	}

	
	// ========== End of factory methods ==========
}
