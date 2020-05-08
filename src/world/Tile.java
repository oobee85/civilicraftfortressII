package world;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Point;
import java.util.ArrayList;

import game.*;
import liquid.*;
import ui.*;
import utils.*;




public class Tile {
	private boolean hasRoad;
	private boolean isHighlight;
//	private boolean hasOre;
//	private boolean hasBuilding;
//	private boolean hasStructure;
//	private boolean hasUnit;
	private boolean isTerritory = false;
	private boolean isSelected = false;
	
	private Position p;
//	private ArrayList<Unit> units = new ArrayList<Unit>();
	int minEntitySize = 20;
	
	private String roadCorner;
	
	private Ore ore;
	private Plant plant;
	private Terrain terr;
	private Structure structure;
	private Building building;
	private Unit unit;
	
	
	public double liquidAmount;
	public LiquidType liquidType;
	
	public Tile(Position point, Terrain t) {
		p = point;
		terr = t;
		isHighlight = false;
		
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
	}
//	public void buildRoad(boolean b) {
//		if(hasRoad == b) {
//			this.hasRoad = false;
//		}else {
//			this.hasRoad = b;
//		}
//	}

	
	public void setRoad(boolean b, String s) {
		this.hasRoad = b;
		if(s != null) {
			roadCorner = s;
		}
		
	}
	public void setTerritory(boolean b) {
		this.isTerritory = b;
	}
	public void setHasOre(Ore o) {
		ore = o;
	}
	public void setHasPlant(Plant p) {
		plant = p;
	}
	public void setHighlight(boolean b) {
		if(isHighlight == b) {
			isHighlight = false;
		}else {
			isHighlight = b;
		}
		
	}
	
	public void setBuilding(Building b) {
		if(this.building == b) {
			this.building = null;
		}else {
			this.building = b;
		}
	}
	
	public void setStructure(Structure s) {
		if(this.structure == s) {
			this.structure = null;
		}else {
			this.structure = s;
		}
		
		
	}
	public void setUnit(Unit u) {
//		units.add(u);
		unit = u;
		
	}

	public void draw(Graphics g, BuildMode bm) {
//		g.setColor(Color.PINK);
//		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		
		drawTerrain(g);
		applyHighlight(g, bm);
		drawEntities(g, bm);
//		g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		isHighlight = false;
	}
	
	public void drawEntities(Graphics g, BuildMode bm) {
	
		drawTerritory(g);
		drawPlant(g);
		drawOre(g);
		drawRoad(g);
		drawWater(g);
		drawBuilding(g, bm);
		drawStructure(g, bm);
		drawHighlightedArea(g);
		drawUnit(g);
		
		
	}
	
	private void drawHighlightedArea(Graphics g) {
		if(isHighlight == true) {
			g.setColor(new Color(0, 0, 0, 64));
			g.drawRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
			g.drawRect(p.getIntX() * Game.tileSize + 1, p.getIntY() * Game.tileSize + 1, Game.tileSize - 1,
					Game.tileSize - 1);
		}
		
	}
	private void applyHighlight(Graphics g, BuildMode bm) {
		
		if(isHighlight == true && bm != BuildMode.NOMODE) {
			Utils.setTransparency(g, 0.5f);
		    if(canBuild() ==false) {
		    	//draws red rectangle over image
		    	Color c = new Color(255, 0, 0, 100); // Red with alpha = 0.5 
		    	g.setColor(c);
				g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize); 
		    }
		    if(bm == BuildMode.IRRIGATE && terr.isPlantable(terr) == false) {
		    	Color c = new Color(255, 0, 0, 100); // Red with alpha = 0.5 
		    	g.setColor(c);
				g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize); 
		    }
		    if(unit != null) {
		    	Color c = new Color(0, 255, 0, 100); 
		    	g.setColor(c);
				g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize); 
		    }
		}else {
			Utils.setTransparency(g, 1);
		}
		
		
	}
	
	private void drawUnit(Graphics g) {
		if(unit != null) {
			g.drawImage(unit.getImage(), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		
	}
	private void drawTerrain(Graphics g) {
		g.drawImage(terr.getImage(Game.tileSize), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		
	}
	
	private void drawWater(Graphics g) {
		float alpha = Utils.getAlphaOfLiquid(liquidAmount);
		Utils.setTransparency(g, alpha);
		g.setColor(liquidType.getColor());
		g.fillRect(p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize);
		Utils.setTransparency(g, 1);
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
		if(plant != null && building != null) {
			plant = null;
		}
		
	}
	
	
	
	private void drawBuilding(Graphics g, BuildMode bm) {
		if(building != null) {
			Utils.setTransparency(g, 1);
			g.drawImage(building.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		
		if(bm == BuildMode.WALL && isHighlight == true) {
			g.drawImage(BuildingType.WALL_BRICK.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		if(bm == BuildMode.MINE && isHighlight == true) {
			g.drawImage(BuildingType.MINE.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		if(bm == BuildMode.IRRIGATE && isHighlight == true) {
			g.drawImage(BuildingType.IRRIGATION.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
	}
	private void drawStructure(Graphics g, BuildMode bm) {
		int extra = 0;
		if (Game.tileSize < minEntitySize) {
			extra = minEntitySize - Game.tileSize;
		}
		if(structure != null) {
			Utils.setTransparency(g, 1);
			g.drawImage(structure.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}else if (structure != null) {
			g.drawImage(structure.getImage(0), p.getIntX() * Game.tileSize - extra / 1,p.getIntY() * Game.tileSize - extra / 1, Game.tileSize + extra, Game.tileSize + extra, null);
		}
		
		if(bm == BuildMode.BARRACKS && isHighlight == true) {
			g.drawImage(StructureType.BARRACKS.getImage(0),p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
		
	}
	private void drawTerritory(Graphics g) {
		if(isTerritory == true) {
			Utils.setTransparency(g, 0.5f);
			Color c = new Color(0, 0, 255, 150); 
	    	g.setColor(c);
	    	g.fillRect(p.getIntX() * Game.tileSize,p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize); 
			
			Utils.setTransparency(g, 1);
		}
		
	}
	private void drawRoad(Graphics g) {
		if (hasRoad == true) {
			g.drawImage(Utils.roadImages.get(roadCorner), p.getIntX() * Game.tileSize, p.getIntY() * Game.tileSize, Game.tileSize, Game.tileSize, null);
		}
	}
	

	

	public boolean getHasOre() {
		return ore != null;
	}
	public boolean getHasUnit() {
		return unit != null;
	}
	public boolean getHasRoad() {
		return hasRoad;
	}
	public boolean getHasStructure() {
		return structure != null;
	}
	public boolean getHasBuilding() {
		return building != null;
	}
	public Ore getOre() {
		return ore;
	}
	public Unit getUnit() {
		return unit;
	}
	public Structure getStructure() {
		return structure;
	}
	public Building getBuilding() {
		return building;
	}
	public Plant getPlant() {
		return plant;
	}
	public Terrain getTerrain() {
		return terr;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public boolean getIsTerritory() {
		return isTerritory;
	}
	
	public boolean isStructure(StructureType s) {
		if(structure.getStructureType() == s) {
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
	
	public void setTerrain(Terrain t) {
		terr = t;
	}
	public boolean checkTerrain(Terrain t) {
		return terr == t;
	}
	public void highlight(Graphics g) {
		isHighlight = true;
		
	}
	public Position getLocation() {
		return p;
	}

}
