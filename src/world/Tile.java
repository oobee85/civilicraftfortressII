package world;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;

public class Tile {
	public static final Color TERRITORY_COLOR = Color.pink;
	private boolean isTerritory = false;
	private boolean isSelected = false;
	
	private TileLoc location;
	private double height;
	int minEntitySize = 20;
	
	private String roadCorner;
	
	private Resource resource;
//	private ResourceType resourceType;
	private RoadType roadType;
	private Plant plant;
	private Terrain terr;
	private Building building;
	
	private ConcurrentLinkedQueue<Unit> units;
	
	public double liquidAmount;
	public LiquidType liquidType;
	
	private List<Tile> neighborTiles = new LinkedList<Tile>();
	
	private Tile(TileLoc location, Terrain t) {
		this.location = location;
		terr = t;
		
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
		units = new ConcurrentLinkedQueue<Unit>();
	}
	
	public static Tile makeTile(TileLoc location, Terrain t) {
		return new Tile(location, t);
	}
	
	public void setRoad(RoadType r, String s) {
		this.roadType = r;
		if(s != null) {
			roadCorner = s;
		}
	}
	public void setTerritory(boolean b) {
		this.isTerritory = b;
	}
	public void setResource(ResourceType o) {
		resource = new Resource(o);
	}
	public Resource getResource() {
		return resource;
	}
	public void setHasPlant(Plant p) {
		plant = p;
	}
	
	
	public Unit getPlayerControlledUnit() {
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				return u;
			}
		}
		return null;
	}
	
	
	public Thing getPlayerControlledThing() {
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				return u;
			}
		}
		if(building != null) {
			return building;
		}
		return null;
	}
	
	public ConcurrentLinkedQueue<Unit> getUnits() {
		return units;
	}
	
	public boolean hasUnit(UnitType unit) {
		for(Unit u : units) {
			if(u.getUnitType() == unit ) {
				return true;
			}
		}
		return false;
	}
	
	private double getBrightnessNonRecursive() {
		double brightness = 0;
		if(this.getHasBuilding() || this.getPlayerControlledThing() != null) {
			brightness += 1;
		}
		if(this.isTerritory) {
			brightness += 0.4;
		}
		brightness += getTerrain().getBrightness();
		brightness += liquidAmount * liquidType.getBrightness();
		return brightness;
	}
	
	public double getBrightness() {
		double brightness = 0;
		brightness += this.getBrightnessNonRecursive();
		for(Tile tile : getNeighbors()) {
			brightness += tile.getBrightnessNonRecursive();
		}
		return brightness;
	}
	
	public void setBuilding(Building b) {
		if (building != null) {
			return;
		}
		if(b != null) {
			building = b;
//			building.setHealth(1);
		}
	}
	
	public void addUnit(Unit u) {
		units.add(u);
	}
	public void removeUnit(Unit u) {
		units.remove(u);
	}
	
	public void drawHeightMap(Graphics g, double height) {
		int r = Math.max(Math.min((int)(255*height), 255), 0);
		g.setColor(new Color(r, 0, 255-r));
		g.fillRect(location.x * Game.tileSize, location.y * Game.tileSize, Game.tileSize, Game.tileSize);
	}
	
	public void drawDebugStrings(Graphics g, List<String> strings, int[][] rows, int fontsize, int stringWidth) {
		int x = location.x * Game.tileSize + 2;
		int y = location.y * Game.tileSize + fontsize/2;
		int row = rows[location.x][location.y];
		
		g.setColor(Color.black);
		g.fillRect(x, y + 2 + row*fontsize, stringWidth, strings.size()*fontsize);
		g.setColor(Color.green);
		for(String s : strings) {
			g.drawString(s, x, y + (++row)*fontsize);
		}
		rows[location.x][location.y] = row;
	}

	
	public boolean isBlocked(Unit u) {
		
		if(u.getUnitType().isFlying()) {
			return false;
		}
		return getHasBuilding() == true && getBuilding().getBuildingType().canMoveThrough() == false;
	}
	
	public RoadType getRoadType() {
		return roadType;
	}
	public Image getRoadImage() {
		return Utils.roadImages.get(roadCorner);
	}
	public boolean getHasBuilding() {
		return building != null;
	}
	public Building getBuilding() {
		return building;
	}
	public Plant getPlant() {
		return plant;
	}
	public Terrain getTerrain() {
		return terr;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public boolean getIsTerritory() {
		return isTerritory;
	}
	public boolean canBuild() {
		return terr.isBuildable(terr) && liquidAmount < liquidType.getMinimumDamageAmount();
	}
	public boolean canPlant() {
		return terr.isPlantable(terr);
	}
	public boolean canOre() {
		return terr.isOreable(terr);
	}
	public void setTerrain(Terrain t) {
		terr = t;
	}
	public boolean canSupportRareOre() {
		return terr.canSupportRare(terr);
	}
	public boolean checkTerrain(Terrain t) {
		return terr == t;
	}
	
	public TileLoc getLocation() {
		return location;
	}
	
	public void setHeight(double newheight) {
		height = newheight;
		if(height > 1) {
			height = 1;
		}
	}
	public double getHeight() {
		return height;
	}
	
	public void setNeighbors(List<Tile> tiles) {
		neighborTiles.clear();
		for(Tile t : tiles) {
			neighborTiles.add(t);
		}
	}
	public List<Tile> getNeighbors() {
		return neighborTiles;
	}
}
