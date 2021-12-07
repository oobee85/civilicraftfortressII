package ui.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ui.KButton;
import ui.KToggleButton;
import ui.KUIConstants;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

public class SettingsMenu {
	
	private JPanel content;
	
	public SettingsMenu(ActionListener backCallback) {
		content = new JPanel();
		content.setBackground(Color.black);
		KButton backButton = KUIConstants.setupButton("Back", null, DebugView.DEBUG_BUTTON_SIZE);
		backButton.addActionListener(backCallback);
		content.add(backButton);
	}
	public void addControlFor(Class c) {
		try {
			for(Field f : c.getFields()) {
				if(f.getType() == boolean.class) {
					KToggleButton toggle = KUIConstants.setupToggleButton(f.getName(), null, DebugView.DEBUG_BUTTON_SIZE);
					KUIConstants.resizeToFitContent(toggle, toggle.getText());
					toggle.setSelected((boolean)f.get(null));
					toggle.addActionListener(e -> {
						try {
							f.set(null, toggle.isSelected());
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
					});
					content.add(toggle);
				}
				else if(f.getType() == int.class || f.getType() == double.class) {
					JTextField text = KUIConstants.setupTextField("" + f.get(null), DebugView.DEBUG_BUTTON_SIZE);
					text.getDocument().addDocumentListener(new DocumentListener() {
						@Override public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
						@Override public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
						@Override
						public void changedUpdate(DocumentEvent e) {
							try {
								if(f.getType() == int.class) {
									int i = Integer.parseInt(text.getText());
									f.set(null, i);
								}
								else if(f.getType() == double.class) {
									double i = Double.parseDouble(text.getText());
									f.set(null, i);
								}
							} catch (NumberFormatException e1) {
								
							}
							catch (IllegalAccessException e1) {
								e1.printStackTrace();
							}
						}
					});
					JLabel label = KUIConstants.setupLabel(f.getName(), null, null);
					label.setBorder(BorderFactory.createEmptyBorder());
					JPanel group = new JPanel();
					group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
					group.add(label);
					group.add(text);
					
					content.add(group);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public JPanel getContentPanel() {
		return content;
	}
}
