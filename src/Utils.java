import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

public final class Utils {
	
	public static final HashMap<String, BufferedImage> roadImages;
	static {
		roadImages = new HashMap<>();
		roadImages.put("top_down", loadImage("Images/road.png"));
		roadImages.put("left_right", loadImage("Images/road_left_right.png"));
	}

    public static final BufferedImage getDefaultSkin() {
        BufferedImage temp = new BufferedImage(50,50,BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = temp.getGraphics();
        g.drawLine(0, 0, 50, 50);
        g.dispose();
        return temp;
    }
    
    public static final BufferedImage loadImage(String filename) {
        BufferedImage temp = Utils.getDefaultSkin();
        try {
        	URL a = Utils.class.getResource(filename);
        	if(a!=null) {
        		temp = ImageIO.read(a);
        	}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        return temp;
    }
}
