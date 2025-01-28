package game.components;

import java.util.*;

import game.BuildingType;

public class Builder extends GameComponent {

	private String[] buildingTypeNames;
	private Set<BuildingType> buildingTypes;

	public Builder(String[] buildingTypeNames) {
		this.buildingTypeNames = buildingTypeNames;
		buildingTypes = new HashSet<BuildingType>();
	}

	private Builder(Set<BuildingType> buildingTypes) {
		this.buildingTypes = new HashSet<BuildingType>();
		this.buildingTypes.addAll(buildingTypes);
	}
	
	public String[] getBuildingTypeNames() {
		return buildingTypeNames;
	}
	
	public Set<BuildingType> getBuildingTypeSet() {
		return this.buildingTypes;
	}

	@Override
	public GameComponent instance() {
		return new Builder(this.buildingTypes);
	}
}
