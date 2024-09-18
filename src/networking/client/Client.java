package networking.client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;

import game.*;
import game.actions.*;
import game.ai.*;
import networking.*;
import networking.message.*;
import networking.server.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class Client {

	private Connection connection;
	private ClientGUI clientGUI;

	private Game gameInstance;
	private CommandInterface localCommands;
	private CommandInterface networkingCommands;
	private volatile Object updatedTerrain = new Object();
	private HashMap<Integer, Thing> things = new HashMap<>();
	
	private ArrayList<AIInterface> ailist = new ArrayList<>();
	private static final long AIDELAY = 1000;
	
	private volatile boolean isFastForwarding;
	private volatile boolean isPaused;

	public Client() {
		gameInstance = new Game(new GUIController() {
			@Override
			public void updateGUI() {
				if(gameInstance.world != null) {
					clientGUI.getGameViewOverlay().updateItems();
					clientGUI.getWorkerView().updateButtons();
					clientGUI.getResearchView().updateButtons();
					clientGUI.getProduceUnitView().updateButtons();
					clientGUI.getCraftingView().updateButtons();
				}
			}
			@Override
			public void selectedUnit(Unit unit, boolean selected) {
				clientGUI.getGameViewOverlay().selectedUnit(unit, selected);
				if(unit.isBuilder()) {
					clientGUI.manageBuildingTab(selected);
				}
				if(selected) {
					UnitInfoPanel infoPanel = new UnitInfoPanel(unit);
					switchInfoPanel(infoPanel);
					SwingUtilities.invokeLater(() -> {
						infoPanel.addExplodeButton().addActionListener(e -> gameInstance.explode(unit));
						infoPanel.addButton("MakeRoads").addActionListener(e -> clientGUI.getGameView().workerRoad(Game.buildingTypeMap.get("STONE_ROAD")));
						infoPanel.addButton("AutoBuild").addActionListener(e -> clientGUI.getGameView().toggleAutoBuild());
						infoPanel.addButton("Guard").addActionListener(e -> clientGUI.getGameView().toggleGuarding());
					});
				}
			}
			@Override
			public void selectedPlant(Plant plant, boolean selected) {
				
			}
			@Override
			public void selectedBuilding(Building building, boolean selected) {
				if(building.getType().unitsCanProduceSet().size() > 0) {
					clientGUI.manageProduceUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("SMITHY")) {
					clientGUI.manageBlacksmithTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("HELLFORGE")) {
					clientGUI.manageBlacksmithTab(selected);
				}
				InfoPanel infoPanel = new BuildingInfoPanel(building);
				switchInfoPanel(infoPanel);
				SwingUtilities.invokeLater(() -> {
					infoPanel.addExplodeButton().addActionListener(e -> gameInstance.explode(building));
//					infoPanel.addButton("Explode")
				});
			}
			@Override
			public void changedFaction(Faction faction) {
				clientGUI.getGameView().setFaction(faction);
				clientGUI.getGameViewOverlay().changeFaction(faction);
				System.out.println("CHANGED FACTION TO " + faction);
			}
			@Override
			public void switchInfoPanel(InfoPanel infoPanel) {
				clientGUI.getInfoPanelView().switchInfoPanel(infoPanel);
			}
			@Override
			public void pushInfoPanel(InfoPanel infoPanel) {
				clientGUI.getInfoPanelView().pushInfoPanel(infoPanel);
			}
			@Override
			public void popInfoPanel() {
				clientGUI.getInfoPanelView().popInfoPanel();
			}
			@Override
			public void pressedSelectedUnitPortrait(Unit unit) {
				clientGUI.getGameView().pressedSelectedUnitPortrait(unit);
			}
			@Override
			public void tryToCraftItem(ItemType type, int amount) {
				clientGUI.getGameView().getCommandInterface().craftItem(clientGUI.getGameView().getFaction(), type, amount);
			}
			@Override
			public void research(ResearchType researchType) {
				clientGUI.getGameView().getCommandInterface().research(clientGUI.getGameView().getFaction(), researchType);
			}
			@Override
			public void setFastForwarding(boolean enabled) {
				isFastForwarding = enabled;
			}
			@Override
			public void setPauseGame(boolean enabled) {
				isPaused = enabled;
			}
		});
		localCommands = Utils.makeFunctionalCommandInterface(gameInstance);
		networkingCommands = new CommandInterface() {
			@Override
			public void setBuildingRallyPoint(Building building, Tile rallyPoint) {
				sendMessage(CommandMessage.makeSetRallyPointCommand(building.id(), rallyPoint.getLocation()));
				localCommands.setBuildingRallyPoint(building, rallyPoint);
			}
//			@Override
//			public void buildThing(Unit unit, Tile target, boolean isRoad, boolean clearQueue) {
//				if(isRoad) {
//					sendMessage(CommandMessage.makeBuildRoadCommand(unit.id(), target.getLocation(), clearQueue));
//				}
//				else {
//					sendMessage(CommandMessage.makeBuildBuildingCommand(unit.id(), target.getLocation(), clearQueue));
//				}
//				localCommands.buildThing(unit, target, isRoad, clearQueue);
//			}
			@Override
			public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType) {
				sendMessage(CommandMessage.makePlanBuildingCommand(unit.id(), target.getLocation(), clearQueue, buildingType.name()));
				localCommands.planAction(unit, PlannedAction.moveTo(target), clearQueue);
				return null;
			}
			@Override
			public void stop(Unit unit) {
				sendMessage(CommandMessage.makeStopCommand(unit.id()));
				localCommands.stop(unit);
			}
			@Override
			public void research(Faction faction, ResearchType researchType) {
				sendMessage(CommandMessage.makeResearchCommand(faction.id(), researchType.name()));
				localCommands.research(faction, researchType);
			}
			@Override
			public void craftItem(Faction faction, ItemType itemType, int amount) {
				sendMessage(CommandMessage.makeCraftItemCommand(faction.id(), itemType.name(), amount));
				localCommands.craftItem(faction, itemType, amount);
			}
			@Override
			public void produceUnit(Building building, UnitType unitType) {
				sendMessage(CommandMessage.makeProduceUnitCommand(building.id(), unitType.name()));
				localCommands.produceUnit(building, unitType);
			}
			@Override
			public void setGuarding(Unit unit, boolean enabled) {
				sendMessage(CommandMessage.makeSetGuardingCommand(unit.id(), enabled));
				localCommands.setGuarding(unit, enabled);
			}
			@Override
			public void planAction(Unit unit, PlannedAction plan, boolean enabled) {
				// TODO figure out more generic networking stuff.
			}
			
		};
	}
	
	public void loadGame() {
		WorldInfo worldInfo = Utils.loadFromFile("save1.civ");
		worldInfoUpdate(worldInfo);
		for(Faction f : worldInfo.getFactions()) {
			factionUpdate(f);
		}
		setupSinglePlayer(false);
	}
	
	public void setupSinglePlayer(boolean createWorld) {
		clientGUI.startedSinglePlayer();
		Thread t = new Thread(() -> {
			clientGUI.getGameView().setCommandInterface(localCommands);
			if(createWorld) {
				LinkedList<PlayerInfo> players = new LinkedList<>();
				players.add(clientGUI.getPlayerInfo());
				for(int i = 0; i < Settings.NUM_AI; i++) {
					players.add(new PlayerInfo("Bot " + i, null));
				}
				clientGUI.getGameView().centerViewOnTile(new TileLoc(Settings.WORLD_WIDTH/10, Settings.WORLD_HEIGHT/10), 10);
				gameInstance.generateWorld(Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT, false, players);
				clientGUI.getGameView().getDrawer().updateTerrainImages();
			}
			boolean assignedPlayer = false;
			for(Faction f : gameInstance.world.getFactions()) {
				if(f.isPlayer()) {
					if(!assignedPlayer) {
						gameInstance.getGUIController().changedFaction(f);
						assignedPlayer = true;
					}
					else {
						// create and assign ai
						ailist.add(new BuildOrderAI(localCommands, f, gameInstance.world));
//						if (ailist.size() % 2 == 0) {
//							ailist.add(new BuildOrderAI(localCommands, f, gameInstance.world));
//						}
//						else {
//							ailist.add(new BasicAI(localCommands, f, gameInstance.world));
//						}
					}
				}
			}
			NeutralAI neutralAI = new NeutralAI(
					localCommands,
					gameInstance.world.getFaction(World.NO_FACTION_ID),
					gameInstance.world);
			ailist.add(neutralAI);
			UndeadAI undeadAI = new UndeadAI(
					localCommands,
					gameInstance.world.getFaction(World.UNDEAD_FACTION_ID),
					gameInstance.world);
			ailist.add(undeadAI);
			

//			SwingUtilities.invokeLater(() -> {
				clientGUI.repaint();
				startLocalGameLoopThread(false);
				SwingUtilities.invokeLater(() -> {
					clientGUI.getGameView().centerViewOnTile(new TileLoc(Settings.WORLD_WIDTH/3, Settings.WORLD_HEIGHT/3), Settings.DEFAULT_TILE_SIZE);
					clientGUI.getGameView().recenterCameraOnPlayer();
					clientGUI.getGameView().enableMouse();
				});
//			});
		});
		t.start();
	}
	public void sendMessage(Object message) {
		connection.sendMessage(message);
	}
	
	public void connectToServer(InetAddress ip) {
		System.out.println("Connecting to " + ip);
		Socket socket = null;
		try {
			socket = new Socket(ip, Server.PORT);
			connection = new Connection(socket);
			System.out.println("Connected to " + ip);
			connection.setDisconnectCallback(() -> {
				clientGUI.disconnected();
				System.out.println("reached callback");
			});
			sendMessage(new ClientMessage(ClientMessageType.INFO, clientGUI.getPlayerInfo()));
			clientGUI.connected(connection.getPanel());
			clientGUI.getGameView().setCommandInterface(networkingCommands);
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
	
	private void startLocalGameLoopThread(boolean simulated) {
		final long millisPerTick = Server.MILLISECONDS_PER_TICK;
		Thread gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					
					
					long start = System.currentTimeMillis();
					
					// IF PAUSED BUTTON PRESSED BUT NOT
					if(!isPaused) {
						if(simulated) {
							gameInstance.simulatedGameTick();
						}
						else {
							gameInstance.gameTick();
						}
					}
					gameInstance.world.getData().clearDeadThings();
					gameInstance.world.getData().clearProjectilesToSend();
					synchronized (updatedTerrain) {
						updatedTerrain.notify();
					}
					long elapsed = System.currentTimeMillis() - start;
					clientGUI.getGameView().setPreviousTickTime(elapsed);
					if(World.ticks % 200 == 1) {
//						System.out.println("time elapsed for tick: " + elapsed + "ms");
					}
					long sleeptime = millisPerTick - elapsed;
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
					Thread.sleep(AIDELAY);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		aiThread.start();
	}
	private boolean tilesReceived;
	private void worldInfoUpdate(WorldInfo worldInfo) {
		boolean firstUpdate = false;
		if(gameInstance.world == null) {
			gameInstance.initializeWorld(worldInfo.getWidth(), worldInfo.getHeight());
			clientGUI.worldReceived();
			firstUpdate = true;
		}
		if(gameInstance.world.getFactions().size() < worldInfo.getFactions().size()) {
			System.out.println("current ");
			for(int i = gameInstance.world.getFactions().size(); i < worldInfo.getFactions().size(); i++) {
				Faction received = worldInfo.getFactions().get(i);
				Faction faction = new Faction(received.name(), received.isPlayer(), received.usesItems(), received.usesResearch(), received.color());
				gameInstance.world.addFaction(faction);
			}
		}
		World.ticks = worldInfo.getTick();
		gameInstance.world.updateTiles(worldInfo.getTileInfos());
		if(worldInfo.getTileInfos().length > 0) {
			tilesReceived = true;
		}
		if(!tilesReceived) {
			return;
		}
		
		for(Thing update : worldInfo.getThings()) {
			if(!things.containsKey(update.id())) {
				createThing(update);
			}
			updateThing(things.get(update.id()), update);
		}
		for(Projectile projectileMessage : worldInfo.getProjectiles()) {
			Projectile newProjectile = new Projectile(
					projectileMessage.getType(), 
					gameInstance.world.get(projectileMessage.getTile().getLocation()), 
					gameInstance.world.get(projectileMessage.getTargetTile().getLocation()), 
					null, 
					projectileMessage.getDamage(),
					projectileMessage.getFromGround(),
					0); // TODO serialize ticksUntilLanding
			gameInstance.world.getData().addProjectile(newProjectile);
		}
		for(Hitsplat hitsplat : worldInfo.getHitsplats()) {
			Thing thing = things.get(hitsplat.getThingID());
			if(thing != null) {
				thing.getHitsplatList()[hitsplat.getSquare()] = hitsplat;
				thing.takeFakeDamage();
			}
		}
		synchronized (updatedTerrain) {
			updatedTerrain.notify();
		}
		if(firstUpdate) {
			startLocalGameLoopThread(true);
		}
		clientGUI.repaint();
	}
	private void createThing(Thing update) {
		System.out.println("creating thing");
		Thing newThing = null;
		if(update instanceof Plant) {
			System.out.println("creating plant");
			Plant plantUpdate = (Plant)update;
			PlantType type = plantUpdate.getType();
			System.out.println(type);
			TileLoc tileLoc = plantUpdate.getTileLocation();
			System.out.println(tileLoc);
			Plant newPlant = new Plant(
					Game.plantTypeMap.get(type.name()), 
					gameInstance.world.get(tileLoc),
					gameInstance.world.getFaction(World.NO_FACTION_ID));
			newThing = newPlant;
			newPlant.getTile().setHasPlant(newPlant);
			things.put(update.id(), newPlant);
			gameInstance.world.addPlant(newPlant);
		}
		else if(update instanceof Building) {
			Building buildingUpdate = (Building)update;
			TileLoc tileLoc = buildingUpdate.getTileLocation();
			Building newBuilding = new Building(
					Game.buildingTypeMap.get(buildingUpdate.getType().name()), 
					gameInstance.world.get(tileLoc), 
					gameInstance.world.getFactions().get(buildingUpdate.getFactionID()));
			newThing = newBuilding;
			if(newBuilding.getType().isRoad()) {
				newBuilding.getTile().setRoad(newBuilding);
			}
			else {
				newBuilding.getTile().setBuilding(newBuilding);
			}
			things.put(update.id(), newBuilding);
			gameInstance.world.addBuilding(newBuilding);
		}
		else if(update instanceof Unit) {
			Unit unitUpdate = (Unit)update;
			TileLoc tileLoc = unitUpdate.getTileLocation();
			Unit newUnit = new Unit(
					Game.unitTypeMap.get(unitUpdate.getType().name()), 
					gameInstance.world.get(tileLoc), 
					gameInstance.world.getFactions().get(unitUpdate.getFactionID()));
			newThing = newUnit;
			if(newUnit.getTile() != null) {
				newUnit.getTile().addUnit(newUnit);
			}
			things.put(update.id(), newUnit);
			gameInstance.world.addUnit(newUnit);
		}
		if(newThing != null) {
			newThing.setID(update.id());
		}
	}

	private void updateThing(Thing existing, Thing update) {
		existing.setFaction(gameInstance.world.getFactions().get(update.getFactionID()));
		existing.setMaxHealth(update.getMaxHealth());
		existing.setHealth(update.getHealth());
		existing.setDead(update.isDead());
		Tile movedFrom = null;
		if(existing.getTile() != null && !existing.getTile().equals(update.getTile())) {
			movedFrom = existing.getTile();
		} 
		existing.setTile(gameInstance.world.get(update.getTileLocation()));
		if(existing instanceof Plant) {
			Plant existingPlant = (Plant)existing;
			Plant plantUpdate = (Plant)update;
			existingPlant.setType(plantUpdate.getType());
		}
		else if(update instanceof Building) {
			Building existingBuilding = (Building)existing;
			Building buildingUpdate = (Building)update;
			existingBuilding.setType(Game.buildingTypeMap.get(buildingUpdate.getType().name()));
			existingBuilding.setRemainingEffort(buildingUpdate.getRemainingEffort());
			existingBuilding.setCulture(buildingUpdate.getCulture());
			existingBuilding.setPlanned(buildingUpdate.isPlanned());
			existingBuilding.setRemainingEffortToProduceUnit(buildingUpdate.getRemainingEffortToProduceUnit());
			existingBuilding.getProducingUnit().clear();
			for(Unit u : buildingUpdate.getProducingUnit()) {
				u.setType(Game.unitTypeMap.get(u.getType().name()));
			}
			existingBuilding.getProducingUnit().addAll(buildingUpdate.getProducingUnit());
		}
		else if(update instanceof Unit) {
			Unit existingUnit = (Unit)existing;
			Unit unitUpdate = (Unit)update;
			existingUnit.setType(Game.unitTypeMap.get(unitUpdate.getType().name()));
			existingUnit.setCombatStats(unitUpdate.getCombatStats());
			if(movedFrom != null) {
				movedFrom.removeUnit(existingUnit);
				existingUnit.getTile().addUnit(existingUnit);
			}
			if(existingUnit.getTile() != null && existingUnit.getFaction().usesItems()) {
				existingUnit.getTile().getInventory().clear();
			}
		}
		if(existing.isDead()) {
			things.remove(update.id());
		}
	}
	
	private void factionUpdate(Faction factionUpdate) {
		Faction faction = gameInstance.world.getFaction(factionUpdate.id());
		
		Research targetUpdate = factionUpdate.getResearchTarget();
		if(targetUpdate != null) {
			Research potentialTarget = faction.getResearch(factionUpdate.getResearchTarget().type());
			potentialTarget.setType(targetUpdate.type());
			potentialTarget.setPayedFor(targetUpdate.isPayedFor());
			potentialTarget.setCompleted(targetUpdate.isCompleted());
			potentialTarget.setResearchPointsSpend(targetUpdate.getPointsSpent());
			if(faction.getResearchTarget() != potentialTarget) {
				faction.setResearchTarget(potentialTarget.type());
			}
		}
		
		Item[] itemsUpdate = factionUpdate.getInventory().getItems();
		for(Item itemUpdate : itemsUpdate) {
			if (itemUpdate == null) {
				continue;
			}
			faction.getInventory().setAmount(itemUpdate.getType(), itemUpdate.getAmount());
		}
		
		if(clientGUI.getGameView().getFaction() != faction) {
			clientGUI.getGameView().getGameInstance().getGUIController().changedFaction(faction);
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
					else if(message instanceof Faction) {
						factionUpdate((Faction)message);
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
		clientGUI.setGameInstance(gameInstance);

		Timer repaintingThread = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.getGUIController().updateGUI();
				clientGUI.repaint();
			}
		});
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
		repaintingThread.start();
		terrainImageThread.start();
	}
}
