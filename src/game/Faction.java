package game;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;

import ui.*;
import utils.*;
import world.*;

public class Faction implements Serializable {
	
	private transient static final Color[] factionColors = new Color[] { 
			Color.lightGray, Color.blue, Color.green.darker(), Color.pink, 
			Color.orange, Color.cyan, Color.yellow};
	private transient static int idCounter = 0;

	private transient final HashMap<ItemType, Item> items = new HashMap<ItemType, Item>();
	
	private transient HashMap<String, Research> researchMap = new HashMap<>();
	private transient HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	private transient HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	private transient HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	private transient Research researchTarget;
	
	private transient LinkedList<AttackedNotification> attacked = new LinkedList<>();
	private transient LinkedList<AttackedNotification> newAttacked = new LinkedList<>();
	
	public final int id;
	public final Color color;
	public final String name;
	private final boolean usesItems;
	private final boolean isPlayer;
	
	public Faction(String name, boolean isPlayer, boolean usesItems) {
		this.id = idCounter++;
		this.color = id < factionColors.length ? factionColors[id] : factionColors[0];
		this.name = name;
		this.usesItems = usesItems;
		this.isPlayer = isPlayer;
		setupResearch();
	}
	public Faction(String name, boolean isPlayer, boolean usesItems, Color color) {
		this.id = idCounter++;
		this.color = color;
		this.name = name;
		this.usesItems = usesItems;
		this.isPlayer = isPlayer;
		setupResearch();
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
		for(Building building : world.buildings) {
			if(building.getIsSelected() && building.getFaction() == this && building.getType() == type) {
				return true;
			}
		}
		return false;
	
	}
	
	public boolean hasResearchLab(World world) {
		for(Building building : world.buildings) {
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
		if(canAfford(research.getCost())) {
			payCost(research.getCost());
			researchTarget = research;
		}
	}
	
	public void setupResearch() {
		for(ResearchType researchType : Game.researchTypeList) {
			Research research = new Research(researchType);
			researchMap.put(researchType.name, research);
		}
		for(Research research : researchMap.values()) {
			for(String required : research.type.researchRequirements) {
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
	
	public void craftItem(ItemType type, int amount, LinkedList<Building> buildings) {
		BuildingType requiredBuilding = Game.buildingTypeMap.get(type.getBuilding());
		for(Building building : buildings) {
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
			return items.containsKey(type) ? items.get(type).getAmount() : 0;
		}
		return 0;
	}
	
	public void addItem(ItemType type, int quantity) {
		if(usesItems) {
			if(!items.containsKey(type)) {
				items.put(type, new Item(0, type));
			}
			items.get(type).addAmount(quantity);
		}
	}
	
	public boolean canAfford(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				if(!items.containsKey(entry.getKey()) || items.get(entry.getKey()).getAmount() < entry.getValue()) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean canAfford(ItemType type, int quantity) {
		return !usesItems || (items.containsKey(type) && items.get(type).getAmount() >= quantity);
	}
	
	public void payCost(HashMap<ItemType, Integer> cost) {
		if(usesItems) {
			for (Entry<ItemType, Integer> entry : cost.entrySet()) {
				items.get(entry.getKey()).addAmount(-entry.getValue());
			}
		}
	}
	public void payCost(ItemType type, int quantity) {
		if(usesItems) {
			items.get(type).addAmount(-quantity);
		}
	}
	
	@Override
	public String toString() {
		return Utils.getNiceName(name);
	}
}
