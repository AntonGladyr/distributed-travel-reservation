package Transactions;

import java.rmi.RemoteException;

import Server.Common.Trace;
import Server.Interface.InvalidTransactionException;

public class timeThread extends Thread{
	
	public void run() {
		Trace.info("timeThread has begun");
		while (!this.isInterrupted()) {
			
			if (!TransactionManager.getActiveTransactions().isEmpty()) {
				//check time elapsed for each active transaction
				for (Transaction t : TransactionManager.getActiveTransactions().values()) {
					if (t.getRemainingTimeToLive() < 0) {
						Trace.info("timeThread:: transaction " + t.getXID() + " has run out of time to live. Commencing abort");
						try {
							TransactionManager.abort(t.getXID());
						} catch (RemoteException | InvalidTransactionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}
		}
	}
	
}
