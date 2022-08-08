package ui;

public enum MapMode {

	TERRAIN_BIG, // this image is twice as big to have offset pixels
	TERRAIN, // Used for texture for 3d terrain
	HEIGHT(true),
	HUMIDITY(true),
	PRESSURE(true),
	TEMPURATURE(true),
	FLOW(true),
	PRESSURE2(true),
	LIGHT(true),
	LIGHT_BIG(false),
	FLOW2,
	
	/** MINIMAP must be the last MapMode*/
	MINIMAP;
	
	public static final MapMode[] HEATMAP_MODES = new MapMode[] {HEIGHT, HUMIDITY, PRESSURE, TEMPURATURE, FLOW, PRESSURE2};
	
	private boolean heatmaptype;
	private MapMode() {
		this(false);
	}
	private MapMode(boolean heatmaptype) {
		this.heatmaptype = heatmaptype;
	}
	
	public boolean isHeatMapType() {
		return heatmaptype;
	}
}
