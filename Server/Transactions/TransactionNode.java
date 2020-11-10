package Transactions;

import java.util.ArrayList;
import java.util.HashMap;

import Server.Common.DataStore;
import Server.Common.ReservableItem;
import Server.Common.Trace;

public class TransactionNode {
	
	// Reference to the location where data is stored on this node
	public static DataStore dataStore;
	
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
			
			ReservableItem objectClone = object == null ? null : (ReservableItem) object.clone();
			BeforeImage image = new BeforeImage(objectKey, objectClone);
			imageList.add(image);
		}
	}
	
	// Commits the given transaction
	public static void commit(int xid) {
		Trace.info("RM::committing (" + xid + ")");
		finalize(xid);
	}
	
	// Aborts the given transaction by restoring its before-images
	public static void abort(int xid) {
		
		Trace.info("RM::aborting and restoring before-images for (" + xid + ")");
		
		ArrayList<BeforeImage> imageList = beforeImages.get(xid);
		
		// Restore each before image
		int count = 0;
		
		if (imageList != null) {
			count = imageList.size();
			for (int i = 0; i < count; i++) {
				BeforeImage before = imageList.get(i);
				dataStore.writeData(xid, before.key, before.object);
			}
		}

		Trace.info("RM::" + count + "before-images for (" + xid + ") were restored");
		
		// Terminate the transaction
		finalize(xid);
	}
	
	// Clears the memory for the given transaction
	public static void finalize(int xid) {
		beforeImages.remove(xid);
		Trace.info("RM::transaction (" + xid + ") before-image memory cleared");
	}
}
