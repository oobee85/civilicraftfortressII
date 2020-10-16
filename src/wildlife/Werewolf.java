package wildlife;

import java.util.*;

import game.*;
import ui.*;
import utils.Thing;
import world.*;

public class Werewolf extends Animal {

	boolean transformed = false;
	
	public Werewolf(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("WEREWOLF"), tile, faction);
	}

	private void transformBack(Unit host) {
		host.setDead(true);
		this.setType(host.getType());
		this.setHealth(host.getHealth());
		this.setMaxHealth(host.getMaxHealth());
		transformed = false;
	}
	
	private void transform() {
		this.setType(Game.unitTypeMap.get("WEREWOLF"));
//		this.setHealth(UnitType.WEREWOLF.getCombatStats().getHealth());
		this.setMaxHealth(getType().getCombatStats().getHealth());
		transformed = true;
	}
	
	public void isTransformed() {
		
	}
	@Override
	public double attack(Thing other) {
		if(transformed) {
			if(other instanceof Unit && inRange(other)) {
				Unit otherUnit = (Unit)other;
				
			}
			return super.attack(other);
		}
		else {
			return super.attack(other);
		}
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(transformed) {
			for(Unit unit : units) {
				if(unit.getFaction() == World.PLAYER_FACTION) {
					setTarget(unit);
				}
			}
		}
	}
}
