package networking.view;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import networking.server.*;
import ui.*;

public class ServerGUI extends JPanel {

//	private Server server;
	private JLabel info;
	private JPanel connectionPanelBar;
	private HashSet<JPanel> connectionPanels = new HashSet<>();
	
	private Game gameInstance;
	
	public ServerGUI() {
		
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		connectionPanelBar = new JPanel();
		connectionPanelBar.setLayout(new BoxLayout(connectionPanelBar, BoxLayout.X_AXIS));
		
		info = new JLabel("info");
		
		topPanel.add(info, BorderLayout.NORTH);
		topPanel.add(connectionPanelBar, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.NORTH);
		
		JPanel gameView = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(gameInstance != null) {
					gameInstance.draw(g, getWidth(), getHeight());
				}
			}
		};
		this.add(gameView, BorderLayout.CENTER);
	}
	
	public void setGameInstance(Game instance) {
		this.gameInstance = instance;
		repaint();
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
	
//	public void setServer(Server server) {
//		this.server = server;
//	}
}
