package networking.client;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import game.*;
import networking.message.*;
import networking.server.*;
import networking.view.*;
import ui.*;
import ui.Frame;
import utils.*;
import world.*;

public class ClientGUI {

	private static final int TAB_ICON_SIZE = 25;
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon WORKER_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/building.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon PRODUCE_UNIT_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/barracks.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private static final ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/crafting.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	
	private static final Dimension MAIN_MENU_BUTTON_SIZE = new Dimension(200, 40);

	
	private Client client;
	
	private JPanel rootPanel;
	
	private JPanel mainMenuPanel;
	private JPanel ingamePanel;
	
	private JPanel topPanel;
	private JPanel sidePanel;
	
	private JPanel playerinfoPanel;
	private JPanel lobbyInfo;
	private JTextField nameTextField;
	private Color selectedColor = Server.DEFAULT_PLAYER_INFO.getColor();
	
	private JButton makeWorldButton;
	private JButton startGameButton;

	private GameView gameView;
	private GameViewOverlay gameViewOverlay;
	private InfoPanelView infoPanelView;

	private JTabbedPane tabbedPane;
	private int RESEARCH_TAB;
	private int WORKER_TAB;
	private int PRODUCE_UNIT_TAB;
	private int CRAFTING_TAB;
	private ResearchView researchView;
	private WorkerView workerView;
	private ProduceUnitView produceUnitView;
	private CraftingView craftingView;
	
	public ClientGUI() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setFocusable(false);
		
		mainMenuPanel = new JPanel();
		mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));
		mainMenuPanel.setFocusable(false);
		
		ingamePanel = new JPanel();
		ingamePanel.setLayout(new BorderLayout());
		ingamePanel.setFocusable(false);

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setFocusable(false);
		
		sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setFocusable(false);
		sidePanel.setPreferredSize(new Dimension(Frame.GUIWIDTH, 0));

		ingamePanel.add(topPanel, BorderLayout.NORTH);
		ingamePanel.add(sidePanel, BorderLayout.EAST);
		

		KButton singlePlayer = KUIConstants.setupButton("Single Player", null, MAIN_MENU_BUTTON_SIZE);
		singlePlayer.addActionListener(e -> {
			client.setupSinglePlayer();
		});
		singlePlayer.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextField ipTextField = KUIConstants.setupTextField("localhost", MAIN_MENU_BUTTON_SIZE);
		ipTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
		KButton startButton = KUIConstants.setupButton("Multiplayer", null, MAIN_MENU_BUTTON_SIZE);
		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.addActionListener(e -> {
			try {
				client.connectToServer(InetAddress.getByName(ipTextField.getText()));
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		});

		int padding = 20;
		mainMenuPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		mainMenuPanel.add(singlePlayer);
		mainMenuPanel.add(Box.createRigidArea(new Dimension(0, padding*2)));
		mainMenuPanel.add(ipTextField);
		mainMenuPanel.add(Box.createRigidArea(new Dimension(0, padding/2)));
		mainMenuPanel.add(startButton);
		mainMenuPanel.add(Box.createVerticalGlue());

		rootPanel.add(mainMenuPanel, BorderLayout.CENTER);
		
		playerinfoPanel = new JPanel();
		playerinfoPanel.setFocusable(false);
		playerinfoPanel.setLayout(new BoxLayout(playerinfoPanel, BoxLayout.X_AXIS));

		nameTextField = KUIConstants.setupTextField(Server.DEFAULT_PLAYER_INFO.getName(), null);
		nameTextField.setMaximumSize(new Dimension(120, 999));
		playerinfoPanel.add(nameTextField);

		KButton colorButton = KUIConstants.setupButton("Pick Color", null, null);
		colorButton.setBorder(BorderFactory.createLineBorder(selectedColor, 10));
		colorButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(rootPanel, "Choose Color", colorButton.getBackground());
			if(newColor != null) {
				selectedColor = newColor;
				colorButton.setBorder(BorderFactory.createLineBorder(selectedColor, 10));
			}
			resetFocus();
		});
		playerinfoPanel.add(colorButton);

		KButton updateInfoButton = KUIConstants.setupButton("Update Info", null, null);
		updateInfoButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.INFO, new PlayerInfo(nameTextField.getText(), selectedColor)));
			resetFocus();
		});
		playerinfoPanel.add(updateInfoButton);

		playerinfoPanel.add(Box.createHorizontalGlue());
		KButton disconnectButton = KUIConstants.setupButton("Disconnect", null, null);
		disconnectButton.addActionListener(e -> {
			client.disconnect();
			resetFocus();
		});
		playerinfoPanel.add(disconnectButton);

		makeWorldButton = KUIConstants.setupButton("Make World", null, null);
		makeWorldButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.MAKE_WORLD, null));
			resetFocus();
		});
		playerinfoPanel.add(makeWorldButton);

		startGameButton = KUIConstants.setupButton("Start Game", null, null);
		startGameButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.START_GAME, null));
			resetFocus();
		});
		playerinfoPanel.add(startGameButton);
		startGameButton.setEnabled(false);
		
		lobbyInfo = new JPanel();
		lobbyInfo.setFocusable(false);
		lobbyInfo.setLayout(new BoxLayout(lobbyInfo, BoxLayout.X_AXIS));
		topPanel.add(lobbyInfo, BorderLayout.CENTER);
	}
	
	public void updatedLobbyList(PlayerInfo[] lobbyList) {
		lobbyInfo.removeAll();
		for(PlayerInfo info : lobbyList) {
			JLabel playerLabel = new JLabel(info.getName());
			playerLabel.setBorder(BorderFactory.createLineBorder(info.getColor(), 4, true));
			lobbyInfo.add(playerLabel);
		}
		lobbyInfo.revalidate();
		lobbyInfo.repaint();
	}
	
	public void worldReceived() {
		startGameButton.setEnabled(true);
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
		resetFocus();
		
		topPanel.add(playerinfoPanel, BorderLayout.NORTH);
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	
	public void disconnected() {
		rootPanel.remove(ingamePanel);
		rootPanel.add(mainMenuPanel);
		topPanel.remove(playerinfoPanel);
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
			ingamePanel.remove(gameView);
		}
		gameView = new GameView(instance);
		MinimapView minimapView = new MinimapView(gameView);
		minimapView.setPreferredSize(new Dimension(Frame.GUIWIDTH, Frame.GUIWIDTH));
		gameView.requestFocus();
		gameViewOverlay = new GameViewOverlay(instance.getGUIController());
		gameViewOverlay.changeFaction(World.PLAYER_FACTION);
		gameView.setLayout(new BorderLayout());
		gameView.add(gameViewOverlay, BorderLayout.CENTER);
		ingamePanel.add(gameView, BorderLayout.CENTER);
		sidePanel.add(minimapView, BorderLayout.NORTH);
		

		infoPanelView = new InfoPanelView();
		JPanel infoPanelViewRoot = infoPanelView.getRootPanel();
		infoPanelViewRoot.setBackground(instance.getBackgroundColor());
		infoPanelViewRoot.setPreferredSize(new Dimension(Frame.GUIWIDTH, (int) (Frame.GUIWIDTH / 2.5)));
		sidePanel.add(infoPanelViewRoot, BorderLayout.SOUTH);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(KUIConstants.buttonFontSmall);
		sidePanel.add(tabbedPane, BorderLayout.CENTER);
		
		researchView = new ResearchView(gameView.getGameInstance().getGUIController());
		RESEARCH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, RESEARCH_TAB_ICON, researchView.getRootPanel(), "Research new technologies");

		workerView = new WorkerView(gameView);
		WORKER_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, WORKER_TAB_ICON, workerView.getRootPanel(), "Build buildings with workers", WORKER_TAB);

		produceUnitView = new ProduceUnitView(gameView);
		PRODUCE_UNIT_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, PRODUCE_UNIT_TAB_ICON, produceUnitView.getRootPanel(), "Make units from castles, barracks, or workshops", PRODUCE_UNIT_TAB);

		craftingView = new CraftingView(gameView);
		CRAFTING_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, BLACKSMITH_TAB_ICON, craftingView.getRootPanel(), "Craft items", CRAFTING_TAB);
		
		
		// disable building tab after setting all of the tabs up
		manageBuildingTab(false);
		manageProduceUnitTab(false);
		manageBlacksmithTab(false);
