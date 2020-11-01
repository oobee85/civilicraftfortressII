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
	private GameView gameView;

	private HashMap<JButton, ResearchType> researchButtons = new HashMap<>();
	
	public ResearchView(GameView gameView) {
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		
		this.gameView = gameView;
		setup();
	}
	private void setup() {
		for (int i = 0; i < Game.researchTypeList.size(); i++) {
			ResearchType researchType = Game.researchTypeList.get(i);
			KButton button = KUIConstants.setupButton(researchType.toString(),
					Utils.resizeImageIcon(researchType.getImageIcon(0), RESEARCH_ICON_SIZE, RESEARCH_ICON_SIZE), null);
			button.setEnabled(false);
			button.addActionListener(e -> {
				gameView.getGameInstance().getGUIController().research(researchType);
			});
			button.addRightClickActionListener(e -> {
				gameView.getGameInstance().getGUIController().switchInfoPanel(new ResearchInfoPanel(gameView.getFaction().getResearch(researchType), gameView.getFaction()));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					gameView.getGameInstance().getGUIController().pushInfoPanel(new ResearchInfoPanel(gameView.getFaction().getResearch(researchType), gameView.getFaction()));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					gameView.getGameInstance().getGUIController().popInfoPanel();
				}
			});
			researchButtons.put(button, researchType);
			rootPanel.add(button);
		}
	}
	
	public void updateButtons(World world) {
		boolean hasResearchLab = gameView.getFaction().hasResearchLab(world);
		for(Entry<JButton, ResearchType> entry : researchButtons.entrySet()) {
			JButton button = entry.getKey();
			Research research = gameView.getFaction().getResearch(entry.getValue());
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
