package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class BuildingTypeInfoPanel extends InfoPanel {
	 
	private BuildingType showing;
	private Faction faction;

	public BuildingTypeInfoPanel(BuildingType showing, Faction faction) {
		super(showing.toString(), showing.getMipMap().getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
		this.faction = faction;
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		if(showing.getResearchRequirement() != null) {
			g.setFont(KUIConstants.combatStatsFont);
			ResearchType req = Game.researchTypeMap.get(showing.getResearchRequirement());
			g.setColor(Color.black);
			if(!faction.getResearch(req).isCompleted()) {
				g.setColor(Color.red);
			}
			g.drawString(req.toString(), getImageSize(), y += g.getFont().getSize());
		}
		if(showing.getCost() != null) {
			UnitTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6, faction);
		}
		
		int offset = g.getFont().getSize();
		int bottomY = getHeight() - offset/2;
		g.setColor(Color.black);
		String statsString = showing.getHealth() + " health";
		if(showing.getCultureRate() > 0) {
			statsString += "    " + showing.getCultureRate() + " culture";
		}
		g.drawString(statsString, getImageSize(), bottomY - offset);
		g.drawString(showing.info(), getImageSize(), bottomY);
	}
}
