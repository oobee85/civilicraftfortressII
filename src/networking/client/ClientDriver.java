package networking.client;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

import game.*;
import utils.*;

public class ClientDriver implements WindowListener {

	private JFrame frame;
	private Client client = new Client();
	private ClientGUI clientGUI = new ClientGUI();
	
	public ClientDriver() {
		frame = new JFrame("Settlers of Might and Magic");
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		height = Math.min(height, 1080);
		height = height * 8 / 9;
		int width = height + ClientGUI.GUIWIDTH;
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(this);
		frame.setIconImage(Utils.loadImageIcon("Images/logo.png").getImage());
		frame.add(clientGUI.getMainPanel());
		
		clientGUI.setClient(client);
		client.setGUI(clientGUI);
	}
	
	public void start() {
		frame.setVisible(true);
	}
	
	static {
		
		Settings.fromFile();
		Loader.loadSounds();
		Loader.loadResearchType(Game.researchTypeMap, Game.researchTypeList);
		Loader.loadUnitType(Game.unitTypeMap, Game.unitTypeList);
		Loader.loadBuildingType(Game.buildingTypeMap, Game.buildingTypeList);
		Loader.loadPlantType(Game.plantTypeMap, Game.plantTypeList);
		Loader.doMappings();
		Loader.loadBuildOrders();
	}
	
	public static void main(String[] args) {
		Settings.fromCmdArgs(args);
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException ex) {
			}
		});
		ClientDriver driver = new ClientDriver();
		driver.start();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Settings.toFile();
	}

	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
}
