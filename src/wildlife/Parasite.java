package wildlife;

import java.util.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class Parasite extends Animal {
	
	private transient boolean transformed;
	private transient Tile volcano;

	public Parasite(Tile tile, Faction faction, Tile volcano) {
		super(Game.unitTypeMap.get("PARASITE"), tile, faction);
		this.volcano = volcano;
	}
	
	@Override
	public void updateState() {
		super.updateState();
		if(getTarget() != null) {
			if(getTarget().isDead() || !getTarget().getFaction().isPlayer()) {
				clearPlannedActions();
			}
		}
	}
	
	@Override
	public boolean attack(Thing other) {
		if(!transformed) {
			if(other instanceof Unit && inRange(other)) {
				Unit otherUnit = (Unit)other;
				transform(otherUnit);
			}
			return true;
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
		this.setType(Game.unitTypeMap.get("PARASITE"));
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
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(!transformed) {
			super.chooseWhatToAttack(units, buildings);
		}
	}
	
	@Override
	public void chooseWhereToMove(World world) {
		if(!transformed) {
			super.chooseWhereToMove(world);
		}
		else {
			clearPlannedActions();
			queuePlannedAction(new PlannedAction(volcano));
		}
	}
}
