package game.ai;

import game.Unit;

public interface Mission {
	
	boolean isComplete();
	boolean isPossible();
	boolean isStarted();
	boolean attempt(Unit unit);
	
	String toString();
}
