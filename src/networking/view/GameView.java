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
	private int tileSize = 15;
	private boolean leftMouseDown = false;
	private boolean middleMouseDown = false;
	
	private Point mousePressLocation = null;
	private Position[] boxSelect = new Position[2];
	private boolean rightMouseDown = false;
	private boolean controlDown = false;
	private boolean shiftDown = false;

	private ConcurrentLinkedQueue<Thing> selectedThings = new ConcurrentLinkedQueue<Thing>();
	private Faction faction = Faction.getTempFaction();
	
	private LeftClickAction leftClickAction = LeftClickAction.NONE;
	private HasImage selectedThingToSpawn;
	private boolean summonPlayerControlled = true;
	private BuildingType selectedBuildingToPlan;
	
	public long previousTickTime;
	
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
					if (rightMouseDown || middleMouseDown) {
						shiftView(dx, dy);
					}
					if(leftMouseDown) {
						boxSelect[1] = getWorldCoordOfPixel(currentMouse);
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
				previousMouse = currentMouse;
				if(SwingUtilities.isLeftMouseButton(e)) {
					boxSelect[0] = getWorldCoordOfPixel(mousePressLocation);
					boxSelect[1] = getWorldCoordOfPixel(currentMouse);
					boxSelect = normalizeRectangle(boxSelect[0], boxSelect[1]);
					selectInBox(boxSelect[0], boxSelect[1], shiftDown);
					mousePressLocation = null;
					leftMouseDown = false;
				}
				else if(SwingUtilities.isRightMouseButton(e)) {
					rightMouseDown = false;
				}
				else if(SwingUtilities.isMiddleMouseButton(e)) {
					middleMouseDown = false;
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				previousMouse = e.getPoint();
				if(SwingUtilities.isLeftMouseButton(e)) {
					leftMouseDown = true;
					mousePressLocation = e.getPoint();
					boxSelect[0] = getWorldCoordOfPixel(mousePressLocation);
					boxSelect[1] = boxSelect[0];
				}
				else if(SwingUtilities.isRightMouseButton(e)) {
					rightMouseDown = true;
				}
				else if(SwingUtilities.isMiddleMouseButton(e)) {
					middleMouseDown = true;
				}
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
				else if(e.getKeyCode() == KeyEvent.VK_G) {
					toggleGuarding();
				}
				else if(e.getKeyCode() == KeyEvent.VK_M) {
					setBuildingToPlan(Game.buildingTypeMap.get("MINE"));
				}
				else if(e.getKeyCode() == KeyEvent.VK_I) {
					setBuildingToPlan(Game.buildingTypeMap.get("IRRIGATION"));
				}
				else if(e.getKeyCode() == KeyEvent.VK_W) {
					setBuildingToPlan(Game.buildingTypeMap.get("WALL_WOOD"));
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
		boolean foundNotGuarding = false;
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(!unit.isGuarding()) {
					foundNotGuarding = true;
				}
			}
		}
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				commandInterface.setGuarding(unit, foundNotGuarding);
			}
		}
	}
	
	public void setDrawDebugStrings(boolean enabled) {
		drawDebugStrings = enabled;
	}
	public boolean getDrawDebugStrings() {
		return drawDebugStrings;
	}

	private List<Tile> getTilesBetween(Position topLeft, Position botRight) {
		int topEvenY = (int) topLeft.y;
		int topOddY = (int) (topLeft.y - 0.5);
		int botEvenY = (int) (botRight.y);
		int botOddY = (int) (botRight.y - 0.5);
		LinkedList<Tile> tiles = new LinkedList<>();
		for(int i = topLeft.getIntX(); i <= botRight.getIntX(); i++) {
			int minj = i % 2 == 0 ? topEvenY : topOddY;
			int maxj = i % 2 == 0 ? botEvenY : botOddY;
			for(int j = minj; j <= maxj; j++) {
				TileLoc otherLoc = new TileLoc(i, j);
				Tile otherTile = game.world.get(otherLoc);
				if(otherTile != null) {
					tiles.add(otherTile);
				}
			}
		}
		return tiles;
	}
	
	public void selectInBox(Position topLeft, Position botRight, boolean shiftDown) {
		if(game.world == null) {
			return;
		}
		Tile topLeftTile = game.world.get(new TileLoc(topLeft.getIntX(), topLeft.getIntY()));
		Tile botRightTile = game.world.get(new TileLoc(botRight.getIntX(), botRight.getIntY()));
		if(topLeftTile == null || botRightTile == null) {
			return;
		}
		toggleSelectionForTiles(getTilesBetween(topLeft, botRight), shiftDown, controlDown);
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
			if(summoned != null && summoned.getFaction() == faction) {
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
			toggleSelectionForTiles(Arrays.asList(tile), shiftDown, controlDown);
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
					if(targetBuilding == null || targetBuilding.isBuilt()) {
						targetBuilding = targetTile.getRoad();
					}
					if(targetBuilding != null && (targetBuilding.getFaction() == unit.getFaction() || targetBuilding.getType().isRoad()) && !targetBuilding.isBuilt()) {
						commandInterface.buildThing(unit, targetBuilding.getTile(), targetBuilding.getType().isRoad(), !shiftDown);
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
	
	public void toggleSelectionForTiles(List<Tile> tiles, boolean shiftEnabled, boolean controlEnabled) {
		
		//deselects everything if shift or control isnt enabled
		if (shiftEnabled == false && !controlEnabled) {
			deselectEverything();
		}
		for(Tile tile : tiles) {
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
			Position tile = getWorldCoordOfPixel(new Position(mx, my));
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
	
	public Position getWorldCoordOfPixel(Position pixel) {
		Position tile = pixel.add(viewOffset).divide(tileSize);
		return tile;
	}
	public Position getWorldCoordOfPixel(Point pixel) {
		double column = ((pixel.x + viewOffset.x)/tileSize);
		double row = ((pixel.y + viewOffset.y)/tileSize);
		return new Position(column, row);
	}
	public Position getTileAtPixel(Point pixel) {
		int column = (int) ((pixel.x + viewOffset.x)/tileSize);
		int row = (int)((pixel.y + viewOffset.y - (column%2)*tileSize/2)/tileSize);
		return new Position(column, row);
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

	private static Position[] normalizeRectangle(Position one, Position two) {
		double x = Math.min(one.x, two.x);
		double y = Math.min(one.y, two.y);
		double width = Math.abs(one.x - two.x);
		double height = Math.abs(one.y - two.y);
		return new Position[] {new Position(x, y), new Position(x + width, y + height)};
	}
	private static Rectangle normalizeRectangle(Point one, Point two) {
		int x = Math.min(one.x, two.x);
		int y = Math.min(one.y, two.y);
		int width = Math.abs(one.x - two.x);
		int height = Math.abs(one.y - two.y);
		return new Rectangle(x, y, width, height);
	}

	public void drawGame(Graphics g, int panelWidth, int panelHeight) {
		if(game.world == null) {
			g.drawString("No World to display", 20, 20);
			return;
		}
		long startTime = System.currentTimeMillis();
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g, panelWidth, panelHeight, viewOffset);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		if(mousePressLocation != null) {
			Rectangle selectionRectangle = normalizeRectangle(mousePressLocation, previousMouse);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(Color.white);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(3));
			g2d.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
			g2d.setStroke(stroke);
		}
		if(faction != null && faction.getResearchTarget() != null && !faction.getResearchTarget().isCompleted()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * faction.getResearchTarget().getPointsSpent() / faction.getResearchTarget().getRequiredPoints();
			String progress = String.format(faction.getResearchTarget() + " %d/%d", faction.getResearchTarget().getPointsSpent(), faction.getResearchTarget().getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, panelWidth - panelWidth/3 - 4, 4, panelWidth/3, 30);
		}
		long endTime = System.currentTimeMillis();
		long deltaTime = endTime - startTime;
		g.setFont(KUIConstants.infoFont);
		int x = 10;
		int y = getHeight() - 5;
		g.setColor(Color.green);
		g.drawString("FPS:" + deltaTime, x, y);
		g.drawString("TICK:" + previousTickTime, x, y - KUIConstants.infoFont.getSize() - 2);
		x += 1;
		y += 1;
		g.setColor(Color.green.darker());
		g.drawString("FPS:" + deltaTime, x, y);
		g.drawString("TICK:" + previousTickTime, x, y - KUIConstants.infoFont.getSize() - 2);
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

			drawHoveredTiles((Graphics2D) g);
			drawPlannedThing((Graphics2D)g);
			drawSelectedThings((Graphics2D)g, lowerX, lowerY, upperX, upperY);
			
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
				Point drawAt = getDrawingCoords(unit.getTile().getLocation());
				int xx = drawAt.x + offset;
				int yy = drawAt.y + (indicatorSize + offset)*count + offset;
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
						Point drawAt = getDrawingCoords(tile.getLocation());
						g.fillRect(drawAt.x, drawAt.y, tileSize, tileSize);
					}
				}
			}
			
			if(drawDebugStrings) {
				if(tileSize >= 36) {
					drawDebugStrings(g, lowerX, lowerY, upperX, upperY);
				}
			}
			if(leftClickAction == LeftClickAction.ATTACK) {
				drawTarget(g, hoveredTile);
			}
		}
	}
	
	private void drawSelectedThings(Graphics2D g, int lowerX, int lowerY, int upperX, int upperY) {
		for(Thing thing : selectedThings) {
			// draw selection circle
			g.setColor(Utils.getTransparentColor(faction.color(), 150));
//			Utils.setTransparency(g, 0.8f);
			Stroke currentStroke = g.getStroke();
			int strokeWidth = tileSize/12;
			g.setStroke(new BasicStroke(strokeWidth));
			Point drawAt = getDrawingCoords(thing.getTile().getLocation());
			g.drawOval(drawAt.x + strokeWidth/2, drawAt.y + strokeWidth/2, tileSize-1 - strokeWidth, tileSize-1 - strokeWidth);
			g.setStroke(currentStroke);
//			Utils.setTransparency(g, 1f);

			// draw spawn location for buildings
			if(thing instanceof Building) {
				Building building = (Building) thing;
				if(building.getSpawnLocation() != building.getTile()) {
					drawAt = getDrawingCoords(building.getSpawnLocation().getLocation());
					g.drawImage(RALLY_POINT_IMAGE, drawAt.x, drawAt.y, tileSize, tileSize, null);
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
					Point prevDrawAt = getDrawingCoords(prev);
					for(Tile t : path) {
						drawAt = getDrawingCoords(t.getLocation());
						if(prev != null) {
							g.drawLine(prevDrawAt.x + tileSize/2, prevDrawAt.y + tileSize/2, 
									drawAt.x + tileSize/2, drawAt.y + tileSize/2);
						}
						prev = t.getLocation();
						prevDrawAt = drawAt;
					}
				}
				// draw destination flags
				for(PlannedAction plan : unit.actionQueue) {
					Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
					drawAt = getDrawingCoords(targetTile.getLocation());
					g.drawImage(FLAG, drawAt.x, drawAt.y, tileSize, tileSize, null);
				}
				int range = unit.getMaxRange();
				if(range > 1) {
					// draws the attack range for units
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile t = game.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							drawAt = getDrawingCoords(t.getLocation());
							int w = tileSize;
							int h = tileSize;

							if (t.getLocation().distanceTo(unit.getTile().getLocation()) <= range) {
								g.setColor(Color.BLACK);
								Utils.setTransparency(g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(unit.getTile().getLocation()) > range) {
										drawBorderBetween(g, t.getLocation(), tile.getLocation());
									}
								}
								Utils.setTransparency(g, 1);
							}
						}
					}
				}
			}
		}
	}
	private void drawPlannedThing(Graphics2D g) {
		BufferedImage bI = null;
		if (leftClickAction == LeftClickAction.PLAN_BUILDING) {
			bI = Utils.toBufferedImage(selectedBuildingToPlan.getImage(tileSize));
		}
		else if (leftClickAction == LeftClickAction.SPAWN_THING) {
			bI = Utils.toBufferedImage(selectedThingToSpawn.getImage(tileSize));
		}
		if(bI != null) {
			Utils.setTransparency(g, 0.5f);
			Point drawAt = getDrawingCoords(hoveredTile);
			g.drawImage(bI, drawAt.x, drawAt.y, tileSize, tileSize , null);
			Utils.setTransparency(g, 1f);
		}
	}
	
	private void drawDebugStrings(Graphics g, int lowerX, int lowerY, int upperX, int upperY) {
		int[][] rows = new int[upperX - lowerX][upperY - lowerY];
		int fontsize = tileSize/4;
		fontsize = Math.min(fontsize, 13);
		Font font = new Font("Consolas", Font.PLAIN, fontsize);
		g.setFont(font);
		for (int i = lowerX; i < upperX; i++) {
			for (int j = lowerY; j < upperY; j++) {
				Tile tile = game.world.get(new TileLoc(i, j));
				Point drawAt = getDrawingCoords(tile.getLocation());
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
				rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, strings, rows[i-lowerX][j-lowerY], fontsize, tileSize, drawAt);
				
				for(Unit unit : tile.getUnits()) {
					rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, unit.getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize, drawAt);
				}
				if(tile.getPlant() != null) {
					rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getPlant().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize, drawAt);
				}
				if(tile.hasBuilding()) {
					rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getBuilding().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize, drawAt);
				}
				if(tile.getRoad() != null) {
					rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getRoad().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize, tileSize, drawAt);
				}
			}
		}
	}
	
	private void drawHoveredTiles(Graphics2D g) {
		int strokeWidth = tileSize/10;
		strokeWidth = strokeWidth < 1 ? 1 : strokeWidth;
		Stroke stroke = g.getStroke();
		g.setStroke(new BasicStroke(strokeWidth));
		g.setColor(new Color(0, 0, 0, 64));
		if(mousePressLocation != null && leftMouseDown) {
			Position[] box = normalizeRectangle(boxSelect[0], boxSelect[1]);
			for(Tile tile : getTilesBetween(box[0], box[1])) {
				Point drawAt = getDrawingCoords(tile.getLocation());
				g.drawRect(drawAt.x + strokeWidth/2, drawAt.y + strokeWidth/2, tileSize-strokeWidth, tileSize-strokeWidth);
			}
		}
		else {
			Point drawAt = getDrawingCoords(hoveredTile);
			g.drawRect(drawAt.x + strokeWidth/2, drawAt.y + strokeWidth/2, tileSize-strokeWidth, tileSize-strokeWidth);
		}
		g.setStroke(stroke);
	}
	
	public Point getDrawingCoords(TileLoc tileLoc) {
		int x = tileLoc.x() * tileSize;
		int y = tileLoc.y() * tileSize + (tileLoc.x()%2)*tileSize/2;
		return new Point(x, y);
	}
	public double getOddDrawingOffset() {
		return tileSize/2;
	}

	public void drawTile(Graphics g, Tile theTile, double lowest, double highest) {
		Point drawAt = getDrawingCoords(theTile.getLocation());
		int draww = tileSize;
		int drawh = tileSize;
		int imagesize = draww < drawh ? draww : drawh;
		
		if(showHeightMap) {
			float heightRatio = (float) ((theTile.getHeight() - lowest) / (highest - lowest));
			int r = Math.max(Math.min((int) (255 * heightRatio), 255), 0);
			g.setColor(new Color(r, 0, 255 - r));
			g.fillRect(drawAt.x, drawAt.y, draww, drawh);
		}
		else {
			g.drawImage(theTile.getTerrain().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh, null);
//			t.drawEntities(g, currentMode);
			
			if(theTile.getResource() != null) {
				g.drawImage(theTile.getResource().getType().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh, null);
			}
			
			if(theTile.getFaction() != null && theTile.getFaction() != game.world.getFaction(World.NO_FACTION_ID)) {
//				g.setColor(Color.black);
//				g.fillRect(x, y, w, h); 
				g.setColor(theTile.getFaction().color());
				
				Utils.setTransparency(g, 0.5f);
				for(Tile tile : theTile.getNeighbors()) {
					if(tile.getFaction() != theTile.getFaction()) {
						drawBorderBetween(g, theTile.getLocation(), tile.getLocation());
					}
				}
				Utils.setTransparency(g, 1);
			}
//			if(game.world.borderTerritory.containsKey(theTile)) {
//				Utils.setTransparency(g, 1);
//				g.setColor(Color.BLACK);
//				g.fillRect(drawAt.x, drawAt.y, draww, drawh); 
//			}
			if (theTile.getRoad() != null) {
				drawBuilding(theTile.getRoad(), g, drawAt.x, drawAt.y, draww, drawh);
			}
			
			if(theTile.liquidType != LiquidType.DRY) {
				double alpha = Utils.getAlphaOfLiquid(theTile.liquidAmount);
//				 transparency liquids
				Utils.setTransparency(g, alpha);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawAt.x, drawAt.y, draww, drawh);
				Utils.setTransparency(g, 1);
				
				int imageSize = (int) Math.min(Math.max(draww*theTile.liquidAmount / 0.2, 1), draww);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawAt.x + draww/2 - imageSize/2, drawAt.y + drawh/2 - imageSize/2, imageSize, imageSize);
				g.drawImage(theTile.liquidType.getImage(imagesize), drawAt.x + draww/2 - imageSize/2, drawAt.y + draww/2 - imageSize/2, imageSize, imageSize, null);
			}
			
			if(theTile.getModifier() != null) {
				Utils.setTransparency(g, 0.9);
				g.drawImage(theTile.getModifier().getType().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh, null);
				Utils.setTransparency(g, 1);
			}
			
			if (!theTile.getItems().isEmpty()) {
				for (Item item : theTile.getItems()) {
					g.drawImage(item.getType().getImage(imagesize), drawAt.x + tileSize/4,
							drawAt.y + tileSize/4, tileSize/2, tileSize/2, null);
				}
			}
			if(theTile.getPlant() != null) {
				Plant p = theTile.getPlant();
				g.drawImage(p.getImage(tileSize), drawAt.x, drawAt.y, draww, drawh, null);
			}
			
			if(theTile.getBuilding() != null) {
				if(theTile.getBuilding().getIsSelected()) {
					g.drawImage(theTile.getBuilding().getHighlight(tileSize), drawAt.x, drawAt.y, draww, drawh, null);
				}
				drawBuilding(theTile.getBuilding(), g, drawAt.x, drawAt.y, draww, drawh);
			}
			for(Unit unit : theTile.getUnits()) {
				if(unit.getIsSelected()) {
					g.drawImage(unit.getHighlight(tileSize), drawAt.x, drawAt.y, draww, drawh, null);
				}
				g.drawImage(unit.getImage(tileSize), drawAt.x, drawAt.y, draww, drawh, null);
				if(unit.getIsHarvesting() == true) {
					g.drawImage(HARVEST_ICON, drawAt.x+draww/4, drawAt.y+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.isGuarding() == true) {
					g.drawImage(GUARD_ICON, drawAt.x+draww/4, drawAt.y+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.getAutoBuild() == true) {
					g.drawImage(AUTO_BUILD_ICON, drawAt.x+draww/4, drawAt.y+drawh/4, draww/2, drawh/2, null);
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
		Point drawAt = getDrawingCoords(tileLoc);
		int w = (int) (tileSize*8/10);
		int hi = (int)(tileSize*8/10);
		g.drawImage(TARGET_IMAGE, drawAt.x + tileSize*1/10, drawAt.y + tileSize*1/10, w, hi, null);
	}
	
	public void drawHealthBar(Graphics g, Thing thing) {
		if( tileSize <= 30) {
			return;
		}
		if(World.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(hoveredTile)) {
			Point drawAt = getDrawingCoords(thing.getTile().getLocation());
			int w = tileSize - 1;
			int h = tileSize / 4 - 1;
			drawHealthBar2(g, thing, drawAt.x + 1, drawAt.y + 1, w, h, 2, thing.getHealth() / thing.getMaxHealth());
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

		Point drawAt = getDrawingCoords(thing.getTile().getLocation());
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
			
			int x = (int) ((drawAt.x) );
			int y = (int) ((drawAt.y) );
			
			if(i == 1) {
				x = (int) ((drawAt.x) + tileSize*0.5);
				y = (int) ((drawAt.y) + tileSize*0.5);
			}
			if(i == 2) {
				x = (int) ((drawAt.x) + tileSize*0.5);
				y = (int) ((drawAt.y) );
			}
			if( i == 3) {
				x = (int) ((drawAt.x) );
				y = (int) ((drawAt.y) + tileSize*0.5);
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
	
	private void drawBorderBetween(Graphics g, TileLoc one, TileLoc two) {
		int width = tileSize / 8;
		Point drawAt = getDrawingCoords(one);
		if (one.x() == two.x()) {
			if (one.y() > two.y()) {
				g.fillRect(drawAt.x, drawAt.y, tileSize, width);
			}
			if (one.y() < two.y()) {
				g.fillRect(drawAt.x, drawAt.y + tileSize - width, tileSize, width);
			}
		}
		else {
			if(one.y() > two.y()) {
				int yoffset = (one.x()%2) * tileSize/2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + tileSize - width, drawAt.y + yoffset, width, tileSize/2);
				}
				else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, tileSize/2);
				}
			}
			else if(one.y() < two.y()) {
				int yoffset = (one.x()%2) * tileSize/2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + tileSize - width, drawAt.y + yoffset, width, tileSize/2);
				}
				else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, tileSize/2);
				}
			}
			else {
				int yoffset = (1 - one.x()%2) * tileSize/2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + tileSize - width, drawAt.y + yoffset, width, tileSize/2);
				}
				else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, tileSize/2);
				}
			}
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
			Position offsetTile = getWorldCoordOfPixel(viewOffset);
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
