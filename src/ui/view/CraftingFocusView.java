package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class CraftingFocusView {
	
	private static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	private static final int BUILDING_ICON_SIZE = 25;

	private JPanel rootPanel;
	private JButton[] craftFocusButtons = new JButton[ItemType.values().length];
	private GameView gameView;
	
	public CraftingFocusView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		setupButtons();
	}
	
	public void setupButtons() {
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (!isCraftingFocusItem(type)) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				gameView.getGameInstance().getGUIController().toggleCraftItemFocus(type);
//				button.setBackground(Color.GRAY);
			});
			button.addRightClickActionListener(e -> {
				gameView.getGameInstance().getGUIController().switchInfoPanel(new ItemTypeInfoPanel(type, gameView.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					gameView.getGameInstance().getGUIController().pushInfoPanel(new ItemTypeInfoPanel(type, gameView.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					gameView.getGameInstance().getGUIController().popInfoPanel();
				}
			});
			craftFocusButtons[i] = button;
			rootPanel.add(button);
		}
	}
	
	// different function from craftingView
	private boolean isCraftingFocusItem(ItemType type) {
		if(type.getCost() == null) {
			return false;
		}
		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("SAWMILL")) {
			return true;
		}
		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("QUARRY")) {
			return true;
		}
		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("SMITHY")) {
			return true;
		}
		return false;
	}
	
	public void updateButtons() {
//		boolean hasSawmill = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("SAWMILL"));
//		boolean hasQuarry = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("QUARRY"));
//		boolean hasSmithy = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("SMITHY"));
		
		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			JButton button = craftFocusButtons[i];
			if(button != null) {
				button.setEnabled(isCraftingFocusItem(type));
			}
			
			
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
