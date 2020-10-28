package networking.message;

import java.io.*;

import liquid.*;
import utils.*;

public class TileInfo implements Serializable {

	private final double height;
	private final int faction;
	private final TileLoc tileLoc;
	private final double humidity;
	private final double liquidAmount;
	private final LiquidType liquidType;
	public TileInfo(double height, int faction, TileLoc tileLoc, double humidity, double liquidAmount, LiquidType liquidType) {
		this.height = height;
		this.faction = faction;
		this.tileLoc = tileLoc;
		this.humidity = humidity;
		this.liquidAmount = liquidAmount;
		this.liquidType = liquidType;
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
}
