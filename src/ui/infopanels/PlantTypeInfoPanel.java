package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;
import world.PlantType;

public class PlantTypeInfoPanel extends InfoPanel {
	 
	private PlantType showing;

	public PlantTypeInfoPanel(PlantType showing) {
		super(showing.toString(), showing.getHasImage().getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
		
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		if(showing.getCost() != null) {
//			PlantTypeInfoPanel.drawCosts(g, showing.getCost(), getImageSize(), y + 6);
		}
		
		int offset = g.getFont().getSize();
		int bottomY = getHeight() - offset/2;
		g.setColor(Color.black);
		String statsString = showing.getHealth() + " health";
		g.drawString(statsString, getImageSize(), bottomY - offset);
//		g.drawString(showing.info(), getImageSize(), bottomY);
	}
}
