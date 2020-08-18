package ui.infopanels;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import game.*;
import ui.*;
import utils.*;

public class UnitTypeInfoPanel extends InfoPanel {

	private static final Image attackImage = Utils.loadImage("resources/Images/interfaces/attack.png");
	private static final Image attackspeedImage = Utils.loadImage("resources/Images/interfaces/attackspeed.png");
	private static final Image visionImage = Utils.loadImage("resources/Images/interfaces/vision.png");
	private static final Image movespeedImage = Utils.loadImage("resources/Images/interfaces/movespeed.png");
	private static final Image healthImage = Utils.loadImage("resources/Images/interfaces/redhitsplat.png");
	
	
	UnitType showing;

	public UnitTypeInfoPanel(UnitType showing) {
		super(showing.toString(), showing.getImage(InfoPanel.IMAGE_SIZE));
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
		g.drawString(stats.getVisionRadius() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);

		y += iconSize + gap;
		g.drawImage(movespeedImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getSpeed() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
		
		y += iconSize + gap;
		g.drawImage(healthImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getHealth() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
	}
	
	public static void drawCosts(Graphics g, HashMap<ItemType, Integer> costs, int x, int y) {
		g.setFont(KUIConstants.combatStatsFont);
		int fontSize = g.getFont().getSize();
		int iconSize = 16;
		int gap = 1;
		
		for(Entry<ItemType, Integer> entry : costs.entrySet()) {
			g.drawImage(entry.getKey().getImage(iconSize), x, y, iconSize, iconSize, null);
			String str = entry.getValue() + " " + entry.getKey().toString();
			g.drawString(str, x + iconSize + gap + 1, y + iconSize/2 + fontSize/3);
			y += iconSize + gap;
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		int x = InfoPanel.IMAGE_SIZE;
		g.setColor(Color.black);
		g.setFont(KUIConstants.infoFontSmaller);
		int offset = g.getFont().getSize();
		if(showing.getResearchRequirement() != null) {
			g.drawString(showing.getResearchRequirement().toString(), x, y += offset);
		}

		if(showing.getCost() != null) {
			drawCosts(g, showing.getCost(), x, y + 6);
		}
		drawCombatStats(g, showing.getCombatStats(), getWidth() - 80, 4);
	}
}
