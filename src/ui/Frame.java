package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.event.*;
import javax.swing.Timer;

import game.*;
import ui.infopanels.*;
import utils.*;
import world.*;

public class Frame extends JPanel {
	public static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
	public static final int GUIWIDTH = 350;
	public static final int MINIMAPBORDERWIDTH = 40;
	
	public static final int MILLISECONDS_PER_TICK = 100;
	private static final String TITLE = "civilicraftfortressII";

	public static final Dimension BUILDING_BUTTON_SIZE = new Dimension(150, 35);
	public static final Dimension DEBUG_BUTTON_SIZE = new Dimension(140, 30);
	public static final Dimension LONG_BUTTON_SIZE = new Dimension(285, 30);
	public static final Dimension SPAWN_BUTTON_SIZE = new Dimension(30, 30);
	public static final Dimension BUILD_UNIT_BUTTON_SIZE = new Dimension(170, 35);

	private static final int TAB_ICON_SIZE = 25;
	private ImageIcon WORKER_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/building.PNG"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon MAKE_UNIT_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/barracks.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon HELLFORGE_TAB_ICON = Utils.resizeImageIcon(Game.buildingTypeMap.get("HELLFORGE").getImageIcon(0), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon BLACKSMITH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/crafting.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon RESEARCH_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/tech.PNG"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon SHADOW_WORD_DEATH = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/soyouhavechosendeath.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon SPAWN_TAB_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/spawn_tab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE);

	private ImageIcon COLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/collapsed.PNG"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon UNCOLLAPSED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/uncollapsed.PNG"), TAB_ICON_SIZE, TAB_ICON_SIZE);
	private ImageIcon FAST_FORWARD_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/fastforward.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon RAIN_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/rain.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon ERUPTION_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/erupt.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon CHANGE_FACTION_ICON = Utils.resizeImageIcon(Game.unitTypeMap.get("CYCLOPS").getImageIcon(DEBUG_BUTTON_SIZE.height-5), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon NIGHT_DISABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_disabled.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon NIGHT_ENABLED_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/night_enabled.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);
	private ImageIcon METEOR_ICON = Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/meteor.PNG"), DEBUG_BUTTON_SIZE.height-5, DEBUG_BUTTON_SIZE.height-5);

	private Timer repaintingThread;
	private JToggleButton easyModeButton;
	private JFrame frame;
	private JPanel mainMenuPanel;
	private volatile boolean readyToStart;
	private JPanel gamepanel;
	private JPanel selectedUnitsPanel;
	private JPanel resourcePanel2;
	private JPanel minimapPanel;
	private JPanel infoPanel;
	private LinkedList<JPanel> infoPanelStack = new LinkedList<>();
	private JPanel makeUnitView;
	private JPanel blacksmithView;
	private JPanel hellforgeView;
//	private JPanel barracksView;
//	private JPanel workshopView;
	private JPanel tileView;
	private JPanel workerMenu;
	private JPanel spawnMenu;
	private JPanel researchView;
	private JPanel statView;
	private JLabel tileSize;
	private JTabbedPane tabbedPane;
	private JPanel guiSplitter;
	private JComboBox<MapType> mapType;
	private JPanel resourcePanel;
	private JButton[] resourceIndicators = new JButton[ItemType.values().length];
	private boolean[] resourceIndicatorsAdded = new boolean[ItemType.values().length];
	private HashMap<JButton, ResearchType> researchButtons = new HashMap<>();
	private JButton[] buildingButtons;
	private class Pair {
		public final JButton button;
		public final UnitType unitType;
		public Pair(JButton button, UnitType unitType) {
			this.button = button;
			this.unitType = unitType;
		}
	}
	private LinkedList<Pair> unitButtons = new LinkedList<>();
	private JButton[] craftButtons = new JButton[ItemType.values().length];
	private JButton[] statButtons = new JButton[7];
	
	private HashMap<Thing, JButton> selectedButtons = new HashMap<>();
	
	private JTextField mapSize;
	private int WIDTH;
	private int HEIGHT;
	private Game gameInstance;
	private int mx;
	private int my;
	private boolean dragged = false;

	private int WORKER_TAB;
	private int RESEARCH_TAB;
	private int BLACKSMITH_TAB;
	private int HELLFORGE_TAB;
	private int DEBUG_TAB;
