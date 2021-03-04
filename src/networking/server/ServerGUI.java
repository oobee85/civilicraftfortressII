package networking.server;

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
	private GameView gameView;
	private JPanel connectionPanelBar;
	private HashSet<JPanel> connectionPanels = new HashSet<>();
	
	public ServerGUI() {
		
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		connectionPanelBar = new JPanel();
		connectionPanelBar.setLayout(new BoxLayout(connectionPanelBar, BoxLayout.X_AXIS));
		
		info = new JLabel("Failed to start ServerSocket");
		
		topPanel.add(info, BorderLayout.NORTH);
		topPanel.add(connectionPanelBar, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.NORTH);
	}
	
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			this.remove(gameView.getPanel());
		}
		gameView = new GameView(instance);
		gameView.setCommandInterface(Utils.makeFunctionalCommandInterface(instance));
		gameView.requestFocus();
		this.add(gameView.getPanel(), BorderLayout.CENTER);
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
