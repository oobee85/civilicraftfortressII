package ui.infopanels;

import java.awt.*;

import javax.swing.*;

import ui.*;
import utils.*;

public class InfoPanel extends JPanel {
	
	private static final Icon EXPLODE_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/units/bomb.png"), 18, 18);
	public static final int DEFAULT_IMAGE_SIZE = 100;
	
	private int imageSize = DEFAULT_IMAGE_SIZE;
	private String name;
	private Image image;
	private int numButtons;
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
	
	public JButton addButton(String text) {
		Dimension size = new Dimension(80, 20);
		JButton button = KUIConstants.setupButton(text, null, size);
		button.setFont(KUIConstants.buttonFontMini);
		this.setLayout(null);
		this.add(button);
		button.setBounds(DEFAULT_IMAGE_SIZE + 2, size.height*numButtons + 26, size.width, size.height);
		numButtons ++;
		return button;
	}
	
	public static final int EXPLODE_BUTTON_SIZE = 28;
	public JButton addExplodeButton() {
		Dimension size = new Dimension(EXPLODE_BUTTON_SIZE, EXPLODE_BUTTON_SIZE);
		JButton button = KUIConstants.setupButton("", EXPLODE_ICON, size);
		button.setFont(KUIConstants.buttonFontMini);
		this.setLayout(null);
		this.add(button);
		button.setBounds(0, 0, size.width, size.height);
		return button;
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
