package world;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import game.*;
import game.liquid.*;
import utils.*;

public class Tile implements Externalizable {

	private TileLoc location;
	private float height;
	private float humidity;
	private double energy;

	public volatile float liquidAmount;
	public volatile LiquidType liquidType;

	private volatile Faction faction;
	private boolean inVisionRange = false;

	private Resource resource;
	private Terrain terr;
	private GroundModifier modifier;

	private Plant plant;
	private Building building;
	private Building road;
	private WeatherEvent weather;
	private Air air;
	private int tickLastTerrainChange;
	
	private double precomputedBrightness;
	

	private ConcurrentLinkedQueue<Unit> units;
	private ConcurrentLinkedQueue<Projectile> projectiles;
	private Inventory inventory;

	private List<Tile> neighborTiles = new LinkedList<Tile>();

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		height = in.readFloat();
		humidity = in.readFloat();
//		air.setHumidity(humidity);
//		air.setTemperature(this.getTemperature());
		liquidAmount = in.readFloat();

		location = TileLoc.readFromExternal(in);

		liquidType = LiquidType.values()[in.readByte()];
		terr = Terrain.values()[in.readByte()];
		faction = new Faction();
		faction.setID(in.readByte());

		resource = (Resource) in.readObject();
		modifier = (GroundModifier) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(height);
		out.writeFloat(humidity);
		out.writeFloat(liquidAmount);

		location.writeExternal(out);

		out.writeByte(liquidType.ordinal());
		out.writeByte(terr.ordinal());
		out.writeByte(faction.id());

		out.writeObject(resource);
		out.writeObject(modifier);
	}

	public Tile() {

	}

	public Tile(TileLoc location, Terrain t) {
		this.location = location;
		terr = t;
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
		units = new ConcurrentLinkedQueue<Unit>();
		projectiles = new ConcurrentLinkedQueue<Projectile>();
		inventory = new Inventory();
		this.humidity = 1;
		this.energy = 20000;
		air = new Air(this.height, 0);
		this.tickLastTerrainChange = -World.MIN_TIME_TO_SWITCH_TERRAIN;
	}

	
	public double getTemperature() {
		double Kgair = World.MMAIR * 10 * 0.721;
		
		double asdf = Kgair*Math.abs(World.MINTEMP);
		double asd = energy - asdf;
		double asdfg = asd / Kgair;
		return asdfg;
//		float season = Season.getSeason2();
//		float night = Season.getNightEnergy();
//		float seasonTemp = Season.winter[getLocation().y()] + season * Season.summer[getLocation().y()];
		
		
//		float seasonTemp = 1 - ((1 - season) * Season.winter[getLocation().y()] + season*Season.summer[getLocation().y()]);
//		float heightTemp = 1 - height;
//		heightTemp = heightTemp*heightTemp;
//		float nightMultiplier = World.isNightTime() ? 0.9f : 1f;
//		return (seasonTemp + heightTemp)*nightMultiplier/2;
//		return season+night;
	}
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	public double getEnergy() {
		return energy;
	}
	public int getTickLastTerrainChange() {
		return tickLastTerrainChange;
	}
	public void setTickLastTerrainChange(int tick) {
		tickLastTerrainChange = tick;
	}
	public void addEnergy(double added) {
		this.energy += added;
	}
	
	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
