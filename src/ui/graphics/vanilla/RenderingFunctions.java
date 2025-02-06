package ui.graphics.vanilla;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import game.*;
import game.actions.*;
import ui.*;
import ui.utils.DrawingUtils;
import utils.*;
import world.*;
import world.liquid.LiquidType;

public class RenderingFunctions {
	private static final Image TARGET_IMAGE = Utils.loadImage("Images/interfaces/ivegotyouinmysights.png");
	private static final Image ATTACK_CURSOR_ICON = Utils.loadImage("Images/interfaces/mouse_cursors/attack_icon.png");
	private static final Image DEFEND_CURSOR_ICON = Utils.loadImage("Images/interfaces/mouse_cursors/defend_icon.png");
	private static final Image MOVE_CURSOR_ICON = Utils.loadImage("Images/interfaces/mouse_cursors/move_icon.png");
	private static final Image RALLY_POINT_IMAGE = Utils.loadImage("Images/interfaces/queuelocation.png");
	private static final Image FLAG = Utils.loadImage("Images/interfaces/flag.png");
	private static final Image BUILD_ICON = Utils.loadImage("Images/interfaces/building.gif");
	private static final Image GUARD_ICON = Utils.loadImage("Images/interfaces/guard.png");
	private static final Image AUTO_BUILD_ICON = Utils.loadImage("Images/interfaces/autobuild.png");
	private static final Image WOODCUTTING_ICON = Utils.loadImage("Images/interfaces/axe.png");
	private static final Image MINING_ICON = Utils.loadImage("Images/interfaces/pick.png");
	private static final Image FARMING_ICON = Utils.loadImage("Images/interfaces/hoe.png");
	private static final Image SNOW = Utils.loadImage("Images/weather/snow.png");
	private static final Image SNOW2 = Utils.loadImage("Images/weather/snow2.png");
	private static final Image RED_HITSPLAT = Utils.loadImage("Images/interfaces/redhitsplat.png");
	private static final Image BLUE_HITSPLAT = Utils.loadImage("Images/interfaces/bluehitsplat.png");
	private static final Image GREEN_HITSPLAT = Utils.loadImage("Images/interfaces/greenhitsplat.png");

	private static final Font DAMAGE_FONT = new Font("Comic Sans MS", Font.BOLD, 14);

	public static void drawLeftClickCursor(RenderingState state, TileLoc tileLoc) {
		Image cursorImage = TARGET_IMAGE;
		switch (state.gameViewState.leftClickAction) {
			case ATTACK:
				cursorImage = ATTACK_CURSOR_ICON;
				break;
			case GUARD:
				cursorImage = DEFEND_CURSOR_ICON;
				break;
			case MOVE:
				cursorImage = MOVE_CURSOR_ICON;
				break;
			default:
				return;
		}
		
		Point drawAt = getDrawingCoords(tileLoc, state.tileSize);
		int w = (int) (state.tileSize * 8 / 10);
		int hi = (int) (state.tileSize * 8 / 10);
		state.g.drawImage(cursorImage, drawAt.x + state.tileSize * 1 / 10, drawAt.y + state.tileSize * 1 / 10, w, hi, null);
	}
	
	public static void drawAirFlow(RenderingState state, boolean arrows, Tile tile, Point drawat) {
//		int w = (int) (state.tileSize * 8 / 10);
//		int hi = (int) (state.tileSize * 8 / 10);
		if(tile.getAir().getFlowDirection() != null) {
			Image image = arrows ? tile.getAir().getFlowDirection().getArrowImage() 
					: tile.getAir().getFlowDirection().getImage() ;
//			System.out.println(tile.getAir().getFlowDirection());
			state.g.drawImage(image, drawat.x, drawat.y, state.tileSize, state.tileSize, null);
//			g.drawImage(TARGET_IMAGE, drawAt.x + frozenTileSize * 1 / 10, drawAt.y + frozenTileSize * 1 / 10, w, hi, null);
		}

	}
	
