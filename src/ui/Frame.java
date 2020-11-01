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
	private static final ImageIcon HELLFORGE_TAB_ICON = Utils.resizeImageIcon(Game.buildingTypeMap.get("HELLFORGE").getImageIcon(0), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/crafting.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon SHADOW_WORD_DEATH = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/soyouhavechosendeath.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon SPAWN_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/spawn_tab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);

	private static final ImageIcon FAST_FORWARD_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/fastforward.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RAIN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/rain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon ERUPTION_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/erupt.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon CHANGE_FACTION_ICON = Utils.resizeImageIcon(Game.unitTypeMap.get("CYCLOPS").getImageIcon(DEBUG_BUTTON_SIZE.height-5), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_DISABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_disabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_ENABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_enabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon METEOR_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/meteor.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	
	private JFrame frame;
	private JPanel mainMenuPanel;
	private GameView gamepanel;
	private GameViewOverlay gamepanelOverlay;
	private GUIController guiController;
	private InfoPanelView infoPanelView;
	private ProduceUnitView makeUnitView;
	private CraftingView blacksmithView;
	private WorkerView workerMenu;
	private JPanel spawnMenu;
	private ResearchView researchView;
	private JTabbedPane tabbedPane;
	private JPanel guiSplitter;
	private JPanel resourcePanel;
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
		
		spawnMenu = new JPanel();
		for (int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gamepanel.setThingToSpawn(type);
			});
			button.addRightClickActionListener(e -> {
				infoPanelView.switchInfoPanel(new UnitTypeInfoPanel(type, gamepanel.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					infoPanelView.pushInfoPanel(new UnitTypeInfoPanel(type, gamepanel.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					infoPanelView.popInfoPanel();
				}
			});
			spawnMenu.add(button);
		}
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gamepanel.setThingToSpawn(type);
			});
			button.addRightClickActionListener(e -> {
				infoPanelView.switchInfoPanel(new BuildingTypeInfoPanel(type, gamepanel.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					infoPanelView.pushInfoPanel(new BuildingTypeInfoPanel(type, gamepanel.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					infoPanelView.popInfoPanel();
				}
			});
//			buildingButtons[i] = button;
			spawnMenu.add(button);
		}
		JToggleButton toggle = KUIConstants.setupToggleButton("Non-playerControlled", null, DEBUG_BUTTON_SIZE);
		toggle.addActionListener(e -> {
			toggle.setText(!toggle.isSelected() ? "Non-playerControlled" : "playerControlled");
			gamepanel.setSummonPlayerControlled(!toggle.isSelected());
		});
		spawnMenu.add(toggle);
		
		makeUnitView = new ProduceUnitView(gamepanel);
		blacksmithView = new CraftingView(gamepanel);
		
		JPanel buttonPanel = new JPanel();

		JToggleButton showHeightMap = KUIConstants.setupToggleButton("Show Height Map", null, DEBUG_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gamepanel.setShowHeightMap(showHeightMap.isSelected());
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

		JButton eruptVolcano = KUIConstants.setupButton("Erupt Volcano", ERUPTION_ICON, DEBUG_BUTTON_SIZE);
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
				gameInstance.addResources(gamepanel.getFaction());
			}
		});

		JButton makeItRain = KUIConstants.setupButton("Rain", RAIN_ICON, DEBUG_BUTTON_SIZE);
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

		JToggleButton fastForward = KUIConstants.setupToggleButton("Fast Forward", FAST_FORWARD_ICON, DEBUG_BUTTON_SIZE);
		fastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isFastForwarding = fastForward.isSelected();
				fastForward.setText(fastForward.isSelected() ? "Stop Fast Forward" : "Fast Forward");
			}
		});

		JButton researchEverything = KUIConstants.setupButton("Research All", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.researchEverything(gamepanel.getFaction());
				buttonPanel.remove(researchEverything);
			}
		});
		JButton shadowWordDeath = KUIConstants.setupButton("Shadow Word: Death", SHADOW_WORD_DEATH, LONG_BUTTON_SIZE);
		shadowWordDeath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.shadowWordDeath(100);
			}
		});

		JButton meteor = KUIConstants.setupButton("Meteor", METEOR_ICON, DEBUG_BUTTON_SIZE);
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
				gameInstance.shadowWordDeath(1);
			}
		});
		JButton setPlayerFaction = KUIConstants.setupButton("Change Faction", CHANGE_FACTION_ICON, DEBUG_BUTTON_SIZE);
		setPlayerFaction.addActionListener(e -> {
			int choice = JOptionPane.showOptionDialog(null, "Choose faction", "Choose faction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, gameInstance.world.getFactions().toArray(), World.NO_FACTION);
			if(choice >= 0 && choice < gameInstance.world.getFactions().size()) {
				if(gamepanel.getFaction() != gameInstance.world.getFactions().get(choice)) {
					gamepanel.deselectEverything();
					gamepanel.setFaction(gameInstance.world.getFactions().get(choice));
					guiController.changedFaction(gameInstance.world.getFactions().get(choice));
				}
			}
		});

		JToggleButton debug = KUIConstants.setupToggleButton(gamepanel.getDrawDebugStrings() ? "Leave Matrix" : "Matrix", null, DEBUG_BUTTON_SIZE);
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gamepanel.setDrawDebugStrings(debug.isSelected());
				debug.setText(gamepanel.getDrawDebugStrings() ? "Leave Matrix" : "Matrix");
			}
		});
		JToggleButton toggleNight = KUIConstants.setupToggleButton(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled", NIGHT_ENABLED_ICON,
				DEBUG_BUTTON_SIZE);
		toggleNight.setSelected(true);
		toggleNight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Game.DISABLE_NIGHT = !toggleNight.isSelected();
				toggleNight.setText(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled");
				toggleNight.setIcon(Game.DISABLE_NIGHT ? NIGHT_DISABLED_ICON : NIGHT_ENABLED_ICON);
			}
		});

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});

		resourcePanel = new JPanel();
		int RESOURCE_PANEL_WIDTH = 100;
		resourcePanel.setPreferredSize(new Dimension(RESOURCE_PANEL_WIDTH, 1000));
//		resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.Y_AXIS));
		buttonPanel.setPreferredSize(new Dimension(GUIWIDTH - RESOURCE_PANEL_WIDTH, 1000));

		buttonPanel.add(showHeightMap);
		buttonPanel.add(flipTable);
		buttonPanel.add(spawnUnit);
		buttonPanel.add(makeItRain);
		buttonPanel.add(makeItDry);
		buttonPanel.add(fastForward);
		buttonPanel.add(eruptVolcano);
		buttonPanel.add(meteor);
		buttonPanel.add(unitEvents);
		buttonPanel.add(debug);
		buttonPanel.add(toggleNight);
		buttonPanel.add(addResources);
		buttonPanel.add(setPlayerFaction);
		buttonPanel.add(researchEverything);
		buttonPanel.add(exit);
		buttonPanel.add(shadowWordDeath);

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
		tabbedPane.insertTab(null, SPAWN_TAB_ICON, spawnMenu, "Summon units for testing", SPAWN_TAB);

		DEBUG_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null,
				Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/debugtab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE),
				buttonPanel, "Various testing functions");

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

	public void exitGame() {
		System.exit(0);
	}
}
