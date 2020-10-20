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
	private Faction isTerritory = World.NEUTRAL_FACTION;
	private boolean isSelected = false;
	private boolean inVisionRange = false;

	private TileLoc location;
	private double height;
	private double humidity;

	private Resource resource;
	private Plant plant;
	private Terrain terr;
	private Building building;
	private Building road;
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
	public double getTempurature() {
		double season = Season.getSeason2();
		double seasonTemp = 1 - ((1 - season) * Season.winter[getLocation().y] + season*Season.summer[getLocation().y]);
		double heightTemp = 1 - height;
		heightTemp = heightTemp*heightTemp;
		double nightMultiplier = World.isNightTime() ? 0.9 : 1;
		return (seasonTemp + heightTemp)*nightMultiplier/2;
	}
	public double getHumidity() {
		return humidity;
	}
	public void updateHumidity(int currentTick) {
		if(liquidType == LiquidType.WATER || liquidType == LiquidType.ICE ||  liquidType == LiquidType.SNOW) {
			humidity += 2/(humidity + liquidAmount);
			
		}
		if(liquidType == LiquidType.LAVA) {
			humidity = 0;
		}
		
		humidity -= 0.01;
		if(humidity < 0) {
			humidity = 0;
		}
		if(humidity > 20) {
			humidity = 20;
		}
			
//		}
		
	}

	public void setRoad(Building road) {
		this.road = road;
		turnRoad();
		for(Tile neighbor : getNeighbors()) {
			neighbor.turnRoad();
		}
	}
	private void turnRoad() {
		if(getRoad() == null) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		TileLoc loc = getLocation();
		for(Tile t : getNeighbors()) {
			if(t.getRoad() == null)
				continue;
			Direction d = Direction.getDirection(loc, t.getLocation());
			if(d != null)
				directions.add(d);
		}
		String s = "";
		for(Direction d : Direction.values()) {
			if(directions.contains(d)) {
				s += d;
			}
		}
		if(s.equals("")) {
			for(Direction d : Direction.values()) {
				s += d;
			}
		}
		getRoad().setRoadCorner(s);
	}

	public void setTerritory(Faction faction) {
		this.isTerritory = faction;
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

	public Unit getUnitOfFaction(Faction faction) {
		for (Unit u : units) {
			if (u.getFaction() == faction) {
				return u;
			}
		}
		return null;
	}
	public int countUnitsOfFaction(Faction faction) {
		int x = 0;
		for(Unit unit : units) {
			if(unit.getFaction() == faction) {
				x++;
			}
		}
		return x;
	}

	public Thing getThingOfFaction(Faction faction) {
		for (Unit u : units) {
			if (u.getFaction() == faction) {
				return u;
			}
		}
		if (building != null && building.getFaction() == faction) {
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

	private double getBrightnessNonRecursive(Faction faction) {
		double brightness = 0;
		if (this.getThingOfFaction(faction) != null) {
			brightness += 1;
		}
		if (this.isTerritory == faction) {
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

	
	public double getBrightness(Faction faction) {
		double brightness = this.getBrightnessNonRecursive(faction);
		for (Tile tile : getNeighbors()) {
			brightness += tile.getBrightnessNonRecursive(faction);
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


	public Building getRoad() {
		return road;
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

	public Faction getIsTerritory() {
		return isTerritory;
	}

	public boolean canBuild() {
		return terr.isBuildable(terr) && liquidAmount < liquidType.getMinimumDamageAmount();
	}

	public boolean canPlant() {
		return terr.isPlantable(terr);
	}
	public boolean isCold() {
		if(liquidType == LiquidType.ICE || liquidType == LiquidType.SNOW) {
			return true;
		}
		if(this.getTempurature() < Season.FREEZING_TEMPURATURE * 1.1) {
			return true;
		}
		return false;
	}
	public boolean canOre() {
		return terr.isOreable(terr);
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
		if(this.countUnitsOfFaction(u.getFaction()) != this.getUnits().size()) {
			return true;
		}
		BuildingType bt = building.getType();
		if(bt.blocksMovement()) {
			if(bt.isGate() && u.getFaction() == building.getFaction()) {
				return false;
			}
			return true;
		}
		return false;
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
		if(buildingType.blocksMovement()) {
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
