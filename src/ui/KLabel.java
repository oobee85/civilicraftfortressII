package ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class KLabel extends JLabel {

	private LinkedList<ActionListener> rightClickListeners = new LinkedList<>();
	
	public KLabel(Icon icon) {
		super(icon);
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3) {
					for(ActionListener l : rightClickListeners) {
						l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, getText(), System.currentTimeMillis(), 0));
					}
				}
			}
			
		});
	}
	public void addRightClickActionListener(ActionListener l) {
		rightClickListeners.add(l);
	}
}
