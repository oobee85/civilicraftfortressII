package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.json.*;

import game.*;
import ui.*;
import world.*;

public class Loader {
	private static String readFile(String filename) {
		String researchCosts = "";
//		URL path = Utils.class.getClassLoader().getResource(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(filename)));
		try {
//		try (BufferedReader br = new BufferedReader(new FileReader(path.getPath()))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				line = line.replaceAll("\\s+","");
				builder.append(line + "\n");
			}
			researchCosts = builder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return researchCosts;
	}
	private static HashMap<ItemType, Integer> loadItemTypeMap(JSONObject costObject) {
		HashMap<ItemType, Integer> map = new HashMap<>();
		for(String itemName : costObject.keySet()) {
			map.put(ItemType.valueOf(itemName), costObject.getInt(itemName));
		}
		return map;
	}
	
	public static void loadBuildingType(HashMap<String, BuildingType> buildingTypeMap, ArrayList<BuildingType> buildingTypeList) {

		String buildingTypeString = readFile("resources/costs/BuildingType.json");
//		System.out.println("Loaded :" + buildingTypeString);
		JSONObject obj = new JSONObject(buildingTypeString);
		JSONArray arr = obj.getJSONArray("buildingtypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject buildingTypeObject = arr.getJSONObject(i);
			
			String name = buildingTypeObject.getString("name");
			String image = buildingTypeObject.getString("image");

			double culture = buildingTypeObject.getDouble("culture");
			double health = buildingTypeObject.getDouble("health");
			int vision = buildingTypeObject.getInt("vision");
			double effort = buildingTypeObject.getDouble("effort");
			double movespeed = 1;
			if(buildingTypeObject.has("movespeed")) {
				movespeed = buildingTypeObject.getDouble("movespeed");
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
			
			BuildingType buildingType = new BuildingType(name, health, effort, image, culture, vision, researchReq, cost, buildsunits, movespeed, attributes);
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
		String unitTypeString = readFile("resources/costs/UnitType.json");
//		System.out.println("Loaded :" + unitTypeString);
		JSONObject obj = new JSONObject(unitTypeString);
		JSONArray arr = obj.getJSONArray("unittypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject unitTypeObject = arr.getJSONObject(i);
			
			String name = unitTypeObject.getString("name");
			String image = unitTypeObject.getString("image");
			
			JSONObject statsObject = unitTypeObject.getJSONObject("stats");
			int attack = statsObject.getInt("attack");
			int range = statsObject.getInt("range");
			int attackspeed = statsObject.getInt("attackspeed");
			int healspeed = statsObject.getInt("healspeed");
			int health = statsObject.getInt("health");
			int movespeed = statsObject.getInt("movespeed");
			int buildtime = statsObject.getInt("buildtime");
			CombatStats combatStats = new CombatStats(health, attack, movespeed, range, attackspeed, buildtime, healspeed);
			
			HashSet<String> attributes = new HashSet<>();;
			if(unitTypeObject.has("attributes")) {
				JSONArray attributelist = unitTypeObject.getJSONArray("attributes");
				for(int j = 0; j < attributelist.length(); j++) {
					attributes.add(attributelist.getString(j));
				}
			}
			
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
			ProjectileType projectile = null;
			if(unitTypeObject.has("projectile")) {
				projectile = ProjectileType.valueOf(unitTypeObject.getString("projectile"));
			}
			TargetInfo[] targeting = null;
			if(unitTypeObject.has("targeting")) {
				JSONArray targetingList = unitTypeObject.getJSONArray("targeting");
				targeting = new TargetInfo[targetingList.length()];
				for(int j = 0; j < targetingList.length(); j++) {
					targeting[j] = parseTargetInfoFromJSON(targetingList.getJSONObject(j));
				}
			}
			
			UnitType unitType = new UnitType(name, image, combatStats, attributes, researchReq, cost, items, projectile, targeting);
			unitTypeMap.put(name, unitType);
			unitTypeList.add(unitType);
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
		String researchCosts = readFile("resources/costs/ResearchType.json");
		System.out.println(researchCosts);
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
	
	public static void doMakingUnitMappings() {
		for(BuildingType type : Game.buildingTypeList) {
			for(String unittypestring : type.unitsCanBuild()) {
				type.unitsCanBuildSet().add(Game.unitTypeMap.get(unittypestring));
			}
		}
	}
	public static void doTargetingMappings() {
		for(UnitType type : Game.unitTypeList) {
			if(type.getTargetingInfoStrings() != null) {
				for(TargetInfo targetingInfo : type.getTargetingInfoStrings()) {
					if(Game.unitTypeMap.containsKey(targetingInfo.type)) {
						type.getTargetingInfo().add(new TargetingInfo(Game.unitTypeMap.get(targetingInfo.type), targetingInfo.faction));
						continue;
					}
					else if(Game.buildingTypeMap.containsKey(targetingInfo.type)) {
						type.getTargetingInfo().add(new TargetingInfo(Game.buildingTypeMap.get(targetingInfo.type), targetingInfo.faction));
						continue;
					}
					try {
						Class<?> cls = Class.forName(targetingInfo.type);
						type.getTargetingInfo().add(new TargetingInfo(cls, targetingInfo.faction));
						continue;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
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
	
	private static TargetInfo parseTargetInfoFromJSON(JSONObject obj) {
		String type = obj.getString("type");
		String faction = null;
		if(obj.has("faction")) {
			faction = obj.getString("faction");
		}
		return new TargetInfo(type, faction);
	}
}
