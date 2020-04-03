import java.awt.Image;

public enum Ore {
	
	ORE_IRON ( 100, new String[] {"Images/ore/ore_iron16.png", "Images/ore/ore_iron128.png"} , 0.6),
	ORE_COPPER ( 100, new String[] {"Images/ore/ore_copper16.png", "Images/ore/ore_copper128.png"} , 1.0),
	ORE_GOLD ( 100, new String[] {"Images/ore/ore_gold16.png", "Images/ore/ore_gold128.png"} , 0.1),
	ORE_SILVER ( 100, new String[] {"Images/ore/ore_silver16.png", "Images/ore/ore_silver128.png"} , 0.3),
	;
	
	
	private final Image[] mipmaps;
    private final int[] mipmapSizes;
    private int yield;
    private double rarity;
    
	Ore(int y, String[] s, double r){
		yield = y;
		rarity = r;
		
		mipmaps = new Image[s.length];
        mipmapSizes = new int[s.length];
        for(int i = 0; i < s.length; i++) {
        	mipmaps[i] = Utils.loadImage(s[i]);
        	mipmapSizes[i] = mipmaps[i].getWidth(null);
        }
	}
	
	public Image getImage(int size) {
    	// Get the first mipmap that is larger than the tile size
    	for(int i = 0; i < mipmapSizes.length; i++) {
    		if(mipmapSizes[i] > size) {
    			return mipmaps[i];
    		}
    	}
    	return mipmaps[mipmaps.length-1];
    }
	
	public int getYield() {
		return yield;
	}
	public double getRarity() {
		return rarity;
	}
	
	
	
}
