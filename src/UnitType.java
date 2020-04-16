import java.awt.Image;

import javax.swing.ImageIcon;
public enum UnitType {
	WORKER ( "resources/Images/units/worker.png", new CombatStats(1,0,0,10)),
 	WARRIOR ( "resources/Images/units/warrior.png", new CombatStats(10,10,10,10)),
 	SPEARMAN ( "resources/Images/units/spearman.png", new CombatStats(10,10,20,10)),
	;
	
	private MipMap mipmap;
    private CombatStats combatStats;
    
    UnitType( String s, CombatStats cs) {
    	combatStats = cs;
    	mipmap = new MipMap(s);
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
