package world;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import game.*;
import game.components.*;
import utils.*;

public class PlantType implements Serializable {
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final HashMap<Integer, Image> tiledImages;
	private transient final double health;	
	private transient final double rarity;
	private transient final LinkedList<Item> harvestItems;
	private transient final HashSet<String> attributes;
	private transient final int inventoryStackSize;
	private transient final Set<GameComponent> components = new HashSet<>();

	public PlantType(String name, String image, String tiledImageFolder,
	                 double rare, double health,
	                 LinkedList<Item> harvestItems, HashSet<String> attributes, int inventoryStackSize){
		this.name = name;
		this.rarity = rare;
		this.health = health;
		mipmap = new MipMap(image);
		this.harvestItems = harvestItems;
		this.attributes = attributes;
		this.inventoryStackSize = inventoryStackSize;
		
		if (tiledImageFolder == null) {
			tiledImages = null;
		}
		else {
			tiledImages = new HashMap<>();
			
			for (int i = 0; i < Utils.MAX_TILED_BITMAP; i++) {
				String filename = tiledImageFolder + "/" + i + ".png";
				tiledImages.put(i, Utils.loadImage(filename));
			}
		}
	}
	
	public boolean isTiledImage() {
		return tiledImages != null;
	}
	
	public Image getTiledImage(int tileBitmap) {
		return tiledImages.get(tileBitmap);
	}

	public int getInventoryStackSize() {
		return inventoryStackSize;
	}
	
	// TODO remove desertresistant attribute and instead use whether or not the plant would take damage on the tile
	public boolean isDesertResistant() {
		return attributes.contains("desertresistant");
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}

	public LinkedList<Item> getItem() {
		return harvestItems;
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
