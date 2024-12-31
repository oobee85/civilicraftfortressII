package utils;
import java.awt.*;

import javax.swing.*;

public class MipMap {
	
	private static final Color HIGHLIGHT_COLOR = Color.yellow;
	public static final int NUM_SUN_SHADOWS = 10;
	private static final double SUN_SHADOW_SQUISH_FACTOR = 0.4; 

	private final ImageIcon[] mipmaps;
	private final ImageIcon[] shadows;
	private final ImageIcon[][] sunShadows;
	private final ImageIcon[] highlights;
	private final int[] mipmapSizes;
	
	private final Color[] avgColors;
	
	public MipMap(Image image) {
		int numFiles = 1;
		
		mipmaps = new ImageIcon[numFiles];
		mipmapSizes = new int[numFiles];
		avgColors = new Color[numFiles];
		shadows = new ImageIcon[numFiles];
		sunShadows = new ImageIcon[numFiles][NUM_SUN_SHADOWS];
		highlights = new ImageIcon[numFiles];
		int index = 0;
		mipmaps[index] = new ImageIcon(image);
		mipmapSizes[index] = mipmaps[index].getIconWidth();
		avgColors[index] = Utils.getAverageColor(Utils.toBufferedImage(mipmaps[index].getImage(), false));
		shadows[index] = Utils.shadowFilter(mipmaps[index]);
		highlights[index] = Utils.highlightFilter(mipmaps[index], HIGHLIGHT_COLOR);
		for (int shearIndex = 0; shearIndex < sunShadows[index].length; shearIndex++) {
			double shear = 3.3 / NUM_SUN_SHADOWS * (shearIndex - (sunShadows[index].length-1.0)/2.0);
			shear = shear * shear * (shear >= 0 ? 1 : -1);
			double squish = SUN_SHADOW_SQUISH_FACTOR - Math.abs(shearIndex - sunShadows[index].length/2)*2*SUN_SHADOW_SQUISH_FACTOR/sunShadows[index].length;
			sunShadows[index][shearIndex] = Utils.sunShadowFilter(mipmaps[index], shear, squish);
		}
	}

	public MipMap(String[] paths) {
		int numFiles = paths.length;
		
		mipmaps = new ImageIcon[numFiles];
		mipmapSizes = new int[numFiles];
		avgColors = new Color[numFiles];
		shadows = new ImageIcon[numFiles];
		sunShadows = new ImageIcon[numFiles][NUM_SUN_SHADOWS];
		highlights = new ImageIcon[numFiles];
		int index = 0;
		for (String s : paths) {
			mipmaps[index] = Utils.loadImageIcon(s);
			if(s.endsWith(".gif")) {
				// resizing animated imageicon is different from static
				mipmaps[index].setDescription(Utils.IMAGEICON_ANIMATED);
			}
			mipmapSizes[index] = mipmaps[index].getIconWidth();
			avgColors[index] = Utils.getAverageColor(Utils.toBufferedImage(mipmaps[index].getImage(), false));
			shadows[index] = Utils.shadowFilter(mipmaps[index]);
			highlights[index] = Utils.highlightFilter(mipmaps[index], HIGHLIGHT_COLOR);
			for (int shearIndex = 0; shearIndex < sunShadows[index].length; shearIndex++) {
				double shear = 3.3 / NUM_SUN_SHADOWS * (shearIndex - (sunShadows[index].length-1.0)/2.0);
				shear = shear * shear * (shear >= 0 ? 1 : -1);
				double squish = SUN_SHADOW_SQUISH_FACTOR - Math.abs(shearIndex - sunShadows[index].length/2)*2*SUN_SHADOW_SQUISH_FACTOR/sunShadows[index].length;
				sunShadows[index][shearIndex] = Utils.sunShadowFilter(mipmaps[index], shear, squish);
			}

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
		this(new String[] {path});
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
	public Image getSunShadow(int size, int sun) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return sunShadows[i][sun].getImage();
			}
		}
		return sunShadows[shadows.length - 1][sun].getImage();
	}

	public Image getHighlight(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return highlights[i].getImage();
			}
		}
		return highlights[highlights.length - 1].getImage();
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
