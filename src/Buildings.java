import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;


public enum Buildings {
	 	WALL_BRICK (100, "resources/Images/buildings/wall_brick.png"),
	 	WALL_WOOD (50, "resources/Images/buildings/wall_brick.png"),
	 	MINE (10, "resources/Images/buildings/mine256.png"),
	 	IRRIGATION (5, "resources/Images/buildings/irrigation.png"),
	 	BRIDGE (10, "resources/Images/buildings/bridge.png")
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
	    public BuildMode getToBuildMode(Buildings b) {
	    	if(b == Buildings.WALL_BRICK) {
	    		return BuildMode.WALL;
	    	}
	    	return null;
	    }
	    
	    private int getHealth() {
	    	return health; 
	    }


}
