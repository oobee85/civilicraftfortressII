package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import networking.client.ClientGUI;
import ui.*;
import ui.utils.WrapLayout;
import utils.Settings;
import utils.Utils;

public class ActionsView {

	private static final Dimension ACTION_BUTTON_SIZE = new Dimension(100, 30);
	
	public static final ImageIcon MOVE_ICON = Utils.resizeImageIcon(
			Utils.loadImageIcon("Images/interfaces/mouse_cursors/move_icon.png"), 
			ACTION_BUTTON_SIZE.height, ACTION_BUTTON_SIZE.height);

	public static final ImageIcon ATTACK_ICON = Utils.resizeImageIcon(
			Utils.loadImageIcon("Images/interfaces/mouse_cursors/attack_icon.png"), 
			ACTION_BUTTON_SIZE.height, ACTION_BUTTON_SIZE.height);

	public static final ImageIcon DEFEND_ICON = Utils.resizeImageIcon(
			Utils.loadImageIcon("Images/interfaces/mouse_cursors/defend_icon.png"), 
			ACTION_BUTTON_SIZE.height, ACTION_BUTTON_SIZE.height);

	private static final ImageIcon EXPLODE_ICON = Utils.resizeImageIcon(
			Utils.loadImageIcon("Images/units/bomb.png"),
			ACTION_BUTTON_SIZE.height, ACTION_BUTTON_SIZE.height);


	private ScrollingPanel scrollingPanel;
	
	private KButton moveButton;
	private KButton attackButton;
//	private KButton makeRoadsButton;
//	private KButton autoBuildButton;
	private KButton guardButton;
	private KButton wanderButton;
	private KButton explodeButton;
	private JPanel actionButtonPanel;
	private WorkerView workerView;
	private ProduceUnitView produceUnitView;
	private CraftingView craftingView;
	private CraftingFocusView craftingFocusView;

	private final GameView gameView;
	public ActionsView(GameView gv) {
		this.gameView = gv;
		

		scrollingPanel = new ScrollingPanel(new Dimension(ClientGUI.GUIWIDTH, 1200));
		scrollingPanel.setFocusable(false);
		scrollingPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
		
		
		actionButtonPanel = new JPanel();
		actionButtonPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, 100));
		actionButtonPanel.setFocusable(false);
		actionButtonPanel.setBackground(new Color(150, 70, 0));
		
		scrollingPanel.add(actionButtonPanel);

		moveButton = addActionButton("Move", MOVE_ICON,
				e -> gameView.setLeftClickAction(LeftClickAction.MOVE));
		attackButton = addActionButton("Attack", ATTACK_ICON,
				e -> gameView.setLeftClickAction(LeftClickAction.ATTACK));
