package wildlife;

import java.util.*;

import game.*;
import utils.Thing;
import world.Tile;

public class Dragon extends Animal {
	
	private Tile home;
	private int timeToFireball;
	private int timeToSleep;
	
	public Dragon(Tile tile, boolean isPlayerControlled) {
		super(UnitType.DRAGON, tile, isPlayerControlled);
		home = tile;
		this.timeToFireball = UnitType.DRAGON.getCombatStats().getAttackSpeed()*10;
		this.timeToSleep = UnitType.DRAGON.getCombatStats().getMoveSpeed()*100;
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
	public void tick() {
		super.tick();
		if(timeToFireball > 0) {
			timeToFireball --;
		}
		if(timeToSleep > 0) {
			timeToSleep --;
		}
		
	}
	public void resetTimeToFireball() {
		timeToFireball = UnitType.DRAGON.getCombatStats().getAttackSpeed()*10;
	}
	public void resetTimeToSleep() {
		timeToSleep = UnitType.DRAGON.getCombatStats().getMoveSpeed()*100;
	}
	public boolean readyToFireball() {
		return timeToFireball <= 0;
	}
	public boolean readyToWakeUp() {
		return timeToSleep <= 0;
	}
	public int getTimeToSleep() {
		return timeToSleep;
	}
	@Override
	public double attack(Thing other) {
		return super.attack(other);
	}

	@Override
	public boolean wantsToReproduce() {
		return false;
	}

	@Override
	public boolean wantsToAttack() {
		return readyToWakeUp();
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
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		//chance to attack either wildlife or player
//		if(Math.random() < 0.95) {
			for (Animal a : animals) {
				if (a != null) {
					setTarget(animals.get((int) (Math.random() * animals.size())));
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
