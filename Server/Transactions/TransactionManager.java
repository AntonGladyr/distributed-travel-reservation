package Transactions;

import java.util.HashMap;

import Server.Common.ResourceManager;
import Server.Common.Trace;

public class TransactionManager {
	
	private static Integer transactionCounter = 0;
	
	private static HashMap<Integer, Transaction> activeTransactions = new HashMap<Integer, Transaction>(); //keeps track of active transactions
	
	// Starts a new transaction and returns its xid
	public static int start() {
		int newXID;
		
		// Increment the transaction counter safely (for concurrency)
		synchronized (transactionCounter) {
			transactionCounter += 1;
			newXID = transactionCounter;
		}
		
		// Create a new transaction object
		activeTransactions.put(newXID, new Transaction(newXID));
		
		return newXID;
	}
	
	public void addRMtoT(int xid, ResourceManager resourceManager) {
		//check if transaction is active
		if (activeTransactions.containsKey(xid)) {
			activeTransactions.get(xid).addRM(resourceManager); //add RM to transaction
		}
		else {
			Trace.info("TransactionManager::addRMtoT() trying to add rm to non-active transaction");
		}
	}
}
