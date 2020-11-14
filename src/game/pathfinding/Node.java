package game.pathfinding;

import world.*;

public class Node {
	Tile tile;
	Node previous;
	double cost;
	public Node(Tile tile, Node previous, double cost) {
		this.tile = tile;
		this.previous = previous;
		this.cost = cost;
	}
}
