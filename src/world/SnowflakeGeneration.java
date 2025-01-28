package world;

import java.util.*;
import java.util.Map.Entry;

import utils.*;

public class SnowflakeGeneration {

	private void addPixel(Random rand, int[][] start, int size, int[][] neighbors) {
		int x = rand.nextInt(size);
		int y = rand.nextInt(size);
		while(start[x][y] != 0) {
			x = rand.nextInt(size);
			y = rand.nextInt(size);
		}
		while(true) {
			int neighborSum = 0;
			for (int[] neighbor : neighbors) {
				int newx = x + neighbor[0];
				int newy = y + neighbor[1];
				if (newx >= 0 && newx < size && newy >= 0 && newy < size) {
					neighborSum += start[newx][newy];
				}
			}
			if (neighborSum != 0) {
				break;
			}
			while (true) {
				int[] direction = neighbors[rand.nextInt(neighbors.length)];
				int newx = x + direction[0];
				int newy = y + direction[1];
				if (newx >= 0 && newx < size && newy >= 0 && newy < size) {
					x = newx;
					y = newy;
					break;
				}
			}
		}
		start[x][y] += 1;
	}
	
	private void distribute(int[][] start, int size, int[][] neighbors) {
		boolean changed = true;
		while(changed) {
			changed = false;
			for (int y = 0; y < start.length; y++) {
				for (int x = 0; x < start.length; x++) {
					for (int[] neighbor : neighbors) {
						int newx = x + neighbor[0];
						int newy = y + neighbor[1];
						if (newx >= 0 && newx < size && newy >= 0 && newy < size) {
							if (start[newy][newx] - start[y][x] >= 2) {
								start[y][x] = start[newy][newx] - 1;
								changed = true;
							}
						}
					}
				}
			}
		}
	}
	private static final  int[][] NO_DIAGONAL_NEIGHBORS = {
			{-1,  0},
			{ 0,  1},
			{ 1,  0},
			{ 0, -1},
	};
	public double[][] generate(
			long seed,
			int width,
			int height,
			double minValue,
			double maxValue) {
		return generateMap3(seed, width, height, minValue, maxValue, Settings.NUM_ITERATIONS_SNOWFLAKE, NO_DIAGONAL_NEIGHBORS);
	}
	
	private double[][] generateMap3(
			long seed,
			int width,
			int height,
			double minValue,
			double maxValue,
			int numIterations,
			int[][] neighbors) {
		Random rand = new Random(seed);
		double[][] map = new double[height][width];
		
		int size = 15;
		int[][] start = new int[size][size];
		start[size/2][size/2] = 1;
		int iteration = 0;
		HashMap<Integer, int[][]> old = new HashMap<>();
		while (iteration < numIterations) {
			int totalPixels = size*size/10;
			for (int i = 0; i < totalPixels; i++) {
				addPixel(rand, start, size, neighbors);
//				infoField.setText("Iteration: " + iteration + ", pixel " + i + "/" + totalPixels);
			}
			old.put(size, start);
//			makeImage(start);

			iteration++;
			if (iteration >= numIterations) {
				break;
			}

			size = size*2;
			start = new int[size][size];
			for (Entry<Integer, int[][]> oldstart : old.entrySet()) {
				int oldsize = oldstart.getKey();
				int offset = 0;
				while(oldsize < size) {
					offset += oldsize/2;
					oldsize = oldsize*2;
				}
				for (int y = 0; y < oldstart.getKey(); y++) {
					for (int x = 0; x < oldstart.getKey(); x++) {
						int existingValue = oldstart.getValue()[y][x];
						if (existingValue == 0) {
							continue;
						}
						start[y+offset][x+offset] = Math.max(start[y+offset][x+offset], existingValue + 2);
					}
				}
			}
//			distribute(start, size, neighbors);
//			makeImage(start);
		}
		distribute(start, size, neighbors);
//		makeImage(start);

		for (int y = 0; y < height; y++) { 
			for (int x = 0; x < width; x++) {
				map[y][x] = start[(int)(size * y / height)][(int)(size * x / width)];
			}
		}

		Utils.normalize(map, maxValue, minValue);
//		makeImage(map);
		return map;
	}
}
