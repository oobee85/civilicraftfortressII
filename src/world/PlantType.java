package world;

import java.awt.*;

import javax.swing.*;

import game.ItemType;
import utils.*;

public enum PlantType implements HasImage {
	
	BERRY ( new String[] {"resources/Images/plants/berry16.png", "resources/Images/plants/berry128.png"} , 1.0, 50, false, ItemType.FOOD),
	BERRY_DEPLETED ( new String[] {"resources/Images/plants/berry_depleted16.png", "resources/Images/plants/berry_depleted128.png"} , 0.2, 1, false, null),
	CATTAIL ( new String[] {"resources/Images/plants/cattail32.png"} , 1.0, 50, true, ItemType.FOOD),
	FOREST1 ( new String[] {"resources/Images/plants/tree1.png"}, 1, 100, false, ItemType.WOOD),
//	FOREST2 ( new String[] {"resources/Images/plants/tree2.png"}, 1, 50)
	;
	
    private double rarity;
    private MipMap mipmap;
    private double health;	
    private boolean aquatic;
    private ItemType itemType;
    
	PlantType( String[] s, double rare, double health, boolean aquatic, ItemType itemType){
		rarity = rare;
		this.health = health;
		this.aquatic = aquatic;
		mipmap = new MipMap(s);
		this.itemType = itemType;
	}
	
	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}

	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}
    
	public ItemType getItem() {
		return itemType;
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
