import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;


public enum Buildings {
	 	WALL_BRICK (100, "Images/buildings/wall_brick.png"),
	 	WALL_WOOD (50, "Images/buildings/wall_brick.png"),
	 	MINE (10, "Images/buildings/mine256.png"),
	 	IRRIGATION (5, "Images/buildings/irrigation.png")
		;
	
	    private final int health;   
	    private final ImageIcon imageicon;
	    
	    
	    Buildings(int hp, String s) {
	        this.health = hp;
	        this.imageicon = Utils.loadImageIcon(s);
	    }
	    
	    public Image getImage() {
	    	return imageicon.getImage();
	    }
	    public ImageIcon getImageIcon() {
	    	return imageicon;
	    }
	    
	    private int getHealth() {
	    	return health; 
	    }


}
