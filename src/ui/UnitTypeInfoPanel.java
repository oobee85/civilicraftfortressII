package ui;

import java.awt.*;
import java.util.Map.*;

import javax.swing.*;

import game.*;

public class UnitTypeInfoPanel extends JPanel {

	UnitType showing;

	public UnitTypeInfoPanel(UnitType showing) {
		this.showing = showing;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		g.drawImage(showing.getImage(20), 5, 5, 25, 25, null);
		
		int x = 35;
		int y = 5;
		
		g.setColor(Color.black);
		int offset = g.getFont().getSize();

		g.setFont(KUIConstants.infoFont);
		g.drawString("" + showing.toString(), x, y += offset);

		g.setFont(KUIConstants.infoFontSmall);
		offset = g.getFont().getSize();
		if(showing.getResearchRequirement() != null) {
			g.drawString("Research: " + showing.getResearchRequirement(), x, y += offset);
		}
		g.drawString("" + showing.getCombatStats(), x, y += offset);

		int costx = x;
		String coststr = "Cost: ";
		if(showing.getCost() != null) {
			boolean first = true;
			for(Entry<ItemType, Integer> entry : showing.getCost().entrySet()) {
				String str = entry.getValue() + " " + entry.getKey().toString();
				if(first) {
					str = coststr + str;
				}
				g.drawString(str, costx, y += offset);
				if(first) {
					costx += g.getFontMetrics().stringWidth(coststr);
					first = false;
				}
			}
		}
		
	}
}
