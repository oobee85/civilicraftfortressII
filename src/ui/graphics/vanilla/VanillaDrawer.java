package ui.graphics.vanilla;

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import game.actions.*;
import game.components.*;
import ui.*;
import ui.graphics.*;
import ui.utils.DrawingUtils;
import ui.view.GameView.*;
import utils.*;
import world.*;
import world.liquid.*;

public class VanillaDrawer extends Drawer {

	private static final int FAST_MODE_TILE_SIZE = 10;
	private static final int NUM_DEBUG_DIGITS = 3;
	
	private static final Font DAMAGE_FONT = new Font("Comic Sans MS", Font.BOLD, 14);
	
	private static final Image RALLY_POINT_IMAGE = Utils.loadImage("Images/interfaces/queuelocation.png");
	private static final Image TARGET_IMAGE = Utils.loadImage("Images/interfaces/ivegotyouinmysights.png");
	private static final Image FLAG = Utils.loadImage("Images/interfaces/flag.png");
	private static final Image BUILD_ICON = Utils.loadImage("Images/interfaces/building.gif");
	private static final Image HARVEST_ICON = Utils.loadImage("Images/interfaces/harvest.png");
	private static final Image RED_HITSPLAT = Utils.loadImage("Images/interfaces/redhitsplat.png");
	private static final Image BLUE_HITSPLAT = Utils.loadImage("Images/interfaces/bluehitsplat.png");
	private static final Image GREEN_HITSPLAT = Utils.loadImage("Images/interfaces/greenhitsplat.png");
	private static final Image SNOW = Utils.loadImage("Images/weather/snow.png");
	private static final Image SNOW2 = Utils.loadImage("Images/weather/snow2.png");
	private static final Image SKY_BACKGROUND = Utils.loadImage("Images/lightbluesky.png");

	
	private JPanel canvas;
	
	private static final int NUM_BUFFERS = 3;
	private volatile BufferedImage[] buffers = new BufferedImage[NUM_BUFFERS];
	private volatile Position[] drawnAtOffset = new Position[NUM_BUFFERS];
	private volatile int[] bufferDrawTimes = new int[NUM_BUFFERS];
	private volatile int[] drawnAtTileSize = new int[NUM_BUFFERS];
	private volatile int currentBuffer = 0;
	private Semaphore nextRequested = new Semaphore(1);
	private Semaphore numAvailable = new Semaphore(0);
	
	// must take snapshot of current tile size and view offset to draw a consistent image
	// otherwise while player is dragging view it will change view offset 
	// while it is drawing which causes a bunch of issues.
	private int frozenTileSize;
	private Position frozenViewOffset = new Position(0, 0);
	
	private RenderingPipeline[] pipelines = new RenderingPipeline[MapMode.values().length];
	
