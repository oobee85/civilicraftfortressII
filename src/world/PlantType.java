package world;
import java.awt.Image;

import utils.*;




public enum PlantType {
	
	BERRY ( new String[] {"resources/Images/plants/berry16.png", "resources/Images/plants/berry128.png"} , 1.0),
	BERRY_DEPLETED ( new String[] {"resources/Images/plants/berry_depleted16.png", "resources/Images/plants/berry_depleted128.png"} , 0.2),
	CATTAIL ( new String[] {"resources/Images/plants/cattail32.png"} , 1.0),
	;
	
    private double rarity;
    private MipMap mipmap;
    
	PlantType( String[] s, double r){
		rarity = r;
		
		mipmap = new MipMap(s);
	}
	
	public Image getImage(int size) {
		return mipmap.getImage(size);
    }
	
	public double getRarity() {
		return rarity;
	}
	
	
	
}
