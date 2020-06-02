package ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import game.*;
import utils.*;
import world.*;


public class Frame extends JPanel{
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	int GUIWIDTH = 400;
	int MINIMAPBORDERWIDTH = 50;
	
	public static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	public static final Dimension BUILD_UNIT_BUTTON_SIZE = new Dimension(170, 35);

	Insets zeroMargin = new Insets(0,0,0,0);
	
//	private static final String fontName = "Comic Sans MS";
//	private static final String fontName = "Chiller";
	private static final String fontName = "TW Cen MT";
	
	Font buttonFont = new Font(fontName, Font.PLAIN, 17);
	Font buttonFontSmall = new Font(fontName, Font.PLAIN, 14);
	Font buttonFontMini = new Font(fontName, Font.PLAIN, 13);
	
	Border massiveBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5));
	
	
	private ImageIcon BUILDING_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/buildwall.png"), 20, 20);
	
	private Timer repaintingThread;
	private JPanel mainMenuPanel;
	private JFrame frame;
	private JPanel gamepanel;
	private JPanel minimapPanel;
	private JPanel cityView;
	private JPanel tileView;
	private JPanel buildingMenu;
	private JPanel techView;
	private JLabel tileSize;
	private JTabbedPane tabbedPane;
	private JComboBox<MapType> mapType;
	private JLabel[] resourceIndicators = new JLabel[ItemType.values().length];
	private JButton[] researchButtons = new JButton[ResearchType.values().length];
	private JButton[] buildingButtons = new JButton[BuildingType.values().length];
	private JButton[] unitButtons = new JButton[UnitType.values().length];
	private JTextField mapSize;
	private int WIDTH;
	private int HEIGHT;
	private Game gameInstance;
	private int mx;
	private int my;
	private boolean dragged = false;
	
	private int RESOURCE_TAB;
	private int BUILDING_TAB;
	private int TECH_TAB;
	private int DEBUG_TAB;
	
	private Thread gameLoopThread;
	
	public Frame() {
	
		frame = new JFrame("Civilization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Utils.loadImage("resources/Images/logo.png"));
		
		HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height * 8/9;
		WIDTH = HEIGHT + GUIWIDTH;
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		GUIController guiController = new GUIController() {
			@Override
			public void toggleCityView() {
				System.out.println("toggle city view");
				frame.setGlassPane(cityView);
				cityView.setVisible(!cityView.isVisible());
				frame.repaint();
			}
			@Override
			public void selectedWorker(boolean selected) {
				if(selected) {
					addBuildingTab();
					tabbedPane.setSelectedIndex(BUILDING_TAB);
				}
				else {
					removeBuildingTab();
				}
				frame.repaint();
			}
			@Override
			public void toggleTileView() {
				tileView.setVisible(!tileView.isVisible());
			}
			
			@Override
			public void updateGUI() {
				for(int i = 0; i < ItemType.values().length; i++) {
					resourceIndicators[i].setText("" + gameInstance.getResourceAmount(ItemType.values()[i]) + " " + ItemType.values()[i]);
				}
				for(int i = 0; i < ResearchType.values().length; i++) {
					Research r = gameInstance.researches.get(ResearchType.values()[i]);
					boolean unlocked = r.isUnlocked();
					ResearchRequirement req = r.getRequirement();
					JButton button = researchButtons[i];
					if(unlocked) {
						button.setEnabled(false);
						button.setVisible(true);
					}
					else if(req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					}
					else if(req.areSecondLayerRequirementsMet()) {
						button.setEnabled(false);
						button.setVisible(true);
					}
					else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for(int i = 0; i < BuildingType.values().length; i++) {
					BuildingType type = BuildingType.values()[i];
					JButton button = buildingButtons[i];
					ResearchRequirement req = gameInstance.buildingResearchRequirements[type];
					if(req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					}
					else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for(int i = 0; i < UnitType.values().length; i++) {
					JButton button = unitButtons[i];
					if(button == null) {
						continue;
					}
					UnitType type = UnitType.values()[i];
					ResearchRequirement req = gameInstance.unitResearchRequirements[type];
					if(req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					}
					else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				frame.repaint();
			}
			
			@Override
			public void openRightClickMenu(int mx, int my, Tile tile) {
				System.out.println("trying to open right click menu");
				JPanel rightClickPanel = new JPanel() {
					@Override
					public void paintComponent(Graphics g){
						
						Graphics2D g2d = (Graphics2D) g; 
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1)); 
						g.drawImage(Utils.toBufferedImage(Utils.loadImage("resources/Images/interfaces/tileinfo.png")),0 ,0, null );
						super.paintComponent(g);
						
					}
				};
				rightClickPanel.setBackground(Color.red);
				rightClickPanel.setPreferredSize(new Dimension(193, 173));
				rightClickPanel.setLayout(null);
				rightClickPanel.setOpaque(false);

				int y = 48;
				
				JLabel terr = setupMiniLabel(tile.getTerrain().toString(), null, null);
				int fontSize = terr.getFont().getSize();
				rightClickPanel.add(terr);
				terr.setBounds(20, y += fontSize, 100, 100);

				if(tile.getHasResource()) {
					JLabel label = setupMiniLabel(tile.getResourceType().toString(), null, null);
					rightClickPanel.add(label);
					label.setBounds(20, y += fontSize, 100, 100);
				}
				
				if(tile.getBuilding() != null) {
					JLabel building = setupMiniLabel(tile.getBuilding().toString(), null, null);
					rightClickPanel.add(building);
					building.setBounds(20, y += fontSize, 100, 100);
				}
				if(tile.getRoadType() != null) {
					JLabel structure = setupMiniLabel(tile.getRoadType().toString(), null, null);
					rightClickPanel.add(structure);
					structure.setBounds(20, y += fontSize, 100, 100);
				}
				if(tile.getPlant() != null) {
					JLabel t = setupMiniLabel(tile.getPlant().getPlantType().toString(), null, null);
					rightClickPanel.add(t);
					t.setBounds(20, y += fontSize, 100, 100);
				}
				for(Unit u : tile.getUnits()) {
					JLabel a = setupMiniLabel(u.getUnitType().toString(), null, null);
					rightClickPanel.add(a);
					a.setBounds(20, y += fontSize, 100, 100);
				}
				
				JPopupMenu popup = new JPopupMenu() {
					@Override
					public void paintComponent(Graphics g) {
						Graphics2D g2d = (Graphics2D) g.create();
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
						super.paintComponent(g2d);
						g2d.dispose();
					}
				};
				popup.setLayout(new BorderLayout());
				popup.add(rightClickPanel);
				popup.setOpaque(false);
				popup.setBorderPainted(false);
				popup.show(gamepanel, mx-100, my-50);
				
			}
		};
		gameInstance = new Game(guiController);
			
		
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
	
	private JButton setupButton(String text, Icon icon, Dimension size) {
		JButton b = new KButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	private JToggleButton setupToggleButton(String text, Icon icon, Dimension size) {
		JToggleButton b = new KToggleButton(text, icon);
		b.setMargin(zeroMargin);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	private JLabel setupLabel(String text, Icon icon, Dimension size) {
		JLabel b = new JLabel(icon);
		b.setText(text);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		return b;
	}
	private JLabel setupMiniLabel(String text, Icon icon, Dimension size) {
		JLabel b = new JLabel(icon);
		b.setText(text);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		setComponentAttributes(b, size);
		b.setBorder(null);
		b.setFont(buttonFontMini);
		return b;
	}
	
	private void setComponentAttributes(JComponent c, Dimension size) {
		c.setFont(buttonFont);
		c.setBorder(massiveBorder);
		c.setFocusable(false);
		if(size != null)
			c.setPreferredSize(size);
	}
	
	private void menu() {
		mainMenuPanel = new JPanel();
		JButton start = setupButton("Start Game", null, BUILDING_BUTTON_SIZE);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					int size = Integer.parseInt(mapSize.getText());
					gameInstance.generateWorld((MapType) mapType.getSelectedItem(), size);
					runGame();
				}
				catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		mainMenuPanel.add(start);
		
		mapType = new JComboBox<>(MapType.values());
		setComponentAttributes(mapType, BUILDING_BUTTON_SIZE);
		mainMenuPanel.add(mapType);
		
		mapSize = new JTextField("128", 10);
		setComponentAttributes(mapSize, BUILDING_BUTTON_SIZE);
		mainMenuPanel.add(mapSize);
		
		
//		JButton exit = new JButton("exit");
//		exit.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				exitGame();
//			}
//		});
//
//		exit.setPreferredSize(new Dimension(100,50));
//		panel.add(exit);
		
		mainMenuPanel.setBackground(Color.WHITE);
		mainMenuPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.add(mainMenuPanel);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocusInWindow();
		frame.setPreferredSize(new Dimension(WIDTH,HEIGHT));
	}
	
	private void setupGamePanel() {
		gamepanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
		        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.setColor(gameInstance.getBackgroundColor());
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
		gamepanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				//+1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(),mx,my);
				tileSize.setText("TileSize = " + gameInstance.getTileSize());
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
				} else if(SwingUtilities.isLeftMouseButton(e)) {
					int mx2 = e.getX();
					int my2 = e.getY();
					gameInstance.mouseOver(mx2, my2);
				}
			}
		});
		
		gamepanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && dragged == false) {
					gameInstance.mouseClick(mx, my);
				}
				if(e.getButton() == MouseEvent.BUTTON3 && dragged == false) {
					gameInstance.rightClick(mx, my);
				}
				dragged = false;
			}
			@Override
			public void mousePressed(MouseEvent e) {
				mx = e.getX();
				my = e.getY();
			}
			@Override
			public void mouseExited(MouseEvent e) { }
			@Override
			public void mouseEntered(MouseEvent e) { }
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
					gameInstance.doubleClick(mx, my);
				}
			}
		});
		
		gamepanel.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) { }
			@Override
			public void keyReleased(KeyEvent e) { }
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
					gameInstance.deselectUnit();
				}
				if(e.getKeyCode()==KeyEvent.VK_R) {
					if(gameInstance.getSelectedUnit() != null) {
						gameInstance.buildRoad(RoadType.STONE_ROAD);
					}
				}
				if(e.getKeyCode()==KeyEvent.VK_M) {
					if(gameInstance.getSelectedUnit() != null) {
						gameInstance.buildBuilding(BuildingType.MINE);
					}
				}
				if(e.getKeyCode()==KeyEvent.VK_I) {
					if(gameInstance.getSelectedUnit() != null) {
						gameInstance.buildBuilding(BuildingType.IRRIGATION);
					}
				}
			}
		});
	}
	
	private void setupMinimapPanel() {
		minimapPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(gameInstance.getBackgroundColor());
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
	}
	
	private void addBuildingTab() {
		tabbedPane.insertTab("Build Stuff", BUILDING_TAB_ICON, buildingMenu, "Does nothing", BUILDING_TAB);
	}
	private void removeBuildingTab() {
		tabbedPane.removeTabAt(BUILDING_TAB);
	}
	
	private void runGame() {
		System.err.println("Starting Game");
		frame.remove(mainMenuPanel);
		
		Dimension RESOURCE_BUTTON_SIZE = new Dimension(200, 35);
		Dimension RESEARCH_BUTTON_SIZE = new Dimension(125, 35);
		int BUILDING_ICON_SIZE = 25;
		int RESOURCE_ICON_SIZE = 35;

		buildingMenu = new JPanel();
		
		JButton makeRoad = setupButton("Road", Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/buildroad.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), BUILDING_BUTTON_SIZE);
		makeRoad.addActionListener(e -> {
			gameInstance.buildRoad(RoadType.STONE_ROAD);
		});
		buildingMenu.add(makeRoad);
		
		for(int i = 0; i < BuildingType.values().length; i++) {
			BuildingType type = BuildingType.values()[i];
			JButton button = setupButton(type.toString(), Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), BUILDING_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameInstance.buildBuilding(type);
			});
			buildingButtons[i] = button;
			buildingMenu.add(button);
		}
		
		Image cityOverlay = Utils.loadImage("resources/Images/interfaces/backgroundbuild.png");
		cityView = new JPanel() {
			protected void paintComponent(Graphics g) {
				g.drawImage(cityOverlay, 0, 0, gamepanel.getWidth(), gamepanel.getHeight(), null);
				super.paintComponent(g);
			}
		};
		cityView.setVisible(false);
		cityView.setOpaque(false);
		cityView.setLayout(null);
		
		int numButtons = 0;
		for(int i = 0; i < UnitType.values().length; i++) {
			UnitType type = UnitType.values()[i];
			if(type.getCost() == null) {
				continue;
			}
			JButton button = setupButton("Build " + type.toString(), 
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), 
					null);
			button.addActionListener(e -> {
				gameInstance.buildUnit(type, gameInstance.buildings[0].getTile());
			});
			cityView.add(button);
			button.setBounds(765, 185 + (BUILD_UNIT_BUTTON_SIZE.height)*(++numButtons-1) +5*numButtons, BUILD_UNIT_BUTTON_SIZE.width, BUILD_UNIT_BUTTON_SIZE.height);
			unitButtons[i] = button;
		}
		
		JButton exitCity = setupButton("", Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/exitbutton.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), BUILDING_BUTTON_SIZE);
		exitCity.setBorder(null);
		exitCity.setContentAreaFilled(false);
		exitCity.addActionListener(e -> {
			gameInstance.exitCity();
		});
		cityView.add(exitCity);
		exitCity.setBounds(790, 20, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE);
		
		JLabel money = setupLabel("Gold = " + gameInstance.getMoney(), Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/coin_icon.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), BUILDING_BUTTON_SIZE);
		
		for(int i = 0; i < ItemType.values().length; i++) {
			resourceIndicators[i] = setupLabel("", Utils.resizeImageIcon(ItemType.values()[i].getImageIcon(0), RESOURCE_ICON_SIZE, RESOURCE_ICON_SIZE), RESOURCE_BUTTON_SIZE);
		}
		
		tileSize = setupLabel("TileSize = "+gameInstance.getTileSize(), null, BUILDING_BUTTON_SIZE);

		JToggleButton showHeightMap = setupToggleButton("Show Height Map", null, BUILDING_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameInstance.setShowHeightMap(showHeightMap.isSelected());
		});

		JToggleButton flipTable = setupToggleButton("Flip Table", null, BUILDING_BUTTON_SIZE);
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});
		
		JButton makeItRain = setupButton("Rain", null, BUILDING_BUTTON_SIZE);
		makeItRain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.rain();
				gameInstance.world.grow();
			}
		});
		
		JButton makeItDry = setupButton("Drought", null, BUILDING_BUTTON_SIZE);
		makeItDry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.drought();
			}
		});
		
		JButton makeItDay = setupButton("Day", null, BUILDING_BUTTON_SIZE);
		makeItDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.fastForwardToDay();
			}
		});

		JToggleButton debug = setupToggleButton(Game.DEBUG_DRAW ? "Stop Debug" : "Debug", null, BUILDING_BUTTON_SIZE);
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Game.DEBUG_DRAW = debug.isSelected();
				debug.setText(Game.DEBUG_DRAW ? "Stop Debug" : "Debug");
			}
		});
		
		JButton exit = setupButton("Exit", null, BUILDING_BUTTON_SIZE);
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});
		
		JPanel resourcePanel = new JPanel();
		int RESOURCE_PANEL_WIDTH = 100;
		resourcePanel.setPreferredSize(new Dimension(RESOURCE_PANEL_WIDTH, 1000));
