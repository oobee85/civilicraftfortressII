package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import com.jogamp.common.util.RunnableExecutor.*;

import game.*;
import ui.*;
import ui.graphics.*;
import ui.graphics.opengl.*;
import ui.graphics.vanilla.*;
import utils.*;
import world.*;

public class GameView {
	
	private static final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
			new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ), new Point(0, 0), "blank cursor");

	private GUIController guiController;
	private CommandInterface commandInterface;

	private Game game;

	private boolean rightMouseDown = false;
	private boolean controlDown = false;
	private boolean shiftDown = false;
	private boolean[] pressedKeys = new boolean[KeyEvent.KEY_LAST];
	private Thread fpsMovementThread;
	private Robot fpsMouseRobot;

	private boolean summonPlayerControlled = true;
	private boolean setSpawnWeather = false;

	private final FillingLayeredPane panel;
	private final JPanel overlayPanel;
	private final Drawer vanillaDrawer;
	private final Drawer glDrawer;
	
	private Drawer currentActiveDrawer;
	private Component drawingCanvas;
	
	private final GameViewState state;

	public class GameViewState {
		public long previousTickTime;
		public volatile Position viewOffset = new Position(0, 0);
		public volatile int tileSize = 15;
		public boolean fpMode = true;
		
		public Point mousePressLocation;
		public Point previousMouse;
		public boolean draggingMouse;
		public boolean leftMouseDown = false;
		public boolean middleMouseDown = false;
		public TileLoc hoveredTile = new TileLoc(-1, -1);
		public Position[] boxSelect = new Position[2];
		public LeftClickAction leftClickAction = LeftClickAction.NONE;

		public boolean drawDebugStrings;
		public MapMode mapMode = MapMode.TERRAIN;
		
		public BuildingType selectedBuildingToPlan;
		public Object selectedThingToSpawn;
		
		public Faction faction = Faction.getTempFaction();
		public ConcurrentLinkedQueue<Thing> selectedThings = new ConcurrentLinkedQueue<Thing>();
	}

	public GameView(Game game, boolean useOpenGL, JPanel overlay) {
		this.overlayPanel = overlay;
		state = new GameViewState();
		vanillaDrawer = new VanillaDrawer(game, state);
		glDrawer = new GLDrawer(game, state);
		panel = new FillingLayeredPane() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(currentActiveDrawer == glDrawer) {
					// for some reason GLCanvas doesnt get repainted so do it manually here
					drawingCanvas.paint(g);
				}
			}
		};
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.black);
		
		glDrawer.getDrawingCanvas().setFocusable(false);
		vanillaDrawer.getDrawingCanvas().setFocusable(false);

		switch3d(useOpenGL);
		
		this.game = game;
		this.guiController = game.getGUIController();
		state.viewOffset = new Position(0, 0);

		MouseWheelListener mouseWheelListener = new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// +1 is in -1 is out
				currentActiveDrawer.zoomView(e.getWheelRotation(), e.getPoint().x, e.getPoint().y);
			}
		};
		MouseMotionListener mouseMotionListener = new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point currentMouse = e.getPoint();
				
				if(state.fpMode && is3d()) {
					int dx = state.previousMouse.x - currentMouse.x;
					int dy = state.previousMouse.y - currentMouse.y;
					currentActiveDrawer.rotateView(dx, dy);
					currentMouse = new Point(drawingCanvas.getWidth()/2,
							drawingCanvas.getHeight()/2);
					fpsMouseRobot.mouseMove(drawingCanvas.getLocationOnScreen().x + currentMouse.x, 
							drawingCanvas.getLocationOnScreen().y + currentMouse.y);
				}
				mouseOver(currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize));
				state.previousMouse = currentMouse;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if(isFirstPerson()) {
					return;
				}
				Point currentMouse = e.getPoint();
				int dx = state.previousMouse.x - currentMouse.x;
				int dy = state.previousMouse.y - currentMouse.y;
				// Only drag if moved mouse at least 10 pixels away
				if (state.draggingMouse || Math.abs(dx) + Math.abs(dy) >= 10) {
					state.draggingMouse = true;
					if (rightMouseDown) {
						currentActiveDrawer.shiftView(dx, dy);
					}
					else if(state.middleMouseDown) {
						currentActiveDrawer.rotateView(dx, dy);
					}
					if (state.leftMouseDown) {
						state.boxSelect[1] = currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize);
					}
					state.previousMouse = currentMouse;
				}
				mouseOver(currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize));
			}
		};
		MouseListener mouseListener = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Point currentMouse = e.getPoint();
				if (!state.draggingMouse) {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClick(currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize), shiftDown);
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						leftClick(currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize), shiftDown);
					}
				} else {
					if (SwingUtilities.isLeftMouseButton(e)) {
						state.boxSelect[0] = currentActiveDrawer.getWorldCoordOfPixel(state.mousePressLocation, state.viewOffset, state.tileSize);
						state.boxSelect[1] = currentActiveDrawer.getWorldCoordOfPixel(currentMouse, state.viewOffset, state.tileSize);
						state.boxSelect = Utils.normalizeRectangle(state.boxSelect[0], state.boxSelect[1]);
						selectInBox(state.boxSelect[0], state.boxSelect[1], shiftDown);
					}
				}
				state.draggingMouse = false;
				state.previousMouse = currentMouse;
				if (SwingUtilities.isLeftMouseButton(e)) {
					state.mousePressLocation = null;
					state.leftMouseDown = false;
				} else if (SwingUtilities.isRightMouseButton(e)) {
					rightMouseDown = false;
				} else if (SwingUtilities.isMiddleMouseButton(e)) {
					state.middleMouseDown = false;
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				state.previousMouse = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e)) {
					state.leftMouseDown = true;
					state.mousePressLocation = e.getPoint();
					state.boxSelect[0] = currentActiveDrawer.getWorldCoordOfPixel(state.mousePressLocation, state.viewOffset, state.tileSize);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					rightMouseDown = true;
				} else if (SwingUtilities.isMiddleMouseButton(e)) {
					state.middleMouseDown = true;
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				state.previousMouse = e.getPoint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				state.previousMouse = e.getPoint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				state.previousMouse = e.getPoint();
			}
		};
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				pressedKeys[e.getKeyCode()] = false;
				if(isFirstPerson()) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						switchFirstPerson(false);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					controlDown = false;
				} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftDown = false;
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					recenterCamera();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				pressedKeys[e.getKeyCode()] = true;
				if(!isFirstPerson()) {
					if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
						controlDown = true;
					} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
						shiftDown = true;
					} else if (e.getKeyCode() == KeyEvent.VK_A) {
						if (e.isControlDown()) {
							selectAllUnits();
						} else {
							state.leftClickAction = LeftClickAction.ATTACK;
						}
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						deselectEverything();
					} else if (e.getKeyCode() == KeyEvent.VK_S) {
						unitStop();
					} else if (e.getKeyCode() == KeyEvent.VK_G) {
						toggleGuarding();
					} else if (e.getKeyCode() == KeyEvent.VK_M) {
						setBuildingToPlan(Game.buildingTypeMap.get("MINE"));
					} else if (e.getKeyCode() == KeyEvent.VK_I) {
						setBuildingToPlan(Game.buildingTypeMap.get("IRRIGATION"));
					} else if (e.getKeyCode() == KeyEvent.VK_W) {
						setBuildingToPlan(Game.buildingTypeMap.get("WALL_WOOD"));
					} else if (e.getKeyCode() == KeyEvent.VK_B) {
						setBuildingToPlan(Game.buildingTypeMap.get("BARRACKS"));
					}
				}
			}
		};
