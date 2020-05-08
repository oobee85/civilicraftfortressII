package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.Position;

public class Structure {

	private StructureType structureType;
	private Position position;
	private int health;
	private int culture;
	
	public Structure(StructureType structureType, Position pos) {
		this.structureType = structureType;
		this.position = position;
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

	public int getHealth() {
		return health;
	}

}
