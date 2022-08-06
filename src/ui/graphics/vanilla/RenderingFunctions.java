package ui.graphics.vanilla;

import java.awt.*;
import java.awt.image.BufferedImage;

import game.*;
import game.actions.*;
import game.components.Inventory;
import ui.*;
import utils.*;
import world.*;

public class RenderingFunctions {

	private static final Image BUILD_ICON = Utils.loadImage("Images/interfaces/building.gif");
	private static final Image GUARD_ICON = Utils.loadImage("Images/interfaces/guard.png");
	private static final Image AUTO_BUILD_ICON = Utils.loadImage("Images/interfaces/autobuild.png");
	private static final Image WOODCUTTING_ICON = Utils.loadImage("Images/interfaces/axe.png");
	private static final Image MINING_ICON = Utils.loadImage("Images/interfaces/pick.png");
	private static final Image FARMING_ICON = Utils.loadImage("Images/interfaces/hoe.png");
	private static final Image SNOW = Utils.loadImage("Images/weather/snow.png");
	private static final Image SNOW2 = Utils.loadImage("Images/weather/snow2.png");

	public static void drawPlannedThing(RenderingState state) {
		BufferedImage bI = null;
		if (state.gameViewState.leftClickAction == LeftClickAction.PLAN_BUILDING) {
			bI = Utils.toBufferedImage(state.gameViewState.selectedBuildingToPlan.getMipMap().getImage(state.tileSize));
		} else if (state.gameViewState.leftClickAction == LeftClickAction.SPAWN_THING) {
			bI = Utils.toBufferedImage(Utils.getImageFromThingType(state.gameViewState.selectedThingToSpawn).getImage(state.tileSize));
		}
		if (bI != null) {
			Utils.setTransparency(state.g, 0.5f);
			Point drawAt = getDrawingCoords(state.gameViewState.hoveredTile, state.tileSize);
			state.g.drawImage(bI, drawAt.x, drawAt.y, state.tileSize, state.tileSize, null);
			Utils.setTransparency(state.g, 1f);
		}
	}
	