//		if(useOpenGL) {
			glDrawer.getDrawingCanvas().addMouseWheelListener(mouseWheelListener);
			glDrawer.getDrawingCanvas().addMouseMotionListener(mouseMotionListener);
			glDrawer.getDrawingCanvas().addMouseListener(mouseListener);
//		}
//		else {
			vanillaDrawer.getDrawingCanvas().addMouseWheelListener(mouseWheelListener);
			vanillaDrawer.getDrawingCanvas().addMouseMotionListener(mouseMotionListener);
			vanillaDrawer.getDrawingCanvas().addMouseListener(mouseListener);
//		}
		panel.addKeyListener(keyListener);
		fpsMovementThread = new Thread(() -> {
			try {
				fpsMouseRobot = new Robot();
			} catch (AWTException e2) {
				e2.printStackTrace();
			}
			switchFirstPerson(true);
			try {
				while(true) {
					if(isFirstPerson()) {
						if(pressedKeys[KeyEvent.VK_W]) {
							currentActiveDrawer.shiftView(0, 1);
						}
						if(pressedKeys[KeyEvent.VK_S]) {
							currentActiveDrawer.shiftView(0, -1);
						}
						if(pressedKeys[KeyEvent.VK_A]) {
							currentActiveDrawer.shiftView(-1, 0);
						}
						if(pressedKeys[KeyEvent.VK_D]) {
							currentActiveDrawer.shiftView(1, 0);
						}
					}
					Thread.sleep(33);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		fpsMovementThread.start();
	}
	
	public void switch3d(boolean activate3D) {
		if(activate3D) {
			currentActiveDrawer = glDrawer;
		}
		else {
			currentActiveDrawer = vanillaDrawer;
		}
		if(drawingCanvas != null) {
			panel.remove(drawingCanvas);
		}
		panel.remove(overlayPanel);
		drawingCanvas = currentActiveDrawer.getDrawingCanvas();
		if(!activate3D) {
			panel.add(overlayPanel);
		}
		panel.add(drawingCanvas);
	}
	public boolean is3d() {
		return currentActiveDrawer == glDrawer;
	}
	
	public void switchFirstPerson(boolean enabled) {
		state.fpMode = enabled;
		if(state.fpMode) {
			glDrawer.getDrawingCanvas().setCursor(BLANK_CURSOR);
		}
		else {
			glDrawer.getDrawingCanvas().setCursor(Cursor.getDefaultCursor());
		}
	}
	
	public boolean isFirstPerson() {
		return is3d() && state.fpMode;
	}

	public void setFaction(Faction faction) {
		System.out.println("setting faction to " + faction);
		this.state.faction = faction;
	}

	public Faction getFaction() {
		return state.faction;
	}

	private void unitStop() {
		for (Thing thing : state.selectedThings) {
			if (thing instanceof Unit) {
				commandInterface.stop((Unit) thing);
			}
		}
	}

	public void toggleAutoBuild() {
		game.toggleAutoBuild(state.selectedThings);
	}

	public void toggleGuarding() {
		boolean foundNotGuarding = false;
		for (Thing thing : state.selectedThings) {
			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if (!unit.isGuarding()) {
					foundNotGuarding = true;
				}
			}
		}
		for (Thing thing : state.selectedThings) {
			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				commandInterface.setGuarding(unit, foundNotGuarding);
			}
		}
	}

	public void setDrawDebugStrings(boolean enabled) {
		state.drawDebugStrings = enabled;
	}

	public boolean getDrawDebugStrings() {
		return state.drawDebugStrings;
	}

	private void selectInBox(Position topLeft, Position botRight, boolean shiftDown) {
		if (game.world == null) {
			return;
		}
		toggleSelectionForTiles(Utils.getTilesBetween(game.world, topLeft, botRight), shiftDown, controlDown);
	}

	private void leftClick(Position tilepos, boolean shiftDown) {
		if (game.world == null) {
			return;
		}
		Tile tile = game.world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		if (tile == null) {
			return;
		}
		if (state.leftClickAction == LeftClickAction.WEATHER) {
			game.spawnWeather(tile, 3);
		} else if (state.leftClickAction == LeftClickAction.PRESSURE) {
			game.increasePressure(tile, 3);
		} else if (state.leftClickAction == LeftClickAction.SET_TERRITORY) {
			game.setTerritory(tile, 2, state.faction);
		}

		// spawning unit or building
		else if (state.leftClickAction == LeftClickAction.SPAWN_THING) {
			Thing summoned = game.summonThing(tile, state.selectedThingToSpawn,
					summonPlayerControlled ? state.faction : game.world.getFaction(World.NO_FACTION_ID));
			if (summoned != null && summoned.getFaction() == state.faction) {
				if (!shiftDown) {
					deselectEverything();
				}
				selectThing(summoned);
			}
			if (state.selectedThingToSpawn instanceof Plant) {
				if (!shiftDown) {
					deselectEverything();
				}
				summoned = game.summonThing(tile, state.selectedThingToSpawn, game.world.getFaction(World.NO_FACTION_ID));

				selectThing(summoned);
			}
		}
		// planning building
		else if (state.leftClickAction == LeftClickAction.PLAN_BUILDING) {
			Building plannedBuilding = null;
			for (Thing thing : state.selectedThings) {
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					plannedBuilding = commandInterface.planBuilding(unit, tile, !shiftDown, state.selectedBuildingToPlan);
				}
			}
			if (plannedBuilding != null) {
				if (plannedBuilding.getFaction() == state.faction) {
					HashSet<Tile> buildingVision = game.world.getNeighborsInRadius(plannedBuilding.getTile(),
							plannedBuilding.getType().getVisionRadius());
					for (Tile invision : buildingVision) {
						invision.setInVisionRange(true);
					}
				}
			}
		}
		// if a-click and the tile has a building or unit
		else if (state.leftClickAction == LeftClickAction.ATTACK) {
			attackCommand(state.selectedThings, tile, shiftDown, true);
		}
		// select units on tile
		else {
			toggleSelectionForTiles(Arrays.asList(tile), shiftDown, controlDown);
		}

		if (!shiftDown) {
			state.leftClickAction = LeftClickAction.NONE;
		}
	}

	private void attackCommand(ConcurrentLinkedQueue<Thing> selectedThings, Tile tile, boolean shiftEnabled,
			boolean forceAttack) {
		for (Thing thing : selectedThings) {
			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				Thing targetThing = null;
				for (Unit tempUnit : tile.getUnits()) {
					if (tempUnit == unit) {
						continue;
					}
					targetThing = tempUnit;
					break;
				}
				if (targetThing == null) {
					targetThing = tile.getBuilding();
				}
				if (targetThing == null) {
					targetThing = tile.getRoad();
				}
				if (targetThing != null) {
					commandInterface.attackThing(unit, targetThing, !shiftEnabled);
				}
			}
		}
	}

	private void rightClick(Position tilepos, boolean shiftDown) {
		Tile targetTile = game.world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		if (targetTile == null) {
			return;
		}
		if (state.leftClickAction != LeftClickAction.NONE) {
			state.leftClickAction = LeftClickAction.NONE;
			return;
		}

		for (Thing thing : state.selectedThings) {
			if (thing instanceof Building) {
				commandInterface.setBuildingRallyPoint((Building) thing, targetTile);
			} else if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if (!shiftDown) {
					unit.clearPlannedActions();
				}
				if(unit.getType().isCaravan()) {
					Building targetBuilding = targetTile.getBuilding();
					if(targetBuilding != null && targetBuilding.getFaction() == unit.getFaction()
							&& targetBuilding.isBuilt() && targetBuilding.getType().isCastle()) {
						commandInterface.deliver(unit, targetBuilding, !shiftDown);
					}
					else if (targetBuilding != null && targetBuilding.getFaction() == unit.getFaction()
							&& targetBuilding.isBuilt() && targetBuilding.getInventory().isEmpty() == false) {
						commandInterface.takeItems(unit, targetBuilding, !shiftDown);
					}else {
						commandInterface.moveTo(unit, targetTile, !shiftDown);
					}
				}
				else if (unit.getType().isBuilder()) {
					Building targetBuilding = targetTile.getBuilding();
					if (targetTile.getResource() != null) {

					}
					if (targetBuilding == null) {
						targetBuilding = targetTile.getRoad();
					}
					if (targetBuilding != null && targetBuilding.getFaction() == unit.getFaction()
							&& targetBuilding.isBuilt() && targetBuilding.getType().isHarvestable()) {
						commandInterface.harvestThing(unit, targetBuilding, !shiftDown);
					} else if (targetBuilding != null
							&& (targetBuilding.getFaction() == unit.getFaction() || targetBuilding.getType().isRoad())
							&& !targetBuilding.isBuilt()) {
						commandInterface.buildThing(unit, targetBuilding.getTile(), targetBuilding.getType().isRoad(),
								!shiftDown);
					} else if (targetTile.getPlant() != null && targetTile.getPlant().isDead() == false) {
						commandInterface.harvestThing(unit, targetTile.getPlant(), !shiftDown);
					} else {
						commandInterface.moveTo(unit, targetTile, !shiftDown);
					}

				} else {
					Thing targetThing = null;
					for (Unit tempUnit : targetTile.getUnits()) {
						if (tempUnit == unit) {
							continue;
						}
						if (tempUnit.getFaction() != unit.getFaction()) {
							targetThing = tempUnit;
						}
					}
					if (targetThing == null && targetTile.getBuilding() != null
							&& (targetTile.getBuilding().getFaction() != unit.getFaction())) {
						targetThing = targetTile.getBuilding();
					}
					if (targetThing != null) {
						commandInterface.attackThing(unit, targetThing, !shiftDown);
					} else {
						commandInterface.moveTo(unit, targetTile, !shiftDown);
					}
				}
			}
		}
	}

	public void tryToBuildUnit(UnitType u) {
		for (Thing thing : state.selectedThings) {
			if (thing instanceof Building) {
				Building building = (Building) thing;
				for (String ut : building.getType().unitsCanProduce()) {
					if (u == Game.unitTypeMap.get(ut)) {
						commandInterface.produceUnit(building, u);
					}
				}
			}
		}
	}

	public void workerRoad(BuildingType type) {
		for (Thing thing : state.selectedThings) {
			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if (unit.getType().isBuilder()) {
					for (Tile tile : Utils.getTilesInRadius(unit.getTile(), game.world, 4)) {
						if (tile.getFaction() != unit.getFaction()) {
							continue;
						}
						commandInterface.planBuilding(unit, tile, false, type);
					}
				}
			}
		}
	}

	public void setThingToSpawn(Object thingType) {
		state.leftClickAction = LeftClickAction.SPAWN_THING;
		state.selectedThingToSpawn = thingType;
	}
	
	public void increasePressure(boolean raising) {
		state.leftClickAction = LeftClickAction.PRESSURE;
	}
	public void setWeather(boolean raising) {
		state.leftClickAction = LeftClickAction.WEATHER;
	}

	public void setSetTerritory(boolean setting) {
		state.leftClickAction = LeftClickAction.SET_TERRITORY;
	}

	public void setSummonPlayerControlled(boolean playerControlled) {
		summonPlayerControlled = playerControlled;
	}
	public void setSpawnWeather(boolean weather) {
		setSpawnWeather = weather;
	}

	public void setBuildingToPlan(BuildingType buildingType) {
		state.leftClickAction = LeftClickAction.PLAN_BUILDING;
		state.selectedBuildingToPlan = buildingType;
	}

	private void selectAllUnits() {
		for (Unit unit : game.world.getUnits()) {
			if (unit.getFaction() == state.faction) {
				selectThing(unit);
			}
		}
	}

	private void toggleSelectionForTiles(List<Tile> tiles, boolean shiftEnabled, boolean controlEnabled) {

		// deselects everything if shift or control isnt enabled
		if (shiftEnabled == false && !controlEnabled) {
			deselectEverything();
		}
		boolean selectedAUnit = false;
		for (Tile tile : tiles) {
			// goes through all the units on the tile and checks if they are selected
			for (Unit candidate : tile.getUnits()) {
				// clicking on tile w/o shift i.e only selects top unit
				if (candidate.getFaction() == state.faction) {
					selectThing(candidate);
					selectedAUnit = true;
					// shift enabled -> selects whole stack
					// shift disabled -> selects top unit
					if (!shiftEnabled && tiles.size() == 1) {
						break;
					}
				}
			}
		}
		if (!selectedAUnit) {
			for (Tile tile : tiles) {
				// selects the building on the tile
				Building building = tile.getBuilding();
				if (building != null && building.getFaction() == state.faction && tile.getUnitOfFaction(state.faction) == null) {
					selectThing(building);
				}
			}
		}
	}

	private void selectThing(Thing thing) {
		thing.setSelected(true);
		state.selectedThings.add(thing);
		if (thing instanceof Unit) {
			guiController.selectedUnit((Unit) thing, true);
		} else if (thing instanceof Building) {
			guiController.selectedBuilding((Building) thing, true);
		} else if (thing instanceof Plant) {
			guiController.selectedPlant((Plant) thing, true);
		}
	}

	public void deselectEverything() {
		for (Thing thing : state.selectedThings) {
			if (thing != null) {
				thing.setSelected(false);

				if (thing instanceof Unit) {
					guiController.selectedUnit((Unit) thing, false);
				}
				if (thing instanceof Building) {
					guiController.selectedBuilding((Building) thing, false);
				}
				if (thing instanceof Plant) {
					guiController.selectedPlant((Plant) thing, false);
				}

			}
			state.selectedThings.remove(thing);
		}
		state.selectedThings.clear();
		state.leftClickAction = LeftClickAction.NONE;
	}

	public void pressedSelectedUnitPortrait(Unit unit) {
		if (controlDown) {
			deselectOneThing(unit);
		} else {
			deselectOtherThings(unit);
		}
	}

	private void deselectOneThing(Thing deselect) {
		state.selectedThings.remove(deselect);
		deselect.setSelected(false);
		if (deselect instanceof Unit) {
			guiController.selectedUnit((Unit) deselect, false);
		}
	}

	private void deselectOtherThings(Thing keep) {
		for (Thing thing : state.selectedThings) {
			thing.setSelected(false);
			if (thing instanceof Unit) {
				guiController.selectedUnit((Unit) thing, false);
			}
		}
		state.selectedThings.clear();
		selectThing(keep);
	}

	public void updateTerrainImages() {
		currentActiveDrawer.updateTerrainImages();
	}
	
	public void setMapMode(MapMode mode) {
		state.mapMode = mode;
	}

	private void mouseOver(Position tilepos) {
		state.hoveredTile = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
	}

	public void moveViewTo(double ratiox, double ratioy, int panelWidth, int panelHeight) {
		Position tile = new Position(ratiox * game.world.getWidth(), ratioy * game.world.getHeight());
		Position pixel = tile.multiply(state.tileSize).subtract(new Position(panelWidth / 2, panelHeight / 2));
		state.viewOffset = pixel;
		panel.repaint();
	}
	public void moveViewTo(TileLoc tileloc, int panelWidth, int panelHeight) {
		moveViewTo(
				(double)tileloc.x() / game.world.getWidth(), 
				(double)tileloc.y() / game.world.getHeight(), 
				panelWidth, panelHeight);
	}
	public void recenterCamera() {
		// move camera to first building or first unit.
		for(Building building : state.faction.getBuildings()) {
			GameView.this.moveViewTo(building.getTile().getLocation(), panel.getWidth(), panel.getHeight());
			return;
		}
		for(Unit unit : state.faction.getUnits()) {
			GameView.this.moveViewTo(unit.getTile().getLocation(), panel.getWidth(), panel.getHeight());
			return;
		}
	}

	public CommandInterface getCommandInterface() {
		return commandInterface;
	}

	public void setCommandInterface(CommandInterface commandInterface) {
		this.commandInterface = commandInterface;
	}

	public Game getGameInstance() {
		return game;
	}
	
	public int getWidth() {
		return panel.getWidth();
	}
	public int getHeight() {
		return panel.getHeight();
	}
	public FillingLayeredPane getPanel() {
		return panel;
	}
	
	public void requestFocus() {
		panel.requestFocus();
	}
	
	public Drawer getDrawer() {
		return currentActiveDrawer;
	}
	
	public void setPreviousTickTime(long time) {
		state.previousTickTime = time;
	}
}
