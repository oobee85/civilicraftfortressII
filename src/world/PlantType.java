package world;

import java.io.*;
import java.util.*;

import game.*;
import game.components.*;
import ui.graphics.opengl.*;
import utils.*;

public class PlantType implements Serializable {
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final TexturedMesh mesh;
	private transient final double health;	
	private transient final double rarity;
	private transient final ItemType itemType;
	private transient final HashSet<String> attributes;
	private transient final Set<GameComponent> components = new HashSet<>();

	public PlantType(String name, String image, Mesh mesh, String textureFile, 
	                 double rare, double health, ItemType itemType, HashSet<String> attributes){
		this.name = name;
		this.rarity = rare;
		this.health = health;
		mipmap = new MipMap(image);
		this.mesh = new TexturedMesh(mesh, textureFile);
		this.itemType = itemType;
		this.attributes = attributes;
	}
	
	// TODO remove desertresistant attribute and instead use whether or not the tile would take damage on the tile
	public boolean isDesertResistant() {
		return attributes.contains("desertresistant");
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
	public Set<GameComponent> getComponents() {
		return components;
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return null;
	}
	public String name() {
		return name;
	}
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
