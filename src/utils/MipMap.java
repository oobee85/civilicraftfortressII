package utils;
import java.awt.*;

import javax.swing.*;

import ui.graphics.opengl.*;

public class MipMap implements HasImage {
	
	private static final Color HIGHLIGHT_COLOR = Color.yellow;

	private final ImageIcon[] mipmaps;
	private final ImageIcon[] shadows;
	private final ImageIcon[] highlights;
	private final int[] mipmapSizes;
	
	private final Color[] avgColors;

	public MipMap(String[] paths) {
		int numFiles = paths.length;
		
		mipmaps = new ImageIcon[numFiles];
		mipmapSizes = new int[numFiles];
		avgColors = new Color[numFiles];
		shadows = new ImageIcon[numFiles];
		highlights = new ImageIcon[numFiles];
		int index = 0;
		for (String s : paths) {
			mipmaps[index] = Utils.loadImageIcon(s);
			mipmapSizes[index] = mipmaps[index].getIconWidth();
			avgColors[index] = Utils.getAverageColor(Utils.toBufferedImage(mipmaps[index].getImage()));
			shadows[index] = Utils.shadowFilter(mipmaps[index]);
			highlights[index] = Utils.highlightFilter(mipmaps[index], HIGHLIGHT_COLOR);
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

	@Override
	public Image getImage(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i].getImage();
			}
		}
		return mipmaps[mipmaps.length - 1].getImage();
	}
	@Override
	public Image getShadow(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return shadows[i].getImage();
			}
		}
		return shadows[shadows.length - 1].getImage();
	}

	@Override
	public Image getHighlight(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return highlights[i].getImage();
			}
		}
		return highlights[highlights.length - 1].getImage();
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i];
			}
		}
		return mipmaps[mipmaps.length - 1];
	}

	@Override
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
