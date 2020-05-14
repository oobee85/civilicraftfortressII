package world;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import game.Resource;
import game.ResourceType;
import game.Unit;
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
	public Tile[][] tiles;
	public double[][] heightMap;
	
	private int width;
	private int height;
	
	public LinkedList<Plant> plantsLand = new LinkedList<Plant>();
	public LinkedList<Plant> plantsAquatic = new LinkedList<Plant>();
	public LinkedList<Unit> units = new LinkedList<Unit>();

	private double bushRarity = 0.005;
	private double waterPlantRarity = 0.05;
	private double forestDensity = 0.3;

	public TileLoc volcano;
	
	public World() {
		tileList = new LinkedList<>();
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public LinkedList<Tile> getTiles() {
		Collections.shuffle(tileList);
		return tileList;
	}
	
	public Tile get(TileLoc loc) {
		if(loc.x < 0 || loc.x >= tiles.length || loc.y < 0 || loc.y >= tiles[0].length) {
			return null;
		}
		return tiles[loc.x][loc.y];
	}
	public double getHeight(TileLoc loc) {
		return heightMap[loc.x][loc.y];
	}

	public void drought() {
		for(Tile tile : getTiles()) {
			tile.liquidAmount = 0;
		}
	}
	public void rain() {
		System.out.println("raining");
		for(Tile tile : getTiles()) {
			if(tile.liquidType == LiquidType.WATER || tile.liquidType == LiquidType.DRY) {
				tile.liquidType = LiquidType.WATER;
				tile.liquidAmount += 0.005;
			}
		}
	}
	
	public void grow() {
		System.out.println("growing plants");
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles.length; j++) {
				Tile tile = tiles[i][j];
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount() 
						&& Math.random()< 0.01) {
					Plant plant = new Plant(PlantType.CATTAIL, tile);
					if(tile.getPlant() == null) {
						tile.setHasPlant(plant);
						plantsAquatic.add(plant);
					}
					
				}
				
				
			}
		}
	}
	
	
	public void updateUnitDamage() {
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();

		for (Unit unit : units) {

			Tile tile = unit.getTile();

			if (tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
					double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
					unit.takeDamage(damageTaken);
				}

			}

		

		for (Unit unit : units) {

			Tile tile = unit.getTile();
			if (unit.isDead() == true) {
				tile.setUnit(null);
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
				if(!plant.isAquatic() || tile.liquidType != LiquidType.WATER) {
					double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
					plant.takeDamage(damageTaken);
				}
				
			}
			
		}	
		
		for(Plant plant : plantsLand) {
			
			Tile tile = plant.getTile();
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
					plant.takeDamage(damageTaken);
				}

			}

		}

		for (Plant plant : plantsAquatic) {

			Tile tile = plant.getTile();
			if (plant.isDead() == true) {
				tile.setHasPlant(null);
			} else {
				plantsAquaticNew.add(plant);
			}
		}
		plantsAquatic = plantsAquaticNew;

	}

	public void genPlants() {
		
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles.length; j++) {
				
				//generates land plants
				if(tiles[i][j].checkTerrain(Terrain.GRASS) && tiles[i][j].getHasRoad()==false && Math.random() < bushRarity) {
					double o = Math.random();
					if(o < PlantType.BERRY.getRarity()) {
						Plant p = new Plant(PlantType.BERRY, tiles[i][j] );
						tiles[i][j].setHasPlant(p);
						plantsLand.add(tiles[i][j].getPlant());
					}
					
				}
				//generates water plants
				if(tiles[i][j].checkTerrain(Terrain.WATER) && Math.random() < waterPlantRarity) {
					double o = Math.random();
					if(o < PlantType.CATTAIL.getRarity()) {
						Plant p = new Plant(PlantType.CATTAIL, tiles[i][j] );
						tiles[i][j].setHasPlant(p);
						plantsAquatic.add(tiles[i][j].getPlant());
					}
					
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
		
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				int dx = i - x0;
				int dy = j - y0;
				
				double forest = (dx*dx)/(forestLength*forestLength) + (dy*dy)/(forestHeight*forestHeight);
				double forestEdge = (dx*dx)/(forestLengthEdge*forestLengthEdge) + (dy*dy)/(forestHeightEdge*forestHeightEdge);
				
				Tile tile = tiles[i][j];
					if(tile.canPlant()==true && tile.getHasRoad() == false) {
						
						
						if((forestEdge < 1 && Math.random()<forestDensity-0.2) 
								|| (forest < 1 && Math.random() < forestDensity)) {
							
							Plant plant = new Plant(PlantType.FOREST1, tiles[i][j]);
							tiles[i][j].setHasPlant(plant);
							plantsLand.add(plant);
							
						}	
					}
					
			}
		}
		
	}
	
	public void generateWorld(MapType mapType, int size) {
		width = size;
		height = size;
		tiles = new Tile[width][height];
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		heightMap = Generation.generateHeightMap(smoothingRadius, width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = Tile.makeTile(new TileLoc(i, j), Terrain.DIRT);
				tileList.add(tiles[i][j]);
			}
		}
		
