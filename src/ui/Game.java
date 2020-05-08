package ui;
import java.awt.*;
import java.awt.List;
import java.awt.image.*;
import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	private int ticks;
	private int turn;
	private Point worldSize;
	public Tile[][] world;
	private double[][] heightMap;
	private BufferedImage terrainImage;
	private BufferedImage minimapImage;
	private BufferedImage heightMapImage;
	ArrayList<Position> structureLoc = new ArrayList<Position>();
	ArrayList<Unit> selectUnits = new ArrayList<Unit>();
	LinkedList<Plant> plantsLand = new LinkedList<Plant>();
	ArrayList<Plant> plantsWater = new ArrayList<Plant>();
	
	public static int tileSize = 10;
	public boolean selectedUnit = false;
	private int money;
	private Position viewOffset;
	private Position hoveredTile;
	private Area hoveredArea;
	private BuildMode currentMode;
	private boolean showHeightMap;
	private int rotate = 0;
	private double snowEdgeRatio = 0.5;
	private double rockEdgeRatio = 0.7;
	private double oreRarity = 0.01;
	private double bushRarity = 0.005;
	private double waterPlantRarity = 0.05;
	private double forestDensity = 0.3;
	private int cultureDifBetweenExpanding = 100;
	
	private volatile int panelWidth;
	private volatile int panelHeight;
	private int fastModeTileSize = 10;
	
	private GUIController guiController;
	
	public static boolean DEBUG_DRAW = false;

	public Game(GUIController guiController) {
		this.guiController = guiController;
		money = 100;
		hoveredTile = new Position(-1,-1);
		hoveredArea = new Area(0,0,0,0);
		viewOffset = new Position(0, 0);
		currentMode = BuildMode.NOMODE;
		showHeightMap = false;
	}
	
	public void gameTick() {
		boolean changedTerrain = false;
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		ticks++;
		
		if(ticks%10 == 0) {
			updateTerritory();
			
		}
//		if(Math.random() < 0.01) {
//			for(int x = 0; x < world.length; x++) {
//				for(int y = 0; y < world[0].length; y++) {
//					world[x][y].liquidAmount *= 0.5;
//				}
//			}
//			makeLake(100);
//			changedTerrain = true;
//		}
		
		
		// rain event
		if(Math.random() < 0.001) {
//			rain();
		}
		
		if(ticks%1 == 0) {
			world[mountx][mounty].liquidAmount += 0.008;
			world[volcanox][volcanoy].liquidType = LiquidType.LAVA;
			world[volcanox][volcanoy].liquidAmount += 1;
			Liquid.propogate(world, heightMap);
			changedTerrain = true;
		}
		
		Wildlife.tick(world);
		updatePlantDamage();
		if(changedTerrain) {
			createTerrainImage();
		}
	}
	
	public void rain() {
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world[0].length; y++) {
				if(world[x][y].liquidType == LiquidType.WATER) {
					world[x][y].liquidAmount += 0.005;
				}
			}
		}
	}
	
	public void updatePlantDamage() {
		LinkedList<Plant> plantsLandNew = new LinkedList<Plant>();
		
		for(Plant plant : plantsLand) {
			
			Tile tile = plant.getTile();
			
			if(tile.liquidAmount > Liquid.MINIMUM_LIQUID_THRESHOLD) {
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
		
		
	}
	
	public void setViewSize(int width, int height) {
		panelWidth = width;
		panelHeight = height;
	}

	private double getRandomNormal(int tries) {
		double rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}
	private void genResources() {
		genOres();
		genPlants();
		makeForest();
	}

	public void generateWorld(MapType mapType, int size) {
		worldSize = new Point(size, size);
		world = new Tile[(int) worldSize.getX()][(int) worldSize.getY()];
		int smoothingRadius = (int) (Math.sqrt(size)/2);
		
		if(mapType == MapType.PANGEA) {
//			generateHeightMap(0.5, smoothingRadius, 0.05);
			generateHeightMap(0.5, smoothingRadius, 0);
		}
		else if(mapType == MapType.CONTINENTS) {
//			generateHeightMap(0.6, smoothingRadius, 0.35);
			generateHeightMap(0.6, smoothingRadius, 0);
		}
		else if(mapType == MapType.ARCHIPELAGO) {
//			generateHeightMap(0.75, smoothingRadius, 0.60);
			generateHeightMap(0.75, smoothingRadius, 0);
		}
		int numTiles = size*size;
//		makeLake(numTiles * 2/100);
//		makeLake(numTiles * 1/100);
//		makeLake(numTiles * 1/200);
//		makeLake(numTiles * 1/400);

		
		makeRoad();
		genResources();
		
		createTerrainImage();
		
		Wildlife.generateWildLife(world);
	}
	
	public void flipTable() {
		for(int x = 0; x < heightMap.length; x++) {
			for(int y = 0; y < heightMap[0].length; y++) {
				heightMap[x][y] = Math.max(Math.min(1-heightMap[x][y], 1), 0);
			}
		}
		createTerrainImage();
	}
	
	private void generateHeightMap(double dirtLevel, int smoothingRadius, double waterLevel) {
		System.out.println("gen terr");
		LinkedList<double[][]> noises = new LinkedList<>();

		for (int octave = 2; octave <= world.length; octave *= 2) {
			double[][] noise1 = new double[octave][octave];
			for (int i = 0; i < noise1.length; i++) {
				for (int j = 0; j < noise1[0].length; j++) {
					noise1[i][j] = getRandomNormal(5);
				}
			}
			noises.add(noise1);
		}

		double[][] combinedNoise = new double[world.length][world[0].length];
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[0].length; j++) {
				double rand = 0;
				int divider = world.length;
				double multiplier = 1;
				for (double[][] noise : noises) {
					divider /= 2;
					multiplier /= 1.4;
					rand += multiplier * noise[i / divider][j / divider];
				}
				combinedNoise[i][j] = rand;
			}
		}
		
		heightMap = Utils.smoothingFilter(combinedNoise, smoothingRadius, 100);
		Utils.normalize(heightMap);
		
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[0].length; j++) {
				Position p = new Position(i, j);
				world[i][j] = new Tile(p, Terrain.DIRT);
			}
		}
		
		makeMountain();
		makeVolcano();
		heightMap = Utils.smoothingFilter(heightMap, 3, 9);
		
		
		// make ten bins to count how many tiles have which value from terrain gen
