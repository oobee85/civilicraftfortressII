package ui.graphics.vanilla;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import game.liquid.*;
import ui.*;
import ui.graphics.*;
import ui.view.GameView.*;
import utils.*;
import world.*;

public class VanillaDrawer extends Drawer {

	private static final int FAST_MODE_TILE_SIZE = 10;
	private static final int NUM_DEBUG_DIGITS = 3;
	
	private static final Font DAMAGE_FONT = new Font("Comic Sans MS", Font.BOLD, 14);
	
	private static final Image RALLY_POINT_IMAGE = Utils.loadImage("Images/interfaces/queuelocation.png");
	private static final Image TARGET_IMAGE = Utils.loadImage("Images/interfaces/ivegotyouinmysights.png");
	private static final Image FLAG = Utils.loadImage("Images/interfaces/flag.png");
	private static final Image BUILD_ICON = Utils.loadImage("Images/interfaces/building.png");
	private static final Image HARVEST_ICON = Utils.loadImage("Images/interfaces/harvest.png");
	private static final Image GUARD_ICON = Utils.loadImage("Images/interfaces/guard.png");
	private static final Image AUTO_BUILD_ICON = Utils.loadImage("Images/interfaces/autobuild.png");
	private static final Image RED_HITSPLAT = Utils.loadImage("Images/interfaces/redhitsplat.png");
	private static final Image BLUE_HITSPLAT = Utils.loadImage("Images/interfaces/bluehitsplat.png");
	private static final Image GREEN_HITSPLAT = Utils.loadImage("Images/interfaces/greenhitsplat.png");
	private static final Image SNOW = Utils.loadImage("Images/weather/snow.png");
	
	private JPanel canvas;
	
	private volatile BufferedImage[] buffers = new BufferedImage[3];
	private volatile Position[] drawnAtOffset = new Position[3];
	private volatile int[] drawnAtTileSize = new int[3];
	private volatile int currentBuffer = 0;
	private Semaphore nextRequested = new Semaphore(1);
	private Semaphore numAvailable = new Semaphore(0);
	private long drawTime;
	
	// must take snapshot of current tile size and view offset to draw a consistent image
	// otherwise while player is dragging view it will change view offset 
	// while it is drawing which causes a bunch of issues.
	private int frozenTileSize;
	private Position frozenViewOffset = new Position(0, 0);
	