	private static int drawUnitQuantitySquaresHelper(Graphics2D g, int numInFaction, Color factionColor, int x, int y, int indicatorSize) {
		while (numInFaction > 0) {
			if (numInFaction >= 20) {
				g.setColor(factionColor);
				int size = indicatorSize * 20 / 2;
				g.fillRect(x, y + 2, indicatorSize, size);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, indicatorSize, size + 4);
				g.drawRect(x, y + 1, indicatorSize, size + 2);
				g.drawRect(x, y + 2, indicatorSize, size);
				for (int i = 1; i < 20; i++) {
					int w = (i % 5 == 0) ? indicatorSize : indicatorSize / 2;
					int yy = y + 1 + size * i / 20;
					g.drawLine(x, yy, x + w, yy);
				}
				y += size + 4;
				numInFaction -= 20;
			}
			else if (numInFaction >= 5) {
				g.setColor(factionColor);
				int size = indicatorSize * 5 * 3 / 5;
				g.fillRect(x, y + 1, indicatorSize, size);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, indicatorSize, size + 2);
				g.drawRect(x, y + 1, indicatorSize, size);
				for (int i = 1; i < 5; i++) {
					g.drawLine(x, y + 1 + size * i / 5, x + indicatorSize/2, y + 1 + size * i / 5);
				}
				y += size + 2;
				numInFaction -= 5;
			}
			else {
				g.setColor(factionColor);
				g.fillRect(x, y, indicatorSize, indicatorSize);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, indicatorSize, indicatorSize);
				y += indicatorSize;
				numInFaction -= 1;
			}
		}
		return y;
	}
	
	public static void drawUnitQuantitySquares(RenderingState state, Tile tile, Point4 drawat) {
		int[] numPerFaction = new int[state.world.getFactions().size()];
		if (state.faction.id() >= numPerFaction.length) {
			return;
		}
		for (Unit unit : tile.getUnits()) {
			numPerFaction[unit.getFactionID()]++;
		}
		
		int offset = state.tileSize / 40;
		int spacePerIndicator = state.tileSize / 10;
		int yy = drawat.y + offset;
		int xx = drawat.x + offset;
		yy = drawUnitQuantitySquaresHelper(state.g, numPerFaction[state.faction.id()], 
				state.faction.color(), xx, yy, spacePerIndicator);
		
		for (int factionid = 0; factionid < numPerFaction.length; factionid++) {
			if (factionid == state.faction.id()) {
				continue;
			}
			yy += offset;
			yy = drawUnitQuantitySquaresHelper(state.g, numPerFaction[factionid], 
					state.world.getFactions().get(factionid).color(), xx, yy, spacePerIndicator);
		}
	}
	
	public static void drawProjectiles(RenderingState state) {
		for (Projectile p : state.world.getData().getProjectiles()) {
			int extra = (int) (state.tileSize * p.getExtraSize());
			double ratio = 0.5 * p.getHeight() / p.getMaxHeight();
			int shadowOffset = (int) (state.tileSize * ratio / 2);
			Point drawAt = getDrawingCoords(p.getTile().getLocation(), state.tileSize);
			
			state.g.drawImage(p.getType().getMipMap().getShadow(0), drawAt.x + shadowOffset,
					drawAt.y + shadowOffset, state.tileSize - shadowOffset * 2,
					state.tileSize - shadowOffset * 2, null);
			state.g.drawImage(p.getType().getMipMap().getImage(0), drawAt.x - extra / 2,
					drawAt.y - p.getHeight() - extra / 2, state.tileSize + extra,
					state.tileSize + extra, null);
		}
	}
	
	public static void drawInventoryHealthBar(RenderingState state, Tile tile, Point drawat) {
		Building road = tile.getRoad();
		if (road != null) {
			drawHealthBar(state, road);
			drawHitsplat(state.g, road, state.tileSize);
		}
		Building building = tile.getBuilding();
		if (building != null) {
			if(building.hasInventory())
				drawInventory(state.g, tile, building.getInventory(), state.tileSize);
			drawHealthBar(state, building);
			drawHitsplat(state.g, building, state.tileSize);
		}
		Plant plant = tile.getPlant();
		if (plant != null) {
			drawHealthBar(state, plant);
			drawHitsplat(state.g, plant, state.tileSize);
		}
		for (Unit unit : tile.getUnits()) {
			if(unit.hasInventory())
				drawInventory(state.g, unit.getTile(), unit.getInventory(), state.tileSize);
			drawHitsplat(state.g, unit, state.tileSize);
		}
		RenderingFunctions.drawUnitHealthBars(state, tile, drawat);
	}
	
	public static void drawTileGradientShading(RenderingState state, Tile tile, Point drawat) {

		double regularShadowTransparency = .5;
		Utils.setTransparency(state.g, regularShadowTransparency);
		state.g.setColor(Color.black);
		
//		float[] neighborHeights = new float[Direction.values().length];
//		for (Tile neighbor : tile.getNeighbors()) {
//			Direction dir = Direction.getDirection(tile.getLocation(), neighbor.getLocation());
//			neighborHeights[dir.ordinal()] = neighbor.getHeight();
//		}
//		
//		
		
		for (Tile neighbor : tile.getNeighbors()) {
			float heightDiff = (neighbor.getHeight() - tile.getHeight() ) - 10;
			int MAX_HEIGHT_DIFF = 50;
			if (heightDiff <= 1) {
				continue;
			}
			double gradientRatio = Math.min(heightDiff, MAX_HEIGHT_DIFF) / (MAX_HEIGHT_DIFF);
			int offset = (int) (gradientRatio * state.tileSize / 2);
			drawGradientBorderBetween(
					state.g, tile.getLocation(), neighbor.getLocation(),
					drawat.x, drawat.y, state.tileSize, offset,
					new Color(0, 0, 0, 0), new Color(0, 0, 0, 255));
		}
		Utils.setTransparency(state.g, 1);
	}

	private static void drawInventory(Graphics g, Tile tile, Inventory inventory, int tileSize) {
		int draww = tileSize/4;
		Point drawAt = getDrawingCoords(tile.getLocation(), tileSize);
		drawAt.x += draww/2;
		drawInventory(g, inventory, drawAt.x, drawAt.y, draww, draww, tile);
	}
	
	private static boolean shouldDrawHealthBar(int tileSize, Thing thing, TileLoc hoveredTile) {
		if (tileSize <= 30) {
			return false;
		}
		if (World.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(hoveredTile)) {
			return true;
		}
		return false;
	}
	
	private static void drawHealthBar(RenderingState state, Thing thing) {
		if (shouldDrawHealthBar(state.tileSize, thing, state.gameViewState.hoveredTile)) {
			Point drawAt = getDrawingCoords(thing.getTile().getLocation(), state.tileSize);
			int w = state.tileSize - 1;
			int h = state.tileSize / 7 - 1;
			int borderThickness = h / 8;
			drawHealthBar2(state.g, thing, drawAt.x + 1, drawAt.y + 1, w, h, borderThickness, thing.getHealth() / thing.getMaxHealth());
		}
	}

	public static void drawHealthBar2(Graphics g, Thing thing, 
			int x, int y, int w, int h, int borderThickness, double ratio) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);

		g.setColor(Color.RED);
		g.fillRect(x + borderThickness, y + borderThickness, w - borderThickness * 2, h - borderThickness * 2);

		int greenBarWidth = (int) (ratio * (w - borderThickness * 2));
		g.setColor(Color.GREEN);
		g.fillRect(x + borderThickness, y + borderThickness, greenBarWidth, h - borderThickness * 2);
	}
	
	private static void drawUnitHealthBar(RenderingState state, int drawatx, int drawaty, int size, Unit unit) {
		if (shouldDrawHealthBar(state.tileSize, unit, state.gameViewState.hoveredTile)) {
			state.g.setColor(Color.RED);
			state.g.fillRect(drawatx + size/4, drawaty, 3, size);
			int greenBarHeight = (int) (size * unit.getHealth() / unit.getMaxHealth());
			state.g.setColor(Color.GREEN);
			state.g.fillRect(drawatx + size/4, drawaty + size - greenBarHeight, 3, greenBarHeight);
		}
	}
	
	private static void drawHitsplat(Graphics g, Thing thing, int tileSize) {
	
		Point drawAt = getDrawingCoords(thing.getTile().getLocation(), tileSize);
		int splatWidth = (int) (tileSize * .5);
		int splatHeight = (int) (tileSize * .5);
	
		thing.updateHitsplats();
		Hitsplat[] hitsplats = thing.getHitsplatList();
	
		for (int m = 0; m < hitsplats.length; m++) {
			if (hitsplats[m] == null) {
				continue;
			}
			double damage = hitsplats[m].getDamage();
			int i = hitsplats[m].getSquare();
	
			int x = (int) ((drawAt.x));
			int y = (int) ((drawAt.y));
	
			if (i == 1) {
				x = (int) ((drawAt.x) + tileSize * 0.5);
				y = (int) ((drawAt.y) + tileSize * 0.5);
			}
			if (i == 2) {
				x = (int) ((drawAt.x) + tileSize * 0.5);
				y = (int) ((drawAt.y));
			}
			if (i == 3) {
				x = (int) ((drawAt.x));
				y = (int) ((drawAt.y) + tileSize * 0.5);
			}
	
			String text = String.format("%.0f", damage);
	
			if (damage > 0) {
				g.drawImage(RED_HITSPLAT, x, y, splatWidth, splatHeight, null);
			} else if (damage == 0) {
				g.drawImage(BLUE_HITSPLAT, x, y, splatWidth, splatHeight, null);
			} else if (damage < 0) {
				g.drawImage(GREEN_HITSPLAT, x, y, splatWidth, splatHeight, null);
				text = String.format("%.0f", -damage);
			}
	
			int fontSize = tileSize / 4;
			g.setFont(new Font(DAMAGE_FONT.getFontName(), Font.BOLD, fontSize));
			int width = g.getFontMetrics().stringWidth(text);
			g.setColor(Color.WHITE);
			g.drawString(text, x + splatWidth / 2 - width / 2, (int) (y + fontSize * 1.5));
		}
	}

	public static void drawSelectedThings(RenderingState state) {
		for (Thing thing : state.gameViewState.selectedThings) {
			// draw selection circle
			state.g.setColor(Utils.getTransparentColor(state.faction.color(), 150));
//			Utils.setTransparency(g, 0.8f);
			Stroke currentStroke = state.g.getStroke();
			int strokeWidth = state.tileSize / 12;
			state.g.setStroke(new BasicStroke(strokeWidth));
			Point drawAt = getDrawingCoords(thing.getTile().getLocation(), state.tileSize);
			state.g.drawOval(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, 
					state.tileSize - 1 - strokeWidth,
					state.tileSize - 1 - strokeWidth);
			state.g.setStroke(currentStroke);
//			Utils.setTransparency(g, 1f);

			// draw spawn location for buildings
			if (thing instanceof Building) {
				Building building = (Building) thing;
				if (building.getSpawnLocation() != building.getTile()) {
					drawAt = getDrawingCoords(building.getSpawnLocation().getLocation(), state.tileSize);
					state.g.drawImage(RALLY_POINT_IMAGE, drawAt.x, drawAt.y, state.tileSize, state.tileSize, null);
				}
				
				int range = building.getType().getVisionRadius();
				if (range > 1) {
					// draws the range for buildings
					for (int i = state.lowerX; i < state.upperX; i++) {
						for (int j = state.lowerY; j < state.upperY; j++) {
							Tile t = state.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							drawAt = getDrawingCoords(t.getLocation(), state.tileSize);
							if (t.getLocation().distanceTo(building.getTile().getLocation()) <= range) {
								state.g.setColor(Color.BLACK);
								Utils.setTransparency(state.g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(building.getTile().getLocation()) > range) {
										drawAt = getDrawingCoords(t.getLocation(), state.tileSize);
										drawBorderBetween(state.g, t.getLocation(), tile.getLocation(), drawAt.x, drawAt.y, state.tileSize);
									}
								}
								Utils.setTransparency(state.g, 1);
							}
						}
					}
				}
			}

			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				// draw attacking target
				Thing target = unit.getTarget();
				if (target != null) {
					drawTarget(state.g, target.getTile().getLocation(), state.tileSize);
				}
				// draw path
				LinkedList<Tile> path = unit.getCurrentPath();
				if (path != null) {
					state.g.setColor(Color.green);
					TileLoc prev = unit.getTile().getLocation();
					Point prevDrawAt = getDrawingCoords(prev, state.tileSize);
					try {
						for (Tile t : path) {
							drawAt = getDrawingCoords(t.getLocation(), state.tileSize);
							if (prev != null) {
								state.g.drawLine(prevDrawAt.x + state.tileSize / 2, prevDrawAt.y + state.tileSize / 2,
										drawAt.x + state.tileSize / 2, drawAt.y + state.tileSize / 2);
							}
							prev = t.getLocation();
							prevDrawAt = drawAt;
						}
					}
					catch (ConcurrentModificationException e) {
						System.err.println("Concurrent modification while drawing path.");
					}
				}
				// draw destination flags
				for (PlannedAction plan : unit.actionQueue) {
					Tile targetTile = plan.getTile();
					if (targetTile == null) {
						continue;
					}
					drawAt = getDrawingCoords(targetTile.getLocation(), state.tileSize);
					state.g.drawImage(FLAG, drawAt.x, drawAt.y, state.tileSize, state.tileSize, null);
				}
				int range = unit.getMaxAttackRange();
				if (range > 1) {
					// draws the attack range for units
					for (int i = state.lowerX; i < state.upperX; i++) {
						for (int j = state.lowerY; j < state.upperY; j++) {
							Tile t = state.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							drawAt = getDrawingCoords(t.getLocation(), state.tileSize);
							if (t.getLocation().distanceTo(unit.getTile().getLocation()) <= range) {
								state.g.setColor(Color.BLACK);
								Utils.setTransparency(state.g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(unit.getTile().getLocation()) > range) {
										drawBorderBetween(state.g, t.getLocation(), tile.getLocation(),
												drawAt.x, drawAt.y, state.tileSize);
									}
								}
								Utils.setTransparency(state.g, 1);
							}
						}
					}
				}
			}
		}
	}

	public static void drawTarget(Graphics g, TileLoc tileLoc, int tileSize) {
		Point drawAt = getDrawingCoords(tileLoc, tileSize);
		int w = (int) (tileSize * 8 / 10);
		int hi = (int) (tileSize * 8 / 10);
		g.drawImage(TARGET_IMAGE, drawAt.x + tileSize * 1 / 10, drawAt.y + tileSize * 1 / 10, w, hi, null);
	}
	
	public static void drawPlannedThing(RenderingState state) {
		BufferedImage bI = null;
		if (state.gameViewState.leftClickAction == LeftClickAction.PLAN_BUILDING) {
			bI = Utils.toBufferedImage(state.gameViewState.selectedBuildingToPlan.getMipMap().getImage(state.tileSize), false);
		} else if (state.gameViewState.leftClickAction == LeftClickAction.SPAWN_THING) {
			bI = Utils.toBufferedImage(Utils.getImageFromThingType(state.gameViewState.selectedThingToSpawn).getImage(state.tileSize), false);
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
	
	public static void drawHeatMapColor(RenderingState state, Tile tile, Point drawat) {
		float ratio = getHeatMapColorRatio(tile, state.mapMode,
				state.lowHeight, state.highHeight, state.lowPressure, state.highPressure, 
				state.lowTemp, state.highTemp, state.lowHumidity, state.highHumidity);
		state.g.setColor(new Color(ratio, 0f, 1f - ratio));
		state.g.fillRect(drawat.x, drawat.y, state.tileSize, state.tileSize);
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
	
	public static void drawSnowTemp(RenderingState state, Tile tile, Point drawat) {
		if(tile.getTemperature() <= Constants.FREEZETEMP) {
			state.g.drawImage(SNOW2, drawat.x, drawat.y, state.tileSize, state.tileSize, null);
		}
		else if(tile.getAir().getTemperature() <= Constants.FREEZETEMP) {
			state.g.drawImage(SNOW, drawat.x, drawat.y, state.tileSize, state.tileSize, null);
		}
	}
	
	public static void drawResource(ResourceType resource, Graphics2D g, int drawx, int drawy, int tileSize) {
		g.drawImage(resource.getMipMap().getImage(tileSize), 
				drawx, drawy, tileSize, tileSize, null);
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
		drawBorderBetween(g, one, two, drawx, drawy, frozenTileSize, frozenTileSize/8);
	}
		
	private static void drawBorderBetween(
			Graphics2D g, TileLoc one, TileLoc two, 
			int drawx, int drawy, int frozenTileSize, int borderWidth) {
		int width = borderWidth;
		// always draw shape counterclockwise with first edge being along tile border
		int[] x = new int[4];
		int[] y = new int[4];
		if (one.x() == two.x()) {
			if (one.y() > two.y()) {
				x[0] = drawx + frozenTileSize;
				x[1] = drawx;
				x[2] = x[1] + width;
				x[3] = x[0] - width;

				y[0] = drawy;
				y[1] = y[0];
				y[2] = drawy + width;
				y[3] = y[2];
			}
			if (one.y() < two.y()) {
				x[0] = drawx;
				x[1] = drawx + frozenTileSize;
				x[2] = x[1] - width;
				x[3] = x[0] + width;

				y[0] = drawy + frozenTileSize;
				y[1] = y[0];
				y[2] = drawy + frozenTileSize - width;
				y[3] = y[2];
			}
		} else {

			int yoffset = 0;
			if ((one.y() > two.y()) || (one.y() < two.y())) {
				yoffset = one.x() % 2;
			} else {
				yoffset = (1 - one.x()) % 2;
			}

			if (one.x() < two.x()) {
				x[0] = drawx + frozenTileSize;
				x[1] = x[0];
				x[2] = drawx + frozenTileSize - width;
				x[3] = x[2];
				
				// top right edge
				if (yoffset == 0) {
					y[0] = drawy + frozenTileSize/2;
					y[1] = drawy;
					y[2] = y[1] + width;
					y[3] = y[0];
				}
				// bottom right edge
				else {
					y[0] = drawy + frozenTileSize;
					y[1] = drawy + frozenTileSize/2;
					y[2] = y[1];
					y[3] = y[0] - width;
				}
			}
			else {
				x[0] = drawx;
				x[1] = x[0];
				x[2] = drawx + width;
				x[3] = x[2];

				// top left edge
				if (yoffset == 0) {
					y[0] = drawy;
					y[1] = drawy + frozenTileSize/2;
					y[2] = y[1];
					y[3] = y[0] + width;
				}
				// bottom left edge
				else {
					y[0] = drawy + frozenTileSize/2;
					y[1] = drawy + frozenTileSize;
					y[2] = y[1] - width;
					y[3] = y[0];
				}
			}
		}
		g.fillPolygon(x, y, x.length);
	}

	private static void drawGradientBorderBetween(
			Graphics2D g, TileLoc one, TileLoc two, 
			int drawx, int drawy, int frozenTileSize, 
			int borderWidth, Color colorInside, Color colorOutside) {
		int width = borderWidth;
		// always draw shape counterclockwise with first edge being along tile border
		int[] x = new int[4];
		int[] y = new int[4];
		int gradientx1 = 0, gradientx2 = 0;
		int gradienty1 = 0, gradienty2 = 0;
		if (one.x() == two.x()) {
			if (one.y() > two.y()) {
				gradientx1 = gradientx2 = drawx;
				gradienty1 = drawy;
				gradienty2 = drawy + width;

				x[0] = drawx + frozenTileSize;
				x[1] = drawx;
				x[2] = x[1] + width;
				x[3] = x[0] - width;

				y[0] = drawy;
				y[1] = y[0];
				y[2] = drawy + width;
				y[3] = y[2];
			}
			if (one.y() < two.y()) {
				gradientx1 = gradientx2 = drawx;
				gradienty1 = drawy + frozenTileSize;
				gradienty2 = drawy + frozenTileSize - width;

				x[0] = drawx;
				x[1] = drawx + frozenTileSize;
				x[2] = x[1] - width;
				x[3] = x[0] + width;

				y[0] = drawy + frozenTileSize;
				y[1] = y[0];
				y[2] = drawy + frozenTileSize - width;
				y[3] = y[2];
			}
		} else {

			int yoffset = 0;
			if ((one.y() > two.y()) || (one.y() < two.y())) {
				yoffset = one.x() % 2;
			} else {
				yoffset = (1 - one.x()) % 2;
			}

			if (one.x() < two.x()) {
				gradientx1 = drawx + frozenTileSize;
				gradientx2 = drawx + frozenTileSize - width;
				gradienty1 = gradienty2 = drawy;

				x[0] = drawx + frozenTileSize;
				x[1] = x[0];
				x[2] = drawx + frozenTileSize - width;
				x[3] = x[2];
				
				// top right edge
				if (yoffset == 0) {
					y[0] = drawy + frozenTileSize/2;
					y[1] = drawy;
					y[2] = y[1] + width;
					y[3] = y[0];
				}
				// bottom right edge
				else {
					y[0] = drawy + frozenTileSize;
					y[1] = drawy + frozenTileSize/2;
					y[2] = y[1];
					y[3] = y[0] - width;
				}
			}
			else {
				gradientx1 = drawx;
				gradientx2 = drawx + width;
				gradienty1 = gradienty2 = drawy;

				x[0] = drawx;
				x[1] = x[0];
				x[2] = drawx + width;
				x[3] = x[2];

				// top left edge
				if (yoffset == 0) {
					y[0] = drawy;
					y[1] = drawy + frozenTileSize/2;
					y[2] = y[1];
					y[3] = y[0] + width;
				}
				// bottom left edge
				else {
					y[0] = drawy + frozenTileSize/2;
					y[1] = drawy + frozenTileSize;
					y[2] = y[1] - width;
					y[3] = y[0];
				}
			}
		}
		Paint originalPaint = g.getPaint();
		g.setPaint(new GradientPaint(
				gradientx1, gradienty1, colorOutside,
				gradientx2, gradienty2, colorInside));
		g.fillPolygon(x, y, x.length);
		g.setPaint(originalPaint);
	}
	
	private static void drawPolygonLiquid(Tile tile, Graphics g, int drawx, int drawy, int tileSize, RenderingState state) {

		int centerTileX = drawx + tileSize/2;
		int centerTileY = drawy + tileSize/2;
		int maxWater = 20;
		int halfTileEdge = tileSize/2;
		
		
		for (int layer = 0; layer <= 2; layer++) {
			int waterLevelOffset = layer * 10;
			Color baseColor = tile.liquidType.getMipMap().getColor(tileSize);
			if (layer == 2) {
				baseColor = baseColor.darker();
			}
		
			int myTileWaterAmount = Math.max(0, (int)(halfTileEdge * (tile.liquidAmount-waterLevelOffset) / maxWater));

			if (layer == 0 || layer == 1) {
//				baseColor = baseColor.brighter();
				Utils.setTransparency(g, Math.min(1.0, 1.0*myTileWaterAmount/halfTileEdge));
			}

			if (myTileWaterAmount == 0) {
				continue;
			}
			int[] neighborAmounts = new int[Direction.values().length];
			for (int i = 0; i < neighborAmounts.length; i++) {
				neighborAmounts[i] = tileSize/2;
			}
			for (Tile neighbor : tile.getNeighbors()) {
				Direction d = Direction.getDirection(tile.getLocation(), neighbor.getLocation());
				neighborAmounts[d.ordinal()] = Math.max(0, (int)(halfTileEdge * (neighbor.liquidAmount-waterLevelOffset) / maxWater));
			}
			
			
			final int numPoints = 12;
			int[] xpoints = new int[numPoints];
			int[] ypoints = new int[numPoints];
			
			int xoffset = 0;
			int yoffset = 0;
//			int totalNeighborLiquid = 0;
//			
//			for (int i = 0; i < 6; i++) {
//				if (neighborAmounts[i] > 0) {
//					totalNeighborLiquid = neighborAmounts[i];
//					xoffset += Direction.values()[i].deltax()*neighborAmounts[i];
//					yoffset += Direction.values()[i].deltay()*neighborAmounts[i];
//				}
//			}
//			if (totalNeighborLiquid > 0) {
//				totalNeighborLiquid += myTileWaterAmount;
//				xoffset = tileSize * xoffset / totalNeighborLiquid / 2;
//				yoffset = tileSize * yoffset / totalNeighborLiquid / 2;
//			}
			xoffset += centerTileX;
			yoffset += centerTileY;
			
			for (int i = 0; i < numPoints; i++) {
				xpoints[i] = xoffset;
				ypoints[i] = yoffset;
			}
			
//			// NW top
			xpoints[11] -= myTileWaterAmount*3/4;
			ypoints[11] -= myTileWaterAmount*3/4;
			
			// N
			ypoints[0] -= myTileWaterAmount;
			
			// NE top
			xpoints[1] += myTileWaterAmount*3/4;
			ypoints[1] -= myTileWaterAmount*2/3;
			
			// NE center
			xpoints[2] += myTileWaterAmount*2/3;
			ypoints[2] -= myTileWaterAmount*3/4;
			
			// NE bottom
			xpoints[3] += myTileWaterAmount;
	
			// SE center
			xpoints[4] += myTileWaterAmount*2/3;
			ypoints[4] += myTileWaterAmount*3/4;
	
			// SE bottom
			xpoints[5] += myTileWaterAmount*3/4;
			ypoints[5] += myTileWaterAmount*2/3;
	
			// S
			ypoints[6] += myTileWaterAmount;
	
			// SW bottom
			xpoints[7] -= myTileWaterAmount*3/4;
			ypoints[7] += myTileWaterAmount*2/3;
	
			// SW center
			xpoints[8] -= myTileWaterAmount*2/3;
			ypoints[8] += myTileWaterAmount*3/4;
	
			// SW top
			xpoints[9] -= myTileWaterAmount;
	
			// NW center
			xpoints[10] -= myTileWaterAmount*2/3;
			ypoints[10] -= myTileWaterAmount*3/4;

			int MINX = drawx;
			int MINY = drawy;
			int MAXX = drawx + tileSize;
			int MAXY = drawy + tileSize;
			if (neighborAmounts[Direction.NORTH.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.NORTH.ordinal()])/2;
				xpoints[11] = centerTileX - avg;
				ypoints[11] = MINY;
				ypoints[0] = MINY;
				xpoints[1] = centerTileX + avg;
				ypoints[1] = MINY;
			}
			if (neighborAmounts[Direction.NORTHEAST.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.NORTHEAST.ordinal()])/2;
				xpoints[1] = MAXX;
				ypoints[1] = Math.min(ypoints[1], drawy + tileSize/4 - avg/2);
				xpoints[2] = MAXX;
				ypoints[2] = drawy + tileSize/4;
				xpoints[3] = MAXX;
				ypoints[3] = drawy + tileSize/4 + avg/2;
			}
			if (neighborAmounts[Direction.SOUTHEAST.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.SOUTHEAST.ordinal()])/2;
				xpoints[3] = MAXX;
				ypoints[3] = Math.min(ypoints[3], drawy + tileSize*3/4 - avg/2);
				xpoints[4] = MAXX;
				ypoints[4] = drawy + tileSize*3/4;
				xpoints[5] = MAXX;
				ypoints[5] = drawy + tileSize*3/4 + avg/2;
			}
			if (neighborAmounts[Direction.SOUTH.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.SOUTH.ordinal()])/2;
				xpoints[5] = Math.max(xpoints[5], centerTileX + avg);
				ypoints[5] = MAXY;
				ypoints[6] = MAXY;
				xpoints[7] = centerTileX - avg;
				ypoints[7] = MAXY;
			}
			if (neighborAmounts[Direction.SOUTHWEST.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.SOUTHWEST.ordinal()])/2;
				xpoints[7] = MINX;
				ypoints[7] = Math.max(ypoints[7], drawy + tileSize*3/4 + avg/2);
				xpoints[8] = MINX;
				ypoints[8] = drawy + tileSize*3/4;
				xpoints[9] = MINX;
				ypoints[9] = drawy + tileSize*3/4 - avg/2;
			}
			if (neighborAmounts[Direction.NORTHWEST.ordinal()] > 0) {
				int avg = (myTileWaterAmount + neighborAmounts[Direction.NORTHWEST.ordinal()])/2;
				xpoints[9] = MINX;
				ypoints[9] = Math.min(ypoints[9], drawy + tileSize/4 + avg/2);
				xpoints[10] = MINX;
				ypoints[10] = drawy + tileSize/4;
				xpoints[11] = MINX;
				ypoints[11] = Math.min(ypoints[11], drawy + tileSize/4 - avg/2);
			}
			
			
			for (int i = 0; i < numPoints; i++) {
//				xpoints[i] += xoffset;
				xpoints[i] = xpoints[i] < drawx ? drawx : xpoints[i];
				xpoints[i] = xpoints[i] > drawx + tileSize ? drawx + tileSize : xpoints[i];
	
//				ypoints[i] += yoffset;
				ypoints[i] = ypoints[i] < drawy ? drawy : ypoints[i];
				ypoints[i] = ypoints[i] > drawy + tileSize ? drawy + tileSize : ypoints[i];
			}
	
			g.setColor(baseColor);
			g.fillPolygon(xpoints, ypoints, numPoints);

			if (layer == 0 || layer == 1) {
				Utils.setTransparency(g, 1);
			}
		}
	}
	
	public static void drawLiquid(Tile tile, Graphics g, int drawx, int drawy, int tileSize, RenderingState state) {
//		drawPolygonLiquid(tile, g, drawx, drawy, tileSize, state);
		
		DrawLiquids.drawPolygonLiquid2(tile, g, drawx, drawy, tileSize, state);

//		double alpha = Utils.getAlphaOfLiquid(tile.liquidAmount);
////		 transparency liquids
//		Utils.setTransparency(g, alpha);
////		if(tile.liquidType == LiquidType.WATER && tile.liquidAmount <= 0.6) {
////			Utils.setTransparency(g, 0);
////		}
//		
//		g.setColor(tile.liquidType.getMipMap().getColor(tileSize));
//		g.fillRect(drawx, drawy, tileSize, tileSize);
//		Utils.setTransparency(g, 1);
//		
//		int imageSize = (int) Math.min(Math.max(tileSize * tile.liquidAmount / 20, 1), tileSize);
//		g.setColor(tile.liquidType.getMipMap().getColor(tileSize));
//		g.fillRect(drawx + tileSize / 2 - imageSize / 2, drawy + tileSize / 2 - imageSize / 2, imageSize,
//				imageSize);
//		g.drawImage(tile.liquidType.getMipMap().getImage(tileSize), drawx + tileSize / 2 - imageSize / 2,
//				drawy + tileSize / 2 - imageSize / 2, imageSize, imageSize, null);
//		
//		if(tile.liquidType == LiquidType.WATER) {
//			alpha = Utils.getAlphaDepthOfLiquid(tile.liquidAmount);
//			g.setColor(Color.BLACK);
//			Utils.setTransparency(g, alpha);
//			g.fillRect(drawx, drawy, tileSize, tileSize);
//			Utils.setTransparency(g, 1);
//		}
		

		// The idea is to draw some sort of reflection in the water but
		// it currently doesn't look very good.
//		Utils.setTransparency(g, alpha/2);
//		double widthRatio = (double) DrawingUtils.SKY_BACKGROUND.getWidth(null) / state.world.getWidth();
//		double heightRatio = (double) DrawingUtils.SKY_BACKGROUND.getHeight(null) / state.world.getHeight();
//		g.drawImage(DrawingUtils.SKY_BACKGROUND,
//				drawx, drawy, drawx + tileSize, drawy + tileSize,
//				(int) (tile.getLocation().x() * widthRatio),
//				(int) (tile.getLocation().y() * heightRatio),
//				(int) ((tile.getLocation().x() + 1) * widthRatio),
//				(int) ((tile.getLocation().y() + 1) * heightRatio),
//				null);
//		Utils.setTransparency(g, 1);
	
	}
	
	public static void drawModifiers(GroundModifier modifier, Graphics g, int drawx, int drawy, int tileSize) {
		Utils.setTransparency(g, 0.9);
		g.drawImage(modifier.getType().getMipMap().getImage(tileSize), 
				drawx, drawy, tileSize, tileSize, null);
		Utils.setTransparency(g, 1);
	}
	
	public static void drawInventory(Graphics g, Inventory inventory, int drawx, int drawy, int draww, int drawh, Tile tile) {
		int numUnique = inventory.numUnique();
		if(numUnique == 0) {
			return;
		}
		int rows = (int) Math.ceil(Math.sqrt(numUnique));
		int imageWidth = Math.max(draww, drawh) / rows;
		int x = 0;
		int y = 0;
		// iterate through items on the tile
		for (Item item : inventory.getItems()) {
			if(item == null || item.getAmount() == 0) {
				continue;
			}
			// draws the item on the ground
			g.drawImage(item.getType().getMipMap().getImage(imageWidth), 
					drawx + x*imageWidth,
					drawy + y*imageWidth, 
					imageWidth, imageWidth, null);
			
			if(tile.getBuilding() == null && tile.getUnits().peek() == null) {
				// draws the number of items on the ground
				g.setFont(new Font("SansSerif", Font.BOLD, 12));
				g.drawString(""+item.getAmount(), drawx + x*imageWidth, drawy + y*imageWidth + 1/2*imageWidth);
			}
			
			
			
			// iterate to next row
			x++;
			if(x >= rows) {
				x = 0;
				y++;
			}
		}
	}
	
	public static void drawPlant(Plant plant, Graphics g, int drawx, int drawy, int tileSize) {
		g.drawImage(plant.getMipMap().getImage(tileSize), drawx, drawy, tileSize, tileSize, null);
	}
	
	public static void drawBuilding(Building building, Graphics g, int drawx, int drawy, int tileSize) {
		if (building.isSelected()) {
			g.drawImage(building.getMipMap().getHighlight(tileSize), drawx, drawy, tileSize, tileSize,
					null);
		}
		
		BufferedImage bI = Utils.toBufferedImage(building.getMipMap().getImage(0), false);
		if (building.isBuilt() == false) {
			// draws the transparent version
			Utils.setTransparency(g, 0.5f);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(bI, drawx, drawy, tileSize, tileSize, null);
			Utils.setTransparency(g, 1f);
			// draws the partial image
			double percentDone = 1 - building.getRemainingEffort() / building.getTotalEffort();
			int imageRatio = Math.max(1, (int) (bI.getHeight() * percentDone));
			int partialHeight = Math.max(1, (int) (tileSize * percentDone));
			bI = bI.getSubimage(0, bI.getHeight() - imageRatio, bI.getWidth(), imageRatio);
			g.drawImage(bI, drawx, drawy - partialHeight + tileSize, tileSize, partialHeight, null);
			g.drawImage(BUILD_ICON, drawx + tileSize / 8, drawy + tileSize / 8, tileSize * 6 / 8, tileSize * 6 / 8, null);
		} else {
			g.drawImage(bI, drawx, drawy, tileSize, tileSize, null);
		}
	}
	
	private static void drawUnitImage(RenderingState state, int drawatx, int drawaty, int size, Unit unit) {
		if (unit.isSelected()) {
			state.g.drawImage(unit.getMipMap().getHighlight(size), drawatx, drawaty, size, size, null);
		}
		state.g.drawImage(unit.getMipMap().getImage(size), drawatx, drawaty, size, size, null);

		if (unit.isGuarding() == true) {
			state.g.drawImage(GUARD_ICON, drawatx + size / 4, drawaty + size / 4, size / 2, size / 2, null);
		}
		if (unit.isAutoBuilding() == true) {
			state.g.drawImage(AUTO_BUILD_ICON, drawatx + size / 4, drawaty + size / 4, size / 2, size / 2, null);
		}
		if (unit.isIdle()) {
			state.g.setColor(Color.gray);
			int w = size / 2;
			int h = size / 10;
			int x = drawatx + w / 2;
			int y = drawaty + size - h;
			state.g.fillRect(x, y, w, h);
			state.g.setColor(Color.DARK_GRAY);
			state.g.drawRect(x, y, w, h);
		} else {
			PlannedAction action = unit.getNextPlannedAction();
			if (action != null) {
				Image image = getIconForAction(action);
				if (image != null) {
					state.g.drawImage(image, drawatx, drawaty, size, size, null);
				}
			}
		}
	}
	
	public static void drawSunShadows(RenderingState state, Tile tile, Point4 drawat) {
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
		sunShadowSetup(state.g);
		if (tile.getPlant() != null) {
			drawSunShadow(tile.getPlant().getMipMap(), state.g,
					drawat.x, drawat.y, state.tileSize, sunShadow);
		}
		if (tile.getBuilding() != null && tile.getBuilding().isBuilt()) {
			drawSunShadow(tile.getBuilding().getMipMap(), state.g,
					drawat.x, drawat.y, state.tileSize, sunShadow);
		}
		drawUnitsIterationHelper(state, tile, drawat, 
				(s, x, y, size, thing) -> drawSunShadow(thing.getMipMap(), s.g, x, y, size, sunShadow));
		sunShadowCleanup(state.g);
	}

	public static final int MAX_NUM_ROWS = 2;
	public static final int MAX_PER_ROW = 6;
	public static final int MAX_TOTAL = MAX_PER_ROW * MAX_NUM_ROWS;
	
	private static UnitDrawLoc computeUnitDrawLoc(Point drawat, int counter, int total, int tileSize) {
		int row = counter / MAX_PER_ROW;
		int rowTotal = (counter <= MAX_PER_ROW) ? counter : counter % MAX_PER_ROW;
		int column = counter % MAX_PER_ROW;
		int drawSize = tileSize;
		int offset = 0;
		if (total >= 2) {
			drawSize = tileSize * (MAX_PER_ROW + 9 - total) / (MAX_PER_ROW + 7);
			offset = drawSize / (total + 1);
		}

		int drawatx = drawat.x + offset * column - offset / 2 + row;
		int drawaty = drawat.y + (tileSize - drawSize) - row * drawSize/2 - column;
		return new UnitDrawLoc(drawatx, drawaty, drawSize);
	}

	private static void drawUnitsIterationHelper(RenderingState state, Tile tile, Point drawat, DrawThingFunction drawFunc) {
		int counter = tile.getUnits().size();
		int total = Math.min(MAX_PER_ROW, counter);
		Iterator<Unit> it = tile.getUnits().descendingIterator();
		while (it.hasNext()) {
			Unit unit = it.next();
			counter--;
			if (counter >= MAX_TOTAL) {
				continue;
			}
			UnitDrawLoc drawLoc = computeUnitDrawLoc(drawat, counter, total, state.tileSize);
//			drawUnitImage(state.g, drawLoc.x, drawLoc.y, drawLoc.size, unit);
			drawFunc.draw(state, drawLoc.x, drawLoc.y, drawLoc.size, unit);
		}
	}
	
	public static void drawUnitImages(RenderingState state, Tile tile, Point drawat) {
		drawUnitsIterationHelper(state, tile, drawat, 
				(s, x, y, size, thing) -> drawUnitImage(s, x, y, size, (Unit)thing));
	}
	
	public static void drawUnitHealthBars(RenderingState state, Tile tile, Point drawat) {
		drawUnitsIterationHelper(state, tile, drawat, 
				(s, x, y, size, thing) -> drawUnitHealthBar(s, x, y, size, (Unit)thing));
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
	
	/**
	 * Must be called before calling drawSunShadow
	 */
	private static void sunShadowSetup(Graphics g) {
		double daylight = World.getDaylight();
		Utils.setTransparency(g, daylight * daylight / 4);
	}
	/**
	 * Must call sunShadowSetup before this and sunShadowCleanup after this
	 */
	private static void drawSunShadow(MipMap m, Graphics g, int drawx, int drawy, int tileSize, int sunShadow) {
		// sun shadow images are double the width of a tile
		g.drawImage(m.getSunShadow(tileSize, sunShadow), 
				drawx - tileSize/2, drawy, tileSize*2, tileSize, null);

	}
	/**
	 * Must be called after all calls to drawSunShadow
	 */
	private static void sunShadowCleanup(Graphics g) {
		Utils.setTransparency(g, 1);
	}

	private static int drawDebugStringsHelper(Graphics g, List<String> strings, int row, int fontsize, int tileSize, Point drawAt) {
		int x = drawAt.x + 2;
		int y = drawAt.y + fontsize / 2;
		int maxWidth = 0;
		for (String s : strings) {
			int stringWidth = g.getFontMetrics().stringWidth(s) + 2;
			maxWidth = maxWidth > stringWidth ? maxWidth : stringWidth;
		}
		g.setColor(Color.black);
		g.fillRect(x, y + 2 + row, maxWidth, fontsize * strings.size());
		for (String s : strings) {
			g.setColor(Color.green);
			row += fontsize;
			g.drawString(s, x, y + row);
		}
		row += 1;
		return row;
	}

	public static void drawDebugStringsHelper1(RenderingState state, Tile tile, Point drawat, int fontsize) {
		int row = 0;
		row = drawDebugStringsHelper(state.g, tile.getDebugStrings(), row, fontsize,
				state.tileSize, drawat);

		for (Unit unit : tile.getUnits()) {
			row = drawDebugStringsHelper(state.g, unit.getDebugStrings(),
					row, fontsize, state.tileSize, drawat);
		}
		if (tile.getPlant() != null) {
			row = drawDebugStringsHelper(state.g, tile.getPlant().getDebugStrings(),
					row, fontsize, state.tileSize, drawat);
		}
		if (tile.hasBuilding()) {
			row = drawDebugStringsHelper(state.g, tile.getBuilding().getDebugStrings(),
					row, fontsize, state.tileSize, drawat);
		}
		if (tile.getRoad() != null) {
			row = drawDebugStringsHelper(state.g, tile.getRoad().getDebugStrings(),
					row, fontsize, state.tileSize, drawat);
		}
	}
	
	public static Point4 getDrawingCoords(TileLoc tileLoc, int frozenTileSize) {
		int yoffset = (tileLoc.x() % 2) * frozenTileSize / 2;
		int x = tileLoc.x() * frozenTileSize;
		int y = tileLoc.y() * frozenTileSize;
		int x2 = (tileLoc.x() + 1) * frozenTileSize;
		int y2 = (tileLoc.y() + 1) * frozenTileSize;
		return new Point4(x, y + yoffset, x2 - x, y2 - y);
	}
}
