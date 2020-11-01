package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.Timer;

import game.*;
import networking.server.*;
import networking.view.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class Frame extends JPanel {
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	public static final int GUIWIDTH = 350;
	
	public static final int MILLISECONDS_PER_TICK = 100;
	private static final String TITLE = "civilicraftfortressII";

	public static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(140, 30);
	public static final Dimension LONG_BUTTON_SIZE = new Dimension(285, 30);
	public static final Dimension SPAWN_BUTTON_SIZE = new Dimension(30, 30);
	public static final Dimension BUILD_UNIT_BUTTON_SIZE = new Dimension(170, 35);

	private static final int TAB_ICON_SIZE = 25;
	private static final ImageIcon WORKER_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/building.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon MAKE_UNIT_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/barracks.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/crafting.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon SPAWN_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/spawn_tab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon DEBUG_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/debugtab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	
	
	private JFrame frame;
	private JPanel mainMenuPanel;
	private GameView gamepanel;
	private GameViewOverlay gamepanelOverlay;
	private GUIController guiController;
	private InfoPanelView infoPanelView;
	private ProduceUnitView makeUnitView;
	private CraftingView blacksmithView;
	private WorkerView workerMenu;
	private SpawnUnitsView spawnMenu;
	private ResearchView researchView;
	private JTabbedPane tabbedPane;
	private JPanel guiSplitter;
	private JToggleButton easyModeButton;
	
	private JTextField mapSize;
	private Game gameInstance;

	private int WORKER_TAB;
	private int RESEARCH_TAB;
	private int BLACKSMITH_TAB;
	private int DEBUG_TAB;
	private int MAKE_UNIT_TAB;
	private int SPAWN_TAB;

	private Timer repaintingThread;
	private Thread gameLoopThread;
	private Thread terrainImageThread;
	private boolean isFastForwarding = false;
	
	public Frame() {

		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Utils.loadImage("resources/Images/logo.png"));

		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		height = Math.min(height, 1080);
		height = height * 8 / 9;
		int width = height + GUIWIDTH;
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		guiController = new GUIController() {
			@Override
			public void pushInfoPanel(InfoPanel infoPanel) {
				infoPanelView.pushInfoPanel(infoPanel);
			}
			@Override
			public void popInfoPanel() {
				infoPanelView.popInfoPanel();
			}
			@Override
			public void switchInfoPanel(InfoPanel infoPanel) {
				infoPanelView.switchInfoPanel(infoPanel);
			}
			@Override
			public void pressedSelectedUnitPortrait(Unit unit) {
				gamepanel.pressedSelectedUnitPortrait(unit);
			}
			@Override
			public void tryToCraftItem(ItemType type, int amount) {
				gamepanel.getCommandInterface().craftItem(gamepanel.getFaction(), type);
			}
			@Override
			public void research(ResearchType researchType) {
				gamepanel.getCommandInterface().research(gamepanel.getFaction(), researchType);
			}
			@Override
			public void selectedBuilding(Building building, boolean selected) {
				if (building.getType() == Game.buildingTypeMap.get("BARRACKS")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("CASTLE")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("WORKSHOP")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("BLACKSMITH")) {
					manageBlacksmithTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("HELLFORGE")) {
					manageBlacksmithTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
					manageResearchLabTab(selected);
				}
				InfoPanel infoPanel = new BuildingInfoPanel(building);
				switchInfoPanel(infoPanel);
				SwingUtilities.invokeLater(() -> {
					infoPanel.addButton("Explode").addActionListener(e -> gameInstance.explode(building));
				});
				frame.repaint();
			}

			@Override
			public void selectedUnit(Unit unit, boolean selected) {
				gamepanelOverlay.selectedUnit(unit, selected);
				if(selected) {
					UnitInfoPanel infoPanel = new UnitInfoPanel(unit);
					switchInfoPanel(infoPanel);
					SwingUtilities.invokeLater(() -> {
						infoPanel.addButton("Explode").addActionListener(e -> gameInstance.explode(unit));
						infoPanel.addButton("RoadEverything").addActionListener(e -> gamepanel.workerRoad(Game.buildingTypeMap.get("STONE_ROAD")));
						infoPanel.addButton("AutoBuild").addActionListener(e -> gamepanel.toggleAutoBuild());
						infoPanel.addButton("SetHarvesting").addActionListener(e -> gamepanel.setHarvesting());
						infoPanel.addButton("Guard").addActionListener(e -> gamepanel.toggleGuarding());
					});
				}
				if(unit.getType().isBuilder()) {
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
			public void changedFaction(Faction faction) {
				gamepanelOverlay.changeFaction(faction);
				gamepanel.setFaction(faction);
			}

			@Override
			public void updateGUI() {
				gamepanelOverlay.updateItems();
				researchView.updateButtons(gameInstance.world);
				workerMenu.updateButtons();
				makeUnitView.updateButtons();
				blacksmithView.updateButtons();

				frame.repaint();
			}
			@Override
			public void setFastForwarding(boolean enabled) {
				isFastForwarding = true;
			}

		};
		gameInstance = new Game(guiController);

		EventQueue.invokeLater(() -> {
			menu();
		});
	}
	
	
	private void menu() {
		mainMenuPanel = new JPanel();
		
		easyModeButton = KUIConstants.setupToggleButton("Enable Easy Mode", null, BUILDING_BUTTON_SIZE);
		easyModeButton.addActionListener(e -> {
			easyModeButton.setText(easyModeButton.isSelected() ? "Disable Easy Mode" : "Enable Easy Mode");
		});
		
		JButton start = KUIConstants.setupButton("Start Game", null, BUILDING_BUTTON_SIZE);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				start.setEnabled(false);
				repaint();
				Thread thread = new Thread(() -> {
					runGame();
				});
				thread.start();
			}
		});
		start.setEnabled(false);
		mainMenuPanel.add(start);

		mapSize = new JTextField("128", 10);
		KUIConstants.setComponentAttributes(mapSize, BUILDING_BUTTON_SIZE);
		mapSize.setFocusable(true);
		mainMenuPanel.add(mapSize);
		mainMenuPanel.add(easyModeButton);
		
		frame.add(mainMenuPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.requestFocusInWindow();
		start.setEnabled(true);
	}

	private void setupGamePanel() {
		gamepanel = new GameView(gameInstance);
		gamepanel.setCommandInterface(Utils.makeFunctionalCommandInterface(gameInstance));
		gamepanelOverlay = new GameViewOverlay(guiController);
		gamepanel.setLayout(new BorderLayout());
		gamepanel.add(gamepanelOverlay, BorderLayout.CENTER);
	}


	private void manageBuildingTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == WORKER_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(WORKER_TAB);
		}
		tabbedPane.setEnabledAt(WORKER_TAB, enabled);
	}
	
	private void manageMakeUnitTab(boolean enabled) {
		if(enabled) {
			tabbedPane.setEnabledAt(MAKE_UNIT_TAB, enabled);
			tabbedPane.setSelectedIndex(MAKE_UNIT_TAB);
		}
		else {
			System.out.println(gamepanel);
			System.out.println(gamepanel.getFaction());
			if(!(gamepanel.getFaction().isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("CASTLE"))
					|| gamepanel.getFaction().isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("BARRACKS"))
					|| gamepanel.getFaction().isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("WORKSHOP")))) {
				if(tabbedPane.getSelectedIndex() == MAKE_UNIT_TAB) {
					tabbedPane.setSelectedIndex(0);
				}
				tabbedPane.setEnabledAt(MAKE_UNIT_TAB, false);
			}
		}
	}

	private void manageBlacksmithTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == BLACKSMITH_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(BLACKSMITH_TAB);
		}
		tabbedPane.setEnabledAt(BLACKSMITH_TAB, enabled);
	}
	
	private void manageResearchLabTab(boolean enabled) {
		if (enabled == true) {
			tabbedPane.setSelectedIndex(RESEARCH_TAB);
		}
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
//		frame.pack();
		
		gamepanel.centerViewOn(gameInstance.world.buildings.getLast().getTile(), 50, gamepanel.getWidth(), gamepanel.getHeight());
		gamepanel.requestFocusInWindow();
		gamepanel.requestFocus();
		frame.repaint();
		gameLoopThread.start();
		repaintingThread.start();
		terrainImageThread.start();
	}
	private void runGame() {
		setupGamePanel();
		System.err.println("Starting Game");
		
		int size = Integer.parseInt(mapSize.getText());
		
		gameInstance.generateWorld(size, size, easyModeButton.isSelected(), Arrays.asList(new PlayerInfo("Player", Color.pink)));
		for(Faction f : gameInstance.world.getFactions()) {
			if(f.isPlayer()) {
				guiController.changedFaction(f);
				break;
			}
		}

		workerMenu = new WorkerView(gamepanel);
		spawnMenu = new SpawnUnitsView(gamepanel);
		makeUnitView = new ProduceUnitView(gamepanel);
		blacksmithView = new CraftingView(gamepanel);
		
		DebugView buttonPanel = new DebugView(gamepanel);

		researchView = new ResearchView(gamepanel);
		MinimapView minimapPanel = new MinimapView(gamepanel);

		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(KUIConstants.buttonFontSmall);

		RESEARCH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, RESEARCH_TAB_ICON, researchView.getRootPanel(), "Research new technologies");
		
		WORKER_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, WORKER_TAB_ICON, workerMenu.getRootPanel(), "Build buildings with workers", WORKER_TAB);

		MAKE_UNIT_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, MAKE_UNIT_TAB_ICON, makeUnitView.getRootPanel(), "Make units from castles, barracks, or workshops", MAKE_UNIT_TAB);

		BLACKSMITH_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, BLACKSMITH_TAB_ICON, blacksmithView.getRootPanel(), "Craft items", BLACKSMITH_TAB);
		
		SPAWN_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, SPAWN_TAB_ICON, spawnMenu.getRootPanel(), "Summon units for testing", SPAWN_TAB);

		DEBUG_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, DEBUG_TAB_ICON, buttonPanel.getRootPanel(), "Various testing functions");

		// disable building tab after setting all of the tabs up
		manageBuildingTab(false);
		manageBlacksmithTab(false);
		manageResearchLabTab(false);
		manageMakeUnitTab(false);
		manageSpawnTab(true);
		

		guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH, frame.getHeight()));
		guiSplitter.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
