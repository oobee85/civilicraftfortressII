package game.ai;

import game.Unit;
import world.Tile;

public interface TileSelector {

	Tile selectTile(Unit unit);
}
