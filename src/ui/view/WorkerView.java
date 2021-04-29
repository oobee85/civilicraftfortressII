package ui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import game.*;
import networking.client.*;
import ui.*;
import ui.infopanels.*;
import utils.*;

public class WorkerView {
	private static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	private static final int BUILDING_ICON_SIZE = 25;
	
	private static final ImageIcon COLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/collapsed.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE);
	private static final ImageIcon UNCOLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/uncollapsed.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE);
	
	private ScrollingPanel scrollingPanel;

	private JButton[] buildingButtons;
	private GameView gameView;
	
	public WorkerView(GameView gameView) {
		this.gameView = gameView;
		scrollingPanel = new ScrollingPanel(new Dimension(ClientGUI.GUIWIDTH, BUILDING_BUTTON_SIZE.height * (Game.buildingTypeList.size()/2 + 4)));
		scrollingPanel.setLayout(new GridBagLayout());
		setupButtons();
	}
	
	private void setupButtons() {
		buildingButtons = new JButton[Game.buildingTypeList.size()];
		
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.addActionListener(e -> {
				if(button.getEnabled()) {
					gameView.setBuildingToPlan(type);
				}
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
			buildingButtons[i] = button;
		}
		
		KToggleButton toggleButton = KUIConstants.setupToggleButton("Walls/Gates", UNCOLLAPSED_ICON, new Dimension(BUILDING_BUTTON_SIZE.width*2, BUILDING_BUTTON_SIZE.height));
		toggleButton.setSelected(true);
		toggleButton.addActionListener(e -> {
			for (int i = 0; i < Game.buildingTypeList.size(); i++) {
				if(Game.buildingTypeList.get(i).blocksMovement()) {
					buildingButtons[i].setVisible(toggleButton.isSelected());
				}
			}
			toggleButton.setIcon(toggleButton.isSelected() ? UNCOLLAPSED_ICON : COLLAPSED_ICON);
		});
		KToggleButton toggleButton2 = KUIConstants.setupToggleButton("Other", UNCOLLAPSED_ICON, new Dimension(BUILDING_BUTTON_SIZE.width*2, BUILDING_BUTTON_SIZE.height));
		toggleButton2.setSelected(true);
		toggleButton2.addActionListener(e -> {
			for (int i = 0; i < Game.buildingTypeList.size(); i++) {
				if(!(Game.buildingTypeList.get(i).blocksMovement())) {
					buildingButtons[i].setVisible(toggleButton2.isSelected());
				}
			}
			toggleButton2.setIcon(toggleButton2.isSelected() ? UNCOLLAPSED_ICON : COLLAPSED_ICON);
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		scrollingPanel.add(toggleButton, c);
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 4; c.weightx = 1; c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		scrollingPanel.add(toggleButton2, c);
		int index1 = 0;
		int index2 = 0;
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			if(Game.buildingTypeList.get(i).isGate() || Game.buildingTypeList.get(i).blocksMovement()) {
				c = new GridBagConstraints();
				c.gridx = index1/3;
				c.gridy = index1%3 + 1;
				c.gridwidth = 1;
				c.weightx = 0.5;
				c.fill = GridBagConstraints.HORIZONTAL;
				scrollingPanel.add(buildingButtons[i], c);
				index1++;
			}
			else {
				c = new GridBagConstraints();
				c.gridx = index2%2;
				c.gridy = index2/2 + 5;
				c.gridwidth = 1;
				c.weightx = 0.5;
				c.fill = GridBagConstraints.HORIZONTAL;
				scrollingPanel.add(buildingButtons[i], c);
				index2++;
			}
		}
		c = new GridBagConstraints();
		c.gridy = Game.buildingTypeList.size()/2 + 3; 
		c.gridx = 0; c.gridwidth = 2; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		JPanel p = new JPanel();
		p.setOpaque(false);
		scrollingPanel.add(p, c);
	}
	
	public void updateButtons() {
		if(gameView.getFaction() != null) {
			for (int i = 0; i < Game.buildingTypeList.size(); i++) {
				BuildingType type = Game.buildingTypeList.get(i);
				JButton button = buildingButtons[i];
				if (gameView.getFaction().areRequirementsMet(type)) {
					button.setEnabled(true);
				} else {
					button.setEnabled(false);
				}
			}
		}
	}
	
	public JPanel getRootPanel() {
		return scrollingPanel.getRootPanel();
	}
}
