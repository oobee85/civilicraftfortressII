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
		// Images/buildings/wall_brick.png is a placeholder image so mipmap doesnt fail to load
		MipMap mipmap = new MipMap("Images/buildings/wall_brick.png") {
			@Override
			public Image getImage(int size) {
				return getType().getTiledImage(tileBitmap);
			}
			@Override
			public Image getShadow(int size) {
				return getType().getMipMap().getShadow(size);
			}
			@Override
			public Image getSunShadow(int size, int sun) {
				return getType().getMipMap().getSunShadow(size, sun);
			}
			@Override
			public Image getHighlight(int size) {
				return getType().getMipMap().getHighlight(size);
			}
			@Override
			public ImageIcon getImageIcon(int size) {
				return getType().getMipMap().getImageIcon(size);
			}
			@Override
			public Color getColor(int size) {
				return getType().getMipMap().getColor(size);
			}
		};
		super.setMipMap(mipmap);
	}
	
	@Override
	public String toString() {
		return plantType.toString();
	}
}
