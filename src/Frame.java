import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;


public class Frame extends JPanel{
	private Timer timmy;
	public JPanel panel;
	public JFrame frame;
	public JPanel gamepanel;
	private int WIDTH;
	private int HEIGHT;
	public Game gameInstance;
	private Point worldSize = new Point();
	private int mx;
	private int my;
	public JPanel gui;
	private boolean dragged = false;
	
	private Thread gameLoopThread;
	
	public Frame(int w, int h, int ws) {
	
		frame = new JFrame("Civilization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		worldSize.x = ws;
		worldSize.y = ws;
		
		WIDTH = w;
		HEIGHT = h;
		
		gameInstance = new Game(w, h, worldSize);
		
		gui = new JPanel();
		gui.add(new JLabel("asdf"));
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					menu();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}
			}
		});
	}
	
	private void menu() {
		panel = new JPanel();
		JButton start = new JButton("startGame");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				runGame();
			}
		});
		start.setPreferredSize(new Dimension(100,50));
		panel.add(start);

		
		
		JButton exit = new JButton("exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});

		exit.setPreferredSize(new Dimension(100,50));
		panel.add(exit);
		
		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocusInWindow();
		frame.setPreferredSize(new Dimension(WIDTH,HEIGHT));
	}
	
	private void runGame() {
		System.err.println("Starting Game");
		
		gamepanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				gameInstance.drawGame(g);
			}
		};
		gamepanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
	        }
		});
		
		
		JButton makeRoad = new JButton("Make Road");
		makeRoad.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.ROAD);
		});
		JButton makeWall = new JButton("Make Wall");
		makeWall.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.WALL);
		});
		JButton buildMine = new JButton("Build Mine");
		buildMine.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.MINE);
		});
		JButton buildBarracks = new JButton("Build Barracks");
		buildBarracks.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.BARRACKS);
		});
		JButton buildIrrigation = new JButton("Irrigate");
		buildIrrigation.addActionListener(e -> {
			gameInstance.setBuildMode(BuildMode.IRRIGATE);
		});
		JLabel money = new JLabel(); 
		money.setText("Gold = "+gameInstance.getMoney());
		JLabel stone = new JLabel(); 
		stone.setText("Stone = "+gameInstance.getMoney());
		
		JLabel tSize = new JLabel(); 
		stone.setText("TileSize = "+gameInstance.getTileSize());
		
		makeRoad.setFocusable(false);
		makeWall.setFocusable(false);
		buildMine.setFocusable(false);
		buildBarracks.setFocusable(false);
		buildIrrigation.setFocusable(false);
		
		JToggleButton showHeightMap = new JToggleButton("Show Height Map");
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameInstance.setShowHeightMap(showHeightMap.isSelected());
		});
		
		gui.setPreferredSize(new Dimension(400,1080));
		gui.add(money);
		gui.add(stone);
		gui.add(tSize);
		gui.add(makeRoad);
		gui.add(makeWall);
		gui.add(buildMine);
		gui.add(buildBarracks);
		gui.add(buildIrrigation);
		gui.add(showHeightMap);
		
		money.setFont(new Font("Verdana",1,20));
		money.setHorizontalAlignment(JLabel.CENTER);
		stone.setHorizontalAlignment(JLabel.CENTER);
		
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});
		exit.setPreferredSize(new Dimension(100,50));
		gamepanel.add(exit);
		
		frame.add(gamepanel,BorderLayout.CENTER);
		frame.add(gui,BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocusInWindow();
		gamepanel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				//+1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(),mx,my);
			}
		});
		gamepanel.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				mx = e.getX();
				my = e.getY();
				gameInstance.mouseOver(mx, my);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
//				System.out.println("mouse drag");
				dragged = true;
				if(SwingUtilities.isRightMouseButton(e)) {
					int mrx = e.getX();
					int mry = e.getY();
					int dx = mx-mrx;
					int dy = my-mry;
					gameInstance.shiftView(dx, dy);
					mx = mrx;
					my = mry;
					
					gameInstance.mouseOver(mx, my);
				}else if(SwingUtilities.isLeftMouseButton(e)) {
					int mx2 = e.getX();
					int my2 = e.getY();
					
					gameInstance.mouseOver(mx2, my2);
					gameInstance.selectBox(mx,my, mx2, my2);
					
				}
					
				
				
			}
		});
		
		
		gamepanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
//				System.out.println("mouse release");
				if(e.getButton()== MouseEvent.BUTTON1 && dragged ==false) {
					
					System.out.println("click");
					gameInstance.mouseClick(mx, my);
					
				}
				dragged = false;
				
				gameInstance.resetHoveredArea();
			}

			@Override
			public void mousePressed(MouseEvent e) {
//				System.out.println("mousepressed");
				mx = e.getX();
				my = e.getY();
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		gamepanel.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				//esc closes game
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
					exitGame();
				}
				//r rotates
				if (e.getKeyCode()==KeyEvent.VK_R) {
					gameInstance.rotateBlock();
				}
				
				
			}
		});
	
		timmy = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.updateGame();
				gamepanel.repaint();
			
				gui.repaint();
			}
		});
		timmy.start();

		frame.remove(panel);
		frame.add(gamepanel, BorderLayout.CENTER); 
		gamepanel.setSize(frame.getSize());
		frame.repaint();
		System.err.println(gamepanel.getWidth());
		gamepanel.requestFocus();
		
		
		gameLoopThread = new Thread(() -> {
			try {
				while(true) {
					gameInstance.gameTick();
					Thread.sleep(100);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		gameLoopThread.start();
	}
	
	

	
	
	public void exitGame() {
		System.exit(0);
	}
}
