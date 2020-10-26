package networking.message;

import java.io.*;

public class ServerMessage implements Serializable {
	private final ServerMessageType type;
	
	public ServerMessage(ServerMessageType type) {
		this.type = type;
	}

	public ServerMessageType getServerMessageType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}
