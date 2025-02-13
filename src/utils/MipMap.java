package utils;
import java.awt.*;
import java.util.concurrent.Future;

import javax.swing.*;

public class MipMap {
	
	private static final Color HIGHLIGHT_COLOR = Color.yellow;
	public static final int NUM_SUN_SHADOWS = 10;
	private static final double SUN_SHADOW_SQUISH_FACTOR = 0.4;
	
	private class ImageData {
		int imageSize;
		Color avgColor;
		ImageIcon image;
		ImageIcon shadow;
		ImageIcon[] sunShadows;
		ImageIcon highlight;
		
		public ImageData() {
			imageSize = 1;
			avgColor = Color.magenta;
			image = Utils.getDefaultSkinImageIcon();
			shadow = Utils.getDefaultSkinImageIcon();
			highlight = Utils.getDefaultSkinImageIcon();
			sunShadows = new ImageIcon[NUM_SUN_SHADOWS];
			for (int shearIndex = 0; shearIndex < sunShadows.length; shearIndex++) {
				sunShadows[shearIndex] = Utils.getDefaultSkinImageIcon();
			}
		}
	}

	public ImageData makeDataFromImage(ImageIcon image) {
		ImageData data = new ImageData();
		data.image = image;
		data.imageSize = data.image.getIconWidth();
		data.avgColor = Utils.getAverageColor(Utils.toBufferedImage(data.image.getImage(), false));
		data.shadow = Utils.shadowFilter(data.image);
		data.highlight = Utils.highlightFilter(data.image, HIGHLIGHT_COLOR);
		data.sunShadows = new ImageIcon[NUM_SUN_SHADOWS];
		for (int shearIndex = 0; shearIndex < data.sunShadows.length; shearIndex++) {
			double shear = 3.3 / NUM_SUN_SHADOWS * (shearIndex - (data.sunShadows.length-1.0)/2.0);
			shear = shear * shear * (shear >= 0 ? 1 : -1);
			double squish = SUN_SHADOW_SQUISH_FACTOR - Math.abs(shearIndex - data.sunShadows.length/2)*2*SUN_SHADOW_SQUISH_FACTOR/data.sunShadows.length;
			data.sunShadows[shearIndex] = Utils.sunShadowFilter(data.image, shear, squish);
		}
		return data;
	}

	public ImageData makeDataFromImage(Image image) {
		return makeDataFromImage(new ImageIcon(image));
	}
	
	private final ImageData[] data;
	
	public MipMap(Image image) {
		data = new ImageData[1];
		data[0] = makeDataFromImage(image);
	}

	public MipMap(String[] paths) {
		this(paths, null);
	}
	public MipMap(String[] paths, Color[] averageColor) {
		int numFiles = paths.length;
		data = new ImageData[numFiles];

		// INITIALIZE IMAGES WITH DEFAULT MAGENTA SQUARES
		for (int i = 0; i < data.length; i++) {
			data[i] = new ImageData();
		}

		// LOAD THE ACTUAL IMAGES IN ANOTHER THREAD
		Utils.executorService.submit(() -> {
			int index = 0;
			for (String s : paths) {
				final int myIndex = index;
				ImageIcon image = Utils.loadImageIcon(s);
				if(s.endsWith(".gif")) {
					// resizing animated imageicon is different from static
					image.setDescription(Utils.IMAGEICON_ANIMATED);
				}
				
				data[myIndex] = makeDataFromImage(image);
				
				if (averageColor != null) {
					data[myIndex].avgColor = averageColor[myIndex];
				}
				index++;
			}
		});
	}
	
	public MipMap(String path) {
		this(new String[] {path});
	}
	
	private ImageData getRelevantSizeImageData(int size) {
		// Get the first ImageData that is larger than the tile size
		for (int i = 0; i < data.length; i++) {
			if (data[i].imageSize > size) {
				return data[i];
			}
		}
		return data[data.length - 1];
	}

	public Image getImage(int size) {
		return getRelevantSizeImageData(size).image.getImage();
	}
	public Image getShadow(int size) {
		return getRelevantSizeImageData(size).shadow.getImage();
	}
	public Image getSunShadow(int size, int sun) {
		return getRelevantSizeImageData(size).sunShadows[sun].getImage();
	}

	public Image getHighlight(int size) {
		return getRelevantSizeImageData(size).highlight.getImage();
	}

	public ImageIcon getImageIcon(int size) {
		return getRelevantSizeImageData(size).image;
	}

	public Color getColor(int size) {
		return getRelevantSizeImageData(size).avgColor;
	}
}
