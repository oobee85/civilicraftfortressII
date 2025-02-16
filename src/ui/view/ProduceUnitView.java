package ui.view;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class ProduceUnitView {

	private class Pair {
		public final JButton button;
		public final UnitType unitType;
		public Pair(JButton button, UnitType unitType) {
			this.button = button;
			this.unitType = unitType;
		}
	}
	
	private JPanel rootPanel;
	private LinkedList<Pair> unitButtons = new LinkedList<>();
	private GameView gameView;
	
	public ProduceUnitView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		Pair[] buttons = populateUnitTypeUI(rootPanel, 25);
		Collections.addAll(unitButtons, buttons);
	}
	
	public Pair[] populateUnitTypeUI(JPanel panel, int BUILDING_ICON_SIZE) {
		Pair[] pairs = new Pair[Game.unitTypeList.size()];
		for(int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton("Build " + type.toString(),
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameView.tryToBuildUnit(type);
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
			pairs[i] = new Pair(button, type);
			panel.add(button);
		}
		return pairs;
	}
	
	public void updateButtons() {
		for(Pair pair : unitButtons) {
			boolean enabled = false;
			boolean visible = false;
			if (gameView.getFaction().areRequirementsMet(pair.unitType)
					&& gameView.getFaction().canAfford(pair.unitType.getCost())) {
				enabled = true;
			}
			for(BuildingType buildingType : Game.buildingTypeList) {
				if(buildingType.unitsCanProduceSet().contains(pair.unitType)
						&& gameView.getFaction().isBuildingSelected(buildingType)) {
					visible = true;
					break;
				}
			}
			pair.button.setEnabled(enabled);
			pair.button.setVisible(visible);
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
