package ui;

import java.awt.*;
import javax.swing.*;

public class ScrollingPanel extends JPanel {

	private JPanel rootPanel;

	public ScrollingPanel(Dimension preferredSize) {
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		rootPanel.setLayout(new BorderLayout());
		
		JScrollPane scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		this.setPreferredSize(preferredSize);
		
		rootPanel.add(scrollPane);
	}
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
