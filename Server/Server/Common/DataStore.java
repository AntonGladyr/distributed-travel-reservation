package Server.Common;

public interface DataStore {
	public void writeData(int xid, String key, RMItem value);
}
