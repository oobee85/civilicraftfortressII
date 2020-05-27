package utils;

import java.awt.*;

import javax.swing.ImageIcon;

public class Quantity implements HasImage {

	private HasImage hasImage;
	private volatile int amount;
	
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
	@Override
	public Color getColor(int size) {
		return hasImage.getColor(size);
	}
	public int getAmount() {
		return amount;
	}
	public void addAmount(int i) {
		amount += i;
	}
}
