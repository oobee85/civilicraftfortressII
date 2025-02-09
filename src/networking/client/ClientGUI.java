package networking.client;

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import game.*;
import networking.message.*;
import networking.server.*;
import ui.*;
import ui.infopanels.*;
import ui.view.*;
import utils.*;
import world.Plant;

import static ui.KUIConstants.MAIN_MENU_BUTTON_SIZE;

public class ClientGUI {
	
	private static final String AZURE_SERVER_IP = "nickciv2.westus.cloudapp.azure.com";
	
	public static final int GUIWIDTH = 350;

	private static final int TAB_ICON_SIZE = 25;
	private static final ImageIcon OVERVIEW_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/overview.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/tech.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon WORKER_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/building.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon PRODUCE_UNIT_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/barracks.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/crafting.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon SPAWN_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/spawn_tab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon DEBUG_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/debugtab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon CRAFTING_FOCUS_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/focus.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);


	
	private static final Dimension CONNECTION_MENU_BUTTON_SIZE = new Dimension(120, 30);

	private static final ImageIcon AZURE_LOGO = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/azurelogo.png"), MAIN_MENU_BUTTON_SIZE.height, MAIN_MENU_BUTTON_SIZE.height);
	private static final ImageIcon LAN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/lan_icon.png"), MAIN_MENU_BUTTON_SIZE.height, MAIN_MENU_BUTTON_SIZE.height);
	private static final ImageIcon SINGLE_PLAYER_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/single_player_icon.png"), MAIN_MENU_BUTTON_SIZE.height, MAIN_MENU_BUTTON_SIZE.height);
	
	
	private Client client;
	
	private JPanel rootPanel;
	
	private JPanel mainMenuPanel;
	private JPanel menuButtonPanel;
	private JPanel mainMenuImagePanel;
	private JPanel ingamePanel;
	
	private JPanel topPanel;
	private JPanel sidePanel;
	
	private JPanel connectionControlsPanel;
	private JPanel lobbyInfo;
	private JTextField nameTextField;
	
	private JButton makeWorldButton;
	private JButton startGameButton;

	private GameView gameView;
	private SelectedThingsView gameViewOverlay;
	private ResourceView resourceView;
	private InfoPanelView infoPanelView;

//	private JPanel actionsParentPanel;
	private ActionsView actionsView;
	private JTabbedPane tabbedPane;
	private int RESEARCH_TAB;
	private int ACTIONS_TAB;
//	private int CRAFTING_FOCUS_TAB; // for selecting crafting for autocrafters
//	private int PRODUCE_UNIT_TAB;
//	private int CRAFTING_TAB;
	private int SPAWN_UNITS_TAB;
	private int DEBUG_TAB;
	private ResearchView researchView;
	private WorkerView workerView;
	private ProduceUnitView produceUnitView;
	private CraftingView craftingView;
	private CraftingFocusView craftingFocusView;
	private KButton idleWorkerButton;
	private KButton idleUnitButton;
	
	public ClientGUI() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setFocusable(false);
		
		mainMenuPanel = new JPanel();
		mainMenuPanel.setLayout(new BorderLayout());
		mainMenuPanel.setFocusable(false);
		mainMenuPanel.setBackground(Color.black);
		
		ingamePanel = new JPanel();
		ingamePanel.setLayout(new BorderLayout());
		ingamePanel.setFocusable(false);

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setFocusable(false);
		
		sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setFocusable(false);
		sidePanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, 0));
		
