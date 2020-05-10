package ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import ui.*;
import utils.*;
import world.*;


public class Frame extends JPanel{
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	int GUIWIDTH = 400;
	
	private Timer timmy;
	private JPanel panel;
	private JFrame frame;
	private JPanel gamepanel;
	private JPanel minimapPanel;
	private JPanel cityView;
	private JPanel tileView;
	JLabel ironOre; 
	private JComboBox<MapType> mapType;
	private JLabel[] resourceIndicators = new JLabel[ResourceType.values().length];
	private JTextField mapSize;
	private int WIDTH;
	private int HEIGHT;
	private Game gameInstance;
	private int mx;
	private int my;
	private JPanel gui;
	private boolean dragged = false;
	
	private Thread gameLoopThread;
	
	public Frame() {
	
		frame = new JFrame("Civilization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Utils.loadImage("resources/Images/logo.png"));
		
		HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height * 8/9;
		WIDTH = HEIGHT + GUIWIDTH;
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		
		gameInstance = new Game(new GUIController() {
			
			@Override
			public void toggleCityView() {
				cityView.setVisible(!cityView.isVisible());
			}
			@Override
			public void toggleTileView() {
				tileView.setVisible(!tileView.isVisible());
			}
			@Override
			public void updateGUI() {
				for(int i = 0; i < ResourceType.values().length; i++) {
					resourceIndicators[i].setText(ResourceType.values()[i]+" = "+gameInstance.getResourceAmount(ResourceType.values()[i]) );
				}
					
				
			}
		});
			
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					menu();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}
			}
		});
	}
	
	private void menu() {
		panel = new JPanel();
		JButton start = new JButton("Start Game");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String sizeString = mapSize.getText();
					int size = Integer.parseInt(sizeString);
					gameInstance.generateWorld((MapType) mapType.getSelectedItem(), size);
					runGame();
				}
				catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		start.setPreferredSize(new Dimension(100,50));
		panel.add(start);
		
		mapType = new JComboBox<>(MapType.values());
		mapType.setPreferredSize(new Dimension(100,50));
		panel.add(mapType);
		
		mapSize = new JTextField("128", 10);
		mapSize.setPreferredSize(new Dimension(100,50));
		panel.add(mapSize);
		

		
		
		JButton exit = new JButton("exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});

		exit.setPreferredSize(new Dimension(100,50));
		panel.add(exit);
		
		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocusInWindow();
		frame.setPreferredSize(new Dimension(WIDTH,HEIGHT));
	}
	
	private void runGame() {
		System.err.println("Starting Game");
		frame.remove(panel);
		
		
		
		gamepanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
		        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				gameInstance.drawGame(g);
				g.setColor(Color.black);
				g.drawRect(-1, 0, getWidth()+1, getHeight());
			}
		};
		gamepanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
	        }
		});
		int MINIMAPBORDERWIDTH = 50;
		minimapPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth(), getHeight());
				gameInstance.drawMinimap(g, MINIMAPBORDERWIDTH, MINIMAPBORDERWIDTH, minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH,  minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
			}
		};
		minimapPanel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double)e.getX() - MINIMAPBORDERWIDTH) / (minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH);
				double ratioy = ((double)e.getY() - MINIMAPBORDERWIDTH) / (minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
		minimapPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double)e.getX() - MINIMAPBORDERWIDTH) / (minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH);
				double ratioy = ((double)e.getY() - MINIMAPBORDERWIDTH) / (minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});

		gui = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth(), getHeight());
			}
		};
		gui.setPreferredSize(new Dimension(GUIWIDTH,frame.getHeight()));
		
		Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
		int BUILDING_ICON_SIZE = 25;
		Insets zeroMargin = new Insets(0,0,0,0);
		
		JButton makeRoad = new JButton("Make Road", Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/road_icon.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		makeRoad.setMargin(zeroMargin);
		makeRoad.setPreferredSize(BUILDING_BUTTON_SIZE);
		makeRoad.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.ROAD);
		});
		JButton makeWall = new JButton("Make Wall", Utils.resizeImageIcon(BuildingType.WALL_BRICK.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		makeWall.setMargin(zeroMargin);
		makeWall.setPreferredSize(BUILDING_BUTTON_SIZE);
		makeWall.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.WALL);
		});
		
		JButton buildMine = new JButton("Build Mine", Utils.resizeImageIcon(BuildingType.MINE.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildMine.setMargin(zeroMargin);
		buildMine.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildMine.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.MINE);
		});
		
		JButton buildBarracks = new JButton("Build Barracks", Utils.resizeImageIcon(StructureType.BARRACKS.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildBarracks.setMargin(zeroMargin);
		buildBarracks.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildBarracks.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.BARRACKS);
		});
		JButton buildIrrigation = new JButton("Irrigate", Utils.resizeImageIcon(BuildingType.IRRIGATION.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildIrrigation.setMargin(zeroMargin);
		buildIrrigation.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildIrrigation.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.IRRIGATE);
		});
		JButton buildWorker = new JButton("Build Worker", Utils.resizeImageIcon(UnitType.WORKER.getImageIcon(), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildWorker.setMargin(zeroMargin);
		buildWorker.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildWorker.addActionListener(e -> {
			gameInstance.buildUnit(UnitType.WORKER);
		});
		JButton buildWarrior = new JButton("Build Warrior", Utils.resizeImageIcon(UnitType.WARRIOR.getImageIcon(), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildWarrior.setMargin(zeroMargin);
		buildWarrior.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildWarrior.addActionListener(e -> {
			gameInstance.buildUnit(UnitType.WARRIOR);
		});
		JButton buildSpearman = new JButton("Build Spearman", Utils.resizeImageIcon(UnitType.SPEARMAN.getImageIcon(), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
		buildSpearman.setMargin(zeroMargin);
		buildSpearman.setPreferredSize(BUILDING_BUTTON_SIZE);
		buildSpearman.addActionListener(e -> {
			gameInstance.buildUnit(UnitType.SPEARMAN);
		});
//		JButton exitCity = new JButton("Exit City", Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/exitbutton.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE));
//		exitCity.setMargin(zeroMargin);
//		exitCity.setPreferredSize(BUILDING_BUTTON_SIZE);
//		exitCity.addActionListener(e -> {
//			gameInstance.exitCity();
//		});
		JLabel money = new JLabel(Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/coin_icon.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE)); 
		//money.setFont(new Font("Verdana",1,20));
		money.setText("Gold = "+gameInstance.getMoney());
		money.setPreferredSize(BUILDING_BUTTON_SIZE);
		money.setBorder(BorderFactory.createLineBorder(Color.gray));
		money.setHorizontalAlignment(JLabel.CENTER);
		
		
		for(int i = 0; i < ResourceType.values().length; i++) {
			resourceIndicators[i] = new JLabel();
			resourceIndicators[i].setPreferredSize(BUILDING_BUTTON_SIZE);
			resourceIndicators[i].setBorder(BorderFactory.createLineBorder(Color.gray));
			resourceIndicators[i].setHorizontalAlignment(JLabel.CENTER);
		}
		
		JLabel tSize = new JLabel(); 
		tSize.setText("TileSize = "+gameInstance.getTileSize());
		tSize.setPreferredSize(BUILDING_BUTTON_SIZE);
		tSize.setBorder(BorderFactory.createLineBorder(Color.gray));
		tSize.setHorizontalAlignment(JLabel.CENTER);

		JToggleButton showHeightMap = new JToggleButton("Show Height Map");
		showHeightMap.setPreferredSize(BUILDING_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameInstance.setShowHeightMap(showHeightMap.isSelected());
		});
		
		makeRoad.setFocusable(false);
		makeWall.setFocusable(false);
		buildMine.setFocusable(false);
		buildBarracks.setFocusable(false);
		buildIrrigation.setFocusable(false);
//		exitCity.setFocusable(false);
		showHeightMap.setFocusable(false);
		
		
		JToggleButton flipTable = new JToggleButton("Flip Table");
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});
		flipTable.setPreferredSize(BUILDING_BUTTON_SIZE);
		
		JButton makeItRain = new JButton("Rain");
		makeItRain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.rain();
				gameInstance.world.grow();
			}
		});
		makeItRain.setPreferredSize(BUILDING_BUTTON_SIZE);

		JToggleButton debug = new JToggleButton(Game.DEBUG_DRAW ? "Stop Debug" : "Debug");
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Game.DEBUG_DRAW = debug.isSelected();
				debug.setText(Game.DEBUG_DRAW ? "Stop Debug" : "Debug");
			}
		});
		debug.setPreferredSize(BUILDING_BUTTON_SIZE);
		
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});
		exit.setPreferredSize(BUILDING_BUTTON_SIZE);

		
		for(JLabel label : resourceIndicators) {
			gui.add(label);
		}
		gui.add(money);
		gui.add(tSize);
		gui.add(makeRoad);
		gui.add(makeWall);
		gui.add(buildMine);
		gui.add(buildBarracks);
		gui.add(buildIrrigation);
		gui.add(showHeightMap);
		gui.add(flipTable);
		gui.add(makeItRain);
		gui.add(debug);
		gui.add(exit);
		
		makeItRain.setFocusable(false);
		flipTable.setFocusable(false);
		
		JPanel guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH,frame.getHeight()));
		guiSplitter.add(gui,BorderLayout.CENTER);
		
		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH,GUIWIDTH));
		guiSplitter.add(minimapPanel,BorderLayout.SOUTH);
		
		Image cityOverlay = Utils.loadImage("resources/Images/interfaces/backgroundbuild.png");
		cityView = new JPanel() {
		    protected void paintComponent(Graphics g) {
	            g.drawImage(cityOverlay, 0, 0, gamepanel.getWidth(), gamepanel.getHeight(), null);
		    	super.paintComponent(g);
		    }
		};
		cityView.setOpaque(false);
		cityView.setLayout(null);
		int numButtons = 0;
		cityView.add(buildWorker);
		buildWorker.setBounds(765,  185 + (BUILDING_BUTTON_SIZE.height)*(++numButtons-1) +5*numButtons , BUILDING_BUTTON_SIZE.width, BUILDING_BUTTON_SIZE.height);
		cityView.add(buildWarrior);
		buildWarrior.setBounds(765, 185 + (BUILDING_BUTTON_SIZE.height)*(++numButtons-1) +5*numButtons, BUILDING_BUTTON_SIZE.width, BUILDING_BUTTON_SIZE.height);
		cityView.add(buildSpearman);
		buildSpearman.setBounds(765, 185 + (BUILDING_BUTTON_SIZE.height)*(++numButtons-1) +5*numButtons, BUILDING_BUTTON_SIZE.width, BUILDING_BUTTON_SIZE.height);
