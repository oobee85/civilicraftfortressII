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
	private boolean hasStructure;
	private boolean isTerritory = false;
	
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
	public void setTerritory(boolean b) {
		this.isTerritory = b;
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
	public void setHasForest(boolean b, int t) {
			this.hasForest = b;
			setForestType(t);
	}
	private void setForestType(int t) {
		forestType = t;
	}
	
	public void setBuilding(Buildings b) {
		this.building = b;
		hasBuilding = true;
	}
	
	public void setStructure(Structure s) {
		this.structure = s;
		hasStructure = true;
	}

	public void draw(Graphics g, BuildMode bm) {
//		g.setColor(Color.PINK);
//		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		
		drawTerrain(g);
		applyHighlight(g, bm);
		drawEntities(g, bm);
//		g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		isHighlight = false;
//		Utils.resetTransparent(g);
	}
	
	public void drawEntities(Graphics g, BuildMode bm) {
		
		drawTerritory(g);
		drawPlant(g);
		drawOre(g);
		drawRoad(g);
		drawBuilding(g, bm);
		drawStructure(g, bm);
		
		
	}
	
	private void applyHighlight(Graphics g, BuildMode bm) {
		
		if(isHighlight == true && bm != BuildMode.NOMODE) {
			Utils.setTransparent(g);
		    if(terr.isBuildable(terr)==false) {
		    	//draws red rectangle over image
		    	Color c = new Color(255, 0, 0, 100); // Red with alpha = 0.5 
		    	g.setColor(c);
				g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize); 
		    }
		    
		}else {
			Utils.resetTransparent(g);
		}
		
		
	}
	
	private void drawTerrain(Graphics g) {
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
		//kills the plant if its built on
		if(plant != null && hasWall == true) {
			plant = null;
		}
		if(hasForest == true && forestType == 0) {
			g.drawImage(Terrain.FOREST0.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null); 
		}
		if(hasForest == true && forestType == 1) {
			g.drawImage(Terrain.FOREST1.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize,Game.tileSize, null); 
		}
		
	}
	
	
	
	private void drawBuilding(Graphics g, BuildMode bm) {
		if(hasBuilding == true) {
			Utils.resetTransparent(g);
			g.drawImage(building.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		
		if(bm == BuildMode.WALL && isHighlight == true) {
			g.drawImage(Buildings.WALL_BRICK.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		if(bm == BuildMode.MINE && isHighlight == true) {
			g.drawImage(Buildings.MINE.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		if(bm == BuildMode.IRRIGATE && isHighlight == true) {
			g.drawImage(Buildings.IRRIGATION.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
	}
	private void drawStructure(Graphics g, BuildMode bm) {
		int extra = 0;
		if (Game.tileSize < minEntitySize) {
			extra = minEntitySize - Game.tileSize;
		}
		if(hasStructure == true) {
			Utils.resetTransparent(g);
			g.drawImage(structure.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}else if (structure != null) {
			g.drawImage(structure.getImage(), p.getIntX() * Game.tileSize - extra / 1,p.getIntY() * Game.tileSize - extra / 1, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		
		if(bm == BuildMode.BARRACKS && isHighlight == true) {
			g.drawImage(Structure.BARRACKS.getImage(),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		
	}
	private void drawTerritory(Graphics g) {
		if(isTerritory == true) {
			g.drawImage(Buildings.IRRIGATION.getImage(), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
	}
	private void drawRoad(Graphics g) {
		if (hasRoad == true) {
			g.drawImage(Utils.roadImages.get(roadCorner), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
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