//		actionsParentPanel = new JPanel();
//		actionsParentPanel.setLayout(new BorderLayout());
//		actionsParentPanel.setFocusable(false);
//		actionsParentPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, 0));

		ingamePanel.add(topPanel, BorderLayout.NORTH);
		ingamePanel.add(sidePanel, BorderLayout.EAST);
		

		KButton singlePlayer = KUIConstants.setupButton("Single Player", SINGLE_PLAYER_ICON, MAIN_MENU_BUTTON_SIZE);
		singlePlayer.setHorizontalAlignment(SwingConstants.CENTER);
		singlePlayer.addActionListener(e -> {
			client.setupSinglePlayer(true);
		});
		singlePlayer.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextField ipTextField = KUIConstants.setupTextField("localhost", MAIN_MENU_BUTTON_SIZE);
		ipTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
		KButton startButton = KUIConstants.setupButton("Custom IP", LAN_ICON, MAIN_MENU_BUTTON_SIZE);
		startButton.setHorizontalAlignment(SwingConstants.CENTER);
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.addActionListener(e -> {
			connectToServer(ipTextField.getText());
		});
		KButton startLocalHostButton = KUIConstants.setupButton("Local Host", null, MAIN_MENU_BUTTON_SIZE);
		startLocalHostButton.setHorizontalAlignment(SwingConstants.CENTER);
		startLocalHostButton.setToolTipText("localhost");
		startLocalHostButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startLocalHostButton.addActionListener(e -> {
			connectToServer("localhost");
		});
		KButton startAzureButton = KUIConstants.setupButton("Azure Server", AZURE_LOGO, MAIN_MENU_BUTTON_SIZE);
		startAzureButton.setHorizontalAlignment(SwingConstants.CENTER);
		startAzureButton.setToolTipText(AZURE_SERVER_IP);
		startAzureButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startAzureButton.addActionListener(e -> {
			connectToServer(AZURE_SERVER_IP);
		});

		KButton loadGameButton = KUIConstants.setupButton("Load Game", null, MAIN_MENU_BUTTON_SIZE);
		loadGameButton.setHorizontalAlignment(SwingConstants.CENTER);
		loadGameButton.setToolTipText("Loads game from save1.civ");
		loadGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadGameButton.addActionListener(e -> {
			client.loadGame();
		});
		
		Dimension colorButtonSize = new Dimension(MAIN_MENU_BUTTON_SIZE.height + 8, MAIN_MENU_BUTTON_SIZE.height);
		nameTextField = KUIConstants.setupTextField(Settings.DEFAULT_PLAYER_NAME, new Dimension(MAIN_MENU_BUTTON_SIZE.width - colorButtonSize.width, MAIN_MENU_BUTTON_SIZE.height));
		nameTextField.setToolTipText("Choose your faction's name");
		nameTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
			@Override public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
			@Override
			public void changedUpdate(DocumentEvent e) {
				Settings.DEFAULT_PLAYER_NAME = nameTextField.getText();
			}
		});
		KButton colorButton = KUIConstants.setupButton("", null, colorButtonSize);
		colorButton.setToolTipText("Choose your faction's color");
		colorButton.setBorder(BorderFactory.createLineBorder(new Color(Settings.DEFAULT_PLAYER_COLOR), 10));
		colorButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(rootPanel, "Choose Color", colorButton.getBackground());
			if(newColor != null) {
				Settings.DEFAULT_PLAYER_COLOR = newColor.getRGB();
				colorButton.setBorder(BorderFactory.createLineBorder(new Color(Settings.DEFAULT_PLAYER_COLOR), 10));
			}
			resetFocus();
		});

		JPanel playerInfoPanel = new JPanel();
		playerInfoPanel.setFocusable(false);
		playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.X_AXIS));
		playerInfoPanel.add(nameTextField);
		playerInfoPanel.add(colorButton);
		
		KButton settingsMenuButton = KUIConstants.setupButton("Settings", null, MAIN_MENU_BUTTON_SIZE);
		settingsMenuButton.setHorizontalAlignment(SwingConstants.CENTER);
		settingsMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		settingsMenuButton.addActionListener(e -> {
			switchToSettingsMenu();
		});
		
		menuButtonPanel = new JPanel();
		menuButtonPanel.setOpaque(false);
		menuButtonPanel.setLayout(new BoxLayout(menuButtonPanel, BoxLayout.Y_AXIS));
		menuButtonPanel.setFocusable(false);

		int padding = 30;
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding*5)));
		menuButtonPanel.add(playerInfoPanel);
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		menuButtonPanel.add(singlePlayer);
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		menuButtonPanel.add(loadGameButton);
		if(Settings.DEBUG) {
			menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
			menuButtonPanel.add(startLocalHostButton);
		}
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		menuButtonPanel.add(startAzureButton);
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		menuButtonPanel.add(ipTextField);
		menuButtonPanel.add(startButton);

		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		menuButtonPanel.add(settingsMenuButton);
		menuButtonPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		
		menuButtonPanel.add(Box.createVerticalGlue());
		
		mainMenuPanel.add(menuButtonPanel, BorderLayout.CENTER);
		mainMenuImagePanel = new MainMenuImageView();
		mainMenuPanel.add(mainMenuImagePanel, BorderLayout.SOUTH);

		rootPanel.add(mainMenuPanel, BorderLayout.CENTER);
		
		connectionControlsPanel = new JPanel();
		connectionControlsPanel.setFocusable(false);
		connectionControlsPanel.setLayout(new BoxLayout(connectionControlsPanel, BoxLayout.X_AXIS));

		lobbyInfo = new JPanel();
		lobbyInfo.setFocusable(false);
		lobbyInfo.setLayout(new BoxLayout(lobbyInfo, BoxLayout.X_AXIS));
		connectionControlsPanel.add(lobbyInfo);
		
		
		connectionControlsPanel.add(Box.createHorizontalGlue());
		KButton disconnectButton = KUIConstants.setupButton("Disconnect", null, CONNECTION_MENU_BUTTON_SIZE);
		disconnectButton.addActionListener(e -> {
			client.disconnect();
			resetFocus();
		});
		connectionControlsPanel.add(disconnectButton);

		makeWorldButton = KUIConstants.setupButton("Make World", null, CONNECTION_MENU_BUTTON_SIZE);
		makeWorldButton.addActionListener(e -> {
			makeWorldButton.setEnabled(false);
			client.sendMessage(new ClientMessage(ClientMessageType.MAKE_WORLD, null));
			resetFocus();
		});
		connectionControlsPanel.add(makeWorldButton);

		startGameButton = KUIConstants.setupButton("Start Game", null, CONNECTION_MENU_BUTTON_SIZE);
		startGameButton.addActionListener(e -> {
			startGameButton.setEnabled(false);
			client.sendMessage(new ClientMessage(ClientMessageType.START_GAME, null));
			resetFocus();
		});
		connectionControlsPanel.add(startGameButton);
		startGameButton.setEnabled(false);
		
