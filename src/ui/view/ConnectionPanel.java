package ui.view;

import java.awt.*;

import javax.swing.*;

import networking.server.*;

public class ConnectionPanel extends JPanel {
	
	private JLabel nameField;
	private JLabel colorField;
	private JLabel myipField;
	private JLabel connectedToipField;

	public ConnectionPanel(String myip, String connectedToIP) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.setBorder(BorderFactory.createLineBorder(Color.black, 3, true));
		
		nameField = new JLabel("");
		this.add(nameField);
		myipField = new JLabel(myip);
		this.add(myipField);
		connectedToipField = new JLabel(connectedToIP);
		this.add(connectedToipField);
	}
	
	public void updatePlayerInfo(PlayerInfo info) {
		nameField.setText(info.getName());
		this.setBorder(BorderFactory.createLineBorder(info.getColor(), 4, true));
		repaint();
	}
}