//	private int BARRACKS_TAB;
//	private int WORKSHOP_TAB;
	private int MAKE_UNIT_TAB;
	private int STAT_TAB;
	private int SPAWN_TAB;

	private Thread gameLoopThread;
	private Thread terrainImageThread;
	
	private Semaphore gameUIReady = new Semaphore(0);

	public Frame() {

		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Utils.loadImage("resources/Images/logo.png"));

		HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
		HEIGHT = Math.min(HEIGHT, 1080);
		HEIGHT = HEIGHT * 8 / 9;
		WIDTH = HEIGHT + GUIWIDTH;
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		GUIController guiController = new GUIController() {
			@Override
			public void selectedBuilding(Building building, boolean selected) {
				if (building.getType() == Game.buildingTypeMap.get("BARRACKS")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("CASTLE")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("BLACKSMITH")) {
					manageBlacksmithTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("HELLFORGE")) {
					manageHellforgeTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("WORKSHOP")) {
					manageMakeUnitTab(selected);
				}
				if (building.getType() == Game.buildingTypeMap.get("RESEARCH_LAB")) {
					manageResearchLabTab(selected);
				}
				InfoPanel infoPanel = new BuildingInfoPanel(building);
				switchInfoPanel(infoPanel);
				SwingUtilities.invokeLater(() -> {
					infoPanel.addButton("Explode").addActionListener(e -> gameInstance.explode(building));
				});
				frame.repaint();
			}

			@Override
			public void selectedUnit(Unit unit, boolean selected) {
				if(selected) {
					if(!selectedButtons.containsKey(unit)) {
						JButton button = setupUnitButton(unit);
						button.addActionListener(e -> {
							if(gameInstance.isControlDown()) {
								gameInstance.deselectOneThing(unit);
							}
							else {
								gameInstance.deselectOtherThings(unit);
							}
						});
						button.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseEntered(MouseEvent e) {
								pushInfoPanel(new UnitInfoPanel(unit));
							}
							@Override
							public void mouseExited(MouseEvent e) {
								popInfoPanel();
							}
						});
						selectedButtons.put(unit, button);
						selectedUnitsPanel.add(button);
						gamepanel.revalidate();
					}
					UnitInfoPanel infoPanel = new UnitInfoPanel(unit);
					switchInfoPanel(infoPanel);
					SwingUtilities.invokeLater(() -> {
						infoPanel.addButton("Explode").addActionListener(e -> gameInstance.explode(unit));
						infoPanel.addButton("RoadEverything").addActionListener(e -> gameInstance.workerRoad(Game.buildingTypeMap.get("STONE_ROAD")));
						infoPanel.addButton("AutoBuild").addActionListener(e -> gameInstance.toggleAutoBuild());
						infoPanel.addButton("SetHarvesting").addActionListener(e -> gameInstance.setHarvesting());
						infoPanel.addButton("Guard").addActionListener(e -> gameInstance.toggleGuarding());
					});
				}
				else {
					if(selectedButtons.containsKey(unit)) {
						JButton button = selectedButtons.remove(unit);
						selectedUnitsPanel.remove(button);
						gamepanel.revalidate();
					}
				}
				if(unit.getType().isBuilder()) {
					manageBuildingTab(selected);
				}
				frame.repaint();
			}

			@Override
			public void selectedSpawnUnit(boolean selected) {
				manageSpawnTab(selected);
				frame.repaint();
			}

			@Override
			public void toggleTileView() {
				tileView.setVisible(!tileView.isVisible());
			}

			@Override
			public void updateGUI() {
				for (int i = 0; i < ItemType.values().length; i++) {
					int amount = World.PLAYER_FACTION.getItemAmount(ItemType.values()[i]);
					resourceIndicators[i].setText("" + amount);
					if(amount > 0 && !resourceIndicatorsAdded[i]) {
						resourceIndicatorsAdded[i] = true;
						resourcePanel2.add(resourceIndicators[i]);
						gamepanel.revalidate();
					}
				}
				boolean hasResearchLab = World.PLAYER_FACTION.hasResearchLab(gameInstance.world);
				for(Entry<JButton, ResearchType> entry : researchButtons.entrySet()) {
					JButton button = entry.getKey();
					Research research = World.PLAYER_FACTION.getResearch(entry.getValue());
					ResearchRequirement req = research.getRequirement();
					if (research.isUnlocked()) {
						button.setEnabled(false);
						button.setVisible(true);
					} else if(research.getTier() > 1 && !hasResearchLab) {
						button.setEnabled(false);
						button.setVisible(false);
					} else if (req.areRequirementsMet()) {
						button.setEnabled(true);
						button.setVisible(true);
					} else if (req.areSecondLayerRequirementsMet()) {
						button.setEnabled(false);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				for (int i = 0; i < Game.buildingTypeList.size(); i++) {
					BuildingType type = Game.buildingTypeList.get(i);
					JButton button = buildingButtons[i];
					if (World.PLAYER_FACTION.areRequirementsMet(type)) {
						button.setEnabled(true);
//						button.setVisible(true);
					} else {
						button.setEnabled(false);
//						button.setVisible(false);
					}
				}
				boolean castleSelected = World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("CASTLE"));
				boolean barracksSelected = World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("BARRACKS"));
				boolean workshopSelected = World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("WORKSHOP"));
				
				for(Pair pair : unitButtons) {
					if((castleSelected && Game.buildingTypeMap.get("CASTLE").unitsCanBuildSet().contains(pair.unitType)) 
							|| (barracksSelected && Game.buildingTypeMap.get("BARRACKS").unitsCanBuildSet().contains(pair.unitType))
							|| (workshopSelected && Game.buildingTypeMap.get("WORKSHOP").unitsCanBuildSet().contains(pair.unitType))) {
						if (World.PLAYER_FACTION.areRequirementsMet(pair.unitType)) {
							pair.button.setEnabled(true);
							pair.button.setVisible(true);
							continue;
						}
					}
					pair.button.setEnabled(false);
					pair.button.setVisible(false);
				}
				for (int i = 0; i < ItemType.values().length; i++) {
					ItemType type = ItemType.values()[i];
					if (type.getCost() == null) {
						continue;
					}
					JButton button = craftButtons[i];
					if (World.PLAYER_FACTION.areRequirementsMet(type)) {
						button.setEnabled(true);
						button.setVisible(true);
					} else {
						button.setEnabled(false);
						button.setVisible(false);
					}
				}
				frame.repaint();
			}

		};
		gameInstance = new Game(guiController);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					menu();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}
			}
		});
	}
	
	private JButton setupUnitButton(Unit unit) {
		KButton button = new KButton(null, Utils.resizeImageIcon(unit.getImageIcon(10), 30, 30)) {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Game.drawHealthBar2(g, unit, 0, getHeight() - 6, getWidth(), 6, 1, unit.getHealth()/unit.getMaxHealth());
			}
		};
		button.setMargin(KUIConstants.zeroMargin);
		button.setHorizontalAlignment(SwingConstants.CENTER);
		KUIConstants.setComponentAttributes(button, null);
		button.addActionListener(e -> {
			if(gameInstance.isControlDown()) {
				gameInstance.deselectOneThing(unit);
			}
			else {
				gameInstance.deselectOtherThings(unit);
			}
		});
		return button;
	}
	
	private void menu() {
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		if(Driver.SHOW_MENU_ANIMATION) {
			mainMenuPanel = new MainMenuBackground();
		}
		else {
			mainMenuPanel = new JPanel();
		}
		
		easyModeButton = KUIConstants.setupToggleButton("Enable Easy Mode", null, BUILDING_BUTTON_SIZE);
		easyModeButton.addActionListener(e -> {
			easyModeButton.setText(easyModeButton.isSelected() ? "Disable Easy Mode" : "Enable Easy Mode");
		});
		
		JButton start = KUIConstants.setupButton("Start Game", null, BUILDING_BUTTON_SIZE);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(readyToStart) {
					start.setEnabled(false);
					readyToStart = false;
					repaint();
					Thread thread = new Thread(() -> {
						runGame();
					});
					thread.start();
				}
			}
		});
		start.setEnabled(false);
		mainMenuPanel.add(start);

		mapType = new JComboBox<>(MapType.values());
		KUIConstants.setComponentAttributes(mapType, BUILDING_BUTTON_SIZE);
		mainMenuPanel.add(mapType);

		mapSize = new JTextField("128", 10);
		KUIConstants.setComponentAttributes(mapSize, BUILDING_BUTTON_SIZE);
		mapSize.setFocusable(true);
		mainMenuPanel.add(mapSize);
		mainMenuPanel.add(easyModeButton);
		
		frame.add(mainMenuPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.requestFocusInWindow();
		if(mainMenuPanel instanceof MainMenuBackground) {
			((MainMenuBackground)mainMenuPanel).start(() -> {
				readyToStart = true;
				start.setEnabled(true);
			});
		}
		else {
			readyToStart = true;
			start.setEnabled(true);
		}
	}

	private void setupGamePanel() {
		gamepanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.setColor(gameInstance.getBackgroundColor());
				g.fillRect(0, 0, getWidth(), getHeight());
				gameInstance.drawGame(g);
				g.setColor(Color.black);
				g.drawRect(-1, 0, getWidth() + 1, getHeight());
			}
		};
		resourcePanel2 = new JPanel();
		resourcePanel2.setLayout(new BoxLayout(resourcePanel2, BoxLayout.Y_AXIS));
		resourcePanel2.setOpaque(false);
		JPanel filler = new JPanel();
		filler.setLayout(new BorderLayout());
		filler.setOpaque(false);
		selectedUnitsPanel = new JPanel();
		selectedUnitsPanel.setOpaque(false);
		gamepanel.setLayout(new BorderLayout());
		gamepanel.add(resourcePanel2, BorderLayout.WEST);
		gamepanel.add(filler, BorderLayout.CENTER);
		filler.add(selectedUnitsPanel, BorderLayout.SOUTH);
		gamepanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
			}
		});
		gamepanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// +1 is in -1 is out
				gameInstance.zoomView(e.getWheelRotation(), mx, my);
				tileSize.setText("TileSize = " + gameInstance.getTileSize());
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
				if (SwingUtilities.isLeftMouseButton(e)) {
					int mrx = e.getX();
					int mry = e.getY();
					int dx = mx - mrx;
					int dy = my - mry;
					gameInstance.shiftView(dx, dy);
					mx = mrx;
					my = mry;

					gameInstance.mouseOver(mx, my);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					int mx2 = e.getX();
					int my2 = e.getY();
					gameInstance.mouseOver(mx2, my2);
				}
			}
		});

		gamepanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && dragged == false) {
					gameInstance.rightClick(mx, my);
				}
				if (e.getButton() == MouseEvent.BUTTON1 && dragged == false) {
					gameInstance.leftClick(mx, my);
				}
				dragged = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
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
				if (e.getClickCount() == 2) {
					gameInstance.doubleClick(mx, my);
				}
			}
		});

		gamepanel.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					gameInstance.shiftControl(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
					gameInstance.controlPressed(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					gameInstance.shiftControl(true);
				}
				else if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
					gameInstance.controlPressed(true);
				}
				if(e.getKeyCode() == KeyEvent.VK_A) {
					if(e.isControlDown()) {
						gameInstance.selectAllUnits();
					}
					else {
						gameInstance.aControl(true);
					}
					
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					gameInstance.deselectEverything();
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					gameInstance.unitStop();
				}
				if (e.getKeyCode() == KeyEvent.VK_R) {
//					if (gameInstance.getSelectedUnit() != null) {
//						gameInstance.buildRoad(RoadType.STONE_ROAD);
//					}
				}
				if (e.getKeyCode() == KeyEvent.VK_M) {
//					if (gameInstance.getSelectedUnit() != null) {
//						gameInstance.buildBuilding(BuildingType.MINE);
//					}
				}
				if (e.getKeyCode() == KeyEvent.VK_I) {
//					if (gameInstance.getSelectedUnit() != null) {
//						gameInstance.buildBuilding(BuildingType.IRRIGATION);
//					}
				}
			}
		});
	}

	/** 
	 * clears infoPanelStack
	 */
	private void switchInfoPanel(JPanel newInfo) {
		infoPanelStack.clear();
		pushInfoPanel(newInfo);
	}
	private void pushInfoPanel(JPanel newInfo) {
		infoPanelStack.addFirst(newInfo);
		setInfoPanel(newInfo);
	}
	private void popInfoPanel() {
		if(infoPanelStack.size() > 1) {
			infoPanelStack.removeFirst();
		}
		JPanel newInfo = infoPanelStack.getFirst();
		setInfoPanel(newInfo);
	}
	private void setInfoPanel(JPanel newInfo) {
		SwingUtilities.invokeLater(() -> {
			infoPanel.removeAll();
			newInfo.setOpaque(false);
			infoPanel.add(newInfo, BorderLayout.CENTER);
			infoPanel.validate();
		});
	}

	private void setupMinimapPanel() {
		minimapPanel = new JPanel() {
			private void drawSunMoon(Graphics g) {
				int padding = 5;
				g.setFont(KUIConstants.infoFontSmaller);
				String dayCounter = "Day: " + gameInstance.getDays() + "   Night: " + gameInstance.getNights();
				Color temp = g.getColor();
				g.setColor(Color.white);
				g.drawString(dayCounter, padding-1, g.getFont().getSize() + padding-1 );
				g.setColor(Color.black);
				g.drawString(dayCounter, padding, g.getFont().getSize() + padding );
				g.setColor(temp);
				
				int offset = gameInstance.world.getCurrentDayOffset() + World.TRANSITION_PERIOD;
				int pathwidth = getWidth() - Frame.MINIMAPBORDERWIDTH;
				int pathheight = getHeight() - Frame.MINIMAPBORDERWIDTH;
				int totallength = 2*pathwidth + 2*pathheight;
				offset = totallength*offset / (World.DAY_DURATION + World.NIGHT_DURATION);
				Image sun = gameInstance.getTimeImage(true);
				Image moon = gameInstance.getTimeImage(false);
				int imagesize = Frame.MINIMAPBORDERWIDTH - padding*2;
				if(offset < pathheight) {
					g.drawImage(sun, padding, padding + pathheight - offset, imagesize, imagesize, null);
					g.drawImage(moon, padding + pathwidth, padding + offset, imagesize, imagesize, null);
				}
				else {
					offset -= pathheight;
					if(offset < pathwidth) {
						g.drawImage(sun, padding + offset, padding, imagesize, imagesize, null);
						g.drawImage(moon, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize, null);
					}
					else {
						offset -= pathwidth;
						if(offset < pathheight) {
							g.drawImage(sun, padding + pathwidth, padding + offset, imagesize, imagesize, null);
							g.drawImage(moon, padding, padding + pathheight - offset, imagesize, imagesize, null);
						}
						else {
							offset -= pathheight;
							if(offset < pathwidth) {
								g.drawImage(sun, padding + pathwidth - offset, padding + pathheight, imagesize, imagesize, null);
								g.drawImage(moon, padding + offset, padding, imagesize, imagesize, null);
							}
							else {
								offset -= pathwidth;
								if(offset < pathheight) {
									g.drawImage(sun, padding, padding + pathheight - offset, imagesize, imagesize, null);
									g.drawImage(moon, padding + pathwidth, padding + offset, imagesize, imagesize, null);
								}
							}
						}
					}
				}
			}
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(gameInstance.getBackgroundColor());
				g.fillRect(0, 0, getWidth(), getHeight());
				drawSunMoon(g);
				g.fillRect(0, getHeight()*4/5, getWidth(), getHeight() - getHeight()*4/5);
				g.setColor(Color.black);
				g.drawRect(0, 0, getWidth(), getHeight());
				gameInstance.drawMinimap(g, MINIMAPBORDERWIDTH, MINIMAPBORDERWIDTH,
						minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH,
						minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
			}
		};
		minimapPanel.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
		minimapPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				double ratiox = ((double) e.getX() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getWidth() - 2 * MINIMAPBORDERWIDTH);
				double ratioy = ((double) e.getY() - MINIMAPBORDERWIDTH)
						/ (minimapPanel.getHeight() - 2 * MINIMAPBORDERWIDTH);
				gameInstance.moveViewTo(ratiox, ratioy);
				frame.repaint();
			}
		});
	}

	private void manageBuildingTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == WORKER_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(WORKER_TAB);
		}
		tabbedPane.setEnabledAt(WORKER_TAB, enabled);
	}
	
	private void manageMakeUnitTab(boolean enabled) {
		if(enabled) {
			tabbedPane.setEnabledAt(MAKE_UNIT_TAB, enabled);
			tabbedPane.setSelectedIndex(MAKE_UNIT_TAB);
		}
		else {
			if(!(World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("CASTLE"))
					|| World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("BARRACKS"))
					|| World.PLAYER_FACTION.isBuildingSelected(gameInstance.world, Game.buildingTypeMap.get("WORKSHOP")))) {
				if(tabbedPane.getSelectedIndex() == MAKE_UNIT_TAB) {
					tabbedPane.setSelectedIndex(0);
				}
				tabbedPane.setEnabledAt(MAKE_UNIT_TAB, false);
			}
		}
	}

	private void manageBlacksmithTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == BLACKSMITH_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(BLACKSMITH_TAB);
		}
		tabbedPane.setEnabledAt(BLACKSMITH_TAB, enabled);
	}
	private void manageHellforgeTab(boolean enabled) {
		if (enabled == false && tabbedPane.getSelectedIndex() == HELLFORGE_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(HELLFORGE_TAB);
		}
		tabbedPane.setEnabledAt(HELLFORGE_TAB, enabled);
	}
	
	private void manageResearchLabTab(boolean enabled) {
		if (enabled == true) {
			tabbedPane.setSelectedIndex(RESEARCH_TAB);
		}
	}

	private void manageSpawnTab(boolean enabled) {

		if (enabled == false && tabbedPane.getSelectedIndex() == SPAWN_TAB) {
			tabbedPane.setSelectedIndex(0);
		} else if (enabled == true) {
			tabbedPane.setSelectedIndex(SPAWN_TAB);
		}
		tabbedPane.setEnabledAt(SPAWN_TAB, enabled);
	}

	private void switchToGame() {
		frame.remove(mainMenuPanel);
		mainMenuPanel = null;
		
		frame.getContentPane().add(gamepanel, BorderLayout.CENTER);
		frame.getContentPane().add(guiSplitter, BorderLayout.EAST);
		frame.pack();
		
		gameInstance.setViewSize(gamepanel.getWidth(), gamepanel.getHeight());
		gameInstance.centerViewOn(gameInstance.world.buildings.getLast().getTile(), 50);
		gamepanel.requestFocusInWindow();
		gamepanel.requestFocus();
		frame.repaint();
		gameLoopThread.start();
		repaintingThread.start();
		terrainImageThread.start();
	}
	private void runGame() {
		System.err.println("Starting Game");
		Runnable menuAnimationStopListener = new Runnable() {
			@Override
			public void run() {
				try {
					gameUIReady.acquire();
					SwingUtilities.invokeLater(() -> {
						switchToGame();
					});
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		};
		if(mainMenuPanel instanceof MainMenuBackground) {
			((MainMenuBackground)mainMenuPanel).stop(menuAnimationStopListener);
		}
		else {
			Thread thread = new Thread(menuAnimationStopListener);
			thread.start();
		}
		
		
		int size = Integer.parseInt(mapSize.getText());
		gameInstance.generateWorld((MapType) mapType.getSelectedItem(), size, easyModeButton.isSelected());

		Dimension RESOURCE_BUTTON_SIZE = new Dimension(80, 30);
		Dimension RESEARCH_BUTTON_SIZE = new Dimension(125, 35);
		int BUILDING_ICON_SIZE = 25;
		int RESOURCE_ICON_SIZE = 25;

		
		workerMenu = new JPanel();
		workerMenu.setLayout(new GridBagLayout());
		buildingButtons = new JButton[Game.buildingTypeList.size()];
		
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.addActionListener(e -> {
				gameInstance.setBuildingToPlan(type);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new BuildingTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			buildingButtons[i] = button;
		}
		
		KToggleButton toggleButton = KUIConstants.setupToggleButton("Walls/Gates", UNCOLLAPSED_ICON, new Dimension(BUILDING_BUTTON_SIZE.width*2, BUILD_UNIT_BUTTON_SIZE.height));
		toggleButton.setSelected(true);
		toggleButton.addActionListener(e -> {
			for (int i = 0; i < Game.buildingTypeList.size(); i++) {
				if(Game.buildingTypeList.get(i).blocksMovement()) {
					buildingButtons[i].setVisible(toggleButton.isSelected());
				}
			}
			toggleButton.setIcon(toggleButton.isSelected() ? UNCOLLAPSED_ICON : COLLAPSED_ICON);
		});
		KToggleButton toggleButton2 = KUIConstants.setupToggleButton("Other", UNCOLLAPSED_ICON, new Dimension(BUILDING_BUTTON_SIZE.width*2, BUILD_UNIT_BUTTON_SIZE.height));
		toggleButton2.setSelected(true);
		toggleButton2.addActionListener(e -> {
			for (int i = 0; i < Game.buildingTypeList.size(); i++) {
				if(!(Game.buildingTypeList.get(i).blocksMovement())) {
					buildingButtons[i].setVisible(toggleButton2.isSelected());
				}
			}
			toggleButton2.setIcon(toggleButton2.isSelected() ? UNCOLLAPSED_ICON : COLLAPSED_ICON);
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		workerMenu.add(toggleButton, c);
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 4; c.weightx = 1; c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		workerMenu.add(toggleButton2, c);
		int index1 = 0;
		int index2 = 0;
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			if(Game.buildingTypeList.get(i).isGate() || Game.buildingTypeList.get(i).blocksMovement()) {
				c = new GridBagConstraints();
				c.gridx = index1/3;
				c.gridy = index1%3 + 1;
				c.gridwidth = 1;
				c.weightx = 0.5;
				c.fill = GridBagConstraints.HORIZONTAL;
				workerMenu.add(buildingButtons[i], c);
				index1++;
			}
			else {
				c = new GridBagConstraints();
				c.gridx = index2%2;
				c.gridy = index2/2 + 5;
				c.gridwidth = 1;
				c.weightx = 0.5;
				c.fill = GridBagConstraints.HORIZONTAL;
				workerMenu.add(buildingButtons[i], c);
				index2++;
			}
		}
		c = new GridBagConstraints();
		c.gridy = Game.buildingTypeList.size()/2 + 3; 
		c.gridx = 0; c.gridwidth = 2; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		JPanel p = new JPanel();
		p.setOpaque(false);
		workerMenu.add(p, c);
		
		spawnMenu = new JPanel();
		for (int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gameInstance.setThingToSpawn(type, null);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new UnitTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			spawnMenu.add(button);
		}
		for (int i = 0; i < Game.buildingTypeList.size(); i++) {
			BuildingType type = Game.buildingTypeList.get(i);
			KButton button = KUIConstants.setupButton(null,
					Utils.resizeImageIcon(type.getImageIcon(0), (int)(SPAWN_BUTTON_SIZE.width), (int)(SPAWN_BUTTON_SIZE.height)),
					SPAWN_BUTTON_SIZE);
			button.setBorder(KUIConstants.tinyBorder);
			button.addActionListener(e -> {
				gameInstance.setThingToSpawn(null, type);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new BuildingTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new BuildingTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
//			buildingButtons[i] = button;
			spawnMenu.add(button);
		}
		JToggleButton toggle = KUIConstants.setupToggleButton("Non-playerControlled", null, DEBUG_BUTTON_SIZE);
		toggle.addActionListener(e -> {
			toggle.setText(!toggle.isSelected() ? "Non-playerControlled" : "playerControlled");
			gameInstance.setSummonPlayerControlled(!toggle.isSelected());
		});
		spawnMenu.add(toggle);
		
		
		makeUnitView = new JPanel();
		Pair[] buttons = populateUnitTypeUI(makeUnitView, BUILDING_ICON_SIZE);
		Collections.addAll(unitButtons, buttons);
		
//		workshopView = new JPanel();
//		buttons = populateUnitTypeUI(workshopView, BUILDING_ICON_SIZE);
//		Collections.addAll(unitButtons, buttons);
		
		blacksmithView = new JPanel();
		BuildingType blacksmithType = Game.buildingTypeMap.get("BLACKSMITH");
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (type.getCost() == null) {
				continue;
			}
			if(Game.buildingTypeMap.get(type.getBuilding()) != blacksmithType) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				int amount = 1;
				if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					amount = 10;
				}
				gameInstance.craftItem(type, amount);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new ItemTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			craftButtons[i] = button;
			blacksmithView.add(button);
		}
		
		hellforgeView = new JPanel();
		BuildingType hellforgeType = Game.buildingTypeMap.get("HELLFORGE");
		for (int i = 0; i < ItemType.values().length; i++) {
			final ItemType type = ItemType.values()[i];
			if (type.getCost() == null) {
				continue;
			}
			if(Game.buildingTypeMap.get(type.getBuilding()) != hellforgeType) {
				continue;
			}
			KButton button = KUIConstants.setupButton(type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE),
					BUILDING_BUTTON_SIZE);
			button.setEnabled(false);
			button.addActionListener(e -> {
				int amount = 1;
				if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					amount = 10;
				}
				gameInstance.craftItem(type, amount);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new ItemTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});

			craftButtons[i] = button;
			hellforgeView.add(button);
		}

//		barracksView = new JPanel();
//		buttons = populateUnitTypeUI(barracksView, BUILDING_ICON_SIZE);
//		Collections.addAll(unitButtons, buttons);

		for (int i = 0; i < ItemType.values().length; i++) {
			ItemType type = ItemType.values()[i];
			KButton button = KUIConstants.setupButton("",
					Utils.resizeImageIcon(type.getImageIcon(0), RESOURCE_ICON_SIZE, RESOURCE_ICON_SIZE),
					RESOURCE_BUTTON_SIZE);
			button.setEnabled(false);
			if(type.getCost() != null) {
				button.addActionListener(e -> {
					int amount = 1;
					if((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
						amount = 10;
					}
					gameInstance.craftItem(type, amount);
				});
			}
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ItemTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new ItemTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			resourceIndicators[i] = button;
		}

		JPanel buttonPanel = new JPanel();
		tileSize = KUIConstants.setupLabel("TileSize = " + gameInstance.getTileSize(), null, DEBUG_BUTTON_SIZE);

		JToggleButton showHeightMap = KUIConstants.setupToggleButton("Show Height Map", null, DEBUG_BUTTON_SIZE);
		showHeightMap.addActionListener(e -> {
			showHeightMap.setText(showHeightMap.isSelected() ? "Hide Height Map" : "Show Height Map");
			gameInstance.setShowHeightMap(showHeightMap.isSelected());
		});

		JToggleButton flipTable = KUIConstants.setupToggleButton("Flip Table", null, DEBUG_BUTTON_SIZE);
		flipTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.flipTable();
				flipTable.setText(flipTable.isSelected() ? "Unflip Table" : "Flip Table");
			}
		});

		JToggleButton spawnUnit = KUIConstants.setupToggleButton("Enable Spawn Unit", null, DEBUG_BUTTON_SIZE);
		spawnUnit.addActionListener(e -> {
			spawnUnit.setText(spawnUnit.isSelected() ? "Disable Spawn Unit" : "Enable Spawn Unit");
			gameInstance.spawnUnit(spawnUnit.isSelected());
		});

		JButton eruptVolcano = KUIConstants.setupButton("Erupt Volcano", ERUPTION_ICON, DEBUG_BUTTON_SIZE);
		eruptVolcano.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.eruptVolcano();
			}
		});
		
		JButton addResources = KUIConstants.setupButton("Give Resources", null, DEBUG_BUTTON_SIZE);
		addResources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.addResources();
			}
		});

		JButton makeItRain = KUIConstants.setupButton("Rain", RAIN_ICON, DEBUG_BUTTON_SIZE);
		makeItRain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.rain();
				gameInstance.world.grow();
			
			}
		});

		JButton makeItDry = KUIConstants.setupButton("Drought", null, DEBUG_BUTTON_SIZE);
		makeItDry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.world.drought();
			}
		});

		JToggleButton fastForward = KUIConstants.setupToggleButton("Fast Forward", FAST_FORWARD_ICON, DEBUG_BUTTON_SIZE);
		fastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.toggleFastForward(fastForward.isSelected());
				fastForward.setText(fastForward.isSelected() ? "Stop Fast Forward" : "Fast Forward");
			}
		});

		JButton researchEverything = KUIConstants.setupButton("Research All", RESEARCH_TAB_ICON, DEBUG_BUTTON_SIZE);
		researchEverything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.researchEverything();
				buttonPanel.remove(researchEverything);
			}
		});
		JButton shadowWordDeath = KUIConstants.setupButton("Shadow Word: Death", SHADOW_WORD_DEATH, LONG_BUTTON_SIZE);
		shadowWordDeath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.shadowWordDeath(100);
			}
		});

		JButton meteor = KUIConstants.setupButton("Meteor", METEOR_ICON, DEBUG_BUTTON_SIZE);
		meteor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.meteorStrike();
			}
		});
		JButton unitEvents = KUIConstants.setupButton("Unit Events", null, DEBUG_BUTTON_SIZE);
		unitEvents.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameInstance.shadowWordDeath(1);
			}
		});
		JButton setPlayerFaction = KUIConstants.setupButton("Change Faction", CHANGE_FACTION_ICON, DEBUG_BUTTON_SIZE);
		setPlayerFaction.addActionListener(e -> {
			int choice = JOptionPane.showOptionDialog(null, "Choose faction", "Choose faction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, World.factions, World.NO_FACTION);
			if(choice >= 0 && choice < World.factions.length) {
				World.PLAYER_FACTION = World.factions[choice];
			}
		});

		JToggleButton debug = KUIConstants.setupToggleButton(Game.DEBUG_DRAW ? "Leave Matrix" : "Matrix", null, DEBUG_BUTTON_SIZE);
		debug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Game.DEBUG_DRAW = debug.isSelected();
				debug.setText(Game.DEBUG_DRAW ? "Leave Matrix" : "Matrix");
			}
		});
		JToggleButton toggleNight = KUIConstants.setupToggleButton(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled", NIGHT_ENABLED_ICON,
				DEBUG_BUTTON_SIZE);
		toggleNight.setSelected(true);
		toggleNight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Game.DISABLE_NIGHT = !toggleNight.isSelected();
				toggleNight.setText(Game.DISABLE_NIGHT ? "Night Disabled" : "Night Enabled");
				toggleNight.setIcon(Game.DISABLE_NIGHT ? NIGHT_DISABLED_ICON : NIGHT_ENABLED_ICON);
			}
		});

		JButton exit = KUIConstants.setupButton("Exit", null, DEBUG_BUTTON_SIZE);
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exitGame();
			}
		});

		resourcePanel = new JPanel();
		int RESOURCE_PANEL_WIDTH = 100;
		resourcePanel.setPreferredSize(new Dimension(RESOURCE_PANEL_WIDTH, 1000));
