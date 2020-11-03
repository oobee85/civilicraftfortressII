package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import liquid.*;
import ui.Game;
import utils.*;
import world.*;

public class Building extends Thing implements Serializable {
	
	private BuildingType buildingType;
	private double remainingEffort;
	private transient LinkedList<Unit> producingUnitList = new LinkedList<Unit>();
	private double culture;
	public transient static double CULTURE_AREA_MULTIPLIER = 0.1;
	private transient Tile spawnLocation;
	private transient double timeToHarvest;
	private transient double baseTimeToHarvest = 20;
	private boolean isPlanned;
	
	public Building(BuildingType buildingType, Tile tile, Faction faction) {
		super(buildingType.getHealth(), buildingType, faction, tile);
		this.remainingEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
		this.spawnLocation = tile;
		this.timeToHarvest = baseTimeToHarvest;
		this.isPlanned = false;
		
		
	}
	public void setPlanned(boolean planned) {
		isPlanned = planned;
	}
	public boolean isPlanned() {
		return isPlanned;
	}
	public void tick(World world) {
		updateInProgressUnit();
		timeToHarvest --;
		
		if(World.ticks % World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			Tile tile = getTile();
			int tileDamage = (int)tile.computeTileDamage(this);
			if (tileDamage != 0) {
				this.takeDamage(tileDamage);
			}
		}

		if(!isBuilt()) {
			return;
		}
		
		// building builds units
		if(!getProducingUnit().isEmpty() && getProducingUnit().peek().isBuilt() == true) {
			Unit unit = getProducingUnit().remove();
			unit.queuePlannedAction(new PlannedAction(getSpawnLocation()));
			getTile().addUnit(unit);
			world.addUnit(unit);
		}
		
		if(!readyToHarvest() ) {
			return;
		}
		if(getType() == Game.buildingTypeMap.get("CASTLE")) {
			getFaction().spendResearch(20);
			resetTimeToHarvest();
		}
		if(getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
			getFaction().spendResearch(20);
			resetTimeToHarvest();
		}
		if(getType() == Game.buildingTypeMap.get("MINE")) {
			if(getTile().getResource() != null && getTile().getResource().getType().isOre() == true) {
				getFaction().addItem(getTile().getResource().getType().getItemType(), 1);
				getTile().getResource().harvest(1);
//				getTile().setHeight(getTile().getHeight() - 0.001);
				if(getTile().getResource().getYield() <= 0) {
					getTile().setResource(null);
				}
				resetTimeToHarvest();
			}
			if(getTile().getTerrain() == Terrain.ROCK && getTile().getResource() == null) {
				getFaction().addItem(ItemType.STONE, 1);
				resetTimeToHarvest();
			}
		}
		if(getType() == Game.buildingTypeMap.get("IRRIGATION") && getTile().canPlant() == true) {
			int extraFood = 0;
			//irrigation produces extra food when placed on water
			if(getTile().liquidType == LiquidType.WATER && getTile().liquidAmount > 0) {
				extraFood = (int) (getTile().liquidAmount * 10 + 0.5);
			}
			getFaction().addItem(ItemType.FOOD, 1 + extraFood);
			resetTimeToHarvest();
		}
		if(getType() == Game.buildingTypeMap.get("GRANARY")) {
			getFaction().addItem(ItemType.FOOD, 2);
			resetTimeToHarvest();
		}
		if(getType() == Game.buildingTypeMap.get("WINDMILL")) {
			getFaction().addItem(ItemType.FOOD, 10);
			resetTimeToHarvest();
		}
		if(getType() == Game.buildingTypeMap.get("SAWMILL")) {
			HashSet<Tile> tilesToCut = new HashSet<>();
			tilesToCut.add(getTile());
			
			for(Tile t : world.getNeighborsInRadius(getTile(), 3)) {
				tilesToCut.add(t);
			}
			for(Tile tile : tilesToCut) {
				if(tile.getPlant() != null && tile.getPlant().getPlantType() == PlantType.FOREST1) {
					tile.getPlant().takeDamage(1);
					getFaction().addItem(ItemType.WOOD, 1);
					if(tile.getPlant().isDead() ) {
						world.numCutTrees ++;
					}
				}
			}
			
			resetTimeToHarvest();
		}

		if(getType() == Game.buildingTypeMap.get("FARM") && getTile().hasUnit(Game.unitTypeMap.get("HORSE"))) {
			getFaction().addItem(ItemType.HORSE, 1);
			getFaction().addItem(ItemType.FOOD, 3);
			resetTimeToHarvest();
		}
	}
	public boolean readyToHarvest() {
		return timeToHarvest <= 0;
	}
	public void resetTimeToHarvest() {
		if(this.getTile().getResource() != null) {
			timeToHarvest = this.getTile().getResource().getType().getTimeToHarvest();
		}else {
			timeToHarvest = baseTimeToHarvest;
		}
		
	}
	public Tile getSpawnLocation() {
		return spawnLocation;
	}
	public void setRallyPoint(Tile tile) {
		spawnLocation = tile;
	}
	public void setProducingUnit(Unit producingUnit) {
		this.producingUnitList.add(producingUnit);
	}
	private void updateInProgressUnit() {
		if (producingUnitList.peek() != null) {
			producingUnitList.peek().expendEffort(1);
		}
	}
	public void updateCulture() {
		if(isBuilt()) {
			culture += buildingType.getCultureRate();
		}
	}
	
	public LinkedList<Unit> getProducingUnit() {
		return producingUnitList;
	}
	public double getCulture() {
		return culture;
	}
	public void setCulture(double culture) {
		this.culture = culture;
	}
	public void expendEffort(double effort) {
		remainingEffort -= effort;
		if(remainingEffort < 0) {
			remainingEffort = 0;
		}
	}
	public boolean isStarted() {
		return getRemainingEffort() < getType().getBuildingEffort();
	}
	public double getRemainingEffort() {
		return remainingEffort;
	}
	public void setRemainingEffort(double effort) {
		remainingEffort = effort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
	}
	public BuildingType getType() {
		return buildingType;
	}
	public void setType(BuildingType type) {
		this.buildingType = type;
	}
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("CT=%.1f", getCulture() ));
		if(!isBuilt()) {
			strings.add(String.format("work^2=%.0f", getRemainingEffort() ));
		}
		return strings;
	}
	private String roadCorner;
	public void setRoadCorner(String s) {
		roadCorner = s;
	}
	@Override
	public Image getImage(int size) {
		if(getType().isRoad()) {
			return getType().getRoadImage(roadCorner);
		}
		else {
			return super.getImage(size);
		}
	}
	
	@Override
	public String toString() {
		return buildingType.toString();
	}
	
}
