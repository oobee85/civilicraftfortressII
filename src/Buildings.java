import java.awt.*;
import java.awt.image.BufferedImage;


public enum Buildings {
	 	WALL_BRICK (5, "Images/wall_brick.png"),
	 	WALL_WOOD (5, "Images/wall_brick.png")
	 	
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
