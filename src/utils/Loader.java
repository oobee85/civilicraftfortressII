package utils;

import java.util.*;
import java.util.Map.Entry;

import org.json.*;

import game.*;
import game.ai.*;
import game.components.*;
import sounds.SoundEffect;
import sounds.SoundManager;
import world.PlantType;

public class Loader {
	
	
	
	public static void loadSounds() {
		System.out.println("Loading Sounds");
		for(SoundEffect sound : SoundEffect.values()) {
			SoundManager.loadSound(sound);
		}
	}
	
	private static HashMap<ItemType, Integer> loadItemTypeMap(JSONObject costObject) {
		HashMap<ItemType, Integer> map = new HashMap<>();
		for(String itemName : costObject.keySet()) {
			map.put(ItemType.valueOf(itemName), costObject.getInt(itemName));
		}
		return map;
	}
	
	public static Set<GameComponent> loadComponents(JSONObject obj) {
		Set<GameComponent> components = new HashSet<>();

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
		if (obj.has("builds")) {
			JSONArray buildingTypesArray = obj.getJSONArray("builds");
			String[] buildingTypeNames = new String[buildingTypesArray.length()];
			for (int i = 0; i < buildingTypesArray.length(); i++) {
				buildingTypeNames[i] = buildingTypesArray.getString(i);
			}
			components.add(new Builder(buildingTypeNames));
		}
		return components;
	}
	
	public static void loadPlantType(HashMap<String, PlantType> plantTypeMap, ArrayList<PlantType> plantTypeList) {
		System.out.println("Loading Plants");
		String plantTypeString = Utils.readFile("costs/PlantType.json");
		JSONObject obj = new JSONObject(plantTypeString);
		JSONArray arr = obj.getJSONArray("planttypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject plantTypeObject = arr.getJSONObject(i);

			String name = plantTypeObject.getString("name");
			String image = plantTypeObject.getString("image");
			String tiledImageFolder = plantTypeObject.has("tiledImages") ? plantTypeObject.getString("tiledImages") : null;
			int health = plantTypeObject.getInt("health");
			double rarity = plantTypeObject.getDouble("rarity");
//			String itemString = plantTypeObject.getString("harvestitem");
//			ItemType itemType = ItemType.valueOf(itemString);
			
			LinkedList<Item> harvestItems = new LinkedList<>();
			if(plantTypeObject.has("harvestitems")) {
				HashMap<ItemType, Integer> loot = loadItemTypeMap(plantTypeObject.getJSONObject("harvestitems"));
				for(Entry<ItemType, Integer> entry : loot.entrySet()) {
					harvestItems.add(new Item(entry.getValue(), entry.getKey()));
				}
			}

			HashSet<String> attributes = new HashSet<>();
			if(plantTypeObject.has("attributes")) {
				JSONArray attributelist = plantTypeObject.getJSONArray("attributes");
				for(int j = 0; j < attributelist.length(); j++) {
					attributes.add(attributelist.getString(j));
				}
			}

			int inventoryStackSize = 0;
			if(plantTypeObject.has("inventory")) {
				JSONObject inventoryProperties = plantTypeObject.getJSONObject("inventory");
				if(!inventoryProperties.has("maxstack")) {
					System.err.println("ERROR inventory does not have maxstack defined.");
					System.exit(0);
				}
				int maxStack = inventoryProperties.getInt("maxstack");
				inventoryStackSize = maxStack;
			}

			Set<GameComponent> components = loadComponents(plantTypeObject);
			PlantType plantType = new PlantType(name, image, tiledImageFolder, rarity, health, harvestItems, attributes, inventoryStackSize);
			plantType.getComponents().addAll(components);
			
			plantTypeMap.put(name, plantType);
			plantTypeList.add(plantType);
		}
		
	}
	
