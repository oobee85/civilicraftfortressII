package networking.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import game.*;
import ui.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class ResearchView {

	private static final int RESEARCH_ICON_SIZE = 25;

	private JPanel rootPanel;
	private GUIController guiInterface;

	private HashMap<JButton, ResearchType> researchButtons = new HashMap<>();
	
	public ResearchView(GUIController guiInterface) {
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		
		this.guiInterface = guiInterface;
		setup();
	}
	private void setup() {
		for (int i = 0; i < Game.researchTypeList.size(); i++) {
			ResearchType researchType = Game.researchTypeList.get(i);
			KButton button = KUIConstants.setupButton(researchType.toString(),
					Utils.resizeImageIcon(researchType.getImageIcon(0), RESEARCH_ICON_SIZE, RESEARCH_ICON_SIZE), null);
			button.setEnabled(false);
			button.addActionListener(e -> {
				guiInterface.research(researchType);
			});
			button.addRightClickActionListener(e -> {
				guiInterface.switchInfoPanel(new ResearchInfoPanel(World.PLAYER_FACTION.getResearch(researchType)));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					guiInterface.pushInfoPanel(new ResearchInfoPanel(World.PLAYER_FACTION.getResearch(researchType)));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					guiInterface.popInfoPanel();
				}
			});
			researchButtons.put(button, researchType);
			rootPanel.add(button);
		}
	}
	
	public void updateButtons(World world) {
		boolean hasResearchLab = World.PLAYER_FACTION.hasResearchLab(world);
		for(Entry<JButton, ResearchType> entry : researchButtons.entrySet()) {
			JButton button = entry.getKey();
			Research research = World.PLAYER_FACTION.getResearch(entry.getValue());
			ResearchRequirement req = research.getRequirement();
			if (research.isUnlocked()) {
				button.setEnabled(false);
				button.setVisible(true);
			} else if(research.getTier() > 1 && !hasResearchLab) {
				button.setEnabled(false);
				button.setVisible(false);
			} else if (req.areRequirementsMet()) {
				button.setEnabled(true);
				button.setVisible(true);
			} else if (req.areSecondLayerRequirementsMet()) {
				button.setEnabled(false);
				button.setVisible(true);
			} else {
				button.setEnabled(false);
				button.setVisible(false);
			}
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