//		cityView.add(exitCity);
//		exitCity.setBounds(790, 22 , BUILDING_BUTTON_SIZE.width, BUILDING_BUTTON_SIZE.height);
		
		frame.getContentPane().add(gamepanel,BorderLayout.CENTER);
		frame.getContentPane().add(guiSplitter,BorderLayout.EAST);
		frame.setGlassPane(cityView);
		frame.pack();
		frame.setVisible(true);
		gamepanel.requestFocusInWindow();
		gamepanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				//+1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(),mx,my);
				tSize.setText("TileSize = " + gameInstance.getTileSize());
			}
		});
		gamepanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				mx = e.getX();
				my = e.getY();
				gameInstance.mouseOver(mx, my);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
//				System.out.println("mouse drag");
				dragged = true;
				if(SwingUtilities.isRightMouseButton(e)) {
					int mrx = e.getX();
					int mry = e.getY();
					int dx = mx-mrx;
					int dy = my-mry;
					gameInstance.shiftView(dx, dy);
					mx = mrx;
					my = mry;
					
					gameInstance.mouseOver(mx, my);
				}else if(SwingUtilities.isLeftMouseButton(e)) {
					int mx2 = e.getX();
					int my2 = e.getY();
					
					gameInstance.mouseOver(mx2, my2);
					gameInstance.selectBox(mx,my, mx2, my2);
					
				}
					
				
				
			}
		});
		
		
		gamepanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
