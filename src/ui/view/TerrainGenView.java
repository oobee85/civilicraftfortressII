package ui.view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.JPanel;

import ui.*;

public class TerrainGenView {

	private JPanel content;
	
	private static LinkedList<String> titles = new LinkedList<>();
	private static HashMap<String, ImagePanel> images = new HashMap<>();
	private static JPanel imagesPanel = new JPanel();
	public static void addMap(float[][] map, String title) {
		double[][] map2 = new double[map.length][map[0].length];
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				map2[y][x] = map[y][x];
			}
		}
		addMap(map2, title);
	}
	public static void addMap(double[][] map, String title) {
		if (!images.containsKey(title)) {
			titles.add(title);
		}
		if (images.containsKey(title)) {
			imagesPanel.remove(images.get(title));
		}
		images.put(title, new ImagePanel(title, toImage(map)));
		imagesPanel.add(images.get(title));
		imagesPanel.revalidate();
	} 

	public TerrainGenView(ActionListener backCallback) {
		content = new JPanel();
		content.setBackground(Color.black);
		content.setLayout(new BorderLayout());
		KButton backButton = KUIConstants.setupButton("Back", null, DebugView.DEBUG_BUTTON_SIZE);
		backButton.addActionListener(backCallback);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(backButton);
		
		content.add(buttonPanel, BorderLayout.NORTH);
		
		imagesPanel.setLayout(new GridLayout(0, 4));
		
		content.add(imagesPanel, BorderLayout.CENTER);
		
	}
	public JPanel getContentPanel() {
		return content;
	}
	
	private static BufferedImage toImage(double[][] map) {
		double low = map[0][0];
		double high = map[0][0];
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				low = Math.min(low, map[y][x]);
				high = Math.max(high, map[y][x]);
			}
		}
		Color[] colors = new Color[] {
				new Color(255 * 0/5, 255 * 0/5, 255 * 0/5),
				new Color(255 * 1/5, 255 * 1/5, 255 * 1/5),
				new Color(255 * 2/5, 255 * 2/5, 255 * 2/5),
				new Color(255 * 5/10, 255 * 5/10, 255 * 5/10),
				new Color(255 * 3/5, 255 * 3/5, 255 * 3/5),
				new Color(255 * 7/10, 255 * 7/10, 255 * 7/10),
				new Color(255 * 4/5, 255 * 4/5, 255 * 4/5),
				new Color(255 * 9/10, 255 * 9/10, 255 * 9/10),
				new Color(255 * 5/5, 255 * 5/5, 255 * 5/5),
		};
		BufferedImage image = new BufferedImage(map[0].length, map.length, BufferedImage.TYPE_3BYTE_BGR);
		
		double highest = map[0][0];
		int highestx = 0;
		int highesty = 0;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				double ratio = (double) (map[y][x] - low) / (high - low);
				Color c = colors[(int)(ratio * (colors.length - 1))];
				image.setRGB(y, x, c.getRGB());
				if (map[y][x] > highest) {
					highest = map[y][x];
					highesty = y;
					highestx = x;
				}
			}
		}
		image.setRGB(highesty, highestx, Color.red.getRGB());
		return image;
	}
}

