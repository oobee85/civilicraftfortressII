package game;
import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;
import world.ResourceType;

public enum UnitType implements HasImage {
	WORKER           ( "resources/Images/units/worker.png", 	new CombatStats(200,  0, 15, 1, 20, 30, 100), false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50);  }}, null, null ),
 	WARRIOR         ( "resources/Images/units/warrior.png", 	new CombatStats(200, 10, 15, 1, 20, 50, 100),  false, false, false, null, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.COPPER_BAR,5); }}, null, null ),
 	SPEARMAN       ( "resources/Images/units/spearman.png", 	new CombatStats(400, 10, 15, 1, 20, 50, 100), false, false, false, ResearchType.BRONZE_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.COPPER_BAR,5);}}, null, null ),
 	ARCHER           ( "resources/Images/units/archer.png", 	new CombatStats(200, 30, 15, 3, 30, 100, 100), false, false, false, ResearchType.WARRIOR_CODE, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); }}, null, ProjectileType.ARROW ),
 	SWORDSMAN     ( "resources/Images/units/swordsman.png",		new CombatStats(300, 20, 15, 1, 20, 100, 100), false, false, false, ResearchType.IRON_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.IRON_SWORD,1);}}, null, null ),
 	LONGBOWMAN     ( "resources/Images/units/longbowman.png", 	new CombatStats(200, 30, 15, 6, 30, 200, 100), false, false, false, ResearchType.IRON_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.WOOD,100); }}, null, ProjectileType.ARROW ),
 	CATAPULT     ( "resources/Images/units/catapult.png", 		new CombatStats(100, 200, 50, 10, 100, 500, 100), false, false, false, ResearchType.IRON_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.MITHRIL_BAR,5);}}, null, ProjectileType.ROCK ),
 	
 	HORSEMAN       ( "resources/Images/units/horseman.png", new CombatStats(400, 20, 5, 1, 20, 100, 100),  false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.IRON_SWORD,1); put(ItemType.HORSE,10);}}, null, null ),
 	KNIGHT           ( "resources/Images/units/knight.png", new CombatStats(600, 45, 10, 1, 30, 200, 100), false, false, false, ResearchType.CHIVALRY, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.MITHRIL_SWORD,1); put(ItemType.HORSE,20);}}, null, null ),
 	CHARIOT         ( "resources/Images/units/chariot.png", new CombatStats(400, 30, 10, 6, 30, 100, 100), false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.WOOD,10); put(ItemType.HORSE,10);}}, null, ProjectileType.ARROW ),
 	HORSEARCHER ( "resources/Images/units/horsearcher.png", new CombatStats(200, 40, 5, 3, 20, 300, 100),  false, false, false, ResearchType.HORSEBACK_RIDING, new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.HORSE,10);}}, null, ProjectileType.ARROW ),
 	
	DEER		("resources/Images/units/deer.png", 	new CombatStats(200, 5, 5, 1, 40, 0, 50), 	false, false, false, null, null, ResourceType.DEAD_ANIMAL, null),
	HORSE		("resources/Images/units/horse.png",	new CombatStats(200, 5, 5, 1, 40, 0, 50), 	false, false, false, null, null, ResourceType.DEAD_ANIMAL, null),
	PIG			("resources/Images/units/pig.png",		new CombatStats(200, 10, 30, 1, 40, 0, 50), false, false, false, null, null, ResourceType.DEAD_ANIMAL, null),
	SHEEP		("resources/Images/units/sheep.png",	new CombatStats(200, 5, 30, 1, 40, 0, 50), 	false, false, false, null, null, ResourceType.DEAD_ANIMAL, null),
	FISH		("resources/Images/units/fish2.png",	new CombatStats( 10, 1, 5, 1, 40, 0, 50),  	true, false, false, null, null, null, null),
	COW			("resources/Images/units/cow.png",		new CombatStats(200, 10, 30, 1, 40, 0, 50), false, false, false, null, null, ResourceType.DEAD_ANIMAL, null),
	
	
	DRAGON	   ("resources/Images/units/dragon.png",	new CombatStats(1000, 120, 5, 2, 40, 0, 10), 	false, true, true, null, null, ResourceType.DEAD_ANIMAL, ProjectileType.FIREBALL),
	OGRE	   ("resources/Images/units/ogre.png",		new CombatStats(1000, 40, 30, 1, 40, 0, 10), 	false, false, true, null, null, ResourceType.DEAD_ANIMAL, null),
	ENT		   ("resources/Images/units/ent.png",		new CombatStats(500, 10, 50, 1, 100, 0, 10), 	false, false, true, null, null, ResourceType.DEAD_ANIMAL, null),
	WEREWOLF   ("resources/Images/units/werewolf.png",	new CombatStats(200, 15, 10, 1, 5, 0, 10), 		false, false, true, null, null, ResourceType.DEAD_ANIMAL, null),
	LAVAGOLEM  ("resources/Images/units/lavagolem.png", new CombatStats(1000, 25, 50, 1, 100, 0, 10), 	false, false, true, null, null, ResourceType.DEAD_ANIMAL, null),
	VAMPIRE    ("resources/Images/units/vampire.png", 	new CombatStats(200, 20, 20, 1, 40, 0, 10), 	false, true, true, null, null, ResourceType.DEAD_ANIMAL, null),
	
	WOLF		("resources/Images/units/wolf.png",		new CombatStats(200, 40, 15, 1, 20, 0, 50), 	false, false, true, null, null, ResourceType.DEAD_ANIMAL, null),
	
	FLAMELET	    ("resources/Images/units/flamelet.png", new CombatStats(1, 0, 25, 0, 1000, 0, 50), 	false, false, true, null, null, null, null),
	WATER_SPIRIT ("resources/Images/units/waterspirit.png", new CombatStats(1, 0, 25, 0, 1000, 0, 50), 	false, false, false, null, null, null, null),
	PARASITE	 ("resources/Images/units/parasite.png", 	new CombatStats(1, 0, 1, 0, 1000, 0, 50), 	false, false, true, null, null, null, null),
	FIREFLY			("resources/Images/units/firefly.png",	new CombatStats(1, 0, 25, 1, 1000, 0, 50),  false, false, false, null, null, null, null),
	BOMB			("resources/Images/units/bomb.png",		new CombatStats(50, 0, 25, 1, 1000, 0, 50),  false, false, false, null, null, null, null),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
	private boolean isAquatic;
	private boolean isFlying;
	private boolean isHostile;
	private HashMap <ItemType, Integer> cost;
	private ResearchType researchRequirement;
	private ResourceType deadResource;
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
    
    UnitType( String s, CombatStats cs, boolean isAquatic, boolean isFlying, boolean isHostile, ResearchType researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, ResourceType deadResource, ProjectileType projectileType) {
    	mipmap = new MipMap(s);
    	combatStats = cs;
		this.isAquatic = isAquatic;
		this.isFlying = isFlying;
		this.isHostile = isHostile;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadResource = deadResource;
		this.projectileType = projectileType;
		
    }
	public ResearchType getResearchRequirement() {
		return researchRequirement;
	}
	public ProjectileType getProjectileType() {
		return projectileType;
	}
	public CombatStats getCombatStats() {
		return combatStats;
	}
	public ResourceType getDeadResource() {
		return deadResource;
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
		return this.getCombatStats().getVisionRadius() > 1;
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
