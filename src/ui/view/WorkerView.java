package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;

public class WorkerView {
	private static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	private static final int BUILDING_ICON_SIZE = 25;
	
	private static final ImageIcon COLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/collapsed.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE);
	private static final ImageIcon UNCOLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("Images/interfaces/uncollapsed.png"), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE);
	
	private class Pair {
		public final JButton button;
		public final BuildingType buildingType;
		public Pair(JButton button, BuildingType buildingType) {
			this.button = button;
			this.buildingType = buildingType;
		}
	}
	private JPanel rootPanel;
	private LinkedList<Pair> buildingButtons = new LinkedList<>();
	private GameView gameView;
	
	public WorkerView(GameView gameView) {
		this.gameView = gameView;
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		Pair[] buttons = populateBuildingTypeUI(rootPanel, 25);
		Collections.addAll(buildingButtons, buttons);
	}

	public Pair[] populateBuildingTypeUI(JPanel panel, int BUILDING_ICON_SIZE) {
		Pair[] pairs = new Pair[Game.buildingTypeList.size()];
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
			pairs[i] = new Pair(button, type);
			panel.add(button);
		}
		return pairs;
	}
	
	public void updateButtons() {
		for(Pair pair : buildingButtons) {
			boolean enabled = false;
			boolean visible = false;
			if (gameView.getFaction().areRequirementsMet(pair.buildingType)) {
				enabled = true;
			}
			for (Thing thing : gameView.getSelectedUnits()) {
				if (!(thing instanceof Unit)) {
					continue;
				}
				Unit unit = (Unit)thing;
				if (!unit.isBuilder()) {
					continue;
				}
				if (unit.getBuildableBuildingTypes().contains(pair.buildingType)) {
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
