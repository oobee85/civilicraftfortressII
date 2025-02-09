package ui.view;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class InfoPanelView {
	
	private JPanel rootPanel;
	private LinkedList<JPanel> infoPanelStack = new LinkedList<>();
	
	public InfoPanelView() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
	}

	/** 
	 * clears infoPanelStack
	 */
	public void switchInfoPanel(JPanel newInfo) {
		infoPanelStack.clear();
		pushInfoPanel(newInfo);
	}
	public void pushInfoPanel(JPanel newInfo) {
		infoPanelStack.addFirst(newInfo);
		setInfoPanel(newInfo);
	}
	public void popInfoPanel() {
		if(infoPanelStack.size() > 1) {
			infoPanelStack.removeFirst();
		}
		JPanel newInfo = infoPanelStack.getFirst();
		setInfoPanel(newInfo);
	}

	private void setInfoPanel(JPanel newInfo) {
		SwingUtilities.invokeLater(() -> {
			rootPanel.removeAll();
			rootPanel.add(newInfo, BorderLayout.CENTER);
			rootPanel.validate();
		});
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
