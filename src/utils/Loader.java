package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.json.*;

import game.*;
import game.components.*;
import ui.*;
import ui.graphics.opengl.*;
import world.*;

public class Loader {
	private static HashMap<ItemType, Integer> loadItemTypeMap(JSONObject costObject) {
		HashMap<ItemType, Integer> map = new HashMap<>();
		for(String itemName : costObject.keySet()) {
			map.put(ItemType.valueOf(itemName), costObject.getInt(itemName));
		}
		return map;
	}
	
	public static Set<Component> loadComponents(JSONObject obj) {
		Set<Component> components = new HashSet<>();

		if(obj.has("resistances")) {
			JSONObject resistances = obj.getJSONObject("resistances");
			int[] resistanceValues = DamageResistance.getDefaultResistance();
			for(String typeString : resistances.keySet()) {
				int value = resistances.getInt(typeString);
				DamageType type = DamageType.valueOf(typeString);
				resistanceValues[type.ordinal()] = value;
			}
			components.add(new DamageResistance(resistanceValues));
		}
		return components;
	}
	
	public static void loadPlantType(HashMap<String, PlantType> plantTypeMap, ArrayList<PlantType> plantTypeList) {
		String plantTypeString = Utils.readFile("costs/PlantType.json");
		JSONObject obj = new JSONObject(plantTypeString);
		JSONArray arr = obj.getJSONArray("planttypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject plantTypeObject = arr.getJSONObject(i);

			String name = plantTypeObject.getString("name");
			String image = plantTypeObject.getString("image");
			int health = plantTypeObject.getInt("health");
			double rarity = plantTypeObject.getDouble("rarity");
			String itemString = plantTypeObject.getString("harvestitem");
			ItemType itemType = ItemType.valueOf(itemString);

			String textureFile = image;
			Mesh mesh = MeshUtils.defaultPlant;
			if(plantTypeObject.has("mesh")) {
				String meshString = plantTypeObject.getString("mesh");
				mesh = MeshUtils.getMeshByFileName(meshString);
				
				if(plantTypeObject.has("texture")) {
					textureFile = plantTypeObject.getString("texture");
				}
			}

			HashSet<String> attributes = new HashSet<>();
			if(plantTypeObject.has("attributes")) {
				JSONArray attributelist = plantTypeObject.getJSONArray("attributes");
				for(int j = 0; j < attributelist.length(); j++) {
					attributes.add(attributelist.getString(j));
				}
			}

			Set<Component> components = loadComponents(plantTypeObject);
			PlantType plantType = new PlantType(name, image, mesh, textureFile, rarity, health, itemType, attributes);
			plantType.getComponents().addAll(components);
			
			plantTypeMap.put(name, plantType);
			plantTypeList.add(plantType);
		}
		
	}
	