//		manageSpawnTab(true);
		
		rootPanel.revalidate();
		rootPanel.repaint();
	}
	public void updateTerrainImages() {
		if(gameView != null) {
			gameView.updateTerrainImages();
		}
	}
	
	public void repaint() {
		rootPanel.repaint();
	}
	
	public JPanel getMainPanel() {
		return rootPanel;
	}
	
	public GameViewOverlay getGameViewOverlay() {
		return gameViewOverlay;
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

	public void manageBuildingTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == WORKER_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(WORKER_TAB);
		}
		tabbedPane.setEnabledAt(WORKER_TAB, enabled);
	}
	
	public void manageProduceUnitTab(boolean enabled) {
		if(enabled) {
			tabbedPane.setEnabledAt(PRODUCE_UNIT_TAB, enabled);
			tabbedPane.setSelectedIndex(PRODUCE_UNIT_TAB);
		}
		else {
			if(gameView.getGameInstance().world != null) {
				if(!(World.PLAYER_FACTION.isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("CASTLE"))
						|| World.PLAYER_FACTION.isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("BARRACKS"))
						|| World.PLAYER_FACTION.isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("WORKSHOP")))) {
					if(tabbedPane.getSelectedIndex() == PRODUCE_UNIT_TAB) {
						tabbedPane.setSelectedIndex(0);
					}
				}
			}
			tabbedPane.setEnabledAt(PRODUCE_UNIT_TAB, false);
		}
	}

	public void manageBlacksmithTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == CRAFTING_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(CRAFTING_TAB);
		}
		tabbedPane.setEnabledAt(CRAFTING_TAB, enabled);
	}

}
