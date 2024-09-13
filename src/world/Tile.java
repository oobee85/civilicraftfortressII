package world;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import game.*;
import game.components.*;
import utils.*;
import world.air.Air;
import world.liquid.*;

public class Tile implements Externalizable {

	private TileLoc location;
	private float height;
	private double energy;
	private double temperature;
	
	public volatile float liquidAmount;
	public volatile LiquidType liquidType;

	private volatile Faction faction;

	private Resource resource;
	private Terrain terr;
	private GroundModifier modifier;

	private Plant plant;
	private Building building;
	private Building road;
	private WeatherEvent weather;
	private Air air;
	private Air air2;
	
	private int tickLastTerrainChange;
	
	private double precomputedBrightness;
	

	private ConcurrentLinkedDeque<Unit> units;
	private ConcurrentLinkedQueue<Projectile> projectiles;
	private Inventory inventory;

	private List<Tile> neighborTiles = new LinkedList<Tile>();

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		height = in.readFloat();
		air.setHumidity(in.readDouble());
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
		out.writeDouble(air.getHumidity());
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
		units = new ConcurrentLinkedDeque<Unit>();
		projectiles = new ConcurrentLinkedQueue<Projectile>();
		inventory = new Inventory();
		this.energy = 100000;
		air = new Air(this.height);
		this.tickLastTerrainChange = -Constants.MIN_TIME_TO_SWITCH_TERRAIN;
	}

	public void setTemperature(double temp) {
		this.temperature = temp;
	}
	public double getTemperature() {
		return this.temperature;
//		double Kgair = 10 * 10 * 0.721;
////		return Kgair;
//		double asdf = Kgair*Math.abs(World.MINTEMP);
//		double asd = energy - asdf;
//		double asdfg = asd / Kgair;
//		return asdfg;

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
	
	public Air getAir() {
		return air;
	}
	
	public void updateAir() {
//		air.setTemperature(this.getTemperature());
//		air.setEnergy(this.getEnergy());
		air.updateHeight(this.height);
		air.updateMaxVolume();
		air.updateHumidity();
		air.updatePressure();
	}
	
	public void updateEnergyToTemperature() {
		double tileEnergy = getEnergy();
		double airEnergy = air.getEnergy();
		
		// Q=mcAT
		// T= Q/mc -273
		double tileTemp = tileEnergy / (Constants.MASSGROUND);
		setTemperature(tileTemp);
		
		// T = Q/moles -273
		double airTemp = airEnergy / (35);
		air.setTemperature(airTemp);
	}
	
	public double getEvaporation() {
		double evaporation = 0.0;
		if(this.liquidType == LiquidType.WATER && this.getTemperature() > Constants.FREEZETEMP && this.getAir().canRain() == false) {
			
//			evaporation = (Math.exp(this.getTemperature()/50) /(2*this.liquidAmount));
			evaporation = ( 0.01*(this.getTemperature() - Constants.KELVINOFFSET)*this.liquidAmount );
//			liquidAmount -= evaporation;
		}
		
//		System.out.println("Evaporation: "+ evaporation);
		return evaporation;
		
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

	public ConcurrentLinkedDeque<Unit> getUnits() {
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
	
	public boolean canGrow() {
		if (isCold() || isHot()) {
			return false;
		}
		if (canPlant() == false) {
			return false;
		}
//		if (liquidType == LiquidType.WATER && liquidAmount > liquidType.getMinimumDamageAmount()) {
//			return false;
//		}
		return true;
	}
	
	
	public boolean isCold() {
		if (liquidType == LiquidType.ICE || liquidType == LiquidType.SNOW) {
			return true;
		}
		
		if (this.air.getTemperature() < Constants.LETHALCOLDTEMP) { // -10 c
			return true;
		}
		return false;
	}
	
	public boolean isHot() {
		if (liquidType == LiquidType.LAVA) {
			return true;
		}
		if (this.air.getTemperature() > Constants.LETHALHOTTEMP) { // 37.8 c, 100f
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
		if (bt.blocksMovement() && building.isBuilt()) {
			if (bt.isGate() && u.getFaction() == building.getFaction()) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean hasBridgeOrPort() {
		return hasBuilding() && getBuilding().getType() == Game.buildingTypeMap.get("PORT");
	}

	public double[] computeTileDanger() {
		double[] damage = new double[DamageType.values().length];
		if (liquidAmount > liquidType.getMinimumDamageAmount()) {
			// Don't take water damage if there is a port or bridge
			if(!(liquidType.isWater() && hasBridgeOrPort())) {
				damage[liquidType.getDamageType().ordinal()] += liquidAmount * liquidType.getDamage();
			}
		}
		else {
			damage[DamageType.DRY.ordinal()] += 1;
		}
		if (modifier != null) {
			damage[DamageType.HEAT.ordinal()] += modifier.getType().getDamage() + modifier.timeLeft() * 0.0001;
		}
//		if ((this.getTemperature()-Constants.KELVINOFFSET >= 50) || (this.getAir().getTemperature()-Constants.KELVINOFFSET) >= 50) {
//			damage[DamageType.HEAT.ordinal()] += 1;
//		}
		return damage;
	}
	
	public int[] computeTileDamage() {
		double[] doubleDamage = computeTileDanger();
		int[] intDamage = new int[doubleDamage.length];
		for(int i = 0; i < intDamage.length; i++) {
			intDamage[i] = (int) doubleDamage[i];
		}
		return intDamage;
//		boolean flying = false;
//		boolean aquatic = false;
//		boolean fireResistant = false;
//		boolean coldResistant = false;
//
//		if (thing instanceof Unit) {
//			Unit unit = (Unit) thing;
//			flying = unit.getType().isFlying();
//			aquatic = unit.getType().isAquatic();
//			fireResistant = unit.getType().isFireResist();
//			coldResistant = unit.getType().isColdResist();
//		}
//		if (thing instanceof Building) {
//			coldResistant = true;
//		}
		
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
	
	public int distanceTo(Tile other) {
		if(other == null) {
			return Integer.MAX_VALUE;
		}
		return this.getLocation().distanceTo(other.getLocation());
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
	
	public List<String> getDebugStrings() {
		int NUM_DEBUG_DIGITS = 3;
		String fvalue = "%." + NUM_DEBUG_DIGITS + "f";
		List<String> strings = new LinkedList<String>(Arrays.asList(
				String.format("H=" + fvalue, getHeight()),
				String.format("PRES=" + fvalue, getAir().getPressure()),
				
//				String.format("HUM" + "=%." + NUM_DEBUG_DIGITS + "f", getAir().getHumidity()),
				String.format("TTEM=" + fvalue, (getTemperature() - Constants.FREEZETEMP)),
				String.format("ATEM=" + fvalue, (getAir().getTemperature() - Constants.FREEZETEMP)),
				String.format("TENE=" + fvalue, getEnergy()),
				String.format("AENE=" + fvalue, getAir().getEnergy()),
				
//				String.format("EVAP=" + fvalue, getEvaporation()),
//				String.format("dVOL=" + fvalue, getAir().getVolumeChange()),
				String.format("VOL=" + fvalue, getAir().getVolumeLiquid()),
				String.format("MVOL=" + fvalue, getAir().getMaxVolumeLiquid())
				
//				String.format("RH=" + fvalue, getAir().getRelativeHumidity()),
//				String.format("DEW=" + fvalue, getAir().getDewPoint()),
//				String.format("MASS=" + fvalue, getAir().getMass())
		));
		
		if (getResource() != null) {
			strings.add(String.format("ORE=%d", getResource().getYield()));
		}

		if (liquidType != LiquidType.DRY) {
			strings.add(String.format(liquidType.name().charAt(0) + "=" + fvalue,
					liquidAmount));
		}

		if (getModifier() != null) {
			strings.add(String.format("GM=%d", getModifier().timeLeft()));
		}
		return strings;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			return getLocation().equals(((Tile) obj).getLocation());
		}
		return false;
	}
	
	static int[] distro = new int[100];
	public int getHash(int salt) {
		return String.format("%d,%d,%d", getLocation().x(), getLocation().y(), salt).hashCode();
		
//		final int prime = 197938439;
//	    int result = 1;
//	    result = result + prime * salt;
//	    result = ((result << 7) & 0xFFFFFF80) | ((result >>> 25) & 0x0000007F);
//	    result = result + prime * (getLocation().x() + salt);
//	    result = ((result << 25) & 0xFE000000) | ((result >>> 7) & 0x01FFFFFF);
//	    result = result + prime * (getLocation().y() + salt);
//	    result = ((result << 5) & 0xFFFFFFE0) | ((result >>> 27) & 0x0000001F);
//	    
//	    distro[(int)(   (((double)result) / Integer.MAX_VALUE / 2 + 0.5)*distro.length    )]++;
//	    return result;
	}
}
