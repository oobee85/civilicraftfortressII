package world;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;

public class World {
	
	public static final double SNOW_LEVEL = 0.75;
	public static final int DAY_DURATION = 500;
	public static final int NIGHT_DURATION = 350;
	public static final int TRANSITION_PERIOD = 100;
	
	private LinkedList<Tile> tileList;
	private LinkedList<Tile> tileListRandom;
	private Tile[][] tiles;
	
	private int width;
	private int height;
	
	public LinkedList<Plant> plantsLand = new LinkedList<Plant>();
	public LinkedList<Plant> plantsAquatic = new LinkedList<Plant>();
	public LinkedList<Unit> units = new LinkedList<Unit>();
	public LinkedList<Building> buildings = new LinkedList<Building>();
	
	public static LinkedList<GroundModifier> groundModifiers = new LinkedList<>();
	
	private double bushRarity = 0.005;
	private double waterPlantRarity = 0.05;
	private double forestDensity = 0.3;

	public TileLoc volcano;
	
	public World() {
		tileList = new LinkedList<>();
		tileListRandom = new LinkedList<>();
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public LinkedList<Tile> getTiles() {
		return tileList;
	}
	public LinkedList<Tile> getTilesRandomly() {
		Collections.shuffle(tileListRandom);
		return tileListRandom;
	}
	
	public Tile get(TileLoc loc) {
		if(loc.x < 0 || loc.x >= tiles.length || loc.y < 0 || loc.y >= tiles[0].length) {
			return null;
		}
		return tiles[loc.x][loc.y];
	}

	public void drought() {
		for(Tile tile : getTiles()) {
			tile.liquidAmount = 0;
		}
	}
	public void rain() {
		System.out.println("raining");
		for(Tile tile : getTiles()) {
			if(tile.getTerrain() != Terrain.SNOW && tile.getTerrain() != Terrain.ROCK) {
				continue;
			}
			if(tile.liquidType == LiquidType.WATER || tile.liquidType == LiquidType.DRY) {
				tile.liquidType = LiquidType.WATER;
				tile.liquidAmount += 0.001;
			}
		}
	}
	public void eruptVolcano() {
		System.out.println("eruption");
		this.get(volcano).liquidAmount += 500;
		
//		world[volcano].liquidType = LiquidType.WATER;
//		world[volcano].liquidAmount += 200;
	}
	public void tick() {
		updateGroundModifiers();
	}
	public void updateGroundModifiers() {
		LinkedList<GroundModifier> GroundModifiersNew = new LinkedList<GroundModifier>();
		synchronized(groundModifiers) {
			for(GroundModifier modifier : groundModifiers) {
				Tile tile = modifier.getTile();
				if(modifier.updateTime() == false) {
					if(tile.getPlant() != null) {
						tile.getPlant().takeDamage(modifier.getType().getDamage());
					}
					GroundModifiersNew.add(modifier);
				}else {
					tile.setModifier(null);
				}
			}
			groundModifiers = GroundModifiersNew;
		}
	}
	
	public void spawnOgre() {
		LinkedList<Tile> tiles = this.getTilesRandomly();
		Tile t = tiles.getFirst();
		for(Tile tile : tiles) {
			if(tile.getTerrain() == Terrain.ROCK) {
				t = tile;
				break;
			}
		}
		System.out.println("Ogre at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		Animal ogre = new Ogre(t, false);
		t.addUnit(ogre);
		Wildlife.addAnimal(ogre);
		ogre.setTile(t);
	}
	public void spawnLavaGolem() {
		LinkedList<Tile> tiles = this.getTilesRandomly();
		Tile t = tiles.getFirst();
		for(Tile tile : tiles) {
			if(tile.getTerrain() == Terrain.VOLCANO) {
				t = tile;
				break;
			}
		}
		System.out.println("Lava Golem at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		Animal lavaGolem = new LavaGolem(t, false);
		t.addUnit(lavaGolem);
		Wildlife.addAnimal(lavaGolem);
		lavaGolem.setTile(t);
	}
	public void spawnWerewolf() {
		LinkedList<Unit> wolves = new LinkedList<Unit>();
		for(Animal animal : Wildlife.getAnimals()) {
			if(animal.getType() == UnitType.WOLF) {
				wolves.add(animal);
			}
		}
		if(wolves.size() == 0) {
			return;
		}
		Unit wolf = wolves.get((int)(Math.random()*wolves.size()));
		Tile t = wolf.getTile();
		
		wolf.takeDamage(1000);
		System.out.println("Werewolf at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		Animal werewolf = new Werewolf(t, false);
		t.addUnit(werewolf);
		Wildlife.addAnimal(werewolf);
		werewolf.setTile(t);
		
	}
	
	public void spawnDragon() {
		LinkedList<Tile> tiles = this.getTilesRandomly();
		Tile t = tiles.getFirst();
		for(Tile tile : tiles) {
			if(tile.getTerrain() == Terrain.VOLCANO) {
				t = tile;
				break;
			}
		}
		Animal dragon = new Dragon(t, false);
		t.addUnit(dragon);
		Wildlife.addAnimal(dragon);
		dragon.setTile(t);
	}

	public void spawnEnt() {
		LinkedList<Tile> tiles = this.getTilesRandomly();
		Tile t = tiles.getFirst();
		for (Tile tile : tiles) {
			if (tile.getTerrain() == Terrain.GRASS) {
				t = tile;
				break;
			}
		}
		System.out.println("Ent at: " + t.getLocation().x + ", " + t.getLocation().y);
		Animal ent = new Ent(t, false);
		t.addUnit(ent);
		Wildlife.addAnimal(ent);
		ent.setTile(t);
	}
	public void spawnFlamelet() {
		Tile tile = this.getTilesRandomly().getFirst();
		Animal flamelet = new Flamelet(tile, false);
		tile.addUnit(flamelet);
		Wildlife.addAnimal(flamelet);
		flamelet.setTile(tile);
	}
	public void spawnWaterSpirit() {
		Tile tile = this.getTilesRandomly().getFirst();
		Animal spirit = new WaterSpirit(tile, false);
		tile.addUnit(spirit);
		Wildlife.addAnimal(spirit);
		spirit.setTile(tile);
	}
	
	public void meteorStrike() {
		Tile t = this.getTilesRandomly().getFirst();
		
		int radius = (int) (Math.random()*20 + 5);
		System.out.println("meteor at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		
		for(Tile tile : this.getTiles()) {
			
			int i =  tile.getLocation().x;
			int j =  tile.getLocation().y;
			int dx = i - t.getLocation().x;
			int dy = j - t.getLocation().y;
			double distanceFromCenter = Math.sqrt(dx*dx + dy*dy);
				
				
				if(distanceFromCenter < radius) {
					if(tile.getTerrain() != Terrain.ROCK && tile.getTerrain() != Terrain.SNOW && tile.getTerrain() != Terrain.VOLCANO) {
						tile.setTerrain(Terrain.BURNED_GROUND);
					}
					
					tile.liquidAmount = 0;
					GroundModifier fire = new GroundModifier(GroundModifierType.FIRE, tile, 10 + (int)(Math.random()*990));
					synchronized(groundModifiers) {
						groundModifiers.add(fire);
					}
					tile.setModifier(fire);
					if(tile.getHasBuilding() == true) {
						tile.getBuilding().takeDamage(10000);
					}
					for(Unit unit : tile.getUnits()) {
						unit.takeDamage(10000);
					}
					if(tile.getPlant() != null) {
						tile.getPlant().takeDamage(10000);
					}
					
					double height = tile.getHeight()+0.2 - (radius/2 - distanceFromCenter)/radius/4;
					if(distanceFromCenter > radius/2) {
						height = tile.getHeight()+0.2 - (distanceFromCenter - radius/2)/radius;
					}
//					double height = tile.getHeight()+0.2 - (distanceFromCenter - radius/2)/radius;
//					if(distanceFromCenter > radius/2) {
//						height = tile.getHeight()+0.2 - (radius/2 - distanceFromCenter)/radius/4;
//					}
					tile.setHeight(Math.max(height, tile.getHeight()));
				}
				
				
		}
		
		
	}
	public void updateTerrainChange(World world) {
		for(Tile tile : getTiles()) {
			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				
				if(tile.checkTerrain(Terrain.DIRT) || tile.checkTerrain(Terrain.GRASS)) {
					double chance = 0.001 * tile.liquidAmount * tile.liquidType.getDamage();
					if(Math.random() < chance) {
						tile.setTerrain(Terrain.SAND);
					}
				}
			}else if(tile.checkTerrain(Terrain.SAND) && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()){
				double chance = 0.001 * tile.liquidType.getDamage();
				if(Math.random() < chance) {
					tile.setTerrain(Terrain.GRASS);
				}
			}
			if(tile.checkTerrain(Terrain.BURNED_GROUND) && tile.liquidType != LiquidType.LAVA 
//					&& (tile.getModifier() != null && tile.getModifier().getType() == GroundModifierType.FIRE)
					) {
//				if(tile.getModifier() != null && tile.getModifier().getType() == GroundModifierType.FIRE) {
//					return;
//				}
				double chance = 0.05;
				if(Math.random() < chance) {
					tile.setTerrain(Terrain.DIRT);
				}
			}
			if(tile.checkTerrain(Terrain.DIRT)) {
				boolean adjacentGrass = false;
				boolean adjacentWater = false;
				for(Tile neighbor : Utils.getNeighbors(tile, world)) {
					if(neighbor.checkTerrain(Terrain.GRASS)) {
						adjacentGrass = true;
					}
					if(neighbor.liquidType == LiquidType.WATER) {
						adjacentWater = true;
					}
				}
				double threshold = 0;
				if(tile.liquidType == LiquidType.WATER) {
					threshold += 0.001;
				}
				if(adjacentGrass) {
					threshold += 0.005;
				}
				if(adjacentWater) {
					threshold += 0.005;
				}
				if(adjacentGrass && adjacentWater) {
					threshold += 0.05;
				}
				if(Math.random() < tile.liquidAmount*threshold) {
					tile.setTerrain(Terrain.GRASS);
				}
			}
		}
		
	}
	
	public void grow() {
		System.out.println(plantsLand.size() + " land plants and " + plantsAquatic.size() + " aquatic plants.");
		LinkedList<Plant> newAquatic = new LinkedList<>();
		LinkedList<Plant> newLand = new LinkedList<>();
		
		for(Tile tile : getTilesRandomly()) {
			
			if(tile.canPlant() == false) {
				continue;
			}
			
			if(tile.getPlant() != null && tile.getPlant().getPlantType() == PlantType.FOREST1 && tile.canPlant()) {
				for(Tile t : tile.getNeighbors()) {
					if(Math.random() < 0.01 && t.getPlant() == null && t != tile && t.canPlant()) {
						t.setHasPlant(new Plant(PlantType.FOREST1, t));
						newLand.add(t.getPlant());
					}
				}
			}
			
			if(tile.getPlant() != null) {
				continue;
			}
			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(Math.random() < 0.01) {
					Plant plant = new Plant(PlantType.CATTAIL, tile);
					tile.setHasPlant(plant);
					newAquatic.add(plant);
				}
			}
			
			if (Math.random() < 0.001) {
				tile.setHasPlant(new Plant(PlantType.BERRY, tile));
				newLand.add(tile.getPlant());
			}

		}
		for(Plant p : plantsAquatic) {
			newAquatic.add(p);
		}
		for(Plant p : plantsLand) {
			newLand.add(p);
		}
		plantsAquatic = newAquatic;
		plantsLand = newLand;
	}
	
	
	public void updateUnitDamage() {
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();

		for (Unit unit : units) {
			Tile tile = unit.getTile();
			if(unit.getTarget() != null) {
				unit.attack(unit.getTarget());
			}
			int tileDamage = tile.computeTileDamage(unit);
			if(tileDamage != 0) {
				unit.takeDamage(tileDamage);
			}
			if (unit.isDead() == true) {
				tile.removeUnit(unit);
			} else {
				unitsNew.add(unit);
			}
			
		}
		units = unitsNew;
	}
	
	public void updatePlantDamage() {
		LinkedList<Plant> plantsLandNew = new LinkedList<Plant>();
		for(Plant plant : plantsLand) {
			Tile tile = plant.getTile();
			if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(plant.isAquatic() == false || tile.liquidType != LiquidType.WATER) {
					double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
					int roundedDamage = (int) (damageTaken+1);
					if(roundedDamage >= 1) {
						plant.takeDamage(roundedDamage);
					}
				}
			}
			if(plant.isDead() == true) {
				tile.setHasPlant(null);
			}else {
				plantsLandNew.add(plant);
			}
		}	
		plantsLand = plantsLandNew;

		LinkedList<Plant> plantsAquaticNew = new LinkedList<Plant>();
		for (Plant plant : plantsAquatic) {
			Tile tile = plant.getTile();
			if (tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
				if (plant.isAquatic() || tile.liquidType != LiquidType.WATER) {
					double difInLiquids = tile.liquidType.getMinimumDamageAmount() - tile.liquidAmount;
					double damageTaken = difInLiquids * tile.liquidType.getDamage();
					int roundedDamage = (int) (damageTaken+1);
					if(roundedDamage >= 1) {
						plant.takeDamage(roundedDamage);
					}
				}
			}
			if (plant.isDead() == true) {
				tile.setHasPlant(null);
			} else {
				plantsAquaticNew.add(plant);
			}
		}
		plantsAquatic = plantsAquaticNew;
	}

	public void genPlants() {
		for(Tile tile : getTiles()) {
			//generates land plants
			if(tile.checkTerrain(Terrain.GRASS) && tile.getRoadType() == null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount() / 2 && Math.random() < bushRarity) {
				double o = Math.random();
				if(o < PlantType.BERRY.getRarity()) {
					Plant p = new Plant(PlantType.BERRY, tile);
					tile.setHasPlant(p);
					plantsLand.add(tile.getPlant());
				}
			}
			//tile.liquidType.WATER &&
			//generates water plants
			if( Math.random() < waterPlantRarity) {
				double o = Math.random();
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()  && o < PlantType.CATTAIL.getRarity()) {
					Plant p = new Plant(PlantType.CATTAIL, tile);
					tile.setHasPlant(p);
					plantsAquatic.add(tile.getPlant());
				}
			}
		}
	}


	public void makeForest() {
		int x0 = (int) (Math.random() * tiles.length);
		int y0 = (int) (Math.random() * tiles.length);
		double forestLength = Math.random()*70+1;
		double forestHeight = Math.random()*70+1;
		double forestLengthEdge = forestLength+30;
		double forestHeightEdge = forestHeight+30;
		for(Tile tile : getTiles()) {
			int dx = tile.getLocation().x - x0;
			int dy = tile.getLocation().y - y0;
			double forest = (dx*dx)/(forestLength*forestLength) + (dy*dy)/(forestHeight*forestHeight);
			double forestEdge = (dx*dx)/(forestLengthEdge*forestLengthEdge) + (dy*dy)/(forestHeightEdge*forestHeightEdge);
			
			if(tile.canPlant() == true && tile.getRoadType() == null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount() / 2) {
				if((forestEdge < 1 && Math.random()<forestDensity-0.2) 
						|| (forest < 1 && Math.random() < forestDensity)) {
					Plant plant = new Plant(PlantType.FOREST1, tile);
					tile.setHasPlant(plant);
					plantsLand.add(plant);
				}	
			}
		}
	}
	
	public List<Tile> getNeighbors(Tile tile) {
		int x = tile.getLocation().x;
		int y = tile.getLocation().y;
		int minX = Math.max(0, tile.getLocation().x - 1);
		int maxX = Math.min(this.getWidth()-1, tile.getLocation().x + 1);
		int minY = Math.max(0, tile.getLocation().y-1);
		int maxY = Math.min(this.getHeight()-1, tile.getLocation().y + 1);

		LinkedList<Tile> tiles = new LinkedList<>();
		for(int i = minX; i <= maxX; i++) {
			for(int j = minY; j <= maxY; j++) {
				if(i == x || j == y) {
					if(i != x || j != y) {
						if(this.get(new TileLoc(i, j)) != null) {
							tiles.add(this.get(new TileLoc(i, j)));
						}
					}
				}
			}
		}
		Collections.shuffle(tiles); 
		return tiles;
	}
	
	public void generateWorld(MapType mapType, int size) {
		width = size;
		height = size;
		tiles = new Tile[width][height];
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		double[][] heightMap = Generation.generateHeightMap(smoothingRadius, width, height);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = Tile.makeTile(new TileLoc(i, j), Terrain.DIRT);
				tileList.add(tiles[i][j]);
				tileListRandom.add(tiles[i][j]);
			}
		}
		
