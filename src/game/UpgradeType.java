package game;

import java.util.HashMap;

import utils.MipMap;
import utils.Utils;

public enum UpgradeType {

	WARRIOR_CODE (100, "resources/Images/buildings/wall_wood.png", new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,50); put(ItemType.HORSE,10);}}),
	;
	
	private MipMap mipmap;
	private int requiredRP;
	
	UpgradeType(int researchPoints, String s, HashMap<ItemType, Integer> resourcesNeeded){
		mipmap = new MipMap(s);
		this.requiredRP = researchPoints;
	}
	public int getRequirdPoints() {
		return requiredRP;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}

