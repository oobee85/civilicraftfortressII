package networking.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ui.*;
import utils.*;

public class DebugView {

	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(140, 30);
	public static final Dimension LONG_BUTTON_SIZE = new Dimension(285, 30);
	
	private static final ImageIcon FAST_FORWARD_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/fastforward.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RAIN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/rain.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon ERUPTION_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/erupt.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon CHANGE_FACTION_ICON = Utils.resizeImageIcon(Game.unitTypeMap.get("CYCLOPS").getImageIcon(DEBUG_BUTTON_SIZE.height-5), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_DISABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_disabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon NIGHT_ENABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_enabled.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon METEOR_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/meteor.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon SHADOW_WORD_DEATH = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/soyouhavechosendeath.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private static final ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.png"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	
	
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

		JToggleButton flipTable = KUIConstants.setupToggleButton("Flip Table", null, DEBUG_BUTTON_SIZE);
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});

		JToggleButton spawnUnit = KUIConstants.setupToggleButton("Enable Spawn Unit", null, DEBUG_BUTTON_SIZE);
		spawnUnit.addActionListener(e -> {
			spawnUnit.setText(spawnUnit.isSelected() ? "Disable Spawn Unit" : "Enable Spawn Unit");
			gameView.getGameInstance().spawnUnit(spawnUnit.isSelected());
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

		JButton researchEverything = KUIConstants.setupButton("Research All", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().researchEverything(gameView.getFaction());
				rootPanel.remove(researchEverything);
			}
		});
		JButton shadowWordDeath = KUIConstants.setupButton("Shadow Word: Death", SHADOW_WORD_DEATH, LONG_BUTTON_SIZE);
		shadowWordDeath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameView.getGameInstance().shadowWordDeath(100);
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

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		rootPanel.add(showHeightMap);
		rootPanel.add(flipTable);
		rootPanel.add(spawnUnit);
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
		rootPanel.add(exit);
		rootPanel.add(shadowWordDeath);
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
