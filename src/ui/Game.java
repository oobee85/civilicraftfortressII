package ui;
import java.awt.*;
import java.util.List;
import java.awt.image.*;
import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	public static final int NUM_DEBUG_DIGITS = 3;
	public static int ticks;
	private int turn;
	private BufferedImage terrainImage;
	private BufferedImage minimapImage;
	private BufferedImage heightMapImage;
	ArrayList<Position> structureLoc = new ArrayList<Position>();
	ArrayList<Unit> selectUnits = new ArrayList<Unit>();
	LinkedList<Plant> plantsLand = new LinkedList<Plant>();
	LinkedList<Plant> plantsAquatic = new LinkedList<Plant>();
	LinkedList<Building> buildings = new LinkedList<Building>();
	LinkedList<Structure> structures = new LinkedList<Structure>();
	
	public static int tileSize = 10;
	public boolean selectedUnit = false;
	private int money;
	private Position viewOffset;
	private Position hoveredTile;
	private Area hoveredArea;
	private BuildMode currentMode;
	private boolean showHeightMap;
	private int rotate = 0;
	private double bushRarity = 0.005;
	private double waterPlantRarity = 0.05;
	private double forestDensity = 0.3;
	private int cultureDifBetweenExpanding = 100;
	
	private volatile int panelWidth;
	private volatile int panelHeight;
	private int fastModeTileSize = 10;
	
	private GUIController guiController;
	
	public static boolean DEBUG_DRAW = false;
	
	private TileLoc mountain;
	private TileLoc volcano;
	public Tile[][] world;
	public double[][] heightMap;
	private World world2;

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
		
		if(ticks%20 == 0) {
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
		
		
		if(ticks == 1) {
			rain();
		}
		// rain event
		if(Math.random() < 0.001) {
			rain();
		}
		if(Math.random() < 0.01) {
			grow();
		}
		if(ticks%1 == 0) {
			world[mountain.x][mountain.y].liquidAmount += 0.008;
			world[volcano.x][volcano.y].liquidType = LiquidType.LAVA;
			world[volcano.x][volcano.y].liquidAmount += .1;
			Liquid.propogate(world2);
			changedTerrain = true;
		}
		
		Wildlife.tick(world);
		updatePlantDamage();
		updateBuildingDamage();
		updateStructureDamage();
		if(changedTerrain) {
			createTerrainImage();
		}
	}
	public void grow() {
		System.out.println("growing plants");
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world.length; j++) {
				Tile tile = world[i][j];
				
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > Liquid.MINIMUM_LIQUID_THRESHOLD 
						&& Math.random()< 0.01) {
					Plant plant = new Plant(PlantType.CATTAIL, tile);
					tile.setHasPlant(plant);
					plantsAquatic.add(plant);
				}
				
				
			}
		}
		
	}
	
	public void rain() {
		System.out.println("raining");
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world[0].length; y++) {
				if(world[x][y].liquidType == LiquidType.WATER || world[x][y].liquidType == LiquidType.DRY) {
					world[x][y].liquidType = LiquidType.WATER;
					world[x][y].liquidAmount += 0.005;
				}
			}
		}
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

	public void updateBuildingDamage() {
		LinkedList<Building> buildingsNew = new LinkedList<Building>();

		for (Building building : buildings) {
			Tile tile = building.getTile();
			if (tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
				building.takeDamage(damageTaken);

			}
		}	
		
		for (Building building : buildings) {

			Tile tile = building.getTile();
			if (building.isDead() == true) {
				tile.setBuilding(null);
			} else {
				buildingsNew.add(building);
			}
		}
		buildings = buildingsNew;
		
	}
	public void updateStructureDamage() {
		LinkedList<Structure> structuresNew = new LinkedList<Structure>();

		for (Structure structure : structures) {
			Tile tile = structure.getTile();
			if (tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
				structure.takeDamage(damageTaken);

			}
		}	
		
		for (Structure structure : structures) {

			Tile tile = structure.getTile();
			if (structure.isDead() == true) {
				tile.setStructure(null);
			} else {
				structuresNew.add(structure);
			}
		}
		structures = structuresNew;
		
	}
	
	public void setViewSize(int width, int height) {
		panelWidth = width;
		panelHeight = height;
	}

	public void generateWorld(MapType mapType, int size) {
		int width = size;
		int height = size;
		world = new Tile[width][height];
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		heightMap = Generation.generateHeightMap(smoothingRadius, width, height);
		world2 = new World(world, heightMap);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				world[i][j] = new Tile(new TileLoc(i, j), Terrain.DIRT);
			}
		}
		
		mountain = Generation.makeMountain(world, heightMap);
		volcano = Generation.makeVolcano(world, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if(world[i][j].getTerrain() == Terrain.DIRT) {
					Terrain t;
					if (heightMap[i][j] > 0.95) {
						t = Terrain.SNOW;
					}
					else if (heightMap[i][j] > 0.85) {
						t = Terrain.ROCK;
					}
					else if (heightMap[i][j] > mapType.dirtLevel) {
						t = Terrain.DIRT;
					}
					else if (heightMap[i][j] > 0) {
						t = Terrain.GRASS;
					}
					else {
						t = Terrain.WATER;
					}
					world[i][j] = new Tile(new TileLoc(i, j), t);
				}
			}
		}
		
		int numTiles = width*height;
		Generation.makeLake(numTiles * 1.0/100, world, heightMap);
		Generation.makeLake(numTiles * 1.0/200, world, heightMap);
		Generation.makeLake(numTiles * 1.0/400, world, heightMap);
		Generation.makeLake(numTiles * 1.0/800, world, heightMap);
		for(int i = 0; i < 100; i++) {
			Liquid.propogate(world2);
		}
		
		makeRoad();
		Generation.genOres(world);
		genPlants();
		makeForest();
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
				}
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
						plantsAquatic.add(world[i][j].getPlant());
					}
					
				}
				
				
			}
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
				
				Tile tile = world[i][j];
					if(tile.canPlant()==true && tile.getHasRoad() == false) {
						
						
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
		double castleDistance = Utils.getRandomNormal(5);
		Position halfway = start.multiply(castleDistance).add(end.multiply(1-castleDistance));
		Tile tile = world[halfway.getIntX()][halfway.getIntY()];
//		Structure struct = new Structure(StructureType.CASTLE, halfway);
		if(tile.canBuild() == true) {
			buildStructure(StructureType.CASTLE, tile);
//			world[halfway.getIntX()][halfway.getIntY()].setStructure(struct);
//			structureLoc.add(halfway);
		}else {
			makeCastle(start, end);
		}
		
		
	}
	
	private void turnRoads(Position current, Position prev) {
		if(current.getIntX()-1 < 0 || current.getIntX()+1 >= world.length) {
			return;
		}
		if(world[current.getIntX()][current.getIntY()].canBuild()==true) {
			
			// makes turns bot left -> top right
			if(world[current.getIntX()-1][current.getIntY()].canBuild()==true) {
				if (current.getIntX() - 1 == prev.getIntX() && current.getIntY() == prev.getIntY()) {
					world[current.getIntX()-1][current.getIntY()].setRoad(true, "left_down");
					world[current.getIntX()][current.getIntY()].setRoad(true, "right_up");
				} else if (current.getIntX() - 1 == prev.getIntX() && current.getIntY() + 1 == prev.getIntY()) {
					world[current.getIntX()-1][current.getIntY()].setRoad(true, "left_down");
					world[current.getIntX()][current.getIntY()].setRoad(true, "right_up");
				}	
			}
			

			// makes turns bot right -> top left
			if(world[current.getIntX()+1][current.getIntY()].canBuild()==true) {
				if (current.getIntX() + 1 == prev.getIntX() && current.getIntY() == prev.getIntY()) {
					world[current.getIntX() + 1][current.getIntY()].setRoad(true, "right_down");
					world[current.getIntX()][current.getIntY()].setRoad(true, "left_up");
				} else if (current.getIntX() + 1 == prev.getIntX() && current.getIntY() + 1 == prev.getIntY()) {
					world[current.getIntX() + 1][current.getIntY()].setRoad(true, "right_down");
					world[current.getIntX()][current.getIntY()].setRoad(true, "left_up");
				}
			}
			

			if (world[current.getIntX()][current.getIntY()].getHasRoad() == false) {
				world[current.getIntX()][current.getIntY()].setRoad(true, "top_down");
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
						setTerritory(new TileLoc(i,j));
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
						t.setRecentTick(ticks);
						t.draw(g, currentMode);
					}
					
				}
			}
			for(Animal animal : Wildlife.getAnimals()) {
				g.drawImage(animal.getImage(0), animal.getTile().getLocation().x * Game.tileSize, animal.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				animal.getTile().drawHealthBar(g, animal);
			}
			if(DEBUG_DRAW) {
				if(Game.tileSize >= 36) {
					int[][] rows = new int[world.length][world[0].length];
					int fontsize = Game.tileSize/4;
					fontsize = Math.min(fontsize, 13);
					Font font = new Font("Consolas", Font.PLAIN, fontsize);
					g.setFont(font);
					int stringWidth = g.getFontMetrics().stringWidth(String.format("??=%." + NUM_DEBUG_DIGITS + "f", 0.0));
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile tile = world[i][j];
							int x = i * Game.tileSize + 2;
							int y = j * Game.tileSize + fontsize/2;
							
							g.setColor(Color.black);
							g.fillRect(x, y + 2, stringWidth, 2*fontsize);
							g.setColor(Color.green);
							
							g.drawString(String.format("L=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount), x, y + (++rows[i][j])*fontsize);
							g.drawString(String.format("H=%." + NUM_DEBUG_DIGITS + "f", heightMap[i][j]), x, y + (++rows[i][j])*fontsize);
						}
					}
					for(Animal animal : Wildlife.getAnimals()) {
						animal.getTile().drawDebugStrings(g, animal.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Plant plant : plantsLand) {
						plant.getTile().drawDebugStrings(g, plant.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Building building : buildings) {
						building.getTile().drawDebugStrings(g, building.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Structure structure : structures) {
						structure.getTile().drawDebugStrings(g, structure.getDebugStrings(), rows, fontsize, stringWidth);
					}
				}
			}
		}
		
	}
	private void updateTerritory() {
		for(Structure structure : structures) {
			structure.updateCulture();
		}
	}
	private void setTerritory(TileLoc p) {
		int culture = world[p.x][p.y].getStructure().getCulture();
		double area = culture * Structure.CULTURE_AREA_MULTIPLIER;
		double radius = Math.sqrt(area);
		expandTerritory(radius, p);	
	}
	private void expandTerritory(double radius, TileLoc p) {
		int r = (int)Math.ceil(radius);
		for (int i=0-r; i <= r; i++) {
			for (int j=0-r; j <= r; j++) {
				double distanceFromCenter = Math.sqrt(i*i + j*j);
				if(distanceFromCenter < radius) {
					if(p.x+i >= 0 && p.x+i < world.length && p.y+j >= 0 && p.y+j < world[0].length) {
						world[p.x+i][p.y+j].setTerritory(true);
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
		Position pos = getTileAtPixel(new Position(mx, my));
		System.out.println(currentMode);
		Tile tile = world[pos.getIntX()][pos.getIntY()];
		
		
		if(currentMode == BuildMode.ROAD) {
			if(tile.canBuild() == true) {
				tile.setRoad(true, null);
			}
		}
			
		if(currentMode == BuildMode.BARRACKS) {
			if(tile.canBuild() == true) {
				buildStructure(StructureType.BARRACKS, tile);
			}
		} 
		
		if(currentMode == BuildMode.WALL) {
			if(tile.canBuild() == true) {
				buildBuilding(BuildingType.WALL_BRICK, tile);
			}
		}
		
		if(currentMode == BuildMode.MINE) {
			if(tile.canBuild() == true || tile.getHasOre() == true) {
				buildBuilding(BuildingType.MINE, tile);
			}
		}
		
		if(currentMode == BuildMode.IRRIGATE) {
			if(tile.canBuild() == true && tile.canPlant() == true) {
				buildBuilding(BuildingType.IRRIGATION, tile);
			}
		}
		
		
	}
	private void buildBuilding(BuildingType bt, Tile t) {
		Building building = new Building(bt, t);
		t.setBuilding(building);
		buildings.add(building);
	}
	private void buildStructure(StructureType st, Tile t) {
		Structure structure = new Structure(st, t);
		t.setStructure(structure);
		structures.add(structure);
	}
	public void buildUnit(UnitType u) {
		Tile t = structures.get(0).getTile();
		Unit unit = new Unit(u , t);
		t.setUnit(unit);
		
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