	public static void loadBuildingType(HashMap<String, BuildingType> buildingTypeMap, ArrayList<BuildingType> buildingTypeList) {

		String buildingTypeString = Utils.readFile("costs/BuildingType.json");
//		System.out.println("Loaded :" + buildingTypeString);
		JSONObject obj = new JSONObject(buildingTypeString);
		JSONArray arr = obj.getJSONArray("buildingtypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject buildingTypeObject = arr.getJSONObject(i);
			
			String name = buildingTypeObject.getString("name");
			String image = buildingTypeObject.getString("image");

			double culture = buildingTypeObject.getDouble("culture");
			int health = buildingTypeObject.getInt("health");
			int vision = buildingTypeObject.getInt("vision");
			double effort = buildingTypeObject.getDouble("effort");
			double movespeed = 1;
			if(buildingTypeObject.has("movespeed")) {
				movespeed = buildingTypeObject.getDouble("movespeed");
			}
			String info = "";
			if(buildingTypeObject.has("info")) {
				info = buildingTypeObject.getString("info");
			}
			
			HashSet<String> attributes = new HashSet<>();
			if(buildingTypeObject.has("attributes")) {
				JSONArray attributelist = buildingTypeObject.getJSONArray("attributes");
				for(int j = 0; j < attributelist.length(); j++) {
					attributes.add(attributelist.getString(j));
				}
			}
			String[] buildsunits = new String[0];
			if(buildingTypeObject.has("buildsunits")) {
				JSONArray buildsUnitsArray = buildingTypeObject.getJSONArray("buildsunits");
				buildsunits = new String[buildsUnitsArray.length()];
				for(int j = 0; j < buildsUnitsArray.length(); j++) {
					buildsunits[j] = buildsUnitsArray.getString(j);
				}
			}
			
			String researchReq = null;
			if(buildingTypeObject.has("research")) {
				researchReq = buildingTypeObject.getString("research");
			}
			
			HashMap<ItemType, Integer> cost = null;
			if(buildingTypeObject.has("cost")) {
				cost = loadItemTypeMap(buildingTypeObject.getJSONObject("cost"));
			}
			
			String textureFile = image;
			Mesh mesh = MeshUtils.defaultBuilding;
			if(buildingTypeObject.has("mesh")) {
				String meshString = buildingTypeObject.getString("mesh");
				mesh = MeshUtils.getMeshByFileName(meshString);
				
				if(buildingTypeObject.has("texture")) {
					textureFile = buildingTypeObject.getString("texture");
				}
			}

			Set<Component> components = loadComponents(buildingTypeObject);
			BuildingType buildingType = new BuildingType(name, info, health, effort, image, mesh, textureFile, culture, vision, researchReq, cost, buildsunits, movespeed, attributes);
			buildingType.getComponents().addAll(components);
			buildingTypeMap.put(name, buildingType);
			buildingTypeList.add(buildingType);
		}
	}
	public static void writeBuildingTypes() {
//		JSONObject obj = new JSONObject();
//		for(BuildingType b : BuildingType.values()) {
//			JSONObject buildingObject = new JSONObject();
//			
//			buildingObject.accumulate("name", b.name());
//			
//			buildingObject.accumulate("image", b.image);
//			buildingObject.accumulate("health", b.getHealth());
//			buildingObject.accumulate("effort", b.getBuildingEffort());
//			buildingObject.accumulate("culture", b.cultureRate);
//			buildingObject.accumulate("vision", b.getVisionRadius());
//			if(b.getSpeed() != 1) {
//				buildingObject.accumulate("movespeed", b.getSpeed());
//			}
//			if(b.getResearchRequirement() != null) {
//				buildingObject.accumulate("research", b.getResearchRequirement());
//			}
//			if(b.getCost() != null && !b.getCost().isEmpty()) {
//				JSONObject costObject = new JSONObject();
//				for(Entry<ItemType, Integer> entry : b.getCost().entrySet()) {
//					costObject.accumulate(entry.getKey().name(), entry.getValue());
//				}
//				buildingObject.accumulate("cost", costObject);
//			}
//			if(b.unitsCanBuild() != null && b.unitsCanBuild().length > 0) {
//				JSONArray attributeArray = new JSONArray();
//				for(String unittype : b.unitsCanBuild()) {
//					attributeArray.put(unittype);
//				}
//				buildingObject.put("buildsunits", attributeArray);
//			}
//			
//			JSONArray attributeArray = new JSONArray();
//			if(!b.canMoveThrough()) {
//				attributeArray.put("blocksmovement");
//			}
//			if(b.isRoad()) {
//				attributeArray.put("road");
//			}
//			if(!attributeArray.isEmpty()) {
//				buildingObject.put("attributes", attributeArray);
//			}
//			
//			obj.accumulate("buildingtypes", buildingObject);
//		}
//		String arr = obj.toString();
//		System.out.println(arr);
//		try(FileWriter fw = new FileWriter("BuildingType.json"); BufferedWriter bw = new BufferedWriter(fw);) {
//			bw.write(arr);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	public static void loadUnitType(HashMap<String, UnitType> unitTypeMap, ArrayList<UnitType> unitTypeList) {
		String unitTypeString = Utils.readFile("costs/UnitType.json");
//		System.out.println("Loaded :" + unitTypeString);
		JSONObject obj = new JSONObject(unitTypeString);
		JSONArray arr = obj.getJSONArray("unittypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject unitTypeObject = arr.getJSONObject(i);
			
			String name = unitTypeObject.getString("name");
			String image = unitTypeObject.getString("image");
			
			JSONObject statsObject = unitTypeObject.getJSONObject("stats");
			int healspeed = statsObject.getInt("healspeed");
			int health = statsObject.getInt("health");
			int movespeed = statsObject.getInt("movespeed");
			int buildtime = statsObject.getInt("buildtime");
			CombatStats combatStats = new CombatStats(health, movespeed, buildtime, healspeed);
			
			HashSet<String> attributes = new HashSet<>();
			if(unitTypeObject.has("attributes")) {
				JSONArray attributelist = unitTypeObject.getJSONArray("attributes");
				for(int j = 0; j < attributelist.length(); j++) {
					attributes.add(attributelist.getString(j));
				}
			}
			
			// TODO resistances: { "heat":50, "cold":200 }
//			DamageResistance damageResistance = null;
//			if(unitTypeObject.has("resistances")) {
//				int[] resistanceValues = DamageResistance.getDefaultResistance();
//				JSONObject resistances = unitTypeObject.getJSONObject("resistances");
//				for(String typeString : resistances.keySet()) {
//					int value = resistances.getInt(typeString);
//					DamageType type = DamageType.valueOf(typeString);
//					resistanceValues[type.ordinal()] = value;
//				}
//				damageResistance = new DamageResistance(resistanceValues);
//			}
			
			String researchReq = null;
			if(unitTypeObject.has("research")) {
				researchReq = unitTypeObject.getString("research");
			}
			
			HashMap<ItemType, Integer> cost = null;
			if(unitTypeObject.has("cost")) {
				cost = loadItemTypeMap(unitTypeObject.getJSONObject("cost"));
			}
			
			LinkedList<Item> items = new LinkedList<>();
			if(unitTypeObject.has("loot")) {
				HashMap<ItemType, Integer> loot = loadItemTypeMap(unitTypeObject.getJSONObject("loot"));
				for(Entry<ItemType, Integer> entry : loot.entrySet()) {
					items.add(new Item(entry.getValue(), entry.getKey()));
				}
			}
			TargetInfo[] targeting = null;
			if(unitTypeObject.has("targeting")) {
				JSONArray targetingList = unitTypeObject.getJSONArray("targeting");
				targeting = new TargetInfo[targetingList.length()];
				for(int j = 0; j < targetingList.length(); j++) {
					targeting[j] = parseTargetInfoFromJSON(targetingList.getJSONObject(j));
				}
			}
			LinkedList<AttackStyle> attackStyles = new LinkedList<>();
			if(unitTypeObject.has("attackstyles")) {
				JSONArray attackStyleList = unitTypeObject.getJSONArray("attackstyles");
				for(int j = 0; j < attackStyleList.length(); j++) {
					attackStyles.add(parseAttackStyleFromJSON(attackStyleList.getJSONObject(j)));
				}
			}

			String textureFile = image;
			Mesh mesh = MeshUtils.defaultUnit;
			if(unitTypeObject.has("mesh")) {
				String meshString = unitTypeObject.getString("mesh");
				mesh = MeshUtils.getMeshByFileName(meshString);
				
				if(unitTypeObject.has("texture")) {
					textureFile = unitTypeObject.getString("texture");
				}
			}
			Set<Component> components = loadComponents(unitTypeObject);
			UnitType unitType = new UnitType(name, image, mesh, textureFile, combatStats, attributes, researchReq, cost, items, targeting, attackStyles);
			unitType.getComponents().addAll(components);
			unitTypeMap.put(name, unitType);
			unitTypeList.add(unitType);

			if(unitType.isDelayedInvasion()) {
				System.out.println(unitType + " has delayed invasion");
			}
		}
	}
	