//		makeRoadsButton = addActionButton("Make Roads", null,
//				e -> gameView.workerRoad(Game.buildingTypeMap.get("STONE_ROAD")));
//		autoBuildButton = addActionButton("Autobuild", null,
//				e -> gameView.toggleAutoBuild());
		guardButton = addActionButton("Guard", DEFEND_ICON,
				e -> gameView.setLeftClickAction(LeftClickAction.GUARD));
		wanderButton = addActionButton("Wander", null,
				e -> gameView.setLeftClickAction(LeftClickAction.WANDER_AROUND));
		
		explodeButton = addActionButton("Explode", EXPLODE_ICON,
			e -> {
				if (Settings.DEBUG) {
					gameView.explodeSelected();
				}
			});
	}
	
	public void addViews(
			ProduceUnitView produceUnitView,
			CraftingView craftingView,
			WorkerView workerView,
			CraftingFocusView craftingFocusView) {
		
		this.produceUnitView = produceUnitView;
		this.produceUnitView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.produceUnitView.getRootPanel());
		
		this.craftingView = craftingView;
		this.craftingView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.craftingView.getRootPanel());
		
		this.craftingFocusView = craftingFocusView;
		this.craftingFocusView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.craftingFocusView.getRootPanel());
		
		this.workerView = workerView;
		this.workerView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.workerView.getRootPanel());
	}
	
	private KButton addActionButton(String text, Icon icon, ActionListener a) {
		KButton newButton = KUIConstants.setupButton(text, icon, ACTION_BUTTON_SIZE);
		newButton.addActionListener(a);
		newButton.setVisible(false);
		actionButtonPanel.add(newButton);
		return newButton;
	}
	
	private Set<Unit> selectedUnits = new HashSet<>();
	private Set<Unit> selectedBuilders = new HashSet<>();
	private Set<Unit> selectedNonBuilders = new HashSet<>();

	private Set<Building> selectedBuildings = new HashSet<>();
	private Set<Building> selectedProducingBuildings = new HashSet<>();
	private Set<Building> selectedCraftUpgradesBuildings = new HashSet<>();
	private Set<Building> selectedCraftingFocusBuildings = new HashSet<>();

	public boolean selectedUnit(Unit unit, boolean selected) {

		if (selected) {
			selectedUnits.add(unit);
			if (unit.isBuilder()) {
				selectedBuilders.add(unit);
			}
			else {
				selectedNonBuilders.add(unit);
			}
		}
		else {
			selectedUnits.remove(unit);
			if (unit.isBuilder()) {
				selectedBuilders.remove(unit);
			}
			else {
				selectedNonBuilders.remove(unit);
			}
		}
		
		int totalSelectedThings = selectedUnits.size() + selectedBuildings.size();
		
		moveButton.setVisible(!selectedUnits.isEmpty());
		wanderButton.setVisible(!selectedUnits.isEmpty());
		attackButton.setVisible(!selectedNonBuilders.isEmpty());
		guardButton.setVisible(!selectedNonBuilders.isEmpty());
		explodeButton.setVisible(Settings.DEBUG && (totalSelectedThings > 0));
		
		workerView.getRootPanel().setVisible(!selectedBuilders.isEmpty());

		updateHeight();
		
		return totalSelectedThings > 0;
	}
	
	public boolean selectedBuilding(Building building, boolean selected) {
		boolean producer = !building.getType().unitsCanProduceSet().isEmpty();
		boolean crafter = building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB");
		boolean craftingFocus = 
				building.getType() == Game.buildingTypeMap.get("SMITHY")
				|| building.getType() == Game.buildingTypeMap.get("QUARRY")
				|| building.getType() == Game.buildingTypeMap.get("SAWMILL");
		if (selected) {
			selectedBuildings.add(building);
			if (producer) {
				selectedProducingBuildings.add(building);
			}
			if (crafter) {
				selectedCraftUpgradesBuildings.add(building);
			}
			if (craftingFocus) {
				selectedCraftingFocusBuildings.add(building);
			}
		}
		else {
			selectedBuildings.remove(building);
			if (producer) {
				selectedProducingBuildings.remove(building);
			}
			if (crafter) {
				selectedCraftUpgradesBuildings.remove(building);
			}
			if (craftingFocus) {
				selectedCraftingFocusBuildings.remove(building);
			}
		}

		int totalSelectedThings = selectedUnits.size() + selectedBuildings.size();

		explodeButton.setVisible(Settings.DEBUG && (totalSelectedThings > 0));
		produceUnitView.getRootPanel().setVisible(!selectedProducingBuildings.isEmpty());
		craftingView.getRootPanel().setVisible(!selectedCraftUpgradesBuildings.isEmpty());
		craftingFocusView.getRootPanel().setVisible(!selectedCraftingFocusBuildings.isEmpty());

		updateHeight();
		
		return totalSelectedThings > 0;
	}
	
	private void updateHeight() {

		int numVisible = 0;
		for (Component c : actionButtonPanel.getComponents()) {
			if (c.isVisible()) {
				numVisible++;
			}
		}
		int numrows = (numVisible+2) / 3;
		int defaultFlowLayoutOffset = 5;
		int rowheight = ACTION_BUTTON_SIZE.height + defaultFlowLayoutOffset;
		actionButtonPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, rowheight * numrows + defaultFlowLayoutOffset));
		

		int totalHeight = 80;
		if (produceUnitView.getRootPanel().isVisible()) {
			totalHeight += produceUnitView.getRootPanel().getPreferredSize().height;
		}
		if (craftingView.getRootPanel().isVisible()) {
			totalHeight += craftingView.getRootPanel().getPreferredSize().height;
		}
		if (craftingFocusView.getRootPanel().isVisible()) {
			totalHeight += craftingFocusView.getRootPanel().getPreferredSize().height;
		}
		if (workerView.getRootPanel().isVisible()) {
			totalHeight += workerView.getRootPanel().getPreferredSize().height;
		}
		scrollingPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, totalHeight));
	}

	public JPanel getRootPanel() {
		return scrollingPanel.getRootPanel();
	}
}
