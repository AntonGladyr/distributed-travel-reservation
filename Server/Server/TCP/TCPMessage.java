// TODO move to common location to avoid duplicate code
package Server.TCP;

public class TCPMessage {

	public MessageType type;
	
	public TCPMessage(MessageType type) {
		this.type = type;
	}
}
