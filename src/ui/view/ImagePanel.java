package ui.view;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private BufferedImage image;
	private String title;
	public ImagePanel(String title, BufferedImage image) {
		this.image = image;
		this.title = title;
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawString(title, 5, 13);
		g.drawImage(image, 0, 15, this.getWidth(), this.getHeight(), null);
	}
}
