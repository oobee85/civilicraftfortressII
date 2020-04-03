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
		roadImages.put("top_down", loadImage("Images/road/road_up.png"));
		roadImages.put("left_right", loadImage("Images/road/road_left_right.png"));
		
		roadImages.put("left_down", loadImage("Images/road/road_left_down.png"));
		roadImages.put("left_up", loadImage("Images/road/road_left_up.png"));
		roadImages.put("right_down", loadImage("Images/road/road_right_down.png"));
		roadImages.put("right_up", loadImage("Images/road/road_right_up.png"));

	}

	public static final Image getDefaultSkin() {
		Image temp = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = temp.getGraphics();
		g.drawLine(0, 0, 50, 50);
		g.dispose();
		return temp;
	}

	public static final Image loadImage(String filename) {
		Image temp = Utils.getDefaultSkin();
		URL a = Utils.class.getResource(filename);
		if (a != null) {
			temp = new ImageIcon(a).getImage();
		}

		return temp;
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
}
