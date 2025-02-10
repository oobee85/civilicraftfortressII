package game;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

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
	private double totalEffort;
	private LinkedList<Unit> producingUnitList = new LinkedList<Unit>(); // TODO need to review if this works over network
	private LinkedList<Item> producingItemList = new LinkedList<Item>();
	private double culture;
	private transient Tile spawnLocation;
	private transient double timeToProduce;
	private transient double baseTimeToProduce;
	private transient double timeToCraft;
	private transient double baseTimeToCraft;
	private boolean isPlanned;

	private int remainingEffortToProduceUnit;
	private transient Unit currentProducingUnit;
	
	private boolean isMoria = false;
	private int amountHarvested = 0;
	
	private transient ArrayList<ItemType> stablesCaptured = new ArrayList<ItemType>();
	
	public Building(BuildingType buildingType, Tile tile, Faction faction) {
		super(buildingType.getHealth(), buildingType.getMipMap(), faction, tile, buildingType.getInventoryStackSize());
		this.remainingEffort = buildingType.getBuildingEffort();
		this.totalEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
		this.spawnLocation = tile;
		this.timeToProduce = buildingType.getEffortToProduceHarvest();
		this.baseTimeToProduce = buildingType.getEffortToProduceHarvest(); // 20
		this.timeToCraft = buildingType.getEffortToProduceItem();
		this.baseTimeToCraft = buildingType.getEffortToProduceItem();
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
		if (timeToProduce > 0) {
			timeToProduce -= 1;
		}
		if (timeToCraft > 0) {
			timeToCraft -= 1;
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
//			if (stablesCaptured == null) { // if theres no animal inside, skip
			if (stablesCaptured != null && stablesCaptured.isEmpty()) { // if theres no animal inside, skip
				// special units which provide their respective resource
				if (getTile().hasUnit(Game.unitTypeMap.get("HORSE"))) {
					stablesCaptured.add(ItemType.HORSE);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("ROC"))) {
					stablesCaptured.add(ItemType.GRIFFIN);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("ENT"))) {
					stablesCaptured.add(ItemType.ENT);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("VAMPIRE"))) {
					stablesCaptured.add(ItemType.VAMPIRE);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("WEREWOLF"))) {
					stablesCaptured.add(ItemType.WEREWOLF);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("WOLF"))) {
					stablesCaptured.add(ItemType.WOLF);
				}
				
				// non special units just give food
				if (getTile().hasUnit(Game.unitTypeMap.get("PIG"))) {
					stablesCaptured.add(ItemType.FOOD);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("COW"))) {
					stablesCaptured.add(ItemType.FOOD);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("SHEEP"))) {
					stablesCaptured.add(ItemType.FOOD);
				}
				if (getTile().hasUnit(Game.unitTypeMap.get("DEER"))) {
					stablesCaptured.add(ItemType.FOOD);
				}
			}
		}
		if(!isBuilt()) {
			return;
		}
		// building produces units
		if(currentProducingUnit == null && !producingUnitList.isEmpty()) {
			currentProducingUnit = producingUnitList.peek();
			remainingEffortToProduceUnit = currentProducingUnit.getCombatStats().getTicksToBuild();
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
							// if building is harvestable
							whatToDo = PlannedAction.harvest(building);
						}else if(building != null && building.getFactionID() == unit.getFactionID() && !building.getType().isHarvestable()) {
							// if building is not harvestable
						}
						
					}
					if(whatToDo == null) {
						whatToDo = PlannedAction.moveTo(getSpawnLocation());
					}
					unit.queuePlannedAction(whatToDo);
					getTile().addUnit(unit);
					world.addUnit(unit);
					
					Sound sound = new Sound(SoundEffect.UNITCREATION, this.getFaction(), this.getTile(), 1f);
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
		
		BuildingType type = this.getType();
		if(!readyToProduce() ) {
			return;
		}
		if(type.isProducing()) {
			if(type.getProduced() == null) {
				System.err.println("Building.java tick() Building.getProduced() is null: " + type.name());
			}
			Faction faction = this.getFaction();
			// produce items
			for (Entry<ItemType, Integer> entry : type.getProduced().entrySet()) {
				faction.getInventory().addItem(entry.getKey(), entry.getValue());
			}
			
			if(type == Game.buildingTypeMap.get("CASTLE")) {
				getFaction().spendResearch(10);
			}
			resetTimeToProduce();
		}
		
		if(getType().isTrap()) {
			for(ItemType itemType: stablesCaptured) {
				this.getInventory().addItem(itemType, 1);
				resetTimeToProduce();
				amountHarvested ++;
				// every 50 harvests, play sound
				if(amountHarvested % 50 == 0) {
					Sound sound = new Sound(SoundEffect.TRAPCOW, this.getFaction(), this.getTile(), 1f);
					SoundManager.theSoundQueue.add(sound);
				}
				
			}
			stablesCaptured.clear();
		}
	}
	
	public void setMoria(boolean moria) {
		isMoria = moria;
	}
	public boolean isMoria() {
		return isMoria;
	}
	
	// for producing items, like food
	public boolean readyToProduce() {
		if(isBuilt() == false) {
			return false;
		}
		return timeToProduce <= 0;
	}
	public void setTimeToProduce(double timeToProduce) {
		this.timeToProduce = timeToProduce;
	}
	public void resetTimeToProduce() {
		this.timeToProduce = this.baseTimeToProduce;
	}
	
	// for crafting items automatically
	public boolean readyToCraft() {
		if(isBuilt() == false) {
			return false;
		}
		return timeToCraft <= 0;
	}
	public void setTimeToCraft(double timeToCraft) {
		this.timeToCraft = timeToCraft;
	}
	public void resetTimeToCraft() {
		this.timeToCraft = this.baseTimeToCraft;
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
//	public void addProducingItem(Item producingItem) {
//		this.producingItemList.add(producingItem);
//	}
//	public LinkedList<Item> getProducingItem() {
//		return producingItemList;
//	}
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
	public void setTotalEffort(double totalEffort) {
		this.totalEffort = totalEffort;
	}
	public double getTotalEffort() {
		return this.totalEffort;
	}
	public void setRemainingEffort(double effort) {
		remainingEffort = effort;
//		remainingEffort = updateEffortBasedOnTileConditions(effort);
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
		super.setMipMap(getType().getTiledImage(tileBitmap));
	}
	
	@Override
	public String toString() {
		return buildingType.toString() + this.id();
	}
	
}
