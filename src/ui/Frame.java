package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.border.*;

import game.*;
import ui.MainMenuBackground.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class Frame extends JPanel {
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	int GUIWIDTH = 350;
	int MINIMAPBORDERWIDTH = 40;
	
	public static final long MILLISECONDS_PER_TICK = 100;

	public static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(130, 30);
	public static final Dimension SPAWN_BUTTON_SIZE = new Dimension(30, 30);
	public static final Dimension BUILD_UNIT_BUTTON_SIZE = new Dimension(170, 35);


	private ImageIcon WORKER_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/building.PNG"), 20, 20);
	private ImageIcon CITY_TAB_ICON = Utils.resizeImageIcon(BuildingType.CASTLE.getImageIcon(0), 20, 20);
	private ImageIcon BARRACKS_TAB_ICON = Utils.resizeImageIcon(BuildingType.BARRACKS.getImageIcon(0), 20, 20);
	private ImageIcon WORKSHOP_TAB_ICON = Utils.resizeImageIcon(BuildingType.WORKSHOP.getImageIcon(0), 20, 20);
	private ImageIcon RESEARCHLAB_TAB_ICON = Utils.resizeImageIcon(BuildingType.RESEARCHLAB.getImageIcon(0), 20, 20);
	private ImageIcon HELLFORGE_TAB_ICON = Utils.resizeImageIcon(BuildingType.HELLFORGE.getImageIcon(0), 20, 20);
	private ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/crafting.png"), 20, 20);
	private ImageIcon TECH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.PNG"), 20, 20);
	private ImageIcon RESOURCE_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/itemicons/adamantite_ore.png"), 20, 20);
	private ImageIcon STAT_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/itemicons/adamant_sword.png"), 20, 20);
	
	private Timer repaintingThread;
	private JToggleButton easyModeButton;
	private JFrame frame;
	private JPanel mainMenuPanel;
	private volatile boolean readyToStart;
	private JPanel gamepanel;
	private JPanel minimapPanel;
	private JPanel infoPanel;
	private JPanel castleView;
	private JPanel blacksmithView;
	private JPanel hellforgeView;
	private JPanel barracksView;
	private JPanel workshopView;
	private JPanel researchLabView;
	private JPanel tileView;
	private JPanel workerMenu;
	private JPanel spawnMenu;
	private JPanel techView;
	private JPanel statView;
	private JLabel tileSize;
	private JTabbedPane tabbedPane;
	private JPanel guiSplitter;
	private JComboBox<MapType> mapType;
	private JLabel[] resourceIndicators = new JLabel[ItemType.values().length];
	private JButton[] researchButtons = new JButton[ResearchType.values().length];
	private JButton[] buildingButtons = new JButton[BuildingType.values().length];
	private JButton[] unitButtons = new JButton[UnitType.values().length];
	private JButton[] craftButtons = new JButton[ItemType.values().length];
	private JButton[] statButtons = new JButton[7];
	
	private JTextField mapSize;
	private int WIDTH;
	private int HEIGHT;
	private Game gameInstance;
	private int mx;
	private int my;
	private boolean dragged = false;

	private int RESOURCE_TAB;
	private int WORKER_TAB;
	private int TECH_TAB;
	private int BLACKSMITH_TAB;
	private int HELLFORGE_TAB;
	private int DEBUG_TAB;
	private int CASTLE_TAB;
	private int BARRACKS_TAB;
	private int WORKSHOP_TAB;
	private int RESEARCHLAB_TAB;
	private int STAT_TAB;
	private int SPAWN_TAB;

	private Thread gameLoopThread;
	
	private Semaphore gameUIReady = new Semaphore(0);

	public Frame() {

		frame = new JFrame("Civilization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Utils.loadImage("resources/Images/logo.png"));

		HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
		HEIGHT = Math.min(HEIGHT, 1080);
		HEIGHT = HEIGHT * 8 / 9;
		WIDTH = HEIGHT + GUIWIDTH;
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		GUIController guiController = new GUIController() {
			@Override
			public void selectedBuilding(Building building, boolean selected) {
				if (building.getBuildingType() == BuildingType.BARRACKS) {
					manageBarracksTab(selected);
				}
				if (building.getBuildingType() == BuildingType.CASTLE) {
					manageCastleTab(selected);
				}
				if (building.getBuildingType() == BuildingType.BLACKSMITH) {
					manageBlacksmithTab(selected);
				}
				if (building.getBuildingType() == BuildingType.HELLFORGE) {
					manageHellforgeTab(selected);
				}
				if (building.getBuildingType() == BuildingType.WORKSHOP) {
					manageWorkshopTab(selected);
				}
				if (building.getBuildingType() == BuildingType.RESEARCHLAB) {
					manageResearchLabTab(selected);
				}
				switchInfoPanel(new BuildingInfoPanel(building));
				frame.repaint();
			}

			@Override
			public void selectedUnit(Unit unit, boolean selected) {
				if(unit == null) {
					return;
				}
				if(selected) {
					switchInfoPanel(new UnitInfoPanel(unit));
				}
				if(unit.getType() == UnitType.WORKER) {
					manageBuildingTab(selected);
				}
				frame.repaint();
			}

			@Override
			public void selectedSpawnUnit(boolean selected) {
				manageSpawnTab(selected);
				frame.repaint();
			}

			@Override
			public void toggleTileView() {
				tileView.setVisible(!tileView.isVisible());
			}

			@Override
			public void updateGUI() {
				for (int i = 0; i < ItemType.values().length; i++) {
					resourceIndicators[i].setText("" + gameInstance.getResourceAmount(ItemType.values()[i]));
				}
				for (int i = 0; i < ResearchType.values().length; i++) {
					Research r = gameInstance.researches.get(ResearchType.values()[i]);
//					if(r.getType().getTier() != 1) {
//						continue;
//					}
					boolean unlocked = r.isUnlocked();
					ResearchRequirement req = r.getRequirement();
					JButton button = researchButtons[i];
					if (unlocked) {
						button.setEnabled(false);
						button.setVisible(true);
					} else if (req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					} else if (req.areSecondLayerRequirementsMet()) {
						button.setEnabled(false);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for (int i = 0; i < BuildingType.values().length; i++) {
					BuildingType type = BuildingType.values()[i];
					JButton button = buildingButtons[i];
					ResearchRequirement req = gameInstance.buildingResearchRequirements.get(type);
					if (req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for (int i = 0; i < UnitType.values().length; i++) {
					JButton button = unitButtons[i];
					if (button == null) {
						continue;
					}
					UnitType type = UnitType.values()[i];
					ResearchRequirement req = gameInstance.unitResearchRequirements.get(type);
					if (req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for (int i = 0; i < ItemType.values().length; i++) {
					ItemType type = ItemType.values()[i];
					if (type.getCost() == null) {
						continue;
					}
					JButton button = craftButtons[i];
					ResearchRequirement req = gameInstance.craftResearchRequirements.get(type);
					if (req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				frame.repaint();
			}

		};
		gameInstance = new Game(guiController);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					menu();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}
			}
		});
	}
	
	private void menu() {
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		if(Driver.SHOW_MENU_ANIMATION) {
			mainMenuPanel = new MainMenuBackground();
		}
		else {
			mainMenuPanel = new JPanel();
		}
		
		easyModeButton = KUIConstants.setupToggleButton("Enable Easy Mode", null, BUILDING_BUTTON_SIZE);
		easyModeButton.addActionListener(e -> {
			easyModeButton.setText(easyModeButton.isSelected() ? "Disable Easy Mode" : "Enable Easy Mode");
		});
		
		JButton start = KUIConstants.setupButton("Start Game", null, BUILDING_BUTTON_SIZE);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(readyToStart) {
					start.setEnabled(false);
					readyToStart = false;
					repaint();
					Thread thread = new Thread(() -> {
						runGame();
					});
					thread.start();
				}
			}
		});
		start.setEnabled(false);
		mainMenuPanel.add(start);

		mapType = new JComboBox<>(MapType.values());
		KUIConstants.setComponentAttributes(mapType, BUILDING_BUTTON_SIZE);
		mainMenuPanel.add(mapType);

		mapSize = new JTextField("128", 10);
		KUIConstants.setComponentAttributes(mapSize, BUILDING_BUTTON_SIZE);
		mapSize.setFocusable(true);
		mainMenuPanel.add(mapSize);
		mainMenuPanel.add(easyModeButton);
		
		frame.add(mainMenuPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.requestFocusInWindow();
		if(mainMenuPanel instanceof MainMenuBackground) {
			((MainMenuBackground)mainMenuPanel).start(() -> {
				readyToStart = true;
				start.setEnabled(true);
			});
		}
		else {
			readyToStart = true;
			start.setEnabled(true);
		}
	}

	private void setupGamePanel() {
		gamepanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.setColor(gameInstance.getBackgroundColor());
				g.fillRect(0, 0, getWidth(), getHeight());
				gameInstance.drawGame(g);
				g.setColor(Color.black);
				g.drawRect(-1, 0, getWidth() + 1, getHeight());
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
				// +1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(), mx, my);
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
				if (SwingUtilities.isLeftMouseButton(e)) {
					int mrx = e.getX();
					int mry = e.getY();
					int dx = mx - mrx;
					int dy = my - mry;
					gameInstance.shiftView(dx, dy);
					mx = mrx;
					my = mry;

					gameInstance.mouseOver(mx, my);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					int mx2 = e.getX();
					int my2 = e.getY();
					gameInstance.mouseOver(mx2, my2);
				}
			}
		});

		gamepanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					gameInstance.rightClick(mx, my);
				}
				if (e.getButton() == MouseEvent.BUTTON1 && dragged == false) {
					gameInstance.leftClick(mx, my);
				}
				dragged = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
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
				if (e.getClickCount() == 2) {
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
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					gameInstance.shiftControl(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					gameInstance.shiftControl(true);
				}
				if(e.getKeyCode() == KeyEvent.VK_A) {
					gameInstance.aControl(true);
					
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					gameInstance.deselectThing();
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					gameInstance.unitStop();
				}
				if (e.getKeyCode() == KeyEvent.VK_R) {
					if (gameInstance.getSelectedUnit() != null) {
						gameInstance.buildRoad(RoadType.STONE_ROAD);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_M) {
					if (gameInstance.getSelectedUnit() != null) {
						gameInstance.buildBuilding(BuildingType.MINE);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_I) {
					if (gameInstance.getSelectedUnit() != null) {
						gameInstance.buildBuilding(BuildingType.IRRIGATION);
					}
				}
			}
		});
	}

	private void switchInfoPanel(JPanel newInfo) {
		SwingUtilities.invokeLater(() -> {
			infoPanel.removeAll();
			newInfo.setOpaque(false);
			JButton explodeUnit = KUIConstants.setupButton("Explode", null, DEBUG_BUTTON_SIZE);
			explodeUnit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					gameInstance.explode(gameInstance.getSelectedThing());
				}
			});
			newInfo.setLayout(null);
			newInfo.add(explodeUnit);
			explodeUnit.setBounds(10, infoPanel.getHeight()-(int)DEBUG_BUTTON_SIZE.getHeight()- 10, (int)DEBUG_BUTTON_SIZE.getWidth(), (int)DEBUG_BUTTON_SIZE.getHeight());
			infoPanel.add(newInfo, BorderLayout.CENTER);
			infoPanel.validate();
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
				gameInstance.drawMinimap(g, MINIMAPBORDERWIDTH, MINIMAPBORDERWIDTH,
						minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH,
						minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
			}
		};
		minimapPanel.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
		minimapPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
	}

	private void manageBuildingTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == WORKER_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(WORKER_TAB);
		}
		tabbedPane.setEnabledAt(WORKER_TAB, enabled);
	}

	private void manageCastleTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == CASTLE_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(CASTLE_TAB);
		}
		tabbedPane.setEnabledAt(CASTLE_TAB, enabled);
	}

	private void manageBlacksmithTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == BLACKSMITH_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(BLACKSMITH_TAB);
		}
		tabbedPane.setEnabledAt(BLACKSMITH_TAB, enabled);
	}
	private void manageHellforgeTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == HELLFORGE_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(HELLFORGE_TAB);
		}
		tabbedPane.setEnabledAt(HELLFORGE_TAB, enabled);
	}
	private void manageBarracksTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == BARRACKS_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(BARRACKS_TAB);
		}
		tabbedPane.setEnabledAt(BARRACKS_TAB, enabled);
	}
	
	private void manageWorkshopTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == WORKSHOP_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(WORKSHOP_TAB);
		}
		tabbedPane.setEnabledAt(WORKSHOP_TAB, enabled);
	}
	
	private void manageResearchLabTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == RESEARCHLAB_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(RESEARCHLAB_TAB);
		}
		tabbedPane.setEnabledAt(RESEARCHLAB_TAB, enabled);
	}

	private void manageSpawnTab(boolean enabled) {

		if (enabled == false && tabbedPane.getSelectedIndex() == SPAWN_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(SPAWN_TAB);
		}
		tabbedPane.setEnabledAt(SPAWN_TAB, enabled);
	}

	private void switchToGame() {
		frame.remove(mainMenuPanel);
		mainMenuPanel = null;
		
		frame.getContentPane().add(gamepanel, BorderLayout.CENTER);
		frame.getContentPane().add(guiSplitter, BorderLayout.EAST);
		frame.pack();
		
		gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
		gameInstance.centerViewOn(gameInstance.world.buildings.get(0).getTile(), 50);
		gamepanel.requestFocusInWindow();
		gamepanel.requestFocus();
		frame.repaint();
		gameLoopThread.start();
		repaintingThread.start();
	}
	private void runGame() {
		System.err.println("Starting Game");
		Runnable menuAnimationStopListener = new Runnable() {
			@Override
			public void run() {
				try {
					gameUIReady.acquire();
					SwingUtilities.invokeLater(() -> {
						switchToGame();
					});
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		};
		if(mainMenuPanel instanceof MainMenuBackground) {
			((MainMenuBackground)mainMenuPanel).stop(menuAnimationStopListener);
		}
		else {
			Thread thread = new Thread(menuAnimationStopListener);
			thread.start();
		}
		
		
		int size = Integer.parseInt(mapSize.getText());
		gameInstance.generateWorld((MapType) mapType.getSelectedItem(), size, easyModeButton.isSelected());

		Dimension RESOURCE_BUTTON_SIZE = new Dimension(100, 35);
		Dimension RESEARCH_BUTTON_SIZE = new Dimension(125, 35);
		int BUILDING_ICON_SIZE = 25;
		int RESOURCE_ICON_SIZE = 35;

		workerMenu = new JPanel();
		
		
		
		JButton makeRoad = KUIConstants.setupButton("Road",
				Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/buildroad.png"),
						BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
				BUILDING_BUTTON_SIZE);
		makeRoad.addActionListener(e -> {
			gameInstance.buildRoad(RoadType.STONE_ROAD);
		});
		workerMenu.add(makeRoad);

		for (int i = 0; i < BuildingType.values().length; i++) {
			BuildingType type = BuildingType.values()[i];
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameInstance.buildBuilding(type);
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
			buildingButtons[i] = button;
			workerMenu.add(button);
		}

		spawnMenu = new JPanel();
		for (int i = 0; i < UnitType.values().length; i++) {
			UnitType type = UnitType.values()[i];
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width/1.2), (int)(SPAWN_BUTTON_SIZE.height/1.2)),
					SPAWN_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameInstance.setThingToSpawn(type, null);
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			unitButtons[i] = button;
			spawnMenu.add(button);
		}
		for (int i = 0; i < BuildingType.values().length; i++) {
			BuildingType type = BuildingType.values()[i];
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width/1.2), (int)(SPAWN_BUTTON_SIZE.height/1.2)),
					SPAWN_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameInstance.setThingToSpawn(null, type);
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
//			buildingButtons[i] = button;
			spawnMenu.add(button);
		}

		castleView = new JPanel() {
		};
		for (int i = 0; i < UnitType.values().length; i++) {
			UnitType type = UnitType.values()[i];
			if (type != UnitType.WORKER) {
				continue;
			}
			KButton button = KUIConstants.setupButton("Build " + type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameInstance.tryToBuildUnit(type);
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			unitButtons[i] = button;
			castleView.add(button);
		}
		workshopView = new JPanel() {
		};
		for (int i = 0; i < UnitType.values().length; i++) {
			UnitType type = UnitType.values()[i];
			if (type != UnitType.CATAPULT && type != UnitType.LONGBOWMAN) {
				continue;
			}
			KButton button = KUIConstants.setupButton("Build " + type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameInstance.tryToBuildUnit(type);
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			unitButtons[i] = button;
			workshopView.add(button);
		}
		
		researchLabView = new JPanel() {
		};
		for (int i = 0; i < ResearchType.values().length; i++) {
			ResearchType type = ResearchType.values()[i];
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameInstance.setResearchTarget(type);
				switchInfoPanel(new ResearchInfoPanel(gameInstance.researches.get(type)));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ResearchInfoPanel(gameInstance.researches.get(type)));
			});
			researchButtons[i] = button;
			researchLabView.add(button);
		}
		
		blacksmithView = new JPanel();
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (type.getCost() == null) {
				continue;
			}
			
			if(type == ItemType.ADAMANTITE_BAR || type == ItemType.ADAMANT_SWORD || type == ItemType.RUNITE_BAR || type == ItemType.RUNE_SWORD || type == ItemType.TITANIUM_BAR || type == ItemType.TITANIUM_SWORD) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				gameInstance.craftItem(type);
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			craftButtons[i] = button;
			blacksmithView.add(button);
		}
		
		hellforgeView = new JPanel();
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (type.getCost() == null) {
				continue;
			}
			if (type != ItemType.ADAMANTITE_BAR && type != ItemType.ADAMANT_SWORD && type != ItemType.RUNITE_BAR
					&& type != ItemType.RUNE_SWORD && type != ItemType.TITANIUM_BAR
							&& type != ItemType.TITANIUM_SWORD) {
				continue;
			}
				KButton button = KUIConstants.setupButton(type.toString(),
						Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
						BUILDING_BUTTON_SIZE);
				button.setEnabled(false);
				button.addActionListener(e -> {
					gameInstance.craftItem(type);
					switchInfoPanel(new ItemTypeInfoPanel(type));
				});
				button.addRightClickActionListener(e -> {
					switchInfoPanel(new ItemTypeInfoPanel(type));
				});

				craftButtons[i] = button;
				hellforgeView.add(button);
		}

		barracksView = new JPanel() {
		};
		for (int i = 0; i < UnitType.values().length; i++) {
			UnitType type = UnitType.values()[i];
			if (type == UnitType.WORKER || type.getCost() == null || type == UnitType.CATAPULT || type == UnitType.LONGBOWMAN) {
				continue;
			}

			KButton button = KUIConstants.setupButton("Build " + type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameInstance.tryToBuildUnit(type);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			unitButtons[i] = button;
			barracksView.add(button);
		}

		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			KLabel label = KUIConstants.setupLabel("",
					Utils.resizeImageIcon(type.getImageIcon(0), RESOURCE_ICON_SIZE, RESOURCE_ICON_SIZE),
					RESOURCE_BUTTON_SIZE);
			label.addRightClickActionListener(e -> {
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			resourceIndicators[i] = label;
		}

		tileSize = KUIConstants.setupLabel("TileSize = " + gameInstance.getTileSize(), null, DEBUG_BUTTON_SIZE);

		JToggleButton showHeightMap = KUIConstants.setupToggleButton("Show Height Map", null, DEBUG_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameInstance.setShowHeightMap(showHeightMap.isSelected());
		});

		JToggleButton flipTable = KUIConstants.setupToggleButton("Flip Table", null, DEBUG_BUTTON_SIZE);
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});

		JToggleButton spawnUnit = KUIConstants.setupToggleButton("Enable Spawn Unit", null, DEBUG_BUTTON_SIZE);
		spawnUnit.addActionListener(e -> {
			spawnUnit.setText(spawnUnit.isSelected() ? "Disable Spawn Unit" : "Enable Spawn Unit");
			gameInstance.spawnUnit(spawnUnit.isSelected());
		});

		JButton eruptVolcano = KUIConstants.setupButton("Erupt Volcano", null, DEBUG_BUTTON_SIZE);
		eruptVolcano.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.eruptVolcano();
			}
		});
		
		JButton addResources = KUIConstants.setupButton("Give Resources", null, DEBUG_BUTTON_SIZE);
		addResources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.addResources();
			}
		});

		JButton makeItRain = KUIConstants.setupButton("Rain", null, DEBUG_BUTTON_SIZE);
		makeItRain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.rain();
				gameInstance.world.grow();
			}
		});

		JButton makeItDry = KUIConstants.setupButton("Drought", null, DEBUG_BUTTON_SIZE);
		makeItDry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.drought();
			}
		});

		JButton makeItDay = KUIConstants.setupButton("Day", null, DEBUG_BUTTON_SIZE);
		makeItDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.fastForwardToDay();
			}
		});

		JToggleButton fastForward = KUIConstants.setupToggleButton("Fast Forward", null, DEBUG_BUTTON_SIZE);
		fastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.toggleFastForward(fastForward.isSelected());
				fastForward.setText(fastForward.isSelected() ? "Stop Fast Forward" : "Fast Forward");
			}
		});

		JButton researchEverything = KUIConstants.setupButton("Research", null, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.researchEverything();
			}
		});

		JButton meteor = KUIConstants.setupButton("Meteor", null, DEBUG_BUTTON_SIZE);
		meteor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.meteorStrike();
			}
		});
		JButton unitEvents = KUIConstants.setupButton("Unit Events", null, DEBUG_BUTTON_SIZE);
		unitEvents.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
