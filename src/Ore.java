import java.awt.Image;

public enum Ore {
	
	ORE_IRON ( 100, new String[] {"resources/Images/ore/iron/ore_iron16.png", "resources/Images/ore/iron/ore_iron128.png"}, 1.0),
	ORE_COPPER ( 100, new String[] {"resources/Images/ore/copper/ore_copper16.png", "resources/Images/ore/copper/ore_copper128.png"} , 0.6),
	ORE_GOLD ( 100, new String[] {"resources/Images/ore/gold/ore_gold16.png", "resources/Images/ore/gold/ore_gold128.png"} , 0.1),
	ORE_SILVER ( 100, new String[] {"resources/Images/ore/silver/ore_silver16.png", "resources/Images/ore/silver/ore_silver128.png"} , 0.3),
	;
	
	
	private MipMap mipmap;
    private int yield;
    private double rarity;
    
	Ore(int y, String[] s, double r){
		yield = y;
		rarity = r;
		
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
	
	
	
}
