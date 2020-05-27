package wildlife;

import java.awt.*;

import javax.swing.*;

import game.*;
import utils.*;

public enum AnimalType implements HasImage{
	DEER  ("resources/Images/units/deer.png", new CombatStats(100, 5, 10, 5), false, false, false),
	HORSE("resources/Images/units/horse.png", new CombatStats(100, 5, 10, 5), false, false, false),
	PIG    ("resources/Images/units/pig.png", new CombatStats(100, 5, 10, 5), false, false, false),
	SHEEP("resources/Images/units/sheep.png", new CombatStats(100, 5, 10, 5), false, false, false),
	FISH ("resources/Images/units/fish2.png", new CombatStats(10, 1, 100, 1), true, false, false),
	
	DRAGON("resources/Images/units/dragon.png", new CombatStats(1000, 50, 100, 50), false, true, true),
	WOLF    ("resources/Images/units/wolf.png", new CombatStats(100, 10, 10, 10), false, false, true),
	;

	private MipMap mipmap;
	private CombatStats combatStats;
	private boolean isAquatic;
	private boolean isFlying;
	private boolean isHostile;
	
	AnimalType(String s, CombatStats cs, boolean isAquatic, boolean isFlying, boolean isHostile) {
		mipmap = new MipMap(s);
		combatStats = cs;
		this.isAquatic = isAquatic;
		this.isFlying = isFlying;
		this.isHostile = isHostile;
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
	public CombatStats getCombatStats() {
		return combatStats;
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
}