		for(Tile tile : getTiles()) {
			tile.setNeighbors(getNeighbors(tile));
		}
		
		volcano = Generation.makeVolcano(this, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		
		for(Tile tile : getTiles()) {
			tile.setHeight(heightMap[tile.getLocation().x][tile.getLocation().y]);
		}

		
		for(Tile tile : getTiles()) {
			if(tile.getTerrain() == Terrain.DIRT) {
				Terrain t;
				if (tile.getHeight() > SNOW_LEVEL) {
					t = Terrain.SNOW;
				}
				else if (tile.getHeight() > 0.6) {
					t = Terrain.ROCK;
				}
				else if (tile.getHeight() > 0.4) {
					t = Terrain.DIRT;
				}
				else {
					t = Terrain.GRASS;
				}
//				else {
//					t = Terrain.WATER;
//				}
				tile.setTerrain(t);
			}
		}
		
		
		int numTiles = width*height;
		Generation.makeLake(numTiles * 1.0/100, this);
		Generation.makeLake(numTiles * 1.0/200, this);
		Generation.makeLake(numTiles * 1.0/400, this);
		Generation.makeLake(numTiles * 1.0/800, this);
		System.out.println("Simulating water for 100 iterations");
		for(int i = 0; i < 100; i++) {
			Liquid.propogate(this);
		}

		Generation.genResources(this);
		this.genPlants();
		this.makeForest();
		Wildlife.generateWildLife(this);
	}

