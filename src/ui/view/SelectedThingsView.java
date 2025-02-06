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

public class SelectedThingsView extends JPanel {

	private static final SelectedButtonSizes[] selectedButtonSizes = new SelectedButtonSizes[] {
			new SelectedButtonSizes(42, 5, 5, 1),
			new SelectedButtonSizes(24, 3, 2, 2),
			new SelectedButtonSizes(17, 2, 0, 3),
	};
	
	private GUIController guiController;
	
	private JPanel selectedUnitsPanel;
	private SelectedButtonSizes currentUnitButtonSize = selectedButtonSizes[0];
	private WrapLayout selectedUnitsPanelLayout;
	
	private HashMap<Thing, JButton> selectedButtons = new HashMap<>();

	public SelectedThingsView(GUIController guiController) {
		this.guiController = guiController;
		this.setFocusable(false);

		selectedUnitsPanel = new JPanel();
		selectedUnitsPanelLayout = new WrapLayout(FlowLayout.CENTER, 5, 5);
		selectedUnitsPanel.setLayout(selectedUnitsPanelLayout);
		selectedUnitsPanel.setFocusable(false);
		
		this.setLayout(new BorderLayout());
		this.add(selectedUnitsPanel, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(0, 52));
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
}
