package networking.view;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import game.*;
import networking.server.*;
import ui.*;
import utils.*;
import world.*;

public class ServerGUI extends JPanel {

//	private Server server;
	private JLabel info;
	private GameView gameView;
	private CommandInterface commandInterface;
	private JPanel connectionPanelBar;
	private HashSet<JPanel> connectionPanels = new HashSet<>();
	
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
	}
	
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			this.remove(gameView);
		}
		commandInterface = Utils.makeFunctionalCommandInterface();
		gameView = new GameView(instance, commandInterface);
		this.add(gameView, BorderLayout.CENTER);
		revalidate();
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
	
	public CommandInterface getCommandInterface() {
		return commandInterface;
	}
	
//	public void setServer(Server server) {
//		this.server = server;
//	}
}