//		int[] bins = new int[10];
//		// if values range from: 0 to 1
//		// bin 0: 0-0.1
//		// bin 1: 0.1-0.2
//		// ..
//		// bin 9: 0.9-1
//		for (int i = 0; i < world.length; i++) {
//			for (int j = 0; j < world[0].length; j++) {
//				int bin = (int) ((bins.length-1) * heightMap[i][j]);
//				bins[bin]++;
//			}
//		}
//		int totalNumTiles = world.length*world[0].length;
//		int numGrassTilesSoFar = 0;
//		double cutoffThreshold = 0;
//		for(int bin = 0; bin < bins.length; bin++) {
//			numGrassTilesSoFar += bins[bin];
//			if(numGrassTilesSoFar >= totalNumTiles * percentageGrass) {
//				cutoffThreshold = (double)bin / bins.length;
//				break;
//			}
//		}
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[0].length; j++) {
				if(world[i][j].getTerrain() == Terrain.DIRT) {
					Position p = new Position(i, j);
					Terrain t;
					if (heightMap[i][j] > 0.95) {
						t = Terrain.SNOW;
					}
					else if (heightMap[i][j] > 0.85) {
						t = Terrain.ROCK;
					}
					else if (heightMap[i][j] > dirtLevel) {
						t = Terrain.DIRT;
					}
					else if (heightMap[i][j] > waterLevel) {
						t = Terrain.GRASS;
					}
					else {
						t = Terrain.WATER;
					}
					Tile tile = new Tile(p, t);
					world[i][j] = tile;
				}
			}
		}
	}
	
	private void createTerrainImage() {
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
		BufferedImage terrainImage = new BufferedImage(world.length, world[0].length, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage minimapImage = new BufferedImage(world.length, world[0].length, BufferedImage.TYPE_3BYTE_BGR);

		Graphics minimapGraphics = minimapImage.getGraphics();
		Graphics terrainGraphics = terrainImage.getGraphics();
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[0].length; j++) {
				minimapImage.setRGB(i, j, terrainColors.get(world[i][j].getTerrain()).getRGB());
				terrainImage.setRGB(i, j, terrainColors.get(world[i][j].getTerrain()).getRGB());
				
				if(world[i][j].liquidAmount > 0) {
					float alpha = Utils.getAlphaOfLiquid(world[i][j].liquidAmount);
					Color newColor = Utils.blendColors(world[i][j].liquidType.getColor(), new Color(minimapImage.getRGB(i, j)), alpha);
					minimapImage.setRGB(i, j, newColor.getRGB());
					newColor = Utils.blendColors(world[i][j].liquidType.getColor(), new Color(terrainImage.getRGB(i, j)), alpha);
					terrainImage.setRGB(i, j, newColor.getRGB());
//				if(world[i][j].liquidType == LiquidType.WATER) {
//					minimapImage.setRGB(i, j, Color.red.getRGB());
//					terrainImage.setRGB(i, j, Color.red.getRGB());
//				}
					
					
				}
				
//				else if(world[i][j].liquidAmount < 0){
//					minimapImage.setRGB(i, j, Color.red.getRGB());
//					terrainImage.setRGB(i, j, Color.red.getRGB());
//				}
//				else {
//					minimapImage.setRGB(i, j, Color.green.getRGB());
//					terrainImage.setRGB(i, j, Color.green.getRGB());
//				}
				
			}
		}
		minimapGraphics.dispose();
		terrainGraphics.dispose();
		
		BufferedImage heightMapImage = new BufferedImage(world.length, world[0].length, BufferedImage.TYPE_3BYTE_BGR);
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[0].length; j++) {
				int r = Math.max(Math.min((int)(255*heightMap[i][j]), 255), 0);
				Color c = new Color(r, 0, 255-r);
				heightMapImage.setRGB(i, j, c.getRGB());
			}
		}
		this.terrainImage = terrainImage;
		this.minimapImage = minimapImage;
		this.heightMapImage = heightMapImage;
	}
	
	private void genPlants() {
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world.length; j++) {
				
				//generates land plants
				if(world[i][j].checkTerrain(Terrain.GRASS) && world[i][j].getHasRoad()==false && Math.random() < bushRarity) {
					double o = Math.random();
					if(o < PlantType.BERRY.getRarity()) {
						Plant p = new Plant(PlantType.BERRY, world[i][j] );
						world[i][j].setHasPlant(p);
						plantsLand.add(world[i][j].getPlant());
					}
					
				}
				//generates water plants
				if(world[i][j].checkTerrain(Terrain.WATER) && Math.random() < waterPlantRarity) {
					double o = Math.random();
					if(o < PlantType.CATTAIL.getRarity()) {
						Plant p = new Plant(PlantType.CATTAIL, world[i][j] );
						world[i][j].setHasPlant(p);
						plantsWater.add(world[i][j].getPlant());
					}
					
				}
				
				
			}
		}
		
		
	}
	
	private void genOres() {
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world.length; j++) {
				
				//ore generation on rock
				if(world[i][j].checkTerrain(Terrain.ROCK) && Math.random() < oreRarity) {
					double o = Math.random();
					if(o < Ore.ORE_GOLD.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_GOLD);
					}else if(o < Ore.ORE_SILVER.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_SILVER);
					}else if(o < Ore.ORE_COPPER.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_COPPER);
					}else if(o < Ore.ORE_IRON.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_IRON);
					}
				}
				
				//ore generation on volcano
				if(world[i][j].checkTerrain(Terrain.VOLCANO) && Math.random() < oreRarity +0.2) {
					double o = Math.random();
					if(o < Ore.ORE_GOLD.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_GOLD);
					}else if(o < Ore.ORE_SILVER.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_SILVER);
					}else if(o < Ore.ORE_COPPER.getRarity()) {
						world[i][j].setHasOre(Ore.ORE_COPPER);
					}
				}
				
				
			}
		}
		
		
	}
	
	int volcanox;
	int volcanoy;
	private void makeVolcano() {
		int x = (int) (Math.random() * world.length);
		int y = (int) (Math.random() * world.length);
		volcanox = x;
		volcanoy = y;
		
		double lavaRadius = 2.5;
		double volcanoRadius = 9;
		double mountainRadius = 20;
		double mountainEdgeRadius = 23;
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[i].length; j++) {
				int dx = i - x;
				int dy = j - y;
				double distanceFromCenter = Math.sqrt(dx*dx + dy*dy);
				Position p = new Position(i, j);
				if(distanceFromCenter < mountainEdgeRadius) {
					
					double height = 1 - (volcanoRadius - distanceFromCenter)/volcanoRadius/2;
					if(distanceFromCenter > volcanoRadius) {
						height = 1 - (distanceFromCenter - volcanoRadius)/mountainEdgeRadius*2;
					}
					heightMap[i][j] = Math.max(height, heightMap[i][j]);
					
					if(distanceFromCenter < lavaRadius) {
						world[i][j] = new Tile(p, Terrain.LAVA);
						world[i][j].liquidType = LiquidType.LAVA;
						world[i][j].liquidAmount = 10;
					}else if(distanceFromCenter < volcanoRadius) {
						world[i][j] = new Tile(p, Terrain.VOLCANO);
					}else if(distanceFromCenter < mountainRadius && world[i][j].checkTerrain(Terrain.SNOW) == false) {
						world[i][j] = new Tile(p, Terrain.ROCK);
					}else if(distanceFromCenter < mountainEdgeRadius && Math.random()<rockEdgeRatio) {
						world[i][j] = new Tile(p, Terrain.ROCK);
					}
				}
				
			}
		}
		
		
	}
	private void makeLake(int volume) {
		
		// Fill tiles until volume reached
		PriorityQueue<Position> queue = new PriorityQueue<Position>((p1, p2) -> {
			return heightMap[p1.getIntX()][p1.getIntY()] - heightMap[p2.getIntX()][p2.getIntY()] > 0 ? 1 : -1;
		});
		boolean[][] visited = new boolean[world.length][world[0].length];
		queue.add(new Position((int) (Math.random() * world.length), (int) (Math.random() * world.length)));
		while(!queue.isEmpty() && volume > 0) {
			Position next = queue.poll();
			int i = next.getIntX();
			int j = next.getIntY();
			world[i][j].liquidAmount += volume/5;
//			if(!world[i][j].checkTerrain(Terrain.WATER)) {
//				world[i][j] = new Tile(next, Terrain.WATER);
//				volume--;
//			}
//			// Add adjacent tiles to the queue
//			if(i > 0 && !visited[i-1][j]) {
//				queue.add(new Position(i-1, j));
//				visited[i-1][j] = true;
//			}
//			if(j > 0 && !visited[i][j-1]) {
//				queue.add(new Position(i, j-1));
//				visited[i][j-1] = true;
//			}
//			if(i + 1 < world.length && !visited[i+1][j]) {
//				queue.add(new Position(i+1, j));
//				visited[i+1][j] = true;
//			}
//			if(j + 1 < world[0].length && !visited[i][j+1]) {
//				queue.add(new Position(i, j+1));
//				visited[i][j+1] = true;
//			}
		}
	}
	private void makeForest() {
		
		int x0 = (int) (Math.random() * world.length);
		int y0 = (int) (Math.random() * world.length);
		
		double forestLength = Math.random()*70+1;
		double forestHeight = Math.random()*70+1;
		double forestLengthEdge = forestLength+30;
		double forestHeightEdge = forestHeight+30;
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[i].length; j++) {
				int dx = i - x0;
				int dy = j - y0;
				
				double forest = (dx*dx)/(forestLength*forestLength) + (dy*dy)/(forestHeight*forestHeight);
				double forestEdge = (dx*dx)/(forestLengthEdge*forestLengthEdge) + (dy*dy)/(forestHeightEdge*forestHeightEdge);
				
					if(world[i][j].canPlant()==true && world[i][j].getHasRoad() == false) {
						
						
						
						
						if((forestEdge < 1 && Math.random()<forestDensity-0.2) 
								|| (forest < 1 && Math.random() < forestDensity)) {
							
							Plant plant = new Plant(PlantType.FOREST1, world[i][j]);
							world[i][j].setHasPlant(plant);
							plantsLand.add(plant);
							
						}	
					}
					
			}
		}
		
	}
	int mountx;
	int mounty;
	private void makeMountain() {
		
		int x0 = (int) (Math.random() * world.length);
		int y0 = (int) (Math.random() * world.length);
		mountx = x0;
		mounty = y0;
		
		int mountainSize = 80 * world.length / 256;
		
		double mountLength = (0.1 + 0.9 * Math.random() ) *mountainSize;
		double mountHeight = (0.1 + 0.9 * Math.random() )*mountainSize;
		double mountLengthEdge = mountLength+3;
		double mountHeightEdge = mountHeight+3;
		
		double snowMountLength = mountLength/4;
		double snowMountHeight = mountHeight/4;
		double snowMountLengthEdge = mountLength/3;
		double snowMountHeightEdge = mountHeight/3;
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[i].length; j++) {
				int dx = i - x0;
				int dy = j - y0;
				double mountain = (dx*dx)/(mountLength*mountLength) + (dy*dy)/(mountHeight*mountHeight);
				double mountainEdge = (dx*dx)/(mountLengthEdge*mountLengthEdge) + (dy*dy)/(mountHeightEdge*mountHeightEdge);
				
				double snowMountain = (dx*dx)/(snowMountLength*snowMountLength) + (dy*dy)/(snowMountHeight*snowMountHeight);
				double snowMountainEdge = (dx*dx)/(snowMountLengthEdge*snowMountLengthEdge) + (dy*dy)/(snowMountHeightEdge*snowMountHeightEdge);

				double ratio = Math.sqrt(dx*dx/mountLength/mountLength + dy*dy/mountHeight/mountHeight);
				//double ratio = dist / Math.max(mountLength, mountHeight);
				Position p = new Position(i, j);
				if(snowMountainEdge < 1 && Math.random()<snowEdgeRatio) {
					world[i][j] = new Tile(p, Terrain.SNOW);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if (snowMountain < 1 ) {
					world[i][j] = new Tile(p, Terrain.SNOW);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if(mountainEdge < 1 && Math.random()<rockEdgeRatio) {
					world[i][j] = new Tile(p, Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if(mountain < 1) {
					world[i][j] = new Tile(p, Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}
				
				
				
				
			}
		}
		
		
	}

	private void makeRoad() {
		int topTile = (int) (Math.random() * world.length);
		int botTile = (int) (Math.random() * world.length);
		
//		topTile = 10;
//		botTile = world.length/6;
		
		Position start = new Position(topTile, 0);
		Position end = new Position(botTile, world.length-1);
		
		Position prevRoad = new Position(0,0);
		for(double t = 0; t < 1; t += 0.1 / world.length) {
			Position current = start.multiply(t).add(end.multiply(1-t));
			
			turnRoads(current,prevRoad);
			prevRoad = current;
			
		}
		makeCastle(start, end);
		
		
	}
	private void makeCastle(Position start, Position end) {
		double castleDistance = getRandomNormal(5);
		Position halfway = start.multiply(castleDistance).add(end.multiply(1-castleDistance));
		Structure struct = new Structure(StructureType.CASTLE, halfway);
		world[halfway.getIntX()][halfway.getIntY()].setStructure(struct);
		structureLoc.add(halfway);
		
	}
	
	private void turnRoads(Position current, Position prev) {
		if(current.getIntX()-1 < 0 || current.getIntX()+1 >= world.length) {
			return;
		}
		if(world[current.getIntX()][current.getIntY()].canBuild()==true) {
			
			// makes turns bot left -> top right
			if(world[current.getIntX()-1][current.getIntY()].canBuild()==true) {
				if (current.getIntX() - 1 == prev.getIntX() && current.getIntY() == prev.getIntY()) {
					world[current.getIntX()-1][current.getIntY()].setHasRoad(true, "left_down");
					world[current.getIntX()][current.getIntY()].setHasRoad(true, "right_up");
				} else if (current.getIntX() - 1 == prev.getIntX() && current.getIntY() + 1 == prev.getIntY()) {
					world[current.getIntX()-1][current.getIntY()].setHasRoad(true, "left_down");
					world[current.getIntX()][current.getIntY()].setHasRoad(true, "right_up");
				}	
			}
			

			// makes turns bot right -> top left
			if(world[current.getIntX()+1][current.getIntY()].canBuild()==true) {
				if (current.getIntX() + 1 == prev.getIntX() && current.getIntY() == prev.getIntY()) {
					world[current.getIntX() + 1][current.getIntY()].setHasRoad(true, "right_down");
					world[current.getIntX()][current.getIntY()].setHasRoad(true, "left_up");
				} else if (current.getIntX() + 1 == prev.getIntX() && current.getIntY() + 1 == prev.getIntY()) {
					world[current.getIntX() + 1][current.getIntY()].setHasRoad(true, "right_down");
					world[current.getIntX()][current.getIntY()].setHasRoad(true, "left_up");
				}
			}
			

			if (world[current.getIntX()][current.getIntY()].getHasRoad() == false) {
				world[current.getIntX()][current.getIntY()].setHasRoad(true, "top_down");
			}
			
			
//			Tile t =  world[current.getIntX()][current.getIntY()];
//			if(
////					world[current.getIntX()][current.getIntY()].getHasRoad() == true && 
//					t.getTerrain().isBridgeable(t.getTerrain()) == true) {
//				System.out.println("bridging");
//				world[current.getIntX()][current.getIntY()].setHasBuilding(Buildings.BRIDGE);
//			}

		}

	}

	public void draw(Graphics g) {
		
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, viewOffset.divide(tileSize).getIntX() - 2);
		int lowerY = Math.max(0, viewOffset.divide(tileSize).getIntY() - 2);
		int upperX = Math.min(world.length, lowerX + panelWidth/tileSize + 4);
		int upperY = Math.min(world[0].length, lowerY + panelHeight/tileSize + 4);
		
		if(Game.tileSize < fastModeTileSize) {
			if(showHeightMap) {
				g.drawImage(heightMapImage, 0, 0, Game.tileSize*world.length, Game.tileSize*world[0].length, null);
			}
			else {
				g.drawImage(terrainImage, 0, 0, Game.tileSize*world.length, Game.tileSize*world[0].length, null);
			}
		}
		else {
			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile t = world[i][j];
					
					if(t.getHasStructure() == true) {
						Position p = new Position(i,j);
						setTerritory(p);
					}
					if(i==hoveredTile.getIntX() && j==hoveredTile.getIntY()) {
						t.highlight(g);
					}
					if(hoveredArea.contains(i, j)) {
						t.highlight(g);
					}
					
					if(showHeightMap) {
						t.drawHeightMap(g, heightMap[i][j]);
					}
					else {
						t.draw(g, currentMode);
					}
					
				}
			}
			for(Animal animal : Wildlife.getAnimals()) {
				g.drawImage(animal.getType().getImage(), animal.getTile().getLocation().getIntX() * Game.tileSize, animal.getTile().getLocation().getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
			}
			if(DEBUG_DRAW) {
				if(Game.tileSize >= 36) {
					int numDigits = 2;
					int fontsize = Game.tileSize/4;
					fontsize = Math.min(fontsize, 13);
					Font font = new Font("Consolas", Font.PLAIN, fontsize);
					g.setFont(font);
					int stringWidth = g.getFontMetrics().stringWidth(String.format("??=%." + numDigits + "f", 0.0));
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile tile = world[i][j];

							int x = i * Game.tileSize + 2;
							int y = j * Game.tileSize + fontsize/2;
							int row = 1;
							
							g.setColor(Color.black);
							g.fillRect(x, y + 2, stringWidth, 2*fontsize);
							g.setColor(Color.green);
							
							g.drawString(String.format("L=%." + numDigits + "f", tile.liquidAmount), x, y + (row++)*fontsize);
							g.drawString(String.format("H=%." + numDigits + "f", heightMap[i][j]), x, y + (row++)*fontsize);
						}
					}
					for(Animal animal : Wildlife.getAnimals()) {
						Tile tile = animal.getTile();
						int x = tile.getLocation().getIntX() * Game.tileSize + 2;
						int y = tile.getLocation().getIntY() * Game.tileSize + fontsize/2 + 2 * fontsize;
						int row = 1;
						
						g.setColor(Color.black);
						g.fillRect(x, y + 2, stringWidth, 2*fontsize);
						g.setColor(Color.green);
						
						g.drawString(String.format("HP=%." + numDigits + "f", animal.getHealth()), x, y + (row++)*fontsize);
						g.drawString(String.format("EN=%." + numDigits + "f", animal.getEnergy()), x, y + (row++)*fontsize);
					}
					for(Plant plant : plantsLand) {
						Tile tile = plant.getTile();
						int x = tile.getLocation().getIntX() * Game.tileSize + 2;
						int y = tile.getLocation().getIntY() * Game.tileSize + fontsize/2 + 2 * fontsize;
						int row = 1;
						g.setColor(Color.black);
						g.fillRect(x, y + 2, stringWidth, 1*fontsize);
						g.setColor(Color.green);
						g.drawString(String.format("HP=%." + numDigits + "f", plant.getHealth()), x, y + (row++)*fontsize);
					}
				}
			}
		}
		
	}
	private void updateTerritory() {
		for (int i = 0; i < structureLoc.size(); i++) {
//			System.out.println("structureloc" +i);
			world[structureLoc.get(i).getIntX()][structureLoc.get(i).getIntY()].getStructure().updateCulture();
		}
	}
	private void updatePlant() {
		
		
	}
	private void setTerritory(Position p) {
		int c = world[p.getIntX()][p.getIntY()].getStructure().getCulture();
		int x = 0;
		while(!(c <= x) ) {
			x+=cultureDifBetweenExpanding;
			expandTerritory(x/cultureDifBetweenExpanding, p);	
		}
		
		
	}
	private void expandTerritory(int r, Position p) {
			
			for (int i=0-r; i < r; i++) {
				for (int j=0-r; j < r; j++) {
					
					double distanceFromCenter = Math.sqrt(i*i + j*j);
					if(distanceFromCenter < r) {
						if(r == 3) {
							if(i==1-r && j==1-r) {
								continue;	
							}	
							if(i==-1+r && j==1-r) {
								continue;	
							}
							if(i==1-r && j==-1+r) {
								continue;	
							}
							if(i==-1+r && j==-1+r) {
								continue;	
							}
						}
						if(p.getIntX()+i >= 0 && p.getIntX()+i < world.length && p.getIntY()+j >= 0 && p.getIntY()+j < world[0].length) {
							world[p.getIntX()+i][p.getIntY()+j].setTerritory(true);
						}
										
				}

			}
		}

			
	}

	public void rightClick(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx,my));
		if(currentMode == BuildMode.NOMODE) {
			if(world[tile.getIntX()][tile.getIntY()].getHasUnit() == true) {
				selectedUnit = true;
				System.out.println("selected unit");
			}
		}
