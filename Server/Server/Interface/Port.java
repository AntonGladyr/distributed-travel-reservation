package Server.Interface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Used to centralize the port number used in the application
public class Port {
	
	private static int port = -1; // Initialized to -1, but gets read from a file when first queried
	private static String filename = "../PORT"; // Name (path) of the file where the port number is stored
	
	public static int getPort() {
		
		if (port == -1) {
			readPortFromFile();
			System.out.println("Using port " + port);
		}
		return port;
	}

	private static void readPortFromFile() {

		Scanner reader = null;
		
		try {
			File myObj = new File(filename);
			reader = new Scanner(myObj);
			String portString = "";

			// Read the contents of the file
			while (reader.hasNextLine()) {
				String data = reader.nextLine();
				portString += data;
			}
			
			// Convert the file contents to a port number
			port = Integer.parseInt(portString);
			
			// Validate port
			if (port < 0) throw new NumberFormatException();
			
		}
		catch (FileNotFoundException e) {
			System.out.println("An error occurred while reading the port number from file " + filename);
			e.printStackTrace();
			System.exit(1);
		}
		catch (NumberFormatException e) {
			System.out.println("The contents of file " + filename + " could not be converted to a port number");
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			if (reader != null) reader.close();
		}
	}
}
