package world;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import utils.Utils;

public class WorldGenViewer {
	
	private static final int DEFAULT_SCALE = 5;
	private static final long DEFAULT_SEED = 123;
	
	private static final int IMAGE_SIZE = 500;
	
	private static final  int[][] DIAGONAL_NEIGHBORS = {
			{-1, -1},
			{-1,  0},
			{-1,  1},
			{ 0,  1},
			{ 1,  1},
			{ 1,  0},
			{ 1, -1},
			{ 0, -1},
	};
	private static final  int[][] NO_DIAGONAL_NEIGHBORS = {
			{-1,  0},
			{ 0,  1},
			{ 1,  0},
			{ 0, -1},
	};
	
	private JFrame frame;
	private JPanel menuPanel;
	private JPanel viewPanel;
	
	private JPanel mapPanel1;
	private ArrayList<BufferedImage> images = new ArrayList<>();

	private JLabel infoField;
	private JTextField seedField;
	private JTextField scaleField;
	private JToggleButton doDiagonals;
	private JButton generateButton;
	
	public WorldGenViewer() {
		frame = new JFrame("World Gen Viewer");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		
		
		mapPanel1 = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int xoffset = 0;
				int yoffset = 0;
				int totalImages = images.size();
				int numPerRow = totalImages/2;
				int maxHeight = 0;
				
				int column = 0;
				ArrayList<BufferedImage> localCopy = null;
				synchronized (images) {
					localCopy = new ArrayList<>(images);
				}
				for (BufferedImage image : localCopy) {
					maxHeight = Math.max(maxHeight, image.getHeight());
					maxHeight = Math.min(maxHeight, mapPanel1.getHeight()/2);
					g.drawImage(image, xoffset, yoffset, maxHeight, maxHeight, null);
					xoffset += maxHeight;
					column++;
					if (column >= numPerRow) {
						column = 0;
						xoffset = 0;
						yoffset = maxHeight;
						maxHeight = 0;
					}
				}
			}
		};
		
		scaleField = new JTextField(DEFAULT_SCALE + "", 10);
		seedField = new JTextField(DEFAULT_SEED + "", 10);
		doDiagonals = new JToggleButton("Do diagonals");
		
		generateButton = new JButton("Generate");
		generateButton.addActionListener(e -> {
			regenerateMaps();
			frame.repaint();
		});

		JButton generateButton2 = new JButton("Glom");
		generateButton2.addActionListener(e -> {
			synchronized (images) {
				images.clear();
			}
			Thread generateThread = new Thread(() -> {
				regenerateMaps2();
			});
			generateThread.start();
			frame.repaint();
		});
		
		menuPanel = new JPanel();
		viewPanel = new JPanel();
		viewPanel.setLayout(new BorderLayout());
		frame.add(menuPanel, BorderLayout.NORTH);
		frame.add(viewPanel, BorderLayout.CENTER);
		
		infoField = new JLabel();
		menuPanel.add(infoField);
		menuPanel.add(generateButton);
		menuPanel.add(generateButton2);
		menuPanel.add(new JLabel("Seed"));
		menuPanel.add(seedField);
		menuPanel.add(new JLabel("Scale"));
		menuPanel.add(scaleField);
		menuPanel.add(doDiagonals);
		
		viewPanel.add(mapPanel1);
		
		frame.setVisible(true);
		
		Thread repaintingThread = new Thread(() -> {
			try {
				while(true) {
					mapPanel1.repaint();
					Thread.sleep(100);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		repaintingThread.start();
	}

	private long parseSeed() {
		try {
			return Long.parseLong(seedField.getText());
		}
		catch (NumberFormatException e) {
			return DEFAULT_SEED;
		}
	}
	
	private int parseScale() {
		try {
			return Integer.parseInt(scaleField.getText());
		}
		catch (NumberFormatException e) {
			return DEFAULT_SCALE;
		}
	}
	
	private void makeImage(int[][] map) {
		int size = map.length;
		int scale = 1;
		while (map.length * (scale+1) < IMAGE_SIZE && scale < 10) {
			scale = scale + 1;
			size = map.length * scale;
		}
		BufferedImage mapImage1 = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
		double max = Integer.MIN_VALUE;
		double min = Integer.MAX_VALUE;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				max = Math.max(max, map[y][x]);
				min = Math.min(min, map[y][x]);
			}
		}
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int value = 0;
				if (map[y/scale][x/scale] != min) {
					value = (int) (160 + 95 * (map[y/scale][x/scale] - min) / (max - min));
				}
				Color color = new Color(value, value, value);
				mapImage1.setRGB(x, y, color.getRGB());
			}
		}
		synchronized (images) {
			images.add(mapImage1);
		}
		System.out.println("max: " + max + ", min: " + min);
	}
	
	private void makeImage(double[][] map) {
		// map must have values from -1 to 1
		BufferedImage mapImage1 = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_BYTE_GRAY);
		double max = -127;
		double min = 128;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				max = Math.max(max, map[y][x]);
				min = Math.min(min, map[y][x]);
				double value0to1 = (map[y][x]*.5 + .5);
				
				int value = (int) (Math.sqrt(Math.sqrt(value0to1)) * 255);
				Color color = new Color(value, value, value);
				mapImage1.setRGB(x, y, color.getRGB());
			}
		}
		synchronized (images) {
			images.add(mapImage1);
		}
		System.out.println(max);
		System.out.println(min);
	}
	
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
	
	public double[][] generateMap3(
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
				infoField.setText("Iteration: " + iteration + ", pixel " + i + "/" + totalPixels);
			}
			old.put(size, start);
			makeImage(start);

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
			makeImage(start);
		}
		distribute(start, size, neighbors);
		makeImage(start);

		for (int y = 0; y < height; y++) { 
			for (int x = 0; x < width; x++) {
				map[y][x] = start[(int)(size * y / height)][(int)(size * x / width)];
			}
		}

		Utils.normalize(map, maxValue, minValue);
		makeImage(map);
		return map;
	}

	private void regenerateMaps2() {
		if (doDiagonals.isSelected()) {
			double[][] map = generateMap3(
					parseSeed(),
					IMAGE_SIZE,
					IMAGE_SIZE,
					-1, 1, parseScale(), DIAGONAL_NEIGHBORS);
		}
		double[][] map2 = generateMap3(
				parseSeed(),
				IMAGE_SIZE,
				IMAGE_SIZE,
				-1, 1, parseScale(), NO_DIAGONAL_NEIGHBORS);
//		makeImage(map);
	}
	
	private void regenerateMaps() {
		double[][] map = Generation.generateMap2(
				parseSeed(),
				IMAGE_SIZE,
				IMAGE_SIZE,
				parseScale(),
				-1, 1);
		makeImage(map);
	}
	
	public static void main(String[] args) {
		new WorldGenViewer();
	}

}
