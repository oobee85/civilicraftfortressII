package wildlife;

import java.util.*;

import game.*;
import ui.*;
import utils.Thing;
import world.*;

public class Dragon extends Animal {
	
	private Tile home;
	private int timeToFireball;
	private int timeToHunt;
	
	public Dragon(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("DRAGON"), tile, faction);
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
	public boolean getHasHome() {
		return true;
	}
	@Override
	public boolean wantsToAttack() {
		return readyToHunt();
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
	public List<String> getDebugStrings(){
		List<String> strings = super.getDebugStrings();
//		strings.add(String.format("TTS=%.1f", getTimeToSleep()));
		return strings;
	}
	
}
