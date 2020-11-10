package Transactions;

import java.rmi.RemoteException;
import java.util.*;
import Server.Common.Trace;
import Server.Interface.IResourceManager;
import Server.Interface.InvalidTransactionException;

public class Transaction {

	int xid; // transaction id
	long timeToLive = 20000; //20 seconds
	long timeLastActive; 

	private boolean accessedCustomers = false;
	private List<IResourceManager> rmList = new ArrayList<IResourceManager>(); // keeps track of Resource Managers related
																				// to this transaction

	public Transaction(int xid) {
		this.xid = xid;
		this.timeLastActive = System.currentTimeMillis(); 
	}
	
	public void setAccessedCustomers(boolean value) {
		this.accessedCustomers = value;
	}
	public int getXID() {
		return this.xid;
	}

	public void addRM(IResourceManager resourceManager) {
		if (!rmList.contains(resourceManager)) {
			rmList.add(resourceManager); // add rm to rmList
		} else {
			// rm already part of list, dont add dup
			Trace.info("Transaction::addRM() rm already in rmList for xid: " + xid); // for testing purposes
		}
	}

	public void resetTimeToLive() {
		timeLastActive = System.currentTimeMillis();
	}

	public long getRemainingTimeToLive() {
		return timeToLive - (System.currentTimeMillis() - timeLastActive);
	}

	// Forwards commit to all relevant nodes
	public void commit() throws RemoteException, InvalidTransactionException {
		if (accessedCustomers) TransactionNode.commit(xid); // Customers are a separate case (local)
		
		for (int i = 0; i < rmList.size(); i++) {
			rmList.get(i).commit(xid);
		}
	}
	
	// Forwards abort to all relevant nodes
	public void abort() throws RemoteException, InvalidTransactionException {
		if (accessedCustomers) TransactionNode.abort(xid); // Customers are a separate case (local)
		
		for (int i = 0; i < rmList.size(); i++) {
			rmList.get(i).abort(xid);
		}
	}
}
