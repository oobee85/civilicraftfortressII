package world;

import ui.*;

public class Season {

	public static final double FREEZING_TEMPURATURE = 0.4;
	public static final double MELTING_TEMPURATURE = 0.5;
	public static final int SEASON_DURATION = 6000;
	public static double[] winter;
	public static double[] summer;
	public static double getSeason2() {
		int season =  (Game.ticks + SEASON_DURATION*1/2)%(SEASON_DURATION*2);
		return Math.abs(SEASON_DURATION - season) / (double)SEASON_DURATION;
	}
	public static double getSeason4() {
		return (Game.ticks + SEASON_DURATION*1/2)%(SEASON_DURATION*2) / (double)SEASON_DURATION;
	}

	private static final double SNOWY_POLES_RATIO = 0.04;
	public static void makeSeasonArrays(int mapheight) {
		summer = new double[mapheight];
		winter = new double[mapheight];
		int northPole = (int) (summer.length * SNOWY_POLES_RATIO);
		int southPole = (int) (summer.length - summer.length * SNOWY_POLES_RATIO);
		
		int winterPoint = summer.length;
		
		for(int i = 0; i < summer.length; i++) {
			double snowyPoles = 0;
			if(i < northPole) {
				snowyPoles += (1.0*northPole - i) / northPole;
			}
//			else if(i > southPole) {
//				snowyPoles += (1.0*i - southPole) / northPole;
//			}
			summer[i] = snowyPoles;
			double winterSeason = 0;
			if(i < winterPoint) {
				winterSeason += (1.0 * winterPoint - i) / winterPoint;
			}
			winter[i] = snowyPoles > winterSeason ? snowyPoles : winterSeason;
		}
	}
}
