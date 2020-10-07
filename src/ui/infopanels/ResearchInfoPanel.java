package ui.infopanels;

import java.awt.*;
import java.util.HashMap;

import game.*;
import ui.*;

public class ResearchInfoPanel extends InfoPanel {
	
	Research showing;

	public ResearchInfoPanel(Research showing) {
		super(showing.toString(), showing.getImage(70), 70);
		this.showing = showing;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		int progressBarHeight = 25;
		int x = getImageSize() + 5;
		
		g.setColor(Color.black);
		g.setFont(KUIConstants.infoFont);
		int offset = g.getFont().getSize();
		int xoffset = 15;

		if(showing.getRequirement().getRequirements().size() > 0 ) {
			g.drawLine(x + xoffset - 10, y, x + xoffset - 10, y + offset*showing.getRequirement().getRequirements().size() - offset/4);
			
			HashMap<ItemType, Integer> cost = showing.getCost();
			g.drawString(cost.toString(), x + xoffset-5, y += offset);
			
			for(Research req : showing.getRequirement().getRequirements()) {
				g.drawString(req.toString(), x + xoffset, y += offset);
				g.drawLine(x + xoffset - 10, y - offset/4, x + xoffset - 1, y - offset/4);
			}
		}
		
		double completedRatio = 1.0 * showing.getPointsSpent() / showing.getRequiredPoints();
		String progress = String.format("%d/%d", showing.getPointsSpent(), showing.getRequiredPoints());
		
		KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, 0, getHeight() - progressBarHeight, getWidth(), progressBarHeight);
	}
}