	public static void drawHoveredTiles(RenderingState state) {
		int strokeWidth = state.tileSize / 10;
		strokeWidth = strokeWidth < 1 ? 1 : strokeWidth;
		Stroke stroke = state.g.getStroke();
		state.g.setStroke(new BasicStroke(strokeWidth));
		state.g.setColor(new Color(0, 0, 0, 64));
		if (state.gameViewState.leftMouseDown && state.gameViewState.draggingMouse && state.gameViewState.boxSelect[0] != null && state.gameViewState.boxSelect[1] != null) {
			for (Tile tile : Utils.getTilesBetween(state.world, state.gameViewState.boxSelect[0], state.gameViewState.boxSelect[1])) {
				Point drawAt = getDrawingCoords(tile.getLocation(), state.tileSize);
				state.g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, state.tileSize - strokeWidth,
						state.tileSize - strokeWidth);
			}
		} else {
			if (state.world.get(state.gameViewState.hoveredTile) != null) {
				Point drawAt = getDrawingCoords(state.gameViewState.hoveredTile, state.tileSize);
				state.g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, state.tileSize - strokeWidth,
						state.tileSize - strokeWidth);
				if(state.gameViewState.drawDebugStrings) {
					state.g.setStroke(stroke);
					state.g.setColor(Color.yellow);
					state.g.drawString(state.gameViewState.hoveredTile.toString(), drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2);
				}
			}
		}
		state.g.setStroke(stroke);
	}
	
	public static void drawHeatMapColor(RenderingState state) {
		float ratio = getHeatMapColorRatio(state.tile, state.mapMode,
				state.lowHeight, state.highHeight, state.lowPressure, state.highPressure, 
				state.lowTemp, state.highTemp, state.lowHumidity, state.highHumidity);
		state.g.setColor(new Color(ratio, 0f, 1f - ratio));
		state.g.fillRect(state.drawx, state.drawy, state.draww, state.drawh);
	}
	
	public static float getHeatMapColorRatio(Tile tile, MapMode mapMode,
			double lowHeight, double highHeight, double lowPressure, double highPressure, 
			double lowTemp, double highTemp, double lowHumidity, double highHumidity) {

		float ratio = 0;
		if (mapMode == MapMode.HEIGHT) {
			ratio = (float) ((tile.getHeight() - lowHeight) / (highHeight - lowHeight));
		} else if (mapMode == MapMode.PRESSURE) {
			ratio = (float) ((tile.getAir().getPressure() - lowPressure)
					/ (highPressure - lowPressure));
		} else if (mapMode == MapMode.TEMPURATURE) {
			ratio = (float) ((tile.getAir().getTemperature() - lowTemp) / (highTemp - lowTemp));
		} else if (mapMode == MapMode.HUMIDITY) {
			ratio = (float) ((tile.getAir().getHumidity() - lowHumidity)
					/ (highHumidity - lowHumidity));
		} else if (mapMode == MapMode.FLOW) {
			ratio = (float) ((tile.getAir().getPressure() - lowPressure)
					/ (highPressure - lowPressure));
		} else if (mapMode == MapMode.PRESSURE2) {
//			ratio = (float) ((tile.getAtmosphere().getPressure() - lowPressure)
//					/ (highPressure - lowPressure));
		}
		return Math.max(Math.min(ratio, 1f), 0f);
	}
	
	public static void drawSnowTemp(Tile tile, Graphics2D g, int drawx, int drawy, int draww, int drawh) {
		if(tile.getTemperature() <= Constants.FREEZETEMP) {
			g.drawImage(SNOW2, drawx, drawy, draww, drawh, null);
		}
		else if(tile.getAir().getTemperature() <= Constants.FREEZETEMP) {
			g.drawImage(SNOW, drawx, drawy, draww, drawh, null);
		}
	}
	
	public static void drawResource(Resource resource, Graphics2D g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		g.drawImage(resource.getType().getMipMap().getImage(frozenTileSize), 
				drawx, drawy, draww, drawh, null);
	}
	
	public static void drawFactionBorders(Tile tile, Graphics2D g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		g.setColor(tile.getFaction().borderColor());
		for (Tile neighbor : tile.getNeighbors()) {
			if (neighbor.getFaction() != tile.getFaction()) {
				drawBorderBetween(g, tile.getLocation(), neighbor.getLocation(), drawx, drawy, frozenTileSize);
			}
		}
	}
	
	private static void drawBorderBetween(Graphics2D g, TileLoc one, TileLoc two, int drawx, int drawy, int frozenTileSize) {
		int width = frozenTileSize / 8;
		if (one.x() == two.x()) {
			if (one.y() > two.y()) {
				g.fillRect(drawx, drawy, frozenTileSize, width);
			}
			if (one.y() < two.y()) {
				g.fillRect(drawx, drawy + frozenTileSize - width, frozenTileSize, width);
			}
		} else {
			if (one.y() > two.y()) {
				int yoffset = (one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawx + frozenTileSize - width, drawy + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawx, drawy + yoffset, width, frozenTileSize / 2);
				}
			} else if (one.y() < two.y()) {
				int yoffset = (one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawx + frozenTileSize - width, drawy + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawx, drawy + yoffset, width, frozenTileSize / 2);
				}
			} else {
				int yoffset = (1 - one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawx + frozenTileSize - width, drawy + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawx, drawy + yoffset, width, frozenTileSize / 2);
				}
			}
		}
	}
	
	public static void drawLiquid(Tile tile, Graphics g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		double alpha = Utils.getAlphaOfLiquid(tile.liquidAmount);
//		 transparency liquids
		Utils.setTransparency(g, alpha);
		g.setColor(tile.liquidType.getMipMap().getColor(frozenTileSize));
		g.fillRect(drawx, drawy, draww, drawh);
		Utils.setTransparency(g, 1);

		int imageSize = (int) Math.min(Math.max(draww * tile.liquidAmount / 20, 1), draww);
		g.setColor(tile.liquidType.getMipMap().getColor(frozenTileSize));
		g.fillRect(drawx + draww / 2 - imageSize / 2, drawy + drawh / 2 - imageSize / 2, imageSize,
				imageSize);
		g.drawImage(tile.liquidType.getMipMap().getImage(frozenTileSize), drawx + draww / 2 - imageSize / 2,
				drawy + draww / 2 - imageSize / 2, imageSize, imageSize, null);
	
	}
	
	public static void drawModifiers(GroundModifier modifier, Graphics g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		Utils.setTransparency(g, 0.9);
		g.drawImage(modifier.getType().getMipMap().getImage(frozenTileSize), 
				drawx, drawy, draww, drawh, null);
		Utils.setTransparency(g, 1);
	}
	
	public static void drawInventory(Graphics g, Inventory inventory, int drawx, int drawy, int draww, int drawh) {
		int numUnique = inventory.numUnique();
		if(numUnique == 0) {
			return;
		}
		int rows = (int) Math.ceil(Math.sqrt(numUnique));
		int imageWidth = Math.max(draww, drawh) / rows;
		int x = 0;
		int y = 0;
		for (Item item : inventory.getItems()) {
			if(item == null || item.getAmount() == 0) {
				continue;
			}
			g.drawImage(item.getType().getMipMap().getImage(imageWidth), 
					drawx + x*imageWidth,
					drawy + y*imageWidth, 
					imageWidth, imageWidth, null);
			x++;
			if(x >= rows) {
				x = 0;
				y++;
			}
		}
	}
	
	public static void drawPlant(Plant plant, Graphics g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		drawSunShadow(plant.getMipMap(), g, drawx, drawy, draww, drawh, frozenTileSize);
		g.drawImage(plant.getMipMap().getImage(frozenTileSize), drawx, drawy, draww, drawh, null);
	
	}
	
	public static void drawBuilding(Building building, Graphics g, int drawx, int drawy, int draww, int drawh, boolean drawSunShadow, int frozenTileSize) {
		if (building.isSelected()) {
			g.drawImage(building.getMipMap().getHighlight(frozenTileSize), drawx, drawy, draww, drawh,
					null);
		}
		
		BufferedImage bI = Utils.toBufferedImage(building.getMipMap().getImage(0));
		if (building.isBuilt() == false) {
			// draws the transparent version
			Utils.setTransparency(g, 0.5f);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(bI, drawx, drawy, draww, drawh, null);
			Utils.setTransparency(g, 1f);
			// draws the partial image
			double percentDone = 1 - building.getRemainingEffort() / building.getType().getBuildingEffort();
			int imageRatio = Math.max(1, (int) (bI.getHeight() * percentDone));
			int partialHeight = Math.max(1, (int) (frozenTileSize * percentDone));
			bI = bI.getSubimage(0, bI.getHeight() - imageRatio, bI.getWidth(), imageRatio);
			g.drawImage(bI, drawx, drawy - partialHeight + drawh, draww, partialHeight, null);
			g.drawImage(BUILD_ICON, drawx + frozenTileSize / 8, drawy + frozenTileSize / 8, draww * 6 / 8, drawh * 6 / 8, null);
		} else {
			if (drawSunShadow) {
				drawSunShadow(building.getMipMap(), g, drawx, drawy, draww, drawh, frozenTileSize);
			}
			g.drawImage(bI, drawx, drawy, draww, drawh, null);
		}
	}
	
	public static void drawUnit(Unit unit, RenderingState state) {

//		LinkedList<Tile> path = unit.getCurrentPath();
//		double timeLeft = unit.getTimeToMove();
//		double timeStart = unit.getCombatStats().getMoveSpeed();
//		double percent = 0;
//		if(timeStart != 0) {
//			percent = timeLeft / timeStart;
//		}

		if (unit.isSelected()) {
			state.g.drawImage(unit.getMipMap().getHighlight(state.tileSize), state.drawx, state.drawy, state.draww, state.drawh, null);
		}
//		if(path != null && path.peek() != null) {
//			Tile targetTile = path.peek();
//			int targetx = targetTile.getLocation().x();
//			int targety = targetTile.getLocation().y();
//			int dx = Math.abs(drawx - targetx);
//			int dy = Math.abs(drawy - targety);
//			if(targetx > drawx) {
//				dx *= -1; 
//			}
//			if(targetx < drawx){
//				dx *= 1; 
//			}
//			
//			if(targety > drawy) {
//				dy *= -1; 
//			}
//			if(targety < drawy){
//				dy *= 1; 
//			}
//			g.drawImage(unit.getImage(frozenTileSize), (int)(drawx + drawx - dx), (int)(drawy + drawy - dy), draww, drawh, null);
//		}else {
		drawSunShadow(unit.getMipMap(), state.g, state.drawx, state.drawy, state.draww, state.drawh, state.tileSize);
		state.g.drawImage(unit.getMipMap().getImage(state.tileSize), state.drawx, state.drawy, state.draww, state.drawh, null);
//		}

		if (unit.isGuarding() == true) {
			state.g.drawImage(GUARD_ICON, state.drawx + state.draww / 4, state.drawy + state.drawh / 4, state.draww / 2, state.drawh / 2, null);
		}
		if (unit.isAutoBuilding() == true) {
			state.g.drawImage(AUTO_BUILD_ICON, state.drawx + state.draww / 4, state.drawy + state.drawh / 4, state.draww / 2, state.drawh / 2, null);
		}
		if (unit.isIdle()) {
			state.g.setColor(Color.gray);
			state.g.fillRect(state.drawx + state.draww * 4 / 5, state.drawy, state.draww / 5, state.drawh / 5);
		} else {
			PlannedAction action = unit.getNextPlannedAction();
			if (action != null) {
				Image image = getIconForAction(action);
				if (image != null) {
					state.g.drawImage(image, state.drawx, state.drawy, state.draww, state.drawh, null);
				}
			}

		}
	}
	
	public static Image getIconForAction(PlannedAction action) {
        if (action.type == ActionType.HARVEST) {
        	
        	if(action.target instanceof Plant) {
        		if(((Plant)action.target).getType() == Game.plantTypeMap.get("TREE")) {
        			return WOODCUTTING_ICON;
        		}
        		else if(((Plant)action.target).getType() == Game.plantTypeMap.get("BERRY")) {
        			return FARMING_ICON;
        		}
        		else if(((Plant)action.target).getType() == Game.plantTypeMap.get("CACTUS")) {
        			return WOODCUTTING_ICON;
        		}
        		else if(((Plant)action.target).getType() == Game.plantTypeMap.get("CATTAILS")) {
        			return FARMING_ICON;
        		}
        	}
        	
        	if(action.target instanceof Building) {
        		if (((Building)action.target).getType() == Game.buildingTypeMap.get("MINE")) {
        			return MINING_ICON;
        		}
        		else if (((Building)action.target).getType() == Game.buildingTypeMap.get("FARM")) {
        			return FARMING_ICON;
        		}
        	}
        	
        	if(action.target == null) {
        		return MINING_ICON;
        	}
        }
		return null;
    }
	
	private static void drawSunShadow(MipMap m, Graphics g, int drawx, int drawy, int draww, int drawh, int frozenTileSize) {
		int dayOffset = World.getCurrentDayOffset();
		if (dayOffset > Constants.DAY_DURATION) {
			if (dayOffset < Constants.DAY_DURATION + Constants.NIGHT_DURATION/2) {
				dayOffset = Constants.DAY_DURATION;
			}
			else {
				dayOffset = 0;
			}
		}
		int sunShadow = (int) (dayOffset * MipMap.NUM_SUN_SHADOWS / (Constants.DAY_DURATION + 1));
		double daylight = World.getDaylight();
		Utils.setTransparency(g, daylight * daylight / 4);
		g.drawImage(m.getSunShadow(frozenTileSize, sunShadow), drawx, drawy, draww, drawh, null);
		Utils.setTransparency(g, 1);
	}
	
	private static Point getDrawingCoords(TileLoc tileLoc, int frozenTileSize) {
		int x = tileLoc.x() * frozenTileSize;
		int y = tileLoc.y() * frozenTileSize + (tileLoc.x() % 2) * frozenTileSize / 2;
		return new Point(x, y);
	}
}
