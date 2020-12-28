package networking.message;

import java.util.*;

import networking.server.*;

public class LobbyListMessage extends ServerMessage {

	private final PlayerInfo[] lobbyList;
	public LobbyListMessage(PlayerInfo[] lobbyList) {
		super(ServerMessageType.LOBBY);
		this.lobbyList = lobbyList;
	}
	public PlayerInfo[] getLobbyList() {
		return lobbyList;
	}
	
	@Override
	public String toString() {
		return super.toString() + ":" + Arrays.toString(lobbyList);
	}
}