	public BufferedImage[] createTerrainImage() {
		double brighnessModifier = getDaylight();
		HashMap<Terrain, Color> terrainColors = new HashMap<>();
		for(Terrain t : Terrain.values()) {
			BufferedImage image = Utils.toBufferedImage(t.getImage(0));
			int sumr = 0;
			int sumg = 0;
			int sumb = 0;
			for(int i = 0; i < image.getWidth(); i++) {
				for(int j = 0; j < image.getHeight(); j++) {
					Color c = new Color(image.getRGB(i, j));
					sumr += c.getRed();
					sumg += c.getGreen();
					sumb += c.getBlue();
				}
			}
			int totalNumPixels = image.getWidth()*image.getHeight();
			Color average = new Color(sumr/totalNumPixels, sumg/totalNumPixels, sumb/totalNumPixels);
			terrainColors.put(t, average);
		}
		BufferedImage terrainImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage minimapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);

		Graphics minimapGraphics = minimapImage.getGraphics();
		Graphics terrainGraphics = terrainImage.getGraphics();
		for(Tile tile : this.getTiles()) {
			Color minimapColor = terrainColors.get(tile.getTerrain());
			Color terrainColor = terrainColors.get(tile.getTerrain());
			if(tile.getResource() != null) {
				terrainColor = tile.getResource().getType().getColor(0);
				minimapColor = tile.getResource().getType().getColor(0);
			}
			if(tile.getRoadType() != null) {
				terrainColor = Utils.roadColor;
				minimapColor = Utils.roadColor;
			}
			if(tile.liquidAmount > 0) {
				double alpha = Utils.getAlphaOfLiquid(tile.liquidAmount);
				minimapColor = Utils.blendColors(tile.liquidType.getColor(0), minimapColor, alpha);
				terrainColor = Utils.blendColors(tile.liquidType.getColor(0), terrainColor, alpha);
			}
			if(tile.getPlant() != null) {
				terrainColor = tile.getPlant().getColor(0);
				minimapColor = tile.getPlant().getColor(0);
			}
			if(tile.getHasBuilding()) {
				terrainColor = tile.getBuilding().getColor(0);
				minimapColor = tile.getBuilding().getColor(0);
			}
			if(tile.getIsTerritory()) {
				minimapColor = Utils.blendColors(Tile.TERRITORY_COLOR, minimapColor, 0.3);
				terrainColor = Utils.blendColors(Tile.TERRITORY_COLOR, terrainColor, 0.3);
			}
			
			double tilebrightness = tile.getBrightness();
			minimapColor = Utils.blendColors(minimapColor, Color.black, brighnessModifier + tilebrightness);
			terrainColor = Utils.blendColors(terrainColor, Color.black, brighnessModifier + tilebrightness);
			minimapImage.setRGB(tile.getLocation().x, tile.getLocation().y, minimapColor.getRGB());
			terrainImage.setRGB(tile.getLocation().x, tile.getLocation().y, terrainColor.getRGB());
		}
		minimapGraphics.dispose();
		terrainGraphics.dispose();
		
