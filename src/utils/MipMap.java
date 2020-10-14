package utils;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import utils.*;
import world.*;

public class MipMap {

	private final ImageIcon[] mipmaps;
	private final ImageIcon[] shadows;
	private final int[] mipmapSizes;
	
	private final Color[] avgColors;


	public MipMap(String[] paths) {
		int numFiles = paths.length;
		
		mipmaps = new ImageIcon[numFiles];
		mipmapSizes = new int[numFiles];
		avgColors = new Color[numFiles];
		shadows = new ImageIcon[numFiles];
		int index = 0;
		for (String s : paths) {
			mipmaps[index] = Utils.loadImageIcon(s);
			mipmapSizes[index] = mipmaps[index].getIconWidth();
			avgColors[index] = Utils.getAverageColor(Utils.toBufferedImage(mipmaps[index].getImage()));
			shadows[index] = Utils.shadowFilter(mipmaps[index]);
			index++;
		}
	}
	public MipMap(String[] paths, Color[] averageColor) {
		this(paths);
		int index = 0;
		for (String s : paths) {
			avgColors[index] = averageColor[index];
			index++;
		}
	}
	
	public MipMap(String path) {
		this(new String[] { path});
	}

	public Image getImage(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i].getImage();
			}
		}
		return mipmaps[mipmaps.length - 1].getImage();
	}
	public Image getShadow(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return shadows[i].getImage();
			}
		}
		return shadows[shadows.length - 1].getImage();
	}

	public ImageIcon getImageIcon(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i];
			}
		}
		return mipmaps[mipmaps.length - 1];
	}
	
	public Color getColor(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return avgColors[i];
			}
		}
		return avgColors[avgColors.length - 1];
	}
}
