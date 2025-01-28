package world;

public class Seasons {
	
	public static float getNightEnergy() {
		float night = (float) (5 * Math.sin(Math.PI*World.ticks/Constants.NIGHT_DURATION));
		return night;
	}
	public static float getRateEnergy() {
		float day = (float) (4*Math.sin(Math.PI*World.ticks/Constants.NIGHT_DURATION));
		float season = (float) (0.35*Math.sin(Math.PI*World.ticks/Constants.SEASON_DURATION)*(World.ticks/Constants.SEASON_DURATION) + 0.01);
		float main = day + season;
		return main;
	}
	
	// for migration
	public static double getSeason4migration() {
		return (World.ticks + Constants.SEASON_DURATION*1/2)%(Constants.SEASON_DURATION*2) / (double)Constants.SEASON_DURATION;
	}
}
