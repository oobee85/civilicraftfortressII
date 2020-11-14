package utils;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import game.liquid.*;
import ui.*;
import world.*;

public final class Utils {
	
	public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public static String getName(Object o) {
		// TODO use map to record these so I can just lookup
		String rawName = "";
		if(o instanceof BuildingType) {
			rawName = ((BuildingType)o).name();
		}
		else if(o instanceof ItemType) {
			rawName = ((ItemType)o).name();
		}
		else if(o instanceof ResearchType) {
			rawName = ((ResearchType)o).name();
		}
		else if(o instanceof Research) {
			rawName = ((Research)o).getName();
		}
		else if(o instanceof UnitType) {
			rawName = ((UnitType)o).name();
		}
		else if(o instanceof LiquidType) {
			rawName = ((LiquidType)o).name();
		}
		else if(o instanceof PlantType) {
			rawName = ((PlantType)o).name();
		}
		else if(o instanceof ResourceType) {
			rawName = ((ResourceType)o).name();
		}
		else if(o instanceof ProjectileType) {
			rawName = ((ProjectileType)o).name();
		}
		else if(o instanceof Terrain) {
			rawName = ((Terrain)o).name();
		}
		return rawName.toLowerCase().replace('_', ' ');
	}
	public static String getNiceName(String rawName) {
		return rawName.toLowerCase().replace('_', ' ');
	}

	public static final Image getDefaultSkin() {
		BufferedImage temp = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D)temp.getGraphics();
		g.setColor(Color.magenta);
		g.fillRect(0, 0, temp.getWidth(), temp.getHeight());
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(5));
		g.drawLine(0, 0, temp.getWidth(), temp.getHeight());
		g.drawLine(temp.getWidth(), 0, 0, temp.getHeight());
		g.dispose();
		return temp;
	}

	public static final ImageIcon loadImageIcon(String filename) {
		URL a = Loader.class.getClassLoader().getResource(filename);
		if (a != null) {
			return new ImageIcon(a);
		}
		else {
			System.err.println("FAILED TO LOAD FILE " + filename);
			return new ImageIcon(getDefaultSkin());
		}
	}
	
	public static final Image loadImage(String filename) {
		ImageIcon icon = loadImageIcon(filename);
		if(icon != null) {
			return icon.getImage();
		}
		else {
			return Utils.getDefaultSkin();
		}
	}
	
	public static final ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
		Image image = icon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		return new ImageIcon(newimg);  // transform it back
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	
	public static ImageIcon shadowFilter(ImageIcon icon) {
		BufferedImage image = toBufferedImage(icon.getImage());
		Color blank = new Color(0, 0, 0, 0);
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				Color c = new Color(image.getRGB(x, y), true);
				if(c.getAlpha() > 0.1) {
					int avg = (c.getRed() + c.getGreen() + c.getBlue())/255;
					image.setRGB(x, y, new Color(avg, avg, avg, c.getAlpha()).getRGB());
				}
				else {
					image.setRGB(x, y, blank.getRGB());
				}
			}
		}
		return new ImageIcon(image);
	}
	
	public static ImageIcon highlightFilter(ImageIcon icon, Color color) {
		BufferedImage image = toBufferedImage(icon.getImage());
		Color blank = new Color(0, 0, 0, 0);
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				Color c = new Color(image.getRGB(x, y), true);
				if(c.getAlpha() > 0.1) {
					image.setRGB(x, y, color.getRGB());
				}
				else {
					image.setRGB(x, y, blank.getRGB());
				}
			}
		}
		int numLayers = Math.max(1, Math.min(3, image.getWidth()*image.getHeight()/900));
		for(int i = 0; i < numLayers; i++) {
			BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			for(int x = 0; x < image.getWidth(); x++) {
				for(int y = 0; y < image.getHeight(); y++) {
					boolean activeNeighbor = (image.getRGB(x, y) == color.getRGB());
					if(x-1 >= 0 && y >= 0 && x-1 < image.getWidth() && y < image.getHeight()) {
						if(image.getRGB(x-1, y) == color.getRGB()) {
							activeNeighbor = true;
						}
					}
					if(x+1 >= 0 && y >= 0 && x+1 < image.getWidth() && y < image.getHeight()) {
						if(image.getRGB(x+1, y) == color.getRGB()) {
							activeNeighbor = true;
						}
					}
					if(x >= 0 && y-1 >= 0 && x < image.getWidth() && y-1 < image.getHeight()) {
						if(image.getRGB(x, y-1) == color.getRGB()) {
							activeNeighbor = true;
						}
					}
					if(x >= 0 && y+1 >= 0 && x < image.getWidth() && y+1 < image.getHeight()) {
						if(image.getRGB(x, y+1) == color.getRGB()) {
							activeNeighbor = true;
						}
					}
					if(activeNeighbor) {
						newImage.setRGB(x, y, color.getRGB());
					}
				}
			}
			image = newImage;
		}
		return new ImageIcon(image);
	}
	
	public static Color getAverageColor(BufferedImage image) {
		float sumr = 0;
		float sumg = 0;
		float sumb = 0;
		float total = 0;
		for(int i = 0; i < image.getWidth(); i++) {
			for(int j = 0; j < image.getHeight(); j++) {
				Color c = new Color(image.getRGB(i, j), true);
				float alpha = c.getAlpha()/255f;
				sumr += c.getRed()*alpha/255f;
				sumg += c.getGreen()*alpha/255f;
				sumb += c.getBlue()*alpha/255f;
				total += alpha;
			}
		}
		return new Color(sumr/total, sumg/total, sumb/total);
	}
	

