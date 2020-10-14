package utils;

import java.awt.*;

import javax.swing.*;

public interface HasImage {
	public Image getImage(int size);
	public Image getShadow(int size);
    public ImageIcon getImageIcon(int size);
    public Color getColor(int size);
}
