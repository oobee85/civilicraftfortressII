package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.json.*;

import game.*;

public class Loader {
	private static String readFile(String filename) {
		String researchCosts = "";
		URL path = Utils.class.getClassLoader().getResource(filename);
		try (BufferedReader br = new BufferedReader(new FileReader(path.getPath()))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				line = line.replaceAll("\\s+","");
				builder.append(line);
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
	public static void loadUnitType(HashMap<String, UnitType> unitTypeMap, ArrayList<UnitType> unitTypeList) {
		String unitTypeString = readFile("resources/costs/UnitType.json");
		System.out.println("Loaded :" + unitTypeString);
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
			
			HashSet<String> attributes = null;
			if(unitTypeObject.has("attributes")) {
				JSONArray attributelist = unitTypeObject.getJSONArray("attributes");
				attributes = new HashSet<>();
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
			
			Item item = null;
			if(unitTypeObject.has("loot")) {
				HashMap<ItemType, Integer> loot = loadItemTypeMap(unitTypeObject.getJSONObject("loot"));
				for(Entry<ItemType, Integer> entry : loot.entrySet()) {
					item = new Item(entry.getValue(), entry.getKey());
					// TODO only get first loot for now but make it list eventually
					break;
				}
			}
			ProjectileType projectile = null;
			if(unitTypeObject.has("projectile")) {
				projectile = ProjectileType.valueOf(unitTypeObject.getString("projectile"));
			}
			
			UnitType unitType = new UnitType(name, image, combatStats, attributes, researchReq, cost, item, projectile);
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

	public static void setupResearch(HashMap<String, Research> researches, ArrayList<Research> researchList) {
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
			Research research = new Research(researchName, imagePath, points, tier);
			researches.put(researchName, research);
			researchList.add(research);
			
			if(researchObject.has("cost")) {
				HashMap<ItemType, Integer> cost = loadItemTypeMap(researchObject.getJSONObject("cost"));
				for(Entry<ItemType, Integer> entry : cost.entrySet()) {
					System.out.println(researchName + ":" + entry.getKey() + ":" + entry.getValue());
					research.addCost(entry.getKey(), entry.getValue());
				}
			}
			if(researchObject.has("requirements")) {
				JSONArray requirementArray = researchObject.getJSONArray("requirements");
				LinkedList<String> reqs = new LinkedList<>();
				for(int j = 0; j < requirementArray.length(); j++) {
					String requirement = requirementArray.getString(j);
					reqs.add(requirement);
				}
				researchRequirements.put(research, reqs);
			}
		}
		// Link research requirements
		for(Research research : researchRequirements.keySet()) {
			for(String req : researchRequirements.get(research)) {
				Research requirement = researches.get(req);
				if(requirement != null) {
					research.getRequirement().addRequirement(requirement);
				}
				System.out.println(research + " require  " + requirement);
			}
		}
	}
}
