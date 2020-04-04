import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Point;
import java.util.ArrayList;




public class Tile {
	private boolean hasRoad;
	private boolean hasWall;
	private boolean hasMine;
	private boolean hasIrrigation;
	private boolean hasForest;
	private int forestType;
	private boolean isHighlight;
	private boolean hasOre;
	private boolean hasBuilding;
	
	private Unit unit;
	private Position p;
	
	int minEntitySize = 20;
	private int rotate;
	private String corner;
	
	private String roadCorner;
	
	private Ore ore;
	private Plant plant;
	private Terrain terr;
	private Structure structure;
	private Buildings building;
	
	public Tile(Unit u, Position point, Terrain t) {
		unit = u;
		p = point;
		terr = t;
		isHighlight = false;
		
	}
	public void setHasBuilding(Buildings b) {
		this.hasBuilding = true;
		building = b;
	}
	public void buildRoad(boolean b) {
		if(hasRoad == b) {
			this.hasRoad = false;
		}else {
			this.hasRoad = b;
		}
	}

	public void setHasRoad(boolean b, String s) {
		this.hasRoad = b;
		roadCorner = s;
	}
	public void setHasOre(Ore o) {
		hasOre = true;
		ore = o;
	}
	public void setHasPlant(Plant p) {
		plant = p;
	}
	public void setHighlight(boolean b) {
		isHighlight = b;
	}
	public void setHasMine(boolean b) {
		if(hasMine == b) {
			this.hasMine = false;
		}else {
			this.hasMine = b;
		}
		
	}
	public void setHasForest(boolean b, int t) {
			this.hasForest = b;
			setForestType(t);
	}
	private void setForestType(int t) {
		forestType = t;
	}
	public void setHasIrrigation(boolean b) {
		if(hasIrrigation == b) {
			this.hasIrrigation = false;
		}else {
			this.hasIrrigation = b;
		}
		
	}
	public void setHasWall(boolean w) {
		if(hasWall == w) {
			this.hasWall = false;
		}else {
			this.hasWall = w;
		}
		
	}

	public void setStructure(Structure s) {
		this.structure = s;
	}

	public void draw(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		g.drawImage(terr.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
	}
	
	public void drawHeightMap(Graphics g, double height) {
		int r = Math.max(Math.min((int)(255*height), 255), 0);
		g.setColor(new Color(r, 0, 255-r));
		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
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
		if(hasForest == true && forestType == 0) {
			g.drawImage(Terrain.FOREST0.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null); 
		}
		if(hasForest == true && forestType == 1) {
			g.drawImage(Terrain.FOREST1.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null); 
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
			g.drawImage(Buildings.WALL_BRICK.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
			
		}
	}

	public void drawEntities(Graphics g, String bm) {
		int extra = 0;
		if (Game.tileSize < minEntitySize) {
			extra = minEntitySize - Game.tileSize;
		}
		
		if(hasWall == false) {
			drawPlant(g);
			drawOre(g);
		}
		
		//kills the plant if its built on
		if(plant != null && hasWall == true) {
			plant = null;
		}
		
		if (hasRoad == true) {
			g.drawImage(Utils.roadImages.get(roadCorner), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		if(hasBuilding == true) {
			g.drawImage(building.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		if (hasMine == true) {
			g.drawImage(Buildings.MINE.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		if (hasIrrigation == true) {
			g.drawImage(Buildings.IRRIGATION.getImage(), p.getIntX() * Game.tileSize - extra / 2,p.getIntY() * Game.tileSize - extra / 2, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		applyHighlight(g, bm);
		
		
		if (structure != null) {
			g.drawImage(structure.getImage(), p.getIntX() * Game.tileSize - extra / 1,p.getIntY() * Game.tileSize - extra / 1, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		
		
		isHighlight = false;
	}

	public boolean getHasWall() {
		return hasWall;
	}
	public boolean getHasOre() {
		return hasOre;
	}
	public boolean getHasRoad() {
		return hasRoad;
	}
	public boolean getHasForest() {
		return hasForest;
	}
	public Ore getOre() {
		return ore;
	}
	public Structure getStructure() {
		return structure;
	}
	public boolean isStructure(Structure s) {
		if(structure == s) {
			return true;
		}
		return false;
	}
	public boolean canBuild() {
		return terr.isBuildable(terr);
	}
	public boolean canPlant() {
		return terr.isPlantable(terr);
	}
	public Terrain getTerrain() {
		return terr;
	}
	public boolean checkTerrain(Terrain t) {
		return terr == t;
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