//		guiSplitter.add(resourcePanel,BorderLayout.WEST);

		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH, GUIWIDTH));
		guiSplitter.add(minimapPanel, BorderLayout.NORTH);

		
		infoPanelView = new InfoPanelView();
		JPanel infoPanelViewRoot = infoPanelView.getRootPanel();
		infoPanelViewRoot.setBackground(gameInstance.getBackgroundColor());
		infoPanelViewRoot.setPreferredSize(new Dimension(GUIWIDTH, (int) (GUIWIDTH / 2.5)));
		guiSplitter.add(infoPanelViewRoot, BorderLayout.SOUTH);
		
		
		

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
		terrainImageThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gamepanel.updateTerrainImages();
					long elapsed = System.currentTimeMillis() - start;
					long sleeptime = MILLISECONDS_PER_TICK - elapsed;
					if(sleeptime > 0) {
						Thread.sleep(sleeptime);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					if(e1 instanceof InterruptedException) {
						break;
					}
				}
			}
		});

		frame.repaint();

		gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					guiController.updateGUI();
					long elapsed = System.currentTimeMillis() - start;
					if(World.ticks % 10 == 0) {
						frame.setTitle(TITLE + " " + elapsed);
					}
					long sleeptime = MILLISECONDS_PER_TICK - elapsed;
					if(sleeptime > 0 && !isFastForwarding) {
						Thread.sleep(sleeptime);
					}
				}
				catch(Exception e) {
					try (FileWriter fw = new FileWriter("ERROR_LOG.txt", true);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw)) {
						e.printStackTrace(out);
					} catch (IOException ee) {
					}
					e.printStackTrace();
					if(e instanceof InterruptedException) {
						break;
					}
				}
			}
		});

		SwingUtilities.invokeLater(() -> {
			switchToGame();
		});
	}
}
