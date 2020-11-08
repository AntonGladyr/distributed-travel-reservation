package Transactions;

import java.util.HashMap;

import Server.Common.ResourceManager;
import Server.Common.Trace;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

public class TransactionManager {
	
	private static Integer transactionCounter = 0;
	
	private static HashMap<Integer, Transaction> activeTransactions = new HashMap<Integer, Transaction>(); //keeps track of active transactions
	
	private static LockManager customerLockManager = new LockManager();
	
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

	// Requests a READ lock on a customer
	public static void readLockCustomer(int xid, int customerID) throws TransactionAbortedException {
		try {
			customerLockManager.Lock(xid, Integer.toString(customerID), TransactionLockObject.LockType.LOCK_READ);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Aborts a transaction
	public static void abort(int xid) {
		// TODO Auto-generated method stub
	}

	// Checks whether the specified transaction exists and is still active
	// TODO ensure that this check fails if the transaction is finished
	public static void validateXID(int xid) throws InvalidTransactionException {
		if (!activeTransactions.containsKey(xid)) throw new InvalidTransactionException();
	}
	
	// Take steps to handle a deadlock when it occurs
	// TODO use timeouts instead of aborting immediately
	private static void handleDeadlock(int xid) throws TransactionAbortedException {
		TransactionManager.abort(xid);
		throw new TransactionAbortedException();
	}
}
