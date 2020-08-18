package ui.infopanels;

import java.awt.*;

import game.*;

public class ItemTypeInfoPanel extends InfoPanel {

	private ItemType showing;

	public ItemTypeInfoPanel(ItemType showing) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		if(showing.getCost() != null && showing.getCost().size() > 0 ) {
			UnitTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6);
		}
	}
}
