import java.awt.Image;

import javax.swing.ImageIcon;

public enum Unit {
	WORKER ( "resources/Images/units/worker32.png", new CombatStats(1,0,0,10) ),
 	WARRIOR ( "resources/Images/units/warrior32.png", new CombatStats(10,10,10,10) ),
 	SPEARMAN ( "resources/Images/units/spearman32.png", new CombatStats(10,10,20,10) ),
	;

    private final ImageIcon imageicon;
    private CombatStats combatStats;
    
    
    
    Unit( String s, CombatStats cs) {
    	combatStats = cs;
        this.imageicon = Utils.loadImageIcon(s);
    }
    
    public CombatStats getCombatStats() {
    	return combatStats;
    }
    
    public Image getImage() {
    	return imageicon.getImage();
    }
    public ImageIcon getImageIcon() {
    	return imageicon;
    }
    
}
