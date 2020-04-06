import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public enum Structure {
	
	CASTLE (500, "resources/Images/buildings/castle256.png", 80, 5),
	BARRACKS (100, "resources/Images/buildings/barracks256.png", 0, 1)
	;
	
    private final int health;   
    private final ImageIcon imageicon;
    private int culture;
    private int cultureRate;
    
    Structure(int hp, String s, int c, int cr) {
        this.health = hp;
        this.culture = c;
        this.cultureRate = cr;
        this.imageicon = Utils.loadImageIcon(s);
        
    }
    
    public Image getImage() {
    	return imageicon.getImage();
    }
    public ImageIcon getImageIcon() {
    	return imageicon;
    }
    
    public int getHealth() {
    	return health; 
    }
    public int getCulture() {
    	return culture; 
    }
    public int getCultureRate() {
    	return cultureRate;
    }
    public void updateCulture() {
    	culture += cultureRate;
    	System.out.println("culture: "+culture);
    	System.out.println("cultureRate: "+cultureRate);
    }
    
}



