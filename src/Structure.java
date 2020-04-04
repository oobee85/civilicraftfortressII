import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public enum Structure {
	
	CASTLE (500, "Images/buildings/castle256.png"),
	BARRACKS (100, "Images/buildings/barracks256.png")
	;
	
    private final int health;   
    private final ImageIcon imageicon;
    
    Structure(int hp, String s) {
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



