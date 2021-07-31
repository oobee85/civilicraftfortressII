package game;
import java.io.*;
import java.util.*;

import game.components.*;
import ui.graphics.opengl.*;
import utils.*;
import utils.Loader.*;

public class UnitType implements Serializable {
	
	public transient static final HashMap <ItemType, Integer> EMPTY_COST = new HashMap <>();

	private transient static int idCounter = 0;
	private transient final int id;
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final TexturedMesh mesh;
	private transient final CombatStats combatStats;
	private transient final HashSet<String> attributes;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final String researchRequirement;
	private transient final LinkedList<Item> deadItem;
	private transient final TargetInfo[] targetingInfoStrings;
	private transient final ArrayList<TargetingInfo> targetingInfo = new ArrayList<>();
	private transient final LinkedList<AttackStyle> attackStyles;
	private transient final Set<GameComponent> components = new HashSet<>();

	public UnitType(String name, String image, Mesh mesh, String textureFile, CombatStats cs, 
	                HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, 
	                LinkedList<Item> deadItem, TargetInfo[] targeting, LinkedList<AttackStyle> attackStyles) {
		id = idCounter++;
		this.name = name;
		this.mipmap = new MipMap(image);
		this.mesh = new TexturedMesh(mesh, textureFile);
		this.combatStats = cs;
		this.attributes = attributes;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadItem = deadItem;
		this.targetingInfoStrings = targeting;
		this.attackStyles = attackStyles;
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
	public boolean isBuilder() {
		return attributes.contains("builder");
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
	
	public TexturedMesh getMesh() {
		return mesh;
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
