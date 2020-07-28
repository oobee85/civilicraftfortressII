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
		
		int x = 50;
		int y = 50;
		
		g.setColor(Color.black);
		g.setFont(KUIConstants.buttonFont);
		int offset = 20;
		
		g.drawString(String.format("%s %d/%d", showing, showing.getPointsSpent(), showing.getType().getRequiredPoints()), x, y += offset);

		if(showing.getRequirement().getRequirements().size() > 0 ) {
			g.drawString("Requirements:", x, y += offset);
			
			for(Research req : showing.getRequirement().getRequirements()) {
				g.drawString(req.toString(), x + offset, y += offset);
			}
		}
		
		double completedRatio = 1.0 * showing.getPointsSpent() / showing.getType().getRequiredPoints();
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), 30);
		g.setColor(Color.blue);
		g.fillRect(0, 0, (int) (getWidth() * completedRatio), 30);
		g.setColor(Color.white);
		String progress = String.format("%d/%d", showing.getPointsSpent(), showing.getType().getRequiredPoints());
		int w = g.getFontMetrics().stringWidth(progress);
		g.drawString(progress, getWidth()/2 - w/2, 20);
	}
}
