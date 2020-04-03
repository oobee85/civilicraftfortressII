import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Point;
import java.util.ArrayList;




public class Tile {
	private Structure structure;
	private boolean hasRoad;
	private boolean hasWall;
	private Unit unit;
	private Position p;
	private Terrain terr;
	int minEntitySize = 20;
	private int rotate;
	private String corner;
	private boolean isHighlight;
	private String roadCorner;
	private Ore ore;
	private Plant plant;
	
	public Tile(Unit u, Position point, Terrain t) {
		unit = u;
		p = point;
		terr = t;
		isHighlight = false;
		
	}
	public void buildRoad(boolean r) {
		if(hasRoad == r) {
			this.hasRoad = false;
		}else {
			this.hasRoad = r;
		}
	}

	public void setHasRoad(boolean r, String s) {
			this.hasRoad = r;
			roadCorner = s;
	}
	public void setHasOre(Ore o) {
		ore = o;
	}
	public void setHasPlant(Plant p) {
		plant = p;
	}
	
	public void setHasWall(boolean w, int i) {
		if(hasWall == w) {
			this.hasWall = false;
		}else {
			this.hasWall = w;
		}
		rotate = i;
		
	}

	public void setStructure(Structure s) {
		this.structure = s;
	}

	public void draw(Graphics g) {
//		System.out.println("filling tile");
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);

		g.drawImage(terr.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null);
	}
	private void drawOre(Graphics g) {
		if(ore != null) {
			g.drawImage(ore.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null);
		}
	}
	private void drawPlant(Graphics g) {
		if(plant != null) {
			g.drawImage(plant.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null);
		}
	}
	
	private void applyHighlight(Graphics g, String bm) {

		Graphics2D g2d = (Graphics2D)g;
		
		if(isHighlight == true && hasWall == false && bm != null && bm.equals("wall") ) {
			AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
		    g2d.setComposite(ac);
		    drawWall(g, true);
		}else {
			AlphaComposite ac2 = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
		    g2d.setComposite(ac2);
		    drawWall(g,false);
		}
		
//		if(terr.isBuildable(terr)==false) {
//			Color c = new Color(255, 0, 0, 4); // Red with alpha = 0.5 
//			g2d.setColor(c);
//			g.setColor(c);
//			g.fillRect(100,100,100,100); 
//		}
		
	}
	
	private void drawWall(Graphics g, boolean highlight) {
		int extra = 0;
		if (Game.tileSize < minEntitySize) {
			extra = minEntitySize - Game.tileSize;
		}

		if (highlight==true || hasWall == true) {
			g.drawImage(Buildings.WALL.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
			
		}
	}

	public void drawEntities(Graphics g, String bm) {
		int extra = 0;
		if (Game.tileSize < minEntitySize) {
			extra = minEntitySize - Game.tileSize;
		}
		if (hasRoad == true) {
			
			g.drawImage(Utils.roadImages.get(roadCorner), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		
		applyHighlight(g, bm);
		
		
		if (structure != null) {
			g.drawImage(structure.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		drawPlant(g);
		drawOre(g);
		isHighlight = false;
	}

	public boolean getHasWall() {
		return hasWall;
	}
	public boolean getHasRoad() {
		return hasRoad;
	}
	public Ore getOre() {
		return ore;
	}
	public boolean canBuild() {
		return terr.isBuildable(terr);
	}
	public boolean checkTerrain(Terrain t) {
		if(terr == t) {
			return true;
		}
		return false;
	}
	public void highlight(Graphics g) {
		isHighlight = true;
		
		g.setColor(new Color(0, 0, 0, 64));
		g.drawRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		g.drawRect(p.getIntX() * Game.tileSize + 1, p.getIntY() * Game.tileSize + 1, Game.tileSize - 1,
				Game.tileSize - 1);
		
		
	    
//		g.setColor(new Color(255,0,0,128));
//		g.fillRect(p.x*Game.tileSize, p.y*Game.tileSize, Game.tileSize, Game.tileSize);

	}

}
