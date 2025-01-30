package ui.view;

import static game.ItemType.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JPanel;

import game.*;
import ui.*;
import ui.infopanels.ItemTypeInfoPanel;
import utils.Utils;

public class ResourceView extends JPanel {

	private static final int RESOURCE_ICON_SIZE = 19;
	private static final Dimension RESOURCE_BUTTON_SIZE = new Dimension(70, 21);

	private Faction faction;
	private KButton[] resourceIndicators = new KButton[ItemType.values().length];
	private boolean[] resourceIndicatorsDiscovered = new boolean[ItemType.values().length];
	
	private GUIController guiController;
	public ResourceView(GUIController guiController) {
		this.guiController = guiController;
		this.setFocusable(false);
		this.setLayout(new GridBagLayout());
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
			button.setFont(KUIConstants.buttonFontMini);
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
		
		// inventory rows
		ItemType[][] rows = new ItemType[][] {
			new ItemType[] {
					FOOD, WOOD, STONE, COPPER_ORE, SILVER_ORE, IRON_ORE,
					MITHRIL_ORE, GOLD_ORE, ADAMANTITE_ORE, RUNITE_ORE, TITANIUM_ORE, SWORD, SHIELD
			},
			new ItemType[] {
					HORSE, MAGIC, CLAY, COAL, BRONZE_BAR, IRON_BAR, MITHRIL_BAR,
					GOLD_BAR, ADAMANTITE_BAR, RUNITE_BAR, TITANIUM_BAR, BOW, BRICK
			},
		};
		for(int row = 0; row < rows.length; row++) {
			for(int column = 0; column < rows[row].length; column++) {
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = column; c.gridy = row;
				this.add(resourceIndicators[rows[row][column].ordinal()], c);
			}
		}
	}
	
	public void updateItems() {
		if(faction != null) {
			for (int i = 0; i < ItemType.values().length; i++) {
				int amount = faction.getInventory().getItemAmount(ItemType.values()[i]);
				resourceIndicators[i].setFont((amount >= 1000) ? KUIConstants.buttonFontMinimini : KUIConstants.buttonFontMini);
				resourceIndicators[i].setText("" + amount);
				resourceIndicators[i].setVisible(resourceIndicatorsDiscovered[i] || amount > 0);
				if(amount > 0) {
					resourceIndicatorsDiscovered[i] = true;
				}
			}
		}
	}
	
	public void changeFaction(Faction faction) {
		this.faction = faction;
		for(int i = 0; i < resourceIndicatorsDiscovered.length; i++) {
			resourceIndicatorsDiscovered[i] = false;
		}
	}
}
