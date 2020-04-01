import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public enum Terrain {
	    GRASS (5, new String[] {"Images/grass.jpg", "Images/grass512.png"}),
	    DIRT  (4, new String[] {"Images/dirt16.png", "Images/dirt.jpg", "Images/dirt512.png"}),
	    MAGMA  (1, new String[] {"Images/lava16.png", "Images/lavaanim.gif"})
	    ;
	
	    private final int moveSpeed;   
	    
	    private final Image[] mipmaps;
	    private final int[] mipmapSizes;
	    
	    
	    Terrain(int speed, String[] s) {
	        this.moveSpeed = speed;
	        mipmaps = new Image[s.length];
	        mipmapSizes = new int[s.length];
	        for(int i = 0; i < s.length; i++) {
	        	mipmaps[i] = Utils.loadImage(s[i]);
	        	mipmapSizes[i] = mipmaps[i].getWidth(null);
	        }
	    }
	    
	    public Image getImage(int size) {
	    	// Get the first mipmap that is larger than the tile size
	    	for(int i = 0; i < mipmapSizes.length; i++) {
	    		if(mipmapSizes[i] > size) {
	    			return mipmaps[i];
	    		}
	    	}
	    	return mipmaps[mipmaps.length-1];
	    }
	    
	    private int moveSpeed() {
	    	return moveSpeed; 
	    }


	  
	}
	
	

