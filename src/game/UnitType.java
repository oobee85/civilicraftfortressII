package game;
import java.awt.Color;
import java.awt.Image;
import java.util.*;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public class UnitType implements HasImage {
	
	private String name;
	private MipMap mipmap;
	private CombatStats combatStats;
	private HashSet<String> attributes;
	private HashMap <ItemType, Integer> cost;
	private String researchRequirement;
	private LinkedList<Item> deadItem;
	private ProjectileType projectileType;

	public UnitType(String name, String image, CombatStats cs, HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, LinkedList<Item> deadItem, ProjectileType projectileType) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.combatStats = cs;
		this.attributes = attributes;
		this.cost = resourcesNeeded;
		this.researchRequirement = researchNeeded;
		this.deadItem = deadItem;
		this.projectileType = projectileType;
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
