package networking.server;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.Timer;

import game.*;
import game.actions.*;
import game.ai.*;
import networking.*;
import networking.message.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class Server {
	public static final int PORT = 25565;
	
	private ConcurrentHashMap<Connection, Boolean> connections = new ConcurrentHashMap<>();
	private Thread thread;
	
	private ServerGUI gui;

	public static final int MILLISECONDS_PER_TICK = 100;
	private boolean isFastForwarding = false;
	private Game gameInstance;
	private volatile boolean startedGame = false;
	private volatile boolean madeWorld = false;

	private ArrayList<AIInterface> ailist = new ArrayList<>();

	public Server() {
		
	}
	public void setGUI(ServerGUI serverGUI) {
		this.gui = serverGUI;
	}
	
	public void startAcceptingConnections() {
		thread = new Thread(() -> {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Listening for connections on " + PORT);
				gui.updateInfo("Listening for connections on " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);
				while(true) {
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
		madeWorld = true;
		System.out.println("Making world");
		gameInstance = new Game(new GUIController() {
			@Override
			public void updateGUI() {}
			@Override
			public void selectedUnit(Unit unit, boolean selected) {}
			@Override
			public void selectedBuilding(Building building, boolean selected) {}
			@Override
			public void selectedPlant(Plant plant, boolean selected) {}
			@Override
			public void changedFaction(Faction faction) {}
			@Override
			public void pushInfoPanel(InfoPanel infoPanel) { }
			@Override
			public void popInfoPanel() { }
			@Override
			public void pressedSelectedUnitPortrait(Unit unit) { }
			@Override
			public void switchInfoPanel(InfoPanel infoPanel) { }
			@Override
			public void tryToCraftItem(ItemType type, int amount) { }
			@Override
			public void research(ResearchType researchType) { }
			@Override
			public void setFastForwarding(boolean enabled) { }
			@Override
			public void setPauseGame(boolean enabled) { }
		});
		LinkedList<PlayerInfo> players = new LinkedList<>();
		for (Connection connection : connections.keySet()) {
			players.add(connection.getPlayerInfo());
		}
		Set<String> bots = new HashSet<>();
		for(int i = 0; i < Settings.NUM_AI; i++) {
			PlayerInfo bot = new PlayerInfo("Bot " + i, null);
			players.add(bot);
			bots.add(bot.getName());
		}
		gameInstance.generateWorld(Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT, false, players);
		gui.setGameInstance(gameInstance);
		gui.getGameView().getDrawer().updateTerrainImages();

		for(Faction f : gameInstance.world.getFactions()) {
			if(f.isPlayer() && bots.contains(f.name())) {
				ailist.add(new BuildOrderAI(gui.getCommandInterface(), f, gameInstance.world));
			}
		}
		NeutralAI neutralAI = new NeutralAI(
				gui.getCommandInterface(),
				gameInstance.world.getFaction(World.NO_FACTION_ID),
				gameInstance.world);
		ailist.add(neutralAI);
		UndeadAI undeadAI = new UndeadAI(
				gui.getCommandInterface(),
				gameInstance.world.getFaction(World.UNDEAD_FACTION_ID),
				gameInstance.world);
		ailist.add(undeadAI);
		
		startWorldNetworkingUpdateThread();

		Game.DISABLE_NIGHT = true;
		Timer repaintingThread = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.getGUIController().updateGUI();
				gui.repaint();
			}
		});
		Thread terrainImageThread = new Thread(() -> {
			while (true) {
				try {
					gui.updateTerrainImages();
					Thread.sleep(500);
				} catch (Exception e1) {
					e1.printStackTrace();
					if(e1 instanceof InterruptedException) {
						break;
					}
				}
			}
		});
		repaintingThread.start();
		terrainImageThread.start();
	}
	
	private void sendWhichFaction() {
		for (Connection connection : connections.keySet()) {
			Faction f = gameInstance.world.getFaction(connection.getPlayerInfo().getName());
			if(f != null) {
				connection.sendMessage(f);
			}
		}
	}
	
	private void startWorldNetworkingUpdateThread() {
		Thread worldNetworkingUpdateThread = new Thread(() -> {
			try {
				int iteration = 0;
				while(true) {
					if(iteration % 5 == 0) {
						sendFullWorld();
					}
					else if(iteration % 3 == 0) {
						sendUnits();
					}
					else {
						sendProjectilesAndDeadThings();
					}
					if(iteration % 100 == 0) {
						System.out.println("skipped sends (nothing to send): " + skippedCount + "/" + (skippedCount + sentCount));
					}
					iteration++;
					iteration = iteration % 100;
					Thread.sleep(MILLISECONDS_PER_TICK);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		worldNetworkingUpdateThread.start();
	}
	
	
	private void sendFullWorld() {
//		saveToFile(worldInfo, "ser/everything_" + World.ticks + ".ser");
		WorldInfo worldInfo = Utils.extractWorldInfo(gameInstance.world, true, true);

//		WorldInfo worldInfo = new WorldInfo(
//				gameInstance.world.getWidth(),
//				gameInstance.world.getHeight(),
//				World.ticks, new Tile[0]);
		sendToAllConnections(worldInfo);
//		Temp t = new Temp(gameInstance.world.getWidth(), gameInstance.world.getHeight(), World.ticks, new Tile[0]);
//		t.getThings().addAll(gameInstance.world.getBuildings());
//		sendToAllConnections(t);
		sendWhichFaction();
	}

	private void sendUnits() {
		WorldInfo worldInfo = Utils.extractWorldInfo(gameInstance.world, false, true);
//		WorldInfo worldInfo = new WorldInfo(gameInstance.world.getWidth(), gameInstance.world.getHeight(), World.ticks, new Tile[0]);
//		worldInfo.getThings().addAll(gameInstance.world.getUnits());
//		worldInfo.getThings().addAll(gameInstance.world.getData().clearDeadThings());
//		worldInfo.addHitsplats(gameInstance.world.getData());
//		worldInfo.getProjectiles().addAll(gameInstance.world.getData().clearProjectilesToSend());
		sendToAllConnections(worldInfo);
//		saveToFile(worldInfo, "ser/units_" + World.ticks + ".ser");
	}
	
	private int skippedCount;
	private int sentCount;
	private void sendProjectilesAndDeadThings() {
		WorldInfo worldInfo = Utils.extractWorldInfo(gameInstance.world, false, false);
//		WorldInfo worldInfo = new WorldInfo(gameInstance.world.getWidth(), gameInstance.world.getHeight(), World.ticks, new Tile[0]);
//		worldInfo.getThings().addAll(gameInstance.world.getData().clearDeadThings());
//		worldInfo.addHitsplats(gameInstance.world.getData());
//		worldInfo.getProjectiles().addAll(gameInstance.world.getData().clearProjectilesToSend());
		if(worldInfo.getThings().isEmpty() 
				&& worldInfo.getHitsplats().isEmpty()
				&& worldInfo.getProjectiles().isEmpty()) {
			skippedCount++;
		}
		else {
			sentCount++;
			sendToAllConnections(worldInfo);
	//		saveToFile(worldInfo, "ser/projectiles_" + World.ticks + ".ser");
		}
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
					gui.getCommandInterface().planAction((Unit)thing, PlannedAction.moveTo(targetTile), message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.ATTACK_THING) {
			if(thing instanceof Unit) {
				Thing target = ThingMapper.get(message.getTargetID());
				if(target != null) {
					gui.getCommandInterface().planAction((Unit)thing, PlannedAction.attack(target), message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.BUILD_ROAD) {
			if(thing instanceof Unit) {
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				if(targetTile != null) {
					gui.getCommandInterface().planAction((Unit)thing, PlannedAction.buildOnTile(targetTile, true), message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.BUILD_BUILDING) {
			if(thing instanceof Unit) {
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				if(targetTile != null) {
					gui.getCommandInterface().planAction((Unit)thing, PlannedAction.buildOnTile(targetTile, false), message.getClearQueue());
				}
			}
		}
		else if(message.getCommand() == CommandType.PLAN_BUILDING) {
			if(thing instanceof Unit) {
				Thing target = ThingMapper.get(message.getTargetID());
				Tile targetTile = gameInstance.world.get(message.getTargetLocation());
				BuildingType buildingType = Game.buildingTypeMap.get(message.getType());
				if(target != null) {
					gui.getCommandInterface().planAction((Unit)thing, PlannedAction.buildOnTile(target.getTile(), buildingType.isRoad()), message.getClearQueue());
				}
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
			if(message.getFaction() >= 0 && message.getFaction() < gameInstance.world.getFactions().size()) {
				ResearchType researchType = Game.researchTypeMap.get(message.getType());
				if(researchType != null) {
					gui.getCommandInterface().research(gameInstance.world.getFactions().get(message.getFaction()), researchType);
				}
			}
		}
		else if(message.getCommand() == CommandType.CRAFT_ITEM) {
			if(message.getFaction() >= 0 && message.getFaction() < gameInstance.world.getFactions().size()) {
				ItemType itemType = ItemType.valueOf(message.getType());
				if(itemType != null) {
					gui.getCommandInterface().craftItem(gameInstance.world.getFactions().get(message.getFaction()), itemType, message.getAmount());
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
		else if(message.getCommand() == CommandType.SET_GUARDING) {
			if(thing instanceof Unit) {
				gui.getCommandInterface().setGuarding((Unit)thing, message.getClearQueue());
			}
		}
		else if (message.getCommand() == CommandType.PLANNED_ACTION) {
			if(thing instanceof Unit) {
				PlannedAction action = message.getPlannedAction();
				Thing target = ThingMapper.get(action.targetID);
				Tile targetTile = (action.targetTileLoc == null) ? null : gameInstance.world.get(action.targetTileLoc);
				PlannedAction mapped = PlannedAction.makeCopy(action, target, targetTile);
				gui.getCommandInterface().planAction((Unit)thing, mapped, message.getClearQueue());
			}
			else {
				System.err.println("planned action should not have a building as the acting thing");
			}
		}
	}
	
	private void startGame() {
		startedGame = true;
		Thread gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					gameInstance.getGUIController().updateGUI();
					long elapsed = System.currentTimeMillis() - start;
					gui.getGameView().setPreviousTickTime(elapsed);
					if(World.ticks % 200 == 1) {
						System.out.println("time elapsed for tick: " + elapsed + "ms");
					}
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
		
		Thread aiThread = new Thread(() -> {
			try {
				while(true) {
					for(AIInterface ai : ailist) {
						ai.tick();
					}
					Thread.sleep(Settings.AIDELAY);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		aiThread.start();
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
							if(!madeWorld) {
								makeWorld();
							}
						}
						else if(clientMessage.getType() == ClientMessageType.START_GAME) {
							if(!startedGame) {
								startGame();
							}
						}
					}
					else if(message instanceof CommandMessage) {
						if(startedGame) {
							CommandMessage commandMessage = (CommandMessage)message;
							handleCommand(commandMessage);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
}