//		mountain = Generation.makeMountain(tiles, heightMap);
		volcano = Generation.makeVolcano(tiles, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if(tiles[i][j].getTerrain() == Terrain.DIRT) {
					Terrain t;
					if (heightMap[i][j] > SNOW_LEVEL) {
						t = Terrain.SNOW;
					}
					else if (heightMap[i][j] > 0.6) {
						t = Terrain.ROCK;
					}
					else if (heightMap[i][j] > 0.4) {
						t = Terrain.DIRT;
					}
					else if (heightMap[i][j] > 0) {
						t = Terrain.GRASS;
					}
					else {
						t = Terrain.WATER;
					}
					tiles[i][j].setTerrain(t);
				}
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

		Generation.genOres(this);
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
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[0].length; j++) {
				Color minimapColor = terrainColors.get(tiles[i][j].getTerrain());
				Color terrainColor = terrainColors.get(tiles[i][j].getTerrain());
				if(tiles[i][j].getOre() != null) {
					terrainColor = tiles[i][j].getOre().getColor(0);
					minimapColor = tiles[i][j].getOre().getColor(0);
				}
				if(tiles[i][j].getHasRoad()) {
					terrainColor = Utils.roadColor;
					minimapColor = Utils.roadColor;
				}
				if(tiles[i][j].liquidAmount > 0) {
					double alpha = Utils.getAlphaOfLiquid(tiles[i][j].liquidAmount);
					minimapColor = Utils.blendColors(tiles[i][j].liquidType.getColor(0), minimapColor, alpha);
					terrainColor = Utils.blendColors(tiles[i][j].liquidType.getColor(0), terrainColor, alpha);
				}
				if(tiles[i][j].getPlant() != null) {
					terrainColor = tiles[i][j].getPlant().getColor(0);
					minimapColor = tiles[i][j].getPlant().getColor(0);
				}
				if(tiles[i][j].getHasStructure()) {
					terrainColor = tiles[i][j].getStructure().getColor(0);
					minimapColor = tiles[i][j].getStructure().getColor(0);
				}
				if(tiles[i][j].getIsTerritory()) {
					minimapColor = Utils.blendColors(Tile.TERRITORY_COLOR, minimapColor, 0.3);
					terrainColor = Utils.blendColors(Tile.TERRITORY_COLOR, terrainColor, 0.3);
				}
				
				double tilebrightness = tiles[i][j].getBrightness();
				minimapColor = Utils.blendColors(minimapColor, Color.black, brighnessModifier + tilebrightness);
				terrainColor = Utils.blendColors(terrainColor, Color.black, brighnessModifier + tilebrightness);
				minimapImage.setRGB(i, j, minimapColor.getRGB());
				terrainImage.setRGB(i, j, terrainColor.getRGB());
			}
		}
		minimapGraphics.dispose();
		terrainGraphics.dispose();
		
		BufferedImage heightMapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[0].length; j++) {
				int r = Math.max(Math.min((int)(255*heightMap[i][j]), 255), 0);
				Color c = new Color(r, 0, 255-r);
				heightMapImage.setRGB(i, j, c.getRGB());
			}
		}
		return new BufferedImage[] { terrainImage, minimapImage, heightMapImage};
	}
	
	public double getDaylight() {
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
