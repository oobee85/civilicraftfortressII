package game;
import java.awt.Color;
import java.awt.Image;
import java.awt.Window.Type;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;
import world.ResourceType;

public enum UnitType implements HasImage {
	WORKER           ( "resources/Images/units/worker.png", 	new CombatStats(100,  0, 15, 1, 20, 30, 50), false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50);  }}, null, null ),
 	WARRIOR         ( "resources/Images/units/warrior.png", 	new CombatStats(200, 10, 15, 1, 30, 50, 50),  false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.WOOD,50); }}, null, null ),
 	SPEARMAN       ( "resources/Images/units/spearman.png", 	new CombatStats(400, 10, 15, 1, 20, 100, 50), false, false, false, "BRONZE_WORKING", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.BRONZE_SWORD,1);}}, null, null ),
 	ARCHER           ( "resources/Images/units/archer.png", 	new CombatStats(200, 0, 15, 3, 30, 100, 50), false, false, false, "WARRIOR_CODE", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.BRONZE_BAR,1); }}, null, ProjectileType.ARROW_ARCHER ),
 	SWORDSMAN     ( "resources/Images/units/swordsman.png",		new CombatStats(400, 20, 15, 1, 20, 200, 50), false, false, false, "IRON_WORKING", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.IRON_SWORD,1);}}, null, null ),
 	LONGBOWMAN     ( "resources/Images/units/longbowman.png", 	new CombatStats(200, 0, 15, 6, 30, 300, 50), false, false, false, "ENGINEERING", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.MITHRIL_BAR, 5); }}, null, ProjectileType.ARROW ),
 	CATAPULT     ( "resources/Images/units/catapult.png", 		new CombatStats(200, 0, 50, 10, 100, 500, 100), false, false, false, "MATHEMATICS", new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.MITHRIL_BAR,5);}}, null, ProjectileType.ROCK_CATAPULT ),
 	TREBUCHET     ( "resources/Images/units/trebuchet.png", 	new CombatStats(200, 0, 50, 20, 100, 750, 100), false, false, false, "MONARCHY", new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.RUNITE_BAR,10);}}, null, ProjectileType.FIREBALL_TREBUCHET ),
 	
 	HORSEMAN       ( "resources/Images/units/horseman.png", new CombatStats(400, 20, 5, 1, 20, 300, 50),  false, false, false, "HORSEBACK_RIDING", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); put(ItemType.MITHRIL_SWORD,1); put(ItemType.HORSE,50);}}, null, null ),
 	KNIGHT           ( "resources/Images/units/knight.png", new CombatStats(1000, 40, 10, 1, 20, 300, 50), false, false, false, "CHIVALRY", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,200); put(ItemType.RUNE_SWORD,1); put(ItemType.HORSE,200);}}, null, null ),
 	CHARIOT         ( "resources/Images/units/chariot.png", new CombatStats(300, 30, 10, 5, 30, 300, 50), false, false, false, "HORSEBACK_RIDING", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,200); put(ItemType.WOOD,200); put(ItemType.HORSE,100); put(ItemType.IRON_BAR,10);}}, null, ProjectileType.ARROW ),
 	HORSEARCHER ( "resources/Images/units/horsearcher.png", new CombatStats(300, 0, 5, 3, 20, 500, 50),  false, false, false, "CHIVALRY", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,200); put(ItemType.HORSE,200); put(ItemType.RUNITE_BAR,5);}}, null, ProjectileType.RUNE_ARROW ),
 	
	DEER		("resources/Images/units/deer.png", 	new CombatStats(200, 5, 5, 1, 40, 0, 50), 	false, false, false, null, null, new Item(100, ItemType.FOOD), null),
	HORSE		("resources/Images/units/horse.png",	new CombatStats(200, 5, 5, 1, 40, 0, 50), 	false, false, false, null, null, new Item(100, ItemType.FOOD), null),
	PIG			("resources/Images/units/pig.png",		new CombatStats(200, 10, 30, 1, 40, 0, 50), false, false, false, null, null, new Item(200, ItemType.FOOD), null),
	SHEEP		("resources/Images/units/sheep.png",	new CombatStats(200, 5, 30, 1, 40, 0, 50), 	false, false, false, null, null, new Item(100, ItemType.FOOD), null),
	COW			("resources/Images/units/cow.png",		new CombatStats(200, 10, 30, 1, 40, 0, 50), false, false, false, null, null, new Item(200, ItemType.FOOD), null),
	FISH		("resources/Images/units/fish2.png",	new CombatStats( 10, 1, 5, 1, 40, 0, 50),  	true, false, false, null, null, new Item(100, ItemType.FOOD), null),
	
	DRAGON	   ("resources/Images/units/dragon.png",	new CombatStats(2000, 50, 5, 1, 30, 0, 10), 	false, true, true, null, null, new Item(10, ItemType.RUNITE_ORE), ProjectileType.FIREBALL_DRAGON),
	VAMPIRE    ("resources/Images/units/vampire.png", 	new CombatStats(200, 20, 15, 1, 40, 0, 1), 		false, true, true, null, null, new Item(100, ItemType.FOOD), null),
	ROC			("resources/Images/units/roc.png",		new CombatStats(500, 50, 10, 1, 25, 0, 10), 	false, true, true, null, null, new Item(100, ItemType.FOOD), null),
	
	OGRE	   ("resources/Images/units/ogre.png",		new CombatStats(500, 40, 50, 1, 40, 0, 5), 	false, false, true, null, null, new Item(10, ItemType.BRONZE_BAR), null),
	ENT		   ("resources/Images/units/ent.png",		new CombatStats(1000, 50, 50, 1, 100, 0, 10), 	false, false, true, null, null, new Item(200, ItemType.WOOD), null),
	WEREWOLF   ("resources/Images/units/werewolf.png",	new CombatStats(100, 20, 10, 1, 5, 0, 10), 		false, false, true, null, null, null, null),
	LAVAGOLEM  ("resources/Images/units/lavagolem.png", new CombatStats(1000, 200, 50, 1, 100, 0, 10), 	false, false, true, null, null, new Item(50, ItemType.MITHRIL_ORE), null),
	WOLF		("resources/Images/units/wolf.png",		new CombatStats(200, 40, 20, 1, 20, 0, 50), 	false, false, true, null, null, new Item(100, ItemType.FOOD), null),
	CYCLOPS    ("resources/Images/units/cyclops.png", 	new CombatStats(500, 40, 50, 6, 40, 0, 10), 	false, false, true, null, null, new Item(1, ItemType.MITHRIL_BAR), ProjectileType.ROCK_CYCLOPS),
	SKELETON	("resources/Images/units/skeleton.png",	new CombatStats(200, 20, 20, 1, 20, 0, 50), 	false, false, true, null, null, new Item(100, ItemType.FOOD), null),
	
	FLAMELET	    ("resources/Images/units/flamelet.png", new CombatStats(1, 0, 25, 0, 1000, 0, 50), 	false, false, true, null, null, new Item(10 ,ItemType.COAL), null),
	WATER_SPIRIT ("resources/Images/units/waterspirit.png", new CombatStats(1, 0, 25, 0, 1000, 0, 50), 	false, false, false, null, null, null, null),
	PARASITE	 ("resources/Images/units/parasite.png", 	new CombatStats(1, 0, 5, 0, 1000, 0, 50), 	false, false, true, null, null, null, null),
	FIREFLY			("resources/Images/units/firefly.png",	new CombatStats(1, 0, 25, 1, 1000, 0, 50),  false, false, false, null, null, null, null),
	BOMB			("resources/Images/units/bomb.png",		new CombatStats(1, 0, 25, 1, 1000, 0, 50),  false, false, false, null, null, null, null),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
	private boolean isAquatic;
	private boolean isFlying;
	private boolean isHostile;
	private HashMap <ItemType, Integer> cost;
	private String researchRequirement;
	private Item deadItem;
	private ProjectileType projectileType;
	
	/**
	 * @param String
	 * @param CombatStats
	 * @param isAquatic
	 * @param isFlying
	 * @param isHostile 
	 * @param researchNeeded
	 * @param resourcesNeeded
	**/
    
    UnitType( String s, CombatStats cs, boolean isAquatic, boolean isFlying, boolean isHostile, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, Item deadItem, ProjectileType projectileType) {
    	mipmap = new MipMap(s);
    	this.combatStats = cs;
		this.isAquatic = isAquatic;
		this.isFlying = isFlying;
		this.isHostile = isHostile;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadItem = deadItem;
		this.projectileType = projectileType;
		
    }
	public String getResearchRequirement() {
		return researchRequirement;
	}
	public ProjectileType getProjectileType() {
		return projectileType;
	}
	public CombatStats getCombatStats() {
		return combatStats;
	}
	public Item getDeadItem() {
		return deadItem;
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
	public boolean isRanged() {
		return this.getCombatStats().getAttackRadius() > 1;
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	public boolean hasLifeSteal() {
		if(this == UnitType.VAMPIRE) {
			return true;
		}
		return false;
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
