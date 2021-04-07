package world;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.graphics.*;
import ui.graphics.opengl.*;
import utils.*;

public enum PlantType implements HasImage, HasMesh {
	
	BERRY ( new String[] {"Images/plants/berry16.png"}, MeshUtils.defaultPlant, false, 1.0, 100, false, ItemType.FOOD),
	BERRY_DEPLETED ( new String[] {"Images/plants/berry_depleted16.png"},MeshUtils.defaultPlant, false, 0.2, 1, false, ItemType.RUNITE_BAR),
	CATTAIL ( new String[] {"Images/plants/cattail32.png"},MeshUtils.defaultPlant, false, 1.0, 50, true, ItemType.FOOD),
	FOREST1 ( new String[] {"Images/plants/tree1.png"},MeshUtils.defaultPlant, false, 1, 100, false, ItemType.WOOD),
	CACTUS ( new String[] {"Images/plants/cactus.png"},MeshUtils.defaultPlant, true, 1, 100, false, ItemType.FOOD),
//	FOREST2 ( new String[] {"Images/plants/tree2.png"}, 1, 50)
	;
	
    private double rarity;
    private MipMap mipmap;
    private Mesh mesh;
    private double health;	
    private boolean aquatic;
    private ItemType itemType;
    private boolean desertResistant;
    
	PlantType( String[] s, Mesh mesh, boolean desertResistant, double rare, double health, boolean aquatic, ItemType itemType){
		this.rarity = rare;
		this.health = health;
		this.aquatic = aquatic;
		mipmap = new MipMap(s);
		this.mesh = mesh;
		this.itemType = itemType;
		this.desertResistant = desertResistant;
		
	}
	
	public boolean isDesertResistant() {
		return desertResistant;
	}
	@Override
	public Mesh getMesh() {
		return mesh;
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
