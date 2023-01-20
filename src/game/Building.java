package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import game.actions.*;
import game.components.GameComponent;
import utils.*;
import world.*;

public class Building extends Thing implements Serializable {

	public transient static final double CULTURE_AREA_MULTIPLIER = 0.1;
	
	private BuildingType buildingType;
	private double remainingEffort;
	private LinkedList<Unit> producingUnitList = new LinkedList<Unit>();
	private double culture;
	private transient Tile spawnLocation;
	private transient double timeToHarvest;
	private transient double baseTimeToHarvest = 20;
	private boolean isPlanned;

	private int remainingEffortToProduceUnit;
	private transient Unit currentProducingUnit;
	
	public Building(BuildingType buildingType, Tile tile, Faction faction) {
		super(buildingType.getHealth(), buildingType.getMipMap(), buildingType.getMesh(), faction, tile);
		this.remainingEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
		this.spawnLocation = tile;
		this.timeToHarvest = baseTimeToHarvest;
		this.isPlanned = false;
		setRoadCorner(Direction.ALL_DIRECTIONS);
		for(GameComponent c : buildingType.getComponents()) {
			this.addComponent(c.getClass(), c);
		}
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
		
		if (World.ticks % Constants.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			Tile tile = getTile();
			int[] tileDamage = tile.computeTileDamage();
			for(int i = 0; i < tileDamage.length; i++) {
				this.takeDamage(tileDamage[i], DamageType.values()[i]);
			}
		}

		if(!isBuilt()) {
			return;
		}
		
		// building builds units
		if(remainingEffortToProduceUnit <= 0 && currentProducingUnit != null) {
			Unit unit = getProducingUnit().remove();
			unit.queuePlannedAction(PlannedAction.moveTo(getSpawnLocation()));
			getTile().addUnit(unit);
			world.addUnit(unit);
			currentProducingUnit = null;
		}
		
		if(!readyToHarvest() ) {
			return;
		}
		resetTimeToHarvest();
		if(getType() == Game.buildingTypeMap.get("CASTLE")) {
			getFaction().spendResearch(20);
			getFaction().getInventory().addItem(ItemType.FOOD, 1);
			getFaction().getInventory().takeAll(this.getInventory());
		}
		if(getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
			getFaction().spendResearch(20);
		}
		if(getType() == Game.buildingTypeMap.get("GRANARY")) {
			this.getInventory().addItem(ItemType.FOOD, 2);
		}
		if(getType() == Game.buildingTypeMap.get("WINDMILL")) {
			this.getInventory().addItem(ItemType.FOOD, 8);
		}
		if(getType() == Game.buildingTypeMap.get("STABLES") 
				&& getTile().hasUnit(Game.unitTypeMap.get("HORSE"))) {
			this.getInventory().addItem(ItemType.HORSE, 1);
		}
		if(getType() == Game.buildingTypeMap.get("STABLES") 
				&& getTile().hasUnit(Game.unitTypeMap.get("PIG"))) {
			this.getInventory().addItem(ItemType.FOOD, 1);
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
		if(buildingType.unitsCanProduce().length > 0) {
			spawnLocation = tile;
		}
	}
	public void setProducingUnit(Unit producingUnit) {
		this.producingUnitList.add(producingUnit);
	}
	private void updateInProgressUnit() {
		if(!isBuilt()) {
			return;
		}
		if(currentProducingUnit == null && !producingUnitList.isEmpty()) {
			currentProducingUnit = producingUnitList.peek();
			remainingEffortToProduceUnit = currentProducingUnit.getType().getCombatStats().getTicksToBuild();
		}
		if (currentProducingUnit != null) {
			remainingEffortToProduceUnit -= 1;
			if (remainingEffortToProduceUnit < 0) {
				remainingEffortToProduceUnit = 0;
			}
		}
	}
	public int getRemainingEffortToProduceUnit() {
		return remainingEffortToProduceUnit;
	}
	public void setRemainingEffortToProduceUnit(int remainingEffortToProduceUnit) {
		this.remainingEffortToProduceUnit = remainingEffortToProduceUnit;
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
	public void setRoadCorner(String roadCorner) {
		if(!getType().isRoad()) {
			return;
		}
		
		// Images/buildings/wall_brick.png is a placeholder image so mipmap doesnt fail to load
		MipMap mipmap = new MipMap("Images/buildings/wall_brick.png") {
			@Override
			public Image getImage(int size) {
				return getType().getRoadImage(roadCorner);
			}
			@Override
			public Image getShadow(int size) {
				return getType().getMipMap().getShadow(size);
			}
			@Override
			public Image getSunShadow(int size, int sun) {
				return getType().getMipMap().getSunShadow(size, sun);
			}
			@Override
			public Image getHighlight(int size) {
				return getType().getMipMap().getHighlight(size);
			}
			@Override
			public ImageIcon getImageIcon(int size) {
				return getType().getMipMap().getImageIcon(size);
			}
			@Override
			public Color getColor(int size) {
				return getType().getMipMap().getColor(size);
			}
		};
		super.setMipMap(mipmap);
	}
	
	@Override
	public String toString() {
		return buildingType.toString() + this.id();
	}
	
}
