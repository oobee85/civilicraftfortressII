package world;

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;

import game.ItemType;
import utils.*;

public enum PlantType implements HasImage {
	
	BERRY ( new String[] {"resources/Images/plants/berry16.png"}, false, 1.0, 50, false, ItemType.FOOD),
	BERRY_DEPLETED ( new String[] {"resources/Images/plants/berry_depleted16.png"},false, 0.2, 1, false, ItemType.RUNITE_BAR),
	CATTAIL ( new String[] {"resources/Images/plants/cattail32.png"},false, 1.0, 50, true, ItemType.FOOD),
	FOREST1 ( new String[] {"resources/Images/plants/tree1.png"},false, 1, 500, false, ItemType.WOOD),
	CACTUS ( new String[] {"resources/Images/plants/cactus.png"},true, 1, 50, false, ItemType.FOOD),
//	FOREST2 ( new String[] {"resources/Images/plants/tree2.png"}, 1, 50)
	;
	
    private double rarity;
    private MipMap mipmap;
    private double health;	
    private boolean aquatic;
    private ItemType itemType;
    private boolean desertResistant;
    
	PlantType( String[] s, boolean desertResistant, double rare, double health, boolean aquatic, ItemType itemType){
		rarity = rare;
		this.health = health;
		this.aquatic = aquatic;
		mipmap = new MipMap(s);
		this.itemType = itemType;
		this.desertResistant = desertResistant;
		
	}
	
	public boolean isDesertResistant() {
		return desertResistant;
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
	public Image getHighlight(int size) {
		return mipmap.getHighlight(size);
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
	
	public HashMap<ItemType, Integer> getCost(){
		return null;
	}
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
