package ui.infopanels;

import java.awt.*;

import javax.swing.*;

import ui.*;
import utils.*;

public class InfoPanel extends JPanel {
	
	public static final int IMAGE_SIZE = 100;
	
	private String name;
	private Image image;
	
	protected int y;

	public InfoPanel(String name, Image image) {
		this.name = name;
		this.image = image;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(name == null) {
			return;
		}
		int x = 5;
		if(image != null) {
			Utils.setTransparency(g, 0.2);
			g.drawImage(image, 1, 5, IMAGE_SIZE, IMAGE_SIZE, null);
			Utils.setTransparency(g, 1);
			x = IMAGE_SIZE + 1;
		}
		if(name != null) {
			int underlineoffset = 4;
			g.setFont(KUIConstants.infoFont);
			y = g.getFont().getSize() - 1;
			int stringWidth = g.getFontMetrics().stringWidth(name);
			g.drawString(name, x + underlineoffset, y);
			
			y += 5;
			g.drawLine(x, y, x + stringWidth + underlineoffset*2, y);
		}
	}
}
