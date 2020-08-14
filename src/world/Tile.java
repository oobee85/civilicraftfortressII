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
	private GroundModifier modifier;
	
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
		if (s != null) {
			roadCorner = s;
		}
	}

	public void setTerritory(boolean b) {
		this.isTerritory = b;
	}

	public void setResource(ResourceType o) {
		if(o == null ) {
			resource = null;
			return;
		}
		resource = new Resource(o);
	}

	public Resource getResource() {
		return resource;
	}
	public GroundModifier getModifier() {
		return modifier;
	}

	public void setHasPlant(Plant p) {
		plant = p;
	}

	public Unit getPlayerControlledUnit() {
		for (Unit u : units) {
			if (u.isPlayerControlled()) {
				return u;
			}
		}
		return null;
	}

	public Thing getPlayerControlledThing() {
		for (Unit u : units) {
			if (u.isPlayerControlled()) {
				return u;
			}
		}
		if (building != null) {
			return building;
		}
		return null;
	}

	public ConcurrentLinkedQueue<Unit> getUnits() {
		return units;
	}

	public boolean hasUnit(UnitType unit) {
		for (Unit u : units) {
			if (u.getUnitType() == unit) {
				return true;
			}
		}
		return false;
	}

	private double getBrightnessNonRecursive() {
		double brightness = 0;
		if (this.getHasBuilding() || this.getPlayerControlledThing() != null) {
			brightness += 1;
		}
		if (this.isTerritory) {
			brightness += 0.4;
		}
		brightness += getTerrain().getBrightness();
		brightness += liquidAmount * liquidType.getBrightness();
		if(modifier != null) {
			brightness += getModifier().getType().getBrightness();
		}
		return brightness;
	}

	public double getBrightness() {
		double brightness = 0;
		brightness += this.getBrightnessNonRecursive();
		for (Tile tile : getNeighbors()) {
			brightness += tile.getBrightnessNonRecursive();
		}
		return brightness;
	}

	public void setBuilding(Building b) {
		if (building != null) {
			return;
		}
		if (b != null) {
			building = b;
//			building.setHealth(1);
		}
	}
	public void setModifier(GroundModifier gm) {
		modifier = gm;
	}

	public void addUnit(Unit u) {
		units.add(u);
	}

	public void removeUnit(Unit u) {
		units.remove(u);
	}

	public void drawHeightMap(Graphics g, double height) {
		int r = Math.max(Math.min((int) (255 * height), 255), 0);
		g.setColor(new Color(r, 0, 255 - r));
		g.fillRect(location.x * Game.tileSize, location.y * Game.tileSize, Game.tileSize, Game.tileSize);
	}

	public int drawDebugStrings(Graphics g, List<String> strings, int row, int fontsize) {
		int x = location.x * Game.tileSize + 2;
		int y = location.y * Game.tileSize + fontsize / 2;
		int maxWidth = 0;
		for (String s : strings) {
			int stringWidth = g.getFontMetrics().stringWidth(s)+2;
			maxWidth = maxWidth > stringWidth ? maxWidth : stringWidth;
		}
		g.setColor(Color.black);
		g.fillRect(x, y + 2 + row, maxWidth, fontsize * strings.size());
		for (String s : strings) {
			g.setColor(Color.green);
			row += fontsize;
			g.drawString(s, x, y + row);
		}
		row += 1;
		return row;
	}

	public boolean isBlocked(Unit u) {

		if (u.getUnitType().isFlying()) {
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

	public boolean canMove(Unit u) {
		if(u.getType().isFlying()) {
			return true;
		}
		if (building == null) {
			return true;
		}
		BuildingType bt = building.getBuildingType();
		if(bt == BuildingType.WALL_WOOD || bt == BuildingType.WALL_STONE || bt == BuildingType.WALL_BRICK) {
			return false;
		}
		return true;
	}
	
	public int computeTileDamage(Unit unit) {
		double damage = 0;
		if(unit.getType().isFlying()) {
			
		}
		else {
			if(unit.getType().isAquatic()) {
				if(liquidAmount < LiquidType.DRY.getMinimumDamageAmount()) {
					damage += (LiquidType.DRY.getMinimumDamageAmount() - liquidAmount) * LiquidType.DRY.getDamage();
				}
			}
			else {
				if(liquidAmount > liquidType.getMinimumDamageAmount()) {
					damage += liquidAmount * liquidType.getDamage();
				}
			}
		}
		if(checkTerrain(Terrain.SNOW)) {
			if(getHeight() > World.SNOW_LEVEL) {
				damage += 0.1 *(getHeight() - World.SNOW_LEVEL) / (1 - World.SNOW_LEVEL);
			}
			else {
				damage += 0.01;
			}
		}
		int roundedDamage = (int) (damage);
		return roundedDamage;
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
		if (height > 1) {
			height = 1;
		}
	}

	public double getHeight() {
		return height;
	}

	public void setNeighbors(List<Tile> tiles) {
		neighborTiles.clear();
		for (Tile t : tiles) {
			neighborTiles.add(t);
		}
	}

	public List<Tile> getNeighbors() {
		return neighborTiles;
	}
}
