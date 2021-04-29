package world;

import java.io.*;

import game.*;
import utils.*;

public class Plant extends Thing implements Serializable {

	private PlantType plantType;
	
	public Plant(PlantType pt, Tile t, Faction faction) {
		super(pt.getHealth(), pt.getHasImage(), pt, faction, t);
		plantType = pt;
	}
	public ItemType getItem() {
		return plantType.getItem();
	}
	public boolean isAquatic() {
		return plantType.isAquatic();
	}
	public PlantType getType() {
		return plantType;
	}
	public void setType(PlantType type) {
		this.plantType = type;
	}
	
	@Override
	public String toString() {
		return plantType.toString();
	}
}
