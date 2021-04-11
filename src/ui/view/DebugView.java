package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import ui.*;
import utils.*;

public class DebugView {

	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(140, 30);
	public static final Dimension LONG_BUTTON_SIZE = new Dimension(285, 30);
	
	private static final ImageIcon FAST_FORWARD_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/fastforward.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RAIN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/rain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon ERUPTION_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/erupt.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon CHANGE_FACTION_ICON = Utils.resizeImageIcon(Game.unitTypeMap.get("CYCLOPS").getImageIcon(DEBUG_BUTTON_SIZE.height-5), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_DISABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/night_disabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_ENABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/night_enabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon METEOR_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/meteor.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SHADOW_WORD_DEATH = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/soyouhavechosendeath.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SHADOW_WORD_PAIN = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/shadowwordpain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/tech.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	
	
	private JPanel rootPanel;
	private GameView gameView;
	
	public DebugView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		
		setupButtons();
	}
	
	private void setupButtons() {

		JToggleButton showHeightMap = KUIConstants.setupToggleButton("Show Height Map", null, DEBUG_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameView.setShowHeightMap(showHeightMap.isSelected());
		});
		JToggleButton showPressureMap = KUIConstants.setupToggleButton("Show Pressure Map", null, DEBUG_BUTTON_SIZE);
		showPressureMap.addActionListener(e -> {
			showPressureMap.setText(showPressureMap.isSelected() ? "Hide Pressure Map" : "Show Pressure Map");
			gameView.setShowPressureMap(showPressureMap.isSelected());
		});
		JToggleButton showTemperatureMap = KUIConstants.setupToggleButton("Show Temperature Map", null, DEBUG_BUTTON_SIZE);
		showTemperatureMap.addActionListener(e -> {
			showTemperatureMap.setText(showTemperatureMap.isSelected() ? "Hide Temperature Map" : "Show Temperature Map");
			gameView.setShowTemperatureMap(showTemperatureMap.isSelected());
		});
		
		JToggleButton showMassMap = KUIConstants.setupToggleButton("Show Mass Map", null, DEBUG_BUTTON_SIZE);
		showMassMap.addActionListener(e -> {
			showMassMap.setText(showMassMap.isSelected() ? "Hide Mass Map" : "Show Mass Map");
			gameView.setShowMassMap(showMassMap.isSelected());
		});

