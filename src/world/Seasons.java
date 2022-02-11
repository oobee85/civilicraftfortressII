package world;

public class Seasons {
	
	
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
