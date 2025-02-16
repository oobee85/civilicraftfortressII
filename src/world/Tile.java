package world;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import game.*;
import utils.*;
import world.air.Air;
import world.liquid.*;

public class Tile implements Externalizable {
	
	private static final boolean AIR_NETWORKING = false;
	private static final int NO_RESOURCE = -1;

	private TileLoc location;
	private float height;
	private double energy;
	private double temperature;
	public volatile float liquidAmount;
	public volatile float runningAverageLiquidAmount;
	public volatile LiquidType liquidType;
	private volatile Faction faction;
	private ResourceType resource;
	private Terrain terrain;
	private GroundModifier modifier;
	private Plant plant;
	private Building building;
	private Building road;
	private Air air;
	private int tickLastTerrainChange;
	private double precomputedBrightness;
	private ConcurrentLinkedDeque<Unit> units;
	private ConcurrentLinkedQueue<Projectile> projectiles;
	private Inventory inventory;

	private List<Tile> neighborTiles = new LinkedList<Tile>();
	
	private Tile latestSentInfo;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		location = (TileLoc)in.readObject();
		height = in.readFloat();
		inventory = (Inventory)in.readObject();
		liquidAmount = in.readFloat();
		liquidType = LiquidType.values()[in.readByte()];
		energy = in.readDouble();
		terrain = Terrain.values()[in.readByte()];
		faction = new Faction();
		faction.setID(in.readByte());
		int resourceOrdinal = in.readInt();
		if (resourceOrdinal == NO_RESOURCE) {
			resource = null;
		}
		else {
			resource = ResourceType.values()[resourceOrdinal];
		}
		modifier = (GroundModifier) in.readObject();
		if (AIR_NETWORKING) {
			air = (Air)in.readObject();
		}
		else {
			air = new Air(height);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if (latestSentInfo == null) {
			latestSentInfo = new Tile();
			latestSentInfo.faction = new Faction();
			latestSentInfo.inventory = new Inventory();
			latestSentInfo.modifier = null;
		}
		out.writeObject(location);
		latestSentInfo.location = location;
		out.writeFloat(height);
		latestSentInfo.height = height;
		out.writeObject(inventory);
		latestSentInfo.inventory.copyFrom(inventory);
		out.writeFloat(liquidAmount);
		latestSentInfo.liquidAmount = liquidAmount;
		out.writeByte(liquidType.ordinal());
		latestSentInfo.liquidType = liquidType;
		out.writeDouble(energy);
		latestSentInfo.energy = energy;
		out.writeByte(terrain.ordinal());
		latestSentInfo.terrain = terrain;
		out.writeByte(faction.id());
		latestSentInfo.faction.setID(faction.id());
		out.writeInt((resource == null) ? NO_RESOURCE : resource.ordinal());
		latestSentInfo.resource = resource;
		out.writeObject(modifier);
		latestSentInfo.modifier = modifier;
		if (AIR_NETWORKING) {
			out.writeObject(air);
//			latestSentInfo.air = air; TODO
		}
	}
	
	public boolean isChangedFromLatestSent() {
		if (latestSentInfo == null) { 
			return true;
		}
		if (height != latestSentInfo.height ||
				terrain != latestSentInfo.terrain ||
				faction.id() != latestSentInfo.faction.id() ||
				resource != latestSentInfo.resource) {
			return true;
		}

		if (Math.abs(liquidAmount - latestSentInfo.liquidAmount) > Settings.LIQUID_CHANGE_THRESHOLD
				|| liquidType != latestSentInfo.liquidType) {
			return true;
		}
		
		GroundModifierType type = (modifier == null) ? null : modifier.getType();
		GroundModifierType otherType = (latestSentInfo.modifier == null) ? null : latestSentInfo.modifier.getType();
		if (type != otherType) {
			return true;
		}
		
		
		if (inventory.isDifferent(latestSentInfo.inventory)) {
			return true;
		}
		// TODO air
		
		// dont care if energy changes as it doesnt impact client rendering.
		return false;
	}
	
	public Tile() {
		
	}