		BufferedImage heightMapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);
		for(Tile tile : getTiles() ) {
			int r = Math.max(Math.min((int)(255*tile.getHeight()), 255), 0);
			Color c = new Color(r, 0, 255-r);
			heightMapImage.setRGB(tile.getLocation().x, tile.getLocation().y, c.getRGB());
		}
		return new BufferedImage[] { terrainImage, minimapImage, heightMapImage};
	}

	public int ticksUntilDay() {
		int currentDayOffset = Game.ticks%(DAY_DURATION + NIGHT_DURATION);
		int skipAmount = (DAY_DURATION + NIGHT_DURATION - TRANSITION_PERIOD) - currentDayOffset;
		if(skipAmount < 0) {
			skipAmount += DAY_DURATION + NIGHT_DURATION;
		}
		return skipAmount;
	}
	
	public boolean isNightTime() {
		return getDaylight() < 0.2;
	}
	
	public double getDaylight() {
		if(Game.DISABLE_NIGHT) {
			return 1;
		}
		int currentDayOffset = (Game.ticks + TRANSITION_PERIOD)%(DAY_DURATION + NIGHT_DURATION);
		double ratio = 1;
		if(currentDayOffset < TRANSITION_PERIOD) {
			ratio = 0.5 + 0.5*currentDayOffset/TRANSITION_PERIOD;
		}
		else if(currentDayOffset < DAY_DURATION - TRANSITION_PERIOD) {
			ratio = 1;
		}
		else if(currentDayOffset < DAY_DURATION + TRANSITION_PERIOD) {
			ratio = 0.5 - 0.5*(currentDayOffset - DAY_DURATION)/TRANSITION_PERIOD;
		}
		else if(currentDayOffset < DAY_DURATION + NIGHT_DURATION - TRANSITION_PERIOD) {
			ratio = 0;
		}
		else {
			ratio = 0.5 - 0.5*(DAY_DURATION + NIGHT_DURATION - currentDayOffset)/TRANSITION_PERIOD;
		}
		return ratio;
	}
	
}
