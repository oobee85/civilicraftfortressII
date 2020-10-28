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
import utils.*;
import world.*;

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
	
	private HashMap<Integer, Thing> things = new HashMap<>();
	
	private void worldInfoUpdate(WorldInfo worldInfo) {
		if(gameInstance.world == null) {
			gameInstance.initializeWorld(worldInfo.getWidth(), worldInfo.getHeight());
			clientGUI.worldReceived();
			Thread cleaningThread = new Thread(() -> {
				try {
					while (true) {
						gameInstance.world.clearDeadAndAddNewThings();
						Thread.sleep(Server.MILLISECONDS_PER_TICK);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
			cleaningThread.start();
		}
		World.ticks = worldInfo.getTick();
		gameInstance.world.updateTiles(worldInfo.getTileInfos());
		for(Thing update : worldInfo.getThings()) {
			if(!things.containsKey(update.id)) {
				createThing(update);
			}
			updateThing(things.get(update.id), update);
		}
		synchronized (updatedTerrain) {
			updatedTerrain.notify();
		}
		clientGUI.repaint();
	}
	private void createThing(Thing update) {
		if(update instanceof Plant) {
			Plant plantUpdate = (Plant)update;
			Plant newPlant = new Plant(plantUpdate.getPlantType(), gameInstance.world.get(plantUpdate.getTile().getLocation()));
			things.put(update.id, newPlant);
			newPlant.getTile().setHasPlant(newPlant);
			gameInstance.world.newPlants.add(newPlant);
		}
		else if(update instanceof Building) {
			Building buildingUpdate = (Building)update;
			Building newBuilding = new Building(
					Game.buildingTypeMap.get(buildingUpdate.getType().name()), 
					gameInstance.world.get(buildingUpdate.getTile().getLocation()), 
					World.factions[buildingUpdate.getFaction().id]);
			things.put(update.id, newBuilding);
			gameInstance.world.newBuildings.add(newBuilding);
			if(newBuilding.getType().isRoad()) {
				newBuilding.getTile().setRoad(newBuilding);
			}
			else {
				newBuilding.getTile().setBuilding(newBuilding);
			}
		}
	}
	
	private void updateThing(Thing existing, Thing update) {
		if(existing instanceof Plant) {
			Plant existingPlant = (Plant)existing;
			Plant plantUpdate = (Plant)update;
			existingPlant.setPlantType(plantUpdate.getPlantType());
		}
		else if(update instanceof Building) {
			Building existingBuilding = (Building)existing;
			Building buildingUpdate = (Building)update;
			existingBuilding.setType(Game.buildingTypeMap.get(buildingUpdate.getType().name()));
			existingBuilding.setRemainingEffort(buildingUpdate.getRemainingEffort());
			existingBuilding.setCulture(buildingUpdate.getCulture());
			existingBuilding.setPlanned(buildingUpdate.isPlanned());
		}
		existing.setFaction(update.getFaction());
		existing.setMaxHealth(update.getMaxHealth());
		existing.setHealth(update.getHealth());
		existing.setDead(update.isDead());
		existing.setTile(gameInstance.world.get(update.getTile().getLocation()));
		if(existing.isDead()) {
			things.remove(update.id);
		}
	}
	
	public void startReceiving() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Object message = connection.getMessage();
					System.err.println("processing message " + message);
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
