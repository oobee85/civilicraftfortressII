package world;

public class Seasons {
	
	public static float getSeasonEnergy() {
		float season = (float) (1/2*Math.sin(Math.PI*World.ticks/World.SEASON_DURATION));
		return season;
	}
	public static float getEnergyChange() {
		
		// added value at the end... positive = summer more extreme... negative = winter more extreme
		float main = (float) (3600*(Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION)*(Math.sin(Math.PI*World.ticks/World.SEASON_DURATION)+1)/50));
		
		// does day/night cycle
		float main2 = (float) (Math.sin(Math.PI*World.ticks/World.SEASON_DURATION)*Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION));
		
		float combined = main + main2;
		//						| this value squishes curve vertically based on size... in summer(large value = low energy less extreme)
		//						V														in winter(large value = high energy less extreme)
		float offset = (float) (2000*Math.sin(Math.PI*World.ticks/World.SEASON_DURATION));
		float dEnergy = offset + combined;
		return dEnergy;
	}
	public static float getNightEnergy() {
		float night = (float) (5 * Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION));
		return night;
	}
	public static float getRateEnergy() {
		float day = (float) (4*Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION));
		float season = (float) (0.35*Math.sin(Math.PI*World.ticks/World.SEASON_DURATION)*(World.ticks/World.SEASON_DURATION) + World.ticks/(2*World.SEASON_DURATION));
		float main = day + season;
		return main;
	}
	
	// for migration
	public static double getSeason4migration() {
		return (World.ticks + World.SEASON_DURATION*1/2)%(World.SEASON_DURATION*2) / (double)World.SEASON_DURATION;
	}
}
