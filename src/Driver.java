import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class Driver {
	private static int worldSize = 256;
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
	
	public static void main(String[] run) {
		
		//new Driver();
		
		
		
		
		
		new Frame(Toolkit.getDefaultToolkit().getScreenSize().width * 3/4, Toolkit.getDefaultToolkit().getScreenSize().height * 3/4, worldSize);
	}


}
