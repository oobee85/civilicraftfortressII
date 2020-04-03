import java.awt.*;
import java.awt.image.BufferedImage;

public enum Structure {
	
	CASTLE (500, "Images/buildings/castle256.png"),
	BARRACKS (100, "Images/buildings/barracks256.png")
	;
	
    private final int health;   
    private final Image image;
    
    Structure(int hp, String s) {
        this.health = hp;
        this.image = Utils.loadImage(s);
    }
    
    public Image getImage() {
    	return image;
    }
    
    private int getHealth() {
    	return health; 
    }

    
}



