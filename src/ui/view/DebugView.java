package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import networking.client.*;
import ui.*;
import utils.*;
import world.*;

public class DebugView {

	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(140, 30);
	public static final Dimension LONG_BUTTON_SIZE = new Dimension(285, 30);
	
	private static final ImageIcon PAUSE_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/pause.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon FAST_FORWARD_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/fastforward.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RAIN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/rain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon ERUPTION_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/erupt.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon CHANGE_FACTION_ICON = Utils.resizeImageIcon(Game.unitTypeMap.get("CYCLOPS").getMipMap().getImageIcon(DEBUG_BUTTON_SIZE.height-5), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_DISABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/night_disabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_ENABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/night_enabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon METEOR_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/meteor.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SHADOW_WORD_DEATH = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/soyouhavechosendeath.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SHADOW_WORD_PAIN = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/shadowwordpain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/tech.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SETTINGS_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/settings.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);

	private JFrame settingsWindow;
	private JFrame terrainGenWindow;
	private ScrollingPanel scrollingPanel;
	private GameView gameView;
	
	public DebugView(GameView gameView) {
		this.gameView = gameView;
		
		scrollingPanel = new ScrollingPanel(new Dimension(ClientGUI.GUIWIDTH, 700));
		setupButtons();
	}
	
