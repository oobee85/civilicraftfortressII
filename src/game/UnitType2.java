package game;
import java.awt.Color;
import java.awt.Image;
import java.util.*;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public class UnitType2 implements HasImage {
	
	private String name;
	private MipMap mipmap;
	private CombatStats combatStats;
	private HashSet<String> attributes;
	private HashMap <ItemType, Integer> cost;
	private String researchRequirement;
	private Item deadItem;
	private ProjectileType projectileType;

	public UnitType2(String name, String image, CombatStats cs, HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, Item deadItem, ProjectileType projectileType) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.combatStats = cs;
		this.attributes = attributes;
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
	public boolean isRanged() {
		return this.getCombatStats().getAttackRadius() > 1;
	}
	public boolean isBuilder() {
		return name.equals("WORKER");
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	public boolean hasLifeSteal() {
		return name.equals("VAMPIRE");
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
