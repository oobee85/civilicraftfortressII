package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import networking.client.ClientGUI;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.PlantType;

public class SpawnUnitsView {
	
	public static final Dimension SPAWN_BUTTON_SIZE = new Dimension(30, 30);

	private ScrollingPanel scrollingPanel;
	private GameView gameView;
	
	public SpawnUnitsView(GameView gameView) {
		this.gameView = gameView;
		scrollingPanel = new ScrollingPanel(new Dimension((int) (ClientGUI.GUIWIDTH - SPAWN_BUTTON_SIZE.getWidth()), 400));
		scrollingPanel.setFocusable(false);
		
		setupButtons();
	}
	
	private void setupButtons() {
		for (int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
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
			scrollingPanel.add(button);
		}
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
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
			scrollingPanel.add(button);
		}
		for (int i = 0; i < Game.plantTypeList.size(); i++) {
			PlantType type = Game.plantTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
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
			scrollingPanel.add(button);
		}
		JToggleButton toggle = KUIConstants.setupToggleButton("Non-playerControlled", null, null);
		toggle.addActionListener(e -> {
			toggle.setText(!toggle.isSelected() ? "Non-playerControlled" : "playerControlled");
			gameView.setSummonPlayerControlled(!toggle.isSelected());
		});
		scrollingPanel.add(toggle);
		JToggleButton spawnWeather = KUIConstants.setupToggleButton("Non-spawnWeather", null, null);
		spawnWeather.addActionListener(e -> {
			spawnWeather.setText(!spawnWeather.isSelected() ? "Non-spawnWeather" : "spawnWeather");
			gameView.setSummonPlayerControlled(!spawnWeather.isSelected());
		});
		scrollingPanel.add(spawnWeather);
	}

	public JPanel getRootPanel() {
		return scrollingPanel.getRootPanel();
	}
}