	private void setupButtons() {
		
		JComboBox<MapMode> mapModeComboBox = new JComboBox<MapMode>();
		KUIConstants.setupComboBox(mapModeComboBox, DEBUG_BUTTON_SIZE);
		for(MapMode mode : MapMode.values()) {
			if(mode == MapMode.MINIMAP) {
				continue;
			}
			mapModeComboBox.addItem(mode);
		}
		
		mapModeComboBox.addActionListener(e -> {
			gameView.setMapMode((MapMode)mapModeComboBox.getSelectedItem());
		});

		JToggleButton flipTable = KUIConstants.setupToggleButton("Flip Table", null, DEBUG_BUTTON_SIZE);
		flipTable.addActionListener(e -> {
			gameView.getGameInstance().flipTable();
			flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
		});

		JButton eruptVolcano = KUIConstants.setupButton("Erupt Volcano", ERUPTION_ICON, DEBUG_BUTTON_SIZE);
		eruptVolcano.addActionListener(e -> {
			gameView.getGameInstance().eruptVolcano();
		});
		
		JButton addResources = KUIConstants.setupButton("Give Resources", null, DEBUG_BUTTON_SIZE);
		addResources.addActionListener(e -> {
			gameView.getGameInstance().addResources(gameView.getFaction());
		});

		JButton makeItRain = KUIConstants.setupButton("Rain", RAIN_ICON, DEBUG_BUTTON_SIZE);
		makeItRain.addActionListener(e -> {
			gameView.getGameInstance().world.rain();
			gameView.getGameInstance().world.grow();
		});
		

		JButton makeItDry = KUIConstants.setupButton("Drought", null, DEBUG_BUTTON_SIZE);
		makeItDry.addActionListener(e -> {
			gameView.getGameInstance().world.drought();
		});

		JToggleButton fastForward = KUIConstants.setupToggleButton("Fast Forward", FAST_FORWARD_ICON, DEBUG_BUTTON_SIZE);
		fastForward.addActionListener(e -> {
			gameView.getGameInstance().getGUIController().setFastForwarding(fastForward.isSelected());
			fastForward.setText(fastForward.isSelected() ? "Stop Fast Forward" : "Fast Forward");
		});
		JToggleButton pauseGame = KUIConstants.setupToggleButton("Pause Game", PAUSE_ICON, DEBUG_BUTTON_SIZE);
		pauseGame.addActionListener(e -> {
			gameView.getGameInstance().getGUIController().setPauseGame(pauseGame.isSelected());
			pauseGame.setText(pauseGame.isSelected() ? "Play Game" : "Pause Game");
		});
		JButton raiseHeight = KUIConstants.setupButton("SpawnWeather", RAIN_ICON, DEBUG_BUTTON_SIZE);
		raiseHeight.addActionListener(e -> {
			gameView.setWeather(true);
		});
		JButton increasePressure = KUIConstants.setupButton("pressure", RAIN_ICON, DEBUG_BUTTON_SIZE);
		increasePressure.addActionListener(e -> {
			gameView.increasePressure(true);
		});
		JButton setTerritoryButton = KUIConstants.setupButton("Set Territory", null, DEBUG_BUTTON_SIZE);
		setTerritoryButton.addActionListener(e -> {
			gameView.setSetTerritory(true);
		});

		JButton researchEverything = KUIConstants.setupButton("Research All", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(e -> {
			gameView.getGameInstance().researchEverything(gameView.getFaction());
		});
		JButton shadowWordDeath = KUIConstants.setupButton("Shadow Word: Death", SHADOW_WORD_DEATH, DEBUG_BUTTON_SIZE);
		shadowWordDeath.addActionListener(e -> {
			gameView.getGameInstance().shadowWordDeath(100);
		});
		JButton shadowWordPain = KUIConstants.setupButton("Shadow Word: Pain", SHADOW_WORD_PAIN, DEBUG_BUTTON_SIZE);
		shadowWordPain.addActionListener(e -> {
			gameView.getGameInstance().shadowWordPain(1);
		});

		JButton meteor = KUIConstants.setupButton("Meteor", METEOR_ICON, DEBUG_BUTTON_SIZE);
		meteor.addActionListener(e -> {
			gameView.getGameInstance().meteorStrike();
		});
		JButton unitEvents = KUIConstants.setupButton("Unit Events", null, DEBUG_BUTTON_SIZE);
		unitEvents.addActionListener(e -> {
			gameView.getGameInstance().shadowWordDeath(1);
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
		debug.addActionListener(e -> {
			gameView.setDrawDebugStrings(debug.isSelected());
			debug.setText(gameView.getDrawDebugStrings() ? "Leave Matrix" : "Matrix");
		});
		JToggleButton toggleNight = KUIConstants.setupToggleButton(
				Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled", 
				NIGHT_ENABLED_ICON,
				DEBUG_BUTTON_SIZE);
		toggleNight.setSelected(true);
		toggleNight.addActionListener(e -> {
			Game.DISABLE_NIGHT = !toggleNight.isSelected();
			toggleNight.setText(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled");
			toggleNight.setIcon(Game.DISABLE_NIGHT ? NIGHT_DISABLED_ICON : NIGHT_ENABLED_ICON);
		});

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
		exit.addActionListener(e -> {
			System.exit(0);
		});
		JButton save = KUIConstants.setupButton("Save", null, DEBUG_BUTTON_SIZE);
		save.addActionListener(e -> {
			gameView.getGameInstance().saveToFile();
		});
		save.setEnabled(false);
		
		JButton reseedButton = KUIConstants.setupButton("Reseed", null, DEBUG_BUTTON_SIZE);
		reseedButton.addActionListener(e -> {
			try {
				String newSeed = JOptionPane.showInputDialog("Input seed");
				if(newSeed == null) {
					return;
				}
				long seed = Long.parseLong(newSeed);
				gameView.getGameInstance().world.reseedTerrain(seed);
			}
			catch(NumberFormatException ex) {
				
			}
		});

		JButton settingsButton = KUIConstants.setupButton("Settings", SETTINGS_ICON, DEBUG_BUTTON_SIZE);
		settingsButton.addActionListener(e -> setupSettingsMenu() );

		JButton terrainGenViewButton = KUIConstants.setupButton("TerrainGen", null, DEBUG_BUTTON_SIZE);
		terrainGenViewButton.addActionListener(e -> setupTerrainGenView() );

		scrollingPanel.add(exit);
		scrollingPanel.add(settingsButton);
		scrollingPanel.add(fastForward);
		scrollingPanel.add(pauseGame);
		scrollingPanel.add(setPlayerFaction);
		scrollingPanel.add(debug);
		scrollingPanel.add(mapModeComboBox);
		scrollingPanel.add(toggleNight);
		scrollingPanel.add(addResources);
		scrollingPanel.add(researchEverything);
		scrollingPanel.add(flipTable);
		scrollingPanel.add(makeItRain);
		scrollingPanel.add(makeItDry);
		scrollingPanel.add(eruptVolcano);
		scrollingPanel.add(meteor);
		scrollingPanel.add(unitEvents);
		scrollingPanel.add(raiseHeight);
		scrollingPanel.add(increasePressure);
		scrollingPanel.add(setTerritoryButton);
		scrollingPanel.add(save); // doesnt currently work
		scrollingPanel.add(shadowWordDeath);
		scrollingPanel.add(shadowWordPain);
		scrollingPanel.add(reseedButton);
		scrollingPanel.add(terrainGenViewButton);
	}
	
	private void setupTerrainGenView() {
		if (terrainGenWindow != null) {
			int sta = terrainGenWindow.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;
			terrainGenWindow.setExtendedState(sta);
			terrainGenWindow.setAlwaysOnTop(true);
			terrainGenWindow.toFront();
			terrainGenWindow.requestFocus();
			terrainGenWindow.setAlwaysOnTop(false);
		}
		else {
			terrainGenWindow = new JFrame("TerrainGenView");
			terrainGenWindow.setLocationRelativeTo(null);
			terrainGenWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			terrainGenWindow.setSize(500, 500);
			terrainGenWindow.addWindowListener(new WindowListener() {
				@Override
				public void windowClosing(WindowEvent e) {
					terrainGenWindow = null;
				}
				@Override public void windowOpened(WindowEvent e) { }
				@Override public void windowIconified(WindowEvent e) { }
				@Override public void windowDeiconified(WindowEvent e) { }
				@Override public void windowDeactivated(WindowEvent e) { }
				@Override public void windowClosed(WindowEvent e) { } 
				@Override public void windowActivated(WindowEvent e) { }
			});
			TerrainGenView terrainGenView = new TerrainGenView(ee -> {
				terrainGenWindow.dispose();
				terrainGenWindow = null;
			});
			terrainGenWindow.add(terrainGenView.getContentPanel());
			terrainGenWindow.setVisible(true);
		}
	}
	
	private void setupSettingsMenu() {
		if (settingsWindow != null) {
			int sta = settingsWindow.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;
			settingsWindow.setExtendedState(sta);
			settingsWindow.setAlwaysOnTop(true);
			settingsWindow.toFront();
			settingsWindow.requestFocus();
			settingsWindow.setAlwaysOnTop(false);
		} 
		else {
			settingsWindow = new JFrame("Settings");
			settingsWindow.setLocationRelativeTo(null);
			settingsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			Dimension size = new Dimension(500, 500);
			settingsWindow.setSize(size);
			settingsWindow.addWindowListener(new WindowListener() {
				@Override
				public void windowClosing(WindowEvent e) {
					settingsWindow = null;
				}
				@Override public void windowOpened(WindowEvent e) { }
				@Override public void windowIconified(WindowEvent e) { }
				@Override public void windowDeiconified(WindowEvent e) { }
				@Override public void windowDeactivated(WindowEvent e) { }
				@Override public void windowClosed(WindowEvent e) { } 
				@Override public void windowActivated(WindowEvent e) { }
			});
			SettingsMenu settingsMenu = new SettingsMenu(ee -> {
				settingsWindow.dispose();
				settingsWindow = null;
			}, size);
			settingsMenu.addControlFor(Settings.class);
			settingsWindow.add(settingsMenu.getContentPanel());
			settingsWindow.setVisible(true);
		}
	}
	
	public JPanel getRootPanel() {
		return scrollingPanel.getRootPanel();
	}
}
