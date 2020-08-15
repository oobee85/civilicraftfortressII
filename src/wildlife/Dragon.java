package wildlife;

import java.util.*;

import game.*;
import world.Tile;

public class Dragon extends Animal {

	public Dragon(Tile tile, boolean isPlayerControlled) {
		super(UnitType.DRAGON, tile, isPlayerControlled);
	}

	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToReproduce() {
		return false;
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	public void moveAroundTarget() {
		if(this.getTarget() == null) {
			return;
		}
		Tile t = this.getTarget().getTile();
		if(t == null) {
			return;
		}
		Tile target = this.getTile();
		target = t.getNeighbors().get((int) (Math.random()*t.getNeighbors().size()));
		this.moveTowards(target);
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		//chance to attack either wildlife or player
		if(Math.random() > 0.05) {
			for (Animal a : animals) {
				if (a != null) {
					setTarget(animals.get((int) (Math.random() * animals.size())));
					return;
				}
			}
		} else {
			
			//chance to attack building or unit
			if(Math.random() > 0.4) {
				for (Building b : buildings) {
					if (buildings.size() > 0) {
						setTarget(buildings.get((int) (Math.random() * buildings.size())));
						return;
					}
				}
			}else {
				for (Unit u : units) {
					if (u.isPlayerControlled()) {
						setTarget(u);
						return;
					}
				}
			}
			
			
		}
		
		return;
	}
	
	
}