		JToggleButton flipTable = KUIConstants.setupToggleButton("Flip Table", null, DEBUG_BUTTON_SIZE);
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});

		JButton eruptVolcano = KUIConstants.setupButton("Erupt Volcano", ERUPTION_ICON, DEBUG_BUTTON_SIZE);
		eruptVolcano.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().eruptVolcano();
			}
		});
		
		JButton addResources = KUIConstants.setupButton("Give Resources", null, DEBUG_BUTTON_SIZE);
		addResources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().addResources(gameView.getFaction());
			}
		});

		JButton makeItRain = KUIConstants.setupButton("Rain", RAIN_ICON, DEBUG_BUTTON_SIZE);
		makeItRain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().world.rain();
				gameView.getGameInstance().world.grow();
			
			}
		});

		JButton makeItDry = KUIConstants.setupButton("Drought", null, DEBUG_BUTTON_SIZE);
		makeItDry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().world.drought();
			}
		});

		JToggleButton fastForward = KUIConstants.setupToggleButton("Fast Forward", FAST_FORWARD_ICON, DEBUG_BUTTON_SIZE);
		fastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().getGUIController().setFastForwarding(fastForward.isSelected());
				fastForward.setText(fastForward.isSelected() ? "Stop Fast Forward" : "Fast Forward");
			}
		});
		JButton raiseHeight = KUIConstants.setupButton("SpawnWeather", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		raiseHeight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.setWeather(true);
			}
		});
		JButton setTerritoryButton = KUIConstants.setupButton("Set Territory", null, DEBUG_BUTTON_SIZE);
		setTerritoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.setSetTerritory(true);
			}
		});

		JButton researchEverything = KUIConstants.setupButton("Research All", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().researchEverything(gameView.getFaction());
			}
		});
		JButton shadowWordDeath = KUIConstants.setupButton("Shadow Word: Death", SHADOW_WORD_DEATH, DEBUG_BUTTON_SIZE);
		shadowWordDeath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().shadowWordDeath(100);
			}
		});
		JButton shadowWordPain = KUIConstants.setupButton("Shadow Word: Pain", SHADOW_WORD_PAIN, DEBUG_BUTTON_SIZE);
		shadowWordPain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().shadowWordPain(1);
			}
		});

		JButton meteor = KUIConstants.setupButton("Meteor", METEOR_ICON, DEBUG_BUTTON_SIZE);
		meteor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().meteorStrike();
			}
		});
		JButton unitEvents = KUIConstants.setupButton("Unit Events", null, DEBUG_BUTTON_SIZE);
		unitEvents.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().shadowWordDeath(1);
			}
		});
		JButton setPlayerFaction = KUIConstants.setupButton("Change Faction", CHANGE_FACTION_ICON, DEBUG_BUTTON_SIZE);
		setPlayerFaction.addActionListener(e -> {
			int choice = JOptionPane.showOptionDialog(null, "Choose faction", "Choose faction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, gameView.getGameInstance().world.getFactions().toArray(), gameView.getGameInstance().world.getFactions().get(0));
			if(choice >= 0 && choice < gameView.getGameInstance().world.getFactions().size()) {
				if(gameView.getFaction() != gameView.getGameInstance().world.getFactions().get(choice)) {
					gameView.deselectEverything();
					gameView.setFaction(gameView.getGameInstance().world.getFactions().get(choice));
					gameView.getGameInstance().getGUIController().changedFaction(gameView.getGameInstance().world.getFactions().get(choice));
				}
			}
		});

		JToggleButton debug = KUIConstants.setupToggleButton(gameView.getDrawDebugStrings() ? "Leave Matrix" : "Matrix", null, DEBUG_BUTTON_SIZE);
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.setDrawDebugStrings(debug.isSelected());
				debug.setText(gameView.getDrawDebugStrings() ? "Leave Matrix" : "Matrix");
			}
		});
		JToggleButton toggleNight = KUIConstants.setupToggleButton(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled", NIGHT_ENABLED_ICON,
				DEBUG_BUTTON_SIZE);
		toggleNight.setSelected(true);
		toggleNight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Game.DISABLE_NIGHT = !toggleNight.isSelected();
				toggleNight.setText(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled");
				toggleNight.setIcon(Game.DISABLE_NIGHT ? NIGHT_DISABLED_ICON : NIGHT_ENABLED_ICON);
			}
		});

		JToggleButton toggleGL = KUIConstants.setupToggleButton(gameView.is3d() ? "3D" : "2D", null, DEBUG_BUTTON_SIZE);
		toggleGL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameView.switch3d(!gameView.is3d());
				toggleGL.setText(gameView.is3d() ? "3D" : "2D");
			}
		});

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		JButton save = KUIConstants.setupButton("Save", null, DEBUG_BUTTON_SIZE);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameView.getGameInstance().saveToFile();
			}
		});

		rootPanel.add(showHeightMap);
		rootPanel.add(showPressureMap);
		rootPanel.add(showTemperatureMap);
		rootPanel.add(showMassMap);
		rootPanel.add(flipTable);
		rootPanel.add(makeItRain);
		rootPanel.add(makeItDry);
		rootPanel.add(fastForward);
		rootPanel.add(eruptVolcano);
		rootPanel.add(meteor);
		rootPanel.add(unitEvents);
		rootPanel.add(debug);
		rootPanel.add(toggleNight);
		rootPanel.add(addResources);
		rootPanel.add(setPlayerFaction);
		rootPanel.add(researchEverything);
		rootPanel.add(raiseHeight);
		rootPanel.add(setTerritoryButton);
		rootPanel.add(save);
		rootPanel.add(exit);
		rootPanel.add(toggleGL);
		rootPanel.add(shadowWordDeath);
		rootPanel.add(shadowWordPain);
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
