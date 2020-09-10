package game;

import java.awt.*;
import javax.swing.*;

import utils.*;

public enum ResearchType implements HasImage {
	
	WARRIOR_CODE (100, "resources/Images/research/warrior_code.png", new ResearchType[] {}, 1),
	BRONZE_WORKING (100, "resources/Images/research/bronze_working.png", new ResearchType[] {}, 1),
	WRITING (100, "resources/Images/research/writing.png", new ResearchType[] {}, 1), 
	WHEEL (100, "resources/Images/research/wheel.png", new ResearchType[] {}, 1),
	
	IRON_WORKING (200, "resources/Images/research/iron_working.png", new ResearchType[] {ResearchType.WARRIOR_CODE, ResearchType.BRONZE_WORKING}, 2),
	FARMING (200, "resources/Images/research/farming.png", new ResearchType[] {ResearchType.BRONZE_WORKING}, 2),
	MATHEMATICS (200, "resources/Images/research/mathematics.png", new ResearchType[] {ResearchType.WRITING, ResearchType.WHEEL}, 2),
	MASONRY (200, "resources/Images/research/masonry.png", new ResearchType[] {ResearchType.WHEEL}, 2),
	
	ARMORING (300, "resources/Images/research/armoring.png", new ResearchType[] {ResearchType.IRON_WORKING}, 3),
	HORSEBACK_RIDING (300, "resources/Images/research/horseback_riding.png", new ResearchType[] {ResearchType.FARMING, ResearchType.IRON_WORKING}, 3),
	CURRENCY (300, "resources/Images/itemicons/gold.png", new ResearchType[] {ResearchType.FARMING, ResearchType.MATHEMATICS}, 3),
	ENGINEERING (300, "resources/Images/research/engineering.png", new ResearchType[] {ResearchType.MASONRY, ResearchType.MATHEMATICS}, 3),
	MYSTICISM (300, "resources/Images/research/mysticism.png",  new ResearchType[] {ResearchType.MASONRY}, 3),
	
	CHIVALRY (400, "resources/Images/research/chivalry.png", new ResearchType[] {ResearchType.ARMORING, ResearchType.HORSEBACK_RIDING}, 3),
	MONARCHY (400, "resources/Images/research/monarchy.png", new ResearchType[] {ResearchType.ENGINEERING, ResearchType.CURRENCY, ResearchType.MYSTICISM}, 3),
	;
	
	private ResearchType[] requiredResearch;
	private MipMap mipmap;
	private int requiredRP;
	private int tier;
	
	ResearchType(int researchPoints, String s, ResearchType[] rt, int tier){
		requiredResearch = rt;
		mipmap = new MipMap(s);
		this.requiredRP = researchPoints;
		this.tier = tier;
	}
	public int getRequiredPoints() {
		return requiredRP;
	}
	public int getTier() {
		return tier;
	}
	
	public ResearchType[] getRequiredResearch() {
		return requiredResearch;
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
