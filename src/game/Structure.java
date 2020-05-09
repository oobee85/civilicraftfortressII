package game;

import java.util.List;
import ui.*;
import utils.*;
import world.*;

public class Structure extends Thing {

	private StructureType structureType;
	private int culture;

	public Structure(StructureType structureType, Tile tile) {
		super(structureType.getHealth(), structureType, tile);
		this.structureType = structureType;
	}

	public void updateCulture() {
		culture += structureType.cultureRate;
	}

	public int getCulture() {
		return culture;
	}

	public StructureType getStructureType() {
		return structureType;
	}

	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("CT=%." + Game.NUM_DEBUG_DIGITS + "f", getCulture() * 1.0));
		return strings;
	}
}
