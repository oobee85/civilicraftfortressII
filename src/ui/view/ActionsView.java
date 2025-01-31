package ui.view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.utils.WrapLayout;

public class ActionsView extends JPanel {

	private static final Dimension ACTION_BUTTON_SIZE = new Dimension(100, 30);

	private KButton moveButton;
	private KButton attackButton;
	private KButton makeRoadsButton;
	private KButton autoBuildButton;
	private KButton guardButton;
	private KButton wanderButton;

	private final GameView gameView;
	public ActionsView(GameView gv) {
		this.gameView = gv;
		setFocusable(false);
		this.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));

		moveButton = addActionButton("Move", null,
				e -> gameView.setLeftClickAction(LeftClickAction.MOVE));
		attackButton = addActionButton("Attack", null,
				e -> gameView.setLeftClickAction(LeftClickAction.ATTACK));
		makeRoadsButton = addActionButton("Make Roads", null,
				e -> gameView.workerRoad(Game.buildingTypeMap.get("STONE_ROAD")));
		autoBuildButton = addActionButton("Autobuild", null,
				e -> gameView.toggleAutoBuild());
		guardButton = addActionButton("Guard", null,
				e -> gameView.setLeftClickAction(LeftClickAction.GUARD));
		wanderButton = addActionButton("Wander", null,
				e -> gameView.setLeftClickAction(LeftClickAction.WANDER_AROUND));
	}
	
	private KButton addActionButton(String text, Icon icon, ActionListener a) {
		KButton newButton = KUIConstants.setupButton(text, icon, ACTION_BUTTON_SIZE);
		newButton.addActionListener(a);
		newButton.setVisible(false);
		this.add(newButton);
		return newButton;
	}
	
	private Set<Unit> selectedUnits = new HashSet<>();
	private Set<Unit> selectedBuilders = new HashSet<>();
	private Set<Unit> selectedNonBuilders = new HashSet<>();

	public void selectedUnit(Unit unit, boolean selected) {
		
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
		boolean makeRoads = false;
		boolean autoBuild = false;
		boolean attack = false;
		boolean guard = false;
		boolean wander = false;
		if (!selectedUnits.isEmpty()) {
			move = true;
			wander = true;
		}
		
		if (!selectedBuilders.isEmpty()) {
			makeRoads = true;
			autoBuild = true;
		}
		
		if (!selectedNonBuilders.isEmpty()) {
			attack = true;
			guard = true;
		}
		
		moveButton.setVisible(move);
		makeRoadsButton.setVisible(makeRoads);
		autoBuildButton.setVisible(autoBuild);
		attackButton.setVisible(attack);
		guardButton.setVisible(guard);
		wanderButton.setVisible(wander);
	}
}
