package networking.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import world.*;

public class GameView extends JPanel {

	public static final int FAST_MODE_TILE_SIZE = 10;
	public static final int NUM_DEBUG_DIGITS = 3;

	private static final Image RALLY_POINT_IMAGE = Utils.loadImage("resources/Images/interfaces/queuelocation.png");
	private static final Image TARGET_IMAGE = Utils.loadImage("resources/Images/interfaces/ivegotyouinmysights.png");
	private static final Image FLAG = Utils.loadImage("resources/Images/interfaces/flag.png");
	private static final Image BUILD_ICON = Utils.loadImage("resources/Images/interfaces/building.png");
	private static final Image HARVEST_ICON = Utils.loadImage("resources/Images/interfaces/harvest.png");
	private static final Image GUARD_ICON = Utils.loadImage("resources/Images/interfaces/guard.png");
	private static final Image AUTO_BUILD_ICON = Utils.loadImage("resources/Images/interfaces/autobuild.png");
	private static final Image RED_HITSPLAT = Utils.loadImage("resources/Images/interfaces/redhitsplat.png");
	private static final Image BLUE_HITSPLAT = Utils.loadImage("resources/Images/interfaces/bluehitsplat.png");
	private static final Image GREEN_HITSPLAT = Utils.loadImage("resources/Images/interfaces/greenhitsplat.png");
	
	private static final Font DAMAGE_FONT = new Font("Comic Sans MS", Font.BOLD, 14);
	
	private GUIController guiController;
	private CommandInterface commandInterface;

	private volatile BufferedImage terrainImage;
	private volatile BufferedImage minimapImage;
	private volatile BufferedImage heightMapImage;
	
	private Game game;
	private Position viewOffset;
	private Point previousMouse;
	private boolean showHeightMap = false;
	private boolean draggingMouse = false;
	private boolean drawDebugStrings = false;
	private TileLoc hoveredTile = new TileLoc(-1,-1);
	private int tileSize = 9;
	private boolean controlDown = false;
	private boolean shiftDown = false;

	private ConcurrentLinkedQueue<Thing> selectedThings = new ConcurrentLinkedQueue<Thing>();
	private Faction faction = Faction.getTempFaction();
	
	private LeftClickAction leftClickAction = LeftClickAction.NONE;
	private HasImage selectedThingToSpawn;
	private boolean summonPlayerControlled = true;
	private BuildingType selectedBuildingToPlan;
	
