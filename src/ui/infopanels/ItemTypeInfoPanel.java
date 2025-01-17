package ui.infopanels;

import java.awt.*;

import game.*;

public class ItemTypeInfoPanel extends InfoPanel {

	private ItemType showing;
	private Faction faction;

	public ItemTypeInfoPanel(ItemType showing, Faction faction) {
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
		
		if(showing.getCost() != null && showing.getCost().size() > 0 ) {
			UnitTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6, faction);
		}
		
		int offset = g.getFont().getSize();
		int bottomY = getHeight() - offset/3;
		g.setColor(Color.black);
//		String statsString = showing.getHealth() + " health";
//		if(showing.getCultureRate() > 0) {
//			statsString += "    " + showing.getCultureRate() + " culture";
//		}*/
//		g.drawString(statsString, 5, bottomY - offset);
		String description = showing.getDescription();
		if (description != null) {
			g.drawString(description, 5, bottomY);
		}
		
	}
}
