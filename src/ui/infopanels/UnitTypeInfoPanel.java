package ui.infopanels;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class UnitTypeInfoPanel extends InfoPanel {

	private static final Image attackImage = Utils.loadImage("resources/Images/interfaces/attack.png");
	private static final Image attackspeedImage = Utils.loadImage("resources/Images/interfaces/attackspeed.png");
	private static final Image visionImage = Utils.loadImage("resources/Images/interfaces/vision.png");
	private static final Image movespeedImage = Utils.loadImage("resources/Images/interfaces/movespeed.png");
	private static final Image healthImage = Utils.loadImage("resources/Images/interfaces/redhitsplat.png");
	
	
	UnitType showing;
	private Faction faction;

	public UnitTypeInfoPanel(UnitType showing, Faction faction) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
		this.faction = faction;
	}
	
	public static void drawCombatStats(Graphics g, CombatStats stats, int x, int y) {
		g.setFont(KUIConstants.combatStatsFont);
		g.setColor(Color.black);
		int fontSize = g.getFont().getSize();
		int iconSize = 20;
		int gap = 2;
//		g.drawImage(attackImage, x, y, iconSize, iconSize, null);
//		g.drawString(stats.getAttack() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
//		
//		y += iconSize + gap;
//		g.drawImage(attackspeedImage, x, y, iconSize, iconSize, null);
//		g.drawString(stats.getAttackSpeed() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
//
//		y += iconSize + gap;
//		g.drawImage(visionImage, x, y, iconSize, iconSize, null);
//		g.drawString(stats.getAttackRadius() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);

		y += iconSize + gap;
		g.drawImage(movespeedImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getMoveSpeed() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
		
		y += iconSize + gap;
		g.drawImage(healthImage, x, y, iconSize, iconSize, null);
		g.drawString(stats.getHealth() + "", x + iconSize + gap, y + iconSize/2 + fontSize/3);
	}
	
	public static void drawCosts(Graphics g, HashMap<ItemType, Integer> costs, int x, int y, Faction faction) {
		g.setFont(KUIConstants.combatStatsFont);
		int fontSize = g.getFont().getSize();
		int iconSize = 16;
		int gap = 1;
		
		for(Entry<ItemType, Integer> entry : costs.entrySet()) {
			g.drawImage(entry.getKey().getImage(iconSize), x, y, iconSize, iconSize, null);
			int currentAmount = faction.getItemAmount(entry.getKey());
			String str = currentAmount + "/" + entry.getValue() + " " + entry.getKey().toString();
			g.setColor(currentAmount >= entry.getValue() ? Color.black : Color.red);
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
		int x = getImageSize();
		g.setColor(Color.black);
		g.setFont(KUIConstants.infoFontSmaller);
		int offset = g.getFont().getSize();
		if(showing.getResearchRequirement() != null) {
			g.drawString(Game.researchTypeMap.get(showing.getResearchRequirement()).toString(), x, y += offset);
		}

		if(showing.getCost() != null) {
			drawCosts(g, showing.getCost(), x, y + 6, faction);
		}
		drawCombatStats(g, showing.getCombatStats(), getWidth() - 80, 4);
	}
}
