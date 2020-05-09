package game;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import utils.*;

public enum StructureType {
	
	CASTLE (5000, "resources/Images/buildings/castle256.png", 80, 5),
	BARRACKS (1000, "resources/Images/buildings/barracks256.png", 0, 1)
	;
	
    private final double health;   
	private MipMap mipmap;
    private int culture;
    public int cultureRate;
    
    StructureType(double hp, String s, int culture, int cultureRate) {
        this.health = hp;
        this.culture = culture;
        this.cultureRate = cultureRate;
        this.mipmap = new MipMap(s);
        
    }
    
    public Image getImage(int size) {
    	return mipmap.getImage(size);
    }
    public ImageIcon getImageIcon(int size) {
    	return mipmap.getImageIcon(size);
    }
    
    public double getHealth() {
    	return health; 
    }
    public int getCulture() {
    	return culture; 
    }
    
    
}