	public static void loadBuildingType(HashMap<String, BuildingType> buildingTypeMap, ArrayList<BuildingType> buildingTypeList) {
		System.out.println("Loading Buildings");
		String buildingTypeString = Utils.readFile("costs/BuildingType.json");
		JSONObject obj = new JSONObject(buildingTypeString);
		JSONArray arr = obj.getJSONArray("buildingtypes");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject buildingTypeObject = arr.getJSONObject(i);
			
			String name = buildingTypeObject.getString("name");
			String image = buildingTypeObject.getString("image");
			String tiledImageFolder = buildingTypeObject.has("tiledImages") ? buildingTypeObject.getString("tiledImages") : null;
			
			double culture = buildingTypeObject.getDouble("culture");
			int health = buildingTypeObject.getInt("health");
			int vision = buildingTypeObject.getInt("vision");
			double effort = buildingTypeObject.getDouble("effort_to_build");
			double effort_to_produce_item = buildingTypeObject.getDouble("effort_to_produce_item");
			double effort_to_produce_harvest = buildingTypeObject.getDouble("effort_to_produce_harvest");
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
			
			int inventoryStackSize = 0;
			if(buildingTypeObject.has("inventory")) {
				JSONObject inventoryProperties = buildingTypeObject.getJSONObject("inventory");
				if(!inventoryProperties.has("maxstack")) {
					System.err.println("ERROR inventory does not have maxstack defined.");
					System.exit(0);
				}
				int maxStack = inventoryProperties.getInt("maxstack");
				inventoryStackSize = maxStack;
			}

			Set<GameComponent> components = loadComponents(buildingTypeObject);
			BuildingType buildingType = new BuildingType(name, info, health, effort, image, tiledImageFolder,
					culture, vision, researchReq, cost, buildsunits, movespeed, attributes, inventoryStackSize,
					effort_to_produce_item, effort_to_produce_harvest);
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
		System.out.println("Loading Units");
		String unitTypeString = Utils.readFile("costs/UnitType.json");
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
			
			String researchReq = null;
			if(unitTypeObject.has("research")) {
				researchReq = unitTypeObject.getString("research");
			}
			
			HashMap<ItemType, Integer> cost = UnitType.EMPTY_COST;
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

			int inventoryStackSize = 0;
			if(unitTypeObject.has("inventory")) {
				JSONObject inventoryProperties = unitTypeObject.getJSONObject("inventory");
				if(!inventoryProperties.has("maxstack")) {
					System.err.println("ERROR inventory does not have maxstack defined.");
					System.exit(0);
				}
				int maxStack = inventoryProperties.getInt("maxstack");
				inventoryStackSize = maxStack;
			}
			Set<GameComponent> components = loadComponents(unitTypeObject);
			UnitType unitType = new UnitType(name, image, combatStats, attributes,
					researchReq, cost, items, targeting, attackStyles, inventoryStackSize);
			unitType.getComponents().addAll(components);
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
		System.out.println("Loading Researches");
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
			HashMap<BuildingType, Integer> buildingRequirement = new HashMap<>();
//			if(researchObject.has("buildingRequirement")) {
//				buildingRequirement = loadItemTypeMap(researchObject.getJSONObject("buildingRequirement"));
//			}
			LinkedList<String> reqs = new LinkedList<>();
			if(researchObject.has("requirements")) {
				JSONArray requirementArray = researchObject.getJSONArray("requirements");
				for(int j = 0; j < requirementArray.length(); j++) {
					String requirement = requirementArray.getString(j);
					reqs.add(requirement);
				}
			}
			ResearchType researchType = new ResearchType(researchName, imagePath, reqs, points, tier, cost, buildingRequirement);
			researchTypeMap.put(researchName, researchType);
			researchTypeList.add(researchType);
		}
	}
	
	public static void doMappings() {
		System.out.println("Doing Mappings");
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
			for (GameComponent c : type.getComponents()) {
				if (c instanceof Builder) {
					Builder builder = (Builder)c;
					Set<BuildingType> buildingTypes = builder.getBuildingTypeSet();
					for (String buildingTypeName : builder.getBuildingTypeNames()) {
						buildingTypes.add(Game.buildingTypeMap.get(buildingTypeName));
					}
				}
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
	
	
	public static void loadBuildOrders() {
		System.out.println("Loading Build Orders");
		
		String basicBuildOrder = Utils.readFile("buildorders/basic.json");
		JSONObject obj = new JSONObject(basicBuildOrder);
		JSONArray rolesArr = obj.getJSONArray("WORKER_ROLES");
		String[] WORKER_ROLES = new String[rolesArr.length()];
		for (int i = 0; i < rolesArr.length(); i++) {
			WORKER_ROLES[i] = rolesArr.getString(i);
		}
		System.out.print("WORKER_ROLES: ");
		for (String role : WORKER_ROLES) {
			System.out.print(role + ", ");
		}
		System.out.println();
		JSONArray arr = obj.getJSONArray("phases");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject phaseObject = arr.getJSONObject(i);
			int order = phaseObject.getInt("order");
			BuildOrderPhase phase = new BuildOrderPhase(order);
			
			if (phaseObject.has("units")) {
				JSONObject unitsObject = phaseObject.getJSONObject("units");
				JSONArray keys = unitsObject.names();
				for (int unitIndex = 0; unitIndex < keys.length(); unitIndex++) {
					String unitType = keys.getString(unitIndex);
					QuantityReq quantities = readQuantityReq(unitsObject.getJSONObject(unitType));
					phase.units.put(Game.unitTypeMap.get(unitType), quantities);
				}
			}
			
			if (phaseObject.has("buildings")) {
				JSONObject buildingsObject = phaseObject.getJSONObject("buildings");
				JSONArray keys = buildingsObject.names();
				for (int buildingIndex = 0; buildingIndex < keys.length(); buildingIndex++) {
					String buildingsType = keys.getString(buildingIndex);
					QuantityReq quantities = readQuantityReq(buildingsObject.getJSONObject(buildingsType));
					BuildingType type = Game.buildingTypeMap.get(buildingsType);
					if (type == null) {
						System.err.println("INVALID JSON");
					}
					phase.buildings.put(Game.buildingTypeMap.get(buildingsType), quantities);
				}
			}
			
			if (phaseObject.has("researchRequired")) {
				JSONArray researchArr = phaseObject.getJSONArray("researchRequired");
				for (int researchIndex = 0; researchIndex < researchArr.length(); researchIndex++) {
					String researchName = researchArr.getString(researchIndex);
					phase.requiredResearches.add(Game.researchTypeMap.get(researchName));
				}
			}
			
			if (phaseObject.has("researchOptional")) {
				JSONArray researchArr = phaseObject.getJSONArray("researchOptional");
				for (int researchIndex = 0; researchIndex < researchArr.length(); researchIndex++) {
					String researchName = researchArr.getString(researchIndex);
					phase.optionalResearches.add(Game.researchTypeMap.get(researchName));
				}
			}

			if (phaseObject.has("workerAssignments")) {
				JSONObject workerObj = phaseObject.getJSONObject("workerAssignments");
				JSONArray keys = workerObj.names();
				int total = 0;
				for (int taskIndex = 0; taskIndex < keys.length(); taskIndex++) {
					String taskType = keys.getString(taskIndex);
					double number = workerObj.getDouble(taskType);
					total += number;
					phase.workerAssignments.put(BuildOrderPhase.WorkerTask.valueOf(taskType), number);
				}
				for (Entry<BuildOrderPhase.WorkerTask, Double> entry : phase.workerAssignments.entrySet()) {
					phase.workerAssignments.put(entry.getKey(), entry.getValue() / total);
				}
			}
			
			phase.finalize();
//			System.out.println(phase);
			BuildOrderPhase.phases.add(phase);
		}
		BuildOrderPhase.phases.sort((a, b) -> {
			return a.order - b.order;
		});
	}

	private static QuantityReq readQuantityReq(JSONObject obj) {
		int min = 0;
		int enough = 0;
		int max = 0;
		if (obj.has("min")) {
			min = obj.getInt("min");
		}
		if (obj.has("enough")) {
			enough = obj.getInt("enough");
		}
		if (obj.has("max")) {
			max = obj.getInt("max");
		}
		else {
			max = Integer.MAX_VALUE;
		}
		return new QuantityReq(min, enough, max);
	}
}

