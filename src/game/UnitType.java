package game;
import java.awt.*;

import javax.swing.ImageIcon;
import utils.*;

public enum UnitType implements HasImage {
	WORKER ( "resources/Images/units/worker.png", new CombatStats(100, 0, 10, 1), false, false, false),
 	WARRIOR ( "resources/Images/units/warrior.png", new CombatStats(100, 10, 10, 1), false, false, false),
 	SPEARMAN ( "resources/Images/units/spearman.png", new CombatStats(200, 10, 10, 1), false, false, false),
 	ARCHER ( "resources/Images/units/archer.png", new CombatStats(50, 20, 10, 2), false, false, false),
 	SWORDSMAN ( "resources/Images/units/swordsman.png", new CombatStats(100, 20, 10, 1), false, false, false),
 	HORSEMAN ( "resources/Images/units/horseman.png", new CombatStats(100, 10, 20, 1), false, false, false),

	DEER  ("resources/Images/units/deer.png", new CombatStats(100, 5, 10, 5), false, false, false),
	HORSE("resources/Images/units/horse.png", new CombatStats(100, 5, 10, 5), false, false, false),
	PIG("resources/Images/units/pig.png", new CombatStats(100, 5, 10, 5), false, false, false),
	SHEEP("resources/Images/units/sheep.png", new CombatStats(100, 5, 10, 5), false, false, false),
	FISH ("resources/Images/units/fish2.png", new CombatStats(10, 1, 100, 1), true, false, false),
	
	DRAGON("resources/Images/units/dragon.png", new CombatStats(1000, 50, 100, 50), false, true, true),
	WOLF("resources/Images/units/wolf.png", new CombatStats(100, 10, 10, 10), false, false, true),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
	private boolean isAquatic;
	private boolean isFlying;
	private boolean isHostile;
    
    UnitType( String s, CombatStats cs, boolean isAquatic, boolean isFlying, boolean isHostile) {
    	mipmap = new MipMap(s);
    	combatStats = cs;
		this.isAquatic = isAquatic;
		this.isFlying = isFlying;
		this.isHostile = isHostile;
    }
    
	public CombatStats getCombatStats() {
		return combatStats;
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
