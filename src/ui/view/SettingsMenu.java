package ui.view;

import javax.swing.*;

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
		KButton backButton = KUIConstants.setupButton("Back", null, KUIConstants.MAIN_MENU_BUTTON_SIZE);
		backButton.addActionListener(backCallback);
		content.add(backButton);
	}
	public void addControlFor(Class c) {
		try {
			for(Field f : c.getFields()) {
				if(f.getType() == boolean.class) {
					KToggleButton toggle = KUIConstants.setupToggleButton(f.getName(), null, KUIConstants.MAIN_MENU_BUTTON_SIZE);
					toggle.setSelected((boolean)f.get(null));
					toggle.addActionListener(e -> {
						try {
							f.set(null, toggle.isSelected());
						} catch (IllegalArgumentException e1) {
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
					});
					content.add(toggle);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public JPanel getContentPanel() {
		return content;
	}
}
