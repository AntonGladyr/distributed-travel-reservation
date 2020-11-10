package Transactions;

import java.util.ArrayList;
import java.util.HashMap;

import Server.Common.ReservableItem;
import Server.Common.Trace;

public class TransactionNode {
	
	// Keeps track of before images for written objects (to undo changes upon abort)
	private static HashMap<Integer, ArrayList<BeforeImage>> beforeImages = new HashMap<Integer, ArrayList<BeforeImage>>();
	
	// Called when writing an object. If it hasn't been edited yet, make a before image of the object.
	public static void beforeWriting(int xid, String objectKey, ReservableItem object)
	{
		ArrayList<BeforeImage> imageList = beforeImages.get(xid);
		
		// Instantiate the transaction's list of before images if it doesn't have one yet
		if (imageList == null) {
			imageList = new ArrayList<BeforeImage>();
			beforeImages.put(xid, imageList);
			
			Trace.info("RM::first write for transaction " + xid);
		}
		
		// Check whether the object has been written yet in this transaction
		boolean alreadyWritten = false;
		
		for (int i = 0; i < imageList.size(); i++) {
			BeforeImage oldObject = imageList.get(i);
			if (oldObject.getKey() == objectKey) {
				alreadyWritten = true;
				break;
			}
		}
		
		// If it hasn't been written yet, save a copy of this object
		if (!alreadyWritten) {
			Trace.info("RM::saving before-image (" + xid + ", " + objectKey + ")");
			
			ReservableItem objectClone = (ReservableItem) object.clone();
			BeforeImage image = new BeforeImage(objectKey, objectClone);
			imageList.add(image);
		}
	}
}
