package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class ResearchInfoPanel extends InfoPanel {
	
	Research showing;

	public ResearchInfoPanel(Research showing) {
		super(showing.toString(), showing.type.getImage(70), 70);
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
		int xoffset = 15;

		g.setFont(KUIConstants.combatStatsFont);
		int offset = g.getFont().getSize();
		if(showing.getRequirement().getRequirements().size() > 0 ) {
			g.drawLine(x + xoffset - 10, y, x + xoffset - 10, y + offset*showing.getRequirement().getRequirements().size() - offset/4);
			
			for(Research req : showing.getRequirement().getRequirements()) {
				if(req.isUnlocked()) {
					g.setColor(Color.black);
				}
				else if(req.getRequirement().areRequirementsMet()) {
					g.setColor(Color.red);
				}
				else {
					g.setColor(Color.red);
				}
				g.drawString(req.toString(), x + xoffset, y += offset);
				g.setColor(Color.black);
				g.drawLine(x + xoffset - 10, y - offset/4, x + xoffset - 1, y - offset/4);
			}
		}
		y += offset/2;
		UnitTypeInfoPanel.drawCosts(g, showing.getCost(), x, y);

		g.setFont(KUIConstants.infoFont);
		double completedRatio = 1.0 * showing.getPointsSpent() / showing.getRequiredPoints();
		String progress = String.format("%d/%d", showing.getPointsSpent(), showing.getRequiredPoints());
		
		KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, 0, getHeight() - progressBarHeight, getWidth(), progressBarHeight);
	}
}
