package utils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.*;

public final class Utils {
	
	public static final HashMap<String, Image> roadImages;
	static {
		roadImages = new HashMap<>();
		roadImages.put("top_down", loadImage("resources/Images/road/road_up.png"));
		roadImages.put("left_right", loadImage("resources/Images/road/road_left_right.png"));
		
		roadImages.put("left_down", loadImage("resources/Images/road/road_left_down.png"));
		roadImages.put("left_up", loadImage("resources/Images/road/road_left_up.png"));
		roadImages.put("right_down", loadImage("resources/Images/road/road_right_down.png"));
		roadImages.put("right_up", loadImage("resources/Images/road/road_right_up.png"));

	}

	public static final Image getDefaultSkin() {
		Image temp = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = temp.getGraphics();
		g.drawLine(0, 0, 50, 50);
		g.dispose();
		return temp;
	}

	public static final ImageIcon loadImageIcon(String filename) {
		URL a = Utils.class.getClassLoader().getResource(filename);
		if (a != null) {
			return new ImageIcon(a);
		}
		else {
			System.err.println("FAILED TO LOAD FILE " + filename);
		}
		return null;
	}
	
	public static final Image loadImage(String filename) {
		ImageIcon icon = loadImageIcon(filename);
		if(icon != null) {
			return icon.getImage();
		}
		else {
			return Utils.getDefaultSkin();
		}
	}
	
	public static final ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	
	/**
	 * @param alpha 1 alpha is opaque, 0 alpha is completely transparent
	 * @param g
	 */
	public static void setTransparency(Graphics g, float alpha) {
	    ((Graphics2D)g).setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	}
	
	/**
	 * result is top*alpha + bottom*(1-alpha)
	 * @param top
	 * @param bottom
	 * @param alpha
	 */
	public static Color blendColors(Color top, Color bottom, float alpha) {
		return new Color(snap((int) (top.getRed()*alpha + bottom.getRed()*(1-alpha))), 
				snap((int) (top.getGreen()*alpha + bottom.getGreen()*(1-alpha))),
				snap((int) (top.getBlue()*alpha + bottom.getBlue()*(1-alpha))));
	}
	private static int snap(int color) {
		return Math.min(Math.max(color, 0), 255);
	}
	
	public static float getAlphaOfLiquid(double amount) {
		// 1 units of fluid is opaque, linearly becoming transparent at 0 units of fluid.
		float alpha = (float)Math.max(Math.min(amount*8, 1), 0);
		return alpha * alpha;
		//return 1 - (1 - alpha) * (1 - alpha);
	}
	

	public static void normalize(double[][] data) {
		double minValue = data[0][0];
		double maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		System.out.println("Min Terrain Gen Value: " + minValue + ", Max value: " + maxValue);
		// Normalize the heightMap to be between 0 and 1
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				data[i][j] = (data[i][j] - minValue) / (maxValue - minValue);
			}
		}
	}

	public static double[][] smoothingFilter(double[][] data, double radius, double c) {
		double[][] smoothed = new double[data.length][data[0].length];
		// apply smoothing filter
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				int mini = (int) Math.max(0, i-radius);
				int maxi = (int) Math.min(data.length-1, i+radius);
				int minj = (int) Math.max(0, j-radius);
				int maxj = (int) Math.min(data[0].length-1, j+radius);
				double count = 0;
				for(int ii = mini; ii <= maxi; ii++) {
					for(int jj = minj; jj < maxj; jj++) {
						double distance = Math.sqrt((ii-i)*(ii-i) + (jj-j)*(jj-j));
						double gaussian = Math.exp(-distance*distance / c);
						smoothed[i][j] += gaussian * data[ii][jj];
						//smoothed[i][j] += data[ii][jj];
						count += gaussian;
					}
				}
				smoothed[i][j] /= count;
			}
		}
		return smoothed;
	}
}
