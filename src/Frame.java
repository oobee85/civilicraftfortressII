import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class Frame extends JPanel{
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	int GUIWIDTH = 400;
	
	private Timer timmy;
	public JPanel panel;
	public JFrame frame;
	public JPanel gamepanel;
	public JPanel minimapPanel;
	private int WIDTH;
	private int HEIGHT;
	public Game gameInstance;
	private Point worldSize = new Point();
	private int mx;
	private int my;
	public JPanel gui;
	private boolean dragged = false;
	
	private Thread gameLoopThread;
	
	public Frame(int ws) {
	
		frame = new JFrame("Civilization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height * 8/9;
		WIDTH = HEIGHT + GUIWIDTH;
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		
		worldSize.x = ws;
		worldSize.y = ws;
		
		gameInstance = new Game(WIDTH, HEIGHT, worldSize);
		
		
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
				g.setColor(BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				gameInstance.drawGame(g);
			}
		};
		gamepanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
	        }
		});
		int MINIMAPBORDERWIDTH = 50;
		minimapPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth(), getHeight());
				gameInstance.drawMinimap(g, MINIMAPBORDERWIDTH, MINIMAPBORDERWIDTH, minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH,  minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
			}
		};
		minimapPanel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double)e.getX() - MINIMAPBORDERWIDTH) / (minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH);
				double ratioy = ((double)e.getY() - MINIMAPBORDERWIDTH) / (minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
		minimapPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double)e.getX() - MINIMAPBORDERWIDTH) / (minimapPanel.getWidth() - 2*MINIMAPBORDERWIDTH);
				double ratioy = ((double)e.getY() - MINIMAPBORDERWIDTH) / (minimapPanel.getHeight() - 2*MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});

		gui = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth(), getHeight());
			}
		};
		gui.setPreferredSize(new Dimension(GUIWIDTH,frame.getHeight()));
		
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


		gui.add(money);
		gui.add(stone);
		gui.add(tSize);
		gui.add(makeRoad);
		gui.add(makeWall);
		gui.add(buildMine);
		gui.add(buildBarracks);
		gui.add(buildIrrigation);
		gui.add(showHeightMap);
		gui.add(exit);

		JPanel guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH,frame.getHeight()));
		guiSplitter.add(gui,BorderLayout.CENTER);
		
		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH,GUIWIDTH));
		guiSplitter.add(minimapPanel,BorderLayout.SOUTH);
		
		frame.add(gamepanel,BorderLayout.CENTER);
		frame.add(guiSplitter,BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocusInWindow();
		gamepanel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				//+1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(),mx,my);
				stone.setText("TileSize = " + gameInstance.getTileSize());
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
	
		timmy = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
		});
		timmy.start();

		frame.remove(panel);
		frame.add(gamepanel, BorderLayout.CENTER); 
		gamepanel.setSize(frame.getSize());
		frame.repaint();
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
