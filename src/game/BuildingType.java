package game;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import game.*;
import ui.*;
import utils.*;


public enum BuildingType {
	 	WALL_BRICK (100, "resources/Images/buildings/wall_brick.png"),
	 	WALL_WOOD (50, "resources/Images/buildings/wall_brick.png"),
	 	MINE (10, "resources/Images/buildings/mine256.png"),
	 	IRRIGATION (5, "resources/Images/buildings/irrigation.png"),
	 	BRIDGE (10, "resources/Images/buildings/bridge.png")
		;
	
	    private final int health;   
	    private MipMap mipmap;
	    
	    
	    BuildingType(int hp, String s) {
	        this.health = hp;
	        mipmap = new MipMap(s);
	    }
	    
	    public Image getImage() {
	    	return mipmap.getImage(0);
	    }
	    public ImageIcon getImageIcon() {
	    	return mipmap.getImageIcon(0);
	    }
	    public BuildMode getToBuildMode(BuildingType b) {
	    	if(b == BuildingType.WALL_BRICK) {
	    		return BuildMode.WALL;
	    	}
	    	return null;
	    }
	    
	    private int getHealth() {
	    	return health; 
	    }


}
