package world;
import java.awt.Image;

import utils.*;




public enum PlantType {
	
	BERRY ( new String[] {"resources/Images/plants/berry16.png", "resources/Images/plants/berry128.png"} , 1.0, 5, false),
	BERRY_DEPLETED ( new String[] {"resources/Images/plants/berry_depleted16.png", "resources/Images/plants/berry_depleted128.png"} , 0.2, 5, false),
	CATTAIL ( new String[] {"resources/Images/plants/cattail32.png"} , 1.0, 5, true),
	FOREST1 ( new String[] {"resources/Images/forest/tree1.png"}, 1, 50, false),
//	FOREST2 ( new String[] {"resources/Images/forest/tree2.png"}, 1, 50)
	;
	
    private double rarity;
    private MipMap mipmap;
    private double health;	
    private boolean aquatic;
    
	PlantType( String[] s, double rare, double health, boolean aquatic){
		rarity = rare;
		this.health = health;
		this.aquatic = aquatic;
		mipmap = new MipMap(s);
		
	}
	
	public Image getImage(int size) {
		return mipmap.getImage(size);
    }
	public double getHealth() {
		return health;
	}
	public double getRarity() {
		return rarity;
	}
	public boolean isAquatic() {
		return aquatic;
	}
	
	
	
}
