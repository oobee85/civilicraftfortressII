import java.awt.*;
import java.awt.image.BufferedImage;


public enum Buildings {
	 WALLT (5, "Images/wall.jpg"),
	 WALLR (5, "Images/wall.jpg"),
	 WALLB (5, "Images/wall.jpg"),
	 WALLL (5, "Images/wall.jpg"),
	 WALLCTR (5, "Images/wall.jpg"),
	 WALLCTL (5, "Images/wall.jpg"),
	 WALLCBR (5, "Images/wall.jpg"),
	 WALLCBL (5, "Images/wall.jpg"),
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
