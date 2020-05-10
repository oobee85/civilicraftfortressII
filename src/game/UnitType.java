package game;
import java.awt.*;

import javax.swing.ImageIcon;
import utils.*;

public enum UnitType implements HasImage{
	WORKER ( "resources/Images/units/worker.png", new CombatStats(30,0,0,10)),
 	WARRIOR ( "resources/Images/units/warrior.png", new CombatStats(30,10,10,10)),
 	SPEARMAN ( "resources/Images/units/spearman.png", new CombatStats(30,10,20,10)),
 	DEER ( "resources/Images/units/deer.png", new CombatStats(30,10,20,10)),
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
	public double getDefence() {
		return combatStats.getDefence();
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