//				gameInstance.spawnEverything();
				gameInstance.world.spawnOgre();
				gameInstance.world.spawnDragon();
				gameInstance.world.spawnWerewolf();
				gameInstance.world.spawnAnimal(UnitType.FLAMELET, gameInstance.world.getTilesRandomly().getFirst());
				gameInstance.world.spawnAnimal(UnitType.WATER_SPIRIT, gameInstance.world.getTilesRandomly().getFirst());
				gameInstance.world.spawnAnimal(UnitType.PARASITE, gameInstance.world.getTilesRandomly().getFirst());
				gameInstance.world.spawnEnt();
				gameInstance.world.spawnLavaGolem();
				gameInstance.world.spawnAnimal(UnitType.BOMB, gameInstance.world.getTilesRandomly().getFirst());
			}
		});

		JToggleButton debug = KUIConstants.setupToggleButton(Game.DEBUG_DRAW ? "Leave Matrix" : "Matrix", null, DEBUG_BUTTON_SIZE);
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Game.DEBUG_DRAW = debug.isSelected();
				debug.setText(Game.DEBUG_DRAW ? "Leave Matrix" : "Matrix");
			}
		});
		JToggleButton toggleNight = KUIConstants.setupToggleButton(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled", null,
				DEBUG_BUTTON_SIZE);
		toggleNight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Game.DISABLE_NIGHT = toggleNight.isSelected();
				toggleNight.setText(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled");
			}
		});

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
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
		for (JLabel label : resourceIndicators) {
			resourcePanel.add(label);
		}
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(GUIWIDTH - RESOURCE_PANEL_WIDTH, 1000));
		buttonPanel.add(tileSize);

		buttonPanel.add(showHeightMap);
		buttonPanel.add(flipTable);
		buttonPanel.add(spawnUnit);
		buttonPanel.add(makeItRain);
		buttonPanel.add(makeItDry);
		buttonPanel.add(makeItDay);
		buttonPanel.add(fastForward);
		buttonPanel.add(researchEverything);
		buttonPanel.add(eruptVolcano);
		buttonPanel.add(meteor);
		buttonPanel.add(unitEvents);
		buttonPanel.add(debug);
		buttonPanel.add(toggleNight);
		buttonPanel.add(addResources);
		buttonPanel.add(exit);

		techView = new JPanel();
		for (int i = 0; i < ResearchType.values().length; i++) {
			final ResearchType type = ResearchType.values()[i];
			if(type.getTier() != 1) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(), null, RESEARCH_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				gameInstance.setResearchTarget(type);
				switchInfoPanel(new ResearchInfoPanel(gameInstance.researches.get(type)));
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ResearchInfoPanel(gameInstance.researches.get(type)));
			});
			researchButtons[i] = button;
			techView.add(researchButtons[i]);
		}
		statView = new JPanel();
		for (int i = 0; i < gameInstance.getCombatBuffs().getStats().size(); i++) {
			ArrayList<Integer> stats = gameInstance.getCombatBuffs().getStats();
			List strings = gameInstance.getCombatBuffs().getStrings();
			int f = i;
			
			KButton button = KUIConstants.setupButton(strings.getItem(i) + ": " + gameInstance.getCombatBuffs().getStat(strings.getItem(i)), null, RESEARCH_BUTTON_SIZE);
			button.setEnabled(true);
			button.addActionListener(e -> {
				System.out.println(gameInstance.getCombatBuffs().getStat(strings.getItem(f)));
				CombatStats cs = new CombatStats(0,0,0,0,0,0,0);
				cs.getStats().set(f, cs.getStats().get(f)+1);
				cs.add(strings.getItem(f), cs.getStats().get(f) );
				gameInstance.addCombatBuff(cs);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new CombatStatInfoPanel(gameInstance.getCombatBuffs()));
			});
			statButtons[i] = button;
			statView.add(statButtons[i]);
		}
		
		setupGamePanel();
		setupMinimapPanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(KUIConstants.buttonFontSmall);

		RESOURCE_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, RESOURCE_TAB_ICON, resourcePanel,"Does nothing");

		TECH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab("Tech Stuff", TECH_TAB_ICON, techView, "Does nothing");

		STAT_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab("Unit Stats", STAT_TAB_ICON, statView, "Does nothing");
		
		BLACKSMITH_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Craft Stuff", BLACKSMITH_TAB_ICON, blacksmithView, "Does nothing", BLACKSMITH_TAB);

		HELLFORGE_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Craft Advanced Stuff",HELLFORGE_TAB_ICON, hellforgeView, "Does nothing", HELLFORGE_TAB);
		
		WORKER_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Worker Tab", WORKER_TAB_ICON, workerMenu, "Does nothing", WORKER_TAB);

		CASTLE_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("City", CITY_TAB_ICON, castleView, "Does nothing", CASTLE_TAB);

		BARRACKS_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Barracks", BARRACKS_TAB_ICON, barracksView, "Does nothing", BARRACKS_TAB);
		
		WORKSHOP_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Workshop", WORKSHOP_TAB_ICON, workshopView, "Does nothing", WORKSHOP_TAB);
		
		RESEARCHLAB_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Research Lab", RESEARCHLAB_TAB_ICON, researchLabView, "Does nothing", RESEARCHLAB_TAB);
		
		SPAWN_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab("Spawner", Utils.resizeImageIcon(UnitType.ARCHER.getImageIcon(0), 20, 20), spawnMenu,
				"Does nothing", SPAWN_TAB);

		DEBUG_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null,
				Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/debugtab.png"), 20, 20),
				buttonPanel, "Does nothing");

		// remove building tab after setting all of the tabs up
		manageBuildingTab(false);
		manageCastleTab(false);
		manageBlacksmithTab(false);
		manageHellforgeTab(false);
		manageWorkshopTab(false);
		manageResearchLabTab(false);
		manageBarracksTab(false);
		manageSpawnTab(true);
		

		guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH, frame.getHeight()));
		guiSplitter.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
//		guiSplitter.add(resourcePanel,BorderLayout.WEST);

		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH, GUIWIDTH));
		guiSplitter.add(minimapPanel, BorderLayout.NORTH);

		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setBackground(gameInstance.getBackgroundColor());
		infoPanel.setPreferredSize(new Dimension(GUIWIDTH, (int) (GUIWIDTH / 2.5)));
		infoPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		guiSplitter.add(infoPanel, BorderLayout.SOUTH);
		
		
		

//		frame.remove(background);
//		frame.getContentPane().add(background, BorderLayout.CENTER);
//		frame.getContentPane().add(guiSplitter, BorderLayout.EAST);
//		frame.pack();
		
//		frame.setVisible(true);

		repaintingThread = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
		});

		frame.repaint();

		gameLoopThread = new Thread(() -> {
			try {
				while (true) {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					long elapsed = System.currentTimeMillis() - start;
					long sleeptime = 100 - elapsed;
					if(sleeptime > 0 && !gameInstance.shouldFastForward()) {
						Thread.sleep(sleeptime);
					}
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		gameUIReady.release();
	}

	public void exitGame() {
		System.exit(0);
	}
}
