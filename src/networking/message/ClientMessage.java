package networking.message;

import java.io.*;

import networking.server.*;

public class ClientMessage implements Serializable {

	private final ClientMessageType type;
	private final PlayerInfo playerInfo;
	
	public ClientMessage(ClientMessageType type, PlayerInfo playerInfo) {
		this.type = type;
		this.playerInfo = playerInfo;
	}

	public ClientMessageType getType() {
		return type;
	}
	
	public PlayerInfo getPlayerInfo() {
		return playerInfo;
	}
	
	@Override
	public String toString() {
		return type + ":" + playerInfo;
	}
}
