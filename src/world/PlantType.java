package world;

import java.awt.*;

import javax.swing.*;
import utils.*;

public enum PlantType implements HasImage {
	
	BERRY ( new String[] {"resources/Images/plants/berry16.png", "resources/Images/plants/berry128.png"} , 1.0, 50, false),
	BERRY_DEPLETED ( new String[] {"resources/Images/plants/berry_depleted16.png", "resources/Images/plants/berry_depleted128.png"} , 0.2, 1, false),
	CATTAIL ( new String[] {"resources/Images/plants/cattail32.png"} , 1.0, 50, true),
	FOREST1 ( new String[] {"resources/Images/plants/tree1.png"}, 1, 100, false),
//	FOREST2 ( new String[] {"resources/Images/plants/tree2.png"}, 1, 50)
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
	
	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
    }
    @Override
    public ImageIcon getImageIcon(int size) {
    	return mipmap.getImageIcon(size);
    }
	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
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

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
