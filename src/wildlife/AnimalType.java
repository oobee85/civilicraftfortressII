package wildlife;

import java.awt.*;

import javax.swing.*;

import game.*;
import utils.*;

public enum AnimalType implements HasImage{
	DEER("resources/Images/units/deer.png", new CombatStats(20, 1, 1, 10))
	;

	private MipMap mipmap;
	private CombatStats combatStats;
	
	AnimalType(String s, CombatStats cs) {
		mipmap = new MipMap(s);
		combatStats = cs;
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
