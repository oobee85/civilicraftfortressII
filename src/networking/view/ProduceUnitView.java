package networking.view;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.Frame.*;
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
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
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
		boolean castleSelected = gameView.getFaction().isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("CASTLE"));
		boolean barracksSelected = gameView.getFaction().isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("BARRACKS"));
		boolean workshopSelected = gameView.getFaction().isBuildingSelected(gameView.getGameInstance().world, Game.buildingTypeMap.get("WORKSHOP"));
		for(Pair pair : unitButtons) {
			if((castleSelected && Game.buildingTypeMap.get("CASTLE").unitsCanBuildSet().contains(pair.unitType)) 
					|| (barracksSelected && Game.buildingTypeMap.get("BARRACKS").unitsCanBuildSet().contains(pair.unitType))
					|| (workshopSelected && Game.buildingTypeMap.get("WORKSHOP").unitsCanBuildSet().contains(pair.unitType))) {
				if (gameView.getFaction().areRequirementsMet(pair.unitType)) {
					pair.button.setEnabled(true);
					pair.button.setVisible(true);
					continue;
				}
			}
			pair.button.setEnabled(false);
			pair.button.setVisible(false);
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
