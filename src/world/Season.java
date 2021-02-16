package world;

import ui.*;

public class Season {

	public static final float FREEZING_TEMPURATURE = 0.33f;
	public static final float MELTING_TEMPURATURE = 0.43f;
	public static final int SEASON_DURATION = 10000;
	public static float[] winter;
	public static float[] summer;
	public static float getSeason2() {
		float season = (float) (15 * Math.sin(World.ticks/SEASON_DURATION * Math.PI) + 10);
		
//		int season =  (World.ticks + SEASON_DURATION*1/2)%(SEASON_DURATION*2);
//		return Math.abs(SEASON_DURATION - season) / (float)SEASON_DURATION;
		return season;
	}
	public static double getSeason4() {
		return (World.ticks + SEASON_DURATION*1/2)%(SEASON_DURATION*2) / (double)SEASON_DURATION;
	}

	private static final float SNOWY_POLES_RATIO = 0.04f;
	public static void makeSeasonArrays(int mapheight) {
		summer = new float[mapheight];
		winter = new float[mapheight];
		int northPole = (int) (summer.length * SNOWY_POLES_RATIO);
		int southPole = (int) (summer.length - summer.length * SNOWY_POLES_RATIO);
		
		int winterPoint = summer.length;
		
		for(int i = 0; i < summer.length; i++) {
			float snowyPoles = 0;
			if(i < northPole) {
				snowyPoles += (1.0*northPole - i) / northPole;
			}
//			else if(i > southPole) {
//				snowyPoles += (1.0*i - southPole) / northPole;
//			}
			summer[i] = snowyPoles;
			float winterSeason = 0;
			if(i < winterPoint) {
				winterSeason += (1.0 * winterPoint - i) / winterPoint;
			}
			winter[i] = snowyPoles > winterSeason ? snowyPoles : winterSeason;
		}
	}
}
