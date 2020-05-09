package world;

public enum MapType {
	PANGEA("Pangea", 0.5), 
	CONTINENTS("Continents", 0.6), 
	ARCHIPELAGO("Archipelago", 0.75)
	;
	
	String name;
	public double dirtLevel;
	private MapType(String name, double dirtLevel) {
		this.name = name;
		this.dirtLevel = dirtLevel;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
