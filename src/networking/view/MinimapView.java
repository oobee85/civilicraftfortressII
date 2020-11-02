package networking.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ui.*;
import utils.*;
import world.*;

public class MinimapView extends JPanel {
	
	private static final Image MOON_IMAGE = Utils.loadImage("resources/Images/interfaces/moon.png");
	private static final Image SUN_IMAGE = Utils.loadImage("resources/Images/interfaces/sun.png");

	public static final int MINIMAPBORDERWIDTH = 40;
	
	private GameView gameView;
	public MinimapView(GameView gameView) {
		this.gameView = gameView;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameView.moveViewTo(ratiox, ratioy, gameView.getWidth(), gameView.getHeight());
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameView.moveViewTo(ratiox, ratioy, gameView.getWidth(), gameView.getHeight());
			}
		});
	}
	private void drawSunMoon(Graphics g) {
		int padding = 5;
		g.setFont(KUIConstants.infoFontSmaller);
		String dayCounter = "Day: " + gameView.getGameInstance().getDays() + "   Night: " + gameView.getGameInstance().getNights();
		Color temp = g.getColor();
		g.setColor(Color.white);
		g.drawString(dayCounter, padding-1, g.getFont().getSize() + padding-1 );
		g.setColor(Color.black);
		g.drawString(dayCounter, padding, g.getFont().getSize() + padding );
		g.setColor(temp);
		
		int offset = World.getCurrentDayOffset() + World.TRANSITION_PERIOD;
		int pathwidth = getWidth() - MINIMAPBORDERWIDTH;
		int pathheight = getHeight() - MINIMAPBORDERWIDTH;
		int totallength = 2*pathwidth + 2*pathheight;
		offset = totallength*offset / (World.DAY_DURATION + World.NIGHT_DURATION);
		int imagesize = MINIMAPBORDERWIDTH - padding*2;
		if(offset < pathheight) {
			g.drawImage(SUN_IMAGE, padding, padding + pathheight - offset, imagesize, imagesize, null);
			g.drawImage(MOON_IMAGE, padding + pathwidth, padding + offset, imagesize, imagesize, null);
		}
		else {
			offset -= pathheight;
			if(offset < pathwidth) {
				g.drawImage(SUN_IMAGE, padding + offset, padding, imagesize, imagesize, null);
				g.drawImage(MOON_IMAGE, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize, null);
			}
			else {
				offset -= pathwidth;
				if(offset < pathheight) {
					g.drawImage(SUN_IMAGE, padding + pathwidth, padding + offset, imagesize, imagesize, null);
					g.drawImage(MOON_IMAGE, padding, padding + pathheight - offset, imagesize, imagesize, null);
				}
				else {
					offset -= pathheight;
					if(offset < pathwidth) {
						g.drawImage(SUN_IMAGE, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize, null);
						g.drawImage(MOON_IMAGE, padding + offset, padding, imagesize, imagesize, null);
					}
					else {
						offset -= pathwidth;
						if(offset < pathheight) {
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
		g.fillRect(0, getHeight()*4/5, getWidth(), getHeight() - getHeight()*4/5);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		gameView.drawMinimap(g, MINIMAPBORDERWIDTH, MINIMAPBORDERWIDTH,
				getWidth() - 2 * MINIMAPBORDERWIDTH,
				getHeight() - 2 * MINIMAPBORDERWIDTH);
	}
}