//		if(selectedUnit == false) {
//			if(world[p1.getIntX()][p1.getIntY()] != null) {
//				Unit temp = world[p1.getIntX()][p1.getIntY()].getUnit();
//				world[p1.getIntX()][p1.getIntY()].setUnit(null);
//				world[p2.getIntX()][p2.getIntY()].setUnit(temp);
//			}
//			
//		}
		
	}
	
	

	public static void printPoint(Point p) {
		System.out.println("Point: (" + p.x + ", " + p.y + ")");
	}

	public Position getTileAtPixel(Position pixel) {
		Position tile = pixel.add(viewOffset).divide(tileSize);
		return tile;
	}
	public Position getPixelForTile(Position tile) {
		return tile.multiply(tileSize).subtract(viewOffset);
	}

	public void mouseOver(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
//		System.out.println("Mouse is on tile " + tile);
		hoveredTile = tile;
	}
	public void selectBox(int x1, int y1, int x2, int y2) {
		Position p1 = getTileAtPixel(new Position(x1,y1));
		Position p2 = getTileAtPixel(new Position(x2,y2));
		
			
		hoveredArea = new Area(p1.getIntX(),p1.getIntY(), p2.getIntX()+1, p2.getIntY()+1);
			
		selectTile();
		
	}
	
	private void selectTile() {
		for (int i = 0; i < hoveredArea.getIntX2()-hoveredArea.getIntX1(); i++) {
			for (int j = 0; j < hoveredArea.getIntY2()-hoveredArea.getIntY1(); j++) {
				world[hoveredArea.getIntX1()+i][hoveredArea.getIntY1()+j].setHighlight(true);
				if(world[hoveredArea.getIntX1()+i][hoveredArea.getIntY1()+j].getUnit() != null) {
					selectUnits.add(world[hoveredArea.getIntX1()+i][hoveredArea.getIntY1()+j].getUnit());
				}
			}
		}
		
	}
	public void mouseClick(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
		System.out.println(currentMode);
		
		
		
		if(currentMode == BuildMode.ROAD) {
			if(world[tile.getIntX()][tile.getIntY()].canBuild() == true) {
				world[tile.getIntX()][tile.getIntY()].buildRoad(true);
			}
		}
			
		if(currentMode == BuildMode.BARRACKS) {
			if(world[tile.getIntX()][tile.getIntY()].canBuild() == true) {
				Structure struct = new Structure(StructureType.BARRACKS, tile);
				world[tile.getIntX()][tile.getIntY()].setStructure(struct);
				structureLoc.add(tile);
			}
		} 
		
		if(currentMode == BuildMode.WALL) {
			if(world[tile.getIntX()][tile.getIntY()].canBuild() == true) {
				Building building = new Building(BuildingType.WALL_BRICK, tile);
				world[tile.getIntX()][tile.getIntY()].setBuilding(building);
			}
		}
		
		if(currentMode == BuildMode.MINE) {
			if(world[tile.getIntX()][tile.getIntY()].canBuild() == true || world[tile.getIntX()][tile.getIntY()].getHasOre() == true) {
				Building building = new Building(BuildingType.MINE, tile);
				world[tile.getIntX()][tile.getIntY()].setBuilding(building);
			}
		}
		
		if(currentMode == BuildMode.IRRIGATE) {
			if(world[tile.getIntX()][tile.getIntY()].canBuild() == true && world[tile.getIntX()][tile.getIntY()].canPlant() == true) {
				Building building = new Building(BuildingType.IRRIGATION, tile);
				world[tile.getIntX()][tile.getIntY()].setBuilding(building);
			}
		}
		
		
	}
	public void buildUnit(UnitType u) {
		Point po = new Point(structureLoc.get(0).getIntX(), structureLoc.get(0).getIntY());
		Position p = new Position(structureLoc.get(0).getIntX(), structureLoc.get(0).getIntY());
		Unit unit = new Unit(u , p);
		world[po.x][po.y].setUnit(unit);
		
	}
	
	public void doubleClick(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
		if(world[tile.getIntX()][tile.getIntY()].isStructure(StructureType.CASTLE) == true ) {
			exitCity();
		}
		
	}
	public void exitCity() {
		guiController.toggleCityView();
	}
	public void exitTile() {
		guiController.toggleTileView();
	}
	public void zoomView(int scroll, int mx, int my) {
		int newTileSize;
		if(scroll > 0) {
			newTileSize = (int) ((tileSize - 1) * 0.95);
		}
		else {
			newTileSize = (int) ((tileSize + 1) * 1.05);
		}
		if (newTileSize > 0) {
			Position tile = getTileAtPixel(new Position(mx, my));
			tileSize = newTileSize;
			Position focalPoint = tile.multiply(tileSize).subtract(viewOffset);
			viewOffset.x -= mx - focalPoint.x;
			viewOffset.y -= my - focalPoint.y;
//			System.out.println("Tilesize: "+tileSize);
		}
	}

	public void shiftView(int dx, int dy) {

		viewOffset.x += dx;
		viewOffset.y += dy;
//		System.out.println(viewOffset.x + "curview" + viewOffset.y);
	}
	public void moveViewTo(double ratiox, double ratioy) {
		Position tile = new Position(ratiox*world.length, ratioy*world[0].length);
		Position pixel = tile.multiply(tileSize).subtract(new Position(panelWidth/2, panelHeight/2));
		viewOffset = pixel;
	}


	public int getMoney() {
		return money;
	}
	public void setBuildMode(BuildMode b) {
		if(currentMode == b) {
			currentMode = BuildMode.NOMODE;
		}else {
			currentMode = b;
		}
		
	}
	public void rotateBlock() {
		
		if(rotate ==3) {
			rotate = 0;
			System.out.println("reset rotate");
		}else {
			System.out.println("rotating");
			rotate++;
		}
		
	}
	public int getTileSize() {
		return tileSize;
	}
	
	public BuildMode getMode() {
		return currentMode;
		
	}
	public void resetHoveredArea() {
		hoveredArea = new Area(0,0,0,0);
	}
	
	protected void drawMinimap(Graphics g, int x, int y, int w, int h) {
		if(showHeightMap) {
			g.drawImage(heightMapImage, x, y, w, h, null);
		}
		else {
			g.drawImage(minimapImage, x, y, w, h, null);
		}
		Position offsetTile = getTileAtPixel(viewOffset);
		int boxx = (int) (offsetTile.x * w / world.length / 2);
		int boxy = (int) (offsetTile.y * h / world[0].length / 2);
		int boxw = (int) (panelWidth/Game.tileSize * w / world.length);
		int boxh = (int) (panelHeight/Game.tileSize * h / world[0].length);
//		System.out.println(boxx);
		g.setColor(Color.yellow);
		g.drawRect(x + boxx, y + boxy, boxw, boxh);
	}
	protected void drawGame(Graphics g) {
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		Toolkit.getDefaultToolkit().sync();
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}
	

}
