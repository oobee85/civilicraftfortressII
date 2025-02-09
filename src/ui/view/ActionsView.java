package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import networking.client.ClientGUI;
import ui.*;
import ui.utils.WrapLayout;
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
	
	private ScrollingPanel scrollingPanel;
	
	
	private KButton moveButton;
	private KButton attackButton;
//	private KButton makeRoadsButton;
//	private KButton autoBuildButton;
	private KButton guardButton;
	private KButton wanderButton;
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

//		JPanel looseButtons = new JPanel();
//		looseButtons.setFocusable(false);
//		looseButtons.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));

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
		
		
//		this.setLayout(new BorderLayout());
//		this.add(looseButtons, BorderLayout.CENTER);
	}
	
	public void addViews(
			ProduceUnitView produceUnitView,
			CraftingView craftingView,
			WorkerView workerView,
			CraftingFocusView craftingFocusView) {
		
//		JPanel views = new JPanel();
//		views.setFocusable(false);
//		views.setLayout(new BorderLayout());
		
		// TODO fix layout
		this.produceUnitView = produceUnitView;
		this.produceUnitView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.produceUnitView.getRootPanel());
//		scrollingPanel.add(this.produceUnitView.getRootPanel(), BorderLayout.NORTH);
		
		this.craftingView = craftingView;
		this.craftingView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.craftingView.getRootPanel());
//		scrollingPanel.add(this.craftingView.getRootPanel(), BorderLayout.CENTER);
		
		this.craftingFocusView = craftingFocusView;
		this.craftingFocusView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.craftingFocusView.getRootPanel());
//		scrollingPanel.add(this.craftingFocusView.getRootPanel(), BorderLayout.SOUTH);
		
		this.workerView = workerView;
		this.workerView.getRootPanel().setVisible(false);
		scrollingPanel.add(this.workerView.getRootPanel());
//		scrollingPanel.add(this.workerView.getRootPanel(), BorderLayout.SOUTH);
		
//		this.add(views, BorderLayout.SOUTH);
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
		
		boolean move = false;
		boolean attack = false;
		boolean guard = false;
		boolean wander = false;
		if (!selectedUnits.isEmpty()) {
			move = true;
			wander = true;
		}
		
		if (!selectedNonBuilders.isEmpty()) {
			attack = true;
			guard = true;
		}
		
		moveButton.setVisible(move);
		attackButton.setVisible(attack);
		guardButton.setVisible(guard);
		wanderButton.setVisible(wander);
		
		int numVisible = (move ? 1 : 0) + (attack ? 1 : 0) + (guard ? 1 : 0) + (wander ? 1 : 0);
		int numrows = (numVisible+1) / 3;
		int defaultFlowLayoutOffset = 5;
		int rowheight = ACTION_BUTTON_SIZE.height + defaultFlowLayoutOffset;
		actionButtonPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, rowheight * numrows + defaultFlowLayoutOffset));
		
		workerView.getRootPanel().setVisible(!selectedBuilders.isEmpty());

		updateHeight();
		
		return !selectedUnits.isEmpty() 
				|| !selectedBuildings.isEmpty();
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
		
		produceUnitView.getRootPanel().setVisible(!selectedProducingBuildings.isEmpty());
		craftingView.getRootPanel().setVisible(!selectedCraftUpgradesBuildings.isEmpty());
		craftingFocusView.getRootPanel().setVisible(!selectedCraftingFocusBuildings.isEmpty());

		updateHeight();
		
		return !selectedUnits.isEmpty() 
				|| !selectedBuildings.isEmpty();
	}
	
	private void updateHeight() {
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
