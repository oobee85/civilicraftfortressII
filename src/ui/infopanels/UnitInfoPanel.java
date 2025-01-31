package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class UnitInfoPanel extends InfoPanel {
	
	Unit showing;

	public UnitInfoPanel(Unit showing) {
		super(showing.getType().toString(), showing.getMipMap().getImage(DEFAULT_IMAGE_SIZE));
		this.showing = showing;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		g.setFont(KUIConstants.infoFontTiny);
		int hpbarHeight = g.getFont().getSize();
		String hpstring =  String.format("%d/%d",(int)showing.getHealth(), (int)showing.getMaxHealth());
		KUIConstants.drawProgressBar(g, Color.green, Color.red, Color.black, showing.getHealth()/showing.getMaxHealth(), hpstring, 0, getHeight()-hpbarHeight, getWidth() - 85, hpbarHeight);

		UnitTypeInfoPanel.drawCombatStats(g, showing.getType(), showing.getFaction(), getWidth() - 100, 4);

		if(showing.hasInventory())
			UnitTypeInfoPanel.drawInventory(g, showing.getInventory(), 2, EXPLODE_BUTTON_SIZE + 2);
	}
}
