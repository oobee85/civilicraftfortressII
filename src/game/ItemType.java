package game;

import java.util.HashMap;

import utils.*;

public enum ItemType {

	FOOD  ( "Images/itemicons/wheat.png"),
	WOOD  ( "Images/itemicons/wood.png"),
	STONE ( "Images/itemicons/rock.png"),
	MAGIC ( "Images/itemicons/magic.png"),
	
	HORSE    ( "Images/units/horse.png"),
	GRIFFIN  ( "Images/units/griffin.png"),
	ENT 	 ( "Images/units/ent.png"),
	VAMPIRE  ( "Images/units/vampire.png"),
	WEREWOLF ( "Images/units/werewolf.png"),
	WOLF 	 ( "Images/units/wolf.png"),
	
	CLAY       ( "Images/itemicons/clay.png"),
	COAL       ( "Images/itemicons/coal.png"),
	COPPER_ORE ( "Images/itemicons/copper_ore.png"),
	SILVER_ORE ( "Images/itemicons/silver_ore.png"),
	GOLD_ORE   ( "Images/itemicons/gold_ore.png"),
	IRON_ORE   ( "Images/itemicons/iron_ore.png"),
	MITHRIL_ORE( "Images/itemicons/mithril_ore.png"),
	
	ADAMANTITE_ORE  ( "Images/itemicons/adamantite_ore.png"),
	RUNITE_ORE 		( "Images/itemicons/runite_ore.png"),
	TITANIUM_ORE 	( "Images/itemicons/titanium_ore.png"),
	
	BRICK 	   	   ("QUARRY",  makeCosts(CLAY,50),						"Images/itemicons/brick.png"),
	
	BRONZE_BAR 	   ("SMITHY",  makeCosts(COPPER_ORE,5, SILVER_ORE, 5),	"Images/itemicons/bronze_bar.png"),
	GOLD_BAR 	   ("SMITHY",  makeCosts(GOLD_ORE, 10), 				"Images/itemicons/gold_bar.png"),
	IRON_BAR 	   ("SMITHY",  makeCosts(COAL,10, 	IRON_ORE,10),		"Images/itemicons/iron_bar.png"),
	MITHRIL_BAR    ("SMITHY",  makeCosts(COAL,20, 	MITHRIL_ORE,20),	"Images/itemicons/mithril_bar.png"),
	ADAMANTITE_BAR ("SMITHY",  makeCosts(COAL,50, 	ADAMANTITE_ORE,50),	"Images/itemicons/adamantite_bar.png"),
	RUNITE_BAR 	   ("SMITHY",  makeCosts(COAL,100, 	RUNITE_ORE,100),	"Images/itemicons/runite_bar.png"),
	TITANIUM_BAR   ("SMITHY",  makeCosts(COAL,100, 	TITANIUM_ORE,100),	"Images/itemicons/titanium_bar.png"),
	
	SWORD  	 ("SAWMILL",  makeCosts(WOOD,100),	"Images/itemicons/sword.png"),
	SHIELD   ("SAWMILL",  makeCosts(WOOD,100),	"Images/itemicons/shield.png"),
	BOW		 ("SAWMILL",  makeCosts(WOOD,100),	"Images/itemicons/bow.png"),
	
	BREAD	("GRANARY",  makeCosts(FOOD,100),	"Images/itemicons/bow.png"),
	
	BETTER_WEAPONS   	("RESEARCH_LAB",  makeCosts(SWORD,25),	"Images/interfaces/upgrades/better_weapons.png", "Increases attack damage of all units by : 25"),
	IMPROVED_SPARRING   ("RESEARCH_LAB",  makeCosts(SWORD,25),	"Images/interfaces/upgrades/improved_sparring.png", "Decreases attack delay of all units by : 5"),
	SHIELDS   			("RESEARCH_LAB",  makeCosts(SHIELD,25),	"Images/interfaces/upgrades/shields.png", "Increases health of all units by : 100"),
	MEDICINE   			("RESEARCH_LAB",  makeCosts(FOOD,1000),	"Images/interfaces/upgrades/medicine.png", "Decreases ticks to heal of all units by : 10"),
	BETTER_FORMATIONS   ("RESEARCH_LAB",  makeCosts(SHIELD,25),	"Images/interfaces/upgrades/better_formations.png", "Decreases ticks to move of all units by : 5"),
	FASTER_TRAINING   	("RESEARCH_LAB",  makeCosts(FOOD,1000),	"Images/interfaces/upgrades/faster_training.png", "Decreases the ticks to produce units by : 50"),
	UNDYING_ARMY   		("RESEARCH_LAB",  makeCosts(FOOD,10000),"Images/interfaces/upgrades/undying_army.png", "Gives all units lifesteal"),
	BROADHEADS   		("RESEARCH_LAB",  makeCosts(BOW, 25),	"Images/interfaces/upgrades/broadheads.png", "Increases damage of all projectiles by : 25"),
	
	;
	
	private MipMap mipmap;
	private HashMap <ItemType, Integer> cost;
	private String building;
	private String description;

	ItemType(String s) {
		 this.mipmap = new MipMap(s);
	}
	
	ItemType(String building, HashMap <ItemType, Integer> resourcesNeeded, String s) {
		this(s);
		this.cost = resourcesNeeded;
		this.building = building;
	}
	ItemType(String building, HashMap <ItemType, Integer> resourcesNeeded, String s, String description) {
		this(s);
		this.cost = resourcesNeeded;
		this.building = building;
		this.description = description;
	}
	
	/**
	 * Creates HashMap of argument pairs
	 * @param costs must be an even sized array of <ItemType, Integer> pairs.
	 */
	private static HashMap<ItemType, Integer> makeCosts(Object ...costs) {
		HashMap<ItemType, Integer> map = new HashMap<>();
		for(int i = 0; i < costs.length; i+=2) {
			ItemType type = (ItemType)costs[i];
			int amount = (int)costs[i+1];
			map.put(type, amount);
		}
		return map;
	}
	
	public String getDescription() {
		return this.description; 
	}
	public static ResearchRequirement getResearchRequirementForCrafting(ItemType type, HashMap<String, Research> researchMap) {
		ResearchRequirement req = new ResearchRequirement();

		if (type == GOLD_BAR) {
			req.addRequirement(researchMap.get("FARMING"));
		}
		else if (type == BRONZE_BAR) {
			req.addRequirement(researchMap.get("FARMING"));
		}
		else if (type == IRON_BAR) {
			req.addRequirement(researchMap.get("BRONZE_WORKING"));
		}
		else if (type == MITHRIL_BAR) {
			req.addRequirement(researchMap.get("IRON_WORKING"));
		}
		else if (type == ADAMANTITE_BAR) {
			req.addRequirement(researchMap.get("ARMORING"));
		}
		else if (type == RUNITE_BAR) {
			req.addRequirement(researchMap.get("ARMORING"));
		}
		else if (type == TITANIUM_BAR) {
			req.addRequirement(researchMap.get("CIVILIZATION"));
		}
		
		return req;
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public String getBuilding() {
		return building;
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
