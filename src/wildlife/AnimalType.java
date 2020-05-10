package wildlife;

import java.awt.*;

import javax.swing.*;

import game.*;
import utils.*;

public enum AnimalType implements HasImage{
	DEER("resources/Images/units/deer.png", new CombatStats(20, 1, 1, 10), false),
	FISH("resources/Images/units/fish2.png", new CombatStats(5, 1, 1, 100), true)
	;

	private MipMap mipmap;
	private CombatStats combatStats;
	private boolean isAquatic;
	
	AnimalType(String s, CombatStats cs, boolean isAquatic) {
		mipmap = new MipMap(s);
		combatStats = cs;
		this.isAquatic = isAquatic;
	}
	
	public boolean isAquatic() {
		return isAquatic;
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
}
