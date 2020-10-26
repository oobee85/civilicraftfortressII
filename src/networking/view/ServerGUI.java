package networking.view;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import networking.server.*;

public class ServerGUI extends JPanel {

//	private Server server;
	private JLabel info;
	private HashSet<JPanel> connectionPanels = new HashSet<>();
	public ServerGUI() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		info = new JLabel("info");
		
		this.add(info);
		
//		JPanel blank = new JPanel();
//		blank.setOpaque(false);
//		constraints.gridx = 0;
//		constraints.gridy = 1;
//		constraints.weightx = 1; 
//		constraints.weighty = 1;
//		constraints.fill = GridBagConstraints.BOTH;
//		this.add(blank, constraints);
	}
	
	public void addedConnection(JPanel panel) {
		if(!connectionPanels.contains(panel)) {
			connectionPanels.add(panel);
			this.add(panel);
			this.revalidate();
			this.repaint();
		}
	}
	
	public void lostConnection(JPanel panel) {
		if(connectionPanels.contains(panel)) {
			connectionPanels.remove(panel);
			this.remove(panel);
			this.revalidate();
			this.repaint();
		}
	}
	
	public void updateInfo(String info) {
		this.info.setText(info);
	}
	
//	public void setServer(Server server) {
//		this.server = server;
//	}
}
