package networking.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.PlantType;

public class SpawnUnitsView {
	
	public static final Dimension SPAWN_BUTTON_SIZE = new Dimension(30, 30);

	private JPanel rootPanel;
	private GameView gameView;
	
	public SpawnUnitsView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		
		setupButtons();
	}
	
	private void setupButtons() {
		for (int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gameView.setThingToSpawn(type);
			});
			button.addRightClickActionListener(e -> {
				gameView.getGameInstance().getGUIController().switchInfoPanel(new UnitTypeInfoPanel(type, gameView.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					gameView.getGameInstance().getGUIController().pushInfoPanel(new UnitTypeInfoPanel(type, gameView.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					gameView.getGameInstance().getGUIController().popInfoPanel();
				}
			});
			rootPanel.add(button);
		}
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gameView.setThingToSpawn(type);
			});
			button.addRightClickActionListener(e -> {
				gameView.getGameInstance().getGUIController().switchInfoPanel(new BuildingTypeInfoPanel(type, gameView.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					gameView.getGameInstance().getGUIController().pushInfoPanel(new BuildingTypeInfoPanel(type, gameView.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					gameView.getGameInstance().getGUIController().popInfoPanel();
				}
			});
//			buildingButtons[i] = button;
			rootPanel.add(button);
		}
		for (int i = 0; i < Game.plantTypeList.size(); i++) {
			PlantType type = Game.plantTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gameView.setThingToSpawn(type);
			});
			button.addRightClickActionListener(e -> {
				gameView.getGameInstance().getGUIController().switchInfoPanel(new PlantTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					gameView.getGameInstance().getGUIController().pushInfoPanel(new PlantTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					gameView.getGameInstance().getGUIController().popInfoPanel();
				}
			});
			rootPanel.add(button);
		}
		JToggleButton toggle = KUIConstants.setupToggleButton("Non-playerControlled", null, null);
		toggle.addActionListener(e -> {
			toggle.setText(!toggle.isSelected() ? "Non-playerControlled" : "playerControlled");
			gameView.setSummonPlayerControlled(!toggle.isSelected());
		});
		rootPanel.add(toggle);
	}

	public JPanel getRootPanel() {
		return rootPanel;
	}
}
