package game;
import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public enum UnitType implements HasImage {
	WORKER           ( "resources/Images/units/worker.png", new CombatStats(100, 00, 10, 1, 20), false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50);  }} ),
 	WARRIOR         ( "resources/Images/units/warrior.png", new CombatStats(100, 10, 10, 1, 20), false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.COPPER_ORE,10); }} ),
 	SPEARMAN       ( "resources/Images/units/spearman.png", new CombatStats(200, 10, 10, 1, 20), false, false, false, ResearchType.BRONZE_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.COPPER_ORE,20);}} ),
 	ARCHER            ( "resources/Images/units/archer.png", new CombatStats(50, 20, 10, 2, 20), false, false, false, ResearchType.WARRIOR_CODE, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); }}),
 	SWORDSMAN     ( "resources/Images/units/swordsman.png", new CombatStats(200, 20, 10, 1, 20), false, false, false, ResearchType.IRON_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.IRON_ORE,20);}}),
 	HORSEMAN       ( "resources/Images/units/horseman.png", new CombatStats(100, 10, 20, 2, 20), false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.COPPER_ORE,10); put(ItemType.HORSE,10);}}),
 	KNIGHT           ( "resources/Images/units/knight.png", new CombatStats(200, 30, 20, 2, 20), false, false, false, ResearchType.CHIVALRY, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.MITHRIL_ORE,10); put(ItemType.HORSE,20);}}),
 	CHARIOT         ( "resources/Images/units/chariot.png", new CombatStats(200, 10, 20, 2, 20), false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.WOOD,10); put(ItemType.HORSE,10);}}),
 	HORSEARCHER ( "resources/Images/units/horsearcher.png", new CombatStats(100, 30, 20, 3, 20), false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.HORSE,10);}}),
 	
	DEER    ("resources/Images/units/deer.png", new CombatStats(200, 5, 10, 1, 20), false, false, false, null, null),
	HORSE  ("resources/Images/units/horse.png", new CombatStats(200, 5, 10, 1, 20), false, false, false, null, null),
	PIG      ("resources/Images/units/pig.png", new CombatStats(200, 5, 10, 1, 20), false, false, false, null, null),
	SHEEP  ("resources/Images/units/sheep.png", new CombatStats(200, 5, 10, 1, 20), false, false, false, null, null),
	FISH   ("resources/Images/units/fish2.png", new CombatStats(10, 1, 100, 1, 20),  true, false, false, null, null),
	COW     ("resources/Images/units/cow.png", new CombatStats(200, 10, 10, 1, 20), false, false, false, null, null),
	
	DRAGON  ("resources/Images/units/dragon.png", new CombatStats(1000, 50, 100, 50, 20), false, true, true, null, null),
	WOLF       ("resources/Images/units/wolf.png", new CombatStats(100, 10, 10, 10, 20), false, false, true, null, null),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
	private boolean isAquatic;
	private boolean isFlying;
	private boolean isHostile;
	private HashMap <ItemType, Integer> cost;
	private ResearchType researchRequirement;
    
    UnitType( String s, CombatStats cs, boolean isAquatic, boolean isFlying, boolean isHostile, ResearchType researchNeeded, HashMap<ItemType, Integer> resourcesNeeded) {
    	mipmap = new MipMap(s);
    	combatStats = cs;
		this.isAquatic = isAquatic;
		this.isFlying = isFlying;
		this.isHostile = isHostile;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
    }
	public ResearchType getResearchRequirement() {
		return researchRequirement;
	}
    
	public CombatStats getCombatStats() {
		return combatStats;
	}
	
	public boolean isAquatic() {
		return isAquatic;
	}
	public boolean isFlying() {
		return isFlying;
	}
	public boolean isHostile() {
		return isHostile;
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}
	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
