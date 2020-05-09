package world;
import java.awt.Image;

import utils.*;

public enum Ore {
	
	ORE_IRON ( 100, new String[] {"resources/Images/ore/iron/ore_iron16.png", "resources/Images/ore/iron/ore_iron128.png"}, 0.05, false),
	ORE_COPPER ( 100, new String[] {"resources/Images/ore/copper/ore_copper16.png", "resources/Images/ore/copper/ore_copper128.png"} , 0.1, false),
	ORE_SILVER ( 100, new String[] {"resources/Images/ore/silver/ore_silver16.png", "resources/Images/ore/silver/ore_silver128.png"} , 0.05, false),
	ORE_MITHRIL ( 100, new String[] {"resources/Images/ore/mithril/ore_mithril16.png", "resources/Images/ore/mithril/ore_mithril128.png"} , 0.01, false),
	ORE_GOLD ( 100, new String[] {"resources/Images/ore/gold/ore_gold16.png", "resources/Images/ore/gold/ore_gold128.png"} , 0.1, true),
	ORE_RUNE ( 100, new String[] {"resources/Images/ore/runite/ore_rune16.png", "resources/Images/ore/runite/ore_rune128.png"} , 0.1, true),
	ORE_ADAMANT ( 100, new String[] {"resources/Images/ore/adamantite/ore_adamant16.png", "resources/Images/ore/adamantite/ore_adamant128.png"} , 0.2, true),
	ORE_TITANIUM ( 100, new String[] {"resources/Images/ore/titanium/ore_titanium16.png", "resources/Images/ore/titanium/ore_titanium128.png"} , 0.05, true)
	;
	
	
	private MipMap mipmap;
    private int yield;
    private double rarity;
    private boolean isRare;
    
	Ore(int y, String[] s, double r, boolean rare){
		yield = y;
		rarity = r;
		isRare = rare;
		mipmap = new MipMap(s);
	}
	
	public Image getImage(int size) {
		return mipmap.getImage(size);
    }
	
	public int getYield() {
		return yield;
	}
	public double getRarity() {
		return rarity;
	}
	public boolean isRare() {
		return isRare;
	}
	
	
	
}
