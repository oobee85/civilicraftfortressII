package world;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import game.Item;
import game.ItemType;
import game.components.GameComponent;
import utils.Direction;
import utils.MipMap;
import utils.Utils;

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
			
			for (int i = 1; i < Utils.MAX_TILED_BITMAP; i+=2) {
				int bits = i;

				boolean northwest = (bits & Direction.NORTHWEST.tilingBit) != 0;
				boolean northeast = (bits & Direction.NORTHEAST.tilingBit) != 0;
				boolean southeast = (bits & Direction.SOUTHEAST.tilingBit) != 0;
				boolean southwest = (bits & Direction.SOUTHWEST.tilingBit) != 0;
				boolean mirrored = false;
				if ((northwest && !northeast)
							|| ((northwest == northeast) && (southwest && !southeast))) {
		            mirrored = true;
		            bits = (bits & Direction.NONE.tilingBit)
		            		| (bits & Direction.NORTH.tilingBit)
		            		| (northwest ? Direction.NORTHEAST.tilingBit : 0)
		            		| (southwest ? Direction.SOUTHEAST.tilingBit : 0)
		            		| (bits & Direction.SOUTH.tilingBit)
		            		| (southeast ? Direction.SOUTHWEST.tilingBit : 0)
		            		| (northeast ? Direction.NORTHWEST.tilingBit : 0);
		            
		            // 0x93 = 64 (29) + 16 (13) + 8 (5) + 4 (1) + 1;
		            // CENTER NE SE S NW
//		            bits = (bits & Direction.NONE.tilingBit)
//		            		| (bits & Direction.NORTH.tilingBit)
//		            		| (bits & Direction.NORTHWEST.tilingBit)
//		            		| (bits & Direction.SOUTHWEST.tilingBit)
//		            		| (bits & Direction.SOUTH.tilingBit)
//		            		| (bits & Direction.SOUTHEAST.tilingBit)
//		            		| (bits & Direction.NORTHEAST.tilingBit);
		            System.out.println("Mirrored " + i + " to  " + bits);
				}
				
				String filename = tiledImageFolder + "/" + bits + ".png";
				Image tiledImage = Utils.loadImage(filename);
				
				if (mirrored) {
					BufferedImage buf = Utils.toBufferedImage(tiledImage);
					
					BufferedImage mirroredImage = new BufferedImage(buf.getWidth(), buf.getHeight(), buf.getType());
					Graphics g = mirroredImage.getGraphics();
					g.drawImage(buf, buf.getWidth(), 0, -buf.getWidth(), buf.getHeight(), null);
					g.dispose();
					
					tiledImage = mirroredImage;
				}
				tiledImages.put(i, tiledImage);
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