//	public Driver() {
//		Image lava = Utils.loadImage("Images/lava_flow.png");
//		System.out.println(lava.getWidth());
//		System.out.println(lava.getHeight());
//		
//		for(int i = 0; i < lava.getHeight(); i += lava.getWidth()) {
//			BufferedImage subimage = lava.getSubimage(0, i, lava.getWidth(), lava.getWidth());
//			System.out.println(subimage.getWidth());
//			System.out.println(subimage.getHeight());
//			try {
//				ImageIO.write(subimage, "png", new File("D:/Workspace/lava_" + i/lava.getWidth() + ".png"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
	
//	public static void resizeImage(String filename, String outputFile, int width, int height) {
//		try {
//			BufferedImage image = ImageIO.read(new File(filename));
//			BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g = target.createGraphics();
//			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//			g.drawImage(image, 0, 0, width, height, null);
//			g.dispose();
//			ImageIO.write(target, "png", new File(outputFile));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void rotateAndScaleImage(String filename, String outputFile) {
//		double angle = 0;
//		
//		try {
//			BufferedImage image = ImageIO.read(new File(filename));
//			double xscale = 16.0/image.getWidth();
//			double yscale = 16.0/image.getHeight();
//			// make image that is twice as big
//			BufferedImage rotated = new BufferedImage(image.getWidth() + image.getHeight(), image.getHeight() + image.getWidth(), BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g = rotated.createGraphics();
//			g.setColor(Color.gray);
//			g.fillRect(0, 0, rotated.getWidth(), rotated.getHeight());
//			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//			g.translate(400, 0);
//			g.rotate(angle * 2 * Math.PI / 360);
//			g.scale(xscale, yscale);
//			g.drawImage(image, 0, 0, null);
//			g.dispose();
//			ImageIO.write(rotated, "png", new File(outputFile));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * @param alpha 1 alpha is opaque, 0 alpha is completely transparent
	 * @param g
	 */
	public static void setTransparency(Graphics g, double alpha) {
	    ((Graphics2D)g).setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha));
	}
	
	/**
	 * @return transparent version of provided color with specified alpha
	 */
	public static Color getTransparentColor(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	/**
	 * result is top*alpha + bottom*(1-alpha)
	 * @param top
	 * @param bottom
	 * @param alpha
	 */
	public static Color blendColors(Color top, Color bottom, double alpha) {
		alpha = alpha > 1 ? 1 : (alpha < 0 ? 0 : alpha);
		double beta = 1-alpha;
		return new Color(
				(int) (top.getRed()*alpha + bottom.getRed()*beta),
				(int) (top.getGreen()*alpha + bottom.getGreen()*beta),
				(int) (top.getBlue()*alpha + bottom.getBlue()*beta)
				);
	}
	
	public static double getAlphaOfLiquid(double amount) {
		// 1 units of fluid is opaque, linearly becoming transparent at 0 units of fluid.
		double alpha = Math.max(Math.min(amount*12 - 0.2, 1), 0);
		return alpha*alpha;
		//return 1 - (1 - alpha) * (1 - alpha);
	}
	

	public static void normalize(float[][] data) {
		float minValue = data[0][0];
		float maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		System.out.println("Min Terrain Gen Value: " + minValue + ", Max value: " + maxValue);
		// Normalize the heightMap to be between 0 and 1
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				data[i][j] = (data[i][j] - minValue) / (maxValue - minValue);
			}
		}
	}
	/**
	 * 
	 * @param data
	 * @param radius
	 * @param c	lower = sharper, higher = smoother
	 * @return
	 */
	public static float[][] smoothingFilter(float[][] data, double radius, double c) {
		ArrayList<Future<float[]>> tasks = new ArrayList<>(data.length);
		// apply smoothing filter
		// each thread applies smoothing for 1 row
		for (int i = 0; i < data.length; i++) {
			final int myI = i;
			Future<float[]> future = executorService.submit(() -> {
				float[] myRow = new float[data[0].length];
				for (int j = 0; j < data[0].length; j++) {
					int mini = (int) Math.max(0, myI-radius);
					int maxi = (int) Math.min(data.length-1, myI+radius);
					int minj = (int) Math.max(0, j-radius);
					int maxj = (int) Math.min(data[0].length-1, j+radius);
					double count = 0;
					for(int ii = mini; ii <= maxi; ii++) {
						for(int jj = minj; jj <= maxj; jj++) {
							double distance = Math.sqrt((ii-myI)*(ii-myI) + (jj-j)*(jj-j));
							double gaussian = Math.exp(-distance*distance / c);
							myRow[j] += gaussian * data[ii][jj];
							count += gaussian;
						}
					}
					myRow[j] /= count;
				}
				return myRow;
			});
			tasks.add(future);
		}
		// combine the results from all the threads
		float[][] smoothed = new float[data.length][];
		try {
			for (int i = 0; i < data.length; i++) {
				Future<float[]> task = tasks.get(i);
				smoothed[i] = task.get();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return smoothed;
	}

	public static List<Tile> getNeighborsIncludingCurrent(Tile tile, World world) {
		List<Tile> tiles = getNeighbors(tile, world);
		tiles.add(tile);
		Collections.shuffle(tiles); 
		return tiles;
	}
	public static List<Tile> getNeighbors(Tile tile, World world) {
		return tile.getNeighbors();
	}
	public static List<Tile> getTilesInRadius(Tile tile, World world, double radius) {
		int maxr = (int) Math.ceil(radius);
		TileLoc center = tile.getLocation();
		int mini = (int) Math.max(0, center.x()-maxr);
		int maxi = (int) Math.min(world.getWidth()-1, center.x()+maxr);
		int minj = (int) Math.max(0, center.y()-maxr);
		int maxj = (int) Math.min(world.getHeight()-1, center.y()+maxr);
		LinkedList<Tile> tiles = new LinkedList<>();
		for(int i = mini; i <= maxi; i++) {
			for(int j = minj; j <= maxj; j++) {
				TileLoc otherLoc = new TileLoc(i, j);
				Tile otherTile = world.get(otherLoc);
				if(otherTile != null) {
					if(center.euclideanDistance(otherLoc) <= radius) {
						tiles.add(otherTile);
					}
				}
			}
		}
		return tiles;
	}
	public static double getRandomNormal(int tries) {
		double rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}
	
	public static CommandInterface makeFunctionalCommandInterface(Game game) {
		return new CommandInterface() {
			@Override
			public void setBuildingRallyPoint(Building building, Tile rallyPoint) {
				building.setRallyPoint(rallyPoint);
			}
			@Override
			public void moveTo(Unit unit, Tile target, boolean clearQueue) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				unit.queuePlannedAction(new PlannedAction(target));
			}
			@Override
			public void attackThing(Unit unit, Thing target, boolean clearQueue) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				unit.queuePlannedAction(new PlannedAction(target));
			}
			@Override
			public void buildThing(Unit unit, Tile target, boolean isRoad, boolean clearQueue) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				unit.queuePlannedAction(new PlannedAction(target, isRoad));
			}
			@Override
			public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				if(unit.getType().isBuilder()) {
					Building plannedBuilding = game.planBuilding(unit, buildingType, target);
					if(plannedBuilding != null) {
						unit.queuePlannedAction(new PlannedAction(target, buildingType.isRoad()));
					}
					return plannedBuilding;
				}
				return null;
			}
			@Override
			public void stop(Unit unit) {
				unit.clearPlannedActions();
			}
			@Override
			public void research(Faction faction, ResearchType researchType) {
				faction.setResearchTarget(researchType);
			}
			@Override
			public void craftItem(Faction faction, ItemType itemType, int amount) {
				faction.craftItem(itemType, amount, game.world.getBuildings());
			}
			@Override
			public void produceUnit(Building building, UnitType unitType) {
				if(building.getFaction().canAfford(unitType.getCost())) {
					Unit unit = new Unit(unitType, building.getTile(), building.getFaction());
					if (building.getTile().isBlocked(unit)) {
						return;
					}
					building.getFaction().payCost(unitType.getCost());
					building.setProducingUnit(unit);
				}
			}
			@Override
			public void setGuarding(Unit unit, boolean enabled) {
				unit.setGuarding(enabled);
			}
		};
	}
}
