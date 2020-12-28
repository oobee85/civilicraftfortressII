package networking;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import javax.swing.*;

import networking.callbacks.*;
import networking.server.*;
import networking.view.*;

public class Connection {

	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	private ConnectionPanel panel;
	
	private DisconnectCallback disconnectCallback;
	
	private PlayerInfo playerInfo = Server.DEFAULT_PLAYER_INFO;

	private LinkedBlockingQueue<Object> messagesToSend = new LinkedBlockingQueue<Object>();
	private LinkedBlockingQueue<Object> receivedMessages = new LinkedBlockingQueue<Object>();
	public Connection(Socket socket) {
		this.socket = socket;
		try {
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			startInputThread();
			startOutputThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			panel = new ConnectionPanel(InetAddress.getLocalHost().getHostAddress(), socket.getInetAddress().getHostAddress());
			panel.updatePlayerInfo(playerInfo);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void setDisconnectCallback(DisconnectCallback callback) {
		this.disconnectCallback = callback;
	}
	
	public void setPlayerInfo(PlayerInfo info) {
		this.playerInfo = info;
		panel.updatePlayerInfo(info);
	}
	public PlayerInfo getPlayerInfo() {
		return playerInfo;
	}
	
	public void sendMessage(Object message) {
		messagesToSend.add(message);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public Object getMessage() throws InterruptedException {
		return receivedMessages.take();
	}

	private void startInputThread() {
		Thread thread = new Thread(() -> {
			Object message;
			try {
				while((message = input.readObject()) != null) {
					receivedMessages.add(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				if(disconnectCallback != null) {
					disconnectCallback.disconnected();
				}
			}
		});
		thread.start();
	}
	private void startOutputThread() {
		Thread thread = new Thread(() -> {
			Object message;
			try {
				while(true) {
					message = messagesToSend.take();
					output.writeObject(message);
					// reset stream to allow sending same object with updated values
					output.reset();
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(disconnectCallback != null) {
					disconnectCallback.disconnected();
				}
			}
		});
		thread.start();
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	@Override
	public String toString() {
		return socket.getInetAddress().getHostAddress().toString();
	}
}
