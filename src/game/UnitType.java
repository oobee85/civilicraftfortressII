package game;
import java.awt.Color;
import java.awt.Image;
import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;

import ui.graphics.*;
import ui.graphics.opengl.*;
import utils.*;
import utils.Loader.*;

public class UnitType implements HasImage, HasMesh, Serializable {
	
	private final String name;
	private transient final MipMap mipmap;
	private transient final Mesh mesh;
	private transient final CombatStats combatStats;
	private transient final HashSet<String> attributes;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final String researchRequirement;
	private transient final LinkedList<Item> deadItem;
	private transient final TargetInfo[] targetingInfoStrings;
	private transient final ArrayList<TargetingInfo> targetingInfo = new ArrayList<>();
	private transient final LinkedList<AttackStyle> attackStyles;

	public UnitType(String name, String image, Mesh mesh, CombatStats cs, HashSet<String> attributes, String researchNeeded, HashMap<ItemType, Integer> resourcesNeeded, LinkedList<Item> deadItem, TargetInfo[] targeting, LinkedList<AttackStyle> attackStyles) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.mesh = mesh;
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
	public boolean isColdResist() {
		return attributes.contains("coldresistant");
	}
	public boolean isFireResist() {
		return attributes.contains("fireresistant");
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
	public boolean isNonSelectable() {
		return attributes.contains("nonselectable");
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
	
	@Override
	public Mesh getMesh() {
		return mesh;
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
	public Image getHighlight(int size) {
		return mipmap.getHighlight(size);
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
