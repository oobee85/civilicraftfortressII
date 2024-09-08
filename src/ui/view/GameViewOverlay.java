package ui.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.graphics.vanilla.*;
import ui.infopanels.*;
import ui.utils.*;
import utils.*;
import static game.ItemType.*;

class SelectedButtonSizes {
	int buttonSize;
	int imageSize;
	int healthBarSize;
	int gapSize;
	int numRowsAllowed;
	public SelectedButtonSizes(int buttonSize, int healthBarSize, int gapSize, int numRowsAllowed) {
		this.buttonSize = buttonSize;
		this.imageSize = buttonSize * 87/100;
		this.healthBarSize = healthBarSize;
		this.gapSize = gapSize;
		this.numRowsAllowed = numRowsAllowed;
	}
}

public class GameViewOverlay extends JPanel {

	private static final int RESOURCE_ICON_SIZE = 21;
	private static final Dimension RESOURCE_BUTTON_SIZE = new Dimension(80, 25);
	
	private static final SelectedButtonSizes[] selectedButtonSizes = new SelectedButtonSizes[] {
			new SelectedButtonSizes(40, 5, 5, 2),
			new SelectedButtonSizes(30, 4, 3, 3),
			new SelectedButtonSizes(25, 3, 2, 4),
	};
	
	private GUIController guiController;
	private Faction faction;
	
	private JPanel resourcePanel2;
	private ScrollingPanel selectedUnitsPanel;
	private SelectedButtonSizes currentUnitButtonSize = selectedButtonSizes[0];
	private WrapLayout selectedUnitsPanelLayout;
	
	private HashMap<Thing, JButton> selectedButtons = new HashMap<>();

	private KButton[] resourceIndicators = new KButton[ItemType.values().length];
	private boolean[] resourceIndicatorsDiscovered = new boolean[ItemType.values().length];
	
	public GameViewOverlay(GUIController guiController) {
		this.guiController = guiController;
		this.setFocusable(false);
		resourcePanel2 = new JPanel();
		resourcePanel2.setLayout(new GridBagLayout());
		resourcePanel2.setOpaque(false);
		
		selectedUnitsPanel = new ScrollingPanel(null);
		selectedUnitsPanelLayout = new WrapLayout(FlowLayout.CENTER, 5, 5);
		selectedUnitsPanel.setLayout(selectedUnitsPanelLayout);
		selectedUnitsPanel.setFocusable(false);
		
		this.setLayout(new BorderLayout());
		this.add(resourcePanel2, BorderLayout.WEST);
		this.add(selectedUnitsPanel.getRootPanel(), BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(0, 100));
		setupButtons();
	}
	
	public void setupButtons() {
		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			KButton button = KUIConstants.setupButton("",
					Utils.resizeImageIcon(type.getMipMap().getImageIcon(0), RESOURCE_ICON_SIZE, RESOURCE_ICON_SIZE),
					RESOURCE_BUTTON_SIZE);
			button.setEnabled(false);
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
				guiController.switchInfoPanel(new ItemTypeInfoPanel(type, faction));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					guiController.pushInfoPanel(new ItemTypeInfoPanel(type, faction));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					guiController.popInfoPanel();
				}
			});
			resourceIndicators[i] = button;
		}
		ItemType[][] columns = new ItemType[][] {
			new ItemType[] {
					FOOD, WOOD, STONE, COAL, HORSE
			},
			new ItemType[] {
					COPPER_ORE, SILVER_ORE, IRON_ORE, MITHRIL_ORE, GOLD_ORE, ADAMANTITE_ORE, RUNITE_ORE, TITANIUM_ORE
			},
			new ItemType[] {
					BRONZE_BAR, IRON_BAR, MITHRIL_BAR, GOLD_BAR, ADAMANTITE_BAR, RUNITE_BAR, TITANIUM_BAR
			}
		};
		for(int row = 0; row < columns.length; row++) {
			for(int column = 0; column < columns[row].length; column++) {
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = column; c.gridy = row;
				resourcePanel2.add(resourceIndicators[columns[row][column].ordinal()], c);
			}
		}
	}
	
	private void updateSelectedButtonSizes(SelectedButtonSizes targetSize) {
		if(targetSize == currentUnitButtonSize) {
			return;
		}
		for(Entry<Thing, JButton> entry : selectedButtons.entrySet()) {
			entry.getValue().setIcon(Utils.resizeImageIcon(entry.getKey().getMipMap().getImageIcon(targetSize.imageSize), targetSize.imageSize, targetSize.imageSize));
			entry.getValue().setPreferredSize(new Dimension(targetSize.buttonSize, targetSize.buttonSize));
		}
		currentUnitButtonSize = targetSize;
	}
	
	private void updateSelectedButtonSizesIfNeeded() {
		for(SelectedButtonSizes sizes : selectedButtonSizes) {
			int numPerRow = (selectedUnitsPanel.getWidth() - sizes.gapSize)/(sizes.buttonSize + sizes.gapSize);
			if(selectedButtons.size() <= numPerRow * sizes.numRowsAllowed) {
				updateSelectedButtonSizes(sizes);
				selectedUnitsPanelLayout.setHgap(sizes.gapSize);
				selectedUnitsPanelLayout.setVgap(sizes.gapSize);
				break;
			}
		}
	}
	
	public void selectedUnit(Unit unit, boolean selected) {
		if(selected) {
			if(!selectedButtons.containsKey(unit)) {
				JButton button = setupUnitButton(unit);
				selectedButtons.put(unit, button);
				selectedUnitsPanel.add(button);
				updateSelectedButtonSizesIfNeeded();
				revalidate();
			}
		}
		else {
			if(selectedButtons.containsKey(unit)) {
				JButton button = selectedButtons.remove(unit);
				selectedUnitsPanel.remove(button);
				updateSelectedButtonSizesIfNeeded();
				revalidate();
			}
		}
	}
	
	public void updateItems() {
		if(faction != null) {
			for (int i = 0; i < ItemType.values().length; i++) {
				int amount = faction.getInventory().getItemAmount(ItemType.values()[i]);
				resourceIndicators[i].setText("" + amount);
				resourceIndicators[i].setVisible(resourceIndicatorsDiscovered[i] || amount > 0);
				if(amount > 0) {
					resourceIndicatorsDiscovered[i] = true;
				}
			}
		}
	}
	private JButton setupUnitButton(Unit unit) {
		KButton button = new KButton(null, Utils.resizeImageIcon(unit.getMipMap().getImageIcon(10), currentUnitButtonSize.imageSize, currentUnitButtonSize.imageSize)) {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				RenderingFunctions.drawHealthBar2(g, unit, 0, getHeight() - currentUnitButtonSize.healthBarSize, getWidth(), currentUnitButtonSize.healthBarSize, 1, unit.getHealth()/unit.getMaxHealth());
			}
		};
		button.setPreferredSize(new Dimension(currentUnitButtonSize.buttonSize, currentUnitButtonSize.buttonSize));
		button.setMargin(KUIConstants.zeroMargin);
		button.setBorder(BorderFactory.createEmptyBorder());
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