//				System.out.println("mouse release");
				if(e.getButton() == MouseEvent.BUTTON1 && dragged == false) {
					
					System.out.println("click");
					gameInstance.mouseClick(mx, my);
				}
				if(e.getButton() == MouseEvent.BUTTON2 && dragged == false) {
					
					System.out.println("right click");
					gameInstance.rightClick(mx, my);
				}
				
				dragged = false;
				
				gameInstance.resetHoveredArea();
			}

			@Override
			public void mousePressed(MouseEvent e) {
//				System.out.println("mousepressed");
				mx = e.getX();
				my = e.getY();
				
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
//					System.out.println("x: "+e.getX());
//					System.out.println("y: "+e.getY());
					gameInstance.doubleClick(mx, my);
				}
			}
		});
		
		gamepanel.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				//esc closes game
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
					exitGame();
				}
				//r rotates
				if (e.getKeyCode()==KeyEvent.VK_R) {
					gameInstance.rotateBlock();
				}
				
				
			}
		});
	
		timmy = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
		});
		timmy.start();

		frame.repaint();
		gamepanel.requestFocus();
		
		gameLoopThread = new Thread(() -> {
			try {
				while(true) {
					gameInstance.gameTick();
					Thread.sleep(100);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		gameLoopThread.start();
	}
	
	public void exitGame() {
		System.exit(0);
	}
}
