package world;
import java.awt.*;

import javax.swing.*;

import game.ItemType;
import utils.*;

public enum ResourceType implements HasImage {
	
	COPPER (100 , 4, 15, false, ItemType.COPPER_ORE, true, 3, "BRONZE_WORKING",
			new String[] {"resources/Images/resources/copper/ore_copper128.png"}),
	SILVER ( 100 , 5, 5, false, ItemType.SILVER_ORE, true, 3, "BRONZE_WORKING",
			new String[] {"resources/Images/resources/silver/ore_silver128.png"}),
	
	COAL ( 100 , 4, 50, false, ItemType.COAL, true, 2, "IRON_WORKING",
			new String[] {"resources/Images/resources/ore_coal128.png"}),
	IRON ( 100, 4, 10, false, ItemType.IRON_ORE, true, 4, "IRON_WORKING",
			new String[] {"resources/Images/resources/iron/ore_iron128.png"}),
	
	MITHRIL ( 100, 3, 7, false, ItemType.MITHRIL_ORE, true, 5, "ARMORING",
			new String[] {"resources/Images/resources/mithril/ore_mithril128.png"} ),
	GOLD ( 100, 2, 6, true, ItemType.GOLD_ORE, true, 6, "ARMORING",
			new String[] {"resources/Images/resources/gold/ore_gold128.png"} ),
	ADAMANTITE ( 100, 1, 15, true, ItemType.ADAMANTITE_ORE, true, 7, "ARMORING",
			new String[] {"resources/Images/resources/adamantite/ore_adamant128.png"} ),
	
	RUNITE ( 100 , 1, 10, true, ItemType.RUNITE_ORE, true, 8,"ENGINEERING",
			new String[] {"resources/Images/resources/runite/ore_rune128.png"}),
	TITANIUM ( 100, 1, 8, true, ItemType.TITANIUM_ORE, true, 10, "ENGINEERING",
			new String[] {"resources/Images/resources/titanium/ore_titanium128.png"} ),
	
	;
	
	
	private MipMap mipmap;
	private int remainingEffort;
	private double numVeins;
	private boolean isRare;
	private ItemType itemType;
	private boolean isOre;
	private double timeToHarvest;
	private int veinSize;
	private String research;

	ResourceType(int y, double numVeins, int veinSize, boolean rare, ItemType itemType, boolean isOre, double timeToHarvest, String research, String[] images){
		remainingEffort = y;
		this.numVeins = numVeins;
		this.isRare = rare;
		this.veinSize = veinSize;
		mipmap = new MipMap(images);
		this.itemType = itemType;
		this.isOre = isOre;
		this.timeToHarvest = timeToHarvest;
		this.research = research;
	}
	
	public String getResearchRequirement() {
		return research;
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
	public Image getHighlight(int size) {
		return mipmap.getHighlight(size);
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
