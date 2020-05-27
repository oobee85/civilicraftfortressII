package game;

import java.util.ArrayList;

import utils.MipMap;

public enum ResearchType {
	
	
	ALPHABET (100, "resources/Images/buildings/wall_wood.png", null),
	WRITING (100, "resources/Images/buildings/wall_wood.png", new ResearchType[] {ResearchType.ALPHABET}),
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