//		resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.Y_AXIS));
		buttonPanel.setPreferredSize(new Dimension(GUIWIDTH - RESOURCE_PANEL_WIDTH, 1000));
		buttonPanel.add(tileSize);

		buttonPanel.add(showHeightMap);
		buttonPanel.add(flipTable);
		buttonPanel.add(spawnUnit);
		buttonPanel.add(makeItRain);
		buttonPanel.add(makeItDry);
		buttonPanel.add(fastForward);
		buttonPanel.add(eruptVolcano);
		buttonPanel.add(meteor);
		buttonPanel.add(unitEvents);
		buttonPanel.add(debug);
		buttonPanel.add(toggleNight);
		buttonPanel.add(addResources);
		buttonPanel.add(setPlayerFaction);
		buttonPanel.add(researchEverything);
		buttonPanel.add(exit);
		buttonPanel.add(shadowWordDeath);

		researchView = new JPanel();
		for (int i = 0; i < Game.researchTypeList.size(); i++) {
			ResearchType researchType = Game.researchTypeList.get(i);
			KButton button = KUIConstants.setupButton(researchType.toString(),
					Utils.resizeImageIcon(researchType.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.setEnabled(false);
			button.addActionListener(e -> {
				gameInstance.setResearchTarget(researchType);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new ResearchInfoPanel(World.PLAYER_FACTION.getResearch(researchType)));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new ResearchInfoPanel(World.PLAYER_FACTION.getResearch(researchType)));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			researchButtons.put(button, researchType);
			researchView.add(button);
		}
		statView = new JPanel();
		for (int i = 0; i < gameInstance.getCombatBuffs().getStats().size(); i++) {
			CombatStats combatBuffs = gameInstance.getCombatBuffs();
			LinkedList<String> strings = combatBuffs.getStrings();
			int f = i;
			
			KButton button = KUIConstants.setupButton(strings.get(i) + ": " + combatBuffs.getStat(strings.get(i)), null, RESEARCH_BUTTON_SIZE);
			button.setEnabled(true);
			button.addActionListener(e -> {
				button.setText(strings.get(f) + ": " + combatBuffs.getStat(strings.get(f)));
				CombatStats cs = new CombatStats(0,0,0,0,0,0,0);
				cs.getStats().set(f, cs.getStats().get(f)+1);
				cs.add(strings.get(f), cs.getStats().get(f) );
				gameInstance.addCombatBuff(cs);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new CombatStatInfoPanel(combatBuffs));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new CombatStatInfoPanel(combatBuffs));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			statButtons[i] = button;
			statView.add(statButtons[i]);
		}
		
		setupGamePanel();
		setupMinimapPanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setFont(KUIConstants.buttonFontSmall);

		RESEARCH_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null, RESEARCH_TAB_ICON, researchView, "Research new technologies");
		
		WORKER_TAB = tabbedPane.getTabCount();
		JScrollPane scrollPane = new JScrollPane(workerMenu, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		workerMenu.setPreferredSize(new Dimension(GUIWIDTH, BUILDING_BUTTON_SIZE.height * (Game.buildingTypeList.size()/2 + 4)));
		tabbedPane.insertTab(null, WORKER_TAB_ICON, scrollPane, "Build buildings with workers", WORKER_TAB);

		MAKE_UNIT_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, MAKE_UNIT_TAB_ICON, makeUnitView, "Make units from castles, barracks, or workshops", MAKE_UNIT_TAB);

		BLACKSMITH_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, BLACKSMITH_TAB_ICON, blacksmithView, "Craft items up to mithril", BLACKSMITH_TAB);

		HELLFORGE_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null,HELLFORGE_TAB_ICON, hellforgeView, "Craft items adamantite and above", HELLFORGE_TAB);
		
		SPAWN_TAB = tabbedPane.getTabCount();
		tabbedPane.insertTab(null, SPAWN_TAB_ICON, spawnMenu, "Summon units for testing", SPAWN_TAB);

		DEBUG_TAB = tabbedPane.getTabCount();
		tabbedPane.addTab(null,
				Utils.resizeImageIcon(Utils.loadImageIcon("resources/Images/interfaces/debugtab.png"), TAB_ICON_SIZE, TAB_ICON_SIZE),
				buttonPanel, "Various testing functions");

		// disable building tab after setting all of the tabs up
		manageBuildingTab(false);
		manageBlacksmithTab(false);
		manageHellforgeTab(false);
		manageResearchLabTab(false);
		manageMakeUnitTab(false);
		manageSpawnTab(true);
		

		guiSplitter = new JPanel();
		guiSplitter.setLayout(new BorderLayout());
		guiSplitter.setPreferredSize(new Dimension(GUIWIDTH, frame.getHeight()));
		guiSplitter.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
