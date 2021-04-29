package world;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.graphics.*;
import ui.graphics.opengl.*;
import utils.*;

public enum PlantType {
	
	BERRY ( new String[] {"Images/plants/berry16.png"}, 
			MeshUtils.cube, 
			"Images/plants/berry16.png",
			false, 1.0, 100, false, ItemType.FOOD),
	BERRY_DEPLETED ( new String[] {"Images/plants/berry_depleted16.png"},
			MeshUtils.cube, 
			"Images/plants/berry_depleted16.png",
			false, 0.2, 1, false, ItemType.RUNITE_BAR),
	CATTAIL ( new String[] {"Images/plants/cattail32.png"},
			MeshUtils.cattail, 
			"textures/pallet1.png",
			false, 1.0, 50, true, ItemType.FOOD),
	TREE ( new String[] {"Images/plants/tree1.png"},
			MeshUtils.getMeshByFileName("models/square_tree.ply"), 
			"Images/plants/tree1.png",
			false, 1, 100, false, ItemType.WOOD),
	CACTUS ( new String[] {"Images/plants/cactus.png"},
			MeshUtils.defaultPlant, 
			"Images/plants/cactus.png",
			true, 1, 100, false, ItemType.FOOD),
	;
	
	private double rarity;
	private MipMap mipmap;
	private TexturedMesh mesh;
	private double health;	
	private boolean aquatic;
	private ItemType itemType;
	private boolean desertResistant;

	PlantType( String[] s, Mesh mesh, String textureFile, boolean desertResistant, double rare, double health, boolean aquatic, ItemType itemType){
		this.rarity = rare;
		this.health = health;
		this.aquatic = aquatic;
		mipmap = new MipMap(s);
		this.mesh = new TexturedMesh(mesh, textureFile);
		this.itemType = itemType;
		this.desertResistant = desertResistant;
		
	}
	
	public boolean isDesertResistant() {
		return desertResistant;
	}
	public TexturedMesh getMesh() {
		return mesh;
	}
	
	public MipMap getMipMap() {
		return mipmap;
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
