package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.Position;
import world.Tile;

public class Structure {

	private StructureType structureType;
	private Tile tile;
	private double health;
	private int culture;
	
	public Structure(StructureType structureType, Tile tile) {
		this.structureType = structureType;
		this.tile = tile;
		this.health = structureType.getHealth();
	}
	
	
	 public void updateCulture() {
	    	culture += structureType.cultureRate;
//	    	System.out.println("culture: "+culture);
//	    	System.out.println("cultureRate: "+cultureRate);
	    }
	 public int getCulture() {
		 return culture; 
	 }
	 public Image getImage(int size) {
		return structureType.getImage(size);
	}

	public ImageIcon getImageIcon() {
		return structureType.getImageIcon(0);
	}
	public StructureType getStructureType() {
		return structureType;
	}
	
	public Tile getTile() {
		return tile;
	}
	public double getHealth() {
		return health;
	}
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health < 0;
	}

}
