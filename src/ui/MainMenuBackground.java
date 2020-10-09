package ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import utils.*;
import wildlife.*;
import world.*;

public class MainMenuBackground extends JPanel {
	
	private Game game;
	private volatile boolean ready;
	private volatile boolean stop;
	private volatile boolean phase2;
	private volatile boolean finished;
	private volatile Runnable listener;

	private volatile int thisWidth;
	private volatile int thisHeight;
	
	private volatile int tickRate = 100;
	
	public MainMenuBackground() {
		this.setOpaque(false);
		game = new Game(new GUIController() {
			@Override
			public void selectedBuilding(Building building, boolean selected) {
			}
			@Override
			public void selectedUnit(Unit unit, boolean selected) {
			}
			@Override
			public void toggleTileView() {
			}
			@Override
			public void updateGUI() {
			}
			@Override
			public void selectedSpawnUnit(boolean selected) {
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				game.setViewSize(getWidth(), getHeight());
			}
		});
	}
	
	public void start(Runnable startedCallback) {
		Thread thread = new Thread(() -> {
			double startingNumTilesOnscreen = 2;
			double endingNumTiles = 100;
			int duration1 = 10000;
			int duration2 = 1000;
			game.generateWorld(MapType.PANGEA, 64, false);
			thisWidth = getWidth();
			thisHeight = getHeight();
			game.setViewSize(thisWidth, thisHeight);
			game.centerViewOn(game.world.buildings.get(0).getTile(), (int)(thisWidth/startingNumTilesOnscreen));
			repaint();
			ready = true;
			Thread gameLoopThread = new Thread(() -> {
				try {
					while (!stop && !phase2) {
						long start = System.currentTimeMillis();
						game.gameTick();
						long elapsed = System.currentTimeMillis() - start;
						long sleeptime = tickRate - elapsed;
						if(sleeptime > 0) {
							Thread.sleep(sleeptime);
						}
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			});
			if(startedCallback != null) {
				startedCallback.run();
			}
			if(phase2) {
			}
			else {
				// if the start button is pressed before the fake world has a chance to load, cancel everything
				gameLoopThread.start();
				try {
					long startTime = System.currentTimeMillis() + 500;
					double numTiles = 1;
					while(!phase2) {
						game.zoomViewTo((int) (thisWidth/numTiles), thisWidth/2, thisHeight/2);
						this.repaint();
						Thread.sleep(30);
						int elapsedTime = (int) (System.currentTimeMillis() - startTime);
						if(elapsedTime <= duration1) {
							numTiles = 1.0*startingNumTilesOnscreen + (endingNumTiles - startingNumTilesOnscreen) * elapsedTime/duration1;
							tickRate = 100 - 90*elapsedTime/duration1;
						}
					}
					startTime = System.currentTimeMillis();
					startingNumTilesOnscreen = numTiles;
					endingNumTiles = 0.1;
					while(phase2) {
						game.zoomViewTo((int) (thisWidth/numTiles), thisWidth/2, thisHeight/2);
						this.repaint();
						Thread.sleep(30);
						int elapsedTime = (int) (System.currentTimeMillis() - startTime);
						if(elapsedTime > duration2) {
							stop = true;
							break;
						}
						numTiles = 1.0*startingNumTilesOnscreen + (endingNumTiles - startingNumTilesOnscreen) * elapsedTime/duration2;
					}
				} catch (InterruptedException e) {
				}
			}
			finished = true;
			listener.run();
		});

		thread.start();
	}
	
	public void stop(Runnable listener) {
		this.listener = listener;
		phase2 = true;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if(!ready) {
			return;
		}
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setColor(game.getBackgroundColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		game.drawGame(g);
		g.setColor(Color.black);
		g.drawRect(-1, 0, getWidth() + 1, getHeight());
		super.paintComponent(g);
	}
}
