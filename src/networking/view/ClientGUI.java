package networking.view;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import networking.client.*;
import networking.message.*;
import networking.server.*;
import ui.*;

public class ClientGUI {
	
	private Client client;
	
	private JPanel mainPanel;
	
	private JPanel topPanel;
	
	private JPanel connectPanel;
	private JPanel myinfoPanel;
//	private JPanel connectionInfo;
	private JPanel lobbyInfo;
	private JTextField nameTextField;
	
	private JButton makeWorldButton;
	private JButton startGameButton;

	private GameView gameView;
	
	public ClientGUI() {
		mainPanel = new JPanel();

		mainPanel.setLayout(new BorderLayout());
		
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		mainPanel.add(topPanel, BorderLayout.NORTH);
		
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
			Color newColor = JColorChooser.showDialog(mainPanel, "Choose Color", colorButton.getBackground());
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
		
		makeWorldButton = new JButton("Make World");
		makeWorldButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.MAKE_WORLD, null));
		});
		myinfoPanel.add(makeWorldButton);

		startGameButton = new JButton("Start Game");
		startGameButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.START_GAME, null));
		});
		myinfoPanel.add(startGameButton);
		startGameButton.setEnabled(false);
		
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
	
	public void worldReceived() {
		startGameButton.setEnabled(true);
	}
	
	public void connected(JPanel connectionInfo) {
//		this.connectionInfo = connectionInfo;
		topPanel.remove(connectPanel);
//		myinfoPanel.add(connectionInfo);
		topPanel.add(myinfoPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		mainPanel.repaint();
	}
	
	public void disconnected() {
//		myinfoPanel.remove(connectionInfo);
//		connectionInfo = null;
		topPanel.remove(myinfoPanel);
		topPanel.add(connectPanel, BorderLayout.NORTH);
		mainPanel.revalidate();
		mainPanel.repaint();
		
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void setGameInstance(Game instance) {
		if(gameView != null) {
			mainPanel.remove(gameView);
		}
		gameView = new GameView(instance);
		mainPanel.add(gameView, BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();
	}
	public void updateTerrainImages() {
		if(gameView != null) {
			gameView.updateTerrainImages();
		}
	}
	
	public void repaint() {
		mainPanel.repaint();
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}

}
