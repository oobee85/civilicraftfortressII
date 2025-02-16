package game;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import game.actions.*;
import game.components.*;

import java.util.*;

import networking.message.*;
import networking.server.*;
import sounds.Sound;
import sounds.SoundEffect;
import sounds.SoundManager;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;
import world.liquid.*;

public class Game {

	public static final ArrayList<UnitType> unitTypeList = new ArrayList<>();
	public static final HashMap<String, UnitType> unitTypeMap = new HashMap<>();
	public static final ArrayList<BuildingType> buildingTypeList = new ArrayList<>();
	public static final HashMap<String, BuildingType> buildingTypeMap = new HashMap<>();
	public static final ArrayList<PlantType> plantTypeList = new ArrayList<>();
	public static final HashMap<String, PlantType> plantTypeMap = new HashMap<>();
	public static final ArrayList<ResearchType> researchTypeList = new ArrayList<>();
	public static final HashMap<String, ResearchType> researchTypeMap = new HashMap<>();

	public static boolean USE_BIDIRECTIONAL_A_STAR = true;
	public static boolean DISABLE_NIGHT = false;

	private GUIController guiController;
	public static final int howFarAwayStuffSpawn = 30;
	public World world;

	public Game(GUIController guiController) {
		this.guiController = guiController;
		Loader.doTargetingMappings();
	}

	public void saveToFile() {
		WorldInfo worldInfo = Utils.extractWorldInfo(world, true, true, false);
		Utils.saveToFile(worldInfo, "save1.civ", false);
	}

	public void meteorAndVolcanoEvents() {
		if (World.days > 10 && Math.random() < 0.00001) {
			meteorStrike();
		}
		if (world.volcano != null) {
			world.get(world.volcano).liquidType = LiquidType.LAVA;
			if (World.days >= 10 && Math.random() < 0.0001 && !Settings.DISABLE_VOLCANO_ERUPT) {
				eruptVolcano();
			}
		}
		world.updateVolcano();
	}

	public GUIController getGUIController() {
		return guiController;
	}

	public int getDays() {
		return World.days;
	}

	public int getNights() {
		return World.nights;
	}

	public void simulatedGameTick() {
		// Do the things that can be simulated client-side for smoother game play
		World.ticks++;
		if (World.ticks % 20 == 0) {
			updateTerritory();
		}
		if (World.ticks % 2 == 0) {
//			LiquidSimulation.propogate(world);
//			world.doAirSimulationStuff();
//			world.updateTerrainChange(false); // not this
			world.doProjectileUpdates(true);
		}

		// Remove dead things
		for (Unit unit : world.getUnits()) {
			if (unit.isDead()) {
				continue;
			}
			PlannedAction plan = unit.actionQueue.peek();
			if (plan != null) {
				if (plan.isDone(unit)) {
					unit.actionQueue.poll();
				}
			}

			unit.updateSimulatedCurrentPath();
		}
		world.clearDeadAndAddNewThings();

		buildingTick(true);
//		unitTick();

	}

	/**
	 * Do all the game events like unit movement, time passing, building things,
	 * growing, etc happens once every 100ms
	 */
	public void gameTick() {
		World.ticks++;

		if (World.ticks % 20 == 0) {
			updateTerritory();
			world.growMoss();
		}

		boolean everyOther = false;
		if (everyOther) {
			if (World.ticks % 2 == 0) {
				LiquidSimulation.propogate(world);
			} else if (World.ticks % 2 == 1) {
				world.doAirSimulationStuff();
				world.updateTerrainChange(false);
			}
		} else {
			LiquidSimulation.propogate(world);
			world.doAirSimulationStuff();
			world.updateTerrainChange(false);
		}

		meteorAndVolcanoEvents();

		// WeatherEvents have been deprecated
		// world.doWeatherUpdate();

		world.clearDeadAndAddNewThings();

		buildingTick(false);
		unitTick();
		world.doProjectileUpdates(false);
		if (World.ticks % Constants.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			world.updatePlantDamage();
		}

		if (World.ticks % (Constants.DAY_DURATION + Constants.NIGHT_DURATION) == 0) {
			if (!Settings.DISABLE_ENEMY_SPAWNS) {
//				dayEvents();
			}
			World.days++;
		}
		if ((World.ticks + Constants.DAY_DURATION) % (Constants.DAY_DURATION + Constants.NIGHT_DURATION) == 0) {
			if (!Settings.DISABLE_ENEMY_SPAWNS) {
				nightEvents();
			}
			World.nights++;
		}
	}

	private void makeAnimal(Tile tile, UnitType unitType, int number) {
		for (Tile t : Utils.getTilesInRadius(tile, world, Math.max(1, (int) (Math.sqrt(number)) - 1))) {
			if (number > 0) {
				world.spawnAnimal(unitType, t, world.getFaction(World.NO_FACTION_ID), null);
				number--;
			} else {
				break;
			}
		}
	}

	private Tile getTargetTileForSpawns(Faction faction) {
		LinkedList<Tile> factionTiles = new LinkedList<Tile>();
		for (Tile t : world.territory.keySet()) {
			if (t.getFaction() == faction) {
				factionTiles.add(t);
			}
		}
		if (!factionTiles.isEmpty()) {
			Tile targetTile = factionTiles.get((int) (Math.random() * factionTiles.size()));
			for (int attempt = 0; attempt < 1000; attempt++) {
				Tile tile = world.getRandomTile();
				if (tile.getFaction().id() != World.NO_FACTION_ID) {
					continue;
				}
				if (tile.distanceTo(targetTile) >= howFarAwayStuffSpawn) {
					continue;
				}
				return tile;
			}
		}
		return null;
	}

	private Tile getTargetTileForSpawns() {
		Tile targetTile = world.getRandomTile();

		for (Faction faction : world.getFactions()) {
			if (faction.getDifficulty() <= 0) {
				continue;
			}

			LinkedList<Tile> factionTiles = new LinkedList<Tile>();
			for (Tile t : world.territory.keySet()) {
				if (t.getFaction() == faction) {
					factionTiles.add(t);
				}
			}
			if (factionTiles.isEmpty() == false) {
				targetTile = factionTiles.get((int) (Math.random() * factionTiles.size()));
			}
		}
		return targetTile;
	}

