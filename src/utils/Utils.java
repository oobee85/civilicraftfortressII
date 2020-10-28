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
	
	public static final String[] DIRECTION_STRINGS = new String[] {"north", "east", "south", "west"};
	public static final String ALL_DIRECTIONS = DIRECTION_STRINGS[0] + DIRECTION_STRINGS[1] + DIRECTION_STRINGS[2] + DIRECTION_STRINGS[3];
	
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
	public static List<Tile> getTilesInRadius(Tile tile, World world, double radius) {
		int maxr = (int) Math.ceil(radius);
		TileLoc center = tile.getLocation();
		int mini = (int) Math.max(0, center.x-maxr);
		int maxi = (int) Math.min(world.getWidth()-1, center.x+maxr);
		int minj = (int) Math.max(0, center.y-maxr);
		int maxj = (int) Math.min(world.getHeight()-1, center.y+maxr);
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
}
