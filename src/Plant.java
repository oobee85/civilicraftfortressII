import java.awt.Image;

public enum Plant {
	
	BERRY ( 100, new String[] {"Images/plants/berry16.png", "Images/plants/berry128.png"} , 1.0),
	BERRY_DEPLETED ( 0, new String[] {"Images/plants/berry_depleted16.png", "Images/plants/berry_depleted128.png"} , 0.2),
	CATTAIL ( 100, new String[] {"Images/plants/cattail32.png"} , 1.0),
	;
	
	
	private final Image[] mipmaps;
    private final int[] mipmapSizes;
    private int yield;
    private double rarity;
    
	Plant(int y, String[] s, double r){
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
