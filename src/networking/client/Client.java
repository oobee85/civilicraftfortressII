package networking.client;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import game.*;
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
	
	private volatile boolean isFastForwarding;
	private volatile boolean isRaiseTerrain;

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
				if(unit.getType().isBuilder()) {
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
				if (building.getType() == Game.buildingTypeMap.get("BARRACKS")) {
					clientGUI.manageProduceUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("STABLES")) {
					clientGUI.manageProduceUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("CASTLE")) {
					clientGUI.manageProduceUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("WORKSHOP")) {
					clientGUI.manageProduceUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("BLACKSMITH")) {
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
			public void setRaiseTerrain(boolean enabled) {
				isRaiseTerrain = enabled;
			}
		});
		localCommands = Utils.makeFunctionalCommandInterface(gameInstance);
		networkingCommands = new CommandInterface() {
			@Override
			public void setBuildingRallyPoint(Building building, Tile rallyPoint) {
				sendMessage(CommandMessage.makeSetRallyPointCommand(building.id(), rallyPoint.getLocation()));
				localCommands.setBuildingRallyPoint(building, rallyPoint);
			}
			@Override
			public void moveTo(Unit unit, Tile target, boolean clearQueue) {
				sendMessage(CommandMessage.makeMoveToCommand(unit.id(), target.getLocation(), clearQueue));
				localCommands.moveTo(unit, target, clearQueue);
			}
			@Override
			public void attackThing(Unit unit, Thing target, boolean clearQueue) {
				sendMessage(CommandMessage.makeAttackThingCommand(unit.id(), target.id(), clearQueue));
				localCommands.attackThing(unit, target, clearQueue);
			}
			@Override
			public void buildThing(Unit unit, Tile target, boolean isRoad, boolean clearQueue) {
				if(isRoad) {
					sendMessage(CommandMessage.makeBuildRoadCommand(unit.id(), target.getLocation(), clearQueue));
				}
				else {
					sendMessage(CommandMessage.makeBuildBuildingCommand(unit.id(), target.getLocation(), clearQueue));
				}
				localCommands.buildThing(unit, target, isRoad, clearQueue);
			}
			@Override
			public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType) {
				sendMessage(CommandMessage.makePlanBuildingCommand(unit.id(), target.getLocation(), clearQueue, buildingType.name()));
				localCommands.moveTo(unit, target, clearQueue);
				return null;
			}
			@Override
			public void harvestThing(Unit unit, Thing target, boolean clearQueue) {
				sendMessage(CommandMessage.makeHarvestThingCommand(unit.id(), target.id(), clearQueue));
				localCommands.harvestThing(unit, target, clearQueue);
			}
			@Override
			public void deliver(Unit unit, Thing target, boolean clearQueue) {
				sendMessage(CommandMessage.makeDeliverCommand(unit.id(), target.id(), clearQueue));
				localCommands.deliver(unit, target, clearQueue);
			}
			@Override
			public void takeItems(Unit unit, Thing target, boolean clearQueue) {
				sendMessage(CommandMessage.makeTakeItemsCommand(unit.id(), target.id(), clearQueue));
				localCommands.takeItems(unit, target, clearQueue);
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
		clientGUI.getGameView().setCommandInterface(localCommands);
		if(createWorld) {
			gameInstance.generateWorld(128, 128, false, Arrays.asList(clientGUI.getPlayerInfo()));
		}
		for(Faction f : gameInstance.world.getFactions()) {
			if(f.isPlayer()) {
				gameInstance.getGUIController().changedFaction(f);
				break;
			}
		}

		SwingUtilities.invokeLater(() -> {
			clientGUI.startedSinglePlayer();
//			frame.remove(mainMenuPanel);
//			mainMenuPanel = null;
			
//			frame.getContentPane().add(gamepanel, BorderLayout.CENTER);
//			frame.getContentPane().add(guiSplitter, BorderLayout.EAST);
//			frame.pack();
//			
//			gamepanel.centerViewOn(gameInstance.world.buildings.getLast().getTile(), 50, gamepanel.getWidth(), gamepanel.getHeight());
//			gamepanel.requestFocusInWindow();
//			gamepanel.requestFocus();
			clientGUI.repaint();
			startLocalGameLoopThread(false);
		});
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
					if(simulated) {
						gameInstance.simulatedGameTick();
					}
					else {
						gameInstance.gameTick();
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
			for(int i = gameInstance.world.getFactions().size(); i < worldInfo.getFactions().size(); i++) {
				Faction received = worldInfo.getFactions().get(i);
				Faction faction = new Faction(received.name(), received.isPlayer(), received.usesItems(), received.color());
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
					projectileMessage.getDamage());
			gameInstance.world.getData().addProjectile(newProjectile);
		}
		for(WeatherEvent weatherMessage : worldInfo.getWeatherEvents()) {
			WeatherEvent newWeatherEvent = new WeatherEvent(
					gameInstance.world.get(weatherMessage.getTile().getLocation()), 
					gameInstance.world.get(weatherMessage.getTargetTile().getLocation()),
//					weatherMessage.timeLeft(),
					weatherMessage.getStrength(),
					weatherMessage.getLiquidType());
			gameInstance.world.getData().addWeatherEvent(newWeatherEvent);
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
		Thing newThing = null;
		if(update instanceof Plant) {
			Plant plantUpdate = (Plant)update;
			Plant newPlant = new Plant(
					plantUpdate.getType(), 
					gameInstance.world.get(plantUpdate.getTile().getLocation()), 
					gameInstance.world.getFaction(World.NO_FACTION_ID));
			newThing = newPlant;
			newPlant.getTile().setHasPlant(newPlant);
			things.put(update.id(), newPlant);
			gameInstance.world.addPlant(newPlant);
		}
		else if(update instanceof Building) {
			Building buildingUpdate = (Building)update;
			Building newBuilding = new Building(
					Game.buildingTypeMap.get(buildingUpdate.getType().name()), 
					gameInstance.world.get(buildingUpdate.getTile().getLocation()), 
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
			Unit newUnit = new Unit(
					Game.unitTypeMap.get(unitUpdate.getType().name()), 
					gameInstance.world.get(unitUpdate.getTile().getLocation()), 
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
		existing.setTile(gameInstance.world.get(update.getTile().getLocation()));
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
				existingUnit.getTile().getItems().clear();
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
//					System.err.println("processing message " + message);
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