	public VanillaDrawer(Game game, GameViewState state) {
		super(game, state);
		canvas = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(game == null) {
					return;
				}
				
				g.setColor(game.getBackgroundColor());
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				
				int buffer = currentBuffer;
				if(state.tileSize == drawnAtTileSize[buffer]) {
					g.drawImage(buffers[buffer], 
							drawnAtOffset[buffer].getIntX() - state.viewOffset.getIntX(), 
							drawnAtOffset[buffer].getIntY() - state.viewOffset.getIntY(), 
							null);
				}
				else {
					g.drawImage(buffers[buffer], 0, 0, null);
				}
				numAvailable.tryAcquire();

				g.setColor(Color.black);
				g.drawRect(-1, 0, canvas.getWidth() + 1, canvas.getHeight());
				
				drawOverlayStuff(g);
				
				if(nextRequested.availablePermits() < 1) {
					nextRequested.release();
				}
				Toolkit.getDefaultToolkit().sync();
			}
		};
		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resetBuffers();
			}
		});
		resetBuffers();
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while(true) {
						nextRequested.acquire();
						int next = (currentBuffer + 1) % buffers.length;
						Graphics2D g = buffers[next].createGraphics();
						frozenViewOffset.x = state.viewOffset.x;
						frozenViewOffset.y = state.viewOffset.y;
						frozenTileSize = state.tileSize;
						drawStuff(g, buffers[next].getWidth(), buffers[next].getHeight());
						g.dispose();
						drawnAtOffset[next].x = frozenViewOffset.x;
						drawnAtOffset[next].y = frozenViewOffset.y;
						drawnAtTileSize[next] = frozenTileSize;
						numAvailable.release();
						currentBuffer = next;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
		thread.start();
	}
	
	private void resetBuffers() {
		for(int i = 0; i < buffers.length; i++) {
			drawnAtOffset[i] = new Position(0, 0);
		}
		int w = Math.max(1, canvas.getWidth());
		int h = Math.max(1, canvas.getHeight());
		for(int i = 0; i < buffers.length; i++) {
			buffers[i] = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		}
	}
	
	public Component getDrawingCanvas() {
		return canvas;
	}
	
	private void drawOverlayStuff(Graphics g) {
		if (state.mousePressLocation != null && state.draggingMouse == true) {
			Graphics2D g2d = (Graphics2D)g;
			Rectangle selectionRectangle = normalizeRectangle(state.mousePressLocation, state.previousMouse);
			g2d.setColor(Color.white);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(3));
			g2d.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width,
					selectionRectangle.height);
			g2d.setStroke(stroke);
		}
		if (state.faction != null && state.faction.getResearchTarget() != null && !state.faction.getResearchTarget().isCompleted()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * state.faction.getResearchTarget().getPointsSpent()
					/ state.faction.getResearchTarget().getRequiredPoints();
			String progress = String.format(state.faction.getResearchTarget() + " %d/%d",
					state.faction.getResearchTarget().getPointsSpent(), state.faction.getResearchTarget().getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress,
					canvas.getWidth() - canvas.getWidth() / 3 - 4, 4, canvas.getWidth() / 3, 30);
		}
		g.setFont(KUIConstants.infoFont);
		for (int i = 0; i < 2; i++) {
			int x = 10;
			int y = canvas.getHeight() - 5;
			g.setColor(Color.green);
			if (i == 1) {
				g.setColor(Color.black);
				x++;
				y++;
			}
			g.drawString("DRAW(ms):" + drawTime, x, y);
			g.drawString("TICK(ms):" + state.previousTickTime, x, y - KUIConstants.infoFont.getSize() - 2);
			if (Game.DEBUG) {
				String fstr = "";
				for (Faction f : game.world.getFactions()) {
					fstr += f.name() + ":" + f.getBuildings().size() + ", ";
				}
				g.drawString(fstr, x + 200, y);
			}
		}
	}

	private void drawStuff(Graphics g, int w, int h) {
		if (game == null) {
			return;
		}
		
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setColor(game.getBackgroundColor());
		g.fillRect(0, 0, w, h);
		drawGame(g);
	}
	
	private void drawGame(Graphics g) {
		if (game.world == null) {
			g.drawString("No World to display", 20, 20);
			return;
		}
		long startTime = System.currentTimeMillis();
		g.translate(-frozenViewOffset.getIntX(), -frozenViewOffset.getIntY());
		draw(g, canvas.getWidth(), canvas.getHeight());
		g.translate(frozenViewOffset.getIntX(), frozenViewOffset.getIntY());
		long endTime = System.currentTimeMillis();
		drawTime = endTime - startTime;
//		Toolkit.getDefaultToolkit().sync();
	}

	private void draw(Graphics g, int panelWidth, int panelHeight) {
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntX() - 2);
		int lowerY = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntY() - 2);
		int upperX = Math.min(game.world.getWidth(), lowerX + panelWidth / frozenTileSize + 4);
		int upperY = Math.min(game.world.getHeight(), lowerY + panelHeight / frozenTileSize + 4);

		if (frozenTileSize < FAST_MODE_TILE_SIZE) {
			if (state.showHeightMap) {
				g.drawImage(heightMapImage, 0, 0, frozenTileSize * game.world.getWidth(), frozenTileSize * game.world.getHeight(),
						null);
			}else if (state.showPressureMap) {
				g.drawImage(pressureMapImage, 0, 0, frozenTileSize * game.world.getWidth(), frozenTileSize * game.world.getHeight(),
						null);
			}else if (state.showTemperatureMap) {
				g.drawImage(temperatureMapImage, 0, 0, frozenTileSize * game.world.getWidth(), frozenTileSize * game.world.getHeight(),
						null);
			} else if (state.showHumidityMap) {
				g.drawImage(humidityMapImage, 0, 0, frozenTileSize * game.world.getWidth(), frozenTileSize * game.world.getHeight(),
						null);
			} else {
				g.drawImage(terrainImage, 0, 0, frozenTileSize * game.world.getWidth(), frozenTileSize * game.world.getHeight(),
						null);
			}
		} else {
			double highHeight = Double.MIN_VALUE;
			double lowHeight = Double.MAX_VALUE;
			double highPressure = Double.MIN_VALUE;
			double lowPressure = Double.MAX_VALUE;
			double highTemp = Double.MIN_VALUE;
			double lowTemp = Double.MAX_VALUE;
			double highHumidity = Double.MIN_VALUE;
			double lowHumidity = Double.MAX_VALUE;
			if (state.showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						highHeight = Math.max(highHeight, tile.getHeight());
						lowHeight = Math.min(lowHeight, tile.getHeight());

					}
				}
			}else if (state.showPressureMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						highPressure = Math.max(highPressure, tile.getAir().getPressure());
						lowPressure = Math.min(lowPressure, tile.getAir().getPressure());

					}
				}
			}else if (state.showTemperatureMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						highTemp = Math.max(highTemp, tile.getAir().getTemperature());
						lowTemp = Math.min(lowTemp, tile.getAir().getTemperature());

					}
				}
			}else if (state.showHumidityMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						highHumidity = Math.max(highHumidity, tile.getAir().getHumidity());
						lowHumidity = Math.min(lowHumidity, tile.getAir().getHumidity());

					}
				}

			}

			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile tile = game.world.get(new TileLoc(i, j));
					if (tile == null) {
						continue;
					}
					drawTile((Graphics2D) g, tile, lowHeight, highHeight, lowHumidity, highHumidity, lowPressure, highPressure, lowTemp, highTemp);
				}
			}

			drawHoveredTiles((Graphics2D) g);
			drawPlannedThing((Graphics2D) g);
			drawSelectedThings((Graphics2D) g, lowerX, lowerY, upperX, upperY);

			for (Building building : game.world.getBuildings()) {
				drawInventory(g, null, building);
				drawHealthBar(g, building);
				drawHitsplat(g, building);
			}
			for (Plant plant : game.world.getPlants()) {
				drawHealthBar(g, plant);
				drawHitsplat(g, plant);
			}
			for (Unit unit : game.world.getUnits()) {
				drawInventory(g, unit, null);
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
//				Point drawAt = getDrawingCoords(unit.getTile().getLocation());
//				int draww = frozenTileSize;
//				int drawh = frozenTileSize;
//				drawUnit(unit, g, drawAt.x, drawAt.y, draww, drawh);
			}

			for (Projectile p : game.world.getData().getProjectiles()) {
				int extra = (int) (frozenTileSize * p.getExtraSize());
				double ratio = 0.5 * p.getHeight() / p.getMaxHeight();
				int shadowOffset = (int) (frozenTileSize * ratio / 2);
				Point drawAt = getDrawingCoords(p.getTile().getLocation());
				
				g.drawImage(p.getShadow(0), drawAt.x + shadowOffset,
						drawAt.y + shadowOffset, frozenTileSize - shadowOffset * 2,
						frozenTileSize - shadowOffset * 2, null);
				g.drawImage(p.getImage(0), drawAt.x - extra / 2,
						drawAt.y - p.getHeight() - extra / 2, frozenTileSize + extra,
						frozenTileSize + extra, null);
			}
			for (WeatherEvent w : game.world.getWeatherEvents()) {
				
				Point drawAt = getDrawingCoords(w.getTile().getLocation());
				g.drawImage(w.getImage(0), drawAt.x ,
						drawAt.y, frozenTileSize, frozenTileSize, null);
			}

			int indicatorSize = frozenTileSize / 12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for (Unit unit : game.world.getUnits()) {
				int count = 0;
				if (visited.containsKey(unit.getTile())) {
					count = visited.get(unit.getTile());
				}
				visited.put(unit.getTile(), count + 1);

				// draws a square for every player unit on the tile
				Point drawAt = getDrawingCoords(unit.getTile().getLocation());
				int xx = drawAt.x + offset;
				int yy = drawAt.y + (indicatorSize + offset) * count + offset;
				g.setColor(unit.getFaction().color());
				g.fillRect(xx, yy, indicatorSize, indicatorSize);
				g.setColor(Color.BLACK);
				g.drawRect(xx, yy, indicatorSize, indicatorSize);
				count++;
			}

			if (!state.showHeightMap && !state.showHumidityMap && !state.showPressureMap && !state.showTemperatureMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null)
							continue;
						double brightness = World.getDaylight() + tile.getBrightness(state.faction);
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int) (255 * (1 - brightness))));
						Point drawAt = getDrawingCoords(tile.getLocation());
						g.fillRect(drawAt.x, drawAt.y, frozenTileSize, frozenTileSize);
					}
				}
			}

			if (state.drawDebugStrings) {
				if (frozenTileSize >= 150) {
					drawDebugStrings(g, lowerX, lowerY, upperX, upperY);
				}
			}
			if (state.leftClickAction == LeftClickAction.ATTACK) {
				drawTarget(g, state.hoveredTile);
			}
		}
	}

	private void drawSelectedThings(Graphics2D g, int lowerX, int lowerY, int upperX, int upperY) {
		for (Thing thing : state.selectedThings) {
			// draw selection circle
			g.setColor(Utils.getTransparentColor(state.faction.color(), 150));
//			Utils.setTransparency(g, 0.8f);
			Stroke currentStroke = g.getStroke();
			int strokeWidth = frozenTileSize / 12;
			g.setStroke(new BasicStroke(strokeWidth));
			Point drawAt = getDrawingCoords(thing.getTile().getLocation());
			g.drawOval(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, frozenTileSize - 1 - strokeWidth,
					frozenTileSize - 1 - strokeWidth);
			g.setStroke(currentStroke);
//			Utils.setTransparency(g, 1f);

			// draw spawn location for buildings
			if (thing instanceof Building) {
				Building building = (Building) thing;
				if (building.getSpawnLocation() != building.getTile()) {
					drawAt = getDrawingCoords(building.getSpawnLocation().getLocation());
					g.drawImage(RALLY_POINT_IMAGE, drawAt.x, drawAt.y, frozenTileSize, frozenTileSize, null);
				}
				
				int range = building.getType().getVisionRadius();
				if (range > 1) {
					// draws the range for buildings
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile t = game.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							drawAt = getDrawingCoords(t.getLocation());
							if (t.getLocation().distanceTo(building.getTile().getLocation()) <= range) {
								g.setColor(Color.BLACK);
								Utils.setTransparency(g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(building.getTile().getLocation()) > range) {
										drawBorderBetween(g, t.getLocation(), tile.getLocation());
									}
								}
								Utils.setTransparency(g, 1);
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
					drawTarget(g, target.getTile().getLocation());
				}
				// draw path
				LinkedList<Tile> path = unit.getCurrentPath();
				if (path != null) {
					g.setColor(Color.green);
					TileLoc prev = unit.getTile().getLocation();
					Point prevDrawAt = getDrawingCoords(prev);
					for (Tile t : path) {
						drawAt = getDrawingCoords(t.getLocation());
						if (prev != null) {
							g.drawLine(prevDrawAt.x + frozenTileSize / 2, prevDrawAt.y + frozenTileSize / 2,
									drawAt.x + frozenTileSize / 2, drawAt.y + frozenTileSize / 2);
						}
						prev = t.getLocation();
						prevDrawAt = drawAt;
					}
				}
				// draw destination flags
				for (PlannedAction plan : unit.actionQueue) {
					Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
					drawAt = getDrawingCoords(targetTile.getLocation());
					g.drawImage(FLAG, drawAt.x, drawAt.y, frozenTileSize, frozenTileSize, null);
				}
				int range = unit.getMaxRange();
				if (range > 1) {
					// draws the attack range for units
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile t = game.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							drawAt = getDrawingCoords(t.getLocation());
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
		if (state.leftClickAction == LeftClickAction.PLAN_BUILDING) {
			bI = Utils.toBufferedImage(state.selectedBuildingToPlan.getImage(frozenTileSize));
		} else if (state.leftClickAction == LeftClickAction.SPAWN_THING) {
			bI = Utils.toBufferedImage(state.selectedThingToSpawn.getImage(frozenTileSize));
		}
		if (bI != null) {
			Utils.setTransparency(g, 0.5f);
			Point drawAt = getDrawingCoords(state.hoveredTile);
			g.drawImage(bI, drawAt.x, drawAt.y, frozenTileSize, frozenTileSize, null);
			Utils.setTransparency(g, 1f);
		}
	}

	private void drawDebugStrings(Graphics g, int lowerX, int lowerY, int upperX, int upperY) {
		if(upperX - lowerX <= 0 || upperY - lowerY <= 0) {
			return;
		}
		int[][] rows = new int[upperX - lowerX][upperY - lowerY];
		int fontsize = frozenTileSize / 4;
		fontsize = Math.min(fontsize, 13);
		Font font = new Font("Consolas", Font.PLAIN, fontsize);
		g.setFont(font);
		for (int i = lowerX; i < upperX; i++) {
			for (int j = lowerY; j < upperY; j++) {
				Tile tile = game.world.get(new TileLoc(i, j));
				Point drawAt = getDrawingCoords(tile.getLocation());
				List<String> strings = new LinkedList<String>();
				strings.add(String.format("H=%." + NUM_DEBUG_DIGITS + "f", tile.getHeight()));
				strings.add(String.format("PRE" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getPressure()));
				strings.add(String.format("TEMP" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getTemperature()));
				strings.add(String.format("HUM" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getHumidity()));
				
				strings.add(String.format("EVAP" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getEvaporation()));
				strings.add(String.format("dVOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getVolumeChange()));
				strings.add(String.format("VOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getVolume()));
				strings.add(String.format("MVOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getMaxVolume()));
				
				strings.add(String.format("RH" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getRelativeHumidity()));
				strings.add(String.format("DEW" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getDewPoint()));
				strings.add(String.format("ENE" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getEnergy()));
				strings.add(String.format("MASS" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getMass()));
				if (tile.getResource() != null) {
					strings.add(String.format("ORE" + "=%d", tile.getResource().getYield()));
				}

				if (tile.liquidType != LiquidType.DRY) {
					strings.add(String.format(tile.liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f",
							tile.liquidAmount));
				}

				if (tile.getModifier() != null) {
					strings.add("GM=" + tile.getModifier().timeLeft());
				}
				rows[i - lowerX][j - lowerY] = tile.drawDebugStrings(g, strings, rows[i - lowerX][j - lowerY], fontsize,
						frozenTileSize, drawAt);

				for (Unit unit : tile.getUnits()) {
					rows[i - lowerX][j - lowerY] = tile.drawDebugStrings(g, unit.getDebugStrings(),
							rows[i - lowerX][j - lowerY], fontsize, frozenTileSize, drawAt);
				}
				if (tile.getPlant() != null) {
					rows[i - lowerX][j - lowerY] = tile.drawDebugStrings(g, tile.getPlant().getDebugStrings(),
							rows[i - lowerX][j - lowerY], fontsize, frozenTileSize, drawAt);
				}
				if (tile.hasBuilding()) {
					rows[i - lowerX][j - lowerY] = tile.drawDebugStrings(g, tile.getBuilding().getDebugStrings(),
							rows[i - lowerX][j - lowerY], fontsize, frozenTileSize, drawAt);
				}
				if (tile.getRoad() != null) {
					rows[i - lowerX][j - lowerY] = tile.drawDebugStrings(g, tile.getRoad().getDebugStrings(),
							rows[i - lowerX][j - lowerY], fontsize, frozenTileSize, drawAt);
				}
			}
		}
	}

	private void drawHoveredTiles(Graphics2D g) {
		int strokeWidth = frozenTileSize / 10;
		strokeWidth = strokeWidth < 1 ? 1 : strokeWidth;
		Stroke stroke = g.getStroke();
		g.setStroke(new BasicStroke(strokeWidth));
		g.setColor(new Color(0, 0, 0, 64));
		if (state.leftMouseDown && state.draggingMouse && state.boxSelect[0] != null && state.boxSelect[1] != null) {
			Position[] box = Utils.normalizeRectangle(state.boxSelect[0], state.boxSelect[1]);
			for (Tile tile : Utils.getTilesBetween(game.world, box[0], box[1])) {
				Point drawAt = getDrawingCoords(tile.getLocation());
				g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, frozenTileSize - strokeWidth,
						frozenTileSize - strokeWidth);
			}
		} else {
			if (game.world.get(state.hoveredTile) != null) {
				Point drawAt = getDrawingCoords(state.hoveredTile);
				g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, frozenTileSize - strokeWidth,
						frozenTileSize - strokeWidth);
			}
		}
		g.setStroke(stroke);
	}

	private Point getDrawingCoords(TileLoc tileLoc) {
		int x = tileLoc.x() * frozenTileSize;
		int y = tileLoc.y() * frozenTileSize + (tileLoc.x() % 2) * frozenTileSize / 2;
		return new Point(x, y);
	}

	private void drawTile(Graphics2D g, Tile theTile, double lowHeight, double highHeight, double lowHumidity,
			double highHumidity, double lowPressure, double highPressure, double lowTemp, double highTemp) {
		Point drawAt = getDrawingCoords(theTile.getLocation());
		int draww = frozenTileSize;
		int drawh = frozenTileSize;
		int imagesize = draww < drawh ? draww : drawh;

		if (state.showHeightMap) {
			float heightRatio = (float) ((theTile.getHeight() - lowHeight) / (highHeight - lowHeight));
			int r = Math.max(Math.min((int) (255 * heightRatio), 255), 0);
			g.setColor(new Color(r, 0, 255 - r));
			g.fillRect(drawAt.x, drawAt.y, draww, drawh);
		} else if (state.showPressureMap) {
			float pressureRatio = (float) ((theTile.getAir().getPressure() - lowPressure)
					/ (highPressure - lowPressure));
			int r = Math.max(Math.min((int) (255 * pressureRatio), 255), 0);
			g.setColor(new Color(r, 0, 255 - r));
			g.fillRect(drawAt.x, drawAt.y, draww, drawh);
		} else if (state.showTemperatureMap) {
			float tempRatio = (float) ((theTile.getAir().getTemperature() - lowTemp) / (highTemp - lowTemp));
			int r = Math.max(Math.min((int) (255 * tempRatio), 255), 0);
			g.setColor(new Color(r, 0, 255 - r));
			g.fillRect(drawAt.x, drawAt.y, draww, drawh);
			if (theTile.getAir().getTemperature() <= World.FREEZETEMP) {
				g.drawImage(SNOW, drawAt.x, drawAt.y, draww, drawh, null);
			}
		} else if (state.showHumidityMap) {
			float humidityRatio = (float) ((theTile.getAir().getHumidity() - lowHumidity)
					/ (highHumidity - lowHumidity));
			int r = Math.max(Math.min((int) (255 * humidityRatio), 255), 0);
			g.setColor(new Color(r, 0, 255 - r));
			g.fillRect(drawAt.x, drawAt.y, draww, drawh);
		} else {
			g.drawImage(theTile.getTerrain().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh, null);
//			t.drawEntities(g, currentMode);

			if (theTile.getResource() != null && state.faction.areRequirementsMet(theTile.getResource().getType())) {
				g.drawImage(theTile.getResource().getType().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh,
						null);
			}

			if (theTile.getFaction() != null && theTile.getFaction() != game.world.getFaction(World.NO_FACTION_ID)) {
				g.setColor(theTile.getFaction().borderColor());
				for (Tile tile : theTile.getNeighbors()) {
					if (tile.getFaction() != theTile.getFaction()) {
						drawBorderBetween(g, theTile.getLocation(), tile.getLocation());
					}
				}
			}
//			if(game.world.borderTerritory.containsKey(theTile)) {
//				Utils.setTransparency(g, 1);
//				g.setColor(Color.BLACK);
//				g.fillRect(drawAt.x, drawAt.y, draww, drawh); 
//			}
			if (theTile.getRoad() != null) {
				drawBuilding(theTile.getRoad(), g, drawAt.x, drawAt.y, draww, drawh);
			}

			if (theTile.liquidType != LiquidType.DRY) {
				double alpha = Utils.getAlphaOfLiquid(theTile.liquidAmount);
//				 transparency liquids
				Utils.setTransparency(g, alpha);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawAt.x, drawAt.y, draww, drawh);
				Utils.setTransparency(g, 1);

				int imageSize = (int) Math.min(Math.max(draww * theTile.liquidAmount / 20, 1), draww);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawAt.x + draww / 2 - imageSize / 2, drawAt.y + drawh / 2 - imageSize / 2, imageSize,
						imageSize);
				g.drawImage(theTile.liquidType.getImage(imagesize), drawAt.x + draww / 2 - imageSize / 2,
						drawAt.y + draww / 2 - imageSize / 2, imageSize, imageSize, null);
			}

			if (theTile.getModifier() != null) {
				Utils.setTransparency(g, 0.9);
				g.drawImage(theTile.getModifier().getType().getImage(imagesize), drawAt.x, drawAt.y, draww, drawh,
						null);
				Utils.setTransparency(g, 1);
			}

			if (!theTile.getItems().isEmpty()) {
				for (Item item : theTile.getItems()) {
					g.drawImage(item.getType().getImage(imagesize), drawAt.x + frozenTileSize / 4,
							drawAt.y + frozenTileSize / 4, frozenTileSize / 2, frozenTileSize / 2, null);
				}
			}
			if (theTile.getPlant() != null) {
				g.drawImage(theTile.getPlant().getHasImage().getImage(frozenTileSize), drawAt.x, drawAt.y, draww, drawh, null);
			}

			if (theTile.getBuilding() != null) {
				if (theTile.getBuilding().isSelected()) {
					g.drawImage(theTile.getBuilding().getHasImage().getHighlight(frozenTileSize), drawAt.x, drawAt.y, draww, drawh,
							null);
				}
				drawBuilding(theTile.getBuilding(), g, drawAt.x, drawAt.y, draww, drawh);
			}
			for (Unit unit : theTile.getUnits()) {
				drawUnit(unit, g, drawAt.x, drawAt.y, draww, drawh);
			}
		}
		
	}

	private void drawUnit(Unit unit, Graphics g, int drawx, int drawy, int draww, int drawh) {
		
//		LinkedList<Tile> path = unit.getCurrentPath();
//		double timeLeft = unit.getTimeToMove();
//		double timeStart = unit.getCombatStats().getMoveSpeed();
//		double percent = 0;
//		if(timeStart != 0) {
//			percent = timeLeft / timeStart;
//		}
		
		if (unit.isSelected()) {
			g.drawImage(unit.getHasImage().getHighlight(frozenTileSize), drawx, drawy, draww, drawh, null);
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
			g.drawImage(unit.getHasImage().getImage(frozenTileSize), drawx, drawy, draww, drawh, null);
//		}
		
		
		if (unit.isGuarding() == true) {
			g.drawImage(GUARD_ICON, drawx + draww / 4, drawy + drawh / 4, draww / 2, drawh / 2, null);
		}
		if (unit.getAutoBuild() == true) {
			g.drawImage(AUTO_BUILD_ICON, drawx + draww / 4, drawy + drawh / 4, draww / 2, drawh / 2,
					null);
		}
	}
	
	
	private void drawBuilding(Building building, Graphics g, int drawx, int drawy, int draww, int drawh) {

		BufferedImage bI = Utils.toBufferedImage(building.getHasImage().getImage(0));
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
			g.drawImage(BUILD_ICON, drawx + frozenTileSize / 4, drawy + frozenTileSize / 4, draww * 3 / 4, drawh * 3 / 4, null);
		} else {
			g.drawImage(bI, drawx, drawy, draww, drawh, null);
		}
	}

	private void drawTarget(Graphics g, TileLoc tileLoc) {
		Point drawAt = getDrawingCoords(tileLoc);
		int w = (int) (frozenTileSize * 8 / 10);
		int hi = (int) (frozenTileSize * 8 / 10);
		g.drawImage(TARGET_IMAGE, drawAt.x + frozenTileSize * 1 / 10, drawAt.y + frozenTileSize * 1 / 10, w, hi, null);
	}

	private void drawInventory(Graphics g, Unit unit, Building building) {
		if (frozenTileSize <= 30) {
			return;
		}
		int numDrawn = 0;
		if (building != null) {
			Point drawAt = getDrawingCoords(building.getTile().getLocation());
			for (Item item : building.getInventory().getItems()) {
				if (item != null) {
					g.drawImage(item.getType().getImage(frozenTileSize/4), drawAt.x+(frozenTileSize/4*numDrawn), drawAt.y, null);
					numDrawn ++;
				}
			}
		}
		if (unit != null) {
			Point drawAt = getDrawingCoords(unit.getTile().getLocation());
			for (Item item : unit.getInventory().getItems()) {
				if (item != null) {
					g.drawImage(item.getType().getImage(frozenTileSize), drawAt.x+(frozenTileSize/4*numDrawn), drawAt.y, null);
					numDrawn ++;
				}
			}
		}

	}

	private void drawHealthBar(Graphics g, Thing thing) {
		if (frozenTileSize <= 30) {
			return;
		}
		if (World.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(state.hoveredTile)) {
			Point drawAt = getDrawingCoords(thing.getTile().getLocation());
			int w = frozenTileSize - 1;
			int h = frozenTileSize / 4 - 1;
			drawHealthBar2(g, thing, drawAt.x + 1, drawAt.y + 1, w, h, 2, thing.getHealth() / thing.getMaxHealth());
		}
	}

	public static void drawHealthBar2(Graphics g, Thing thing, int x, int y, int w, int h, int thickness,
			double ratio) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);

		g.setColor(Color.RED);
		g.fillRect(x + thickness, y + thickness, w - thickness * 2, h - thickness * 2);

		int greenBarWidth = (int) (ratio * (w - thickness * 2));
		g.setColor(Color.GREEN);
		g.fillRect(x + thickness, y + thickness, greenBarWidth, h - thickness * 2);
	}

	private void drawHitsplat(Graphics g, Thing thing) {

		Point drawAt = getDrawingCoords(thing.getTile().getLocation());
		int splatWidth = (int) (frozenTileSize * .5);
		int splatHeight = (int) (frozenTileSize * .5);

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
				x = (int) ((drawAt.x) + frozenTileSize * 0.5);
				y = (int) ((drawAt.y) + frozenTileSize * 0.5);
			}
			if (i == 2) {
				x = (int) ((drawAt.x) + frozenTileSize * 0.5);
				y = (int) ((drawAt.y));
			}
			if (i == 3) {
				x = (int) ((drawAt.x));
				y = (int) ((drawAt.y) + frozenTileSize * 0.5);
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

			int fontSize = frozenTileSize / 4;
			g.setFont(new Font(DAMAGE_FONT.getFontName(), Font.BOLD, fontSize));
			int width = g.getFontMetrics().stringWidth(text);
			g.setColor(Color.WHITE);
			g.drawString(text, x + splatWidth / 2 - width / 2, (int) (y + fontSize * 1.5));
		}
	}

	private void drawBorderBetween(Graphics2D g, TileLoc one, TileLoc two) {
		int width = frozenTileSize / 8;
		Point drawAt = getDrawingCoords(one);
		if (one.x() == two.x()) {
			if (one.y() > two.y()) {
				g.fillRect(drawAt.x, drawAt.y, frozenTileSize, width);
			}
			if (one.y() < two.y()) {
				g.fillRect(drawAt.x, drawAt.y + frozenTileSize - width, frozenTileSize, width);
			}
		} else {
			if (one.y() > two.y()) {
				int yoffset = (one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + frozenTileSize - width, drawAt.y + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, frozenTileSize / 2);
				}
			} else if (one.y() < two.y()) {
				int yoffset = (one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + frozenTileSize - width, drawAt.y + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, frozenTileSize / 2);
				}
			} else {
				int yoffset = (1 - one.x() % 2) * frozenTileSize / 2;
				if (one.x() < two.x()) {
					g.fillRect(drawAt.x + frozenTileSize - width, drawAt.y + yoffset, width, frozenTileSize / 2);
				} else if (one.x() > two.x()) {
					g.fillRect(drawAt.x, drawAt.y + yoffset, width, frozenTileSize / 2);
				}
			}
		}
	}
	
	/**
	 * 
	 * @return size 4 array of positions on the map of the bounds of the tiles that are in view
	 */
	public Position[] getVisibleTileBounds() {
		if (game.world == null) {
			return null;
		}
		Position offsetTile = Utils.getWorldCoordOfPixel(new Position(0, 0), state.viewOffset, state.tileSize);
		Position offsetTilePlusCanvas = Utils.getWorldCoordOfPixel(
				new Position(canvas.getWidth(), canvas.getHeight()), state.viewOffset, state.tileSize);
		return new Position[] {
				offsetTile,
				new Position(offsetTile.x, offsetTilePlusCanvas.y),
				offsetTilePlusCanvas,
				new Position(offsetTilePlusCanvas.x, offsetTile.y)
		};
	}

	@Override
	public Position getWorldCoordOfPixel(Point pixelOnScreen, Position viewOffset, int tileSize) {
		double column = ((pixelOnScreen.x + viewOffset.x) / tileSize);
		int row = (int) ((pixelOnScreen.y + viewOffset.y - (column % 2) * tileSize / 2) / tileSize);
		return new Position(column, row);
	}

	@Override
	public void zoomView(int scroll, int mx, int my) {
		int newTileSize;
		if (scroll > 0) {
			newTileSize = (int) ((state.tileSize - 1) * 0.95);
		} else {
			newTileSize = (int) ((state.tileSize + 1) * 1.05);
		}
		zoomViewTo(newTileSize, mx, my);
	}

	@Override
	public void zoomViewTo(int newTileSize, int mx, int my) {
		if (newTileSize > 0) {
			Position tile = Utils.getWorldCoordOfPixel(new Position(mx, my), state.viewOffset, state.tileSize);
			state.tileSize = newTileSize;
			Position focalPoint = tile.multiply(state.tileSize).subtract(state.viewOffset);
			state.viewOffset.x -= mx - focalPoint.x;
			state.viewOffset.y -= my - focalPoint.y;
		}
		canvas.repaint();
	}

	@Override
	public void shiftView(int dx, int dy) {
		state.viewOffset.x += dx;
		state.viewOffset.y += dy;
		canvas.repaint();
	}
	@Override
	public void rotateView(int dx, int dy) {
		shiftView(dx, dy);
	}

	private static Rectangle normalizeRectangle(Point one, Point two) {
		int x = Math.min(one.x, two.x);
		int y = Math.min(one.y, two.y);
		int width = Math.abs(one.x - two.x);
		int height = Math.abs(one.y - two.y);
		return new Rectangle(x, y, width, height);
	}

}
