package world;
import java.awt.*;

import javax.swing.*;

import game.ItemType;
import utils.*;

public enum ResourceType implements HasImage {
	
	COAL ( 100, new String[] {"resources/Images/resources/ore_coal16.png", "resources/Images/resources/ore_coal128.png"} , 								4, 50, false, ItemType.COAL, true, 20),
	COPPER ( 100, new String[] {"resources/Images/resources/copper/ore_copper16.png", "resources/Images/resources/copper/ore_copper128.png"} , 			4, 15, false, ItemType.COPPER_ORE, true, 30),
	SILVER ( 100, new String[] {"resources/Images/resources/silver/ore_silver16.png", "resources/Images/resources/silver/ore_silver128.png"} ,			 5, 5, false, ItemType.SILVER_ORE, true, 30),
	IRON ( 100, new String[] {"resources/Images/resources/iron/ore_iron16.png", "resources/Images/resources/iron/ore_iron128.png"}, 						4, 10, false, ItemType.IRON_ORE, true, 40),
	
	MITHRIL ( 100, new String[] {"resources/Images/resources/mithril/ore_mithril16.png", "resources/Images/resources/mithril/ore_mithril128.png"} , 		3, 7, false, ItemType.MITHRIL_ORE, true, 50),
	GOLD ( 100, new String[] {"resources/Images/resources/gold/ore_gold16.png", "resources/Images/resources/gold/ore_gold128.png"} , 						2, 6, true, ItemType.GOLD_ORE, true, 60),
	ADAMANTITE ( 100, new String[] {"resources/Images/resources/adamantite/ore_adamant16.png", "resources/Images/resources/adamantite/ore_adamant128.png"} , 1, 15, true, ItemType.ADAMANTITE_ORE, true, 70),
	
	RUNITE ( 100, new String[] {"resources/Images/resources/runite/ore_rune16.png", "resources/Images/resources/runite/ore_rune128.png"} , 					1, 10, true, ItemType.RUNITE_ORE, true, 80),
	TITANIUM ( 100, new String[] {"resources/Images/resources/titanium/ore_titanium16.png", "resources/Images/resources/titanium/ore_titanium128.png"} , 	1, 8, true, ItemType.TITANIUM_ORE, true, 100),
	
	;
	
	
	private MipMap mipmap;
    private int remainingEffort;
    private double numVeins;
    private boolean isRare;
    private ItemType itemType;
    private boolean isOre;
    private double timeToHarvest;
    private int veinSize;
    
	ResourceType(int y, String[] s, double numVeins, int veinSize, boolean rare, ItemType itemType, boolean isOre, double timeToHarvest){
		remainingEffort = y;
		this.numVeins = numVeins;
		this.isRare = rare;
		this.veinSize = veinSize;
		mipmap = new MipMap(s);
		this.itemType = itemType;
		this.isOre = isOre;
		this.timeToHarvest = timeToHarvest;
	}
	
	public void expendEffort(double effort) {
		remainingEffort -= effort;
		if(remainingEffort < 0) {
			remainingEffort = 0;
		}
	}
	public int getVeinSize() {
		return veinSize;
	}
	public double getTimeToHarvest() {
		return timeToHarvest;
	}
	public int getRemainingEffort() {
		return remainingEffort;
	}
	public double getNumVeins() {
		return numVeins;
	}
	public boolean isRare() {
		return isRare;
	}
	public boolean isOre() {
		return isOre;
	}
	public ItemType getItemType() {
		return itemType;
	}


	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
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
