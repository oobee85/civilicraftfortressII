package wildlife;

import java.awt.*;

import javax.swing.*;

import game.*;
import utils.*;

public enum AnimalType {
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

    public Image getImage() {
    	return mipmap.getImage(0);
    }
    public ImageIcon getImageIcon() {
    	return mipmap.getImageIcon(0);
    }
}
