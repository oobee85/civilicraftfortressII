package ui.infopanels;

import java.awt.*;

import game.*;

public class BuildingTypeInfoPanel extends InfoPanel {
	
	BuildingType showing;

	public BuildingTypeInfoPanel(BuildingType showing) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}

		if(showing.getCost() != null) {
			UnitTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6);
		}
	}
}
