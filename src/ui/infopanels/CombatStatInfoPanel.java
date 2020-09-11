package ui.infopanels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import game.CombatStats;
import game.Unit;
import ui.KUIConstants;
import utils.Utils;

public class CombatStatInfoPanel extends InfoPanel{

	private CombatStats showing;
	private static final Image attackImage = Utils.loadImage("resources/Images/interfaces/attack.png");
	private static final Image attackspeedImage = Utils.loadImage("resources/Images/interfaces/attackspeed.png");
	private static final Image visionImage = Utils.loadImage("resources/Images/interfaces/vision.png");
	private static final Image movespeedImage = Utils.loadImage("resources/Images/interfaces/movespeed.png");
	private static final Image healthImage = Utils.loadImage("resources/Images/interfaces/redhitsplat.png");
	
	public CombatStatInfoPanel(CombatStats showing) {
		super(showing.toString(), attackImage);
		this.showing = showing;
	}
	
	public static void drawCombatStats(Graphics g, CombatStats stats, int x, int y) {
		g.setFont(KUIConstants.combatStatsFont);
		int fontSize = g.getFont().getSize();
		int iconSize = 20;
		int gap = 2;
		g.drawImage(attackImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getAttack() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
		
		y += iconSize + gap;
		g.drawImage(attackspeedImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getAttackSpeed() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);

		y += iconSize + gap;
		g.drawImage(visionImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getAttackRadius() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);

		y += iconSize + gap;
		g.drawImage(movespeedImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getMoveSpeed() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
		
		y += iconSize + gap;
		g.drawImage(healthImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getHealth() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		g.setColor(Color.black);
		drawCombatStats(g, showing, getWidth() - 80, 4);
	}
	
	
}