//		guiSplitter.add(resourcePanel,BorderLayout.WEST);

		minimapPanel.setPreferredSize(new Dimension(GUIWIDTH, GUIWIDTH));
		guiSplitter.add(minimapPanel, BorderLayout.NORTH);

		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setBackground(gameInstance.getBackgroundColor());
		infoPanel.setPreferredSize(new Dimension(GUIWIDTH, (int) (GUIWIDTH / 2.5)));
		infoPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		guiSplitter.add(infoPanel, BorderLayout.SOUTH);
		
		
		

//		frame.remove(background);
//		frame.getContentPane().add(background, BorderLayout.CENTER);
//		frame.getContentPane().add(guiSplitter, BorderLayout.EAST);
//		frame.pack();
		
//		frame.setVisible(true);

		repaintingThread = new Timer(30, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.repaint();
			}
		});
		terrainImageThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.updateTerrainImages();
					long elapsed = System.currentTimeMillis() - start;
					long sleeptime = MILLISECONDS_PER_TICK - elapsed;
					if(sleeptime > 0) {
						Thread.sleep(sleeptime);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					if(e1 instanceof InterruptedException) {
						break;
					}
				}
			}
		});

		frame.repaint();

		gameLoopThread = new Thread(() -> {
			while (true) {
				try {
					long start = System.currentTimeMillis();
					gameInstance.gameTick();
					long elapsed = System.currentTimeMillis() - start;
					if(Game.ticks % 10 == 0) {
						frame.setTitle(TITLE + " " + elapsed);
					}
					long sleeptime = MILLISECONDS_PER_TICK - elapsed;
					if(sleeptime > 0 && !gameInstance.shouldFastForward()) {
						Thread.sleep(sleeptime);
					}
				}
				catch(Exception e) {
					try (FileWriter fw = new FileWriter("ERROR_LOG.txt", true);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter out = new PrintWriter(bw)) {
						e.printStackTrace(out);
					} catch (IOException ee) {
					}
					e.printStackTrace();
					if(e instanceof InterruptedException) {
						break;
					}
				}
			}
		});
		gameUIReady.release();
	}
	
	public Pair[] populateUnitTypeUI(JPanel panel, int BUILDING_ICON_SIZE) {
		Pair[] pairs = new Pair[Game.unitTypeList.size()];
		for(int i = 0; i < Game.unitTypeList.size(); i++) {
			UnitType type = Game.unitTypeList.get(i);
			KButton button = KUIConstants.setupButton("Build " + type.toString(),
					Utils.resizeImageIcon(type.getImageIcon(0), BUILDING_ICON_SIZE, BUILDING_ICON_SIZE), null);
			button.addActionListener(e -> {
				gameInstance.tryToBuildUnit(type);
			});
			button.addRightClickActionListener(e -> {
				switchInfoPanel(new UnitTypeInfoPanel(type));
			});
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					pushInfoPanel(new UnitTypeInfoPanel(type));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					popInfoPanel();
				}
			});
			pairs[i] = new Pair(button, type);
			panel.add(button);
		}
		return pairs;
	}

	public void exitGame() {
		System.exit(0);
	}
}