//		this.humidity = humidity;
	}
	
	public Air getAir() {
		return air;
	}
	
	
	public void updateAir() {
		air.setTemperature(this.getTemperature());
		air.updateMaxVolume();
		air.updateHumidity();
		air.updateHeight(this.height);
		air.updatePressure();
	}
	
	public double getEvaporation() {
		double evaporation = 0.0;
		if(this.liquidType == LiquidType.WATER && this.getTemperature() > 0 && this.getAir().canRain() == false) {
			evaporation = ( 0.01*this.getTemperature()*this.liquidAmount ) /10;
//			liquidAmount -= evaporation;
		}
		
//		System.out.println("Evaporation: "+ evaporation);
		return evaporation;
		
	}
	
	public void updateHumidity(int currentTick) {

		if (liquidType == LiquidType.WATER || liquidType == LiquidType.ICE || liquidType == LiquidType.SNOW) {
			if (liquidAmount > 0) {
//				humidity += Math.sqrt(Math.sqrt(liquidAmount)); // sqrt(0.01) -> 0.1
			}
		}
		if (modifier != null && modifier.isHot()) {
//			humidity -= 1;
		}
		if (liquidType == LiquidType.LAVA) {
//			humidity -= liquidAmount * 10;
		}

//		humidity *= 0.995;
		if (terr == Terrain.GRASS) {
//			humidity *= 1.004;
		}
		if (humidity < 0) {
//			humidity = 0;
		}
		if (humidity > 20) {
//			humidity = 20;
		}
	}

	public void setWeather(WeatherEvent weatherEvent) {
		weather = weatherEvent;
	}

	public void removeWeather() {
		weather = null;
	}

	public WeatherEvent getWeather() {
		return weather;
	}

	public boolean hasWeather() {
		return weather != null;
	}

	public void setRoad(Building road) {
		this.road = road;
		turnRoad();
		for (Tile neighbor : getNeighbors()) {
			neighbor.turnRoad();
		}
	}

	private void turnRoad() {
		if (getRoad() == null) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		TileLoc loc = getLocation();
		for (Tile t : getNeighbors()) {
			if (t.getRoad() == null)
				continue;
			Direction d = Direction.getDirection(loc, t.getLocation());
			if (d != null)
				directions.add(d);
		}
		String s = "";
		for (Direction d : Direction.values()) {
			if (directions.contains(d)) {
				s += d;
			}
		}
		if (s.equals("")) {
			for (Direction d : Direction.values()) {
				s += d;
			}
		}
		getRoad().setRoadCorner(s);
	}

	public void setFaction(Faction faction) {
		this.faction = faction;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
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
		for (Unit unit : units) {
			if (unit.getFaction() == faction) {
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

	public Inventory getInventory() {
		return inventory;
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
	public boolean isInVision() {
		return inVisionRange;
	}

	public double getBrightness(Faction faction) {
		return precomputedBrightness;
	}
	
	public void setBrightness(double brightness) {
		precomputedBrightness = brightness;
	}

	public void setBuilding(Building b) {
		building = b;
	}

	public void setModifier(GroundModifier gm) {
		modifier = gm;
	}

	public void replaceOrAddDurationModifier(GroundModifierType type, int duration, WorldData worldData) {
		if (getModifier() != null) {
			if (getModifier().getType() == type) {
				if (getModifier().timeLeft() < duration) {
					this.getModifier().setDuration(duration);
				}
				int toadd = duration - getModifier().timeLeft();
				toadd = toadd < 0 ? 0 : toadd;
				return;
			}
		}
		GroundModifier gm = new GroundModifier(type, this, duration);
		this.setModifier(gm);
		worldData.addGroundModifier(gm);
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

//	public void drawHeightMap(Graphics g, double height, int tileSize) {
//		int r = Math.max(Math.min((int) (255 * height), 255), 0);
//		g.setColor(new Color(r, 0, 255 - r));
//		g.fillRect(location.x() * tileSize, location.y() * tileSize, tileSize, tileSize);
//	}

	public int drawDebugStrings(Graphics g, List<String> strings, int row, int fontsize, int tileSize, Point drawAt) {
		int x = drawAt.x + 2;
		int y = drawAt.y + fontsize / 2;
		int maxWidth = 0;
		for (String s : strings) {
			int stringWidth = g.getFontMetrics().stringWidth(s) + 2;
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
	
	public boolean hasRoad() {
		return road != null;
	}
	public Building getRoad() {
		return road;
	}

	public boolean hasBuilding() {
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

	public Faction getFaction() {
		return faction;
	}

	public boolean canBuild() {
		return terr.isBuildable(terr) && liquidAmount < liquidType.getMinimumDamageAmount();
	}

	public boolean canPlant() {
		return terr.isPlantable(terr);
	}

	public boolean isCold() {
		if (liquidType == LiquidType.ICE || liquidType == LiquidType.SNOW) {
			return true;
		}
		return airTemperature();
	}

	public boolean canGrow() {
		if (isCold()) {
			return false;
		}
		if (liquidType == LiquidType.LAVA) {
			return false;
		}
		return true;
	}

	public boolean airTemperature() {
		double temp = this.air.getTemperature();
		if (temp < World.FREEZING_TEMPURATURE) {
			return true;
		}
		return false;
	}

	public boolean canOre() {
		return terr.isOreable(terr);
	}

	public boolean isBlocked(Unit u) {
		if (u.getType().isFlying()) {
			return false;
		}
		if (hasBuilding() == false) {
			return false;
		}
		if (building.isPlanned() == true) {
			return false;
		}
		if (this.countUnitsOfFaction(u.getFaction()) != this.getUnits().size()) {
			return true;
		}
//		if(this.getBuilding().getFaction() != u.getFaction()) {
//			return true;
//		}
		BuildingType bt = building.getType();
		if (bt.blocksMovement()) {
			if (bt.isGate() && u.getFaction() == building.getFaction()) {
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

		if (thing instanceof Unit) {
			Unit unit = (Unit) thing;
			flying = unit.getType().isFlying();
			aquatic = unit.getType().isAquatic();
			fireResistant = unit.getType().isFireResist();
			coldResistant = unit.getType().isColdResist();
		}
		if (thing instanceof Building) {
			coldResistant = true;
		}

		double damage = 0;
		if (flying) {

		} else {
			if (aquatic) {
				if (liquidAmount < LiquidType.WATER.getMinimumDamageAmount()) {

//					damage += (LiquidType.WATER.getMinimumDamageAmount() - liquidAmount) * LiquidType.WATER.getDamage();
					damage += 1;
				} else if (liquidType == LiquidType.LAVA && !fireResistant) {
					damage += liquidAmount * liquidType.getDamage();
				}
			} else {
				if (liquidAmount > liquidType.getMinimumDamageAmount()) {
					if (liquidType != LiquidType.LAVA || !fireResistant) {
						damage += liquidAmount * liquidType.getDamage();
					}

				}
			}
			if (modifier != null) {
				if (modifier.getType() == GroundModifierType.FIRE && fireResistant) {
					// resisted environment damage
				} else {
					damage += modifier.getType().getDamage() + modifier.timeLeft() * 0.0001;
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

	public void setHeight(float newheight) {
		height = newheight;
		if (height > 1000) {
			height = 1000;
		}
	}

	public float getHeight() {
		return height;
	}

	public boolean hasWall() {
		if (building == null) {
			return false;
		}
		if (building.isPlanned() == true) {
			return false;
		}
		BuildingType buildingType = building.getType();
		if (buildingType.blocksMovement()) {
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			return getLocation().equals(((Tile) obj).getLocation());
		}
		return false;
	}
}
