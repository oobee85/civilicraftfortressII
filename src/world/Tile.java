package world;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;

public class Tile {
	private boolean isTerritory = false;
	private boolean isSelected = false;
	private boolean inVisionRange = false;

	private TileLoc location;
	private double height;
	private double humidity;
	private int tickLastTouchedWater = 0;
	int minEntitySize = 20;

	private String roadCorner;

	private Resource resource;
	private Plant plant;
	private Terrain terr;
	private Building building;
	private Road road;
	private GroundModifier modifier;
	
	private ConcurrentLinkedQueue<Unit> units;
	private ConcurrentLinkedQueue<Projectile> projectiles;
	private ConcurrentLinkedQueue<Item> items;
	
	public double liquidAmount;
	public LiquidType liquidType;
	
	
	private List<Tile> neighborTiles = new LinkedList<Tile>();

	private Tile(TileLoc location, Terrain t) {
		this.location = location;
		terr = t;
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
		units = new ConcurrentLinkedQueue<Unit>();
		projectiles = new ConcurrentLinkedQueue<Projectile>();
		items = new ConcurrentLinkedQueue<Item>();
		this.humidity = 1;
	}

	public static Tile makeTile(TileLoc location, Terrain t) {
		return new Tile(location, t);
	}
	public double getHumidity() {
		return humidity;
	}
	public void setHumidity(double set) {
		humidity = set;
	}
	public void setWaterTouched(int tick) {
		tickLastTouchedWater = tick;
	}
	public void updateHumidity(int currentTick) {
		if(liquidType == LiquidType.WATER || liquidType == LiquidType.ICE ||  liquidType == LiquidType.SNOW) {
			humidity += 1/(humidity + 1);
			
		}
//			else if(liquidType == LiquidType.LAVA){
//			humidity = 0;
//		} else {
//			int difference = currentTick - tickLastTouchedWater;
//			if (difference != 0) {
//				humidity -= (1 / difference / 100);
//			}
		
		humidity -= 0.02;
		if(humidity < 0) {
			humidity = 0;
		}
			
//		}
		
	}

	public void setRoad(Road r, String s) {
		this.road = r;
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
	public int getNumPlayerControlledUnits() {
		int x = 0;
		for(Unit unit : units) {
			if(unit.isPlayerControlled()) {
				x++;
			}
		}
		return x;
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
	public ConcurrentLinkedQueue<Projectile> getProjectiles() {
		return projectiles;
	}
	public ConcurrentLinkedQueue<Item> getItems() {
		return items;
	}

	public boolean hasUnit(UnitType unit) {
		for (Unit u : units) {
			if (u.getUnitType() == unit) {
				return true;
			}
		}
		return false;
	}
	public void setInVisionRange(boolean inRange) {
		this.inVisionRange = inRange;
	}

	private double getBrightnessNonRecursive() {
		double brightness = 0;
		if (this.getHasBuilding() || this.getPlayerControlledThing() != null) {
			brightness += 1;
		}
		if (this.isTerritory) {
			brightness += 0.4;
		}
		if(inVisionRange == true) {
			brightness += 1;
		}
		brightness += getTerrain().getBrightness();
		brightness += liquidAmount * liquidType.getBrightness();
		if(modifier != null) {
			brightness += getModifier().getType().getBrightness();
		}
		return brightness;
	}

	
	public double getBrightness() {
		double brightness = this.getBrightnessNonRecursive();
		for (Tile tile : getNeighbors()) {
			brightness += tile.getBrightnessNonRecursive();
		}
		if(inVisionRange == true) {
			brightness += 1;
		}
		return brightness;
	}

	public void setBuilding(Building b) {
		building = b;
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
	
	public void addProjectile(Projectile p) {
		projectiles.add(p);
	}
	public void removeProjectile(Projectile p) {
		projectiles.remove(p);
	}
	public void addItem(Item i) {
		items.add(i);
	}
	public void clearItems() {
		items.clear();
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
		if(u.getType().isFlying()) {
			return false;
		}
		if(getHasBuilding() == false) {
			return false;
		}
		if(building.isPlanned() == true) {
			return false;
		}
		BuildingType bt = building.getType();
		if(bt == BuildingType.WALL_WOOD || bt == BuildingType.WALL_STONE || bt == BuildingType.WALL_BRICK) {
			return true;
		}

		if((building.getType() == BuildingType.GATE_WOOD 
				|| building.getType() == BuildingType.GATE_STONE 
				|| building.getType() == BuildingType.GATE_BRICK) && u.isPlayerControlled() == building.getIsPlayerControlled()) {
			return false;
		}
		return false;
	}

	public Road getRoad() {
		return road;
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
		if(building.isPlanned() == true) {
			return true;
		}
		BuildingType bt = building.getType();
		if(bt == BuildingType.WALL_WOOD || bt == BuildingType.WALL_STONE || bt == BuildingType.WALL_BRICK) {
			return false;
		}

		if ((building.getType() == BuildingType.GATE_WOOD
				|| building.getType() == BuildingType.GATE_STONE
				|| building.getType() == BuildingType.GATE_BRICK) && u.isPlayerControlled() == false) {
			return false;
		}
		return true;
	}
	
	public double computeTileDamage(Thing thing) {
		boolean flying = false;
		boolean aquatic = false;
		boolean fireResistant = false;
		boolean coldResistant = false;
		
		if(thing instanceof Unit) {
			Unit unit = (Unit)thing;
			flying = unit.getType().isFlying();
			aquatic = unit.getType().isAquatic();
			fireResistant = unit.getType().isFireResist();
			coldResistant = unit.getType().isColdResist();
		}
		if(thing instanceof Building) {
			coldResistant = true;
		}
		
		double damage = 0;
		if(flying) {
			
		}
		else {
			if(aquatic) {
				if(liquidAmount < LiquidType.WATER.getMinimumDamageAmount()) {
					
//					damage += (LiquidType.WATER.getMinimumDamageAmount() - liquidAmount) * LiquidType.WATER.getDamage();
					damage += 1;
				}else if(liquidType == LiquidType.LAVA && !fireResistant){
					damage += liquidAmount * liquidType.getDamage();
				}
			}
			else {
				if(liquidAmount > liquidType.getMinimumDamageAmount()) {
					if(liquidType != LiquidType.LAVA || !fireResistant) {
						damage += liquidAmount * liquidType.getDamage();
					}
					
				}
			}
			if(modifier != null) {
				if(modifier.getType() == GroundModifierType.FIRE && fireResistant) {
					// resisted environment damage
				} 
				else {
					damage += modifier.getType().getDamage() + modifier.timeLeft()*0.0001;
				}
				
			}
		}
		return damage;
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
	public boolean hasWall() {
		if(building == null) {
			return false;
		}
		if(building.isPlanned() == true) {
			return false;
		}
		BuildingType buildingType = building.getType();
		if(buildingType == BuildingType.WALL_WOOD || buildingType == BuildingType.WALL_STONE || buildingType == BuildingType.WALL_BRICK) {
			return true;
		}
		return false;
	}
	public void setNeighbors(List<Tile> tiles) {
		neighborTiles = tiles;
	}

	public List<Tile> getNeighbors() {
		return neighborTiles;
	}
	@Override
	public String toString() {
		return location.toString();
	}
}
