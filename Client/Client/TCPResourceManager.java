package Client;

import java.rmi.RemoteException;
import java.util.Vector;

import Server.Interface.IResourceManager;

/*
 * Uses TCP sockets to implement the same remote commands as those offered using RMI.
 * 
 * Sockets usage based on the following Java tutorial:
 * https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/SocketProgramming/SocketProgram.html#overview
 */
public class TCPResourceManager implements IResourceManager {
	
	private String server;
	private int port;
	
	public TCPResourceManager(String server, int port) {
		this.server = server;
		this.port = port;
		System.out.println("Created TCPResourceManager for [" + this.server + ":" + this.port + "]");
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet.");
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
		// TODO Auto-generated method stub
		throw new RemoteException("Not implemented yet.");
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
