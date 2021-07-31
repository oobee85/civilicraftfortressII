package networking.client;

import java.awt.*;

import javax.swing.*;

import game.*;
import utils.*;

public class ClientDriver {

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
		frame.setIconImage(Utils.loadImageIcon("Images/logo.png").getImage());
		frame.add(clientGUI.getMainPanel());
		
		clientGUI.setClient(client);
		client.setGUI(clientGUI);
	}
	
	public void start() {
		frame.setVisible(true);
	}
	
	static {
		Loader.loadResearchType(Game.researchTypeMap, Game.researchTypeList);
		Loader.loadUnitType(Game.unitTypeMap, Game.unitTypeList);
		Loader.loadBuildingType(Game.buildingTypeMap, Game.buildingTypeList);
		Loader.loadPlantType(Game.plantTypeMap, Game.plantTypeList);
		Loader.doMappings();
	}
	
	public static void main(String[] args) {
		for(int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
		if(args.length >= 1) {
			if(args[0].toLowerCase().equals("debug")) {
				Game.DEBUG = true;
			}
		}
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

}
