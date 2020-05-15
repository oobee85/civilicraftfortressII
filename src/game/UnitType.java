package game;
import java.awt.*;

import javax.swing.ImageIcon;
import utils.*;

public enum UnitType implements HasImage {
	WORKER ( "resources/Images/units/worker.png", new CombatStats(100, 0, 10)),
 	WARRIOR ( "resources/Images/units/warrior.png", new CombatStats(100, 10, 10)),
 	SPEARMAN ( "resources/Images/units/spearman.png", new CombatStats(200, 10, 10)),
 	ARCHER ( "resources/Images/units/archer.png", new CombatStats(50, 20, 10)),
 	SWORDSMAN ( "resources/Images/units/swordsman.png", new CombatStats(100, 20, 10)),
 	HORSEMAN ( "resources/Images/units/horseman.png", new CombatStats(100, 10, 20)),
 	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
    
    UnitType( String s, CombatStats cs) {
    	combatStats = cs;
    	mipmap = new MipMap(s);
    }
    
    
    public Image getImage() {
    	return mipmap.getImage(0);
    }
    public ImageIcon getImageIcon() {
    	return mipmap.getImageIcon(0);
    }

	public double getHealth() {
		return combatStats.getHealth();
	}
	public double getAttack() {
		return combatStats.getAttack();
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
