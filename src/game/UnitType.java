package game;
import java.io.*;
import java.util.*;

import game.components.*;
import networking.server.Server;
import utils.*;
import utils.Loader.*;

public class UnitType implements Serializable {
	
	public transient static final HashMap <ItemType, Integer> EMPTY_COST = new HashMap <>();

	private transient static int idCounter = 0;
	private transient final int id;
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final CombatStats combatStats;
	private transient final HashSet<String> attributes;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final String researchRequirement;
	private transient final LinkedList<Item> deadItem;
	private transient final TargetInfo[] targetingInfoStrings;
	private transient final ArrayList<TargetingInfo> targetingInfo = new ArrayList<>();
	private transient final LinkedList<AttackStyle> attackStyles;
	private transient final int inventoryStackSize;
	private transient final boolean isDangerousToOwnTeam;
	private transient final int powerLevel;
	private transient final Set<GameComponent> components = new HashSet<>();

	public UnitType(String name, String image, CombatStats cs, 
	                HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, 
	                LinkedList<Item> deadItem, TargetInfo[] targeting, LinkedList<AttackStyle> attackStyles,
	                int inventoryStackSize) {
		id = idCounter++;
		this.name = name;
		this.mipmap = new MipMap(image);
		this.combatStats = cs;
		this.attributes = attributes;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadItem = deadItem;
		this.targetingInfoStrings = targeting;
		this.attackStyles = attackStyles;
		this.inventoryStackSize = inventoryStackSize;
		
		this.isDangerousToOwnTeam = computeIsDangerousToOwnTeam();
		this.powerLevel = computePowerLevel();
	}
	
	private int computePowerLevel() {
		int power = 0;
		power += combatStats.getHealth();
		power -= combatStats.getMoveSpeed();
		for (AttackStyle att : attackStyles) {
			int attackCooldown = att.getCooldown() == 0 ? 1 : att.getCooldown();
			int dps = att.getDamage() * Server.MILLISECONDS_PER_TICK / attackCooldown;
			power += dps;
			power += att.getRange()*2;
		}
		return power;
	}

	public int getInventoryStackSize() {
		return inventoryStackSize;
	}
	
	public int getPowerLevel() {
		return powerLevel;
	}

	private boolean computeIsDangerousToOwnTeam() {
		for (AttackStyle style : attackStyles) {
			if (style.getProjectile() != null && style.getProjectile().isExplosive()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDangerousToOwnTeam() {
		return isDangerousToOwnTeam;
	}
	public LinkedList<AttackStyle> getAttackStyles() {
		return attackStyles;
	}
	public TargetInfo[] getTargetingInfoStrings() {
		return targetingInfoStrings;
	}
	public ArrayList<TargetingInfo> getTargetingInfo() {
		return targetingInfo;
	}
	public String name() {
		return name;
	}
	public String getResearchRequirement() {
		return researchRequirement;
	}
	public CombatStats getCombatStats() {
		return combatStats;
	}
	public LinkedList<Item> getDeadItem() {
		return deadItem;
	}
	public boolean isAquatic() {
		return attributes.contains("aquatic");
	}
	public boolean isFlying() {
		return attributes.contains("flying");
	}
	public boolean isHostile() {
		return attributes.contains("hostile");
	}
	public boolean isHerbivore() {
		return attributes.contains("herbivore");
	}
	public boolean isMigratory() {
		return attributes.contains("migratory");
	}
	public boolean isHealer() {
		return attributes.contains("healer");
	}
	public boolean isCaravan() {
		return attributes.contains("caravan");
	}
	public boolean hasCleave() {
		return attributes.contains("cleave");
	}
	public boolean isDelayedInvasion() {
		return attributes.contains("delayedinvasion");
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public Set<GameComponent> getComponents() {
		return components;
	}

	public MipMap getMipMap() {
		return mipmap;
	}
	
	public int id() {
		return id;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
