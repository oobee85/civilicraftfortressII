package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class BuildingInfoPanel extends InfoPanel {
	
	Building showing;

	public BuildingInfoPanel(Building showing) {
		super(showing.getType().toString(), showing.getMipMap().getImage(DEFAULT_IMAGE_SIZE));
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
		
		if(showing.getCulture() > 0 ) {
			g.drawString("culture " + (int)showing.getCulture(), getImageSize(), y += lineHeight);
		}
		
		
		if(showing.getProducingUnit().peek() != null) {
			Unit inProgress = showing.getProducingUnit().peek();
			y += lineHeight/2;
			int x = getImageSize();
			int progressBarHeight = 30;
			int buffer = 1;
			int imageSize = progressBarHeight - 2*buffer;
			g.drawImage(inProgress.getMipMap().getImage(imageSize), x + buffer, y + buffer, imageSize, imageSize, null);
			
			
			int totalEffort = inProgress.getType().getCombatStats().getTicksToBuild();
			int expendedEffort = totalEffort - showing.getRemainingEffortToProduceUnit();
			double completedRatio = 1.0 * expendedEffort / totalEffort;
			String progressString = String.format("%s %d/%d", inProgress.getUnitType(), expendedEffort, totalEffort);
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progressString, x + progressBarHeight, y, getWidth() - x - progressBarHeight - 10, progressBarHeight);
			
			y += progressBarHeight;
			int offset = 0;
			boolean first = true;
			if(showing.getProducingUnit().size() > 1) {
				y += lineHeight/4;
				for(Unit unit : showing.getProducingUnit()) {
					if(first) {
						first = false;
						continue;
					}
					g.drawImage(unit.getMipMap().getImage(imageSize), x + buffer + offset, y + buffer, imageSize, imageSize, null);
					offset += imageSize + buffer;
				}
			}
		}

		g.setFont(KUIConstants.infoFontTiny);
		int hpbarHeight = g.getFont().getSize();
		String hpstring =  String.format("%d/%d",(int)showing.getHealth(), (int)showing.getType().getHealth());
		KUIConstants.drawProgressBar(g, Color.green, Color.red, Color.black, showing.getHealth()/showing.getType().getHealth(), hpstring, 0, getHeight()-hpbarHeight, getWidth(), hpbarHeight);
		
		if(showing.hasInventory())
			UnitTypeInfoPanel.drawInventory(g, showing.getInventory(), 2, EXPLODE_BUTTON_SIZE + 2);
	}
}
