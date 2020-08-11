package ui;

import java.awt.*;

import javax.swing.*;

import game.*;

public class ResearchInfoPanel extends JPanel {
	
	Research showing;

	public ResearchInfoPanel(Research showing) {
		this.showing = showing;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		int progressBarHeight = 30;
		int x = 20;
		int y = progressBarHeight;
		
		g.setColor(Color.black);
		g.setFont(KUIConstants.infoFont);
		int offset = g.getFont().getSize();

		if(showing.getRequirement().getRequirements().size() > 0 ) {
			g.drawString("Requirements:", x, y += offset);
			
			for(Research req : showing.getRequirement().getRequirements()) {
				g.drawString(req.toString(), x + offset, y += offset);
			}
		}
		
		double completedRatio = 1.0 * showing.getPointsSpent() / showing.getType().getRequiredPoints();
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), progressBarHeight);
		g.setColor(Color.blue);
		g.fillRect(0, 0, (int) (getWidth() * completedRatio), progressBarHeight);
		g.setColor(Color.white);
		String progress = String.format("%s %d/%d", showing, showing.getPointsSpent(), showing.getType().getRequiredPoints());
		int w = g.getFontMetrics().stringWidth(progress);
		g.drawString(progress, getWidth()/2 - w/2, progressBarHeight*2/3);
	}
}
