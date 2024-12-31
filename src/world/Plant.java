package world;

import java.awt.Color;
import java.awt.Image;
import java.io.*;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import game.*;
import game.components.*;
import utils.*;

public class Plant extends Thing implements Serializable {

	private PlantType plantType;
	
	public Plant(PlantType pt, Tile t, Faction faction) {
		super(pt.getHealth(), pt.getMipMap(), faction, t, pt.getInventoryStackSize());
		plantType = pt;
		for(GameComponent c : plantType.getComponents()) {
			this.addComponent(c.getClass(), c);
		}
	}
	public LinkedList<Item> getItem() {
		return plantType.getItem();
	}
	public PlantType getType() {
		return plantType;
	}
	public void setType(PlantType type) {
		this.plantType = type;
	}

	public void setTiledImage(int tileBitmap) {
		super.setMipMap(getType().getTiledMipmap(tileBitmap));
	}
	
	@Override
	public String toString() {
		return plantType.toString();
	}
}
