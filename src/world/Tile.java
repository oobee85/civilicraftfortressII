package world;

import java.awt.*;
import java.util.*;
import java.util.List;

import game.*;
import liquid.*;
import ui.*;
import utils.*;

public class Tile {
	public static final Color TERRITORY_COLOR = Color.pink;
	private boolean isTerritory = false;
	private boolean isSelected = false;
	
	private TileLoc location;
	private double height;
	int minEntitySize = 20;
	
	private String roadCorner;
	
	private ResourceType resourceType;
	private RoadType roadType;
	private Plant plant;
	private Terrain terr;
	private Structure structure;
	private Building building;
	
	private List<Unit> units;
	
	
	public double liquidAmount;
	public LiquidType liquidType;
	
	private Tile(TileLoc location, Terrain t) {
		this.location = location;
		terr = t;
		
		liquidType = LiquidType.WATER;
		liquidAmount = 0;
		units = new LinkedList<Unit>();
	}
	
	public static Tile makeTile(TileLoc location, Terrain t) {
		return new Tile(location, t);
	}
	
	public void setRoad(RoadType r, String s) {
		this.roadType = r;
		if(s != null) {
			roadCorner = s;
		}
	}
	public void setTerritory(boolean b) {
		this.isTerritory = b;
	}
	public void setResource(ResourceType o) {
		resourceType = o;
	}
	public void setHasPlant(Plant p) {
		plant = p;
	}
	
	public boolean hasPlayerControlledUnit() {
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				return true;
			}
		}
		return false;
	}
	public Unit getPlayerControlledUnit() {
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				return u;
			}
		}
		return null;
	}
	
	public List<Unit> getUnits() {
		return units;
	}
	
	public double getBrightness() {
		double brightness = 0;
		if(this.getHasStructure() || this.getHasBuilding() || this.hasPlayerControlledUnit()) {
			brightness += 1;
		}
		
		if(this.isTerritory) {
			brightness += 0.8;
		}
		brightness += getTerrain().getBrightness();
		brightness += liquidAmount * liquidType.getBrightness();
		return brightness;
	}
	
	public void setBuilding(Building b) {
		if(b != null) {
			if (building != null && building.getBuildingType() == b.getBuildingType()) {
				this.building = null;
			} else if (structure == null) {
				this.building = b;
			}
		}else {
			building = b;
		}
		
			
	}
	
	public void setStructure(Structure s) {
		if(s != null) {
			if (structure != null && structure.getStructureType() == s.getStructureType()) {
				this.structure = null;
				
			}else if (building == null) {
				this.structure = s;
			}	
		}else {
			structure = s;
		}
		
		
		
	}
	public void addUnit(Unit u) {
		units.add(u);
	}
	public void removeUnit(Unit u) {
		units.remove(u);
	}
	
	public void drawHeightMap(Graphics g, double height) {
		int r = Math.max(Math.min((int)(255*height), 255), 0);
		g.setColor(new Color(r, 0, 255-r));
		g.fillRect(location.x * Game.tileSize, location.y * Game.tileSize, Game.tileSize, Game.tileSize);
	}
	
	public void drawDebugStrings(Graphics g, List<String> strings, int[][] rows, int fontsize, int stringWidth) {
		int x = location.x * Game.tileSize + 2;
		int y = location.y * Game.tileSize + fontsize/2;
		int row = rows[location.x][location.y];
		
		g.setColor(Color.black);
		g.fillRect(x, y + 2 + row*fontsize, stringWidth, strings.size()*fontsize);
		g.setColor(Color.green);
		for(String s : strings) {
			g.drawString(s, x, y + (++row)*fontsize);
		}
		rows[location.x][location.y] = row;
	}

	public boolean getHasResource() {
		return resourceType != null;
	}
	
	public boolean isBlocked(Unit u) {
		if(u.isPlayerControlled() && this.hasPlayerControlledUnit()) {
			return true;
		}
		if(u.getUnitType().isFlying()) {
			return false;
		}
		return getHasBuilding() == true && getBuilding().getBuildingType().canMoveThrough() == false;
	}
	
//	public boolean getHasUnit() {
//		return units.isEmpty();
//	}
	public RoadType getRoadType() {
		return roadType;
	}
	public Image getRoadImage() {
		return Utils.roadImages.get(roadCorner);
	}
	public boolean getHasStructure() {
		return structure != null;
	}
	public boolean getHasBuilding() {
		return building != null;
	}
	public ResourceType getResourceType() {
		return resourceType;
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
	public boolean canBuild() {
		return terr.isBuildable(terr) && liquidAmount < liquidType.getMinimumDamageAmount();
	}
	public boolean canPlant() {
		return terr.isPlantable(terr);
	}
	public boolean canOre() {
		return terr.isOreable(terr);
	}
	public void setTerrain(Terrain t) {
		terr = t;
	}
	public boolean canSupportRareOre() {
		return terr.canSupportRare(terr);
	}
	public boolean checkTerrain(Terrain t) {
		return terr == t;
	}
	
	public TileLoc getLocation() {
		return location;
	}
	
	public void setHeight(double newheight) {
		height = newheight;
		if(height > 1) {
			height = 1;
		}
	}
	public double getHeight() {
		return height;
	}

}
