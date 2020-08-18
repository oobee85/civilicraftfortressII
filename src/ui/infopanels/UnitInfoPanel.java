package ui.infopanels;

import java.awt.*;

import game.*;
import ui.*;

public class UnitInfoPanel extends InfoPanel {
	
	Unit showing;

	public UnitInfoPanel(Unit showing) {
		super(showing.toString(), showing.getImage(DEFAULT_IMAGE_SIZE));
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
		String hpstring =  String.format("%d/%d",(int)showing.getHealth(), (int)showing.getType().getCombatStats().getHealth());
		KUIConstants.drawProgressBar(g, Color.green, Color.red, Color.black, showing.getHealth()/showing.getType().getCombatStats().getHealth(), hpstring, 0, getHeight()-hpbarHeight, getWidth() - 85, hpbarHeight);

		UnitTypeInfoPanel.drawCombatStats(g, showing.getType().getCombatStats(), getWidth() - 80, 4);
	}
}
