package game;

import java.awt.*;
import javax.swing.*;

import utils.*;

public enum ResearchType implements HasImage {
	
	WARRIOR_CODE (100, "resources/Images/buildings/barracks256.png", new ResearchType[] {}),
	BRONZE_WORKING (100, "resources/Images/units/spearman.png", new ResearchType[] {}),
	WRITING (100, "resources/Images/research/writing.png", new ResearchType[] {}),
	WHEEL (100, "resources/Images/research/wheel.png", new ResearchType[] {}),
	WOODCUTTING (100, "resources/Images/buildings/sawmill.png", new ResearchType[] {}),
	
	IRON_WORKING (200, "resources/Images/research/iron_working.png", new ResearchType[] {ResearchType.WARRIOR_CODE, ResearchType.BRONZE_WORKING}),
	FARMING (200, "resources/Images/buildings/farm.png", new ResearchType[] {ResearchType.BRONZE_WORKING}),
	MASONRY (200, "resources/Images/buildings/wall_stone.png", new ResearchType[] {ResearchType.WHEEL, ResearchType.WOODCUTTING}),
	MYSTICISM (200, "resources/Images/research/mysticism.png",  new ResearchType[] {ResearchType.WOODCUTTING}),
	MATHEMATICS (200, "resources/Images/research/mathematics.png", new ResearchType[] {ResearchType.WRITING, ResearchType.WHEEL}),
	
	ARMORING (300, "resources/Images/research/armoring.png", new ResearchType[] {ResearchType.IRON_WORKING}),
	HORSEBACK_RIDING (300, "resources/Images/research/horseback_riding.png", new ResearchType[] {ResearchType.FARMING}),
	CURRENCY (300, "resources/Images/itemicons/gold.png", new ResearchType[] {ResearchType.FARMING, ResearchType.MATHEMATICS}),
	CONSTRUCTION (300, "resources/Images/buildings/wall_brick.png", new ResearchType[] {ResearchType.MASONRY, ResearchType.MATHEMATICS}),
	
	CHIVALRY (400, "resources/Images/units/knight.png", new ResearchType[] {ResearchType.ARMORING, ResearchType.HORSEBACK_RIDING}),
	CODE_OF_LAWS (400, "resources/Images/research/code_of_laws.png", new ResearchType[] {ResearchType.CURRENCY}),
	MONARCHY (400, "resources/Images/research/monarchy.png", new ResearchType[] {ResearchType.CONSTRUCTION, ResearchType.MYSTICISM}),
	;
	
	private ResearchType[] children;
	private MipMap mipmap;
	private int requiredRP;
	
	ResearchType(int researchPoints, String s, ResearchType[] rt){
		children = rt;
		mipmap = new MipMap(s);
		this.requiredRP = researchPoints;
	}
	public int getRequiredPoints() {
		return requiredRP;
	}
	
	public ResearchType[] getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}
	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}
	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}
	
	
}
