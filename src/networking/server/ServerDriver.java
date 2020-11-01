package networking.server;


import java.awt.*;

import javax.swing.*;

import networking.view.*;
import ui.*;
import utils.*;

public class ServerDriver {
	JFrame frame;
	ServerGUI serverGUI = new ServerGUI();
	Server server = new Server();
	public ServerDriver() {
		frame = new JFrame("civilicraftfortressIII Server");
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(serverGUI);
		
//		serverGUI.setServer(server);
		server.setGUI(serverGUI);
	}
	
	public void start() {
		frame.setVisible(true);
		server.startAcceptingConnections();
	}

	static {
		Loader.loadResearchType(Game.researchTypeMap, Game.researchTypeList);
		Loader.loadUnitType(Game.unitTypeMap, Game.unitTypeList);
		Loader.loadBuildingType(Game.buildingTypeMap, Game.buildingTypeList);
		Loader.doMakingUnitMappings();
	}

	public static void main(String[] args) {
//		EventQueue.invokeLater(() -> {
//			try {
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
//					| UnsupportedLookAndFeelException ex) {
//			}
//		});
		ThingMapper.ACTIVE = true;
		ServerDriver driver = new ServerDriver();
		driver.start();
	}

}
