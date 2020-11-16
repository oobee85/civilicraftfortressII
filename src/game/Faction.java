package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;

import utils.*;
import world.*;

public class Faction implements Externalizable {
	
	private static final Color[] factionColors = new Color[] { 
			Color.lightGray, Color.blue, Color.green.darker(), Color.pink, 
			Color.orange, Color.cyan, Color.yellow};
	private static int idCounter = 0;
	
	private HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	private HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	private HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	private HashMap<ResourceType, ResearchRequirement> resourceResearchRequirements = new HashMap<>();
	

	private Item[] items = new Item[ItemType.values().length];
	
	private HashMap<String, Research> researchMap = new HashMap<>();
	private Research researchTarget;
	
	private LinkedList<AttackedNotification> attacked = new LinkedList<>();
	private LinkedList<AttackedNotification> newAttacked = new LinkedList<>();
	
	private HashSet<Building> buildings = new HashSet<>();
	
	private int id;
	private Color color;
	private Color borderColor;
	private String name;
	private boolean usesItems;
	private boolean isPlayer;
	private double environmentalDifficulty = 1;
	

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readInt();
		this.setColor((Color)in.readObject());
		name = in.readUTF();
		usesItems = in.readBoolean();
		isPlayer = in.readBoolean();
		researchTarget = (Research)in.readObject();
		items = (Item[])in.readObject();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(id);
		out.writeObject(color);
		out.writeUTF(name);
		out.writeBoolean(usesItems);
		out.writeBoolean(isPlayer);
		out.writeObject(researchTarget);
		out.writeObject(items);
	}
	
	public Faction() {
		initializeItems();
	}
	
	public Faction(String name, boolean isPlayer, boolean usesItems) {
		this(name, isPlayer, usesItems, idCounter < factionColors.length ? factionColors[idCounter] : factionColors[0]);
	}
	public Faction(String name, boolean isPlayer, boolean usesItems, Color color) {
		this.id = idCounter++;
		this.setColor(color);
		this.name = name;
		this.usesItems = usesItems;
		this.isPlayer = isPlayer;
		setupResearch();
		initializeItems();
	}
	
	public void addBuilding(Building building) {
		buildings.add(building);
	}
	public void removeBuilding(Building building) {
		buildings.remove(building);
	}
	public HashSet<Building> getBuildings() {
		return buildings;
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
	public Item[] getItems() {
		return items;
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
	private void initializeItems() {
		for(ItemType itemType : ItemType.values()) {
			items[itemType.ordinal()] = new Item(0, itemType);
		}
	}
	public static Faction getTempFaction() {
		Faction temp = new Faction("temp", false, false);
		idCounter--;
		return temp;
	}
	
	public boolean isNeutral() {
		return id == 0;
	}
	public boolean isPlayer() {
		return isPlayer;
	}

	public boolean isBuildingSelected(World world, BuildingType type) {
		for(Building building : world.getBuildings()) {
			if(building.getIsSelected() && building.getFaction() == this && building.getType() == type) {
				return true;
			}
		}
		return false;
	
	}
	
	public boolean hasResearchLab(World world) {
		for(Building building : world.getBuildings()) {
			if(building.getFaction() == this && building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
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
		return unitResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(BuildingType type) {
		return buildingResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(ItemType type) {
		return craftResearchRequirements.get(type).areRequirementsMet();
	}
	public boolean areRequirementsMet(ResourceType type) {
		return resourceResearchRequirements.get(type).areRequirementsMet();
	}
	
	public void spendResearch(int points) {
		if(researchTarget != null) {
			researchTarget.spendResearch(points);
		}
	}
	public Research getResearch(ResearchType type) {
		return researchMap.get(type.name);
	}
	public Research getResearchTarget() {
		return researchTarget;
	}
	public void setResearchTarget(ResearchType researchType) {
		Research research = researchMap.get(researchType.name);
		if(!research.getRequirement().areRequirementsMet()) {
			return;
		}
		if(!research.isPayedFor()) {
			if(canAfford(research.getCost())) {
				payCost(research.getCost());
				research.setPayedFor(true);
			}
			else {
				return;
			}
		}
		researchTarget = research;
	}
	
	public void setupResearch() {
		for(ResearchType researchType : Game.researchTypeList) {
			Research research = new Research(researchType);
			researchMap.put(researchType.name, research);
		}
		for(Research research : researchMap.values()) {
			for(String required : research.type().researchRequirements) {
				research.getRequirement().addRequirement(researchMap.get(required));
				System.out.println(research + " require  " + required);
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
					addItem(type, 1);
				}
				return;
			}
		}
	}
	
	public boolean usesItems() {
		return usesItems;
	}
	
	public int getItemAmount(ItemType type) {
		if(usesItems) {
			return items[type.ordinal()] != null ? items[type.ordinal()].getAmount() : 0;
		}
		return 0;
	}
	
	public void addItem(ItemType type, int quantity) {
		if(usesItems) {
			if(items[type.ordinal()] == null) {
				items[type.ordinal()] = new Item(0, type);
			}
			items[type.ordinal()].addAmount(quantity);
		}
	}
	
	public void setAmount(ItemType type, int amount) {
		if(usesItems) {
			if(items[type.ordinal()] == null) {
				items[type.ordinal()] = new Item(0, type);
			}
			items[type.ordinal()].addAmount(amount - items[type.ordinal()].getAmount());
		}
	}
	
	public boolean canAfford(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				if(items[entry.getKey().ordinal()] == null || items[entry.getKey().ordinal()].getAmount() < entry.getValue()) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean canAfford(ItemType type, int quantity) {
		return !usesItems || (items[type.ordinal()] != null && items[type.ordinal()].getAmount() >= quantity);
	}
	
	public void payCost(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				items[entry.getKey().ordinal()].addAmount(-entry.getValue());
			}
		}
	}
	public void payCost(ItemType type, int quantity) {
		if(usesItems) {
			items[type.ordinal()].addAmount(-quantity);
		}
	}
	
	@Override
	public String toString() {
		return Utils.getNiceName(name);
	}
}
