package networking.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ui.*;
import utils.*;
import world.*;

public class GameView extends JPanel {

	public static int tileSize = 9;
	
	
	
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
		game.draw(g, panelWidth, panelHeight, viewOffset);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		if(World.PLAYER_FACTION.getResearchTarget() != null && !World.PLAYER_FACTION.getResearchTarget().isUnlocked()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * World.PLAYER_FACTION.getResearchTarget().getPointsSpent() / World.PLAYER_FACTION.getResearchTarget().getRequiredPoints();
			String progress = String.format(World.PLAYER_FACTION.getResearchTarget() + " %d/%d", World.PLAYER_FACTION.getResearchTarget().getPointsSpent(), World.PLAYER_FACTION.getResearchTarget().getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, panelWidth - panelWidth/3 - 4, 4, panelWidth/3, 30);
		}
		Toolkit.getDefaultToolkit().sync();
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
