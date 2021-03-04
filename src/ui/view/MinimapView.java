package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import javax.swing.*;

import ui.*;
import utils.*;
import world.*;

public class MinimapView extends JPanel {

	private static final Image MOON_IMAGE = Utils.loadImage("Images/interfaces/moon.png");
	private static final Image SUN_IMAGE = Utils.loadImage("Images/interfaces/sun.png");

	public static final int MINIMAPBORDERWIDTH = 40;

	private GameView gameView;

	public MinimapView(GameView gameView) {
		this.gameView = gameView;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH) / (getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH) / (getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameView.moveViewTo(ratiox, ratioy, gameView.getWidth(), gameView.getHeight());
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH) / (getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH) / (getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameView.moveViewTo(ratiox, ratioy, gameView.getWidth(), gameView.getHeight());
			}
		});
	}

	private void drawSunMoon(Graphics g) {
		int padding = 5;
		g.setFont(KUIConstants.infoFontSmaller);
		String dayCounter = "Day: " + gameView.getGameInstance().getDays() + "   Night: "
				+ gameView.getGameInstance().getNights();
		Color temp = g.getColor();
		g.setColor(Color.white);
		g.drawString(dayCounter, padding - 1, g.getFont().getSize() + padding - 1);
		g.setColor(Color.black);
		g.drawString(dayCounter, padding, g.getFont().getSize() + padding);
		g.setColor(temp);

		int offset = World.getCurrentDayOffset() + World.TRANSITION_PERIOD;
		int pathwidth = getWidth() - MINIMAPBORDERWIDTH;
		int pathheight = getHeight() - MINIMAPBORDERWIDTH;
		int totallength = 2 * pathwidth + 2 * pathheight;
		offset = totallength * offset / (World.DAY_DURATION + World.NIGHT_DURATION);
		int imagesize = MINIMAPBORDERWIDTH - padding * 2;
		if (offset < pathheight) {
			g.drawImage(SUN_IMAGE, padding, padding + pathheight - offset, imagesize, imagesize, null);
			g.drawImage(MOON_IMAGE, padding + pathwidth, padding + offset, imagesize, imagesize, null);
		} else {
			offset -= pathheight;
			if (offset < pathwidth) {
				g.drawImage(SUN_IMAGE, padding + offset, padding, imagesize, imagesize, null);
				g.drawImage(MOON_IMAGE, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize, null);
			} else {
				offset -= pathwidth;
				if (offset < pathheight) {
					g.drawImage(SUN_IMAGE, padding + pathwidth, padding + offset, imagesize, imagesize, null);
					g.drawImage(MOON_IMAGE, padding, padding + pathheight - offset, imagesize, imagesize, null);
				} else {
					offset -= pathheight;
					if (offset < pathwidth) {
						g.drawImage(SUN_IMAGE, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize,
								null);
						g.drawImage(MOON_IMAGE, padding + offset, padding, imagesize, imagesize, null);
					} else {
						offset -= pathwidth;
						if (offset < pathheight) {
							g.drawImage(SUN_IMAGE, padding, padding + pathheight - offset, imagesize, imagesize, null);
							g.drawImage(MOON_IMAGE, padding + pathwidth, padding + offset, imagesize, imagesize, null);
						}
					}
				}
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(gameView.getGameInstance().getBackgroundColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		drawSunMoon(g);
		g.fillRect(0, getHeight() * 4 / 5, getWidth(), getHeight() - getHeight() * 4 / 5);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		if (gameView.getDrawDebugStrings()) {
			drawMatrix(g);
		}

		int x = MINIMAPBORDERWIDTH;
		int y = MINIMAPBORDERWIDTH;
		int w = getWidth() - 2 * MINIMAPBORDERWIDTH;
		int h = getHeight() - 2 * MINIMAPBORDERWIDTH;
		g.drawImage(gameView.getDrawer().getImageToDrawMinimap(), x, y, w, h, null);
		drawViewFrustrum(g, x, y, w, h);
	}
	
	private void drawViewFrustrum(Graphics g, int x, int y, int w, int h) {
		Position[] viewBounds = gameView.getDrawer().getVisibleTileBounds();
		if(viewBounds != null) {
			int[][] viewFrustomPixels = new int[viewBounds.length][2];
			for(int i = 0; i < viewBounds.length; i++) {
				viewFrustomPixels[i][0] = (int) (viewBounds[i].x * w / gameView.getGameInstance().world.getWidth());
				viewFrustomPixels[i][1] = (int) (viewBounds[i].y * h / gameView.getGameInstance().world.getHeight());
			}
			g.setColor(Color.yellow);
			for(int i = 0; i < viewBounds.length; i++) {
				int iplus = (i+1) % viewBounds.length;
				g.drawLine( x + viewFrustomPixels[iplus][0], x + viewFrustomPixels[iplus][1], 
							x + viewFrustomPixels[i][0], x + viewFrustomPixels[i][1]);
			}
		}
	}
	
	private void drawMatrix(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.green);
		String string = "4 8 15 16 23 42";

		for (int i = 0; i < 20; i++) {
			int xpos = (int) (Math.random() * (getWidth() / 20 + 20));

			for (int j = 0; j < getHeight() * 2; j++) {
				int offset = (int) (Math.random() * 10);
				int character = (int) (Math.random() * string.length());
				g.drawString("" + string.charAt(character), xpos * 20 + offset, j * 20);

			}
		}
	}
}
