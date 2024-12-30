package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import game.actions.*;
import game.components.GameComponent;
import sounds.Sound;
import sounds.SoundEffect;
import sounds.SoundManager;
import utils.*;
import world.*;

public class Building extends Thing implements Serializable {

	public transient static final double CULTURE_AREA_MULTIPLIER = 0.1;
	
	private BuildingType buildingType;
	private double remainingEffort;
	private LinkedList<Unit> producingUnitList = new LinkedList<Unit>(); // TODO need to review if this works over network
	private double culture;
	private transient Tile spawnLocation;
	private transient double timeToHarvest;
	private transient double baseTimeToHarvest = 20;
	private transient double timeToProduce;
	private transient double baseTimeToProduce = 100;
	private boolean isPlanned;

	private int remainingEffortToProduceUnit;
	private transient Unit currentProducingUnit;
	
	private boolean isMoria = false;
	private int amountHarvested = 0;
	
	private transient ItemType stablesCaptured;
	
	public Building(BuildingType buildingType, Tile tile, Faction faction) {
		super(buildingType.getHealth(), buildingType.getMipMap(), faction, tile, buildingType.getInventoryStackSize());
		this.remainingEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
		this.spawnLocation = tile;
		this.timeToHarvest = baseTimeToHarvest;
		this.timeToProduce = baseTimeToProduce;
		this.isPlanned = false;
//		setRoadCorner(Direction.ALL_DIRECTIONS);
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
	public void tick(World world, boolean simulated) {
		if (timeToHarvest > 0) {
			timeToHarvest -= 1;
		}
		if (timeToProduce > 0) {
			timeToProduce -= 1;
		}
		
		if (!simulated && World.ticks % Constants.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			Tile tile = getTile();
			int[] tileDamage = tile.computeTileDamage();
			for(int i = 0; i < tileDamage.length; i++) {
				this.takeDamage(tileDamage[i], DamageType.values()[i]);
			}
		}
		
		// if building is a trap
		if(!simulated && isBuilt() && getType().isTrap()) {
			if (stablesCaptured == null) { // if theres no animal inside, skip
				
				// special units which provide their respective resource
				if (getTile().hasUnit(Game.unitTypeMap.get("HORSE"))) {
					stablesCaptured = ItemType.HORSE;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("ROC"))) {
					stablesCaptured = ItemType.GRIFFIN;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("ENT"))) {
					stablesCaptured = ItemType.ENT;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("VAMPIRE"))) {
					stablesCaptured = ItemType.VAMPIRE;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("WEREWOLF"))) {
					stablesCaptured = ItemType.WEREWOLF;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("WOLF"))) {
					stablesCaptured = ItemType.WOLF;
				}
				
				// non special units just give food
				if (getTile().hasUnit(Game.unitTypeMap.get("PIG"))) {
					stablesCaptured = ItemType.FOOD;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("COW"))) {
					stablesCaptured = ItemType.FOOD;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("SHEEP"))) {
					stablesCaptured = ItemType.FOOD;
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("DEER"))) {
					stablesCaptured = ItemType.FOOD;
				}
			}
		}
		

		if(!isBuilt()) {
			return;
		}
		
		

		// building produces units
		if(currentProducingUnit == null && !producingUnitList.isEmpty()) {
			currentProducingUnit = producingUnitList.peek();
			remainingEffortToProduceUnit = currentProducingUnit.getType().getCombatStats().getTicksToBuild();
		}
		if (currentProducingUnit != null) {
			remainingEffortToProduceUnit -= 1;
			if(remainingEffortToProduceUnit <= 0) {
				if (!simulated) {
					Unit unit = getProducingUnit().remove();
					PlannedAction whatToDo = null;
					if(unit.isBuilder()) {
						Plant plant = getSpawnLocation().getPlant();
						Building building = getSpawnLocation().getBuilding();
						
						if(plant != null && plant.getItem() != null) {// PLANT CASE
							whatToDo = PlannedAction.harvest(plant);
						}// BUILDING CASE
						else if(building != null && building.getFactionID() == unit.getFactionID() && building.getType().isHarvestable()) {
							whatToDo = PlannedAction.harvest(building);
						}else if(building != null && building.getFactionID() == unit.getFactionID() && building.getType().isHarvestable()) {
							
						}
						
					}
					if(whatToDo == null) {
						whatToDo = PlannedAction.moveTo(getSpawnLocation());
					}
					unit.queuePlannedAction(whatToDo);
					getTile().addUnit(unit);
					world.addUnit(unit);
					
					Sound sound = new Sound(SoundEffect.UNITCREATION, this.getFaction(), this.getTile());
					SoundManager.theSoundQueue.add(sound);
					
				}
				currentProducingUnit = null;
			}
		}

		if (simulated) {
			return;
		}

		if(getType().isCastle()) {
			getFaction().getInventory().takeAll(this.getInventory());
		}

		if(!readyToHarvest() ) {
			return;
		}
		if(getType() == Game.buildingTypeMap.get("CASTLE")) {
			getFaction().spendResearch(10);
			getFaction().getInventory().addItem(ItemType.FOOD, 1);
			resetTimeToHarvest();
		}
//		else if(getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
//			getFaction().spendResearch(10);
//			resetTimeToHarvest();
//		}
		else if(getType() == Game.buildingTypeMap.get("GRANARY")) {
			getFaction().getInventory().addItem(ItemType.FOOD, 2);
//			this.getInventory().addItem(ItemType.FOOD, 1);
			resetTimeToHarvest();
		}
		else if(getType() == Game.buildingTypeMap.get("WINDMILL")) {
			getFaction().getInventory().addItem(ItemType.FOOD, 5);
//			this.getInventory().addItem(ItemType.FOOD, 5);
			resetTimeToHarvest();
		}
		else if(getType().isTrap()) {
			if (stablesCaptured != null) {
				this.getInventory().addItem(stablesCaptured, 1);
				resetTimeToHarvest();
				amountHarvested ++;
				// every 50 harvests, play sound
				if(amountHarvested % 50 == 0) {
					Sound sound = new Sound(SoundEffect.TRAPCOW, this.getFaction(), this.getTile());
					SoundManager.theSoundQueue.add(sound);
				}
				
			}
		}
	}
	
	public void setMoria(boolean moria) {
		isMoria = moria;
	}
	public boolean isMoria() {
		return isMoria;
	}
	public boolean readyToHarvest() {
		return timeToHarvest <= 0;
	}
	public void resetTimeToHarvest(double timeToHarvest) {
		this.timeToHarvest = timeToHarvest;
	}
	public void resetTimeToHarvest() {
		if(getTile().getResource() != null) {
			resetTimeToHarvest(getTile().getResource().getTimeToHarvest());
		}
		else {
			resetTimeToHarvest(baseTimeToHarvest);
		}	
	}
	public boolean readyToProduce() {
		return timeToProduce <= 0;
	}
	public void resetTimeToProduce(double timeToProduce) {
		this.timeToProduce = timeToProduce;
	}
	public void resetTimeToProduce() {
		this.timeToProduce = this.baseTimeToProduce;
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
	public void setTiledImage(int tileBitmap) {
//		if(!getType().isRoad()) {
//			return;
//		}
		
		// Images/buildings/wall_brick.png is a placeholder image so mipmap doesnt fail to load
		MipMap mipmap = new MipMap("Images/buildings/wall_brick.png") {
			@Override
			public Image getImage(int size) {
				return getType().getTiledImage(tileBitmap);
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