	public GameView(Game game) {
		this.game = game;
		this.guiController = game.getGUIController();
		this.setBackground(Color.black);
		viewOffset = new Position(0, 0);
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// +1 is in -1 is out
				zoomView(e.getWheelRotation(), e.getPoint().x, e.getPoint().y);
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseOver(getTileAtPixel(e.getPoint()));
				repaint();
				previousMouse = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Point currentMouse = e.getPoint();
				int dx = previousMouse.x - currentMouse.x;
				int dy = previousMouse.y - currentMouse.y;
				// Only drag if moved mouse at least 3 pixels away
				if(draggingMouse || Math.abs(dx) + Math.abs(dy) >= 5) {
					draggingMouse = true;
					if (SwingUtilities.isLeftMouseButton(e)) {
						shiftView(dx, dy);
					}
					mouseOver(getTileAtPixel(currentMouse));
					previousMouse = currentMouse;
				}
			}
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Point currentMouse = e.getPoint();
				if(!draggingMouse) {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClick(getTileAtPixel(currentMouse), shiftDown);
					}
					else if (SwingUtilities.isLeftMouseButton(e)) {
						leftClick(getTileAtPixel(currentMouse), shiftDown);
					}
				}
				draggingMouse = false;
				previousMouse = e.getPoint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				previousMouse = e.getPoint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				previousMouse = e.getPoint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				previousMouse = e.getPoint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				previousMouse = e.getPoint();
			}
		});

		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
					controlDown = false;
				}
				else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftDown = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
					controlDown = true;
				}
				else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					shiftDown = true;
				}
				else if(e.getKeyCode() == KeyEvent.VK_A) {
					if(e.isControlDown()) {
						selectAllUnits();
					}
					else {
						leftClickAction = LeftClickAction.ATTACK;
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					deselectEverything();
				}
				else if (e.getKeyCode() == KeyEvent.VK_S) {
					unitStop();
				}
				else if(e.getKeyCode() == KeyEvent.VK_M) {
					setBuildingToPlan(Game.buildingTypeMap.get("MINE"));
				}
				else if(e.getKeyCode() == KeyEvent.VK_I) {
					setBuildingToPlan(Game.buildingTypeMap.get("IRRIGATION"));
				}
				else if(e.getKeyCode() == KeyEvent.VK_W) {
					setBuildingToPlan(Game.buildingTypeMap.get("SAWMILL"));
				}
				else if(e.getKeyCode() == KeyEvent.VK_B) {
					setBuildingToPlan(Game.buildingTypeMap.get("BARRACKS"));
				}
			}
		});
	}
	public void setFaction(Faction faction) {
		System.out.println("setting faction to " + faction);
		this.faction = faction;
	}
	public Faction getFaction() {
		return faction;
	}
	public void unitStop() {
		for (Thing thing : selectedThings) {
			if (thing instanceof Unit) {
				commandInterface.stop((Unit)thing);
			}
		}
	}
	public void toggleAutoBuild() {
		game.toggleAutoBuild(selectedThings);
	}
	public void setHarvesting() {
		game.setHarvesting(selectedThings);
	}
	public void toggleGuarding() {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				commandInterface.setGuarding(unit, !unit.isGuarding());
//				unit.setGuarding(!unit.isGuarding());
			}
		}
	}
	
	public void setDrawDebugStrings(boolean enabled) {
		drawDebugStrings = enabled;
	}
	public boolean getDrawDebugStrings() {
		return drawDebugStrings;
	}

	public void leftClick(Position tilepos, boolean shiftDown) {
		if(game.world == null) {
			return;
		}
		Tile tile = game.world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		if(tile == null) {
			return;
		}
		if(leftClickAction == LeftClickAction.RAISE_TERRAIN) {
			game.raiseTerrain(tile, 5);
		}
		else if(leftClickAction == LeftClickAction.SET_TERRITORY) {
			game.setTerritory(tile, 5, faction);
		}
		
		// spawning unit or building
		else if(leftClickAction == LeftClickAction.SPAWN_THING) {
			Thing summoned = game.summonThing(tile, selectedThingToSpawn, summonPlayerControlled ? faction : game.world.getFaction(World.NO_FACTION_ID));
			if(summoned.getFaction() == faction) {
				if(!shiftDown) {
					deselectEverything();
				}
				selectThing(summoned);
			}
		}
		//planning building
		else if (leftClickAction == LeftClickAction.PLAN_BUILDING) {
			Building plannedBuilding = null;
			for(Thing thing : selectedThings) {
				if(thing instanceof Unit) {
					Unit unit = (Unit) thing;
					plannedBuilding = commandInterface.planBuilding(unit, tile, !shiftDown, selectedBuildingToPlan);
				}
			}
			if(plannedBuilding != null) {
				if(plannedBuilding.getFaction() == faction) {
					HashSet<Tile> buildingVision = game.world.getNeighborsInRadius(plannedBuilding.getTile(), plannedBuilding.getType().getVisionRadius());
					for(Tile invision : buildingVision) {
						invision.setInVisionRange(true);
					}
				}
			}
		}
		//if a-click and the tile has a building or unit
		else if(leftClickAction == LeftClickAction.ATTACK) {
			attackCommand(selectedThings, tile, shiftDown, true);
		}
		//select units on tile
		else {
			toggleSelectionOnTile(tile, shiftDown, controlDown);
		}
		
		if(!shiftDown) {
			leftClickAction = LeftClickAction.NONE;
		}
	}

	public void attackCommand(ConcurrentLinkedQueue<Thing> selectedThings, Tile tile, boolean shiftEnabled, boolean forceAttack) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				Thing targetThing = null;
				for(Unit tempUnit : tile.getUnits()) {
					if(tempUnit == unit) {
						continue;
					}
					targetThing = tempUnit;
					break;
				}
				if(targetThing == null) {
					targetThing = tile.getBuilding();
				}
				if(targetThing == null) {
					targetThing = tile.getRoad();
				}
				if(targetThing != null) {
					commandInterface.attackThing(unit, targetThing, !shiftEnabled);
				}
			}
		}
	}

	public void rightClick(Position tilepos, boolean shiftDown) {
		Tile targetTile = game.world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		if(targetTile == null) {
			return;
		}
		if(leftClickAction != LeftClickAction.NONE) {
			leftClickAction = LeftClickAction.NONE;
			return;
		}

		for(Thing thing : selectedThings) {
			if(thing instanceof Building) {
				commandInterface.setBuildingRallyPoint((Building) thing, targetTile);
			}
			else if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if(!shiftDown) {
					unit.clearPlannedActions();
				}
				if(unit.getType().isBuilder()) {
					Building targetBuilding = targetTile.getBuilding();
					if(targetBuilding == null) {
						targetBuilding = targetTile.getRoad();
					}
					if(targetBuilding != null && (targetBuilding.getFaction() == unit.getFaction() || targetBuilding.getType().isRoad()) && !targetBuilding.isBuilt()) {
						commandInterface.buildThing(unit, targetBuilding, !shiftDown);
					}
					else {
						commandInterface.moveTo(unit, targetTile, !shiftDown);
					}
				}
				else {
					Thing targetThing = null;
					for(Unit tempUnit : targetTile.getUnits()) {
						if(tempUnit == unit) {
							continue;
						}
						if(tempUnit.getFaction() != unit.getFaction()) {
							targetThing = tempUnit;
						}
					}
					if(targetThing == null && targetTile.getBuilding() != null
							&& (targetTile.getBuilding().getFaction() != unit.getFaction())) {
						targetThing = targetTile.getBuilding();
					}
					if(targetThing != null) {
						commandInterface.attackThing(unit, targetThing, !shiftDown);
					}
					else {
						commandInterface.moveTo(unit, targetTile, !shiftDown);
					}
				}
			}
		}
	}
	
	public void tryToBuildUnit(UnitType u) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Building ) {
				Building building = (Building)thing;
				for(String ut : building.getType().unitsCanBuild()) {
					if(u == Game.unitTypeMap.get(ut)) {
						commandInterface.produceUnit(building, u);
					}
				}
			}
		}
	}
	
	public void workerRoad(BuildingType type) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					for(Tile tile : Utils.getTilesInRadius(unit.getTile(), game.world, 4)) {
						if(tile.getFaction() != unit.getFaction()) {
							continue;
						}
						commandInterface.planBuilding(unit, tile, false, type);
					}
				}
			}
		}
	}

	public void setThingToSpawn(HasImage thingType) {
		leftClickAction = LeftClickAction.SPAWN_THING;
		selectedThingToSpawn = thingType;
	}
	
	public void setRaisingTerrain(boolean raising) {
		leftClickAction = LeftClickAction.RAISE_TERRAIN;
	}
	public void setSetTerritory(boolean setting) {
		leftClickAction = LeftClickAction.SET_TERRITORY;
	}
	
	public void setSummonPlayerControlled(boolean playerControlled) {
		summonPlayerControlled = playerControlled;
	}
	
	public void setBuildingToPlan(BuildingType buildingType) {
		leftClickAction = LeftClickAction.PLAN_BUILDING;
		selectedBuildingToPlan = buildingType;
	}

	public void selectAllUnits() {
		for(Unit unit : game.world.getUnits()) {
			if(unit.getFaction() == faction) {
				selectThing(unit);
			}
		}
	}
	
	public void toggleSelectionOnTile(Tile tile, boolean shiftEnabled, boolean controlEnabled) {
		
		//deselects everything if shift or control isnt enabled
		if (shiftEnabled == false && !controlEnabled) {
			deselectEverything();
		}
		
		//selects the building on the tile
		Building building = tile.getBuilding();
		if(building != null && building.getFaction() == faction && tile.getUnitOfFaction(faction) == null) {
			selectThing(building);
		}
		//goes through all the units on the tile and checks if they are selected
		for(Unit candidate : tile.getUnits()) {
			// clicking on tile w/o shift i.e only selects top unit
			if (candidate.getFaction() == faction) {
				selectThing(candidate);
				//shift enabled -> selects whole stack
				//shift disabled -> selects top unit
				if (!shiftEnabled) {
					break;
				}
			}
		}
	}
	
	public void selectThing(Thing thing) {
		thing.setIsSelected(true);
		selectedThings.add(thing);
		if(thing instanceof Unit) {
			guiController.selectedUnit((Unit)thing, true);
		}
		else if(thing instanceof Building) {
			guiController.selectedBuilding((Building)thing, true);
		}
	}
	
	public void deselectEverything() {
		for (Thing thing : selectedThings) {
			if (thing != null) {
				thing.setIsSelected(false);
				
				if (thing instanceof Unit) {
					guiController.selectedUnit((Unit) thing, false);
				}
				if (thing instanceof Building) {
					guiController.selectedBuilding((Building) thing, false);
				}

			}
			selectedThings.remove(thing);
		}
		selectedThings.clear();
		leftClickAction = LeftClickAction.NONE;
	}
	
	public void pressedSelectedUnitPortrait(Unit unit) {
		if(controlDown) {
			deselectOneThing(unit);
		}
		else {
			deselectOtherThings(unit);
		}
	}

	public void deselectOneThing(Thing deselect) {
		selectedThings.remove(deselect);
		deselect.setIsSelected(false);
		if(deselect instanceof Unit) {
			guiController.selectedUnit((Unit)deselect, false);
		}
	}
	
	public void deselectOtherThings(Thing keep) {
		for (Thing thing : selectedThings) {
			thing.setIsSelected(false);
			if(thing instanceof Unit) {
				guiController.selectedUnit((Unit)thing, false);
			}
		}
		selectedThings.clear();
		selectThing(keep);
	}

	public void updateTerrainImages() {
		if(game.world != null) {
			BufferedImage[] images = game.world.createTerrainImage(faction);
			this.terrainImage = images[0];
			this.minimapImage = images[1];
			this.heightMapImage = images[2];
		}
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}

	public void mouseOver(Position tilepos) {
		hoveredTile = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
	}
	
	public void centerViewOn(Tile tile, int zoom, int panelWidth, int panelHeight) {
		tileSize = zoom;
		viewOffset.x = (tile.getLocation().x() - panelWidth/2/tileSize) * tileSize + tileSize/2;
		viewOffset.y = (tile.getLocation().y() - panelHeight/2/tileSize) * tileSize;
		repaint();
	}

	public void zoomView(int scroll, int mx, int my) {
		int newTileSize;
		if(scroll > 0) {
			newTileSize = (int) ((tileSize - 1) * 0.95);
		}
		else {
			newTileSize = (int) ((tileSize + 1) * 1.05);
		}
		zoomViewTo(newTileSize, mx, my);
	}
	
	public void zoomViewTo(int newTileSize, int mx, int my) {
		if (newTileSize > 0) {
			Position tile = getTileAtPixel(new Position(mx, my));
			tileSize = newTileSize;
			Position focalPoint = tile.multiply(tileSize).subtract(viewOffset);
			viewOffset.x -= mx - focalPoint.x;
			viewOffset.y -= my - focalPoint.y;
		}
		repaint();
	}
	
	public void shiftView(int dx, int dy) {
		viewOffset.x += dx;
		viewOffset.y += dy;
		repaint();
	}
	
	public void moveViewTo(double ratiox, double ratioy, int panelWidth, int panelHeight) {
		Position tile = new Position(ratiox*game.world.getWidth(), ratioy*game.world.getHeight());
		Position pixel = tile.multiply(tileSize).subtract(new Position(panelWidth/2, panelHeight/2));
		viewOffset = pixel;
		repaint();
	}
	
	public Position getTileAtPixel(Position pixel) {
		Position tile = pixel.add(viewOffset).divide(tileSize);
		return tile;
	}
	public Position getTileAtPixel(Point pixel) {
		return new Position((pixel.x + viewOffset.x)/tileSize, (pixel.y + viewOffset.y)/tileSize);
	}
	public Position getPixelForTile(Position tile) {
		return tile.multiply(tileSize).subtract(viewOffset);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(game == null) {
			return;
		}
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setColor(game.getBackgroundColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		drawGame(g, getWidth(), getHeight());
		g.setColor(Color.black);
		g.drawRect(-1, 0, getWidth() + 1, getHeight());
	}

	public void drawGame(Graphics g, int panelWidth, int panelHeight) {
		if(game.world == null) {
			return;
		}
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g, panelWidth, panelHeight, viewOffset);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		if(faction != null && faction.getResearchTarget() != null && !faction.getResearchTarget().isCompleted()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * faction.getResearchTarget().getPointsSpent() / faction.getResearchTarget().getRequiredPoints();
			String progress = String.format(faction.getResearchTarget() + " %d/%d", faction.getResearchTarget().getPointsSpent(), faction.getResearchTarget().getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, panelWidth - panelWidth/3 - 4, 4, panelWidth/3, 30);
		}
		Toolkit.getDefaultToolkit().sync();
	}
	
	public void draw(Graphics g, int panelWidth, int panelHeight, Position viewOffset) {
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, viewOffset.divide(tileSize).getIntX() - 2);
		int lowerY = Math.max(0, viewOffset.divide(tileSize).getIntY() - 2);
		int upperX = Math.min(game.world.getWidth(), lowerX + panelWidth/tileSize + 4);
		int upperY = Math.min(game.world.getHeight(), lowerY + panelHeight/tileSize + 4);
		
		if(tileSize < FAST_MODE_TILE_SIZE) {
			if(showHeightMap) {
				g.drawImage(heightMapImage, 0, 0, tileSize*game.world.getWidth(), tileSize*game.world.getHeight(), null);
			}
			else {
				g.drawImage(terrainImage, 0, 0, tileSize*game.world.getWidth(), tileSize*game.world.getHeight(), null);
			}
		}
		else {
			double highest = 0;
			double lowest = 1;
			if(showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						highest = Math.max(highest, tile.getHeight());
						lowest = Math.min(lowest, tile.getHeight());
					}
				}
			}
			
			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile tile = game.world.get(new TileLoc(i, j));
					if(tile == null)
						continue;
					drawTile(g, tile, lowest, highest);
				}
			}
			
			for(Building building : game.world.getBuildings()) {
				drawHealthBar(g, building);
				drawHitsplat(g, building);
			}
			for(Plant plant : game.world.getPlants()) {
				drawHealthBar(g, plant);
				drawHitsplat(g, plant);
			}
			for(Unit unit : game.world.getUnits()) {
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
			}
			
			for(Projectile p : game.world.getData().getProjectiles()) {
				int extra = (int) (tileSize * p.getExtraSize());
				double ratio = 0.5*p.getHeight() / p.getMaxHeight();
				int shadowOffset = (int) (tileSize*ratio/2);
				g.drawImage(p.getShadow(0), p.getTile().getLocation().x() * tileSize + shadowOffset, p.getTile().getLocation().y() * tileSize + shadowOffset, tileSize - shadowOffset*2, tileSize - shadowOffset*2, null);
				g.drawImage(p.getImage(0), p.getTile().getLocation().x() * tileSize - extra/2, p.getTile().getLocation().y() * tileSize - p.getHeight() - extra/2, tileSize + extra, tileSize + extra, null);
			}
			for(WeatherEvent w : game.world.getWeatherEvents()) {
				g.drawImage(w.getImage(0), w.getTile().getLocation().x() * tileSize, w.getTile().getLocation().y() * tileSize, tileSize, tileSize, null);
			}
			for(Thing thing : selectedThings) {
				// draw selection circle
				g.setColor(Utils.getTransparentColor(faction.color(), 150));
//				Utils.setTransparency(g, 0.8f);
				Graphics2D g2d = (Graphics2D)g;
				Stroke currentStroke = g2d.getStroke();
				int strokeWidth = tileSize/12;
				g2d.setStroke(new BasicStroke(strokeWidth));
				g.drawOval(thing.getTile().getLocation().x() * tileSize + strokeWidth/2, thing.getTile().getLocation().y() * tileSize + strokeWidth/2, tileSize-1 - strokeWidth, tileSize-1 - strokeWidth);
				g2d.setStroke(currentStroke);
//				Utils.setTransparency(g, 1f);

				// draw spawn location for buildings
				if(thing instanceof Building) {
					Building building = (Building) thing;
					if(building.getSpawnLocation() != building.getTile()) {
						g.drawImage(RALLY_POINT_IMAGE, building.getSpawnLocation().getLocation().x() * tileSize, building.getSpawnLocation().getLocation().y() * tileSize, tileSize, tileSize, null);
					}
				}
				
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					// draw attacking target
					Thing target = unit.getTarget();
					if(target != null) {
						drawTarget(g, target.getTile().getLocation());
					}
					// draw path 
					LinkedList<Tile> path = unit.getCurrentPath();
					if(path != null) {
						g.setColor(Color.green);
						TileLoc prev = unit.getTile().getLocation();
						for(Tile t : path) {
							if(prev != null) {
								g.drawLine(prev.x() * tileSize + tileSize/2, prev.y() * tileSize + tileSize/2, 
										t.getLocation().x() * tileSize + tileSize/2, t.getLocation().y() * tileSize + tileSize/2);
							}
							prev = t.getLocation();
						}
					}
					// draw destination flags
					for(PlannedAction plan : unit.actionQueue) {
						Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
						g.drawImage(FLAG, targetTile.getLocation().x() * tileSize, targetTile.getLocation().y() * tileSize, tileSize, tileSize, null);
					}
					int range = unit.getMaxRange();
					if(range > 1) {
						// draws the attack range for units
						for (int i = lowerX; i < upperX; i++) {
							for (int j = lowerY; j < upperY; j++) {
								Tile t = game.world.get(new TileLoc(i, j));
								if (t == null)
									continue;
								int x = t.getLocation().x() * tileSize;
								int y = t.getLocation().y() * tileSize;
								int w = tileSize;
								int h = tileSize;
	
								if (t.getLocation().distanceTo(unit.getTile().getLocation()) <= range) {
									g.setColor(Color.BLACK);
									Utils.setTransparency(g, 0.3f);
	
									for (Tile tile : t.getNeighbors()) {
										if (tile.getLocation().distanceTo(unit.getTile().getLocation()) > range) {
											TileLoc tileLoc = tile.getLocation();
	
											if (tileLoc.x() == t.getLocation().x()) {
												if (tileLoc.y() < t.getLocation().y()) {
													g.fillRect(x, y, w, 5);
												}
												if (tileLoc.y() > t.getLocation().y()) {
													g.fillRect(x, y + h - 5, w, 5);
												}
	
											}
											if (tileLoc.y() == t.getLocation().y()) {
												if (tileLoc.x() < t.getLocation().x()) {
													g.fillRect(x, y, 5, h);
												}
												if (tileLoc.x() > t.getLocation().x()) {
													g.fillRect(x + w - 5, y, 5, h);
												}
											}
	
										}
									}
									Utils.setTransparency(g, 1);
								}
							}
						}
					}
				}
			}

			int indicatorSize = tileSize/12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for(Unit unit : game.world.getUnits()) {
				int count = 0;
				if(visited.containsKey(unit.getTile())) {
					count = visited.get(unit.getTile());
				}
				visited.put(unit.getTile(), count+1);
					
				//draws a square for every player unit on the tile
				int xx = unit.getTile().getLocation().x() * tileSize + offset;
				int yy = unit.getTile().getLocation().y() * tileSize + (indicatorSize + offset)*count + offset;
				g.setColor(unit.getFaction().color());
				g.fillRect(xx, yy, indicatorSize, indicatorSize);
				g.setColor(Color.BLACK);
				g.drawRect(xx, yy, indicatorSize, indicatorSize);
				count++;
			}
			
			
			if(!showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						double brightness = World.getDaylight() + tile.getBrightness(faction);
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int)(255 * (1 - brightness))));
						g.fillRect(i * tileSize, j * tileSize, tileSize, tileSize);
					}
				}
			}
			
			if (leftClickAction == LeftClickAction.PLAN_BUILDING) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedBuildingToPlan.getImage(tileSize));
				g2d.drawImage(bI, hoveredTile.x() * tileSize, hoveredTile.y() * tileSize, tileSize, tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (leftClickAction == LeftClickAction.SPAWN_THING) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedThingToSpawn.getImage(tileSize));
				g2d.drawImage(bI, hoveredTile.x() * tileSize, hoveredTile.y() * tileSize, tileSize, tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			
			if(drawDebugStrings) {
				if(tileSize >= 36) {
					int[][] rows = new int[upperX - lowerX][upperY - lowerY];
					int fontsize = tileSize/4;
					fontsize = Math.min(fontsize, 13);
					Font font = new Font("Consolas", Font.PLAIN, fontsize);
					g.setFont(font);
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile tile = game.world.get(new TileLoc(i, j));
							List<String> strings = new LinkedList<String>();
							strings.add(String.format("H=%." + NUM_DEBUG_DIGITS + "f", tile.getHeight()));
							strings.add(String.format("HUM" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getHumidity()));
							strings.add(String.format("TEMP" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getTempurature()));
							if(tile.getResource() != null) {
								strings.add(String.format("ORE" + "=%d", tile.getResource().getYield()));
							}
							
							
							if(tile.liquidType != LiquidType.DRY) {
								strings.add(String.format(tile.liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount));
							}
							
							
							if(tile.getModifier() != null) {
								strings.add("GM=" + tile.getModifier().timeLeft());
							}
							rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, strings, rows[i-lowerX][j-lowerY], fontsize, tileSize);
							
							for(Unit unit : tile.getUnits()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, unit.getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize);
							}
							if(tile.getPlant() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getPlant().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize);
							}
							if(tile.hasBuilding()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getBuilding().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize);
							}
							if(tile.getRoad() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getRoad().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize);
								
							}
						}
					}
				}
			}
			if(leftClickAction == LeftClickAction.ATTACK) {
				drawTarget(g, hoveredTile);
			}
			g.setColor(new Color(0, 0, 0, 64));
			g.drawRect(hoveredTile.x() * tileSize, hoveredTile.y() * tileSize, tileSize-1, tileSize-1);
			g.drawRect(hoveredTile.x() * tileSize + 1, hoveredTile.y() * tileSize + 1, tileSize - 3, tileSize - 3);
		}
	}

	public void drawTile(Graphics g, Tile theTile, double lowest, double highest) {
		int column = theTile.getLocation().x();
		int row = theTile.getLocation().y();
		int drawx = column * tileSize;
		int drawy = (int) (row * tileSize);
		int draww = tileSize;
		int drawh = tileSize;
		int imagesize = draww < drawh ? draww : drawh;
		
		if(showHeightMap) {
			theTile.drawHeightMap(g, (game.world.get(new TileLoc(column, row)).getHeight() - lowest) / (highest - lowest), tileSize);
		}
		else {
			g.drawImage(theTile.getTerrain().getImage(imagesize), drawx, drawy, draww, drawh, null);
//			t.drawEntities(g, currentMode);
			
			if(theTile.getResource() != null) {
				g.drawImage(theTile.getResource().getType().getImage(imagesize), drawx, drawy, draww, drawh, null);
			}
			
			if(theTile.getFaction() != null && theTile.getFaction() != game.world.getFaction(World.NO_FACTION_ID)) {
//				g.setColor(Color.black);
//				g.fillRect(x, y, w, h); 
				g.setColor(theTile.getFaction().color());
				
				Utils.setTransparency(g, 0.5f);
				for(Tile tile : theTile.getNeighbors()) {
					if(tile.getFaction() != theTile.getFaction()) {
						TileLoc tileLoc = tile.getLocation();
						
						if(tileLoc.x() == theTile.getLocation().x() ) {
							if(tileLoc.y() < theTile.getLocation().y()) {
								g.fillRect(drawx, drawy, draww, 10); 
							}
							if(tileLoc.y() > theTile.getLocation().y()) {
								g.fillRect(drawx, drawy + drawh - 10, draww, 10); 
							}
							
						}
						if(tileLoc.y() == theTile.getLocation().y() ) {
							if(tileLoc.x() < theTile.getLocation().x()) {
								g.fillRect(drawx, drawy, 10, drawh); 
							}
							if(tileLoc.x() > theTile.getLocation().x()) {
								g.fillRect(drawx + draww - 10, drawy, 10, drawh); 
							}
						}
						
					}
				}
				Utils.setTransparency(g, 1);
			}
//			if(game.world.borderTerritory.containsKey(theTile)) {
//				Utils.setTransparency(g, 1);
//				g.setColor(Color.BLACK);
//				g.fillRect(drawx, drawy, draww, drawh); 
//			}
			if (theTile.getRoad() != null) {
				drawBuilding(theTile.getRoad(), g, drawx, drawy, draww, drawh);
			}
			
			if(theTile.liquidType != LiquidType.DRY) {
				double alpha = Utils.getAlphaOfLiquid(theTile.liquidAmount);
//				 transparency liquids
				Utils.setTransparency(g, alpha);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawx, drawy, draww, drawh);
				Utils.setTransparency(g, 1);
				
				int imageSize = (int) Math.min(Math.max(draww*theTile.liquidAmount / 0.2, 1), draww);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawx + draww/2 - imageSize/2, drawy + drawh/2 - imageSize/2, imageSize, imageSize);
				g.drawImage(theTile.liquidType.getImage(imagesize), drawx + draww/2 - imageSize/2, drawy + draww/2 - imageSize/2, imageSize, imageSize, null);
			}
			
			if(theTile.getModifier() != null) {
				Utils.setTransparency(g, 0.9);
				g.drawImage(theTile.getModifier().getType().getImage(imagesize), drawx, drawy, draww, drawh, null);
				Utils.setTransparency(g, 1);
			}
			
			if (!theTile.getItems().isEmpty()) {
				for (Item item : theTile.getItems()) {
					g.drawImage(item.getType().getImage(imagesize), drawx + tileSize/4,
							drawy + tileSize/4, tileSize/2, tileSize/2, null);
				}
			}
			if(theTile.getPlant() != null) {
				Plant p = theTile.getPlant();
				g.drawImage(p.getImage(tileSize), drawx, drawy, draww, drawh, null);
			}
			
			if(theTile.getBuilding() != null) {
				drawBuilding(theTile.getBuilding(), g, drawx, drawy, draww, drawh);
			}
			for(Unit unit : theTile.getUnits()) {
				g.drawImage(unit.getImage(tileSize), drawx, drawy, draww, drawh, null);
				if(unit.getIsHarvesting() == true) {
					g.drawImage(HARVEST_ICON, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.isGuarding() == true) {
					g.drawImage(GUARD_ICON, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.getAutoBuild() == true) {
					g.drawImage(AUTO_BUILD_ICON, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
			}
		}
	}
	public void drawBuilding(Building building, Graphics g, int drawx, int drawy, int draww, int drawh) {
		
		BufferedImage bI = Utils.toBufferedImage(building.getImage(0));
		if(building.isBuilt() == false) {
			//draws the transparent version
			Utils.setTransparency(g, 0.5f);
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(bI, drawx, drawy, draww, drawh, null);
			Utils.setTransparency(g, 1f);
			//draws the partial image
			double percentDone = 1 - building.getRemainingEffort()/building.getType().getBuildingEffort();
			int imageRatio =  Math.max(1, (int) (bI.getHeight() * percentDone));
			int partialHeight = Math.max(1, (int) (tileSize * percentDone));
			bI = bI.getSubimage(0, bI.getHeight() - imageRatio, bI.getWidth(), imageRatio);
			g.drawImage(bI, drawx, drawy - partialHeight + drawh, draww, partialHeight , null);
			g.drawImage(BUILD_ICON, drawx + tileSize/4, drawy + tileSize/4, draww*3/4, drawh*3/4, null);
		}
		else {
			g.drawImage(bI, drawx, drawy, draww, drawh, null);
		}
	}

	public void drawTarget(Graphics g, TileLoc tileLoc) {
		int x = (int) ((tileLoc.x() * tileSize + tileSize*1/10) );
		int y = (int) ((tileLoc.y() * tileSize + tileSize*1/10) );
		int w = (int) (tileSize*8/10);
		int hi = (int)(tileSize*8/10);
		g.drawImage(TARGET_IMAGE, x, y, w, hi, null);
	}
	
	public void drawHealthBar(Graphics g, Thing thing) {
		if( tileSize <= 30) {
			return;
		}
		if(World.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(hoveredTile)) {
			int x = thing.getTile().getLocation().x() * tileSize + 1;
			int y = thing.getTile().getLocation().y() * tileSize + 1;
			int w = tileSize - 1;
			int h = tileSize / 4 - 1;
			drawHealthBar2(g, thing, x, y, w, h, 2, thing.getHealth() / thing.getMaxHealth());
		}
	}
	
	public static void drawHealthBar2(Graphics g, Thing thing, int x, int y, int w, int h, int thickness, double ratio) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);
		
		g.setColor(Color.RED);
		g.fillRect(x + thickness, y + thickness, w - thickness*2, h - thickness*2);

		int greenBarWidth = (int) (ratio * (w - thickness*2));
		g.setColor(Color.GREEN);
		g.fillRect(x + thickness, y + thickness, greenBarWidth, h - thickness*2);
	}
	
	public void drawHitsplat(Graphics g, Thing thing) {

		int splatWidth = (int) (tileSize*.5);
		int splatHeight = (int) (tileSize*.5);
		
		thing.updateHitsplats();
		Hitsplat[] hitsplats = thing.getHitsplatList();
		
		for(int m = 0; m < hitsplats.length; m++) {
			if(hitsplats[m] == null) {
				continue;
			}
			double damage = hitsplats[m].getDamage();
			int i = hitsplats[m].getSquare();
			
			int x = (int) ((thing.getTile().getLocation().x() * tileSize) );
			int y = (int) ((thing.getTile().getLocation().y() * tileSize) );
			
			if(i == 1) {
				x = (int) ((thing.getTile().getLocation().x() * tileSize) + tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y() * tileSize) + tileSize*0.5);
			}
			if(i == 2) {
				x = (int) ((thing.getTile().getLocation().x() * tileSize) + tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y() * tileSize) );
			}
			if( i == 3) {
				x = (int) ((thing.getTile().getLocation().x() * tileSize) );
				y = (int) ((thing.getTile().getLocation().y() * tileSize) + tileSize*0.5);
			}
			
			String text = String.format("%.0f", damage);

			if(damage > 0) {
				g.drawImage(RED_HITSPLAT, x, y, splatWidth, splatHeight, null);
			}else if(damage == 0){
				g.drawImage(BLUE_HITSPLAT, x, y, splatWidth, splatHeight, null);
			}
			else if(damage < 0) {
				g.drawImage(GREEN_HITSPLAT, x, y, splatWidth, splatHeight, null);
				text = String.format("%.0f", -thing.getHitsplatDamage());
			}
			
			int fontSize = tileSize/4;
			g.setFont(new Font(DAMAGE_FONT.getFontName(), Font.BOLD, fontSize));
			int width = g.getFontMetrics().stringWidth(text);
			g.setColor(Color.WHITE);
			g.drawString(text, x + splatWidth/2 - width/2, (int) (y+fontSize*1.5));
		}
	}
	
	public void drawMinimap(Graphics g, int x, int y, int w, int h) {
		if(showHeightMap) {
			g.drawImage(heightMapImage, x, y, w, h, null);
		}
		else {
			g.drawImage(minimapImage, x, y, w, h, null);
		}
		if(game.world != null) { 
			Position offsetTile = getTileAtPixel(viewOffset);
			int boxx = (int) (offsetTile.x * w / game.world.getWidth() / 2);
			int boxy = (int) (offsetTile.y * h / game.world.getHeight() / 2);
			int boxw = (int) (getWidth() * w / tileSize / game.world.getWidth());
			int boxh = (int) (getHeight() * h / tileSize / game.world.getHeight());
			g.setColor(Color.yellow);
			g.drawRect(x + boxx, y + boxy, boxw, boxh);
		}
	}

	public ConcurrentLinkedQueue<Thing> getSelectedThings() {
		return selectedThings;
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
}
