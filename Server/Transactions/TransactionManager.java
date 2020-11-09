package Transactions;

import java.util.HashMap;

import Server.Common.ResourceManager;
import Server.Common.Trace;
import Server.Interface.InvalidTransactionException;
import Server.Interface.TransactionAbortedException;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

public class TransactionManager {

	private static Integer transactionCounter = 0;

	private static HashMap<Integer, Transaction> activeTransactions = new HashMap<Integer, Transaction>(); // keeps track of active transactions

	private static LockManager lockManager = new LockManager();

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
		// check if transaction is active
		if (activeTransactions.containsKey(xid)) {
			activeTransactions.get(xid).addRM(resourceManager); // add RM to transaction
		} else {
			Trace.info("TransactionManager::addRMtoT() trying to add rm to non-active transaction");
		}
	}
	
	public void resetTimeToLive(int xid) {
		// check if transaction is active
				if (activeTransactions.containsKey(xid)) {
					activeTransactions.get(xid).resetTimeToLive(); 
				} else {
					Trace.info("TransactionManager::aresetTimeToLive() trying to reset time to live of non-active transaction");
				}
	}

	// ----------------------------------------------read/write lock methods------------------------------------------------------------------

	// Requests a READ lock on a customer
	public static void readLockCustomer(int xid, int customerID) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "customer-" + customerID, TransactionLockObject.LockType.LOCK_READ);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a customer
	public static void writeLockCustomer(int xid, int customerID) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "customer-" + customerID, TransactionLockObject.LockType.LOCK_WRITE);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a READ lock on a flight
	public static void readLockFlight(int xid, int flightNumber) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "flight-" + flightNumber, TransactionLockObject.LockType.LOCK_READ);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a flight
	public static void writeLockFlight(int xid, int flightNumber) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "flight-" + flightNumber, TransactionLockObject.LockType.LOCK_WRITE);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a READ lock on a car
	public static void readLockCar(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_READ);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a car
	public static void writeLockCar(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_WRITE);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}
	
	// Requests a READ lock on a room
	public static void readLockRoom(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_READ);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a car
	public static void writeLockRoom(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_WRITE);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}
	
	// ----------------------------------------------end of read/write lock methods------------------------------------------------------------------
	
	// Aborts a transaction
	public static void abort(int xid) {
		// TODO Auto-generated method stub
	}

	// Checks whether the specified transaction exists and is still active
	// TODO ensure that this check fails if the transaction is finished
	public static void validateXID(int xid) throws InvalidTransactionException {
		if (!activeTransactions.containsKey(xid))
			throw new InvalidTransactionException();
	}

	// Take steps to handle a deadlock when it occurs
	// TODO use timeouts instead of aborting immediately
	private static void handleDeadlock(int xid) throws TransactionAbortedException {
		TransactionManager.abort(xid);
		throw new TransactionAbortedException();
	}
}
