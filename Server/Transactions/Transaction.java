package Transactions;

import java.util.*;
import Server.Common.ResourceManager;

public class Transaction {
	int xid; //transaction id
	private List<ResourceManager> rmList = new ArrayList<ResourceManager>(); //keeps track of Resource Managers related to this transaction
	
}
