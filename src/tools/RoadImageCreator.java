package tools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import utils.*;



public class RoadImageCreator {
	
	private static enum EntityType {
		ROAD, WALL, GATE, LIQUID;
		
		public static String listNames() {
			StringBuilder sb = new StringBuilder();
			for (EntityType type : EntityType.values()) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(type.name());
			}
			return sb.toString();
		}
		
		public static EntityType getTypeFromFilepath(String filepath) throws Exception {
			EntityType type = null;
			String uppercased = filepath.toUpperCase();
			for (EntityType potentialType : EntityType.values()) {
				if (uppercased.contains(potentialType.name())) {
					if (type != null) {
						throw new Exception("ERROR: found multiple EntityType matches for " + filepath);
					}
					type = potentialType;
				}
			}
			return type;
		}
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("RoadImageCreator <image filepath 1> [image filepath 2] [image filepath 3] [...]");
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
		}
		processFiles(args);
	}
	
	private static String getOutputPath(File inputFile) {
		String filename = inputFile.getName();
		String filepath = inputFile.getPath();
		String filenameWithoutExtension = filename.substring(0, filename.length() - 4);
		String outputpath = filepath.substring(0, filepath.length() - filename.length()) + filenameWithoutExtension;
		return outputpath;
	}
	private static void processFiles(String[] files) {
		for (String filepath : files) {
			System.out.println("Processing " + filepath);
			EntityType type = null;
			try {
				type = EntityType.getTypeFromFilepath(filepath);
			} catch (Exception e1) {
				System.err.println("ERROR: Unknown entity type for filepath: " + filepath);
				System.out.println("filepath must contain one of: " + EntityType.listNames());
				e1.printStackTrace();
				continue;
			}

			File file = new File(filepath);
			if (!file.exists()) {
				System.err.println("Failed to find file: " + file.getAbsolutePath());
				System.out.print("File name must contain one of: ");
				continue;
			}
			File outputDirectory = new File(getOutputPath(file));
			if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
				System.err.println("ERROR output path already exists and is a file, not a directory: " + outputDirectory.getAbsolutePath());
				continue;
			}
			if (!outputDirectory.exists()) {
				outputDirectory.mkdir();
			}
			BufferedImage image = null;
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
				System.err.println("Failed to read image from file: " + file.getAbsolutePath());
				e.printStackTrace();
				continue;
			}
			if (!processImage(image, type, outputDirectory)) {
				System.err.println("Failed to process image. file: " + file.getAbsolutePath());
			}
//			System.exit(0);
		}
	}
	
	private static List<DirectionCombo> getAllDirectionCombinations() {
		int counter_max = 2 * 2 * 2 * 2 * 2 * 2; // 2 ^ 6
		System.out.println("num combos: " + counter_max);
		List<DirectionCombo> combos = new ArrayList<>(counter_max);
		for (int counter = 0; counter < counter_max; counter++) {
			combos.add(new DirectionCombo(counter));
		}
		
		return combos;
	}
	
	private static boolean processImage(BufferedImage image, EntityType type, File outputDirectory) {
		System.out.println("Processing image as a " + type.name() + " with output to " + outputDirectory.getAbsolutePath());
		
		for (DirectionCombo combo : getAllDirectionCombinations()) {
			String outputFilename = combo.toString() + ".png";
			File outputFile = new File(outputDirectory + "\\" + outputFilename);
			
			System.out.println(String.format("Direction combo: %03X == %s", combo.bitmap, combo.toString()));
		
			BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR);
			
			Graphics g = result.getGraphics();
			
			final int MAX = 64;
			final int SIZE = 16;
			final int CENTER = 24;
			final int ALIGN = 12;
			
			if (combo.bitmap == 0) {
				g.drawImage(image, CENTER, CENTER, SIZE, SIZE, null);
			}
			
			int[] layeredOrdering = {1, 3, 5, 2, 4, 0};
			int[] almostCenter = {5, 5, 5, 5, 5, 5};
			
			for(Direction dir : Direction.values()) {
				if (dir == Direction.NONE) {
					continue;
				}
				if (combo.isPresent(dir)) {
					double deltax = dir.deltax();
					double deltay = dir.deltay();
					g.drawImage(image, MAX/2 - SIZE/2 + (int)(deltax*MAX/2), MAX/2 - SIZE/2 + (int)(deltay*MAX/2), SIZE, SIZE, null);
					if (combo.numActive == 1) {
						// connect to center
						for (int i = 0; i < layeredOrdering.length; i++) {
							g.drawImage(image, 
									MAX/2 - SIZE/2 + (int)(deltax*MAX*layeredOrdering[i]/layeredOrdering.length/2), 
									MAX/2 - SIZE/2 + (int)(deltay*MAX*layeredOrdering[i]/layeredOrdering.length/2),
									SIZE, SIZE, null);
						}
					}
					if (combo.reachesAcross) {
						// connect to center
						for (int i = 0; i < layeredOrdering.length; i++) {
							g.drawImage(image, 
									MAX/2 - SIZE/2 + (int)(deltax*MAX*layeredOrdering[i]/layeredOrdering.length/2), 
									MAX/2 - SIZE/2 + (int)(deltay*MAX*layeredOrdering[i]/layeredOrdering.length/2), SIZE, SIZE, null);
						}
					}
					else {
						// connect to adjacent
						Direction adjacentPlus = Direction.values()[(dir.ordinal() + 1) % 6];
						if (combo.isPresent(adjacentPlus)) {
							for (int i = 0; i < layeredOrdering.length; i++) {
								double dxPlus = (deltax * layeredOrdering[i] + adjacentPlus.deltax() * (layeredOrdering.length - 1 - layeredOrdering[i])) / layeredOrdering.length;
								double dyPlus = (deltay * layeredOrdering[i] + adjacentPlus.deltay() * (layeredOrdering.length - 1 - layeredOrdering[i])) / layeredOrdering.length;

								g.drawImage(image, 
										MAX/2 - SIZE/2 + (int)(dxPlus*MAX/2),
										MAX/2 - SIZE/2 + (int)(dyPlus*MAX/2), SIZE, SIZE, null);
							}
						}
					}
				}
			}
			
			g.dispose();
			try {
				ImageIO.write(result, "PNG", outputFile);
			} catch (IOException e) {
				System.out.println("ERROR: Failed to write iamge file: " + outputFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		
		return true;
	}

}
