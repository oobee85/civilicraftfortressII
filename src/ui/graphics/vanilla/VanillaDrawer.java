package ui.graphics.vanilla;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.graphics.*;
import ui.utils.DrawingUtils;
import ui.view.GameView.*;
import utils.*;
import world.*;

public class VanillaDrawer extends Drawer {

	private static final int FAST_MODE_TILE_SIZE = 10;
	
	private static final Image TARGET_IMAGE = Utils.loadImage("Images/interfaces/ivegotyouinmysights.png");
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
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					nextRequested.acquire();
					long startDrawing = System.currentTimeMillis();
					int next = (currentBuffer + 1) % buffers.length;
					frozenViewOffset.x = state.viewOffset.x;
					frozenViewOffset.y = state.viewOffset.y;
					frozenTileSize = state.tileSize;
					
					drawStuff(buffers[next]);
					
					drawnAtOffset[next].x = frozenViewOffset.x;
					drawnAtOffset[next].y = frozenViewOffset.y;
					drawnAtTileSize[next] = frozenTileSize;
					long finishedDrawing = System.currentTimeMillis();
					bufferDrawTimes[next] = (int) (finishedDrawing - startDrawing);
					currentBuffer = next;
					numAvailable.release();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
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

	private void drawStuff(BufferedImage image) {
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		Utils.clearBufferedImageTo(g, new Color(0, 0, 0, 0), image.getWidth(), image.getHeight());
		if (game == null || game.world == null) {
			g.drawString("No World to display", 20, 20);
		}
		else {
			g.translate(-frozenViewOffset.getIntX(), -frozenViewOffset.getIntY());
			draw(g, canvas.getWidth(), canvas.getHeight());
			g.translate(frozenViewOffset.getIntX(), frozenViewOffset.getIntY());
		}
		g.dispose();
	}

	private void draw(Graphics g, int panelWidth, int panelHeight) {
		RenderingState renderState = new RenderingState();
		// Start by drawing plain terrain image
		g.drawImage(mapImages[state.mapMode.ordinal()], 0, 0, 
				frozenTileSize * game.world.getWidth(), 
				frozenTileSize * game.world.getHeight() + frozenTileSize/2, null);

		// Try to only draw stuff that is visible on the screen
		renderState.lowerX = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntX() - 2);
		renderState.lowerY = Math.max(0, state.viewOffset.divide(frozenTileSize).getIntY() - 2);
		renderState.upperX = Math.min(game.world.getWidth(), renderState.lowerX + panelWidth / frozenTileSize + 4);
		renderState.upperY = Math.min(game.world.getHeight(), renderState.lowerY + panelHeight / frozenTileSize + 4);

		if (frozenTileSize >= FAST_MODE_TILE_SIZE && state.mapMode != MapMode.LIGHT) {

			renderState.gameViewState = state;
			renderState.world = game.world;
			renderState.mapMode = state.mapMode;
			renderState.g = (Graphics2D) g;
			renderState.faction = state.faction;
			renderState.tileSize = frozenTileSize;
			renderState.draww = frozenTileSize;
			renderState.drawh = frozenTileSize;

			for (RenderingStep step : pipelines[state.mapMode.ordinal()].steps) {
				step.render(renderState);
				for (int i = renderState.lowerX; i <= renderState.upperX; i++) {
					for (int j = renderState.lowerY; j <= renderState.upperY; j++) {
						Tile tile = game.world.get(new TileLoc(i, j));
						if (tile == null) {
							continue;
						}
						Point drawAt = RenderingFunctions.getDrawingCoords(tile.getLocation(), state.tileSize);
						step.render(renderState, tile, drawAt);
					}
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
