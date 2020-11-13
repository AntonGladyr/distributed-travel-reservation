package Server.RMI;

public class ShutdownThread extends Thread {
	
	public void run() {
		
		try {
			Thread.sleep(1000);
			System.exit(0);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
}
