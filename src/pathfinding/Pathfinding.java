package pathfinding;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Pathfinding {
	
	public static LinkedList<Tile> getBestPath(Unit unit, Tile startingTile, Tile targetTile) {
		LinkedList<Tile> bestTilePath = new LinkedList<Tile>();
		if(startingTile == targetTile) {
			bestTilePath.add(startingTile);
			return bestTilePath;
		}
		if(startingTile.getLocation().distanceTo(targetTile.getLocation()) == 1) {
			bestTilePath.add(targetTile);
			return bestTilePath;
		}
		PriorityQueue<Node> search = new PriorityQueue<>((x, y) -> {
			int speed = unit.getUnitType().getCombatStats().getMoveSpeed();
			// if speed is 0 then A* pathfinding will look through entire map
			speed = speed <= 0 ? 1 : speed;
			double xcost = x.cost + x.tile.getLocation().distanceTo(targetTile.getLocation())*speed;
			double ycost = y.cost + y.tile.getLocation().distanceTo(targetTile.getLocation())*speed;
			if (ycost < xcost) {
				return 1;
			} else if (ycost > xcost) {
				return -1;
			} else {
				return 0;
			}
		});
		PriorityQueue<Node> reverseSearch = new PriorityQueue<>((x, y) -> {
			int speed = unit.getUnitType().getCombatStats().getMoveSpeed();
			// if speed is 0 then A* pathfinding will look through entire map
			speed = speed <= 0 ? 1 : speed;
			double xcost = x.cost + x.tile.getLocation().distanceTo(startingTile.getLocation())*speed;
			double ycost = y.cost + y.tile.getLocation().distanceTo(startingTile.getLocation())*speed;
			if (ycost < xcost) {
				return 1;
			} else if (ycost > xcost) {
				return -1;
			} else {
				return 0;
			}
		});
		HashMap<Tile, Node> visited = new HashMap<>();
		HashMap<Tile, Node> reverseVisited = new HashMap<>();
		
		Node startingNode = new Node(startingTile, null, 0);
		search.add(startingNode);
		visited.put(startingNode.tile, startingNode);
		
		Node endingNode = new Node(targetTile, null, 0);
		reverseSearch.add(endingNode);
		reverseVisited.put(endingNode.tile, endingNode);
		
		Tile bestTile = startingTile;
		Tile meetingPoint = startingTile;
		double bestForwardCost = Double.MAX_VALUE;
		double bestReverseCost = Double.MAX_VALUE;
		
		boolean forwardDone = false;
		boolean reverseDone = !Game.USE_BIDIRECTIONAL_A_STAR;
		

		Node bestNode = null;
		Node bestNodeReverse = null;
		boolean reverseFinish = true;
		while (true) {
			if(search.isEmpty() || reverseSearch.isEmpty()) {
				break;
			}
			if(forwardDone && reverseDone) {
				break;
			}
			if(!forwardDone) {
				Node currentNode = search.remove();
				if(currentNode.cost >= bestForwardCost) {
					// If the next cheapest path is already worse than the best, nothing left to do.
					forwardDone = true;
					search.add(currentNode);
				}
				if(!forwardDone) {
					Node[] results = processNode(unit, currentNode, targetTile, visited, reverseVisited, search, true);
					if(results != null) {
						if(currentNode.tile != meetingPoint) {
							reverseDone = !Game.USE_BIDIRECTIONAL_A_STAR;
						}
						bestForwardCost = results[0].cost;
						bestTile = results[0].tile;
						bestReverseCost = results[1].cost;
						bestNode= currentNode;
						bestNodeReverse = results[2];
						reverseFinish = false;
					}
				}
			}
			if(!reverseDone) {
				Node currentNode = reverseSearch.remove();
				if(currentNode.cost >= bestReverseCost) {
					// If the next cheapest path is already worse than the best, nothing left to do.
					reverseDone = true;
					reverseSearch.add(currentNode);
				}
				if(!reverseDone) {
					Node[] results = processNode(unit, currentNode, startingTile, reverseVisited, visited, reverseSearch, false);
					if(results != null) {
						if(currentNode.tile != meetingPoint) {
							forwardDone = false;
						}
						bestForwardCost = results[0].cost;
						bestTile = results[0].tile;
						bestReverseCost = results[1].cost;
						bestNode = currentNode;
						bestNodeReverse = results[2];
						reverseFinish = true;
					}
				}
			}
		}
		Node prev = bestNode;
		if(prev == null) {
			return null;
		}
		while(prev.previous != null) {
			if(reverseFinish) {
				bestTilePath.addLast(prev.tile);
			}
			else {
				bestTilePath.addFirst(prev.tile);
			}
			prev = prev.previous;
		}
		prev = bestNodeReverse;
		boolean skip = true;
		while(prev.previous != null) {
			if(!skip) {
				if(reverseFinish) {
					bestTilePath.addFirst(prev.tile);
				}
				else {
					bestTilePath.addLast(prev.tile);
				}
			}
			skip = false;
			prev = prev.previous;
		}
		bestTilePath.add(targetTile);
		return bestTilePath;
	}
	private static Node[] processNode(Unit unit, Node currentNode, Tile targetTile, HashMap<Tile, Node> visited, HashMap<Tile, Node> reverseVisited, PriorityQueue<Node> search, boolean forward) {
		Tile currentTile = currentNode.tile;
		if(reverseVisited.containsKey(currentTile)) {
			if(forward) {
				Node parent = currentNode;
				Node child = parent.previous;
				while(child != null && child.previous != null) {
					parent = child;
					child = parent.previous;
				}
				Tile bestTile = parent.tile;
				return new Node[] {
						new Node(bestTile, null, currentNode.cost),
						new Node(bestTile, null, reverseVisited.get(currentTile).cost),
						reverseVisited.get(currentTile)
					};
			}
			else {
				Tile bestTile = currentNode.previous.tile;
				if(reverseVisited.get(currentTile).tile != targetTile) {
					Node parent = reverseVisited.get(currentTile);
					Node child = parent.previous;
					while(child != null && child.previous != null) {
						parent = child;
						child = parent.previous;
					}
					bestTile = parent.tile;
				}
				return new Node[] {
					new Node(bestTile, null, reverseVisited.get(currentTile).cost),
					new Node(bestTile, null, currentNode.cost),
					reverseVisited.get(currentTile)
				};
			}
		}
		
		for (Tile neighbor : currentTile.getNeighbors()) {
			if(neighbor.isBlocked(unit)) {
				continue;
			}
			double tiledamage = neighbor.computeTileDamage(unit);
			double cost = currentNode.cost;
			double movePenalty = 0;
			if(forward) {
				movePenalty = unit.movePenaltyTo(currentTile, neighbor);
			}
			else {
				movePenalty = unit.movePenaltyTo(neighbor, currentTile);
			}
			cost += movePenalty;
			if(tiledamage >= 1) {
				cost += tiledamage*movePenalty*10;
			}
			// if tile not visited, or the cost can be improved
			if(!visited.containsKey(neighbor) || cost < visited.get(neighbor).cost) {
				Node newNode = new Node(neighbor, currentNode, cost);
				visited.put(neighbor, newNode);
				search.add(newNode);
			}
		}
		return null;
	}
}
