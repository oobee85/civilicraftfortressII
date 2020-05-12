package world;
import java.awt.Image;

import game.ResourceType;
import utils.*;

public enum Ore {
	GOLD ( 100, new String[] {"resources/Images/ore/gold/ore_gold16.png", "resources/Images/ore/gold/ore_gold128.png"} , 0.0005, true, ResourceType.GOLD_ORE),
	COPPER ( 100, new String[] {"resources/Images/ore/copper/ore_copper16.png", "resources/Images/ore/copper/ore_copper128.png"} , 0.002, false, ResourceType.COPPER_ORE),
	IRON ( 100, new String[] {"resources/Images/ore/iron/ore_iron16.png", "resources/Images/ore/iron/ore_iron128.png"}, 0.001, false, ResourceType.IRON_ORE),
	SILVER ( 100, new String[] {"resources/Images/ore/silver/ore_silver16.png", "resources/Images/ore/silver/ore_silver128.png"} , 0.001, false, ResourceType.SILVER_ORE),
	RUNITE ( 100, new String[] {"resources/Images/ore/runite/ore_rune16.png", "resources/Images/ore/runite/ore_rune128.png"} , 0.000125, true, ResourceType.RUNITE_ORE),
	MITHRIL ( 100, new String[] {"resources/Images/ore/mithril/ore_mithril16.png", "resources/Images/ore/mithril/ore_mithril128.png"} , 0.0005, false, ResourceType.MITHRIL_ORE),
	ADAMANTITE ( 100, new String[] {"resources/Images/ore/adamantite/ore_adamant16.png", "resources/Images/ore/adamantite/ore_adamant128.png"} , 0.00025, true, ResourceType.ADAMANTITE_ORE),
	TITANIUM ( 100, new String[] {"resources/Images/ore/titanium/ore_titanium16.png", "resources/Images/ore/titanium/ore_titanium128.png"} , 0.0000625, true, ResourceType.TITANIUM_ORE)
	;
	
	
	private MipMap mipmap;
    private int yield;
    private double rarity;
    private boolean isRare;
    private ResourceType resourceType;
    
	Ore(int y, String[] s, double r, boolean rare, ResourceType resourceType){
		yield = y;
		rarity = r;
		isRare = rare;
		mipmap = new MipMap(s);
		this.resourceType = resourceType;
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
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	
	
}