	private void spawnScorpion() {
		Optional<Tile> potential = world.getTiles().stream()
				.filter(e -> e.getTerrain() == Terrain.SAND && e.getBuilding() == null && e.getPlant() == null
						&& e.computeTileDamage()[DamageType.WATER.ordinal()] == 0)
				.findFirst();
		if (potential.isPresent()) {
			summonUnit(potential.get(), unitTypeMap.get("SCORPION"), world.getFaction(World.NO_FACTION_ID));
		}
	}

	private void nightEvents() {
		spawnScorpion();
		for (Faction faction : world.getFactions()) {
			if (!faction.isPlayer()) {
				continue;
			}
			faction.recomputeInfluence();
			double influence = faction.getLastComputedInfluence();
			System.out.println("Faction " + faction.name() + " influence = " + influence);
			if (influence <= 3500) {
				continue;
			}
			Tile targetTile = getTargetTileForSpawns(faction);
			while (targetTile != null && influence > 20000) {
				UnitType hard = EnemySpawns.getRandomHardType();
				makeAnimal(targetTile, hard, 1);
				influence -= hard.getCombatStats().getHealth() * 5;
			}
			targetTile = getTargetTileForSpawns(faction);
			while (targetTile != null && influence > 12000) {
				UnitType medium = EnemySpawns.getRandomMediumType();
				makeAnimal(targetTile, medium, 1);
				influence -= medium.getCombatStats().getHealth() * 5;
			}
			targetTile = getTargetTileForSpawns(faction);
			while (targetTile != null && influence > 3500) {
				UnitType easy = EnemySpawns.getRandomEasyType();
				makeAnimal(targetTile, easy, 1);
				influence -= easy.getCombatStats().getHealth() * 5;
			}
		}
		return;
//		
//		double day = Math.sqrt(World.days);
//		Tile targetTile = getTargetTileForSpawns();
//		
//		//all the forced spawns
//		if(World.days % 6 == 0) {
//			world.spawnLavaGolem(targetTile);
//		}
//		if(World.days % 7 == 0) {
//			world.spawnIceGiant(targetTile);
//		}
//		if(World.days % 11 == 0) {
//			meteorStrike();
//		}
//		if(World.days % 8 == 0) {
//			world.spawnOgre(targetTile);
//		}
//		if(World.days % 12 == 0) {
//			world.spawnSkeletonArmy(targetTile);
//			System.out.println(day + " skeletons");
//		}
//		if(World.days % 20 == 0) {
//			spawnCyclops();
//			System.out.println("cyclops");
//		}
//		if(World.days % 15 == 0) {
//			world.spawnAnimal(Game.unitTypeMap.get("PARASITE"), world.getTilesRandomly().getFirst(), world.getFaction(World.NO_FACTION_ID), null);
//			System.out.println("parasite");
//		}
//		
//		
//		//random spawns
//		if(World.days >= 10 && Math.random() < 0.1) {
////			int number = (int)(Math.random() * Season.FREEZING_TEMPURATURE * day);
////			for(int i = 0; i < number; i++) {
//			world.spawnIceGiant(targetTile);
////			}
////			System.out.println(number + " ice giants");
//		}
//		if(World.days >= 10 && Math.random() < 0.1) {
////			int number = (int)(Math.random() * Season.FREEZING_TEMPURATURE * day/2);
////			number = 1;
////			for(int i = 0; i < number; i++) {
//			world.spawnStoneGolem(targetTile);
////			}
////			System.out.println(number + " stone golem");
//		}
//		if(World.days >= 10 && Math.random() < 0.2) {
////			int number = (int)(Math.random() * day/2);
////			for(int i = 0; i < number; i++) {
//			world.spawnRoc(targetTile);
////			}
////			System.out.println(number + " roc");
//		}
//		
//		if(World.days >= 7 && Math.random() < 0.2) {
////			int number = (int)(Math.random()*day/2);
////			for(int i = 0; i < number; i++) {
//			world.spawnEnt(targetTile);
////			}
////			System.out.println(number + " ents");
//			
//		}
//		if(World.days >= 8 && Math.random() < 0.1) {
////			int number = (int)(Math.random() * day);
////			for(int i = 0; i < number; i++) {
//			world.spawnTermite(targetTile);
////			}
////			System.out.println(number + " termite");
//		}
//		if(World.days >= 6 && Math.random() < 0.5) {
////			int number = (int)(Math.random() * day);
////			for(int i = 0; i < number; i++) {
//			world.spawnBomb(targetTile);
////			}
////			System.out.println(number + " bomb");
//		}
//
//		Tile spawnTile = targetTile;
//		for(Tile t: world.getTilesRandomly()) {
//			if(t.getLocation().distanceTo(targetTile.getLocation()) < howFarAwayStuffSpawn 
//					&& t.getFaction() == world.getFaction(World.NO_FACTION_ID)) {
//				spawnTile = t;
//			}
//		}
//		if(World.days >= 1 && Math.random() > 0.5) {
//			int number = (int)(Math.random()*day);
//			makeAnimal(spawnTile, Game.unitTypeMap.get("FLAMELET"), number);
//			System.out.println(number + " flamelets");
//		}
//		
//		if(Math.random() < 0.2) {
//			makeAnimal(spawnTile, Game.unitTypeMap.get("WATER_SPIRIT"), 4);
//			System.out.println(4 + " water spirits");
//		}
////		if(ticks >= 3000 && Math.random() < 0.0005) {
////			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
////		}
	}

//	private void nightEvents() {
//		double day = Math.sqrt(World.days);
//		Tile targetTile = getTargetTileForSpawns();
//		if(World.days >= 10) {
//			if(Math.random() > 0.5) {
//				world.spawnWerewolf(targetTile);
//			}
//		}
//		if(World.days >= 10) {
//			int number = (int)(Math.random() * day);
//			for(int i = 0; i < number; i++) {
//				world.spawnVampire(targetTile);
//			}
//			System.out.println(number + " vampire");
//		}
//	}
	public void addResources(Faction faction) {
		for (ItemType itemType : ItemType.values()) {
			faction.getInventory().addItem(itemType, 1000);
		}

	}

	public void eruptVolcano() {
		world.eruptVolcano();
	}

	public void meteorStrike() {
		world.meteorStrike();
	}

