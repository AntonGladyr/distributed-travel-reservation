// TODO move to common location to avoid duplicate code
package Server.TCP;

import java.io.Serializable;

public class TCPMessage  implements Serializable {

	private static final long serialVersionUID = 1L;

	public MessageType type;
	
	public TCPMessage(MessageType type) {
		this.type = type;
	}
}
