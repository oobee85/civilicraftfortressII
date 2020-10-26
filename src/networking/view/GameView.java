package networking.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import world.*;

public class GameView extends JPanel {

	public static int tileSize = 9;
	public static final int FAST_MODE_TILE_SIZE = 1;
	public static final int NUM_DEBUG_DIGITS = 3;

	private static final Image RALLY_POINT_IMAGE = Utils.loadImage("resources/Images/interfaces/queuelocation.png");
	private static final Image TARGET_IMAGE = Utils.loadImage("resources/Images/interfaces/ivegotyouinmysights.png");
	private static final Image FLAG = Utils.loadImage("resources/Images/interfaces/flag.png");
	
	
	
	private Game game;
	public Position viewOffset;
	private Point previousMouse;
	private boolean draggingMouse = false;
	
	
	public GameView(Game game) {
		this.game = game;
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
				game.mouseOver(getTileAtPixel(e.getPoint()));
				repaint();
				previousMouse = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Point currentMouse = e.getPoint();
				int dx = previousMouse.x - currentMouse.x;
				int dy = previousMouse.y - currentMouse.y;
				// Only drag if moved mouse at least 3 pixels away
				if(Math.abs(dx) + Math.abs(dy) >= 3) {
					draggingMouse = true;
					if (SwingUtilities.isLeftMouseButton(e)) {
						shiftView(dx, dy);
					}
					game.mouseOver(getTileAtPixel(currentMouse));
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
						game.rightClick(getTileAtPixel(currentMouse));
					}
					else if (SwingUtilities.isLeftMouseButton(e)) {
						game.leftClick(getTileAtPixel(currentMouse));
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
	}
	
	public void centerViewOn(Tile tile, int zoom, int panelWidth, int panelHeight) {
		tileSize = zoom;
		viewOffset.x = (tile.getLocation().x - panelWidth/2/tileSize) * tileSize + tileSize/2;
		viewOffset.y = (tile.getLocation().y - panelHeight/2/tileSize) * tileSize;
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
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g, panelWidth, panelHeight, viewOffset);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		if(World.PLAYER_FACTION.getResearchTarget() != null && !World.PLAYER_FACTION.getResearchTarget().isUnlocked()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * World.PLAYER_FACTION.getResearchTarget().getPointsSpent() / World.PLAYER_FACTION.getResearchTarget().getRequiredPoints();
			String progress = String.format(World.PLAYER_FACTION.getResearchTarget() + " %d/%d", World.PLAYER_FACTION.getResearchTarget().getPointsSpent(), World.PLAYER_FACTION.getResearchTarget().getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, panelWidth - panelWidth/3 - 4, 4, panelWidth/3, 30);
		}
		Toolkit.getDefaultToolkit().sync();
	}
	
public void draw(Graphics g, int panelWidth, int panelHeight, Position viewOffset) {
		
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, viewOffset.divide(GameView.tileSize).getIntX() - 2);
		int lowerY = Math.max(0, viewOffset.divide(GameView.tileSize).getIntY() - 2);
		int upperX = Math.min(game.world.getWidth(), lowerX + panelWidth/GameView.tileSize + 4);
		int upperY = Math.min(game.world.getHeight(), lowerY + panelHeight/GameView.tileSize + 4);
		
		if(GameView.tileSize < FAST_MODE_TILE_SIZE) {
			if(game.showHeightMap) {
				g.drawImage(game.heightMapImage, 0, 0, GameView.tileSize*game.world.getWidth(), GameView.tileSize*game.world.getHeight(), null);
			}
			else {
				g.drawImage(game.terrainImage, 0, 0, GameView.tileSize*game.world.getWidth(), GameView.tileSize*game.world.getHeight(), null);
			}
		}
		else {
			double highest = 0;
			double lowest = 1;
			if(game.showHeightMap) {
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
					game.drawTile(g, tile, lowest, highest);
				}
			}
			
