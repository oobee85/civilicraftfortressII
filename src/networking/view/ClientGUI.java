package networking.view;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import networking.*;
import networking.client.*;
import networking.message.*;
import networking.server.*;
import ui.*;

public class ClientGUI extends JPanel {
	
	private Client client;
	
	private JPanel topPanel;
	
	private JPanel connectPanel;
	private JPanel myinfoPanel;
//	private JPanel connectionInfo;
	private JPanel lobbyInfo;
	private JTextField nameTextField;

	private GameView gameView;
	
	public ClientGUI() {

		this.setLayout(new BorderLayout());
		
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		this.add(topPanel, BorderLayout.NORTH);
		
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
		topPanel.add(connectPanel, BorderLayout.NORTH);
		
		myinfoPanel = new JPanel();
		myinfoPanel.setLayout(new BoxLayout(myinfoPanel, BoxLayout.X_AXIS));

		nameTextField = new JTextField(Server.DEFAULT_PLAYER_INFO.getName(), 16);
		myinfoPanel.add(nameTextField);

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
			client.sendMessage(new ClientMessage(ClientMessageType.INFO, new PlayerInfo(nameTextField.getText(), colorButton.getBackground())));
		});
		myinfoPanel.add(updateInfoButton);

		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(e -> {
			client.disconnect();
		});
		myinfoPanel.add(disconnectButton);
		
		JButton makeWorldButton = new JButton("Make World");
		makeWorldButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.MAKE_WORLD, null));
		});
		myinfoPanel.add(makeWorldButton);
		
		lobbyInfo = new JPanel();
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
	
	public void connected(JPanel connectionInfo) {
//		this.connectionInfo = connectionInfo;
		topPanel.remove(connectPanel);
//		myinfoPanel.add(connectionInfo);
		topPanel.add(myinfoPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
	}
	
	public void disconnected() {
//		myinfoPanel.remove(connectionInfo);
//		connectionInfo = null;
		topPanel.remove(myinfoPanel);
		topPanel.add(connectPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
		
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			this.remove(gameView);
		}
		gameView = new GameView(instance);
		this.add(gameView, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

}
