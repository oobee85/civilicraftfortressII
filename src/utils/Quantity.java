package utils;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Quantity implements HasImage{

	private HasImage hasImage;
	private int amount;
	
	public Quantity(int amount) {
		this.amount = amount;
	}

	@Override
	public Image getImage(int size) {
		return hasImage.getImage(size);
	}
	@Override
	public ImageIcon getImageIcon(int size) {
		return hasImage.getImageIcon(size);
	}
}
