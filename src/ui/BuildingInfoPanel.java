package ui;

import java.awt.*;

import javax.swing.*;

import game.*;

public class BuildingInfoPanel extends JPanel {
	
	Building showing;

	public BuildingInfoPanel(Building showing) {
		this.showing = showing;
	}
	
	
	private void drawProgressBar(Graphics g, Color foreground, Color background, Color textColor, double ratio, String text, int x, int y, int w, int h) {
		g.setColor(background);
		g.fillRect(x, y, w, h);
		g.setColor(foreground);
		g.fillRect(x, y, (int) (w * ratio), h);
		g.setColor(textColor);
		int textWidth = g.getFontMetrics().stringWidth(text);
		g.drawString(text, w/2 - textWidth/2, y + h/2 + g.getFont().getSize()/3);
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		g.setFont(KUIConstants.infoFont);
		int lineHeight = g.getFont().getSize();
		int y = g.getFont().getSize() - 1;
		String name = showing.toString();
		int w = g.getFontMetrics().stringWidth(name);
		g.drawString(name, getWidth()/2 - w/2, y);
		
		y += lineHeight;
		g.setFont(KUIConstants.infoFontSmall);
		
		if(showing.getCulture() > 0 ) {
			g.drawString("culture " + (int)showing.getCulture(), 30, y);
			y += lineHeight;
		}
		
		
		if(showing.getBuildingUnit() != null) {
			int progressBarHeight = 30;
			int totalEffort = showing.getBuildingUnit().getType().getCombatStats().getTicksToBuild();
			int expendedEffort = totalEffort - showing.getBuildingUnit().getRemainingEffort();

			double completedRatio = 1.0 * expendedEffort / totalEffort;
			String progress = String.format("%s %d/%d", showing.getBuildingUnit(), expendedEffort, totalEffort);
			drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, 30, y, getWidth()-60, progressBarHeight);
			g.drawImage(showing.getBuildingUnit().getImage(26), 2, y + 2, 26, 26, null);
			
			y += progressBarHeight + lineHeight;
		}

		g.setFont(KUIConstants.infoFontTiny);
		int hpbarHeight = g.getFont().getSize();
		String hpstring =  String.format("%d/%d",(int)showing.getHealth(), (int)showing.getBuildingType().getHealth());
		drawProgressBar(g, Color.green, Color.red, Color.black, showing.getHealth()/showing.getBuildingType().getHealth(), hpstring, 0, getHeight()-hpbarHeight, getWidth(), hpbarHeight);
	}
}
