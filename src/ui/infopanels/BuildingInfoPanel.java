package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class BuildingInfoPanel extends InfoPanel {
	
	Building showing;

	public BuildingInfoPanel(Building showing) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}

		g.setFont(KUIConstants.infoFontSmall);
		int lineHeight = g.getFont().getSize() + 1;
		y += lineHeight;
		
		if(showing.getCulture() > 0 ) {
			g.drawString("culture " + (int)showing.getCulture(), getImageSize(), y);
			y += lineHeight;
		}
		
		
		if(showing.getBuildingUnit().peek() != null) {
			int x = getImageSize();
			int progressBarHeight = 30;
			int buffer = 1;
			int imageSize = progressBarHeight - 2*buffer;
			g.drawImage(showing.getBuildingUnit().peek().getImage(imageSize), x + buffer, y + buffer, imageSize, imageSize, null);
			
			
			int totalEffort = showing.getBuildingUnit().peek().getType().getCombatStats().getTicksToBuild();
			int expendedEffort = totalEffort - showing.getBuildingUnit().peek().getRemainingEffort();
			double completedRatio = 1.0 * expendedEffort / totalEffort;
			String progressString = String.format("%s %d/%d", showing.getBuildingUnit(), expendedEffort, totalEffort);
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progressString, x + progressBarHeight, y, getWidth() - x - progressBarHeight - 10, progressBarHeight);
			
			y += progressBarHeight + lineHeight;
		}

		g.setFont(KUIConstants.infoFontTiny);
		int hpbarHeight = g.getFont().getSize();
		String hpstring =  String.format("%d/%d",(int)showing.getHealth(), (int)showing.getBuildingType().getHealth());
		KUIConstants.drawProgressBar(g, Color.green, Color.red, Color.black, showing.getHealth()/showing.getBuildingType().getHealth(), hpstring, 0, getHeight()-hpbarHeight, getWidth(), hpbarHeight);
	}
}
