package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

public class KSlider extends JPanel implements MouseListener, MouseMotionListener {
	
	private int min;
	private int max;
	private int value;
	private String label;
	private ArrayList<ChangeListener> listeners = new ArrayList<>();
	
	public KSlider(int min, int max, String label) {
		this.min = min;
		this.max = max;
		this.value = 0;
		this.label = label;
		this.setFocusable(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public int getValue() {
		return value;
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
	public void setValue(int proposedValue) {
		int oldValue = value;
		value = (proposedValue < min) ? min : ((proposedValue > max) ? max : proposedValue);
		if (oldValue != value) {
			ChangeEvent event = new ChangeEvent(this);
			for (ChangeListener l : listeners) {
				l.stateChanged(event);
			}
			repaint();
			SwingUtilities.invokeLater(() -> repaint());
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		int pixelsToFill = this.getWidth() * (value - min) / (max - min);
		g.setColor(Color.black);
		g.fillRect(0, 0, pixelsToFill, getHeight());
		
		String formatted = String.format(label, value);
		
		int labelWidth = g.getFontMetrics().stringWidth(formatted);
		g.setColor(Color.white);
		g.drawString(formatted, 5, 2 + g.getFont().getSize());
		g.setColor(Color.black);
		g.drawString(formatted, this.getWidth() - 5 - labelWidth, this.getHeight() - 2);
	}

	private void adjustValueBasedOnMouseClick(Point p) {
		int newValue = (max - min) * p.x / getWidth();
		setValue(newValue);
	}

	private boolean mouseDown = false;
	@Override
	public void mouseClicked(MouseEvent e) {
		adjustValueBasedOnMouseClick(e.getPoint());
		mouseDown = false;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		adjustValueBasedOnMouseClick(e.getPoint());
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		adjustValueBasedOnMouseClick(e.getPoint());
		mouseDown = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (mouseDown) {
			adjustValueBasedOnMouseClick(e.getPoint());
		}
		mouseDown = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		adjustValueBasedOnMouseClick(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
//		adjustValueBasedOnMouseClick(e.getPoint());
	}
}
