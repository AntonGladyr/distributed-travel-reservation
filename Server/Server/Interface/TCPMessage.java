package Server.Interface;

import java.io.Serializable;
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
	public int carPrice;
	public int price;
	public int numRooms;
	public int roomPrice;
	public int cid;
	public int customerID;
	public int flightNumber;
	public Vector<String> flightNumbers;
	public boolean car;
	public boolean room;
	
	// Message response result (different result type depending on the message type)
	public boolean booleanResult;
	public int intResult;
	public String stringResult;
	
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
	public static TCPMessage newAddCars(int id, String location, int numCars, int carPrice) {
		TCPMessage message = new TCPMessage(MessageType.ADD_CARS); 
		message.id = id;
		message.location = location;
		message.numCars = numCars;
		message.carPrice = carPrice;
		return message;
	}
		
		
	
	// ========== End of factory methods ==========
}
