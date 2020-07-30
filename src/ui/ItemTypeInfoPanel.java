package ui;

import java.awt.*;
import java.util.Map.*;

import javax.swing.*;

import game.*;
import utils.*;

public class ItemTypeInfoPanel extends JPanel {

	private int ICONSIZE = 50;
	private ItemType showing;
	private ImageIcon icon;
	

	public ItemTypeInfoPanel(ItemType showing) {
		this.showing = showing;
		icon = Utils.resizeImageIcon(showing.getImageIcon(0), ICONSIZE, ICONSIZE);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(showing == null) {
			return;
		}
		
		int x = 20;
		int y = 20;
		
		int padding = 10;
		g.drawImage(icon.getImage(), x, y, null);
		g.setColor(Color.black);
		g.drawRect(x - padding, y - padding, ICONSIZE + padding*2, ICONSIZE + padding*2);
		
		y -= padding;
		
		x += ICONSIZE + padding*2 + padding;
		g.setColor(Color.black);
		g.setFont(KUIConstants.infoFont);
		int offset = g.getFont().getSize();
		
		g.drawString(String.format("%s", showing), x, y += offset);

		if(showing.getCost() != null && showing.getCost().size() > 0 ) {
			g.drawString("Crafting:", x, y += offset);
			
			for(Entry<ItemType, Integer> entry : showing.getCost().entrySet()) {
				g.drawImage(entry.getKey().getImage(0), x, y + offset - offset*2/3, 20, 20, null);
				
				g.drawString(entry.getValue() + " " + entry.getKey(), x + 25, y += offset);
			}
		}
	}
}
