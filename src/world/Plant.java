package world;
import java.awt.Image;

import utils.*;

public enum Plant {
	
	BERRY ( 100, new String[] {"resources/Images/plants/berry16.png", "resources/Images/plants/berry128.png"} , 1.0),
	BERRY_DEPLETED ( 0, new String[] {"resources/Images/plants/berry_depleted16.png", "resources/Images/plants/berry_depleted128.png"} , 0.2),
	CATTAIL ( 100, new String[] {"resources/Images/plants/cattail32.png"} , 1.0),
	;
	
    private int yield;
    private double rarity;
    private MipMap mipmap;
    
	Plant(int y, String[] s, double r){
		yield = y;
		rarity = r;
		
		mipmap = new MipMap(s);
	}
	
	public Image getImage(int size) {
		return mipmap.getImage(size);
    }
	
	public int getYield() {
		return yield;
	}
	public double getRarity() {
		return rarity;
	}
	
	
	
}
