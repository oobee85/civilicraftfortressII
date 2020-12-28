package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class KButton extends JButton {
	
	private LinkedList<ActionListener> rightClickListeners = new LinkedList<>();
	
	private boolean hovered;
	private boolean pressed;
	private boolean enabled = true;
	
	public KButton(String text, Icon icon) {
		super(text, icon);
		this.setContentAreaFilled(false);
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				if(e.getButton() == MouseEvent.BUTTON3) {
					for(ActionListener l : rightClickListeners) {
						l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, text, System.currentTimeMillis(), 0));
					}
				}
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
	
	public void addRightClickActionListener(ActionListener l) {
		rightClickListeners.add(l);
	}

	// Note we are overriding setEnabled so that the parent class always thinks it is enabled to allow mouse interaction.
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * not to be confused with isEnabled(). getEnabled() returns the KButton enabled status, the underlying button is always enabled.
	 */
	public boolean getEnabled() {
		return enabled;
	}

	@Override
	public void paintComponent(Graphics g) {
		if(pressed && enabled) {
			g.setColor(KUIConstants.SELECTED_COLOR);
		}
		else if(hovered && enabled) {
			g.setColor(KUIConstants.HOVERED_COLOR);
		}
		else if(enabled) {
			g.setColor(KUIConstants.NORMAL_COLOR);
		}
		else {
			g.setColor(KUIConstants.HOVERED_COLOR);
		}
		g.fillRect(0, 0, getWidth(), getHeight());

		if(pressed && enabled) {
			this.setForeground(KUIConstants.SELECTED_TEXT_COLOR);
			this.setBorderPainted(false);
		}
		else if(enabled) {
			this.setForeground(KUIConstants.NORMAL_TEXT_COLOR);
			this.setBorderPainted(true);
		}
		else {
			this.setForeground(KUIConstants.DISABLED_TEXT_COLOR);
			this.setBorderPainted(false);
		}
		super.paintComponent(g);
	}
}
