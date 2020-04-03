import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public enum Terrain {
	    GRASS (10, new String[] {"Images/grass/grass16.png", "Images/grass/grass128.png", "Images/grass/grass512.png"}),
	    DIRT  (8, new String[] {"Images/dirt/dirt16.png", "Images/dirt/dirt128.png", "Images/dirt/dirt512.png"}),
	    LAVA  (0, new String[] {"Images/lava/lava16.png", "Images/lava/lavaanim32.gif", "Images/lava/lava128.gif", "Images/lava/lava512.png"}),
	    VOLCANO (1, new String[] {"Images/lava/volcano16.png", "Images/lava/volcano128.png", "Images/lava/magma512.png"}),
	    ROCK (2, new String[] {"Images/mountain/rock16.png", "Images/mountain/rock128.png"}),
	    ROCK_SNOW (1, new String[] {"Images/mountain/rock_snow16.png", "Images/mountain/rock_snow128.png"}),
	    WATER (0, new String[] {"Images/water/water16.png", "Images/water/water128.png", "Images/water/water512.png"})
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
	    
	    public boolean isBuildable(Terrain t) {
	    	if(t==Terrain.VOLCANO || t==Terrain.LAVA || t==Terrain.ROCK_SNOW || t==Terrain.WATER) {
	    		return false;
	    	}
	    	return true;
	    	
	    }

	  
	    
	}
	
	

