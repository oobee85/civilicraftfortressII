package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import game.components.*;
import utils.*;
import world.*;

public class Faction implements Externalizable {
	
	private static final Color[] factionColors = new Color[] { 
			Color.lightGray, Color.blue, Color.green.darker(), Color.pink, 
			Color.orange, Color.cyan, Color.yellow, Color.red, Color.magenta, Color.green};
	private static int idCounter = 0;
	
	private HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	private HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	private HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	private HashMap<ResourceType, ResearchRequirement> resourceResearchRequirements = new HashMap<>();
	

//	private Item[] items = new Item[ItemType.values().length];
	private Inventory inventory;
	
	private HashMap<String, Research> researchMap = new HashMap<>();
	private Research researchTarget;
	
	private LinkedList<AttackedNotification> attacked = new LinkedList<>();
	private LinkedList<AttackedNotification> newAttacked = new LinkedList<>();
	
	private Set<Building> buildings = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private SortedSet<Unit> units = new ConcurrentSkipListSet<>();
//	private Set<Unit> units = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	private int id;
	private Color color;
	private Color borderColor;
	private String name;
	private boolean usesItems;
	private boolean usesResearch;
	private boolean usesBuildings;
	private boolean isPlayer;
	private double environmentalDifficulty = 1;
	private int influence;
	

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readInt();
		this.setColor((Color)in.readObject());
		name = in.readUTF();
		usesItems = in.readBoolean();
		usesBuildings = in.readBoolean();
		isPlayer = in.readBoolean();
		researchTarget = (Research)in.readObject();
//		items = (Item[])in.readObject();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(id);
		out.writeObject(color);
		out.writeUTF(name);
		out.writeBoolean(usesItems);
		out.writeBoolean(isPlayer);
		out.writeObject(researchTarget);
//		out.writeObject(items);
	}
	
	public Faction() {
	}
	
	public Faction(String name, boolean isPlayer, boolean usesItems, boolean usesResearch) {
		this(name, isPlayer, usesItems, usesResearch, null);
	}
	public Faction(String name, boolean isPlayer, boolean usesItems, boolean usesResearch, Color color) {
		this.id = idCounter++;
		if(color == null) {
			this.setColor(idCounter < factionColors.length ? factionColors[idCounter] : factionColors[0]);
		}
		else {
			this.setColor(color);
		}
		this.name = name;
		this.usesItems = usesItems;
		this.usesResearch = usesResearch;
		this.isPlayer = isPlayer;
		this.inventory = new Inventory();
		setupResearch();
	}
	
	public boolean inRangeColony(Unit unit, Tile targetTile) {
		for(Building building: buildings) {
			if(building.getType().isColony() || building.getType().isCastle()) {
				int radius = building.getType().getVisionRadius();
				if(targetTile.getLocation().distanceTo(building.getTile().getLocation()) <= radius) {
					return true;
				}

			}
		}
		return false;
	}
	public void addUnit(Unit unit) {
		units.add(unit);
	}
	public void removeUnit(Unit unit) {
		units.remove(unit);
	}
	public Set<Unit> getUnits() {
		return Collections.unmodifiableSet(units);
	}
	public void addBuilding(Building building) {
		buildings.add(building);
	}
	public void removeBuilding(Building building) {
		buildings.remove(building);
	}
	public Set<Building> getBuildings() {
		return Collections.unmodifiableSet(buildings);
	}
	
	private void setColor(Color color) {
		this.color = color;
		this.borderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
	}
	public void raiseDifficultyBy(double add) {
		environmentalDifficulty += add; 
	}
	public double getDifficulty() {
		return environmentalDifficulty;
	}
	public Inventory getInventory() {
		return this.inventory;
	}
	public int id() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	public Color color() {
		return color;
	}
	public Color borderColor() {
		return borderColor;
	}
	public String name() {
		return name;
	}
	public static Faction getTempFaction() {
		Faction temp = new Faction("temp", false, false, false);
		idCounter--;
		return temp;
	}
	
	public boolean isNeutral() {
		return id == 0;
	}
	public boolean isPlayer() {
		return isPlayer;
	}
	
	public void recomputeInfluence() {
		double influence = 0;
		for (Building building : getBuildings()) {
			if (building.getType().isRoad()) {
				influence += building.getMaxHealth() / 20;
			}
			else if (building.getType().blocksMovement()) {
				influence += building.getMaxHealth() / 10;
			}
			else {
				influence += building.getMaxHealth() / 2;
			}
		}
		for (Unit unit : getUnits()) {
			influence += unit.getMaxHealth();
		}
		this.influence = (int) influence;
	}
	
	public int getLastComputedInfluence() {
		return influence;
	}
	
	public double computeVisibilityOfTile(Tile tile) {
		double visibility = 0;
		for (Unit u : tile.getUnits()) {
			if (u.getFaction() == this) {
				visibility += 1;
			}
		}
		if (tile.getBuilding() != null 
			&& tile.getBuilding().getFaction() == this
			&& tile.getBuilding().isBuilt()) {

			if (tile.getBuilding().getType().blocksMovement()) {
				visibility += 0.2;
			}
			else {
				visibility += 2;
			}
		}
		if (tile.getFaction() == this) {
			visibility += 0.2;
			visibility += tile.getTerrain().getBrightness();
			visibility += tile.liquidAmount * tile.liquidType.getBrightness();
			if (tile.getModifier() != null) {
				visibility += tile.getModifier().getType().getBrightness();
			}
		}
		return visibility;
	}

	public boolean isBuildingSelected(BuildingType type) {
		for(Building building : getBuildings()) {
			if(building.isSelected() 
					&& building.getFaction() == this 
					&& building.getType() == type
					&& building.isBuilt()) {
				return true;
			}
		}
		return false;
	
	}
	
	public boolean hasResearchLab() {
		BuildingType researchLabType = Game.buildingTypeMap.get("RESEARCH_LAB");
		for(Building building : getBuildings()) {
			if(building.getFaction() == this 
					&& building.getType() == researchLabType
					&& building.isBuilt()) {
				return true;
			}
		}
		return false;
	}
	
	public void researchEverything() {
		for(Research research : researchMap.values()) {
			research.spendResearch(research.getRequiredPoints());
		}
	}
	public boolean areRequirementsMet(UnitType type) {
		if (!usesResearch) {
			return true;
		}
		return unitResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(BuildingType type) {
		if (!usesResearch) {
			return true;
		}
		return buildingResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(ItemType type) {
		if (!usesResearch) {
			return true;
		}
		return craftResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(ResourceType type) {
		if (!usesResearch) {
			return true;
		}
		return resourceResearchRequirements.get(type).areRequirementsMet();
	}
	
	public void spendResearch(int points) {
		if(researchTarget != null) {
			researchTarget.spendResearch(points);
			if(researchTarget.isCompleted()) {
				researchTarget = null;
			}
		}
	}
	public Research getResearch(ResearchType type) {
		return researchMap.get(type.name);
	}
	public Research getResearchTarget() {
		return researchTarget;
	}
	public boolean setResearchTarget(ResearchType researchType) {
		Research research = researchMap.get(researchType.name);
		if(!research.getRequirement().areRequirementsMet() || research.isCompleted()) {
			return false;
		}
		if(!research.isPayedFor()) {
			if(canAfford(research.getCost()) && canAffordBuildingRequirement(research.getBuildingRequirement())) {
				payCost(research.getCost());
				research.setPayedFor(true);
			}
			else {
				return false;
			}
		}
		researchTarget = research;
		return true;
	}
	
	public void setupResearch() {
		for(ResearchType researchType : Game.researchTypeList) {
			Research research = new Research(researchType);
			researchMap.put(researchType.name, research);
		}
		for(Research research : researchMap.values()) {
			for(String required : research.type().researchRequirements) {
				research.getRequirement().addRequirement(researchMap.get(required));
			}
		}
		for(BuildingType type : Game.buildingTypeList) {
			ResearchRequirement req = new ResearchRequirement();
			if(type.getResearchRequirement() != null) {
				Research typesRequirement = researchMap.get(type.getResearchRequirement());
				req.addRequirement(typesRequirement);
			}
			buildingResearchRequirements.put(type, req);
		}
		for(UnitType type : Game.unitTypeList) {
			ResearchRequirement req = new ResearchRequirement();
			if(type.getResearchRequirement() != null) {
				Research typesRequirement = researchMap.get(type.getResearchRequirement());
				req.addRequirement(typesRequirement);
			}
			unitResearchRequirements.put(type, req);
		}
		for(ItemType type : ItemType.values()) {
			ResearchRequirement req = new ResearchRequirement();
//			if(type.getResearchRequirement() != null) {
//				Research typesRequirement = researches.get(type.getResearchRequirement());
//				req.addRequirement(typesRequirement);
//			}
			craftResearchRequirements.put(type, req);
		}
		for(ResourceType type : ResourceType.values()) {
			ResearchRequirement req = new ResearchRequirement();
			if(type.getResearchRequirement() != null) {
				Research typesRequirement = researchMap.get(type.getResearchRequirement());
				req.addRequirement(typesRequirement);
			}
			resourceResearchRequirements.put(type, req);
		}
	}
	
	public void gotAttacked(Tile tile) {
		newAttacked.add(new AttackedNotification(tile));
	}
	public LinkedList<AttackedNotification> getAttackedNotifications() {
		return attacked;
	}
	public void clearExpiredAttackedNotifications() {
		LinkedList<AttackedNotification> attackedNew = new LinkedList<>();
		for(AttackedNotification a : attacked) {
			if(!a.isExpired()) {
				attackedNew.add(a);
			}
		}
		attacked.addAll(newAttacked);
		newAttacked.clear();
		attacked = attackedNew;
	}
	
	public void craftItem(ItemType type, int amount) {
		BuildingType requiredBuilding = Game.buildingTypeMap.get(type.getBuilding());
		for(Building building : getBuildings()) {
			if(building.getType() == requiredBuilding && building.getFaction() == this) {
				for(int i = 0; i < amount && canAfford(type.getCost()); i++) {
					payCost(type.getCost());
					inventory.addItem(type, 1);
				}
				return;
			}
		}
	}
	
	public boolean usesItems() {
		return usesItems;
	}
	public boolean usesResearch() {
		return usesResearch;
	}
	
	public boolean usesBuildings() {
		return usesBuildings;
	}
	
	public boolean canAfford(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				if(this.inventory.getItemAmount(entry.getKey()) < entry.getValue()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean canAffordBuildingRequirement(HashMap<BuildingType, Integer> cost) {
		if(usesBuildings) {
			for (Entry<BuildingType, Integer> entry : cost.entrySet()) {
				int buildingCount = 0;
				for(Building building: this.getBuildings()) {
					if(building.getType() == entry) {
						buildingCount ++;
					}

				}
				if(buildingCount < entry.getValue()) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean canAfford(ItemType type, int quantity) {
		return !usesItems ||  this.inventory.getItemAmount(type) >= quantity;
	}
	
	public void payCost(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				this.inventory.addItem(entry.getKey(), -entry.getValue());
			}
		}
	}
	public void payCost(ItemType type, int quantity) {
		if(usesItems) {
			this.inventory.addItem(type, -quantity);
		}
	}
	
	@Override
	public String toString() {
		return Utils.getNiceName(name);
	}
}