//		topPanel.add(lobbyInfo, BorderLayout.CENTER);
	}
	
	private void connectToServer(String ip) {
		try {
			client.connectToServer(InetAddress.getByName(ip));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}
	
	public void updatedLobbyList(PlayerInfo[] lobbyList) {
		lobbyInfo.removeAll();
		for(PlayerInfo info : lobbyList) {
			JLabel playerLabel = new JLabel(info.getName(), SwingConstants.CENTER);
			playerLabel.setPreferredSize(new Dimension(CONNECTION_MENU_BUTTON_SIZE.height*3, CONNECTION_MENU_BUTTON_SIZE.height));
			playerLabel.setBorder(BorderFactory.createLineBorder(info.getColor(), 4, true));
			lobbyInfo.add(playerLabel);
		}
		lobbyInfo.revalidate();
		lobbyInfo.repaint();
	}
	
	public PlayerInfo getPlayerInfo() {
		return new PlayerInfo(nameTextField.getText(), new Color(Settings.DEFAULT_PLAYER_COLOR));
	}
	
	public void worldReceived() {
		startGameButton.setEnabled(true);
	}
	
	public void switchToMainMenu() {
		mainMenuPanel.removeAll();
		mainMenuPanel.add(menuButtonPanel, BorderLayout.CENTER);
		mainMenuPanel.add(mainMenuImagePanel, BorderLayout.SOUTH);
		resetFocus();
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public void switchToSettingsMenu() {
		SettingsMenu settingsMenu = new SettingsMenu(e -> switchToMainMenu());
		settingsMenu.addControlFor(Settings.class);
		mainMenuPanel.removeAll();
		mainMenuPanel.add(settingsMenu.getContentPanel(), BorderLayout.CENTER);
		mainMenuPanel.add(mainMenuImagePanel, BorderLayout.SOUTH);
		resetFocus();
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public void startedSinglePlayer() {
		rootPanel.remove(mainMenuPanel);
		rootPanel.add(ingamePanel);
		resetFocus();
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	public void connected(JPanel connectionInfo) {
		
		rootPanel.remove(mainMenuPanel);
		rootPanel.add(ingamePanel);
		
		topPanel.add(connectionControlsPanel, BorderLayout.NORTH);
		resetFocus();
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public void disconnected() {
		rootPanel.remove(ingamePanel);
		rootPanel.add(mainMenuPanel);
		topPanel.remove(connectionControlsPanel);
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	private void resetFocus() {
		if(gameView != null) {
			gameView.requestFocus();
		}
	}
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			ingamePanel.remove(gameView.getPanel());
		}
		gameViewOverlay = new SelectedThingsView(instance.getGUIController());
		resourceView = new ResourceView(instance.getGUIController());
		gameView = new GameView(instance, gameViewOverlay, resourceView);
		MinimapView minimapView = new MinimapView(gameView);
		minimapView.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, ClientGUI.GUIWIDTH));
		gameView.requestFocus();
		ingamePanel.add(gameView.getPanel(), BorderLayout.CENTER);
		sidePanel.add(minimapView, BorderLayout.NORTH);
		
		infoPanelView = new InfoPanelView();
		JPanel infoPanelViewRoot = infoPanelView.getRootPanel();
		infoPanelViewRoot.setBackground(instance.getBackgroundColor());
		infoPanelViewRoot.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, (int) (ClientGUI.GUIWIDTH / 2.5)));
		sidePanel.add(infoPanelViewRoot, BorderLayout.SOUTH);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(KUIConstants.buttonFontSmall);
		
		actionsView = new ActionsView(getGameView());

//		actionsParentPanel.add(actionsView, BorderLayout.NORTH);
//		actionsParentPanel.add(tabbedPane, BorderLayout.CENTER);
//		sidePanel.add(actionsParentPanel, BorderLayout.CENTER);
		sidePanel.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel overviewTab = new JPanel();
		overviewTab.setLayout(new GridBagLayout());
		overviewTab.setFocusable(false);
		overviewTab.setBackground(Color.red);
		int OVERVIEW_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, OVERVIEW_TAB_ICON, overviewTab, "Overview");

		GridBagConstraints cs = new GridBagConstraints();
		idleWorkerButton = KUIConstants.setupButton("Idle Workers (?)", null, new Dimension(160, 30));
		idleWorkerButton.addActionListener(e -> {
			Unit worker = gameView.getFaction().getIdleWorker();
			if (worker != null) {
				gameView.selectThing(worker, false);
			}
		});
		cs.gridx = 0;
		cs.gridy = 0;
		cs.weightx = 1;
		cs.anchor = GridBagConstraints.FIRST_LINE_START;
		overviewTab.add(idleWorkerButton, cs);
		idleUnitButton = KUIConstants.setupButton("Idle Units (?)", null, new Dimension(160, 30));
		idleUnitButton.addActionListener(e -> {
			Unit unit = gameView.getFaction().getIdleNonworker();
			if (unit != null) {
				gameView.selectThing(unit, false);
			}
		});
		cs.gridx = 0;
		cs.gridy = 1;
		overviewTab.add(idleUnitButton, cs);
		
		KButton gotoResearchButton = KUIConstants.setupButton("Research", RESEARCH_TAB_ICON, new Dimension(160, 30));
		gotoResearchButton.addActionListener(e -> tabbedPane.setSelectedIndex(RESEARCH_TAB));
		cs.gridx = 0;
		cs.gridy = 2;
		overviewTab.add(gotoResearchButton, cs);

		cs.gridx = 0;
		cs.gridy = 3;
		cs.gridwidth = 6;
		cs.gridheight = 3;
		cs.weighty = 1;
		cs.fill = GridBagConstraints.BOTH;
		
		overviewTab.add(new JPanel(), cs);
		cs.weighty = 0;
		cs.gridwidth = 1;
		cs.gridheight = 1;
		cs.fill = 0;
		

		if(Settings.DEBUG) {
			KButton gotoSpawnUnitsButton = KUIConstants.setupButton("", SPAWN_TAB_ICON, new Dimension(35, 30));
			gotoSpawnUnitsButton.addActionListener(e -> tabbedPane.setSelectedIndex(SPAWN_UNITS_TAB));
			cs.gridx = 4;
			cs.gridy = 6;
			cs.weightx = 0;
			cs.anchor = GridBagConstraints.LAST_LINE_END;
			overviewTab.add(gotoSpawnUnitsButton, cs);

			KButton gotoDebugViewButton = KUIConstants.setupButton("", DEBUG_TAB_ICON, new Dimension(35, 30));
			gotoDebugViewButton.addActionListener(e -> tabbedPane.setSelectedIndex(DEBUG_TAB));
			cs.gridx = 5;
			cs.gridy = 6;
			cs.weightx = 0;
			cs.anchor = GridBagConstraints.LAST_LINE_END;
			overviewTab.add(gotoDebugViewButton, cs);
		}
		
		researchView = new ResearchView(gameView);
		RESEARCH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, RESEARCH_TAB_ICON, researchView.getRootPanel(), "Research new technologies");

		workerView = new WorkerView(gameView);
		ACTIONS_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, WORKER_TAB_ICON, actionsView.getRootPanel(), "Actions", ACTIONS_TAB);

		produceUnitView = new ProduceUnitView(gameView);