	public Tile(TileLoc location, Terrain t) {
		this();
		this.location = location;
		terrain = t;
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
		units = new ConcurrentLinkedDeque<Unit>();
		projectiles = new ConcurrentLinkedQueue<Projectile>();
		inventory = new Inventory();
		this.energy = 30000;
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
			evaporation = ( 0.01*(this.getTemperature() - Constants.KELVINOFFSET)*Math.min(2, this.liquidAmount) );
			
//			liquidAmount -= evaporation;
		}
		
//		System.out.println("Evaporation: "+ evaporation);
		return evaporation;
		
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
		if (!this.getRoad().getType().isTiledImage()) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		directions.add(Direction.NONE);
		TileLoc loc = getLocation();
		for (Tile t : getNeighbors()) {
			if (t.getRoad() == null) {
				continue;
			}
			Direction d = Direction.getDirection(loc, t.getLocation());
			if (d != null)
				directions.add(d);
		}
		int tileBitmap = 0;
		int bit = 1;
		for (Direction d : Direction.TILING_DIRECTIONS) {
			if (directions.contains(d)) {
				tileBitmap += bit;
			}
			bit *= 2;
		}
		getRoad().setTiledImage(tileBitmap);
	}
	private void turnBuilding() {
		if (getBuilding() == null) {
			return;
		}
		if (!this.getBuilding().getType().isTiledImage()) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		directions.add(Direction.NONE);
		TileLoc loc = getLocation();
		for (Tile t : getNeighbors()) {
			if (t.getBuilding() == null) {
				continue;
			}
			Direction d = Direction.getDirection(loc, t.getLocation());
			if (d != null)
				directions.add(d);
		}
		int tileBitmap = 0;
		int bit = 1;
		for (Direction d : Direction.TILING_DIRECTIONS) {
			if (directions.contains(d)) {
				tileBitmap += bit;
			}
			bit *= 2;
		}
		getBuilding().setTiledImage(tileBitmap);
	}
	private void turnTree() {
		if (getPlant() == null) {
			return;
		}
		if (!this.getPlant().getType().isTiledImage()) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		directions.add(Direction.NONE);
		TileLoc loc = getLocation();
		for (Tile t : getNeighbors()) {
			if (t.getPlant() == null) {
				continue;
			}
			if (t.getPlant().getType() != this.getPlant().getType()) {
				continue;
			}
			Direction d = Direction.getDirection(loc, t.getLocation());
			if (d != null)
				directions.add(d);
		}
		int tileBitmap = 0;
		int bit = 1;
		for (Direction d : Direction.TILING_DIRECTIONS) {
			if (directions.contains(d)) {
				tileBitmap += bit;
			}
			bit *= 2;
		}
		getPlant().setTiledImage(tileBitmap);
	}

	public void setFaction(Faction faction) {
		this.faction = faction;
	}

	public void setResource(ResourceType resource) {
		this.resource = resource;
	}

	public ResourceType getResource() {
		return resource;
	}

	public GroundModifier getModifier() {
		return modifier;
	}

	public void setHasPlant(Plant p) {
		plant = p;

		this.turnTree();
		for (Tile neighbor : getNeighbors()) {
			neighbor.turnTree();
		}
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
		turnBuilding();
		for (Tile neighbor : getNeighbors()) {
			neighbor.turnBuilding();
		}
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
		return terrain;
	}

	public Faction getFaction() {
		return faction;
	}

	public boolean canBuild() {
		return terrain.isBuildable(terrain) && liquidAmount < liquidType.getMinimumDamageAmount();
	}

	public boolean canPlant() {
		return terrain.isPlantable(terrain);
	}
	
	// returns true if the terrain can support plants, regardless of weather
	public boolean isPlantable() {
		if(this.terrain == Terrain.DIRT || this.terrain == Terrain.GRASS) {
			return true;
		}else {
			return false;
		}
	}
	// returns true of terrain is kind of rocky
	public boolean isRocky() {
		if(this.terrain == Terrain.ROCK || this.terrain == Terrain.VOLCANO) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isRoughTerrain() {
		if(this.terrain == Terrain.ROCK || this.terrain == Terrain.VOLCANO || this.terrain == Terrain.SAND) {
			return true;
		}else {
			return false;
		}
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
		
//		if (this.air.getTemperature() < Constants.LETHALCOLDTEMP) { // -10 c
//			return true;
//		}
		return false;
	}
	
	public boolean isHot() {
		if (liquidType == LiquidType.LAVA) {
			return true;
		}
//		if (this.air.getTemperature() > Constants.LETHALHOTTEMP) { // 37.8 c, 100f
//			return true;
//		}
		return false;
	}

	public boolean canOre() {
		return terrain.isOreable(terrain);
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
//		if (this.countUnitsOfFaction(u.getFaction()) != this.getUnits().size()) {
//			return true;
//		}
//		if(this.getBuilding().getFaction() != u.getFaction()) {
//			return true;
//		}
		BuildingType bt = building.getType();
		if (bt.blocksMovement() && building.isBuilt()) { // if building is a wall and is finished building
			
			// check if neighboring tiles have siege tower
			for(Tile t: this.getNeighbors()) { // iterate through tiles neighbors
				for(Unit un: t.units) { // iterate through units on the neighbors
					if(u.getFaction() == un.getFaction()) { // check if unit on neighbor is current units faction
						if(u.getTile() == un.getTile()) { // check if both units are on the same tile
							if(un.getType().isSiegeTower()) { // check if unit on neighbor is siege tower
								return false;
							}
						}
						
					}
					
				}
			}
			
			// if its a gate
			if (bt.isGate() && u.getFaction() == building.getFaction()) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean hasBridgeOrPort() {
		if(hasBuilding() && getBuilding().getType() == Game.buildingTypeMap.get("PORT")) {
			return true;
		}
		if(this.getRoad() != null && this.getRoad().getType().isBridge()) {
			return true;
		}
		return false;
	}
	private boolean hasDrain() {
		return (hasBuilding() && getBuilding().getType().isDrain());
	}

	public double[] computeTileDanger() {
		double[] damage = new double[DamageType.values().length];
		if (liquidAmount > liquidType.getMinimumDamageAmount()) {
			// Don't take water damage if there is a port or bridge or drain
			if(!(liquidType == LiquidType.WATER && (hasBridgeOrPort() || hasDrain()))) {
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
		terrain = t;
	}

	public boolean canSupportRareOre() {
		return terrain.canSupportRare(terrain);
	}
	
	public boolean checkTerrain(Terrain t) {
		return terrain == t;
	}

	public TileLoc getLocation() {
		return location;
	}

	public void setHeight(float newheight) {
		height = newheight;
		if (height > Constants.MAXHEIGHT) {
			height = Constants.MAXHEIGHT;
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
			strings.add(getResource().name());
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