	public void shadowWordDeath(int num) {
		Faction undead = world.getFaction(World.UNDEAD_FACTION_ID);
		Faction cyclops = world.getFaction(World.CYCLOPS_FACTION_ID);
		int x = 15;
		int y = 50;
		int x2 = 45;
		for (int i = 0; i < 30; ++i) {
			Tile tile = world.get(new TileLoc(x, y + i));
			if (i % 2 == 0) {
				world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, undead, null);
			} else {
				world.spawnAnimal(Game.unitTypeMap.get("CATAPULT"), tile, undead, null);
			}
			tile = world.get(new TileLoc(x + 3, y + i));
			world.spawnAnimal(Game.unitTypeMap.get("SWORDSMAN"), tile, undead, null);
			tile = world.get(new TileLoc(x + 4, y + i));
			world.spawnAnimal(Game.unitTypeMap.get("SPEARMAN"), tile, undead, null);

			tile = world.get(new TileLoc(x2 + 5, y + i));
			Tile tile2 = world.get(new TileLoc(x2 + 4, y + i));
			if ((x2 + i) % 2 == 0) {
				world.spawnAnimal(Game.unitTypeMap.get("ARCHER"), tile, cyclops, null);
				world.spawnAnimal(Game.unitTypeMap.get("LONGBOWMAN"), tile2, cyclops, null);
			} else {
				world.spawnAnimal(Game.unitTypeMap.get("LONGBOWMAN"), tile, cyclops, null);
				world.spawnAnimal(Game.unitTypeMap.get("ARCHER"), tile2, cyclops, null);
			}
			tile = world.get(new TileLoc(x2 + 3, y + i));
			world.spawnAnimal(Game.unitTypeMap.get("SWORDSMAN"), tile, cyclops, null);

			tile = world.get(new TileLoc(x2 + 2, y + i));
			world.spawnAnimal(Game.unitTypeMap.get("HORSEARCHER"), tile, cyclops, null);
			tile = world.get(new TileLoc(x2, y + i));
			world.spawnAnimal(Game.unitTypeMap.get("KNIGHT"), tile, cyclops, null);
		}
//		Tile t = world.getTilesRandomly().getFirst();
//		for(int i = 0; i < num; i++) {
//			world.spawnOgre(t);
//			world.spawnDragon(t);
//			world.spawnWerewolf(t);
//			world.spawnEnt(t);
//			world.spawnLavaGolem(t);
//			world.spawnIceGiant(t);
//			world.spawnSkeletonArmy(t);
//			world.spawnStoneGolem(t);
//			world.spawnRoc(t);
//			world.spawnVampire(t);
//			spawnCyclops();
//		}
//		for(int i = 0; i <= num/10; i++) {
//			spawnEverything();
//		}
	}

	public void shadowWordPain(int num) {
		Faction undead = world.getFaction(World.UNDEAD_FACTION_ID);
		Faction cyclops = world.getFaction(World.CYCLOPS_FACTION_ID);
//		for(int i = 0; i < 40; i++) {
//			world.spawnDragon(null);
//		}
		for (int x = 0; x < world.getWidth(); x++) {

			Tile tile;
			tile = world.get(new TileLoc(x, 0));
			world.spawnAnimal(Game.unitTypeMap.get("STONE_GOLEM"), tile, undead, null);
			world.spawnAnimal(Game.unitTypeMap.get("TERMITE"), tile, undead, null);
			tile = world.get(new TileLoc(x, 1));
			world.spawnAnimal(Game.unitTypeMap.get("VAMPIRE"), tile, undead, null);
			tile = world.get(new TileLoc(x, 2));
			world.spawnAnimal(Game.unitTypeMap.get("ICE_GIANT"), tile, undead, null);
			for (int y = 0; y < 3; y++) {
				tile = world.get(new TileLoc(x, y + 3));
				world.spawnAnimal(Game.unitTypeMap.get("SKELETON"), tile, undead, null);
			}

			tile = world.get(new TileLoc(x, world.getHeight() - 2));
			world.spawnAnimal(Game.unitTypeMap.get("ROC"), tile, cyclops, null);
			tile = world.get(new TileLoc(x, 5));
			world.spawnAnimal(Game.unitTypeMap.get("HORSEARCHER"), tile, undead, null);
			tile = world.get(new TileLoc(x, 6));
			world.spawnAnimal(Game.unitTypeMap.get("KNIGHT"), tile, undead, null);
			tile = world.get(new TileLoc(x, 7));
			world.spawnAnimal(Game.unitTypeMap.get("CATAPULT"), tile, undead, null);
			tile = world.get(new TileLoc(x, 8));
			world.spawnAnimal(Game.unitTypeMap.get("LONGBOWMAN"), tile, undead, null);
			tile = world.get(new TileLoc(x, 9));
			world.spawnAnimal(Game.unitTypeMap.get("ARCHER"), tile, undead, null);
		}
		for (int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if (x > 4 && x < world.getWidth() - 3) {
				if (x % 6 == 0) {
					tile = world.get(new TileLoc(x, world.getHeight() - 16));
					spawnCyclopsFort(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		for (int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if (x > 4 && x < world.getWidth() - 3) {
				if (x % 6 == 3) {
					tile = world.get(new TileLoc(x, world.getHeight() - 10));
					spawnCyclopsFort(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		for (int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if (x > 4 && x < world.getWidth() - 3) {
				if (x % 6 == 0) {
					tile = world.get(new TileLoc(x, world.getHeight() - 5));
					spawnCyclopsFort(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		int y = world.getHeight() - 18;
		for (int x = 0; x < 5; x++) {
			summonBuilding(world.get(new TileLoc(x, y)), Game.buildingTypeMap.get("WALL_WOOD"), cyclops);
			summonBuilding(world.get(new TileLoc(world.getWidth() - x, y)), Game.buildingTypeMap.get("WALL_WOOD"),
					cyclops);
		}

	}

	public void spawnEverything() {
		for (UnitType type : Game.unitTypeList) {
			if (type.name() == "TWIG") {
				System.out.println("twig");
				continue;
			} else {
				world.spawnAnimal(type, world.getRandomTile(), world.getFaction(World.NO_FACTION_ID), null);
			}

		}
	}

	public void initializeWorld(int width, int height) {
		world = new World(width, height);
	}

	public void generateWorld(int width, int height, boolean easymode, List<PlayerInfo> players) {
		initializeWorld(width, height);

		Faction NO_FACTION = new Faction("NONE", false, false, false);
		world.addFaction(NO_FACTION);

		Faction CYCLOPS_FACTION = new Faction("CYCLOPS", false, true, false);
		CYCLOPS_FACTION.getInventory().addItem(ItemType.FOOD, 50);
		if (Settings.CINEMATIC) {
			CYCLOPS_FACTION.getInventory().addItem(ItemType.FOOD, 5000);
		}
		world.addFaction(CYCLOPS_FACTION);

		Faction UNDEAD_FACTION = new Faction("UNDEAD", false, false, false);
//		UNDEAD_FACTION.getInventory().addItem(ItemType.FOOD, 999999);
		world.addFaction(UNDEAD_FACTION);

		Faction BALROG_FACTION = new Faction("BALROG", false, false, false);
		world.addFaction(BALROG_FACTION);

		AttackUtils.world = world;
		world.generateWorld();
		makeRoads(easymode);
		world.clearDeadAndAddNewThings();
//		meteorStrike();
		makeStartingCastleAndUnits(easymode, players, world.worldRNG);
		spawnStartingEnemies();
	}

	public void spawnStartingEnemies() {
		spawnCyclopsFort();
		spawnUndead();
		makeDwarves(world);
		// ent grove
		// move dwarves here
		// orc town?
		// ogre swamp
		// dragon cave/volcano
	}

	private void makeDwarves(World world) {

		List<Tile> mines = new LinkedList<>();
		for (Tile tile : world.getTilesRandomly()) {
			if (tile.getResource() != null && tile.getResource().isOre() && tile.getResource().isRare()) {
				boolean tooclose = false;
				for (Tile mineTile : mines) {
					if (tile.distanceTo(mineTile) < 8) {
						tooclose = true;
						break;
					}
				}
				if (!tooclose) {
					Thing mine = world.summonBuilding(tile, Game.buildingTypeMap.get("QUARRY"),
							world.getFaction(World.NO_FACTION_ID));
					if (mine != null) {
						mines.add(tile);
					}
				}
			}
		}

		for (Tile mine : mines) {
			LinkedList<Tile> candidateTiles = new LinkedList<>();
			for (Tile tile : Utils.getTilesInRadius(mine, world, 7)) {
				if (tile != mine && tile.getResource() != null && tile.getResource().isOre()
						&& tile.getResource().isRare()) {
					candidateTiles.add(tile);
				}
			}
			for (int i = 0; i < 4 && !candidateTiles.isEmpty(); i++) {
				Animal dwarf = world.spawnAnimal(Game.unitTypeMap.get("DWARF"), mine,
						world.getFaction(World.NO_FACTION_ID), null);
				Tile tile = candidateTiles.remove((int) (Math.random() * candidateTiles.size()));
				dwarf.prequeuePlannedAction(PlannedAction.harvestTile(tile));
			}
		}
	}

	public void spawnUndead() {
		Tile highestTile = null;
		for (Tile t : world.getTiles()) {
			if (highestTile == null || t.getHeight() > highestTile.getHeight()) {
				highestTile = t;
			}
		}

		Thing necropolis = summonBuilding(highestTile, Game.buildingTypeMap.get("NECROPOLIS"),
				world.getFaction(World.UNDEAD_FACTION_ID));

		List<Tile> neighbors = Utils.getTilesInRadius(highestTile, world, 1);
		Collections.shuffle(neighbors);

		if (neighbors.isEmpty()) {
			System.err.println("");
		}
		Tile forWindmill = neighbors.remove(0);
		Thing windmill = summonBuilding(forWindmill, Game.buildingTypeMap.get("WINDMILL"),
				world.getFaction(World.UNDEAD_FACTION_ID));
		windmill.setInventory(world.getFaction(World.UNDEAD_FACTION_ID).getInventory());

//		for (TileLoc neighbor : neighbors) {
//			Animal skeleton = world.spawnAnimal(
//					Game.unitTypeMap.get("SKELETON"),
//					world.get(neighbor),
//					world.getFaction(World.CYCLOPS_FACTION_ID),
//					null);
//			skeleton.setPassiveAction(PlannedAction.GUARD);
//		}
	}

	public void spawnCyclopsFort() {
		for (Tile t : world.getTiles()) {
			if (t.getResource() == ResourceType.RUNITE) {
				spawnCyclopsFort(t);
				break;
			}
		}
	}

	private void spawnCyclopsFort(Tile tile) {
		Faction cyclopsFaction = world.getFaction(World.CYCLOPS_FACTION_ID);
		summonBuilding(world.get(new TileLoc(tile.getLocation().x(), tile.getLocation().y())),
				Game.buildingTypeMap.get("WATCHTOWER"), cyclopsFaction);
		Thing granary = summonBuilding(world.get(new TileLoc(tile.getLocation().x() - 1, tile.getLocation().y() - 1)),
				Game.buildingTypeMap.get("GRANARY"), cyclopsFaction);
		summonBuilding(world.get(new TileLoc(tile.getLocation().x() + 1, tile.getLocation().y() - 1)),
				Game.buildingTypeMap.get("BARRACKS"), cyclopsFaction);
		Thing windmill = summonBuilding(world.get(new TileLoc(tile.getLocation().x() + 1, tile.getLocation().y() + 1)),
				Game.buildingTypeMap.get("WINDMILL"), cyclopsFaction);
		summonBuilding(world.get(new TileLoc(tile.getLocation().x() - 1, tile.getLocation().y() + 1)),
				Game.buildingTypeMap.get("MINE"), cyclopsFaction);
		windmill.setInventory(cyclopsFaction.getInventory());
		granary.setInventory(cyclopsFaction.getInventory());
		// makes the walls
		for (int i = 0; i < 5; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if (i == 2) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall;
			wall = world.get(new TileLoc(tile.getLocation().x() + 3, tile.getLocation().y() - 2 + i));
			summonBuilding(wall, type, cyclopsFaction);
			wall = world.get(new TileLoc(tile.getLocation().x() - 3, tile.getLocation().y() - 2 + i));
			summonBuilding(wall, type, cyclopsFaction);
		}
		for (int i = 1; i < 6; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if (i == 3) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall;
			int yoffset = i;
			if (i > 3) {
				yoffset = (6 - yoffset);
			}
			yoffset += tile.getLocation().x() % 2;
			yoffset /= 2;

			wall = world.get(new TileLoc(tile.getLocation().x() - 3 + i, tile.getLocation().y() - 2 - yoffset));
			summonBuilding(wall, type, cyclopsFaction);

			yoffset = yoffset + (tile.getLocation().x() + i) % 2 - 2 - (tile.getLocation().x() % 2);
			wall = world.get(new TileLoc(tile.getLocation().x() - 3 + i, tile.getLocation().y() + 4 + yoffset));
			summonBuilding(wall, type, cyclopsFaction);
		}

		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				Tile temp = world.get(new TileLoc(tile.getLocation().x() + i, tile.getLocation().y() + j));
				if (temp == null) {
					continue;
				}
				Animal cyclops = world.spawnAnimal(Game.unitTypeMap.get("CYCLOPS"), temp, cyclopsFaction, null);
				// Makes the cyclopses attack anything in range of the cyclops fort home tile
				cyclops.queuePlannedAction(PlannedAction.guardTile(tile));
			}
		}
	}

	private void doBuildingCulture(Building b) {
		TileLoc loc = b.getTile().getLocation();
		double culture = b.getCulture();
		double area = culture * Building.CULTURE_AREA_MULTIPLIER;
		double radius = Math.sqrt(area);
		int r = (int) Math.ceil(radius);
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				TileLoc targetLoc = new TileLoc(loc.x() + i, loc.y() + j);
				Tile tile = world.get(targetLoc);
				if (tile == null) {
					continue;
				}
				int distance = loc.distanceTo(targetLoc);
//				double distanceFromCenter = Math.sqrt(i*i + j*j);
				if (distance > radius) {
					continue;
				}

				double influence = radius == 0 ? culture : culture * (radius - distance) / radius;

				if (!cultureInfluence.containsKey(tile)) {
					cultureInfluence.put(tile, 0.0);
				}
				double existingInfluence = cultureInfluence.get(tile);

				if (tile.getFaction() == b.getFaction()) {
					if (influence > existingInfluence) {
						cultureInfluence.put(tile, influence);
					}
				} else if (tile.getFaction() == world.getFaction(World.NO_FACTION_ID)) {
					tile.setFaction(b.getFaction());
					world.addToTerritory(tile);
					cultureInfluence.put(tile, influence);
				} else {
					if (influence > 2 * existingInfluence) {
						tile.setFaction(b.getFaction());
						world.addToTerritory(tile);
						cultureInfluence.put(tile, influence);
					}
				}
			}
		}
	}

	HashMap<Tile, Double> cultureInfluence;

	public void buildingTick(boolean simulated) {
		if (cultureInfluence == null) {
			cultureInfluence = new HashMap<>();
		}
		for (Building building : world.getBuildings()) {
			if (!simulated) {
				doBuildingCulture(building);
			}
			building.tick(world, simulated);
			
			if(building.getType().isCrafting() && building.readyToProduce()) {
				haveBuildingProduceItem(building);
				
				
			}
			

			if (building.isMoria() && building.isBuilt()) {
				generateMoria(building);

			}
		}
	}
	private void generateMoria(Building building) {
		Tile tile = building.getTile();
		world.spawnExplosionCircle(tile, 3, 500);
		Faction balrogFaction = world.getFaction(World.BALROG_FACTION_ID);

		Animal balrog = world.spawnAnimal(Game.unitTypeMap.get("BALROG"), tile, balrogFaction, null);
		balrog.queuePlannedAction(PlannedAction.guardTile(tile));
		
		tile.setFaction(balrogFaction);
		building.setMoria(false);
		world.summonBuilding(tile, buildingTypeMap.get("MORIA"), balrogFaction);
	}

	private void haveBuildingProduceItem(Building building) {
		if(building == null) {
			return;
		}
		
		final ItemType[] forgeItems = new ItemType[] { ItemType.BRONZE_BAR, ItemType.IRON_BAR, ItemType.GOLD_BAR,
				ItemType.MITHRIL_BAR, ItemType.ADAMANTITE_BAR, ItemType.RUNITE_BAR, ItemType.TITANIUM_BAR, ItemType.BRICK};
		
		final ItemType[] lumberItems = new ItemType[] { ItemType.SWORD, ItemType.BOW, ItemType.SHIELD,
				};
		
		List<ItemType> canCraft = new ArrayList<>();
		
		// if building is smithy, craft from forgeItems
		if(building.getType().isSmithy()) {
			// go through craftable bars and add affordable ones to a new list
			for (ItemType item : forgeItems) {
				if (building.getFaction().canAfford(item, 1) == true) {
					canCraft.add(item);
				}
			}
		}
		
		// if building is lumberYard, craft from lumberItems
		if(building.getType().isLumber()) {
			// go through craftable bars and add affordable ones to a new list
			for (ItemType item : lumberItems) {
				if (building.getFaction().canAfford(item, 1) == true) {
					canCraft.add(item);
				}
			}
		}
		
		// randomly select item from crafting list to craft
		if (!canCraft.isEmpty()) {
			ItemType crafting = canCraft.get((int) (Math.random() * canCraft.size()));
			building.getFaction().craftItem(crafting, 1);
			building.resetTimeToProduce();
			
			// decide what sound to play
			if(building.getType().isSmithy()) {
				Sound sound = new Sound(SoundEffect.SMITHYPRODUCE, building.getFaction(), building.getTile());
				SoundManager.theSoundQueue.add(sound);
			}
			if(building.getType().isLumber()) {
				Sound sound = new Sound(SoundEffect.SMITHYPRODUCE, building.getFaction(), building.getTile());
				SoundManager.theSoundQueue.add(sound);
			}
		}
		
	}
	
	private void haveSmithyProduceBar(Building building) {
		// if building is smithy // if smithy is ready to produce

		final ItemType[] craftableItems = new ItemType[] { ItemType.BRONZE_BAR, ItemType.IRON_BAR, ItemType.GOLD_BAR,
				ItemType.MITHRIL_BAR, ItemType.ADAMANTITE_BAR, ItemType.RUNITE_BAR, ItemType.TITANIUM_BAR, ItemType.BRICK};
		List<ItemType> canCraft = new ArrayList<>();

		// go through craftable bars and add affordable ones to a new list
		for (ItemType item : craftableItems) {
			if (building.getFaction().canAfford(item, 1) == true) {
				canCraft.add(item);
			}
		}
		if (!canCraft.isEmpty()) {
			ItemType crafting = canCraft.get((int) (Math.random() * canCraft.size()));
			building.getFaction().craftItem(crafting, 1);
			building.resetTimeToProduce();
			Sound sound = new Sound(SoundEffect.SMITHYPRODUCE, building.getFaction(), building.getTile());
			SoundManager.theSoundQueue.add(sound);
		}

	}

	public void flipTable() {
		float minheight = Integer.MAX_VALUE;
		float maxheight = Integer.MIN_VALUE;
		for (Tile tile : world.getTiles()) {
			minheight = Math.min(minheight, tile.getHeight());
			maxheight = Math.max(maxheight, tile.getHeight());
		}
		for (Tile tile : world.getTiles()) {
			tile.setHeight(maxheight - (tile.getHeight() - minheight));
		}
	}

	private double computeCost(Tile current, Tile next, Tile target) {
		double distanceCosts = 1;
		if (next.getRoad() == null) {
			double deltaHeight = 10000 * Math.abs(current.getHeight() - next.getHeight());
			distanceCosts += next.getTerrain().getRoadCost() + deltaHeight * deltaHeight
					+ 1000000 * next.liquidAmount * next.liquidType.getDamage();
		}
		return distanceCosts;
	}

	private class Path {
		double cost;
		LinkedList<Tile> tiles = new LinkedList<>();

		public Path() {
			cost = 0;
		}

		public Path(Path other) {
			tiles.addAll(other.tiles);
			this.cost = other.cost;
		}

		public void addTile(Tile tile, double addedCost) {
			tiles.add(tile);
			cost += addedCost;
		}

		public Tile getHead() {
			return tiles.getLast();
		}

		public Path clone() {
			return new Path(this);
		}

		public double getCost() {
			return cost;
		}

		public LinkedList<Tile> getTiles() {
			return tiles;
		}

		@Override
		public String toString() {
			String s = "";
			for (Tile t : tiles) {
				s += t.getLocation() + ", ";
			}
			return s;
		}
	}

	private void makeRoadBetween(Tile start, Tile target) {
		PriorityQueue<Path> search = new PriorityQueue<>((x, y) -> {
			if (y.getCost() < x.getCost()) {
				return 1;
			} else if (y.getCost() > x.getCost()) {
				return -1;
			} else {
				return 0;
			}
		});

		Path startingPath = new Path();
		startingPath.addTile(start, 0);
		search.add(startingPath);

		double bestCost = Double.MAX_VALUE;
		Path selectedPath = null;
		HashMap<Tile, Double> visited = new HashMap<>();
		visited.put(startingPath.getHead(), startingPath.getCost());

		int iterations = 0;
		while (!search.isEmpty()) {
			iterations++;
			Path currentPath = search.remove();
			Tile currentTile = currentPath.getHead();
			if (currentTile == target && currentPath.getCost() < bestCost) {
				selectedPath = currentPath;
				bestCost = currentPath.getCost();
				continue;
			}
			if (currentPath.getCost() > bestCost) {
				// if current cost is already more than the best cost
				continue;
			}
			List<Tile> neighbors = Utils.getNeighbors(currentTile, world);
			for (Tile neighbor : neighbors) {
				double cost = computeCost(currentTile, neighbor, target);
				Path p = currentPath.clone();
				p.addTile(neighbor, cost);
				if (visited.containsKey(neighbor)) {
					if (p.getCost() > visited.get(currentTile)) {
						// Already visited this tile at a lower cost
						continue;
					}
				}
				visited.put(neighbor, p.getCost());
				search.add(p);
			}
		}
		System.out.println("road iterations: " + iterations);

		if (selectedPath != null) {
			for (Tile t : selectedPath.getTiles()) {
				if (t != null) {
					Building road = new Building(Game.buildingTypeMap.get("STONE_ROAD"), t,
							world.getFaction(World.NO_FACTION_ID));
					road.setRemainingEffort(0);
					t.setRoad(road);
					world.addBuilding(road);
					if(t.getPlant() != null) {
						t.getPlant().setDead(true);
						t.getPlant().setRemoved(true);
					}
				}
			}
		}
	}

	private void makeRoads(boolean easymode) {
		double highest = -1000;
		Tile highestTile = null;
		double lowest = +1000;
		Tile lowestTile = null;
		for (Tile tile : world.getTiles()) {
			if (tile.getHeight() > highest) {
				highestTile = tile;
				highest = tile.getHeight();
			}
			if (tile.getHeight() < lowest) {
				lowestTile = tile;
				lowest = tile.getHeight();
			}
		}

		makeRoadBetween(world.get(new TileLoc(world.getWidth() - 1, 0)),
				world.get(new TileLoc(0, world.getHeight() - 1)));
		makeRoadBetween(world.get(new TileLoc(0, 0)),
				world.get(new TileLoc(world.getWidth() - 1, world.getHeight() - 1)));
		makeRoadBetween(highestTile, lowestTile);
	}

	private boolean isValidSpawnLocation(Tile spawnTile, int radius) {
		List<Tile> tiles = Utils.getTilesInRadius(spawnTile, world, radius);
		int numSafeTiles = 0;

		for (Tile t : tiles) {

			if (isValidSpawnTileForBuilding(t, Game.buildingTypeMap.get("CASTLE"))) {
				numSafeTiles++;
			}
		}
		if (numSafeTiles >= 6) {
			return true;
		}
		return false;
	}

	private boolean isValidSpawnTileForBuilding(Tile tile, BuildingType type) {
		return tile.canBuild() == true && !tile.hasBuilding()
				&& tile.liquidAmount <= tile.liquidType.getMinimumDamageAmount()
				&& tile.getLocation().distanceTo(world.volcano) > 30
				&& (tile.getTerrain() != Terrain.ROCK || type != Game.buildingTypeMap.get("CASTLE"))
				&& tile.getHeight() >= Constants.MAXHEIGHT * 0.05;
	}

	private List<Tile> chooseSpawnTiles(int numPlayers) {

		ArrayList<Tile> allTiles = new ArrayList<>(world.getTiles().size());
		allTiles.addAll(world.getTiles());
		Collections.sort(allTiles, (Tile a, Tile b) -> {
			if (a.getHeight() > b.getHeight()) {
				return 1;
			} else if (a.getHeight() < b.getHeight()) {
				return -1;
			}
			return 0;
		});

		List<Tile> validSpawns = new LinkedList<>();
		int edgeofmapbuffer = 7;
		for (int tileIndex = (int) (allTiles.size() * 0.5); tileIndex < (int) (allTiles.size() * 0.7); tileIndex++) {
			Tile t = allTiles.get(tileIndex);
			TileLoc l = t.getLocation();
			if (l.x() < edgeofmapbuffer || l.x() >= world.getWidth() - edgeofmapbuffer) {
				continue;
			}
			if (l.y() < edgeofmapbuffer || l.y() >= world.getHeight() - edgeofmapbuffer) {
				continue;
			}
			if (!isValidSpawnLocation(t, 5)) {
				continue;
			}
			validSpawns.add(t);
		}

		return validSpawns;
	}

	private void makeStartingCastleAndUnits(boolean easymode, List<PlayerInfo> players, Random rand) {
		List<Tile> validSpawnTiles = chooseSpawnTiles(players.size());
		if (validSpawnTiles.isEmpty()) {
			System.err.println("NO VALID SPAWN TILES");
			return;
		}
		List<Tile> chosenSpawnTiles = new LinkedList<>();
		for (PlayerInfo player : players) {
			Faction newFaction = new Faction(player.getName(), true, true, true, player.getColor());
			newFaction.getInventory().addItem(ItemType.WOOD, 100);
//			newFaction.getInventory().addItem(ItemType.STONE, 200);
			newFaction.getInventory().addItem(ItemType.FOOD, 200);
			world.addFaction(newFaction);

			LinkedList<Object> thingsToPlace = new LinkedList<>();
			thingsToPlace.add(Game.buildingTypeMap.get("CASTLE"));
			thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
			thingsToPlace.add(Game.plantTypeMap.get("BERRY"));
			thingsToPlace.add(Game.plantTypeMap.get("BERRY"));
			thingsToPlace.add(Game.plantTypeMap.get("TREE"));
			thingsToPlace.add(Game.plantTypeMap.get("TREE"));
			if (easymode || Settings.SPAWN_EXTRA) {
				addResources(newFaction);
				thingsToPlace.add(Game.buildingTypeMap.get("BARRACKS"));
				thingsToPlace.add(Game.buildingTypeMap.get("WORKSHOP"));
				thingsToPlace.add(Game.buildingTypeMap.get("SMITHY"));
				thingsToPlace.add(Game.buildingTypeMap.get("SAWMILL"));
				thingsToPlace.add(Game.plantTypeMap.get("BERRY"));
				thingsToPlace.add(Game.plantTypeMap.get("TREE"));
				thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
				thingsToPlace.add(Game.unitTypeMap.get("CARAVAN"));
				thingsToPlace.add(Game.unitTypeMap.get("WARRIOR"));
			}
			if (Settings.SPAWN_EXTRA) {
				thingsToPlace.add(Game.buildingTypeMap.get("WINDMILL"));
			}

			Tile spawnTile = null;

			if (chosenSpawnTiles.isEmpty()) {
				spawnTile = validSpawnTiles.get((int) (Math.random() * validSpawnTiles.size()));
			} else {
				int maximumDistance = 0;
				Tile bestTile = null;

				for (Tile tile : validSpawnTiles) {
					TileLoc l = tile.getLocation();
					// edge of map dist is doubled so that it doesnt matter as much
					int minDistance = 2 * Math.min(Math.min(l.x(), world.getWidth() - l.x()),
							Math.min(l.y(), world.getHeight() - l.y()));
					for (Tile chosen : chosenSpawnTiles) {
						int dist = chosen.distanceTo(tile);
						if (dist < minDistance) {
							minDistance = dist;
						}
					}

					if (minDistance > maximumDistance) {
						maximumDistance = minDistance;
						bestTile = tile;
					}
				}

				spawnTile = bestTile;
			}
			validSpawnTiles.remove(spawnTile);
			chosenSpawnTiles.add(spawnTile);

			System.out.println("Spawning " + player.getName() + " at " + spawnTile);

			HashSet<Tile> visited = new HashSet<>();
			LinkedList<Tile> tovisit = new LinkedList<>();

			tovisit.add(spawnTile);
			visited.add(spawnTile);

			while (!thingsToPlace.isEmpty()) {
				if (tovisit.size() > 50) {
					System.err.println(
							"FAILSAFE, could not find tiles to place " + thingsToPlace.size() + " more things");
					break;
				}
				Collections.shuffle(tovisit);
				if (tovisit.isEmpty()) {
					break;
				}
				Tile current = tovisit.removeFirst();
				Object thingType = thingsToPlace.getFirst();
				if (thingType instanceof BuildingType) {
					BuildingType type = (BuildingType) thingType;
					if (isValidSpawnTileForBuilding(current, type)) {
						summonBuilding(current, type, newFaction);
						thingType = null;
						if (current.getPlant() != null) {
							current.getPlant().setRemoved(true);
							current.getPlant().setDead(true);
						}

					}
				} else if (thingType instanceof PlantType) {
					if (current.getTerrain().isPlantable(current.getTerrain()) && current.getRoad() == null
							&& current.liquidAmount < current.liquidType.getMinimumDamageAmount() / 2
							&& current.hasBuilding() == false) {
						world.makePlantVein(current, (PlantType) thingType, 2, rand);
						thingType = null;
					}
				} else if (thingType instanceof UnitType) {
					if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
						summonUnit(current, (UnitType) thingType, newFaction);
						thingType = null;
					}
				}
				if (thingType == null) {
					tovisit.clear();
					visited.clear();
					visited.add(current);
					thingsToPlace.remove();
				}

				for (Tile neighbor : current.getNeighbors()) {
					if (!visited.contains(neighbor)) {
						visited.add(neighbor);
						tovisit.add(neighbor);
					}
				}
			}
			if (Settings.SPAWN_EXTRA) {
				spawnExtraStuff(newFaction);
			}
		}
	}

	private void spawnExtraStuff(Faction faction) {
		for (Unit u : faction.getUnits()) {
			if (u.hasInventory()) {
				u.getInventory().addItem(ItemType.values()[(int) (Math.random() * ItemType.values().length)],
						u.getInventory().getMaxStack() / 4);
			}
		}
		Building windmill = null;
		for (Building b : faction.getBuildings()) {
			if (b.hasInventory()) {
				b.getInventory().addItem(ItemType.values()[(int) (Math.random() * ItemType.values().length)],
						b.getInventory().getMaxStack() / 4);
			}
			if (b.getType() == buildingTypeMap.get("WINDMILL")) {
				windmill = b;
			}
		}
		if (windmill != null) {
			windmill.setRemainingEffort(0);
			windmill.setPlanned(true);
		}
	}

	private void updateTerritory() {
		for (Building building : world.getBuildings()) {
			building.updateCulture();
		}
	}

	public Thing summonUnit(Tile tile, UnitType unitType, Faction faction) {
		if (tile == null) {
			return null;
		}
		if (faction.isPlayer()) {
			Unit unit = new Unit(unitType, tile, faction);
			world.addUnit(unit);
			tile.addUnit(unit);
			unit.setCooldownToDoAction(0);
			return unit;
		} else {
			return world.spawnAnimal(unitType, tile, faction, null);
		}
	}

	public Thing summonBuilding(Tile tile, BuildingType buildingType, Faction faction) {
		return world.summonBuilding(tile, buildingType, faction);
	}

	public Thing summonPlant(Tile tile, PlantType plantType, Faction faction) {
		if (tile == null) {
			return null;
		}
		if (tile.getPlant() != null) {
			tile.getPlant().setRemoved(true);
			tile.getPlant().setDead(true);
		}
		Plant plant = new Plant(plantType, tile, faction);
		world.addPlant(plant);
		tile.setHasPlant(plant);
		return plant;

	}

	public Thing summonThing(Tile tile, Object thingType, Faction faction) {
		if (thingType instanceof UnitType) {
			return summonUnit(tile, (UnitType) thingType, faction);
		} else if (thingType instanceof BuildingType) {
			return summonBuilding(tile, (BuildingType) thingType, faction);
		} else if (thingType instanceof PlantType) {
			return summonPlant(tile, (PlantType) thingType, faction);
		} else {
			System.err.println("ERROR tried to summon invalid type: " + thingType);
			return null;
		}
	}

	public void spawnWeather(Tile center, int radius) {
		for (Tile t : Utils.getTilesInRadius(center, world, radius)) {
			t.liquidType = LiquidType.SNOW;
			t.liquidAmount += 5;
//			double distance = t.getLocation().distanceTo(center.getLocation());
//			float height = (float) (t.getHeight() + (radius - distance) / (radius) * 0.1);
//			t.setHeight(height);
		}
	}

	public void increasePressure(Tile center, int radius) {
		for (Tile t : Utils.getTilesInRadius(center, world, radius)) {
			t.getAir().decreaseVolumePerTile(500);
//			double distance = t.getLocation().distanceTo(center.getLocation());
//			float height = (float) (t.getHeight() + (radius - distance) / (radius) * 0.1);
//			t.setHeight(height);
		}
	}

	public void setTerritory(Tile center, int radius, Faction faction) {
		for (Tile t : Utils.getTilesInRadius(center, world, radius)) {
			t.setFaction(faction);
			world.addToTerritory(t);
		}
	}

	public void toggleAutoBuild(ConcurrentLinkedQueue<Thing> selectedThings) {
		for (Thing thing : selectedThings) {
			if (thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if (unit.isBuilder()) {
					unit.setAutoBuild(!unit.isAutoBuilding());
				}
			}
		}
	}

	public void explode(Thing thing) {
		if (thing == null) {
			return;
		}
		Sound sound = new Sound(SoundEffect.EXPLOSION, null, thing.getTile());
		SoundManager.theSoundQueue.add(sound);
		world.spawnExplosionCircle(thing.getTile(), 1, 1000000);
	}

	private void unitTick() {
		for (Unit unit : world.getUnits()) {
			unit.updateState();
			unit.planActions(world);
			unit.doMovement();
			unit.doActions(world);
			unit.doPassiveThings(world);
		}
	}

	private boolean canBuild(Unit unit, BuildingType bt, Tile tile) {
		if (!unit.getFaction().areRequirementsMet(bt)) {
			return false;
		}
		if (bt.isRoad() && tile.getRoad() != null) {
			return false;
		}
		if (!bt.isRoad() && tile.hasBuilding()) {
			return false;
		}
		if (!unit.getFaction().canAfford(bt.getCost())) {
			return false;
		}
		if (bt == Game.buildingTypeMap.get("IRRIGATION") && tile.canPlant() == false) {
			return false;
		}
		return true;

	}

	public boolean checkForAdjacentMines(Building building, boolean finishedBuilding) {
		Tile tile = building.getTile();
		int mineCount = 0;
		if (finishedBuilding == false) {
			mineCount++;
		}
		for (Tile adjacent : tile.getNeighbors()) {
			for (Tile t : adjacent.getNeighbors()) {
				if (t.getBuilding() != null && t.getBuilding().getType() == Game.buildingTypeMap.get("MINE")) {
					mineCount++;
				}
			}
			if (adjacent.getBuilding() != null
					&& adjacent.getBuilding().getType() == Game.buildingTypeMap.get("MINE")) {
				mineCount++;
			}
		}
		if (mineCount >= 19) {
			building.setMoria(true);
			return true;
		}
		return false;

	}

	public Building planBuilding(Unit unit, BuildingType bt, Tile tile) {
		if (canBuild(unit, bt, tile) == true) { // if the unit can build it
			unit.getFaction().payCost(bt.getCost());
			Building building = new Building(bt, tile, unit.getFaction());
			double totalEffort = bt.getBuildingEffort();
			if(tile.getPlant() != null) { // if tile has a plant on it, increase the time to build
				totalEffort += (tile.getPlant().getMaxHealth() / 4);
			}
			if(tile.isRoughTerrain()) { // if tile is sand, rock, increase cost to build by 10%
				totalEffort += bt.getBuildingEffort() *  0.1;
			}
			totalEffort += tile.getHeight() / 100; // increase cost by height
			totalEffort += tile.liquidAmount; // increase cost by liquid
			
			building.setRemainingEffort(totalEffort);
			building.setTotalEffort(totalEffort);
			
			world.addBuilding(building);
			building.setPlanned(true);
			// Start off planned buildings at 1% health
			building.setHealth(bt.getHealth() * 0.01);
			if (bt.isRoad()) {
				tile.setRoad(building);
			} else {
				tile.setBuilding(building);
			}

			if (checkForAdjacentMines(building, false) == true) {
				Sound sound = new Sound(SoundEffect.TOODEEP, null);
				SoundManager.theSoundQueue.add(sound);
			}
			return building;
		} else if (bt.isRoad() && tile.getRoad() != null) {
			if (bt == tile.getRoad().getType()) {
				return tile.getRoad();
			}
		} else if (!bt.isRoad() && tile.getBuilding() != null) {
			if (bt == tile.getBuilding().getType()) {
				return tile.getBuilding();
			}
		}

		return null;
	}

	public Color getBackgroundColor() {
		return new Color(0, 0, 0, (float) (1 - World.getDaylight()));
	}

	public void researchEverything(Faction faction) {
		faction.researchEverything();
		guiController.updateGUI();
	}
}
