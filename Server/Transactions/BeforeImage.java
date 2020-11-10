package Transactions;

import Server.Common.ReservableItem;

public class BeforeImage {

	public String key;
	public ReservableItem object;
	
	public BeforeImage(String key, ReservableItem object) {
		this.key = key;
		this.object = object;
	}
	
	public String getKey() {
		return key;
	}
}
