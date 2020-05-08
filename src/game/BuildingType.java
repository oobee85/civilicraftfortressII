package game;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import game.*;
import ui.*;
import utils.*;


public enum BuildingType {
	 	WALL_BRICK (1000, "resources/Images/buildings/wall_brick.png"),
	 	WALL_WOOD (100, "resources/Images/buildings/wall_brick.png"),
	 	MINE (100, "resources/Images/buildings/mine256.png"),
	 	IRRIGATION (50, "resources/Images/buildings/irrigation.png"),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png")
		;
	
	    private final int health;   
	    private MipMap mipmap;
	    
	    
	    BuildingType(int hp, String s) {
	        this.health = hp;
	        mipmap = new MipMap(s);
	    }
	    
	    public Image getImage(int size) {
	    	return mipmap.getImage(size);
	    }
	    public ImageIcon getImageIcon(int size) {
	    	return mipmap.getImageIcon(size);
	    }
	    
	    public int getHealth() {
	    	return health; 
	    }


}
