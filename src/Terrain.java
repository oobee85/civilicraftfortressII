import java.awt.*;

public enum Terrain {
	    GRASS (10, new String[] {"resources/Images/grass/grass16.png", "resources/Images/grass/grass128.png", "resources/Images/grass/grass512.png"}),
	    DIRT  (8, new String[] {"resources/Images/dirt/dirt16.png", "resources/Images/dirt/dirt128.png", "resources/Images/dirt/dirt512.png"}),
	    LAVA  (0, new String[] {"resources/Images/lava/lava16.png", "resources/Images/lava/lavaanim32.gif", "resources/Images/lava/lava128.gif", "resources/Images/lava/lava512.png"}),
	    VOLCANO (1, new String[] {"resources/Images/lava/volcano16.png", "resources/Images/lava/volcano128.png", "resources/Images/lava/magma512.png"}),
	    ROCK (3, new String[] {"resources/Images/mountain/rock16.png", "resources/Images/mountain/rock128.png"}),
	    SNOW (1, new String[] {"resources/Images/mountain/snow16.png", "resources/Images/mountain/snow128.png"}),
	    WATER (0, new String[] {"resources/Images/water/water16.png", "resources/Images/water/water128.png", "resources/Images/water/water512.png"}),
	    FOREST1 (5, new String[] {"resources/Images/forest/tree1.png"}),
	    FOREST2 (5, new String[] {"resources/Images/forest/tree2.png"})
	    ;
	
	    private final int moveSpeed;   
	    
	    private final Image[] mipmaps;
	    private final int[] mipmapSizes;
	    
	    
	    Terrain(int speed, String[] s) {
	        this.moveSpeed = speed;
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
	    
	    private int moveSpeed() {
	    	return moveSpeed; 
	    }
	    
	    public boolean isBuildable(Terrain t) {
	    	if(t==Terrain.VOLCANO || t==Terrain.LAVA || t==Terrain.SNOW || t==Terrain.WATER) {
	    		return false;
	    	}
	    	return true;
	    	
	    }
	    public boolean isPlantable(Terrain t) {
	    	if(t==Terrain.VOLCANO || t==Terrain.LAVA || t==Terrain.SNOW || t==Terrain.WATER || t==Terrain.ROCK) {
	    		return false;
	    	}
	    	return true;
	    	
	    }
	    public boolean isBridgeable(Terrain t) {
	    	if(t==Terrain.WATER) {
	    		return true;
	    	}
	    	return false;
	    	
	    }
	    
	}
	
	

