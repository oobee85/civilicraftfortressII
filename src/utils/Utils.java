package utils;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import game.actions.*;
import networking.message.*;
import ui.*;
import world.*;
import world.liquid.*;

public final class Utils {

	public static final String IMAGEICON_ANIMATED = "GIF";
	public static final int MAX_TILED_BITMAP = 2 * 2 * 2 * 2 * 2 * 2 * 2;
	
	public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public static int stringWidth(String text, Font font) {
	  FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
	  return (int)(font.getStringBounds(text, frc).getWidth());
	}
	
	public static MipMap getImageFromThingType(Object thingType) {
		if(thingType instanceof UnitType) {
			return ((UnitType)thingType).getMipMap();
		}
		else if(thingType instanceof BuildingType) {
			return ((BuildingType)thingType).getMipMap();
		}
		else if(thingType instanceof PlantType) {
			return ((PlantType)thingType).getMipMap();
		}
		return null;
	}

	public static String loadFileAsString(String path) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Utils.class.getResourceAsStream(path)))) {
			String line = "";
			while((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
		}
		catch (IOException e) {
			System.err.println("ERROR: Failed to read file at " + path);
			e.printStackTrace();
		}
		return result.toString();
	}
	
	
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
	
	public static final HashMap<Integer, Image> loadTiledImages(String tiledImageFolder) {

		HashMap<Integer, Image> tiledImages = new HashMap<>();
		
		for (int i = 1; i < Utils.MAX_TILED_BITMAP; i+=2) {
			int bits = i;

			boolean northwest = (bits & Direction.NORTHWEST.tilingBit) != 0;
			boolean northeast = (bits & Direction.NORTHEAST.tilingBit) != 0;
			boolean southeast = (bits & Direction.SOUTHEAST.tilingBit) != 0;
			boolean southwest = (bits & Direction.SOUTHWEST.tilingBit) != 0;
			boolean mirrored = false;
			if ((northwest && !northeast)
						|| ((northwest == northeast) && (southwest && !southeast))) {
	            mirrored = true;
	            bits = (bits & Direction.NONE.tilingBit)
	            		| (bits & Direction.NORTH.tilingBit)
	            		| (northwest ? Direction.NORTHEAST.tilingBit : 0)
	            		| (southwest ? Direction.SOUTHEAST.tilingBit : 0)
	            		| (bits & Direction.SOUTH.tilingBit)
	            		| (southeast ? Direction.SOUTHWEST.tilingBit : 0)
	            		| (northeast ? Direction.NORTHWEST.tilingBit : 0);
			}
			
			String filename = tiledImageFolder + "/" + bits + ".png";
			Image tiledImage = Utils.loadImage(filename);
			
			if (mirrored) {
				BufferedImage buf = Utils.toBufferedImage(tiledImage, false);
				
				BufferedImage mirroredImage = new BufferedImage(buf.getWidth(), buf.getHeight(), buf.getType());
				Graphics g = mirroredImage.getGraphics();
				g.drawImage(buf, buf.getWidth(), 0, -buf.getWidth(), buf.getHeight(), null);
				g.dispose();
				
				tiledImage = mirroredImage;
			}
			tiledImages.put(i, tiledImage);
		}
		return tiledImages;
	}
	
	public static String readFile(String filename) {
		String fileContents = "";
	    try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(filename);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				builder.append(line + "\n");
			}
			fileContents = builder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContents;
	}
	
	public static final ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
		int scalingMode = java.awt.Image.SCALE_SMOOTH;
		if(icon.getDescription() != null && icon.getDescription().equals(IMAGEICON_ANIMATED)) {
			scalingMode = java.awt.Image.SCALE_DEFAULT;
		}
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, scalingMode));
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img, boolean requireNewDataBuffer) {
		if (img == null) {
			return toBufferedImage(Utils.getDefaultSkin(), true);
		}
		if (!requireNewDataBuffer && img instanceof BufferedImage) {
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
		BufferedImage image = toBufferedImage(icon.getImage(), true);
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
	
	public static ImageIcon sunShadowFilter(ImageIcon icon, double shear, double squish) {
		BufferedImage image = toBufferedImage(icon.getImage(), true);
		Color blank = new Color(0, 0, 0, 0);
		for(int x = 0; x < image.getWidth(); x++) {
			for(int y = 0; y < image.getHeight(); y++) {
				Color c = new Color(image.getRGB(x, y), true);
				if(c.getAlpha() > 0.1) {
					image.setRGB(x, y, new Color(0, 0, 0, c.getAlpha()).getRGB());
				}
				else {
					image.setRGB(x, y, blank.getRGB());
				}
			}
		}
		BufferedImage sheared = new BufferedImage(image.getWidth()*2, image.getHeight(), image.getType());
		Graphics2D g = sheared.createGraphics();
		g.transform(AffineTransform.getShearInstance(shear, 0));
		g.translate(-image.getWidth() * shear, 0);
		g.drawImage(image, image.getWidth()/2, (int)(image.getHeight()*squish), image.getWidth(), (int)(image.getHeight()*(1-squish)), null);
		g.dispose();
		return new ImageIcon(sheared);
	}
	
	public static ImageIcon highlightFilter(ImageIcon icon, Color color) {
		BufferedImage image = toBufferedImage(icon.getImage(), true);
		
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
	
	public static HashMap<Terrain, Color> computeTerrainAverageColor() {
		HashMap<Terrain, Color> terrainColors = new HashMap<>();
		for(Terrain t : Terrain.values()) {
			BufferedImage image = Utils.toBufferedImage(t.getImage(0), false);
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
		return terrainColors;
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

	/**
	 * Interpolates between min and max depending on ratio.
	 * ratio gets bounded between 0 and 1
	 */
	public static int lerp(int min, int max, double ratio) {
		ratio = Math.max(Math.min(ratio, 1), 0);
		max = Math.max(max, min);
		return (int) (min + (max - min) * ratio);
	}
	
	public static void clearBufferedImageTo(Graphics2D g, Color color, int w, int h) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT));
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}
	
	public static double getAlphaOfLiquid(double amount) {
		// 1 units of fluid is opaque, linearly becoming transparent at 0 units of fluid.
//		amount = 1- (1/(1+amount));
		double alpha = Math.sqrt(Math.max(Math.min((amount-0.6)*0.1, 1), 0));
		
		return alpha;
		//return 1 - (1 - alpha) * (1 - alpha);
	}
	
	public static double getAlphaDepthOfLiquid(double amount) {
		double alpha = Math.sqrt(Math.max(Math.min(amount*0.01, 1), 0)) / 2;
		return alpha*alpha;
	}

	public static void normalize(double[][] data, float upper, float inner) {
		double minValue = data[0][0];
		double maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				double ratio = (data[i][j] - minValue) / (maxValue - minValue);
				data[i][j] = ratio * (upper-inner) + inner;
			}
		}
	}
	public static void normalize(double[][] data, double upper, double inner) {
		double minValue = data[0][0];
		double maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				double ratio = (data[i][j] - minValue) / (maxValue - minValue);
				data[i][j] = ratio * (upper-inner) + inner;
			}
		}
	}
	public static void normalize(float[][] data, float low, float high) {
		
		
		float minValue = data[0][0];
		float maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		System.out.println("Before normalize Min Terrain Gen Value: " + minValue + ", Max value: " + maxValue);
		// Normalize the heightMap to be between 0 and 1
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				data[i][j] = (data[i][j] - minValue) / (maxValue - minValue);
//				data[i][j] = data[i][j] * upper;
				data[i][j] = data[i][j] * (high-low) + low;
			}
		}
		minValue = data[0][0];
		maxValue = data[0][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				minValue = data[i][j] < minValue ? data[i][j] : minValue;
				maxValue = data[i][j] > maxValue ? data[i][j] : maxValue;
			}
		}
		System.out.println("After normalize Min Terrain Gen Value: " + minValue + ", Max value: " + maxValue);
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
	
	public static WorldInfo extractWorldInfo(World world, boolean everything, boolean units, boolean onlyChangedTiles) {
		Tile[] tileArray;
		if (everything) {
			ArrayList<Tile> tileInfos = new ArrayList<>(world.getTiles().size());
			for (Tile t : world.getTiles()) {
				if (!onlyChangedTiles || t.isChangedFromLatestSent()) {
					tileInfos.add(t);
				}
			}
			tileArray = tileInfos.toArray(new Tile[0]);
		}
		else {
			tileArray = new Tile[0];
		}
		WorldInfo worldInfo = new WorldInfo(world.getWidth(), world.getHeight(), World.ticks, tileArray);
		if (everything) {
			worldInfo.getThings().addAll(world.getPlants());
			worldInfo.getThings().addAll(world.getBuildings());
			worldInfo.getFactions().addAll(world.getFactions());
		}
		if (units) {
			worldInfo.getThings().addAll(world.getUnits());
		}
		worldInfo.getThings().addAll(world.getData().clearDeadThings());
		worldInfo.addHitsplats(world.getData());
		worldInfo.getProjectiles().addAll(world.getData().clearProjectilesToSend());
		return worldInfo;
	}

	public static void saveToFile(WorldInfo worldInfo, String filename, boolean append) {
		try(ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(filename, append))) {
			objOut.writeObject(worldInfo);
		} catch (FileNotFoundException e) {
			// missing folder is fine
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static WorldInfo loadFromFile(String filename) {
		WorldInfo worldInfo = null;
		try(ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(filename))) {
			worldInfo = (WorldInfo)objIn.readObject();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return worldInfo;
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
		LinkedList<TileLoc> ring = new LinkedList<>();
		for(int r = 0; r <= radius; r++) {
			Utils.getRingOfTileLocs(tile.getLocation(), world, r, ring);
		}
		LinkedList<Tile> tiles = new LinkedList<>();
		for(TileLoc loc : ring) {
			Tile t = world.get(loc);
			if(t != null) {
				tiles.add(t);
			}
		}
		return tiles;
	}
	public static List<Tile> getRingOfTiles(Tile center, World world, int radius) {
		LinkedList<TileLoc> ring = new LinkedList<>();
		Utils.getRingOfTileLocs(center.getLocation(), world, radius, ring);
		LinkedList<Tile> tiles = new LinkedList<>();
		for(TileLoc loc : ring) {
			Tile t = world.get(loc);
			if(t != null) {
				tiles.add(t);
			}
		}
		return tiles;
	}
	private static void getRingOfTileLocs(TileLoc center, World world, int radius, List<TileLoc> ring) {
		// top corner
		ring.add(new TileLoc(center.x(), center.y() - radius));
		if(radius == 0) {
			return;
		}
		// top right edge
		for(int i = 1; i < radius; i++) {
			int minusyoffset = radius - i/2;
			if(center.x()%2 == 1 && i%2 == 1) {
				minusyoffset -= 1;
			}
			ring.add(new TileLoc(center.x()+i, center.y() - minusyoffset));
		}
		// right edge
		for(int i = radius; i >= 0; i--) {
			int y = center.y() + radius/2 - i;
			if(radius%2 == 1 && center.x()%2 == 1) {
				y += 1;
			}
			ring.add(new TileLoc(center.x()+radius, y));
		}
		// bottom right edge
		for(int i = radius - 1; i >= 1; i--) {
			int yoffset = radius - i/2;
			if(center.x()%2 == 0 && i%2 == 1) {
				yoffset -= 1;
			}
			ring.add(new TileLoc(center.x()+i, center.y() + yoffset));
		}
		// bottom corner
		ring.add(new TileLoc(center.x(), center.y() + radius));
		// bottom left edge
		for(int i = 1; i < radius; i++) {
			int yoffset = radius - i/2;
			if(center.x()%2 == 0 && i%2 == 1) {
				yoffset -= 1;
			}
			ring.add(new TileLoc(center.x()-i, center.y() + yoffset));
		}
		// left edge
		for(int i = 0; i < radius+1; i++) {
			int y = center.y() + radius/2 - i;
			if(radius%2 == 1 && center.x()%2 == 1) {
				y += 1;
			}
			ring.add(new TileLoc(center.x()-radius, y));
		}
		// top left edge
		for(int i = radius - 1; i >= 1; i--) {
			int minusyoffset = radius - i/2;
			if(center.x()%2 == 1 && i%2 == 1) {
				minusyoffset -= 1;
			}
			ring.add(new TileLoc(center.x()-i, center.y() - minusyoffset));
		}
	}
	public static double getRandomNormal(int tries) {
		double rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}
	public static float getRandomNormalF(int tries) {
		float rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}

	public static Position[] normalizeRectangle(Position one, Position two) {
		double x = Math.min(one.x, two.x);
		double y = Math.min(one.y, two.y);
		double width = Math.abs(one.x - two.x);
		double height = Math.abs(one.y - two.y);
		return new Position[] { new Position(x, y), new Position(x + width, y + height) };
	}

	public static List<Tile> getTilesBetween(World world, Position cornerOne, Position cornerTwo) {
		int leftEdge = Math.min(cornerOne.getIntX(), cornerTwo.getIntX());
		int rightEdge = Math.max(cornerOne.getIntX(), cornerTwo.getIntX());
		int topEvenY = cornerOne.getIntY();
		int topOddY = cornerOne.getIntY();
		if (cornerOne.getIntX() % 2 == 0) {
			if (cornerOne.y - cornerOne.getIntY() < 0.5f) {
				topOddY = cornerOne.getIntY() - 1;
			}
		}
		else {
			if (cornerOne.y - cornerOne.getIntY() >= 0.5f) {
				topEvenY = cornerOne.getIntY() + 1;
			}
		}
		int botEvenY = cornerTwo.getIntY();
		int botOddY = cornerTwo.getIntY();
		if (cornerTwo.getIntX() % 2 == 0) {
			if (cornerTwo.y - cornerTwo.getIntY() < 0.5f) {
				botOddY = cornerTwo.getIntY() - 1;
			}
		}
		else {
			if (cornerTwo.y - cornerTwo.getIntY() >= 0.5f) {
				botEvenY = cornerTwo.getIntY() + 1;
			}
		}
		
		if (topEvenY > botEvenY) {
			int temp = topEvenY;
			topEvenY = botEvenY;
			botEvenY = temp;
		}
		if (topOddY > botOddY) {
			int temp = topOddY;
			topOddY = botOddY;
			botOddY = temp;
		}
		
		LinkedList<Tile> tiles = new LinkedList<>();
		for (int i = leftEdge; i <= rightEdge; i++) {
			int minj = i % 2 == 0 ? topEvenY : topOddY;
			int maxj = i % 2 == 0 ? botEvenY : botOddY;
			for (int j = minj; j <= maxj; j++) {
				TileLoc otherLoc = new TileLoc(i, j);
				Tile otherTile = world.get(otherLoc);
				if (otherTile != null) {
					tiles.add(otherTile);
				}
			}
		}
		return tiles;
	}
	
	public static CommandInterface makeFunctionalCommandInterface(Game game) {
		return new CommandInterface() {
			@Override
			public void setBuildingRallyPoint(Building building, Tile rallyPoint) {
				building.setRallyPoint(rallyPoint);
			}
			@Override
			public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				if(unit.isBuilder() && unit.getBuildableBuildingTypes().contains(buildingType)) {
					Building plannedBuilding = game.planBuilding(unit, buildingType, target);
					// if building is planned, and is harvestable, should queue harvest action
					if(plannedBuilding != null && plannedBuilding.getType().isHarvestable()) {
						PlannedAction followup = PlannedAction.harvest(plannedBuilding);
						unit.queuePlannedAction(PlannedAction.buildOnTile(target, buildingType.isRoad(), followup));
					}
					else {
						unit.queuePlannedAction(PlannedAction.buildOnTile(target, buildingType.isRoad()));
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
				faction.craftItem(itemType, amount);
			}
			@Override
			public void produceUnit(Building building, UnitType unitType) {
				if (!building.getFaction().areRequirementsMet(unitType)) {
					return;
				}
				if(building.getFaction().canAfford(unitType.getCost())) {
					Unit unit = new Unit(unitType, building.getTile(), building.getFaction());
					if (building.getTile().isBlocked(unit)) {
						return;
					}
//					unit.getCombatStats().mergeCombatStats(building.getFaction().getUpgradedCombatStats());
					unit.getCombatStats().addTicksToBuild(building.getFaction().getUpgradedCombatStats().getTicksToBuild());
					building.getFaction().payCost(unitType.getCost());
					building.setProducingUnit(unit);
				}
			}
			@Override
			public void planAction(Unit unit, PlannedAction plan, boolean clearQueue) {
				if(clearQueue) {
					unit.clearPlannedActions();
				}
				unit.queuePlannedAction(plan);
			}
		};
	}
}
