package utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TiledMipmap {
	private final HashMap<Integer, MipMap> mipmaps;
	
	public TiledMipmap(String imagepath) {
		this.mipmaps = loadTiledImages(imagepath);
	}
	
	public MipMap get(int tileBitmap) {
		return mipmaps.get(tileBitmap);
	}
	

	private static final HashMap<Integer, MipMap> loadTiledImages(String tiledImageFolder) {

		HashMap<Integer, MipMap> tiledImages = new HashMap<>();
		
		for (int i = 1; i < Utils.MAX_TILED_BITMAP; i+=2) {
			int bits = i;

			boolean northwest = (bits & Direction.NORTHWEST.tilingBit) != 0;
			boolean northeast = (bits & Direction.NORTHEAST.tilingBit) != 0;
			boolean southeast = (bits & Direction.SOUTHEAST.tilingBit) != 0;
			boolean southwest = (bits & Direction.SOUTHWEST.tilingBit) != 0;
			boolean mirrored = false;
			if ((northwest && !northeast)
						|| ((northwest == northeast) && (southwest && !southeast))) {
	            mirrored = true;
	            bits = (bits & Direction.NONE.tilingBit)
	            		| (bits & Direction.NORTH.tilingBit)
	            		| (northwest ? Direction.NORTHEAST.tilingBit : 0)
	            		| (southwest ? Direction.SOUTHEAST.tilingBit : 0)
	            		| (bits & Direction.SOUTH.tilingBit)
	            		| (southeast ? Direction.SOUTHWEST.tilingBit : 0)
	            		| (northeast ? Direction.NORTHWEST.tilingBit : 0);
			}
			
			String filename = tiledImageFolder + "/" + bits + ".png";
			Image tiledImage = Utils.loadImage(filename);
			
			if (mirrored) {
				BufferedImage buf = Utils.toBufferedImage(tiledImage, false);
				
				BufferedImage mirroredImage = new BufferedImage(buf.getWidth(), buf.getHeight(), buf.getType());
				Graphics g = mirroredImage.getGraphics();
				g.drawImage(buf, buf.getWidth(), 0, -buf.getWidth(), buf.getHeight(), null);
				g.dispose();
				
				tiledImage = mirroredImage;
			}
			tiledImages.put(i, new MipMap(tiledImage));
		}
		return tiledImages;
	}
}
