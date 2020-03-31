import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public enum Terrain {
	    GRASS (5, "Images/grass.jpg"),
	    DIRT  (4, "Images/dirt.jpg"),
	    MAGMA  (1, "Images/magma.png")
		;
	    private final int moveSpeed;   
	    
	    private final BufferedImage image;
	    
	    
	    Terrain(int speed, String s) {
	        this.moveSpeed = speed;
	        this.image = Utils.loadImage(50, 50, s);
	    }
	    
	    public BufferedImage getImage() {
	    	
	    	return image;
	    }
	    
	    private int moveSpeed() {
	    	return moveSpeed; 
	    }


	  
	}
	
	

