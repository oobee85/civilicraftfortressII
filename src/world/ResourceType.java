package world;

import game.ItemType;
import utils.*;

public enum ResourceType {
	
	COPPER (100 , 10, 10, false, ItemType.COPPER_ORE, true, 3, "FARMING",
			new String[] {"Images/resources/copper/ore_copper128.png"}),
	SILVER ( 100, 10, 5, false, ItemType.SILVER_ORE, true, 3, "FARMING",
			new String[] {"Images/resources/silver/ore_silver128.png"}),
	GOLD   ( 100, 5, 8, false, ItemType.GOLD_ORE, true, 6, "FARMING",
			new String[] {"Images/resources/gold/ore_gold128.png"} ),
	
	
	COAL ( 100 , 10, 5, false, ItemType.COAL, true, 2, "BRONZE_WORKING",
			new String[] {"Images/resources/ore_coal128.png"}),
	IRON ( 100, 5, 8, false, ItemType.IRON_ORE, true, 4, "BRONZE_WORKING",
			new String[] {"Images/resources/iron/ore_iron128.png"}),
	
	MITHRIL ( 100, 4, 8, true, ItemType.MITHRIL_ORE, true, 5, "IRON_WORKING",
			new String[] {"Images/resources/mithril/ore_mithril128.png"} ),
	
	ADAMANTITE (100, 2, 5, true, ItemType.ADAMANTITE_ORE, true, 7, "ARMORING",
			new String[] {"Images/resources/adamantite/ore_adamant128.png"} ),
	
	RUNITE  ( 100 , 1, 5, true, ItemType.RUNITE_ORE, true, 8,"CIVILIZATION",
			new String[] {"Images/resources/runite/ore_rune128.png"}),
	TITANIUM ( 100, 1, 3, true, ItemType.TITANIUM_ORE, true, 10, "CIVILIZATION",
			new String[] {"Images/resources/titanium/ore_titanium128.png"} ),
	
	;
	
	
	private MipMap mipmap;
	private int remainingEffort;
	private boolean isRare;
	private ItemType itemType;
	private boolean isOre;
	private double timeToHarvest;
	private double numVeins;
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

	public MipMap getMipMap() {
		return mipmap;
	}
}
