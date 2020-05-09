package game;

import java.awt.*;

import javax.swing.*;

import utils.*;
import world.*;

public class Structure extends Thing {

	private StructureType structureType;
	private Tile tile;
	private int culture;
	
	public Structure(StructureType structureType, Tile tile) {
		super(structureType.getHealth());
		this.structureType = structureType;
		this.tile = tile;
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
}
