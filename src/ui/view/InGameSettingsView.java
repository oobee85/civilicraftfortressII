package ui.view;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import networking.client.ClientGUI;
import ui.KSlider;
import utils.Settings;

public class InGameSettingsView {

	private JPanel rootPanel;
	
	public InGameSettingsView() {
		rootPanel = new JPanel();
		rootPanel.setFocusable(false);
		rootPanel.setPreferredSize(new Dimension(ClientGUI.GUIWIDTH, 500));
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

		Dimension SLIDER_SIZE = new Dimension(160, 30);
		int padding = SLIDER_SIZE.height/2;
		
		rootPanel.add(Box.createRigidArea(new Dimension(0, padding)));
		
		KSlider volumeSlider = new KSlider(0, 100, "Global Volume %d%%");
		volumeSlider.setPreferredSize(SLIDER_SIZE);
		volumeSlider.setMaximumSize(SLIDER_SIZE);
		volumeSlider.setValue(Settings.VOLUME);
		volumeSlider.addChangeListener(e -> {
			Settings.VOLUME = volumeSlider.getValue();
		});
		rootPanel.add(volumeSlider);

		rootPanel.add(Box.createRigidArea(new Dimension(0, padding)));

		KSlider volumeSliderMusic = new KSlider(0, 100, "Music Volume %d%%");
		volumeSliderMusic.setPreferredSize(SLIDER_SIZE);
		volumeSliderMusic.setMaximumSize(SLIDER_SIZE);
		volumeSliderMusic.setValue(Settings.VOLUME_MUSIC);
		volumeSliderMusic.addChangeListener(e -> {
			Settings.VOLUME_MUSIC = volumeSliderMusic.getValue();
		});
		rootPanel.add(volumeSliderMusic);

		rootPanel.add(Box.createRigidArea(new Dimension(0, padding)));

		KSlider volumeSliderEffects = new KSlider(0, 100, "Effects Volume %d%%");
		volumeSliderEffects.setPreferredSize(SLIDER_SIZE);
		volumeSliderEffects.setMaximumSize(SLIDER_SIZE);
		volumeSliderEffects.setValue(Settings.VOLUME_EFFECTS);
		volumeSliderEffects.addChangeListener(e -> {
			Settings.VOLUME_EFFECTS = volumeSliderEffects.getValue();
		});
		rootPanel.add(volumeSliderEffects);
	}
	
	public JPanel getRootPanel() {
		return rootPanel;
	}
}
