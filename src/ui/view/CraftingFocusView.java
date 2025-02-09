package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import networking.client.ClientGUI;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class CraftingFocusView {
	
	private static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	private static final int BUILDING_ICON_SIZE = 25;

	private JPanel rootPanel;
	private JToggleButton[] craftFocusButtons = new JToggleButton[ItemType.values().length];
	private GameView gameView;
	
	public CraftingFocusView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, 500));
		rootPanel.setFocusable(false);
		rootPanel.setBackground(Color.magenta);
		setupButtons();
	}
	
	public void setupButtons() {
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (!isCraftingFocusItem(type)) {
				continue;
			}
			JToggleButton button = KUIConstants.setupToggleButton(type.toString(),
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			
			button.setSelected(false);
			button.setEnabled(false);
			button.addActionListener(e -> {

//				if(button.isSelected() == true) {
//					button.setEnabled(false);
//				}else {
//					button.setEnabled(true);
//				}
				
				gameView.getGameInstance().getGUIController().toggleCraftItemFocus(type);
//			button.addActionListener(e -> {
//				gameView.getGameInstance().getGUIController().toggleCraftItemFocus(type);
				
				
				if(button.isEnabled() == true) {
					button.setEnabled(false);
				}else {
					button.setEnabled(true);
				}
//				button.setEnabled(!button.getEnabled());
			});
//			button.addRightClickActionListener(e -> {
//				gameView.getGameInstance().getGUIController().switchInfoPanel(new ItemTypeInfoPanel(type, gameView.getFaction()));
//			});
//			button.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseEntered(MouseEvent e) {
//					gameView.getGameInstance().getGUIController().pushInfoPanel(new ItemTypeInfoPanel(type, gameView.getFaction()));
//				}
//				@Override
//				public void mouseExited(MouseEvent e) {
//					gameView.getGameInstance().getGUIController().popInfoPanel();
//				}
//			});
			craftFocusButtons[i] = button;
			rootPanel.add(button);
		}
	}
	
	// different function from craftingView
	private boolean isCraftingFocusItem(ItemType type) {
		if(type.getCost() == null) {
			return false;
		}
		if(Game.buildingTypeMap.get(type.getBuilding()).isCrafting()) {
			return true;
		}
//		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("SAWMILL")) {
//			return true;
//		}
//		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("QUARRY")) {
//			return true;
//		}
//		if(Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("SMITHY")) {
//			return true;
//		}
		return false;
	}
	
	public void updateButtons() {
		int numVisible = 0;
		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			JToggleButton button = craftFocusButtons[i];
			if(button != null) {
				if(isCraftingFocusItem(type) == true) {
					button.setVisible(true);
					numVisible++;
					button.setEnabled(true);
				}else {
					button.setVisible(false);
					button.setEnabled(false);
				}
//				button.setText(button.isVisible() ? "Enable buildings to craft this item" : "");
//				toggleNight.setIcon(button.isVisible() ? NIGHT_DISABLED_ICON : NIGHT_ENABLED_ICON);
			}
			
			
		}
		int numrows = (numVisible+1) / 2;
		int defaultFlowLayoutOffset = 5;
		int rowheight = BUILDING_BUTTON_SIZE.height + defaultFlowLayoutOffset;
		rootPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, rowheight * numrows + defaultFlowLayoutOffset));
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
