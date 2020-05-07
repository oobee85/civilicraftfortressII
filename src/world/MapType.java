package world;

public enum MapType {
	PANGEA("Pangea"), 
	CONTINENTS("Continents"), 
	ARCHIPELAGO("Archipelago")
	;
	
	String name;
	private MapType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
