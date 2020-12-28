package world;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import utils.*;

public class PerlinNoise {
	
	private static final double interpolate2(double a0, double a1, double w) {
		if (0.0 > w) return a0;
		if (1.0 < w) return a1;
		return (a1 - a0) * w + a0;
	}
	private static final double interpolate(double a0, double a1, double w) {
		if (0.0 > w) return a0;
		if (1.0 < w) return a1;
//		return (a1 - a0) * w + a0;
		return (a1 - a0) * (3.0 - w * 2.0) * w * w + a0;
	}

	// From https://en.wikipedia.org/wiki/Perlin_noise
	private static final Vec2 randomGradient(int ix, int iy) {
		// Random float. No precomputed gradients mean this works for any number of grid coordinates
		double random = 2920 * Math.sin(ix * 21942.0 + iy * 171324.0 + 8912.0) * Math.cos(ix * 23157.0 * iy * 217832.0 + 9758.0);
		return new Vec2(Math.cos(random), Math.sin(random));
	}
	
	// Computes the dot product of the distance and gradient vectors.
	private static final double dotGridGradient(int ix, int iy, double x, double y) {
		// Get gradient from integer coordinates
		Vec2 gradient = randomGradient(ix, iy);

		// Compute the distance vector
		double dx = x - (double) ix;
		double dy = y - (double) iy;

		// Compute the dot-product
		return (dx * gradient.x + dy * gradient.y);
	}
	
	// Compute Perlin noise at coordinates x, y
	private static final double perlin(double x, double y) {
		// Determine grid cell coordinates
		int x0 = (int) x;
		int x1 = x0 + 1;
		int y0 = (int) y;
		int y1 = y0 + 1;

		// Determine interpolation weights
		// Could also use higher order polynomial/s-curve here
		double sx = x - (double) x0;
		double sy = y - (double) y0;

		// Interpolate between grid point gradients
		double n0, n1, ix0, ix1, value;

		n0 = dotGridGradient(x0, y0, x, y);
		n1 = dotGridGradient(x1, y0, x, y);
		ix0 = interpolate(n0, n1, sx);

		n0 = dotGridGradient(x0, y1, x, y);
		n1 = dotGridGradient(x1, y1, x, y);
		ix1 = interpolate(n0, n1, sx);

		value = interpolate(ix0, ix1, sy);
		return value;
	}
	
	public static float[][] generateHeightMap(int width, int height) {
		
		int numoctaves = 8;
		double amplitude = 0.5;
		int frequency = 1;
		float[][] heightmap = new float[height][width];
		for(int octave = 0; octave < numoctaves; octave++) {
//			if(octave > 4) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						double nx = 1.0*x / width + 1;
						double ny = 1.0*y / height;
						heightmap[y][x] += (float)(amplitude * perlin( frequency*nx, frequency*ny));
					}
				}
//			}
			frequency *= 2;
			amplitude *= 0.7;
		}
		Utils.normalize(heightmap);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				heightmap[y][x] = (float) Math.pow(heightmap[y][x], 0.5);
			}
		}
		Utils.normalize(heightmap);
		saveImage(heightmap);
		return heightmap;
	}
	
	private static final void saveImage(float[][] map) {
		BufferedImage image = new BufferedImage(map.length, map[0].length, BufferedImage.TYPE_BYTE_GRAY);
		for(int y = 0; y < map.length; y++) {
			for(int x = 0; x < map[y].length; x++) {
				int value = (int) (map[y][x]*255);
				image.setRGB(y, x, new Color(value, value, value).getRGB());
			}
		}
		try {
			ImageIO.write(image, "png", new File("map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
