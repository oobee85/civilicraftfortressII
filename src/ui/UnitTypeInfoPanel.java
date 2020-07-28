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
		
		int x = 25;
		int y = 25;
		
		g.setColor(Color.black);
		g.setFont(KUIConstants.buttonFont);
		int offset = 20;
		
		g.drawString("" + showing.toString(), x, y += offset);
		if(showing.getResearchRequirement() != null) {
			g.drawString("Research: " + showing.getResearchRequirement(), x, y += offset);
		}
		g.drawString("" + showing.getCombatStats(), x, y += offset);

		if(showing.getCost() != null) {
			g.drawString("Cost:", x, y += offset);
			
			for(Entry<ItemType, Integer> entry : showing.getCost().entrySet()) {
				g.drawString(entry.getValue() + " " + entry.getKey().toString(), x + offset, y += offset);
			}
		}
		
	}
}
