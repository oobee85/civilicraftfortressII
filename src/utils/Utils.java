package utils;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import game.*;
import liquid.*;
import world.*;

public final class Utils {
	
	public static Color roadColor;
	public static final HashMap<String, Image> roadImages;
	public static final String[] DIRECTION_STRINGS = new String[] {"north", "east", "south", "west"};
	static {
		roadImages = new HashMap<>();
		for(Direction[] arr : ImageCreation.getAllDirectionCombinations()) {
			String s = "";
			for(Direction d: arr) {
				s += d;
			}
			roadImages.put(s, loadImage("resources/Images/road/road_" + s + ".png"));
			if(roadColor == null) {
				roadColor = getAverageColor(Utils.toBufferedImage(roadImages.get(s)));
			}
		}
	}
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors()-1, 1));
	
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
		else if(o instanceof RoadType) {
			rawName = ((RoadType)o).name();
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
		else if(o instanceof Terrain) {
			rawName = ((Terrain)o).name();
		}
		return rawName.toLowerCase().replace('_', ' ');
	}

	public static final Image getDefaultSkin() {
		Image temp = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = temp.getGraphics();
		g.drawLine(0, 0, 50, 50);
		g.dispose();
		return temp;
	}

	public static final ImageIcon loadImageIcon(String filename) {
		URL a = Utils.class.getClassLoader().getResource(filename);
		if (a != null) {
			return new ImageIcon(a);
		}
		else {
			System.err.println("FAILED TO LOAD FILE " + filename);
		}
		return null;
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
	 * @param img
	 *            The Image to be converted
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
	
	/**
	 * @param alpha 1 alpha is opaque, 0 alpha is completely transparent
	 * @param g
	 */
	public static void setTransparency(Graphics g, double alpha) {
	    ((Graphics2D)g).setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha));
	}
	
	/**
	 * result is top*alpha + bottom*(1-alpha)
	 * @param top
	 * @param bottom
	 * @param alpha
	 */
	public static Color blendColors(Color top, Color bottom, double alpha) {
		alpha = Math.max(Math.min(alpha, 1), 0);
		return new Color(snap((int) (top.getRed()*alpha + bottom.getRed()*(1-alpha))), 
				snap((int) (top.getGreen()*alpha + bottom.getGreen()*(1-alpha))),
				snap((int) (top.getBlue()*alpha + bottom.getBlue()*(1-alpha))));
	}
	private static int snap(int color) {
		return Math.min(Math.max(color, 0), 255);
	}
	
	public static double getAlphaOfLiquid(double amount) {
		// 1 units of fluid is opaque, linearly becoming transparent at 0 units of fluid.
		double alpha = Math.max(Math.min(amount*12 - 0.2, 1), 0);
		return alpha*alpha;
		//return 1 - (1 - alpha) * (1 - alpha);
	}
	

	public static void normalize(double[][] data) {
		double minValue = data[0][0];
		double maxValue = data[0][0];
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
	public static double[][] smoothingFilter(double[][] data, double radius, double c) {
		ArrayList<Future<double[]>> tasks = new ArrayList<>(data.length);
		// apply smoothing filter
		// each thread applies smoothing for 1 row
		for (int i = 0; i < data.length; i++) {
			final int myI = i;
			Future<double[]> future = executorService.submit(() -> {
				double[] myRow = new double[data[0].length];
				for (int j = 0; j < data[0].length; j++) {
					int mini = (int) Math.max(0, myI-radius);
					int maxi = (int) Math.min(data.length-1, myI+radius);
					int minj = (int) Math.max(0, j-radius);
					int maxj = (int) Math.min(data[0].length-1, j+radius);
					double count = 0;
					for(int ii = mini; ii <= maxi; ii++) {
						for(int jj = minj; jj < maxj; jj++) {
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
		double[][] smoothed = new double[data.length][];
		try {
			for (int i = 0; i < data.length; i++) {
				Future<double[]> task = tasks.get(i);
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
	public static double getRandomNormal(int tries) {
		double rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}
}