			for(Building building : game.world.buildings) {
				game.drawHealthBar(g, building);
				game.drawHitsplat(g, building);
			}
			for(Plant plant : game.world.plants) {
				game.drawHealthBar(g, plant);
				game.drawHitsplat(g, plant);
			}
			for(Unit unit : game.world.units) {
				game.drawHealthBar(g, unit);
				game.drawHitsplat(g, unit);
			}
			
			for(Projectile p : game.world.projectiles) {
				int extra = (int) (GameView.tileSize * p.getExtraSize());
				g.drawImage(p.getShadow(0), p.getTile().getLocation().x * GameView.tileSize, p.getTile().getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * GameView.tileSize - extra/2, p.getTile().getLocation().y * GameView.tileSize - p.getHeight() - extra/2, GameView.tileSize + extra, GameView.tileSize + extra, null);
			}
			
			for(Thing thing : game.selectedThings) {
				// draw selection circle
				g.setColor(Utils.getTransparentColor(World.PLAYER_FACTION.color, 150));
//				Utils.setTransparency(g, 0.8f);
				Graphics2D g2d = (Graphics2D)g;
				Stroke currentStroke = g2d.getStroke();
				int strokeWidth = GameView.tileSize/12;
				g2d.setStroke(new BasicStroke(strokeWidth));
				g.drawOval(thing.getTile().getLocation().x * GameView.tileSize + strokeWidth/2, thing.getTile().getLocation().y * GameView.tileSize + strokeWidth/2, GameView.tileSize-1 - strokeWidth, GameView.tileSize-1 - strokeWidth);
				g2d.setStroke(currentStroke);
//				Utils.setTransparency(g, 1f);

				// draw spawn location for buildings
				if(thing instanceof Building) {
					Building building = (Building) thing;
					if(building.getSpawnLocation() != building.getTile()) {
						g.drawImage(RALLY_POINT_IMAGE, building.getSpawnLocation().getLocation().x * GameView.tileSize, building.getSpawnLocation().getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
					}
				}
				
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					// draw attacking target
					drawTarget(g, unit);
					// draw path 
					LinkedList<Tile> path = unit.getCurrentPath();
					if(path != null) {
						g.setColor(Color.green);
						TileLoc prev = unit.getTile().getLocation();
						for(Tile t : path) {
							if(prev != null) {
								g.drawLine(prev.x * GameView.tileSize + GameView.tileSize/2, prev.y * GameView.tileSize + GameView.tileSize/2, 
										t.getLocation().x * GameView.tileSize + GameView.tileSize/2, t.getLocation().y * GameView.tileSize + GameView.tileSize/2);
							}
							prev = t.getLocation();
						}
					}
					// draw destination flags
					for(PlannedAction plan : unit.actionQueue) {
						Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
						g.drawImage(FLAG, targetTile.getLocation().x * GameView.tileSize, targetTile.getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
					}
					int range = unit.getType().getCombatStats().getAttackRadius();
					if(range == 1) {
						range = -1;
					}
					// draws the attack range for units
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile t = game.world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							int x = t.getLocation().x * GameView.tileSize;
							int y = t.getLocation().y * GameView.tileSize;
							int w = GameView.tileSize;
							int h = GameView.tileSize;

							if (t.getLocation().distanceTo(unit.getTile().getLocation()) <= range) {
								g.setColor(Color.BLACK);
								Utils.setTransparency(g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(unit.getTile().getLocation()) > range) {
										TileLoc tileLoc = tile.getLocation();

										if (tileLoc.x == t.getLocation().x) {
											if (tileLoc.y < t.getLocation().y) {
												g.fillRect(x, y, w, 5);
											}
											if (tileLoc.y > t.getLocation().y) {
												g.fillRect(x, y + h - 5, w, 5);
											}

										}
										if (tileLoc.y == t.getLocation().y) {
											if (tileLoc.x < t.getLocation().x) {
												g.fillRect(x, y, 5, h);
											}
											if (tileLoc.x > t.getLocation().x) {
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

			int indicatorSize = GameView.tileSize/12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for(Unit unit : game.world.units) {
				int count = 0;
				if(visited.containsKey(unit.getTile())) {
					count = visited.get(unit.getTile());
				}
				visited.put(unit.getTile(), count+1);
					
				//draws a square for every player unit on the tile
				int xx = unit.getTile().getLocation().x * GameView.tileSize + offset;
				int yy = unit.getTile().getLocation().y * GameView.tileSize + (indicatorSize + offset)*count + offset;
				g.setColor(unit.getFaction().color);
				g.fillRect(xx, yy, indicatorSize, indicatorSize);
				g.setColor(Color.BLACK);
				g.drawRect(xx, yy, indicatorSize, indicatorSize);
				count++;
			}
			
			
			if(!game.showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						double brightness = game.world.getDaylight() + tile.getBrightness(World.PLAYER_FACTION);
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int)(255 * (1 - brightness))));
						g.fillRect(i * GameView.tileSize, j * GameView.tileSize, GameView.tileSize, GameView.tileSize);
					}
				}
			}
			
			if (game.selectedBuildingToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(game.selectedBuildingToSpawn.getImage(0));
				g2d.drawImage(bI, game.hoveredTile.x * GameView.tileSize, game.hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (game.selectedBuildingToPlan != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(game.selectedBuildingToPlan.getImage(0));
				g2d.drawImage(bI, game.hoveredTile.x * GameView.tileSize, game.hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (game.selectedUnitToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(game.selectedUnitToSpawn.getImage(0));
				g2d.drawImage(bI, game.hoveredTile.x * GameView.tileSize, game.hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			
			if(Game.DEBUG_DRAW) {
				if(GameView.tileSize >= 36) {
					int[][] rows = new int[upperX - lowerX][upperY - lowerY];
					int fontsize = GameView.tileSize/4;
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
							
							if(tile.liquidType != LiquidType.DRY) {
								strings.add(String.format(tile.liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount));
							}
							
							
							if(tile.getModifier() != null) {
								strings.add("GM=" + tile.getModifier().timeLeft());
							}
							rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, strings, rows[i-lowerX][j-lowerY], fontsize);
							
							for(Unit unit : tile.getUnits()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, unit.getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
							if(tile.getPlant() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getPlant().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
							if(tile.getHasBuilding()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getBuilding().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
							if(tile.getRoad() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getRoad().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
								
							}
						}
					}
				}
			}
			g.setColor(new Color(0, 0, 0, 64));
			g.drawRect(game.hoveredTile.x * GameView.tileSize, game.hoveredTile.y * GameView.tileSize, GameView.tileSize-1, GameView.tileSize-1);
			g.drawRect(game.hoveredTile.x * GameView.tileSize + 1, game.hoveredTile.y * GameView.tileSize + 1, GameView.tileSize - 3, GameView.tileSize - 3);
		}
	}

	public void drawTarget(Graphics g, Unit unit) {
		if(unit.getTarget() != null) {
			Thing target = unit.getTarget();
			int x = (int) ((target.getTile().getLocation().x * GameView.tileSize + GameView.tileSize*1/10) );
			int y = (int) ((target.getTile().getLocation().y * GameView.tileSize + GameView.tileSize*1/10) );
			int w = (int) (GameView.tileSize*8/10);
			int hi = (int)(GameView.tileSize*8/10);
			g.drawImage(TARGET_IMAGE, x, y, w, hi, null);
		}
	}
	
	public void drawMinimap(Graphics g, int x, int y, int w, int h, int panelWidth, int panelHeight) {
		if(game.showHeightMap) {
			g.drawImage(game.heightMapImage, x, y, w, h, null);
		}
		else {
			g.drawImage(game.minimapImage, x, y, w, h, null);
		}
		Position offsetTile = getTileAtPixel(viewOffset);
		int boxx = (int) (offsetTile.x * w / game.world.getWidth() / 2);
		int boxy = (int) (offsetTile.y * h / game.world.getHeight() / 2);
		int boxw = (int) (panelWidth * w / GameView.tileSize / game.world.getWidth());
		int boxh = (int) (panelHeight * h / GameView.tileSize / game.world.getHeight());
		g.setColor(Color.yellow);
		g.drawRect(x + boxx, y + boxy, boxw, boxh);
	}
}
