import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public enum Structure {
	
	CASTLE (500, "resources/Images/buildings/castle256.png", 100),
	BARRACKS (100, "resources/Images/buildings/barracks256.png", 0)
	;
	
    private final int health;   
    private final ImageIcon imageicon;
    private int culture;
    
    Structure(int hp, String s, int c) {
        this.health = hp;
        this.culture = c;
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
    
}



