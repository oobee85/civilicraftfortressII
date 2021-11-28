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

  private JFrame settingsWindow;
	private ScrollingPanel scrollingPanel;
	private GameView gameView;
	
	public DebugView(GameView gameView) {
		this.gameView = gameView;
		
		scrollingPanel = new ScrollingPanel(new Dimension(ClientGUI.GUIWIDTH, 700));
		setupButtons();
	}
	
	private void setupButtons() {
		
		ButtonGroup group = new ButtonGroup();
		LinkedList<JRadioButton> mapModeButtons = new LinkedList<>();
		for(MapMode mode : MapMode.values()) {
			if(mode == MapMode.MINIMAP) {
				continue;
			}
			JRadioButton button = KUIConstants.setupRadioButton(Utils.getNiceName(mode.toString()), null, DEBUG_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameView.setMapMode(mode);
			});
			group.add(button);
			mapModeButtons.add(button);
		}
		mapModeButtons.getFirst().setSelected(true);

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
		JButton raiseHeight = KUIConstants.setupButton("SpawnWeather", RAIN_ICON, DEBUG_BUTTON_SIZE);
		raiseHeight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.setWeather(true);
			}
		});
		JButton increasePressure = KUIConstants.setupButton("pressure", RAIN_ICON, DEBUG_BUTTON_SIZE);
		increasePressure.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.increasePressure(true);
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

		JButton enableFirstPerson = KUIConstants.setupButton("FP Mode", null, DEBUG_BUTTON_SIZE);
		enableFirstPerson.addActionListener(e -> {
			gameView.switchFirstPerson(true);
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
		
		JButton reseedButton = KUIConstants.setupButton("Reseed", null, DEBUG_BUTTON_SIZE);
		reseedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
			}
		});

    JButton settingsButton = KUIConstants.setupButton("Settings", null, DEBUG_BUTTON_SIZE);
    settingsButton.addActionListener(e -> {
      if(settingsWindow != null) {    
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
        settingsWindow.setSize(500, 500);
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
        SettingsMenu settingsMenu = new SettingsMenu(
            ee -> {
              settingsWindow.dispose();
              settingsWindow = null;
            });
        settingsMenu.addControlFor(Settings.class);
        settingsWindow.add(settingsMenu.getContentPanel());
        settingsWindow.setVisible(true);
      }
    });

		for(JRadioButton button : mapModeButtons) {
			scrollingPanel.add(button);
		}
		scrollingPanel.add(flipTable);
		scrollingPanel.add(makeItRain);
		scrollingPanel.add(makeItDry);
		scrollingPanel.add(fastForward);
		scrollingPanel.add(eruptVolcano);
		scrollingPanel.add(meteor);
		scrollingPanel.add(unitEvents);
		scrollingPanel.add(debug);
		scrollingPanel.add(toggleNight);
		scrollingPanel.add(addResources);
		scrollingPanel.add(setPlayerFaction);
		scrollingPanel.add(researchEverything);
		scrollingPanel.add(raiseHeight);
		scrollingPanel.add(increasePressure);
		scrollingPanel.add(setTerritoryButton);
//		scrollingPanel.add(save); // doesnt currently work
		scrollingPanel.add(exit);
		scrollingPanel.add(toggleGL);
		scrollingPanel.add(enableFirstPerson);
		scrollingPanel.add(shadowWordDeath);
		scrollingPanel.add(shadowWordPain);
		scrollingPanel.add(reseedButton);
    scrollingPanel.add(settingsButton);
	}
	
	public JPanel getRootPanel() {
		return scrollingPanel.getRootPanel();
	}
}
