package ui.infopanels;

import java.awt.*;

import game.*;

public class BuildingTypeInfoPanel extends InfoPanel {
	
	BuildingType showing;
	private Faction faction;

	public BuildingTypeInfoPanel(BuildingType showing, Faction faction) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
		this.faction = faction;
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}

		if(showing.getCost() != null) {
			UnitTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6, faction);
		}
	}
}