	public VanillaDrawer(Game game, GameViewState state) {
		super(game, state);
		for (MapMode mode : MapMode.values()) {
			pipelines[mode.ordinal()] = RenderingPipeline.getRenderingPipeline(mode);
		}
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
				Utils.setTransparency(g, World.getDaylight());
				g.drawImage(SKY_BACKGROUND, -state.viewOffset.getIntX()/20 - 100, -state.viewOffset.getIntY()/20 - 100, null);
				Utils.setTransparency(g, 1);
				
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
						long startDrawing = System.currentTimeMillis();
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
						long finishedDrawing = System.currentTimeMillis();
						bufferDrawTimes[next] = (int) (finishedDrawing - startDrawing);
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
		if (!Settings.CINEMATIC) {
			g.setFont(KUIConstants.infoFont);
			int x = 10;
			int y = canvas.getHeight() - 5;
			if(Settings.SHOW_FPS) {
				int yOffset = KUIConstants.infoFont.getSize() + 2;
				int avgBufferDrawTime = 0;
				for (int i = 0; i < bufferDrawTimes.length; i++) {
					avgBufferDrawTime += bufferDrawTimes[i];
				}
				avgBufferDrawTime /= 3;
				DrawingUtils.drawStringWithShadow(g, String.format("DRAW(ms): %d", avgBufferDrawTime), x, y);
				DrawingUtils.drawStringWithShadow(g, String.format("TICK(ms): %d", state.previousTickTime), x, y - yOffset);
			}
			if (Settings.SHOW_BUILDING_COUNTS) {
				String fstr = "";
				for (Faction f : game.world.getFactions()) {
					fstr += f.name() + ":" + f.getBuildings().size() + ", ";
				}
				DrawingUtils.drawStringWithShadow(g, fstr, x + 200, y);
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
		Utils.clearBufferedImageTo(((Graphics2D)g), new Color(0, 0, 0, 0), w, h);
		drawGame(g);
	}
	
	private void drawGame(Graphics g) {
		if (game.world == null) {
			g.drawString("No World to display", 20, 20);
			return;
		}
		g.translate(-frozenViewOffset.getIntX(), -frozenViewOffset.getIntY());
		draw(g, canvas.getWidth(), canvas.getHeight());
		g.translate(frozenViewOffset.getIntX(), frozenViewOffset.getIntY());
//		Toolkit.getDefaultToolkit().sync();
	}
	

	private void draw(Graphics g, int panelWidth, int panelHeight) {
		// Start by drawing plain terrain image
		g.drawImage(mapImages[state.mapMode.ordinal()], 0, 0, 
				frozenTileSize * game.world.getWidth(), 
				frozenTileSize * game.world.getHeight() + frozenTileSize/2, null);
		
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntX() - 2);
		int lowerY = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntY() - 2);
		int upperX = Math.min(game.world.getWidth(), lowerX + panelWidth / frozenTileSize + 4);
		int upperY = Math.min(game.world.getHeight(), lowerY + panelHeight / frozenTileSize + 4);

		if (frozenTileSize >= FAST_MODE_TILE_SIZE && state.mapMode != MapMode.LIGHT) {

			RenderingState renderState = new RenderingState();
			renderState.mapMode = state.mapMode;
			renderState.g = (Graphics2D) g;
			renderState.faction = state.faction;
			renderState.tileSize = frozenTileSize;
			renderState.draww = frozenTileSize;
			renderState.drawh = frozenTileSize;

			for (RenderingStep step : pipelines[state.mapMode.ordinal()].steps) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						renderState.tile = tile;
						Point drawAt = getDrawingCoords(tile.getLocation());
						renderState.drawx = drawAt.x;
						renderState.drawy = drawAt.y;
						
						step.render(renderState);
					}
				}
			}

			drawHoveredTiles((Graphics2D) g);
			drawPlannedThing((Graphics2D) g);
			drawSelectedThings((Graphics2D) g, lowerX, lowerY, upperX, upperY);

			for (Building building : game.world.getBuildings()) {
				if(building.hasInventory())
					drawInventory(g, building.getTile(), building.getInventory());
				drawHealthBar(g, building);
				drawHitsplat(g, building);
			}
			for (Plant plant : game.world.getPlants()) {
				drawHealthBar(g, plant);
				drawHitsplat(g, plant);
			}
			for (Unit unit : game.world.getUnits()) {
				if(unit.hasInventory())
					drawInventory(g, unit.getTile(), unit.getInventory());
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
			}

			for (Projectile p : game.world.getData().getProjectiles()) {
				int extra = (int) (frozenTileSize * p.getExtraSize());
				double ratio = 0.5 * p.getHeight() / p.getMaxHeight();
				int shadowOffset = (int) (frozenTileSize * ratio / 2);
				Point drawAt = getDrawingCoords(p.getTile().getLocation());
				
				g.drawImage(p.getType().getMipMap().getShadow(0), drawAt.x + shadowOffset,
						drawAt.y + shadowOffset, frozenTileSize - shadowOffset * 2,
						frozenTileSize - shadowOffset * 2, null);
				g.drawImage(p.getType().getMipMap().getImage(0), drawAt.x - extra / 2,
						drawAt.y - p.getHeight() - extra / 2, frozenTileSize + extra,
						frozenTileSize + extra, null);
			}
			for (WeatherEvent w : game.world.getWeatherEvents()) {
				
				Point drawAt = getDrawingCoords(w.getTile().getLocation());
				g.drawImage(WeatherEventType.RAIN.getMipMap().getImage(0), drawAt.x ,
						drawAt.y, frozenTileSize, frozenTileSize, null);
			}

			int indicatorSize = frozenTileSize / 12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for (Unit unit : game.world.getUnits()) {
				int count = visited.getOrDefault(unit.getTile(), 0);
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
			
			// draw brightness of tiles as translucent rectangle
			if (state.mapMode == MapMode.TERRAIN_BIG) {
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
			if (state.mapMode == MapMode.FLOW) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null)
							continue;
						drawAirFlow(g, tile, true);
					}
				}
			}
			if (state.mapMode == MapMode.FLOW2) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null)
							continue;
						drawAirFlow(g, tile, false);
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
					try {
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
					catch (ConcurrentModificationException e) {
						System.err.println("Concurrent modification while drawing path.");
					}
				}
				// draw destination flags
				for (PlannedAction plan : unit.actionQueue) {
					Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
					drawAt = getDrawingCoords(targetTile.getLocation());
					g.drawImage(FLAG, drawAt.x, drawAt.y, frozenTileSize, frozenTileSize, null);
				}
				int range = unit.getMaxAttackRange();
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
			bI = Utils.toBufferedImage(state.selectedBuildingToPlan.getMipMap().getImage(frozenTileSize));
		} else if (state.leftClickAction == LeftClickAction.SPAWN_THING) {
			bI = Utils.toBufferedImage(Utils.getImageFromThingType(state.selectedThingToSpawn).getImage(frozenTileSize));
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
				strings.add(String.format("PRES" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getPressure()));

//				strings.add(String.format("HUM" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getHumidity()));
				strings.add(String.format("TTEM" + "=%." + NUM_DEBUG_DIGITS + "f", (tile.getTemperature() - Constants.FREEZETEMP)));
				strings.add(String.format("ATEM" + "=%." + NUM_DEBUG_DIGITS + "f", (tile.getAir().getTemperature() - Constants.FREEZETEMP)));
				strings.add(String.format("TENE" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getEnergy()));
				strings.add(String.format("AENE" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getEnergy()));
				
				
//				strings.add(String.format("EVAP" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getEvaporation()));
//				strings.add(String.format("dVOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getVolumeChange()));
				strings.add(String.format("VOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getVolumeLiquid()));
				strings.add(String.format("MVOL" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getMaxVolumeLiquid()));
				
//				strings.add(String.format("RH" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getRelativeHumidity()));
//				strings.add(String.format("DEW" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getDewPoint()));

//				strings.add(String.format("MASS" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getAir().getMass()));
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
			for (Tile tile : Utils.getTilesBetween(game.world, state.boxSelect[0], state.boxSelect[1])) {
				Point drawAt = getDrawingCoords(tile.getLocation());
				g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, frozenTileSize - strokeWidth,
						frozenTileSize - strokeWidth);
			}
		} else {
			if (game.world.get(state.hoveredTile) != null) {
				Point drawAt = getDrawingCoords(state.hoveredTile);
				g.drawRect(drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2, frozenTileSize - strokeWidth,
						frozenTileSize - strokeWidth);
				if(state.drawDebugStrings) {
					g.setStroke(stroke);
					g.setColor(Color.yellow);
					g.drawString(state.hoveredTile.toString(), drawAt.x + strokeWidth / 2, drawAt.y + strokeWidth / 2);
				}
			}
		}
		g.setStroke(stroke);
	}

	private Point getDrawingCoords(TileLoc tileLoc) {
		int x = tileLoc.x() * frozenTileSize;
		int y = tileLoc.y() * frozenTileSize + (tileLoc.x() % 2) * frozenTileSize / 2;
		return new Point(x, y);
	}

	private void drawTarget(Graphics g, TileLoc tileLoc) {
		Point drawAt = getDrawingCoords(tileLoc);
		int w = (int) (frozenTileSize * 8 / 10);
		int hi = (int) (frozenTileSize * 8 / 10);
		g.drawImage(TARGET_IMAGE, drawAt.x + frozenTileSize * 1 / 10, drawAt.y + frozenTileSize * 1 / 10, w, hi, null);
	}
	private void drawAirFlow(Graphics g, Tile tile, boolean arrows) {
		TileLoc tileLoc = tile.getLocation();
		Point drawAt = getDrawingCoords(tileLoc);
		int w = (int) (frozenTileSize * 8 / 10);
		int hi = (int) (frozenTileSize * 8 / 10);
		if(tile.getAir().getFlowDirection() != null) {
			Image image = arrows ? tile.getAir().getFlowDirection().getArrowImage() : tile.getAir().getFlowDirection().getImage() ;
//			System.out.println(tile.getAir().getFlowDirection());
			g.drawImage(image, drawAt.x, drawAt.y, frozenTileSize, frozenTileSize, null);
//			g.drawImage(TARGET_IMAGE, drawAt.x + frozenTileSize * 1 / 10, drawAt.y + frozenTileSize * 1 / 10, w, hi, null);
		}

	}

	private void drawInventory(Graphics g, Tile tile, Inventory inventory) {
		int draww = frozenTileSize/4;
		Point drawAt = getDrawingCoords(tile.getLocation());
		drawAt.x += draww/2;
		drawInventory(g, inventory, drawAt.x, drawAt.y, draww, draww);
	}
	
	private void drawInventory(Graphics g, Inventory inventory, int drawx, int drawy, int draww, int drawh) {
		if (frozenTileSize <= 20) {
			return;
		}
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

	private void drawHealthBar(Graphics g, Thing thing) {
		if (frozenTileSize <= 30) {
			return;
		}
		if (World.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(state.hoveredTile)) {
			Point drawAt = getDrawingCoords(thing.getTile().getLocation());
			int w = frozenTileSize - 1;
			int h = frozenTileSize / 7 - 1;
			int borderThickness = h / 8;
			drawHealthBar2(g, thing, drawAt.x + 1, drawAt.y + 1, w, h, borderThickness, thing.getHealth() / thing.getMaxHealth());
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
		Position offsetTile = getWorldCoordOfPixel(new Point(0, 0), state.viewOffset, state.tileSize);
		Position offsetTilePlusCanvas = getWorldCoordOfPixel(
				new Point(canvas.getWidth(), canvas.getHeight()), state.viewOffset, state.tileSize);
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
		int yoffset = ((int)column % 2) * tileSize / 2;
		double row = (pixelOnScreen.y + viewOffset.y - yoffset) / tileSize;
		return new Position(column, row);
	}
	public Position getWorldCoordOfPixelWithoutOffset(Point pixelOnScreen, Position viewOffset, int tileSize) {
		double column = ((pixelOnScreen.x + viewOffset.x) / tileSize);
		double row = (pixelOnScreen.y + viewOffset.y) / tileSize;
		return new Position(column, row);
	}
	@Override
	public Point getPixelOfWorldCoord(Position worldCoord, int tileSize) {
		int onScreenX = (int) (worldCoord.x*tileSize);
		int yoffset = ((int)worldCoord.x % 2) * tileSize / 2;
		int onScreenY = (int) (worldCoord.y*tileSize + yoffset);
		return new Point(onScreenX, onScreenY);
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
			Position tile = getWorldCoordOfPixelWithoutOffset(new Point(mx, my), state.viewOffset, state.tileSize);
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
