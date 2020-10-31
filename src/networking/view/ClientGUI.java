package networking.view;

import java.awt.*;
import java.net.*;

import javax.swing.*;

import game.*;
import networking.client.*;
import networking.message.*;
import networking.server.*;
import ui.*;
import ui.Frame;
import utils.*;
import world.*;

public class ClientGUI {
	
	private Client client;
	
	private JPanel mainPanel;
	
	private JPanel topPanel;
	
	private JPanel connectPanel;
	private JPanel myinfoPanel;
//	private JPanel connectionInfo;
	private JPanel lobbyInfo;
	private JTextField nameTextField;
	private Color selectedColor = Server.DEFAULT_PLAYER_INFO.getColor();
	
	private JButton makeWorldButton;
	private JButton startGameButton;

	private GameView gameView;
	private GameViewOverlay gameViewOverlay;
	
	public ClientGUI() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setFocusable(false);
		
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setFocusable(false);
		mainPanel.add(topPanel, BorderLayout.NORTH);
		
		connectPanel = new JPanel();
		connectPanel.setFocusable(false);
		connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
		
		JTextField ipTextField = new JTextField("localhost", 12);
		connectPanel.add(ipTextField);

		KButton startButton = KUIConstants.setupButton("Connect", null, null);
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
		myinfoPanel.setFocusable(false);
		myinfoPanel.setLayout(new BoxLayout(myinfoPanel, BoxLayout.X_AXIS));

		nameTextField = new JTextField(Server.DEFAULT_PLAYER_INFO.getName(), 16);
		myinfoPanel.add(nameTextField);

		KButton colorButton = KUIConstants.setupButton("Pick Color", null, null);
		colorButton.setBorder(BorderFactory.createLineBorder(selectedColor, 10));
		colorButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(mainPanel, "Choose Color", colorButton.getBackground());
			if(newColor != null) {
				selectedColor = newColor;
				colorButton.setBorder(BorderFactory.createLineBorder(selectedColor, 10));
			}
			resetFocus();
		});
		myinfoPanel.add(colorButton);

		KButton updateInfoButton = KUIConstants.setupButton("Update Info", null, null);
		updateInfoButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.INFO, new PlayerInfo(nameTextField.getText(), selectedColor)));
			resetFocus();
		});
		myinfoPanel.add(updateInfoButton);

		KButton disconnectButton = KUIConstants.setupButton("Disconnect", null, null);
		disconnectButton.addActionListener(e -> {
			client.disconnect();
			resetFocus();
		});
		myinfoPanel.add(disconnectButton);

		makeWorldButton = KUIConstants.setupButton("Make World", null, null);
		makeWorldButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.MAKE_WORLD, null));
			resetFocus();
		});
		myinfoPanel.add(makeWorldButton);

		startGameButton = KUIConstants.setupButton("Start Game", null, null);
		startGameButton.addActionListener(e -> {
			client.sendMessage(new ClientMessage(ClientMessageType.START_GAME, null));
			resetFocus();
		});
		myinfoPanel.add(startGameButton);
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
	
	private void resetFocus() {
		if(gameView != null) {
			gameView.requestFocus();
		}
	}
	public void setGameInstance(Game instance, CommandInterface commandInterface) {
		if(gameView != null) {
			mainPanel.remove(gameView);
		}
		gameView = new GameView(instance, commandInterface);
		gameView.requestFocus();
		gameViewOverlay = new GameViewOverlay(instance.getGUIController());
		gameViewOverlay.changeFaction(World.PLAYER_FACTION);
		gameView.setLayout(new BorderLayout());
		gameView.add(gameViewOverlay, BorderLayout.CENTER);
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
	
	public GameViewOverlay getGameViewOverlay() {
		return gameViewOverlay;
	}

}
