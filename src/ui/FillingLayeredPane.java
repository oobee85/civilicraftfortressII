package ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class FillingLayeredPane extends JLayeredPane {
	private static final long serialVersionUID = 1L;
	public FillingLayeredPane() {
		super();
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				fillComponents();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				fillComponents();
			}
		});
		this.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				fillComponents();
			}
		});
	}
	private void fillComponents() {
		for(Component c : getComponents()) {
			c.setBounds(getBounds());
		}
	}
}
