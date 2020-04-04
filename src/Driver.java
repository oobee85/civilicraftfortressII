import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class Driver {
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
	
	public static void rotateAndScaleImage(String filename, String outputFile) {
		double angle = 45;
		double xscale = 1;
		double yscale = 2;
		try {
			BufferedImage image = ImageIO.read(new File(filename));
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
		//rotateAndScaleImage("irrigation.png", "rotated.png");
		
		new Frame();
	}


}
