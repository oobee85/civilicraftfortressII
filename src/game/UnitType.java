package game;
import java.awt.Color;
import java.awt.Image;
import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;

import utils.*;
import utils.Loader.*;

public class UnitType implements HasImage, Serializable {
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final CombatStats combatStats;
	private transient final HashSet<String> attributes;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final String researchRequirement;
	private transient final LinkedList<Item> deadItem;
	private transient final ProjectileType projectileType;
	private transient final TargetInfo[] targetingInfoStrings;
	private transient final ArrayList<TargetingInfo> targetingInfo = new ArrayList<>();

	public UnitType(String name, String image, CombatStats cs, HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, LinkedList<Item> deadItem, ProjectileType projectileType, TargetInfo[] targeting) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.combatStats = cs;
		this.attributes = attributes;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadItem = deadItem;
		this.projectileType = projectileType;
		this.targetingInfoStrings = targeting;
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
	public ProjectileType getProjectileType() {
		return projectileType;
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
	public boolean isColdResist() {
		return attributes.contains("coldresistant");
	}
	public boolean isFireResist() {
		return attributes.contains("fireresistant");
	}
	public boolean hasLifeSteal() {
		return attributes.contains("lifesteal");
	}
	public boolean isMigratory() {
		return attributes.contains("migratory");
	}
	public boolean isRanged() {
		return this.getCombatStats().getAttackRadius() > 1;
	}
	public boolean isBuilder() {
		return attributes.contains("builder");
	}
	public boolean isDelayedInvasion() {
		return attributes.contains("delayedinvasion");
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
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
