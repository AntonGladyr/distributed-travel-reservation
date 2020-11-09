package Transactions;

import java.util.*;
import Server.Common.ResourceManager;
import Server.Common.Trace;

public class Transaction {

	int xid; // transaction id
	// int timeToLive; TODO implement timeToLive mechanism

	private List<ResourceManager> rmList = new ArrayList<ResourceManager>(); // keeps track of Resource Managers related
																				// to this transaction

	public Transaction(int xid) {
		this.xid = xid;
	}

	public void addRM(ResourceManager resourceManager) {
		if (!rmList.contains(resourceManager)) {
			rmList.add(resourceManager); // add rm to rmList
		} else {
			// rm already part of list, dont add dup
			Trace.info("Transaction::addRM() rm already in rmList for xid: " + xid); // for testing purposes
		}
	}

	public void resetTimeToLive() {
		// TODO
	}

	public int getRemainingTimeToLive() {
		// TODO
		return 0;
	}
}
