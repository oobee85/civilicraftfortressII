package networking.client;

import java.awt.*;
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
import ui.Frame;
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

	public Client() {
		gameInstance = new Game(new GUIController() {
			@Override
			public void updateGUI() {
				if(gameInstance.world != null) {
					clientGUI.getGameViewOverlay().updateItems();
					clientGUI.getWorkerView().updateButtons();
					clientGUI.getResearchView().updateButtons(gameInstance.world);
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
			}
			@Override
			public void selectedSpawnUnit(boolean selected) {}
			@Override
			public void selectedBuilding(Building building, boolean selected) {
				if (building.getType() == Game.buildingTypeMap.get("BARRACKS")) {
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
			public void pressedSelectedUnitPortrait(Unit unit) {}
			@Override
			public void tryToCraftItem(ItemType type, int amount) {}
			@Override
			public void research(ResearchType researchType) {
				clientGUI.getGameView().getCommandInterface().research(clientGUI.getGameView().getFaction(), researchType);
			}
			@Override
			public void setFastForwarding(boolean enabled) {
				isFastForwarding = enabled;
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
			public void buildThing(Unit unit, Thing target, boolean clearQueue) {
				sendMessage(CommandMessage.makeBuildTargetCommand(unit.id(), target.id(), clearQueue));
				localCommands.buildThing(unit, target, clearQueue);
			}
			@Override
			public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType) {
				sendMessage(CommandMessage.makePlanBuildingCommand(unit.id(), target.getLocation(), clearQueue, buildingType.name()));
				localCommands.moveTo(unit, target, clearQueue);
				return null;
			}
			@Override
			public void stop(Unit unit) {
				sendMessage(CommandMessage.makeStopCommand(unit.id()));
				localCommands.stop(unit);
			}
			@Override
			public void research(Faction faction, ResearchType researchType) {
				sendMessage(CommandMessage.makeResearchCommand(faction.id, researchType.name()));
				localCommands.research(faction, researchType);
			}
			@Override
			public void craftItem(Faction faction, ItemType itemType) {
				sendMessage(CommandMessage.makeCraftItemCommand(faction.id, itemType.name()));
				localCommands.craftItem(faction, itemType);
			}
			@Override
			public void produceUnit(Building building, UnitType unitType) {
				sendMessage(CommandMessage.makeProduceUnitCommand(building.id(), unitType.name()));
				localCommands.produceUnit(building, unitType);
			}
		};
	}
	public void setupSinglePlayer() {
		clientGUI.getGameView().setCommandInterface(localCommands);
		gameInstance.generateWorld(128, 128, false, Arrays.asList(new PlayerInfo("Player", Color.pink)));
		for(Faction f : gameInstance.world.getFactions()) {
			if(f.isPlayer()) {
				gameInstance.getGUIController().changedFaction(f);
				break;
			}
		}

		Thread gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					gameInstance.getGUIController().updateGUI();
					synchronized (updatedTerrain) {
						updatedTerrain.notify();
					}
					long elapsed = System.currentTimeMillis() - start;
					long sleeptime = Frame.MILLISECONDS_PER_TICK - elapsed;
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
			gameLoopThread.start();
		});
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
		if(gameInstance.world.getFactions().size() < worldInfo.getFactions().size()) {
			for(int i = gameInstance.world.getFactions().size(); i < worldInfo.getFactions().size(); i++) {
				Faction received = worldInfo.getFactions().get(i);
				Faction faction = new Faction(received.name, received.isPlayer(), received.usesItems(), received.color);
				gameInstance.world.addFaction(faction);
			}
		}
		gameInstance.world.updateTiles(worldInfo.getTileInfos());
		
		for(Thing update : worldInfo.getThings()) {
			if(!things.containsKey(update.id())) {
				createThing(update);
			}
			updateThing(things.get(update.id()), update);
		}
		synchronized (updatedTerrain) {
			updatedTerrain.notify();
		}
		clientGUI.repaint();
	}
	private void createThing(Thing update) {
		Thing newThing = null;
		if(update instanceof Plant) {
			Plant plantUpdate = (Plant)update;
			Plant newPlant = new Plant(plantUpdate.getPlantType(), gameInstance.world.get(plantUpdate.getTile().getLocation()), gameInstance.world.getFaction(World.NO_FACTION_ID));
			newThing = newPlant;
			things.put(update.id(), newPlant);
			newPlant.getTile().setHasPlant(newPlant);
			gameInstance.world.newPlants.add(newPlant);
		}
		else if(update instanceof Building) {
			Building buildingUpdate = (Building)update;
			Building newBuilding = new Building(
					Game.buildingTypeMap.get(buildingUpdate.getType().name()), 
					gameInstance.world.get(buildingUpdate.getTile().getLocation()), 
					gameInstance.world.getFactions().get(buildingUpdate.getFactionID()));
			newThing = newBuilding;
			things.put(update.id(), newBuilding);
			gameInstance.world.newBuildings.add(newBuilding);
			if(newBuilding.getType().isRoad()) {
				newBuilding.getTile().setRoad(newBuilding);
			}
			else {
				newBuilding.getTile().setBuilding(newBuilding);
			}
		}
		else if(update instanceof Unit) {
			Unit unitUpdate = (Unit)update;
			Unit newUnit = new Unit(
					Game.unitTypeMap.get(unitUpdate.getType().name()), 
					gameInstance.world.get(unitUpdate.getTile().getLocation()), 
					gameInstance.world.getFactions().get(unitUpdate.getFactionID()));
			newThing = newUnit;
			things.put(update.id(), newUnit);
			gameInstance.world.newUnits.add(newUnit);
			if(newUnit.getTile() != null) {
				newUnit.getTile().addUnit(newUnit);
			}
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
		else if(update instanceof Unit) {
			Unit existingUnit = (Unit)existing;
			Unit unitUpdate = (Unit)update;
			existingUnit.setType(Game.unitTypeMap.get(unitUpdate.getType().name()));
			existingUnit.setRemainingEffort(unitUpdate.getRemainingEffort());
			existingUnit.setCombatStats(unitUpdate.getCombatStats());
			if(movedFrom != null) {
				movedFrom.removeUnit(existingUnit);
				existingUnit.getTile().addUnit(existingUnit);
			}
		}
		if(existing.isDead()) {
			things.remove(update.id());
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
						Faction faction = gameInstance.world.getFaction(((Faction)message).id);
						clientGUI.getGameView().getGameInstance().getGUIController().changedFaction(faction);
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
