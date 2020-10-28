package networking.client;

import javax.swing.*;

import networking.view.*;

public class ClientDriver {

	private JFrame frame;
	private Client client = new Client();
	private ClientGUI clientGUI = new ClientGUI();
	
	public ClientDriver() {
		frame = new JFrame("Grid Client");
		frame.setSize(700, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(clientGUI.getMainPanel());
		
		clientGUI.setClient(client);
		client.setGUI(clientGUI);
	}
	
	public void start() {
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ClientDriver driver = new ClientDriver();
		driver.start();
	}

}
