package networking.view;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import networking.*;
import networking.client.*;
import networking.message.*;
import networking.server.*;

public class ClientGUI extends JPanel {
	
	private Client client;
	
	private JPanel connectPanel;
	private JPanel myinfoPanel;
//	private JPanel connectionInfo;
	private JPanel lobbyInfo;
	
	private JTextField messageTextField;
	
	public ClientGUI() {

		this.setLayout(new BorderLayout());
		
		connectPanel = new JPanel();
		connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
		
		JTextField ipTextField = new JTextField("localhost", 12);
		connectPanel.add(ipTextField);

		JButton startButton = new JButton("Connect");
		startButton.addActionListener(e -> {
			try {
				client.connectToServer(InetAddress.getByName(ipTextField.getText()));
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		});
		connectPanel.add(startButton);
		this.add(connectPanel, BorderLayout.NORTH);
		
		myinfoPanel = new JPanel();
		myinfoPanel.setLayout(new BoxLayout(myinfoPanel, BoxLayout.X_AXIS));

		messageTextField = new JTextField(Server.DEFAULT_PLAYER_INFO.getName(), 16);
		myinfoPanel.add(messageTextField);

		JButton colorButton = new JButton("Pick Color");
		colorButton.setBackground(Server.DEFAULT_PLAYER_INFO.getColor());
		colorButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(this, "Choose Color", colorButton.getBackground());
			if(newColor != null) {
				colorButton.setBackground(newColor);
			}
		});
		myinfoPanel.add(colorButton);
		
		JButton updateInfoButton = new JButton("Update Info");
		updateInfoButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.INFO, new PlayerInfo(messageTextField.getText(), colorButton.getBackground())));
		});
		myinfoPanel.add(updateInfoButton);

		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(e -> {
			client.disconnect();
		});
		myinfoPanel.add(disconnectButton);
		
		lobbyInfo = new JPanel();
		lobbyInfo.setLayout(new BoxLayout(lobbyInfo, BoxLayout.Y_AXIS));
		this.add(lobbyInfo, BorderLayout.CENTER);
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
	
	public void connected(JPanel connectionInfo) {
//		this.connectionInfo = connectionInfo;
		this.remove(connectPanel);
//		myinfoPanel.add(connectionInfo);
		this.add(myinfoPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
	}
	
	public void disconnected() {
//		myinfoPanel.remove(connectionInfo);
//		connectionInfo = null;
		this.remove(myinfoPanel);
		this.add(connectPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
		
	}
	
	public void setClient(Client client) {
		this.client = client;
	}

}
