package networking.server;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import game.*;
import networking.*;
import networking.message.*;
import networking.view.*;
import ui.*;
import world.*;

public class Server {
	public static final int PORT = 25565;
	public static final PlayerInfo DEFAULT_PLAYER_INFO = new PlayerInfo("Default", Color.LIGHT_GRAY);
	
	
	private ConcurrentHashMap<Connection<ServerMessage, ClientMessage>, Boolean> connections = new ConcurrentHashMap<>();
	private volatile boolean stop = false;
	private Thread thread;
	
	private ServerGUI gui;
	
	private Game gameInstance;

	public Server() {
		
	}
	public void setGUI(ServerGUI serverGUI) {
		this.gui = serverGUI;
	}
	
//	public void sendMessage(String message) {
//		for(Connection connection : connections) {
//			connection.sendMessage(message);
//		}
//	}
	
	public void startAcceptingConnections() {
		stop = false;
		thread = new Thread(() -> {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Listening for connections on " + PORT);
				gui.updateInfo("Listening for connections on " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);
				while(!stop) {
					Socket socket = serverSocket.accept();
					addNewConnection(socket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if(serverSocket != null) {
					try {
						serverSocket.close();
						System.out.println("Closed server socket");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				for(Connection<ServerMessage, ClientMessage> connection : connections.keySet()) {
					try {
						connection.close();
						System.out.println("Closed client socket");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		);
		thread.start();
	}
	
	private void addNewConnection(Socket socket) {
		Connection<ServerMessage, ClientMessage> connection = new Connection<>(socket);
		connection.setDisconnectCallback(() -> {
			gui.lostConnection(connection.getPanel());
			connections.remove(connection);
		});
		gui.addedConnection(connection.getPanel());
		connections.put(connection, true);
		startProcessing(connection);
		System.out.println("Accepted connection");
		updatedLobbyList();
	}
	
	private void updatedLobbyList() {
		ArrayList<PlayerInfo> infos = new ArrayList<>();
		for (Connection<ServerMessage, ClientMessage> connection : connections.keySet()) {
			infos.add(connection.getPlayerInfo());
		}
		PlayerInfo[] namesArray = infos.toArray(new PlayerInfo[0]);
		LobbyListMessage message = new LobbyListMessage(namesArray);
		for (Connection<ServerMessage, ClientMessage> connection : connections.keySet()) {
			connection.sendMessage(message);
		}
	}
	
	public void startProcessing(Connection<ServerMessage, ClientMessage> connection) {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					ClientMessage message = connection.getMessage();
					System.out.println("received " + message + " from " + connection);
					if(message.getType() == ClientMessageType.INFO) {
						connection.setPlayerInfo(message.getPlayerInfo());
						updatedLobbyList();
					}
					else if(message.getType() == ClientMessageType.MAKE_WORLD) {
						System.out.println("Making world");
						gameInstance = new Game(new GUIController() {
							@Override
							public void updateGUI() {}
							@Override
							public void toggleTileView() {}
							@Override
							public void selectedUnit(Unit unit, boolean selected) {}
							@Override
							public void selectedSpawnUnit(boolean selected) {}
							@Override
							public void selectedBuilding(Building building, boolean selected) {}
						});
						gameInstance.generateWorld(128, false);
						gui.setGameInstance(gameInstance);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
}