//		resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.Y_AXIS));
		for(JLabel label : resourceIndicators) {
			resourcePanel.add(label);
		}
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(GUIWIDTH-RESOURCE_PANEL_WIDTH, 1000));
		buttonPanel.add(money);
		buttonPanel.add(tileSize);
		
		buttonPanel.add(showHeightMap);
		buttonPanel.add(flipTable);
		buttonPanel.add(makeItRain);
		buttonPanel.add(makeItDry);
		buttonPanel.add(makeItDay);
		buttonPanel.add(debug);
		buttonPanel.add(exit);
		
		
		techView = new JPanel();
		
		for(int i = 0; i < ResearchType.values().length; i++) {
			final ResearchType type = ResearchType.values()[i];
			researchButtons[i] = setupButton(type.toString(), null, RESEARCH_BUTTON_SIZE);
			researchButtons[i].setEnabled(false);
			researchButtons[i].addActionListener(e -> {
				gameInstance.setResearchTarget(type);
			});
			techView.add(researchButtons[i]);
		}

		setupGamePanel();
		setupMinimapPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(buttonFontSmall);
		
		RESOURCE_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, Utils.resizeImageIcon(ItemType.ADAMANTITE_ORE.getImageIcon(0), 20, 20), resourcePanel, "Does nothing");
		TECH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab("Tech Stuff", Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.png"), 20, 20), techView, "Does nothing");
		BUILDING_TAB = tabbedPane.getTabCount();
		addBuildingTab();
		DEBUG_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/debugtab.png"), 20, 20), buttonPanel, "Does nothing");

		// remove building tab after setting all of the tabs up
		removeBuildingTab();
		
		JPanel guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH,frame.getHeight()));
		guiSplitter.add(tabbedPane,BorderLayout.CENTER);
//		guiSplitter.add(resourcePanel,BorderLayout.WEST);

		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH,GUIWIDTH));
		guiSplitter.add(minimapPanel,BorderLayout.NORTH);
		
		
		frame.getContentPane().add(gamepanel,BorderLayout.CENTER);
		frame.getContentPane().add(guiSplitter,BorderLayout.EAST);
//		frame.setGlassPane(cityView);
//		frame.setGlassPane(workerView);
		frame.pack();
		frame.setVisible(true);
		gamepanel.requestFocusInWindow();
	
		repaintingThread = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
		});
		repaintingThread.start();

		frame.repaint();
		gamepanel.requestFocus();
		
		gameLoopThread = new Thread(() -> {
			try {
				while(true) {
					gameInstance.gameTick();
					if(!gameInstance.shouldFastForward())
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
