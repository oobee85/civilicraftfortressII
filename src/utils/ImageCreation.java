package utils;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;

public class ImageCreation {
	
	private static void getAllDirectionCombinationsHelper(List<Direction[]> list, Direction[] sofar, int startingIndex) {
		for (int l = startingIndex; l < Direction.values().length; l++) {
			Direction d = Direction.values()[l];
			Direction[] newcombo = Arrays.copyOf(sofar, sofar.length+1);
			newcombo[newcombo.length-1] = d;
			list.add(newcombo);
			getAllDirectionCombinationsHelper(list, newcombo, l+1);
		}
	}
	
	public static List<Direction[]> getAllDirectionCombinations() {
		List<Direction[]> result = new LinkedList<Direction[]>();
		getAllDirectionCombinationsHelper(result, new Direction[0], 0);
		return result;
	}
	
	public static HashMap<String, Image> createRoadImages(String roadtilefile) {
		HashMap<String, Image> roadImages = new HashMap<String, Image>();
		BufferedImage roadtile = Utils.toBufferedImage(Utils.loadImage(roadtilefile));
		int width = roadtile.getWidth();
		int height = roadtile.getHeight();

		int centerx = width*3/2;
		int centery = height*3/2;
		int roadimagewidth = width * 4;
		int roadimageheight = height*4;
		
		for(Direction[] arr : getAllDirectionCombinations()) {
			BufferedImage target = new BufferedImage(roadimagewidth, roadimageheight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = target.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			String filename = "";
			for(Direction dir : arr) {
				filename += dir;
				double deltax = dir.deltax();
				double deltay = dir.deltay();
				for(int i = 0; i < 10; i++) {
					g.drawImage(roadtile, (int)(centerx + deltax*width*i/4), (int)(centery + deltay*width*i/4), null);
				}
				g.drawImage(roadtile, (int)(centerx + deltax*width), (int)(centery + deltay*width), null);
				g.drawImage(roadtile, (int)(centerx + deltax*width*3/2), (int)(centery + deltay*width*3/2), null);
			}
			g.drawImage(roadtile, centerx, centery, null);
			g.dispose();
			roadImages.put(filename, target);
		}
		return roadImages;
	}
	
	public static BufferedImage convertToHexagonal(BufferedImage image) {
		BufferedImage hex = new BufferedImage(image.getWidth()*2, image.getHeight()*2 + 1, image.getType());
		Graphics2D g = hex.createGraphics();
		for(int x = 0; x < image.getWidth(); x++) {
			int yoffset = x%2;
			g.drawImage(image, x*2, yoffset, (x+1)*2, hex.getHeight() - 1 + yoffset, x, 0, x + 1, image.getHeight(), null);
		}
		g.dispose();
		return hex;
	}
	public static BufferedImage convertFromHexagonal(BufferedImage image) {
		BufferedImage normal = new BufferedImage(image.getWidth()/2, image.getHeight()/2, image.getType());
		Graphics2D g = normal.createGraphics();
		for(int x = 0; x < normal.getWidth(); x++) {
			for(int y = 0; y < normal.getHeight(); y++) {
				int yoff = x%2;
				int[] values = new int[] {
					image.getRGB(x*2, y*2 + yoff),
					image.getRGB(x*2 + 1, y*2 + yoff),
					image.getRGB(x*2 + 1, y*2 + yoff + 1),
					image.getRGB(x*2, y*2 + yoff + 1),
				};
				int averagedRGB = getAverageRGB(values);
				normal.setRGB(x, y, averagedRGB);
			}
		}
		return normal;
	}
	private static int getAverageRGB(int[] rgbValues) {
		int[] avgValues = new int[4];
		int finalVal = 0;
		for(int i = 0; i < avgValues.length; i++) {
			int offset = i*8;
			for(int rgbValue : rgbValues) {
				int val = (rgbValue >> offset) & 0xFF;
				avgValues[i] += val;
			}
			avgValues[i] /= rgbValues.length;
			finalVal = finalVal | (avgValues[i] << offset);
		}
		return finalVal;
	}
}
