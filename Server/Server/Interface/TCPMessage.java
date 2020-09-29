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
	public int price; // price, flightPrice
	public String location;
	public int amount; // flightSeats, numCars, numRooms
	public int cid; // cid, customerID
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
		message.amount = flightSeats;
		message.price = flightPrice;
		return message;
	}

	// ========== End of factory methods ==========
}
