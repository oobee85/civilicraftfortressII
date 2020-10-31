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
import ui.infopanels.*;
import utils.*;
import world.*;

public class Server {
	public static final int PORT = 25565;
	public static final PlayerInfo DEFAULT_PLAYER_INFO = new PlayerInfo("Default", Color.LIGHT_GRAY);
	
	private ConcurrentHashMap<Connection, Boolean> connections = new ConcurrentHashMap<>();
	private volatile boolean stop = false;
	private Thread thread;
	
	private ServerGUI gui;

	public static final int MILLISECONDS_PER_TICK = 100;
	private boolean isFastForwarding = false;
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
				for(Connection connection : connections.keySet()) {
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
		Connection connection = new Connection(socket);
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
	
	private void sendToAllConnections(Object message) {
		for (Connection connection : connections.keySet()) {
			connection.sendMessage(message);
		}
	}
	
	private void updatedLobbyList() {
		ArrayList<PlayerInfo> infos = new ArrayList<>();
		for (Connection connection : connections.keySet()) {
			infos.add(connection.getPlayerInfo());
		}
		PlayerInfo[] namesArray = infos.toArray(new PlayerInfo[0]);
		LobbyListMessage message = new LobbyListMessage(namesArray);
		sendToAllConnections(message);
	}
	
	private void makeWorld() {
		System.out.println("Making world");
		gameInstance = new Game(new GUIController() {
			@Override
			public void updateGUI() {}
			@Override
			public void selectedUnit(Unit unit, boolean selected) {}
			@Override
			public void selectedSpawnUnit(boolean selected) {}
			@Override
			public void selectedBuilding(Building building, boolean selected) {}
			@Override
			public void changedFaction(Faction faction) {}
			@Override
			public void pushInfoPanel(InfoPanel infoPanel) {
			}
			@Override
			public void popInfoPanel() {
			}
			@Override
			public void pressedSelectedUnitPortrait(Unit unit) {
			}
			@Override
			public void switchInfoPanel(InfoPanel infoPanel) {
			}
			@Override
			public void tryToCraftItem(ItemType type, int amount) {
			}
		});
		gameInstance.generateWorld(128, 128, false);
		gui.setGameInstance(gameInstance);
		startWorldNetworkingUpdateThread();
	}
	
	private void startWorldNetworkingUpdateThread() {
		Thread worldNetworkingUpdateThread = new Thread(() -> {
			try {
				while(true) {
					sendFullWorld();
					Thread.sleep(MILLISECONDS_PER_TICK*10);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		worldNetworkingUpdateThread.start();
	}
	
	private void sendFullWorld() {
		ArrayList<Tile> tileInfos = new ArrayList<>(gameInstance.world.getTiles().size()); 
		for(Tile t : gameInstance.world.getTiles()) {
//			TileInfo info = new TileInfo(t.getHeight(), t.getIsTerritory().id, t.getLocation(), t.getHumidity(), t.liquidAmount, t.liquidType, t.getTerrain());
			tileInfos.add(t);
		}
		WorldInfo worldInfo = new WorldInfo(gameInstance.world.getWidth(), gameInstance.world.getHeight(), World.ticks, tileInfos.toArray(new Tile[0]));
		worldInfo.getThings().addAll(gameInstance.world.plants);
		worldInfo.getThings().addAll(gameInstance.world.buildings);
		worldInfo.getThings().addAll(gameInstance.world.units);
		
		sendToAllConnections(worldInfo);
	}
	
	private void handleCommand(CommandMessage message) {
		System.out.println(message);
		Thing thing = ThingMapper.get(message.getThingID());
		// TODO check if the player is in control of this thing before proceeding
		if(message.getCommand() == CommandType.SET_RALLY_POINT) {
			if(thing instanceof Building) {
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				if(targetTile != null) {
					gui.getCommandInterface().setBuildingRallyPoint((Building)thing, targetTile);
				}
			}
		}
		else if(message.getCommand() == CommandType.MOVE_TO) {
			if(thing instanceof Unit) {
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				if(targetTile != null) {
					gui.getCommandInterface().moveTo((Unit)thing, targetTile, message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.ATTACK_THING) {
			if(thing instanceof Unit) {
				Thing target = ThingMapper.get(message.getTargetID());
				if(target != null) {
					gui.getCommandInterface().attackThing((Unit)thing, target, message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.BUILD_THING) {
			if(thing instanceof Unit) {
				Thing target = ThingMapper.get(message.getTargetID());
				if(target != null) {
					gui.getCommandInterface().buildThing((Unit)thing, target, message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.PLAN_BUILDING) {
			if(thing instanceof Unit) {
				Thing target = ThingMapper.get(message.getTargetID());
				if(target != null) {
					gui.getCommandInterface().buildThing((Unit)thing, target, message.getClearQueue());
				}
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				BuildingType buildingType = Game.buildingTypeMap.get(message.getType());
				if(targetTile != null) {
					gui.getCommandInterface().planBuilding((Unit)thing, targetTile, message.getClearQueue(), buildingType);
				}
			}
		}
		else if(message.getCommand() == CommandType.STOP) {
			if(thing instanceof Unit) {
				gui.getCommandInterface().stop((Unit)thing);
			}
		}
		else if(message.getCommand() == CommandType.RESEARCH) {
			if(message.getFaction() >= 0 && message.getFaction() < World.factions.length) {
				ResearchType researchType = Game.researchTypeMap.get(message.getType());
				if(researchType != null) {
					gui.getCommandInterface().research(World.factions[message.getFaction()], researchType);
				}
			}
		}
		else if(message.getCommand() == CommandType.CRAFT_ITEM) {
			if(message.getFaction() >= 0 && message.getFaction() < World.factions.length) {
				ItemType itemType = ItemType.valueOf(message.getType());
				if(itemType != null) {
					gui.getCommandInterface().craftItem(World.factions[message.getFaction()], itemType);
				}
			}
		}
		else if(message.getCommand() == CommandType.PRODUCE_UNIT) {
			if(thing instanceof Building) {
				UnitType unitType = Game.unitTypeMap.get(message.getType());
				if(unitType != null) {
					gui.getCommandInterface().produceUnit((Building)thing, unitType);
				}
			}
		}
	}
	
	private void startGame() {
		Thread gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					gameInstance.getGUIController().updateGUI();
					long elapsed = System.currentTimeMillis() - start;
					long sleeptime = MILLISECONDS_PER_TICK - elapsed;
					if(sleeptime > 0 && !isFastForwarding) {
						Thread.sleep(sleeptime);
					}
				}
				catch(Exception e) {
					try (FileWriter fw = new FileWriter("ERROR_LOG.txt", true);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw)) {
						e.printStackTrace(out);
					} catch (IOException ee) {
					}
					e.printStackTrace();
					if(e instanceof InterruptedException) {
						break;
					}
				}
			}
		});
		gameLoopThread.start();
	}
	
	public void startProcessing(Connection connection) {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Object message = connection.getMessage();
					System.out.println("received " + message + " from " + connection);
					if(message instanceof ClientMessage) {
						ClientMessage clientMessage = (ClientMessage)message;
						if(clientMessage.getType() == ClientMessageType.INFO) {
							connection.setPlayerInfo(clientMessage.getPlayerInfo());
							updatedLobbyList();
						}
						else if(clientMessage.getType() == ClientMessageType.MAKE_WORLD) {
							makeWorld();
						}
						else if(clientMessage.getType() == ClientMessageType.START_GAME) {
							startGame();
						}
					}
					else if(message instanceof CommandMessage) {
						CommandMessage commandMessage = (CommandMessage)message;
						handleCommand(commandMessage);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
}
