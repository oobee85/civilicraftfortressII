package networking.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;

public class GameViewOverlay extends JPanel {

	private static final int RESOURCE_ICON_SIZE = 25;
	private static final Dimension RESOURCE_BUTTON_SIZE = new Dimension(80, 30);
	
	private GUIController guiController;
	private Faction faction;
	
	private JPanel resourcePanel2;
	private JPanel selectedUnitsPanel;
	
	private HashMap<Thing, JButton> selectedButtons = new HashMap<>();

	private KButton[] resourceIndicators = new KButton[ItemType.values().length];
	private boolean[] resourceIndicatorsDiscovered = new boolean[ItemType.values().length];
	
	public GameViewOverlay(GUIController guiController) {
		this.guiController = guiController;
		this.setOpaque(false);
		resourcePanel2 = new JPanel();
		resourcePanel2.setLayout(new BoxLayout(resourcePanel2, BoxLayout.Y_AXIS));
		resourcePanel2.setOpaque(false);
		JPanel filler = new JPanel();
		filler.setLayout(new BorderLayout());
		filler.setOpaque(false);
		selectedUnitsPanel = new JPanel();
		selectedUnitsPanel.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.add(resourcePanel2, BorderLayout.WEST);
		this.add(filler, BorderLayout.CENTER);
		filler.add(selectedUnitsPanel, BorderLayout.SOUTH);
		setupButtons();
	}
	
	public void setupButtons() {
		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			KButton button = KUIConstants.setupButton("",
					Utils.resizeImageIcon(type.getImageIcon(0), RESOURCE_ICON_SIZE, RESOURCE_ICON_SIZE),
					RESOURCE_BUTTON_SIZE);
			button.setEnabled(false);
			resourcePanel2.add(button);
			button.setVisible(false);
			if(type.getCost() != null) {
				button.addActionListener(e -> {
					int amount = 1;
					if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
						amount = 10;
					}
					guiController.tryToCraftItem(type, amount);
				});
			}
			button.addRightClickActionListener(e -> {
				guiController.switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					guiController.pushInfoPanel(new ItemTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					guiController.popInfoPanel();
				}
			});
			resourceIndicators[i] = button;
		}
	}
	
	public void selectedUnit(Unit unit, boolean selected) {
		if(selected) {
			if(!selectedButtons.containsKey(unit)) {
				JButton button = setupUnitButton(unit);
				selectedButtons.put(unit, button);
				selectedUnitsPanel.add(button);
				revalidate();
			}
		}
		else {
			if(selectedButtons.containsKey(unit)) {
				JButton button = selectedButtons.remove(unit);
				selectedUnitsPanel.remove(button);
				revalidate();
			}
		}
	}
	
	public void updateItems() {
		if(faction != null) {
			for (int i = 0; i < ItemType.values().length; i++) {
				int amount = faction.getItemAmount(ItemType.values()[i]);
				resourceIndicators[i].setText("" + amount);
				resourceIndicators[i].setVisible(resourceIndicatorsDiscovered[i] || amount > 0);
				if(amount > 0) {
					resourceIndicatorsDiscovered[i] = true;
				}
			}
		}
	}
	private JButton setupUnitButton(Unit unit) {
		KButton button = new KButton(null, Utils.resizeImageIcon(unit.getImageIcon(10), 30, 30)) {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				GameView.drawHealthBar2(g, unit, 0, getHeight() - 6, getWidth(), 6, 1, unit.getHealth()/unit.getMaxHealth());
			}
		};
		button.setMargin(KUIConstants.zeroMargin);
		button.setHorizontalAlignment(SwingConstants.CENTER);
		KUIConstants.setComponentAttributes(button, null);
		button.addActionListener(e -> {
			guiController.pressedSelectedUnitPortrait(unit);
		});
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				guiController.pushInfoPanel(new UnitInfoPanel(unit));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				guiController.popInfoPanel();
			}
		});
		return button;
	}
	
	public void changeFaction(Faction faction) {
		this.faction = faction;
		for(int i = 0; i < resourceIndicatorsDiscovered.length; i++) {
			resourceIndicatorsDiscovered[i] = false;
		}
	}
}
