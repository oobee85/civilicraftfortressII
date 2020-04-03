import java.awt.*;
import java.awt.image.BufferedImage;


public enum Buildings {
	 WALL (5, "Images/wall.png"),
	 WALLR (5, "Images/wall.png"),
	 WALLB (5, "Images/wall.png"),
	 WALLL (5, "Images/wall.png"),
	 WALLCTR (5, "Images/wall.png"),
	 WALLCTL (5, "Images/wall.png"),
	 WALLCBR (5, "Images/wall.png"),
	 WALLCBL (5, "Images/wall.png"),
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
