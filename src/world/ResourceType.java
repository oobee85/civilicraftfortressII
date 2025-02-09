package world;

import game.ItemType;
import utils.*;

public enum ResourceType {
	
	CLAY (200, 100 , 10, 5, false, 0, 100, ItemType.CLAY, true, 10, "FARMING",
			new String[] {"Images/resources/clay_ore.png"}),
	
	//		    numVeins  isRare?   maxHeight
	//   effort     Veinsize   minHeight                      ticksHarvest
	COPPER (200, 100 , 12, 10, false, 0, 250, ItemType.COPPER_ORE, true, 5, "FARMING",
			new String[] {"Images/resources/copper/ore_copper128.png"}),
	SILVER (200, 100, 12, 5, false, 0, 250, ItemType.SILVER_ORE, true, 5, "FARMING",
			new String[] {"Images/resources/silver/ore_silver128.png"}),
	GOLD   (200, 100, 5, 8, false, 0, 350, ItemType.GOLD_ORE, true, 10, "FARMING",
			new String[] {"Images/resources/gold/ore_gold128.png"} ),
	
	
	COAL (200, 100 , 10, 5, false, 0, 500, ItemType.COAL, true, 5, "BRONZE_WORKING",
			new String[] {"Images/resources/ore_coal128.png"}),
	IRON (200, 100, 5, 8, false, 0, 500, ItemType.IRON_ORE, true, 5, "BRONZE_WORKING",
			new String[] {"Images/resources/iron/ore_iron128.png"}),
	
	MITHRIL (200, 100, 4, 8, true, 350, 1000, ItemType.MITHRIL_ORE, true, 10, "IRON_WORKING",
			new String[] {"Images/resources/mithril/ore_mithril128.png"} ),
	
	ADAMANTITE (200, 100, 2, 5, true, 500, 800, ItemType.ADAMANTITE_ORE, true, 15, "ARMORING",
			new String[] {"Images/resources/adamantite/ore_adamant128.png"} ),
	
	RUNITE  (200, 100 , 1, 5, true, 500, 1000, ItemType.RUNITE_ORE, true, 15,"CIVILIZATION",
			new String[] {"Images/resources/runite/ore_rune128.png"}),
	TITANIUM (200, 100, 1, 3, true, 500, 1000, ItemType.TITANIUM_ORE, true, 15, "CIVILIZATION",
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
	private int minHeight;
	private int maxHeight;
	private int resourceLeft;

	ResourceType(int resourceLeft, int remainingEffort, double numVeins, int veinSize, boolean rare, int minHeightLevel, int maxHeightLevel, ItemType itemType, boolean isOre, double timeToHarvest, String research, String[] images){
		this.resourceLeft = resourceLeft;
		this.remainingEffort = remainingEffort;
		this.numVeins = numVeins;
		this.isRare = rare;
		this.veinSize = veinSize;
		mipmap = new MipMap(images);
		this.itemType = itemType;
		this.isOre = isOre;
		this.timeToHarvest = timeToHarvest;
		this.research = research;
		this.minHeight = minHeightLevel;
		this.maxHeight = maxHeightLevel;
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
	public int getResourceAmount() {
		return resourceLeft;
	}
	public void subtractRemainingAmount(int amount) {
		this.resourceLeft -= 1;
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
	public int getMinHeight() {
		return minHeight;
	}
	public int getMaxHeight() {
		return maxHeight;
	}
	public MipMap getMipMap() {
		return mipmap;
	}
}
