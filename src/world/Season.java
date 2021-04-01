package world;

import ui.*;

public class Season {

	public static final float FREEZING_TEMPURATURE = 0.33f;
	public static final float MELTING_TEMPURATURE = 0.43f;
	public static final int SEASON_DURATION = 10000;
	public static float[] winter;
	public static float[] summer;
	
	public static float getSeason2() {
		float season = (float) (10.0 * Math.sin(Math.PI*World.ticks/SEASON_DURATION) + 10);
		
		
//		int season =  (World.ticks + SEASON_DURATION*1/2)%(SEASON_DURATION*2);
//		return Math.abs(SEASON_DURATION - season) / (float)SEASON_DURATION;
		return season;
	}
	public static float getSeasonEnergy() {
		float season = (float) (1/2*Math.sin(Math.PI*World.ticks/SEASON_DURATION));
		return season;
	}
	public static float getEnergyChange() {
		
		// added value at the end... positive = summer more extreme... negative = winter more extreme
		float main = (float) (3600*(Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION)*(Math.sin(Math.PI*World.ticks/SEASON_DURATION)+1)/50));
		
		// does day/night cycle
		float main2 = (float) (Math.sin(Math.PI*World.ticks/SEASON_DURATION)*Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION));
		
		float combined = main + main2;
		//						| this value squishes curve vertically based on size... in summer(large value = low energy less extreme)
		//						V														in winter(large value = high energy less extreme)
		float offset = (float) (2000*Math.sin(Math.PI*World.ticks/SEASON_DURATION));
		float dEnergy = offset + combined;
		return dEnergy;
	}
	public static float getNightEnergy() {
		float night = (float) (5 * Math.sin(Math.PI*World.ticks/World.NIGHT_DURATION));
		return night;
	}
	public static float getRateEnergy() {
		float day = (float) (2.5*Math.sin(Math.PI*World.ticks/250));
		float season = (float) (0.5*Math.sin(Math.PI*World.ticks/1000));
		float main = day + season;
		return main;
		
	}
	
	// for migration
	public static double getSeason4migration() {
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
