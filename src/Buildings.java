import java.awt.*;
import java.awt.image.BufferedImage;


public enum Buildings {
	 	WALL_BRICK (100, "Images/buildings/wall_brick.png"),
	 	WALL_WOOD (50, "Images/buildings/wall_brick.png"),
	 	MINE (10, "Images/buildings/mine256.png"),
	 	IRRIGATION (5, "Images/buildings/irrigation.png")
		;
	
	    private final int health;   
	    private final Image image;
	    
	    
	    Buildings(int hp, String s) {
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
