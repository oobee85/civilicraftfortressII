package world;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class WorldGenViewer {
	
	private static final int DEFAULT_SCALE = 60;
	private static final long DEFAULT_SEED = 123;
	
	private JFrame frame;
	private JPanel menuPanel;
	private JPanel viewPanel;
	
	private JPanel mapPanel1;
	private BufferedImage mapImage1;

	private JTextField seedField;
	private JTextField scaleField;
	private JButton generateButton;
	
	public WorldGenViewer() {
		frame = new JFrame("World Gen Viewer");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		
		mapImage1 = new BufferedImage(500, 500, BufferedImage.TYPE_BYTE_GRAY);
		
		mapPanel1 = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(mapImage1, 0, 0, this.getWidth(), this.getHeight(), null);
			}
		};
		
		scaleField = new JTextField(DEFAULT_SCALE + "", 10);
		seedField = new JTextField(DEFAULT_SEED + "", 10);
		
		generateButton = new JButton("Generate");
		generateButton.addActionListener(e -> {
			regenerateMaps();
			frame.repaint();
		});
		
		menuPanel = new JPanel();
		viewPanel = new JPanel();
		viewPanel.setLayout(new BorderLayout());
		frame.add(menuPanel, BorderLayout.NORTH);
		frame.add(viewPanel, BorderLayout.CENTER);
		
		menuPanel.add(generateButton);
		menuPanel.add(new JLabel("Seed"));
		menuPanel.add(seedField);
		menuPanel.add(new JLabel("Scale"));
		menuPanel.add(scaleField);
		
		viewPanel.add(mapPanel1);
		
		frame.setVisible(true);
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
	
	private void regenerateMaps() {
		mapImage1 = new BufferedImage(mapPanel1.getWidth(), mapPanel1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		double[][] map = Generation.generateMap2(
				parseSeed(),
				mapPanel1.getWidth(),
				mapPanel1.getHeight(),
				parseScale(),
				-1, 1);
		double max = -100;
		double min = 100;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				max = Math.max(max, map[y][x]);
				min = Math.min(min, map[y][x]);
				int value = (int) ((map[y][x]*.5 + .5) * 255);
				Color color = new Color(value, value, value);
				mapImage1.setRGB(x, y, color.getRGB());
			}
		}
		System.out.println(max);
		System.out.println(min);
	}
	
	public static void main(String[] args) {
		new WorldGenViewer();
	}

}
