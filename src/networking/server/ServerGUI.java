package networking.server;

import static ui.KUIConstants.MAIN_MENU_BUTTON_SIZE;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.view.*;
import utils.*;

public class ServerGUI extends JPanel {

//	private Server server;
	private JLabel info;
	private JPanel ingamePanel;
	private JPanel mainMenuPanel;
	private JPanel menuButtonPanel;
	private ArrayList<JComponent> mainMenuButtons = new ArrayList<>();
	private GameView gameView;
	private JPanel connectionPanelBar;
	private HashSet<JPanel> connectionPanels = new HashSet<>();
	
	public ServerGUI() {
		
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		connectionPanelBar = new JPanel();
		connectionPanelBar.setLayout(new BoxLayout(connectionPanelBar, BoxLayout.Y_AXIS));
		connectionPanelBar.setOpaque(false);
		
		info = new JLabel("Failed to start ServerSocket");
		
		mainMenuPanel = new JPanel();
		mainMenuPanel.setLayout(new BorderLayout());
		mainMenuPanel.setFocusable(false);
		mainMenuPanel.setBackground(Color.black);
		MainMenuImageView mainMenuImagePanel = new MainMenuImageView();
		mainMenuPanel.add(mainMenuImagePanel, BorderLayout.SOUTH);

		KButton settingsMenuButton = KUIConstants.setupButton("Settings", null, MAIN_MENU_BUTTON_SIZE);
		settingsMenuButton.setHorizontalAlignment(SwingConstants.CENTER);
		settingsMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		settingsMenuButton.addActionListener(e -> {
			switchToSettingsMenu();
		});
		mainMenuButtons.add(settingsMenuButton);
		
		menuButtonPanel = new JPanel();
		menuButtonPanel.setOpaque(false);
		menuButtonPanel.setLayout(new BoxLayout(menuButtonPanel, BoxLayout.Y_AXIS));
		menuButtonPanel.setFocusable(false);
		
		for (JComponent c : mainMenuButtons) {
			menuButtonPanel.add(c);
		}
		
		mainMenuPanel.add(menuButtonPanel, BorderLayout.CENTER);
		
		topPanel.add(info, BorderLayout.NORTH);
		this.add(topPanel, BorderLayout.NORTH);
		this.add(connectionPanelBar, BorderLayout.WEST);
		this.add(mainMenuPanel, BorderLayout.CENTER);
		
		ingamePanel = new JPanel();
		ingamePanel.setLayout(new BorderLayout());
		ingamePanel.setFocusable(false);
	}
	
	public void switchToMainMenu() {
		menuButtonPanel.removeAll();
		for (JComponent c : mainMenuButtons) {
			menuButtonPanel.add(c);
		}
		
		mainMenuPanel.revalidate();
		mainMenuPanel.repaint();
	}
	
	public void switchToSettingsMenu() {
		SettingsMenu settingsMenu = new SettingsMenu(e -> switchToMainMenu());
		settingsMenu.addControlFor(Settings.class);
		menuButtonPanel.removeAll();
		menuButtonPanel.add(settingsMenu.getContentPanel(), BorderLayout.CENTER);

		mainMenuPanel.revalidate();
		mainMenuPanel.repaint();
	}
	
	public void switchToGame() {
		this.remove(mainMenuPanel);
		this.add(ingamePanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
	
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			ingamePanel.remove(gameView.getPanel());
		}
		gameView = new GameView(instance, null, null);
		gameView.setCommandInterface(Utils.makeFunctionalCommandInterface(instance));
		gameView.requestFocus();
		
		ingamePanel.add(gameView.getPanel(), BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void updateTerrainImages() {
		if(gameView != null) {
			gameView.updateTerrainImages();
		}
	}
	public GameView getGameView() {
		return gameView;
	}
	
	public void addedConnection(JPanel panel) {
		if(!connectionPanels.contains(panel)) {
			connectionPanels.add(panel);
			connectionPanelBar.add(panel);
			connectionPanelBar.revalidate();
			connectionPanelBar.repaint();
		}
	}
	
	public void lostConnection(JPanel panel) {
		if(connectionPanels.contains(panel)) {
			connectionPanels.remove(panel);
			connectionPanelBar.remove(panel);
			connectionPanelBar.revalidate();
			connectionPanelBar.repaint();
		}
	}
	
	public void updateInfo(String info) {
		this.info.setText(info);
	}
	
	public CommandInterface getCommandInterface() {
		return gameView.getCommandInterface();
	}
	
//	public void setServer(Server server) {
//		this.server = server;
//	}
}
