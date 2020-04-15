import java.awt.Image;

import javax.swing.ImageIcon;

public enum Unit {
	WORKER ( "resources/Images/units/worker.png", new CombatStats(1,0,0,10), null ),
 	WARRIOR ( "resources/Images/units/warrior.png", new CombatStats(10,10,10,10), null ),
 	SPEARMAN ( "resources/Images/units/spearman.png", new CombatStats(10,10,20,10), null ),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
    private Position pos;
    
    Unit( String s, CombatStats cs, Position p) {
    	combatStats = cs;
    	mipmap = new MipMap(s);
    	pos = p;
    }
    
    public CombatStats getCombatStats() {
    	return combatStats;
    }
    
    public Image getImage() {
    	return mipmap.getImage(0);
    }
    public Position getPos() {
    	return pos;
    }
    public ImageIcon getImageIcon() {
    	return mipmap.getImageIcon(0);
    }
    
}