//		PRODUCE_UNIT_TAB = tabbedPane.getTabCount();
//		tabbedPane.insertTab(null, PRODUCE_UNIT_TAB_ICON, produceUnitView.getRootPanel(), "Make units from castles, barracks, workshops, or stables", PRODUCE_UNIT_TAB);

		craftingView = new CraftingView(gameView);
//		CRAFTING_TAB = tabbedPane.getTabCount();
//		tabbedPane.insertTab(null, BLACKSMITH_TAB_ICON, craftingView.getRootPanel(), "Craft items", CRAFTING_TAB);
		
		craftingFocusView = new CraftingFocusView(gameView);
//		CRAFTING_FOCUS_TAB = tabbedPane.getTabCount();
//		tabbedPane.insertTab(null, CRAFTING_FOCUS_TAB_ICON, craftingFocusView.getRootPanel(), "Select Items to focus", CRAFTING_FOCUS_TAB);
		
		
		
		if(Settings.DEBUG) {
			SpawnUnitsView spawnUnitsView = new SpawnUnitsView(gameView);
			SPAWN_UNITS_TAB = tabbedPane.getTabCount();
			tabbedPane.insertTab(null, SPAWN_TAB_ICON, spawnUnitsView.getRootPanel(), "Summon units for testing", SPAWN_UNITS_TAB);
			
			DebugView debugView = new DebugView(gameView);
			DEBUG_TAB = tabbedPane.getTabCount();
			tabbedPane.insertTab(null, DEBUG_TAB_ICON, debugView.getRootPanel(), "Various testing functions", DEBUG_TAB);
		}
		
		// disable building tab after setting all of the tabs up
