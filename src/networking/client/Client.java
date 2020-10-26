package networking.client;

import java.io.*;
import java.net.*;

import networking.*;
import networking.message.*;
import networking.server.*;
import networking.view.*;

public class Client {
	
	private Connection<ClientMessage, ServerMessage> connection;
	private ClientGUI clientGUI;

	public Client() {
		
	}
	public void sendMessage(ClientMessage message) {
		connection.sendMessage(message);
	}
	
	public void connectToServer(InetAddress ip) {
		Socket socket = null;
		try {
			socket = new Socket(ip, Server.PORT);
			connection = new Connection<>(socket);
			connection.setDisconnectCallback(() -> {
				clientGUI.disconnected();
				System.out.println("reached callback");
			});
			clientGUI.connected(connection.getPanel());
			startReceiving();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startReceiving() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					ServerMessage message = connection.getMessage();
					System.out.println("received message " + message);
					if(message.getServerMessageType() == ServerMessageType.LOBBY) {
						LobbyListMessage lobbyListMessage = (LobbyListMessage) message;
						clientGUI.updatedLobbyList(lobbyListMessage.getLobbyList());
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
	public void setGUI(ClientGUI clientGUI) {
		this.clientGUI = clientGUI;
	}
}
