package ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class KButton extends JButton {
	
	private boolean hovered;
	private boolean pressed;
	
	public KButton(String text, Icon icon) {
		super(text, icon);
		this.setContentAreaFilled(false);
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				pressed = true;
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				hovered = false;
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				pressed = false;
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		if(pressed) {
			g.setColor(KUIConstants.SELECTED_COLOR);
		}
		else if(hovered) {
			g.setColor(KUIConstants.HOVERED_COLOR);
		}
		else {
			g.setColor(KUIConstants.NORMAL_COLOR);
		}
		g.fillRect(0, 0, getWidth(), getHeight());

		if(pressed) {
			this.setForeground(KUIConstants.SELECTED_TEXT_COLOR);
			this.setBorderPainted(false);
		}
		else {
			this.setForeground(KUIConstants.NORMAL_TEXT_COLOR);
			this.setBorderPainted(true);
		}
		super.paintComponent(g);
	}
}
