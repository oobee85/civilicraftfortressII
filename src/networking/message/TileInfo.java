package networking.message;

import java.io.*;

import game.liquid.*;
import utils.*;
import world.*;

public class TileInfo implements Serializable {

	private final double height;
	private final int faction;
	private final TileLoc tileLoc;
	private final double humidity;
	private final double liquidAmount;
	private final LiquidType liquidType;
	private final Terrain terrain;
	public TileInfo(double height, int faction, TileLoc tileLoc, double humidity, double liquidAmount, LiquidType liquidType, Terrain terrain) {
		this.height = height;
		this.faction = faction;
		this.tileLoc = tileLoc;
		this.humidity = humidity;
		this.liquidAmount = liquidAmount;
		this.liquidType = liquidType;
		this.terrain = terrain;
	}
	public double getHeight() {
		return height;
	}
	public int getFaction() {
		return faction;
	}
	public TileLoc getTileLoc() {
		return tileLoc;
	}
	public double getHumidity() {
		return humidity;
	}
	public double getLiquidAmount() {
		return liquidAmount;
	}
	public LiquidType getLiquidType() {
		return liquidType;
	}
	public Terrain getTerrain() {
		return terrain;
	}
	@Override
	public String toString() {
		return "TileInfo " + tileLoc;
	}
}
