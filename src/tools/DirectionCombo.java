package tools;

import utils.Direction;

public class DirectionCombo {
	public final int bitmap;
	public final int numActive;
	
	public final boolean reachesAcross;
	
	public DirectionCombo(int bitmap) {
		this.bitmap = bitmap;
		int numActive = 0;
		for (Direction d : Direction.values()) { 
			if (isPresent(d)) {
				numActive++;
			}
		}
		this.numActive = numActive;
		
		boolean reachesAcross = false;
		int firstGap = -1;
		int gap = 0;
		for (Direction d : Direction.values()) {
			if (d == Direction.NONE) { 
				continue;
			}
			if (!isPresent(d)) {
				gap++;
				continue;
			}
			if (firstGap == -1) {
				firstGap = gap;
			}
			else {
				System.out.println("Gap: " + gap);
				if (gap >= 1 && gap <= 2) { 
					reachesAcross = true;
				}
			}
			gap = 0;
		}
		firstGap += gap;
		if (firstGap >= 1 && firstGap <= 2) { 
			reachesAcross = true;
		}
		System.out.println("Final Gap: " + firstGap);
		for (Direction d : Direction.values()) {
			if (d == Direction.NONE) { 
				continue;
			}
			Direction opposite = Direction.values()[(d.ordinal() + 3) % 6];
			if (isPresent(d) && isPresent(opposite)) {
				reachesAcross = true;
			}
		}
		this.reachesAcross = reachesAcross;
	}
	
	public boolean isPresent(Direction direction) {
		if (direction == Direction.NONE) { 
			return bitmap == 0;
		}
		return ((bitmap & (1 << direction.ordinal())) != 0);
	}
	
	@Override
	public String toString() {
		if (bitmap == 0) {
			return Direction.NONE.name();
		}
		String s = "";
		for (Direction d : Direction.values()) {
			if (d == Direction.NONE) { 
				continue;
			}
			if (isPresent(d)) {
				s += d.getShortName();
			}
		}
		return s;
	}
}
