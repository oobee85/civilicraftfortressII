package wildlife;

import java.util.*;

import game.*;
import utils.*;
import world.*;

public class Parasite extends Animal {
	
	private boolean transformed;
	private Tile volcano;

	public Parasite(Tile tile, boolean isPlayerControlled, Tile volcano) {
		super(UnitType.PARASITE, tile, isPlayerControlled);
		this.volcano = volcano;
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}
	
	@Override
	public void updateState() {
		super.updateState();
		if(getTarget() != null) {
			if(getTarget().isDead() || !getTarget().isPlayerControlled()) {
				setTarget(null);
			}
		}
	}
	
	@Override
	public double attack(Thing other) {
		if(!transformed) {
			if(other instanceof Unit && inRange(other)) {
				Unit otherUnit = (Unit)other;
				transform(otherUnit);
			}
			return 0;
		}
		else {
			return super.attack(other);
		}
	}
	
	private void transform(Unit host) {
		host.setDead(true);
		this.setType(host.getType());
		this.setHealth(host.getHealth());
		this.setMaxHealth(host.getMaxHealth());
		transformed = true;
	}
	
	private void transformBack() {
		this.setType(UnitType.PARASITE);
		this.setHealth(1);
		this.setMaxHealth(1);
		transformed = false;
	}
	
	@Override
	public boolean takeDamage(double damage) {
		boolean lethal = super.takeDamage(damage);
		if(transformed) {
			if(lethal) {
				transformBack();
				lethal = false;
			}
		}
		return lethal;
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(!transformed) {
			for(Unit unit : units) {
				if(unit.isPlayerControlled()) {
					setTarget(unit);
					if(Math.random() < 0.2) {
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void chooseWhereToMove(World world) {
		if(!transformed) {
			super.chooseWhereToMove(world);
		}
		else {
			if(getTile() != volcano) {
				setTargetTile(volcano);
			}
		}
	}
}
