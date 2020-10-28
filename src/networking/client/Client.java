package networking.client;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.Timer;

import game.*;
import networking.*;
import networking.message.*;
import networking.server.*;
import networking.view.*;
import ui.*;

public class Client {
	
	private Connection connection;
	private ClientGUI clientGUI;

	private Game gameInstance;
	private volatile Object updatedTerrain = new Object();

	public Client() {
		
	}
	public void sendMessage(Object message) {
		connection.sendMessage(message);
	}
	
	public void connectToServer(InetAddress ip) {
		Socket socket = null;
		try {
			socket = new Socket(ip, Server.PORT);
			connection = new Connection(socket);
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
	
	private void worldInfoUpdate(WorldInfo worldInfo) {
		if(gameInstance.world == null) {
			gameInstance.initializeWorld(worldInfo.getWidth(), worldInfo.getHeight());
			clientGUI.worldReceived();
		}
		gameInstance.world.updateTiles(worldInfo.getTileInfos());
		synchronized (updatedTerrain) {
			updatedTerrain.notify();
		}
		clientGUI.repaint();
	}
	
	public void startReceiving() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Object message = connection.getMessage();
					System.out.println("received message " + message);
					if(message instanceof ServerMessage) {
						ServerMessage serverMessage = (ServerMessage)message;
						if(serverMessage.getServerMessageType() == ServerMessageType.LOBBY) {
							LobbyListMessage lobbyListMessage = (LobbyListMessage) serverMessage;
							clientGUI.updatedLobbyList(lobbyListMessage.getLobbyList());
						}
					}
					else if(message instanceof WorldInfo) {
						worldInfoUpdate((WorldInfo)message);
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
		clientGUI.setGameInstance(gameInstance);

		Timer repaintingThread = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clientGUI.repaint();
			}
		});
		repaintingThread.start();
		Thread terrainImageThread = new Thread(() -> {
			while (true) {
				try {
					synchronized (updatedTerrain) {
						updatedTerrain.wait();
					}
					clientGUI.updateTerrainImages();
				} catch (Exception e1) {
					e1.printStackTrace();
					if(e1 instanceof InterruptedException) {
						break;
					}
				}
			}
		});
		terrainImageThread.start();
	
	}
}