//		manageBuildingTab(false);
//		manageProduceUnitTab(false);
//		tabbedPane.setEnabledAt(CRAFTING_TAB, false);
		
		actionsView.addViews(produceUnitView, craftingView, workerView, craftingFocusView);
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	public void updateTerrainImages() {
		if(gameView != null) {
			gameView.updateTerrainImages();
		}
	}
	
	public void repaint() {
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public JPanel getMainPanel() {
		return rootPanel;
	}
	
	public SelectedThingsView getGameViewOverlay() {
		return gameViewOverlay;
	}
	public ResourceView getResourceView() {
		return resourceView;
	}
	public GameView getGameView() {
		return gameView;
	}
	public InfoPanelView getInfoPanelView() {
		return infoPanelView;
	}
	
	public ResearchView getResearchView() {
		return researchView;
	}
	
	public WorkerView getWorkerView() {
		return workerView;
	}
	
	public ProduceUnitView getProduceUnitView() {
		return produceUnitView;
	}
	
	public CraftingView getCraftingView() {
		return craftingView;
	}
	public CraftingFocusView getCraftingFocusView() {
		return craftingFocusView;
	}

	public void manageActionsTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == ACTIONS_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(ACTIONS_TAB);
		}
		tabbedPane.setEnabledAt(ACTIONS_TAB, enabled);
	}
	
