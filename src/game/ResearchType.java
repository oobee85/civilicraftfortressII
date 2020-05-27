package game;

import java.util.ArrayList;

import utils.MipMap;

public enum ResearchType {
	
	
	WARRIOR_CODE (100, "resources/Images/buildings/wall_wood.png", null),
	BRONZE_WORKING (100, "resources/Images/buildings/wall_wood.png", null),
	WRITING (100, "resources/Images/buildings/wall_wood.png", null),
	WHEEL (100, "resources/Images/buildings/wall_wood.png", null),
	WOODCUTTING (100, "resources/Images/buildings/wall_wood.png", null),
	
	IRON_WORKING (200, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.WARRIOR_CODE, ResearchType.BRONZE_WORKING}),
	FARMING (200, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.BRONZE_WORKING}),
	MASONRY (200, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.WHEEL, ResearchType.WOODCUTTING}),
	MYSTICISM (200, "resources/Images/buildings/wall_wood.png",  new ResearchType[] {ResearchType.WOODCUTTING}),
	MATHEMATICS (200, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.WRITING, ResearchType.WHEEL}),
	
	ARMORING (300, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.IRON_WORKING}),
	HORSEBACK_RIDING (300, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.FARMING}),
	CURRENCY (300, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.FARMING, ResearchType.MATHEMATICS}),
	CONSTRUCTION (300, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.MASONRY, ResearchType.MATHEMATICS}),
	
	CHIVALRY (400, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.ARMORING, ResearchType.HORSEBACK_RIDING}),
	CODE_OF_LAWS (400, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.CURRENCY}),
	MONARCHY (400, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.CONSTRUCTION, ResearchType.MYSTICISM}),
	;
	
	private ResearchType[] children;
	private MipMap mipmap;
	private String name;
	private int requiredRP;
	
	ResearchType(int researchPoints, String s, ResearchType[] rt){
		children = rt;
		mipmap = new MipMap(s);
		this.requiredRP = researchPoints;
		name = this.name().toLowerCase().replace('_', ' ');
	}
	public int getRequirdPoints() {
		return requiredRP;
	}
	public ResearchType[] getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}
