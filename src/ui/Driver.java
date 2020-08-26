package ui;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import utils.*;

public class Driver {
	
	public static final boolean SHOW_MENU_ANIMATION = false;
//	
//	public Driver() {
//		Image lava = Utils.loadImage("Images/lava_flow.png");
//		System.out.println(lava.getWidth());
//		System.out.println(lava.getHeight());
//		
//		for(int i = 0; i < lava.getHeight(); i += lava.getWidth()) {
//			BufferedImage subimage = lava.getSubimage(0, i, lava.getWidth(), lava.getWidth());
//			System.out.println(subimage.getWidth());
//			System.out.println(subimage.getHeight());
//			try {
//				ImageIO.write(subimage, "png", new File("D:/Workspace/lava_" + i/lava.getWidth() + ".png"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
	
	public static void resizeImage(String filename, String outputFile, int width, int height) {
		try {
			BufferedImage image = ImageIO.read(new File(filename));
			BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = target.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.drawImage(image, 0, 0, width, height, null);
			g.dispose();
			ImageIO.write(target, "png", new File(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void rotateAndScaleImage(String filename, String outputFile) {
		double angle = 0;
		
		try {
			BufferedImage image = ImageIO.read(new File(filename));
			double xscale = 16.0/image.getWidth();
			double yscale = 16.0/image.getHeight();
			// make image that is twice as big
			BufferedImage rotated = new BufferedImage(image.getWidth() + image.getHeight(), image.getHeight() + image.getWidth(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = rotated.createGraphics();
			g.setColor(Color.gray);
			g.fillRect(0, 0, rotated.getWidth(), rotated.getHeight());
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.translate(400, 0);
			g.rotate(angle * 2 * Math.PI / 360);
			g.scale(xscale, yscale);
			g.drawImage(image, 0, 0, null);
			g.dispose();
			ImageIO.write(rotated, "png", new File(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] run) {
//		rotateAndScaleImage("fish2.png", "rotated.png");
//		resizeImage("roadtile.png", "newroadtile.png", 16, 16);
//		ImageCreation.createRoadImages("roadtile.png");
		new Frame();
	}


}
