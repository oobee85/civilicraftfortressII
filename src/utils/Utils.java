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
	public static void resetTransparent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	    g2d.setComposite(ac);
	}
	public static void setTransparent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	    g2d.setComposite(ac);
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
