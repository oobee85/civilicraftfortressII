package world;
import java.awt.*;

import javax.swing.*;

import game.ItemType;
import utils.*;

public enum ResourceType implements HasImage {
	
	COPPER ( 100, new String[] {"resources/Images/resources/copper/ore_copper16.png", "resources/Images/resources/copper/ore_copper128.png"} , 0.002, false, ItemType.COPPER_ORE, true),
	SILVER ( 100, new String[] {"resources/Images/resources/silver/ore_silver16.png", "resources/Images/resources/silver/ore_silver128.png"} , 0.001, false, ItemType.SILVER_ORE, true),
	IRON ( 100, new String[] {"resources/Images/resources/iron/ore_iron16.png", "resources/Images/resources/iron/ore_iron128.png"}, 0.001, false, ItemType.IRON_ORE, true),
	
	MITHRIL ( 100, new String[] {"resources/Images/resources/mithril/ore_mithril16.png", "resources/Images/resources/mithril/ore_mithril128.png"} , 0.0005, false, ItemType.MITHRIL_ORE, true),
	GOLD ( 100, new String[] {"resources/Images/resources/gold/ore_gold16.png", "resources/Images/resources/gold/ore_gold128.png"} , 0.0005, true, ItemType.GOLD_ORE, true),
	ADAMANTITE ( 100, new String[] {"resources/Images/resources/adamantite/ore_adamant16.png", "resources/Images/resources/adamantite/ore_adamant128.png"} , 0.00025, true, ItemType.ADAMANTITE_ORE, true),
	
	RUNITE ( 100, new String[] {"resources/Images/resources/runite/ore_rune16.png", "resources/Images/resources/runite/ore_rune128.png"} , 0.000125, true, ItemType.RUNITE_ORE, true),
	TITANIUM ( 100, new String[] {"resources/Images/resources/titanium/ore_titanium16.png", "resources/Images/resources/titanium/ore_titanium128.png"} , 0.0000625, true, ItemType.TITANIUM_ORE, true)
	;
	
	
	private MipMap mipmap;
    private int yield;
    private double rarity;
    private boolean isRare;
    private ItemType resourceType;
    private boolean isOre;
    
	ResourceType(int y, String[] s, double r, boolean rare, ItemType resourceType, boolean isOre){
		yield = y;
		rarity = r;
		isRare = rare;
		mipmap = new MipMap(s);
		this.resourceType = resourceType;
		this.isOre = isOre;
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
	public boolean isOre() {
		return isOre;
	}
	public ItemType getResourceType() {
		return resourceType;
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
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
