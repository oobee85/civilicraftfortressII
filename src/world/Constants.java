package world;

public class Constants {

	public static final int TICKS_PER_ENVIRONMENTAL_DAMAGE = 10;
	public static final double TERRAIN_SNOW_LEVEL = 1;
	public static final double DESERT_HUMIDITY = 1;
	public static final int DAY_DURATION = 1000;
	public static final int NIGHT_DURATION = 1000;
	public static final int SEASON_DURATION = (DAY_DURATION+NIGHT_DURATION)*10;
	public static final int TRANSITION_PERIOD = 100;
	public static final double CHANCE_TO_SWITCH_TERRAIN = 0.1;
	public static final int MIN_TIME_TO_SWITCH_TERRAIN = 100;
	public static final int TICKSTOUPDATEAIR = 2;
	
	public static final int KELVINOFFSET = 273;
	public static final int MINTEMP = 0; // [K]
	public static final int MAXTEMP = 1273; // [K]
	public static final int BALANCETEMP = 283; // [K]
	public static final int FREEZETEMP = 273; // [K]
	public static final int LETHALHOTTEMP = 311; // [K]
	public static final int LETHALCOLDTEMP = 263; // [K]
	public static final float FREEZING_TEMPURATURE = 0.33f;
	public static final int BALANCEWATER = 10;
	
	public static final int MAXHEIGHT = 1000; // [m]
	public static final int SEALEVEL = 300; // [m]
	public static final int WATTSPERTILE = 1; // [J/s]
	public static final double STANDARDPRESSURE = 760; // [mmHg]
	public static final int VOLUMEPERTILE = 100; // [m^3]
	public static final int STARTINGMASS = (int)(VOLUMEPERTILE * 1.22); // [kg]
//	public static final int STARTINGMASS = 10; // [kg]
//	public static final int MMAIR = 10;
//	public static final double MMCO2 = 0.04401; // [kg/mol CO2]
	public static final double MASSGROUND = 1 * VOLUMEPERTILE; // [kg]
	public static final double MMAIR = 0.05; // [kg/mol AIR]
	public static final double R = 8.31432; // [Nm/mol*K]  [J/mol*K]
	public static final double RYDBERG = 0.08206; // [atm*L/mol*K]
	public static final double G = 9.80665; // [m/s^2]
	public static final double BOLTZMANN = 1.380649e-23; // [J/K]
	public static final double BOLTZMANNMODIFIED = 5.670374e-7; // [J/K]
	public static final double DEFAULTENERGY = 28000;
	
	
	public static final double BUSH_RARITY = 0.005;
	public static final double WATER_PLANT_RARITY = 0.05;
	public static final double FOREST_DENSITY = 0.005;
}
