package wildlife;

import java.util.*;

import game.*;
import ui.*;
import utils.Thing;
import world.Tile;

public class Dragon extends Animal {
	
	private Tile home;
	private int timeToFireball;
	private int timeToHunt;
	
	public Dragon(Tile tile, boolean isPlayerControlled) {
		super(Game.unitTypeMap.get("DRAGON"), tile, isPlayerControlled);
		this.home = tile;
		resetTimeToFireball();
		resetTimeToHunt();
	}
	
	public Tile getHome() {
		return home;
	}
	public void setHome(Tile tile) {
		home = tile;
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public void updateState() {
		super.updateState();
		if(timeToFireball > 0) {
			timeToFireball --;
		}
		if(timeToHunt > 0) {
			timeToHunt --;
		}
		
	}
	public void resetTimeToFireball() {
		timeToFireball = getType().getCombatStats().getAttackSpeed()*10;
	}
	public void resetTimeToHunt() {
		timeToHunt = getType().getCombatStats().getMoveSpeed()*100;
	}
	public boolean readyToFireball() {
		return timeToFireball <= 0;
	}
	public boolean readyToHunt() {
		return timeToHunt <= 0;
	}
	public int getTimeToHunt() {
		return timeToHunt;
	}
	@Override
	public double attack(Thing other) {
		return super.attack(other);
	}

	@Override
	public boolean getHasHome() {
		return true;
	}
	@Override
	public boolean wantsToAttack() {
		return readyToHunt();
	}
	public void goHome() {
		this.setTargetTile(home);
	}
	
	public void moveAroundTarget() {
		if(this.getTarget() == null) {
			return;
		}
		if(!this.readyToMove()) {
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
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		//chance to attack either wildlife or player
//		if(Math.random() < 0.95) {
			for (Unit a : units) {
				if (a != this) {
					setTarget(units.get((int) (Math.random() * units.size())));
					resetTimeToHunt();
					return;
				}
			}
//		} else {
			
			//chance to attack building or unit
//			if(Math.random() < 0.4) {
//				for (Building b : buildings) {
//					if (buildings.size() > 0) {
//						Building targetBuilding = buildings.get((int) (Math.random() * buildings.size()));
//						if(targetBuilding.getBuildingType() != BuildingType.CASTLE) {
//							setTarget(targetBuilding);
//						}
//						return;
//					}
//				}
//			}else {
//				for (Unit u : units) {
//					if (u.isPlayerControlled()) {
//						setTarget(u);
//						return;
//					}
//				}
//			}
			
			
//		}
		
		return;
	}
	@Override 
	public List<String> getDebugStrings(){
		List<String> strings = super.getDebugStrings();
//		strings.add(String.format("TTS=%.1f", getTimeToSleep()));
		return strings;
	}
	
}
