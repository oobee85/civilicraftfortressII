package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class CraftingView {
	
	private static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	private static final int BUILDING_ICON_SIZE = 25;

	private JPanel rootPanel;
	private JButton[] craftButtons = new JButton[ItemType.values().length];
	private GameView gameView;
	
	public CraftingView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		setupButtons();
	}
	
	public void setupButtons() {
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (!isCraftableItem(type)) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				int amount = 1;
				if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					amount = 10;
				}
				gameView.getGameInstance().getGUIController().tryToCraftItem(type, amount);
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
			craftButtons[i] = button;
			rootPanel.add(button);
		}
	}
	
	private boolean isCraftableItem(ItemType type) {
		return type.getCost() != null
				&& Game.buildingTypeMap.get(type.getBuilding()) == Game.buildingTypeMap.get("RESEARCH_LAB");
	}
	
	public void updateButtons() {
		boolean hasBlacksmith = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("SMITHY"));
		boolean hasHellForge = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("HELLFORGE"));
		boolean hasResearchLab = gameView.getFaction().hasBuilding(Game.buildingTypeMap.get("RESEARCH_LAB"));
		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			if (!isCraftableItem(type)) {
				continue;
			}
			JButton button = craftButtons[i];
			if(gameView.getFaction().areRequirementsMet(type)) {
				button.setVisible(true);
			}
			else {
				button.setVisible(false);
			}
			if (gameView.getFaction().canAfford(type.getCost()) && hasResearchLab) {
				button.setEnabled(true);
			}
			else {
				button.setEnabled(false);
			}
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
