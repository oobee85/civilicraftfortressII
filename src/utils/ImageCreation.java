package utils;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;

public class ImageCreation {
	
	public static List<Direction[]> getAllDirectionCombinations() {
		List<Direction[]> result = new LinkedList<Direction[]>();
		for (int i = 0; i < Direction.values().length; i++) {
			Direction a = Direction.values()[i];
			result.add(new Direction[] {a});
			for (int j = i + 1; j < Direction.values().length; j++) {
				Direction b = Direction.values()[j];
				result.add(new Direction[] {a, b});
				for (int k = j + 1; k < Direction.values().length; k++) {
					Direction c = Direction.values()[k];
					result.add(new Direction[] {a, b, c});
					for (int l = k + 1; l < Direction.values().length; l++) {
						Direction d = Direction.values()[l];
						result.add(new Direction[] {a, b, c, d});
					}
				}
			}
		}
		return result;
	}
	
	public static void createRoadImages(String roadtilefile) {
		try {
			BufferedImage roadtile = ImageIO.read(new File(roadtilefile));
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
				g.drawImage(roadtile, centerx, centery, null);
				String filename = "road_";
				for(Direction dir : arr) {
					filename += dir;
					TileLoc delta = dir.getDelta();
					g.drawImage(roadtile, centerx + delta.x*width, centery + delta.y*width, null);
					g.drawImage(roadtile, centerx + delta.x*width*3/2, centery + delta.y*width*3/2, null);
				}
				filename += ".png";
				g.dispose();
				ImageIO.write(target, "png", new File(filename));
			}
			
//			for(Direction a : Direction.values()) {
//				for(Direction b : Direction.values()) {
//					for(Direction c : Direction.values()) {
//						for(Direction d : Direction.values()) {
//							
//							BufferedImage target = new BufferedImage(roadimagewidth, roadimageheight, BufferedImage.TYPE_INT_ARGB);
//							Graphics2D g = target.createGraphics();
//							g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//							g.drawImage(roadtile, centerx, centery, null);
//							
//							Set<Direction> directions = new HashSet<>();
//							directions.add(a);
//							directions.add(b);
//							directions.add(c);
//							directions.add(d);
//							
//							String filename = "road_";
//							for(Direction dir : Direction.values()) {
//								if(directions.contains(dir)) {
//									filename += dir;
//									TileLoc delta = dir.getDelta();
//									g.drawImage(roadtile, centerx + delta.x*width, centery + delta.y*width, null);
//									g.drawImage(roadtile, centerx + delta.x*width*3/2, centery + delta.y*width*3/2, null);
//								}
//							}
//							
//							filename += ".png";
//							g.dispose();
//							ImageIO.write(target, "png", new File(filename));
//							
//						}
//					}
//				}
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
