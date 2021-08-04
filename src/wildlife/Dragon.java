package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Dragon extends Animal {
	
	private transient Tile home;
	private transient int timeToHunt;
	
	public Dragon(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("DRAGON"), tile, faction);
		this.home = tile;
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
		if(timeToHunt > 0) {
			timeToHunt --;
		}
		
	}
	public void resetTimeToHunt() {
		timeToHunt = getType().getCombatStats().getMoveSpeed()*100;
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
		return false;
//		return readyToHunt();
	}
	
	@Override 
	public List<String> getDebugStrings(){
		List<String> strings = super.getDebugStrings();
//		strings.add(String.format("TTS=%.1f", getTimeToSleep()));
		return strings;
	}
	
}
