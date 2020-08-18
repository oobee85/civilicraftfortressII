package ui.infopanels;

import java.awt.*;

import javax.swing.*;

import ui.*;
import utils.*;

public class InfoPanel extends JPanel {
	
	public static final int DEFAULT_IMAGE_SIZE = 100;
	
	private int imageSize = DEFAULT_IMAGE_SIZE;
	private String name;
	private Image image;
	
	protected int y;

	public InfoPanel(String name, Image image, int imageSize) {
		this(name, image);
		this.imageSize = imageSize;
	}
	
	public InfoPanel(String name, Image image) {
		this.name = name;
		this.image = image;
	}
	
	protected int getImageSize() {
		return imageSize;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(name == null) {
			return;
		}
		int x = 5;
		if(image != null) {
			Utils.setTransparency(g, 0.3);
			g.drawImage(image, 1, 5, imageSize, imageSize, null);
			Utils.setTransparency(g, 1);
			x = imageSize + 1;
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
