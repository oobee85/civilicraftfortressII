package ui;

public enum MapMode {
	
	TERRAIN, HEIGHT, HUMIDITY, PRESSURE, TEMPURATURE, FLOW, PRESSURE2, LIGHT, 
	
	// MINIMAP must be last
	MINIMAP;
	
	public static final MapMode[] HEATMAP_MODES = new MapMode[] {HEIGHT, HUMIDITY, PRESSURE, TEMPURATURE, FLOW, PRESSURE2};
}
