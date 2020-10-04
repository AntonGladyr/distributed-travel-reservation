package Server.TCP;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import Server.Common.ResourceManager;
import Server.Interface.MessageType;
import Server.Interface.TCPMessage;

import Server.Common.Middleware;

public class TCPMiddleware extends TCPMiddlewareVersion implements Runnable{


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public TCPMiddleware(String p_name, String flightsHost, String carsHost, String roomsHost) {
		super(p_name, flightsHost, carsHost, roomsHost);
	}
	
	

}