//	public void manageProduceUnitTab(boolean enabled) {
//		if(enabled) {
//			tabbedPane.setEnabledAt(PRODUCE_UNIT_TAB, enabled);
//			tabbedPane.setSelectedIndex(PRODUCE_UNIT_TAB);
//		}
//		else {
//			if(gameView.getGameInstance().world != null) {
//				if(!(gameView.getFaction().isBuildingSelected(Game.buildingTypeMap.get("CASTLE"))
//						|| gameView.getFaction().isBuildingSelected(Game.buildingTypeMap.get("BARRACKS"))
//						|| gameView.getFaction().isBuildingSelected(Game.buildingTypeMap.get("STABLES"))
//						|| gameView.getFaction().isBuildingSelected(Game.buildingTypeMap.get("WORKSHOP")))) {
//					if(tabbedPane.getSelectedIndex() == PRODUCE_UNIT_TAB) {
//						tabbedPane.setSelectedIndex(0);
//					}
//				}
//			}
//			tabbedPane.setEnabledAt(PRODUCE_UNIT_TAB, false);
//		}
//	}

//	public void manageCraftUpgradesTab(boolean enabled) {
//		if (enabled == false && tabbedPane.getSelectedIndex() == CRAFTING_TAB) {
//			tabbedPane.setSelectedIndex(0);
//		} else if (enabled == true) {
//			tabbedPane.setSelectedIndex(CRAFTING_TAB);
//		}
//		tabbedPane.setEnabledAt(CRAFTING_TAB, true);
//	}
//	
//	public void manageCraftingFocusTab(boolean enabled) {
//		if (enabled == false && tabbedPane.getSelectedIndex() == CRAFTING_FOCUS_TAB) {
//			tabbedPane.setSelectedIndex(0);
//		} else if (enabled == true) {
//			tabbedPane.setSelectedIndex(CRAFTING_FOCUS_TAB);
//		}
//		tabbedPane.setEnabledAt(CRAFTING_FOCUS_TAB, true);
//	}

	public void changedFaction(Faction faction) {
		getGameView().setFaction(faction);
		getResourceView().changeFaction(faction);
	}

	public void selectedBuilding(Building building, boolean selected) {
		updateViews();
		manageActionsTab(actionsView.selectedBuilding(building, selected));
//		if(building.getType().unitsCanProduceSet().size() > 0) {
//			manageProduceUnitTab(selected);
//		}
//		if (building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
//			manageBlacksmithTab(selected);
//		}
		if(selected) {
			InfoPanel infoPanel = new BuildingInfoPanel(building);
			getInfoPanelView().switchInfoPanel(infoPanel);
			SwingUtilities.invokeLater(() -> {
				infoPanel.addExplodeButton().addActionListener(e -> gameView.getGameInstance().explode(building));
			});
		}
// 			TODO fix this
//		if (building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
//			manageCraftUpgradesTab(selected);
//		}
		// TODO fix this
//		if (building.getType() == Game.buildingTypeMap.get("SMITHY") || building.getType() == Game.buildingTypeMap.get("QUARRY")
//				|| building.getType() == Game.buildingTypeMap.get("SAWMILL")) {
//			manageCraftingFocusTab(selected);
//		}
	}

	public void selectedUnit(Unit unit, boolean selected) {
		updateViews();
		getGameViewOverlay().selectedUnit(unit, selected);
		manageActionsTab(actionsView.selectedUnit(unit, selected));
		
		if(selected) {
//			manageActionsTab(selected);
			UnitInfoPanel infoPanel = new UnitInfoPanel(unit);
			getInfoPanelView().switchInfoPanel(infoPanel);
			SwingUtilities.invokeLater(() -> {
				infoPanel.addExplodeButton().addActionListener(e -> gameView.getGameInstance().explode(unit));
			});
		}
	}
	
	public void updateViews() {
		int idleWorkers = 0;
		int idleNonworkers = 0;
		for (Unit unit : gameView.getFaction().getUnits()) {
			if (unit.isIdle()) {
				if (unit.isBuilder()) {
					idleWorkers++;
				}
				else {
					idleNonworkers++;
				}
			}
		}
		
		idleWorkerButton.setText("Idle Workers (" + idleWorkers + ")");
		idleUnitButton.setText("Idle Units (" + idleNonworkers + ")");
		
		getResourceView().updateItems();
		getWorkerView().updateButtons();
		getResearchView().updateButtons();
		getProduceUnitView().updateButtons();
		getCraftingView().updateButtons();
		getCraftingFocusView().updateButtons();
	}
}
