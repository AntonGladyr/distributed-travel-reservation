package Transactions;

import java.rmi.RemoteException;
import java.util.HashMap;

import Server.Common.Trace;
import Server.Interface.IResourceManager;
import Server.Interface.InvalidTransactionException;
import Server.Interface.TransactionAbortedException;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

public class TransactionManager {

	private static Integer transactionCounter = 0;

	private static HashMap<Integer, Transaction> activeTransactions = new HashMap<Integer, Transaction>(); // keeps track of active transactions

	private static LockManager lockManager = new LockManager();

	// References to the resource managers
	private static IResourceManager customersManager;
	private static IResourceManager flightsManager;
	private static IResourceManager carsManager;
	private static IResourceManager roomsManager;
	
	// Saves references to all the resource managers
	public static void registerResourceManagers(IResourceManager customersManager, IResourceManager flightsManager, IResourceManager carsManager,
			IResourceManager roomsManager) {
		TransactionManager.customersManager = customersManager;
		TransactionManager.flightsManager = flightsManager;
		TransactionManager.carsManager = carsManager;
		TransactionManager.roomsManager = roomsManager;
	}

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

	public static void addRMtoT(int xid, IResourceManager resourceManager) {
		// check if transaction is active
		if (activeTransactions.containsKey(xid)) {
			activeTransactions.get(xid).addRM(resourceManager); // add RM to transaction
		} else {
			Trace.info("TransactionManager::addRMtoT() trying to add rm to non-active transaction");
		}
	}
	
	public static void resetTimeToLive(int xid) {
		// check if transaction is active
				if (activeTransactions.containsKey(xid)) {
					activeTransactions.get(xid).resetTimeToLive(); 
				} else {
					Trace.info("TransactionManager::resetTimeToLive() trying to reset time to live of non-active transaction");
				}
	}

	// ----------------------------------------------read/write lock methods------------------------------------------------------------------

	// Requests a READ lock on a customer
	public static void readLockCustomer(int xid, int customerID) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "customer-" + customerID, TransactionLockObject.LockType.LOCK_READ);
			addRMtoT(xid, customersManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a customer
	public static void writeLockCustomer(int xid, int customerID) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "customer-" + customerID, TransactionLockObject.LockType.LOCK_WRITE);
			addRMtoT(xid, customersManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a READ lock on a flight
	public static void readLockFlight(int xid, int flightNumber) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "flight-" + flightNumber, TransactionLockObject.LockType.LOCK_READ);
			addRMtoT(xid, flightsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a flight
	public static void writeLockFlight(int xid, int flightNumber) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "flight-" + flightNumber, TransactionLockObject.LockType.LOCK_WRITE);
			addRMtoT(xid, flightsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a READ lock on a car
	public static void readLockCar(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_READ);
			addRMtoT(xid, carsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a car
	public static void writeLockCar(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_WRITE);
			addRMtoT(xid, carsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}
	
	// Requests a READ lock on a room
	public static void readLockRoom(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_READ);
			addRMtoT(xid, roomsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}

	// Requests a WRITE lock on a car
	public static void writeLockRoom(int xid, String location) throws TransactionAbortedException {
		try {
			lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_WRITE);
			addRMtoT(xid, roomsManager);
		} catch (DeadlockException e) {
			handleDeadlock(xid);
		}
	}
	
	// ----------------------------------------------end of read/write lock methods------------------------------------------------------------------
	
	// Aborts a transaction
	public static void abort(int xid) {
		// TODO Auto-generated method stub
	}

	public static void commit(int xid) throws RemoteException {
		//unlock relevent locks
		lockManager.UnlockAll(xid);
		
		//remove from active transactions list
		activeTransactions.remove(xid);
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
