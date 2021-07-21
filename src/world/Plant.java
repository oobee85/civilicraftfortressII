package world;

import java.io.*;

import game.*;
import game.components.*;
import utils.*;

public class Plant extends Thing implements Serializable {

	private PlantType plantType;
	
	public Plant(PlantType pt, Tile t, Faction faction) {
		super(pt.getHealth(), pt.getMipMap(), pt.getMesh(), faction, t);
		plantType = pt;
		for(GameComponent c : plantType.getComponents()) {
			this.addComponent(c.getClass(), c);
		}
	}
	public ItemType getItem() {
		return plantType.getItem();
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
