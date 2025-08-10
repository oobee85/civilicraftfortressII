package ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;

import game.Game;
import game.Research;
import game.ResearchRequirement;
import game.ResearchType;
import ui.KButton;
import ui.KUIConstants;
import ui.infopanels.ResearchInfoPanel;
import utils.Utils;

public class ResearchView {

	private static final int RESEARCH_ICON_SIZE = 25;

	private JPanel rootPanel;
	private JPanel progressPanel;
	private JPanel buttonsPanel;
	private GameView gameView;

	private HashMap<JButton, ResearchType> researchButtons = new HashMap<>();
	
	public ResearchView(GameView gameView) {
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		
		progressPanel = new ResearchProgressView(gameView);
		progressPanel.setPreferredSize(KUIConstants.MAIN_MENU_BUTTON_SIZE);
		
		buttonsPanel = new JPanel();
		buttonsPanel.setFocusable(false);
		
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(buttonsPanel, BorderLayout.CENTER);
		rootPanel.add(progressPanel, BorderLayout.NORTH);
		
		this.gameView = gameView;
		setup();
	}
	private void setup() {
		
		for (int i = 0; i < Game.researchTypeList.size(); i++) {
			ResearchType researchType = Game.researchTypeList.get(i);
			KButton button = KUIConstants.setupButton(researchType.toString(),
					Utils.resizeImageIcon(researchType.getMipMap().getImageIcon(0), RESEARCH_ICON_SIZE, RESEARCH_ICON_SIZE), null);
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
			buttonsPanel.add(button);
		}
	}
	
	public void updateButtons() {
		boolean hasResearchLab = gameView.getFaction().hasResearchLab();
		for(Entry<JButton, ResearchType> entry : researchButtons.entrySet()) {
			JButton button = entry.getKey();
			Research research = gameView.getFaction().getResearch(entry.getValue());
			ResearchRequirement req = research.getRequirement();
			if (research.isCompleted()) {
				button.setEnabled(false);
				button.setVisible(true);
			} 
//			else if(research.getTier() > 1) {
////				button.setEnabled(false);
////				button.setVisible(false);
//			} 
			else if (req.areRequirementsMet()) {
				button.setEnabled(true);
				button.setVisible(true);
			} 
//			else if (req.areSecondLayerRequirementsMet()) {
//				button.setEnabled(false);
//				button.setVisible(true);
//			} 
			else {
				button.setEnabled(false);
				button.setVisible(false);
			}
		}
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