	public static void writeUnitTypes() {
//		JSONObject obj = new JSONObject();
//		for(UnitType u : UnitType.values()) {
//			JSONObject unitObject = new JSONObject();
//			
//			unitObject.accumulate("name", u.name());
//			
//			unitObject.accumulate("image", u.image);
//			
//			JSONObject statsObject = new JSONObject();
//			statsObject.accumulate("attack", u.getCombatStats().getAttack());
//			statsObject.accumulate("range", u.getCombatStats().getAttackRadius());
//			statsObject.accumulate("attackspeed", u.getCombatStats().getAttackSpeed());
//			statsObject.accumulate("healspeed", u.getCombatStats().getHealSpeed());
//			statsObject.accumulate("health", u.getCombatStats().getHealth());
//			statsObject.accumulate("movespeed", u.getCombatStats().getMoveSpeed());
//			statsObject.accumulate("buildtime", u.getCombatStats().getTicksToBuild());
//			unitObject.accumulate("stats", statsObject);
//			
//			JSONArray attributeArray = new JSONArray();
//			if(u.isAquatic()) {
//				attributeArray.put("aquatic");
//			}
//			if(u.isFlying()) {
//				attributeArray.put("flying");
//			}
//			if(u.isHostile()) {
//				attributeArray.put("hostile");
//			}
//			if(u.isColdResist()) {
//				attributeArray.put("coldresistant");
//			}
//			if(u.isFireResist()) {
//				attributeArray.put("fireresistant");
//			}
//			unitObject.put("attributes", attributeArray);
//			
//			if(u.getResearchRequirement() != null) {
//				unitObject.accumulate("research", u.getResearchRequirement());
//			}
//			if(u.getCost() != null && !u.getCost().isEmpty()) {
//				JSONObject costObject = new JSONObject();
//				for(Entry<ItemType, Integer> entry : u.getCost().entrySet()) {
//					costObject.accumulate(entry.getKey().name(), entry.getValue());
//				}
//				unitObject.accumulate("cost", costObject);
//			}
//			if(u.getDeadItem() != null) {
//				JSONObject lootObject = new JSONObject();
//				lootObject.accumulate(u.getDeadItem().getType().name(), u.getDeadItem().getAmount());
//				unitObject.accumulate("loot", lootObject);
//			}
//			if(u.getProjectileType() != null) {
//				unitObject.accumulate("projectile", u.getProjectileType().name());
//			}
//			
//			obj.accumulate("unittypes", unitObject);
//		}
//		String arr = obj.toString();
//		System.out.println(arr);
//		try(FileWriter fw = new FileWriter("UnitType.json"); BufferedWriter bw = new BufferedWriter(fw);) {
//			bw.write(arr);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static void loadResearchType(HashMap<String, ResearchType> researchTypeMap, ArrayList<ResearchType> researchTypeList) {
		HashMap<Research, LinkedList<String>> researchRequirements = new HashMap<>();
		String researchCosts = Utils.readFile("costs/ResearchType.json");
		JSONObject obj = new JSONObject(researchCosts);
		JSONArray arr = obj.getJSONArray("researches");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject researchObject = arr.getJSONObject(i);
			String researchName = researchObject.getString("name");
			String imagePath = Research.DEFAULT_RESEARCH_IMAGE_PATH;
			if(researchObject.has("image")) {
				imagePath = researchObject.getString("image");
			}
			int points = researchObject.getInt("points");
			int tier = researchObject.getInt("tier");
			HashMap<ItemType, Integer> cost = new HashMap<>();
			if(researchObject.has("cost")) {
				cost = loadItemTypeMap(researchObject.getJSONObject("cost"));
			}
			LinkedList<String> reqs = new LinkedList<>();
			if(researchObject.has("requirements")) {
				JSONArray requirementArray = researchObject.getJSONArray("requirements");
				for(int j = 0; j < requirementArray.length(); j++) {
					String requirement = requirementArray.getString(j);
					reqs.add(requirement);
				}
			}
			ResearchType researchType = new ResearchType(researchName, imagePath, reqs, points, tier, cost);
			researchTypeMap.put(researchName, researchType);
			researchTypeList.add(researchType);
		}
	}
	
	public static void doMappings() {
		for(BuildingType type : Game.buildingTypeList) {
			for(String unittypestring : type.unitsCanProduce()) {
				type.unitsCanProduceSet().add(Game.unitTypeMap.get(unittypestring));
			}
		}
		for(ResearchType type : Game.researchTypeList) {
			for(String req : type.researchRequirements) {
				ResearchType researchReq = Game.researchTypeMap.get(req);
				researchReq.unlocks.add(Utils.getNiceName(type.name));
			}
		}
		for(BuildingType type : Game.buildingTypeList) {
			if(type.getResearchRequirement() != null) {
				ResearchType researchReq = Game.researchTypeMap.get(type.getResearchRequirement());
				researchReq.unlocks.add(Utils.getNiceName(type.name()));
			}
		}
		for(UnitType type : Game.unitTypeList) {
			if(type.getResearchRequirement() != null) {
				ResearchType researchReq = Game.researchTypeMap.get(type.getResearchRequirement());
				researchReq.unlocks.add(Utils.getNiceName(type.name()));
			}
		}
	}
	public static void doTargetingMappings() {
		for(UnitType type : Game.unitTypeList) {
			if(type.getTargetingInfoStrings() != null) {
				for(TargetInfo targetingInfo : type.getTargetingInfoStrings()) {
					Object targetType = null;
					boolean isWall = false;
					if(Game.unitTypeMap.containsKey(targetingInfo.type)) {
						targetType = Game.unitTypeMap.get(targetingInfo.type);
					}
					else if(Game.buildingTypeMap.containsKey(targetingInfo.type)) {
						targetType = Game.buildingTypeMap.get(targetingInfo.type);
					}
					else if(targetingInfo.type.equals("WALL")) {
						targetType = Building.class;
						isWall = true;
					}
					else {
						try {
							Class<?> cls = Class.forName(targetingInfo.type);
							targetType = cls;
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					if(targetType != null) {
						type.getTargetingInfo().add(new TargetingInfo(targetType, targetingInfo.faction, isWall));
					}
				}
			}
		}
	}
	public static class TargetInfo {
		public final String type;
		public final String faction;
		public TargetInfo(String type, String faction) {
			this.type = type;
			this.faction = faction;
		}
	}
	
	private static AttackStyle parseAttackStyleFromJSON(JSONObject obj) {
		int damage = obj.getInt("damage");
		int range = obj.getInt("range");
		int cooldown = obj.getInt("cooldown");
		int minRange = 0;
		if(obj.has("minrange")) {
			minRange = obj.getInt("minrange");
		}
		boolean lifesteal = false;
		if(obj.has("lifesteal")) {
			lifesteal = obj.getBoolean("lifesteal");
		}
		ProjectileType projectile = null;
		if(obj.has("projectile")) {
			projectile = ProjectileType.valueOf(obj.getString("projectile"));
		}
		return new AttackStyle(damage, range, minRange, lifesteal, cooldown, projectile);
	}
	
	private static TargetInfo parseTargetInfoFromJSON(JSONObject obj) {
		String type = obj.getString("type");
		String faction = null;
		if(obj.has("faction")) {
			faction = obj.getString("faction");
		}
		return new TargetInfo(type, faction);
	}
}
