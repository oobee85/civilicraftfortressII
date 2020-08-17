package wildlife;

import java.util.*;

import game.*;
import utils.*;
import world.*;

public class Parasite extends Animal {

	public Parasite(Tile tile, boolean isPlayerControlled) {
		super(UnitType.PARASITE, tile, isPlayerControlled);
	}
	@Override
	public boolean isFireResistant() {
		return true;
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
	public void tick() {
		super.tick();
		if(getTarget() != null) {
			if(getTarget().isDead() || !getTarget().isPlayerControlled()) {
				setTarget(null);
			}
		}
	}
	
	@Override
	public double attack(Thing other) {
		if(inRange(other)) {
			other.setPlayerControlled(false);
			this.setHealth(0);
		}
		return 0;
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		for(Unit unit : units) {
			if(unit.isPlayerControlled()) {
				setTarget(unit);
				if(Math.random() < 0.2) {
					return;
				}
			}
		}
		return;
	}
	
	

}
