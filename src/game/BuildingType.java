package game;

import java.awt.*;
import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	WALL_BRICK (1000, "resources/Images/buildings/wall_brick.png"),
	 	WALL_WOOD (100, "resources/Images/buildings/wall_brick.png"),
	 	MINE (100, "resources/Images/buildings/mine256.png"),
	 	IRRIGATION (50, "resources/Images/buildings/irrigation.png"),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png")
		;
	
	    private final double health;   
	    private MipMap mipmap;
	    
	    
	    BuildingType(double hp, String s) {
	        this.health = hp;
	        mipmap = new MipMap(s);
	    }
	    
	    @Override
	    public Image getImage(int size) {
	    	return mipmap.getImage(size);
	    }
	    @Override
	    public ImageIcon getImageIcon(int size) {
	    	return mipmap.getImageIcon(size);
	    }
	    
	    public double getHealth() {
	    	return health; 
	    }


}
