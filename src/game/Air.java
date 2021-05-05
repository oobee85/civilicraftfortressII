package game;

import world.World;

public class Air {

	private double humidity;
	private double temperature;
	private double pressure;
	private double height;
	private double volume;
	private double mass;
	
	private double maxHumidity;
	private double maxVolume;
	private boolean canRain;
	private double volumeChange;
	
	public Air(double height, double temp) {
		this.maxHumidity = 1.0;
		this.height = height;
		this.temperature = temp;
		this.maxVolume = 10;
		this.canRain = false;
		this.volume = 0;
		this.humidity = 0.0;
		this.pressure = 760;
		this.updateMaxVolume();
		this.updateHumidity();
	}
	
	public void setTemperature(double temp) {
		this.temperature = temp;
	}
	public double getVolume() {
		return volume;
	}
	public double getMaxVolume() {
		return maxVolume;
	}
	public double getHumidity() {
		return humidity;
	}
	public double getTemperature() {
		return temperature;
	}
	public double getPressure() {
		return pressure;
	}
	public void setVolume(double set) {
		volume = set;
	}
	public void addVolume(double add) {
		this.volume += add;
		volumeChange = add;
	}
	public double getVolumeChange() {
		return volumeChange;
	}
	public void updateHeight(double height) {
		this.height = height;
	}
	public void updateMaxVolume() {
		if(temperature > 2) {
			maxVolume = this.temperature/2;
		}else {
			maxVolume = 1;
		}
		
	}
	public void updateHumidity() {
		if(maxVolume > 0) {
			humidity = volume / maxVolume;
		}else {
			humidity = 0;
		}
		
	}
	public void addHumidity(double added) {
		this.humidity += added;
		if(humidity >= 1.0) {
			canRain = true;
		}else {
			canRain = false;
		}
		
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	public double getMass() {
		return mass;
	}
	public void addMass(double mass) {
		this.mass += mass;
	}
	public void updatePressure() {
		
		
		double P0 = World.STANDARDPRESSURE; // mmHg
		double g = 9.80665; // m/s^2
		double MMair = 0.04401; // kg/mol CO2
		double R = 8.31432; // Nm/molK
		double h0 = 100; // m sealevel
		double h = this.height; // m
		double boltz = 1.380649e-23;
		double temp = getTemperature();
		
//		double sub = boltz * temp;
		
		double sub = R * (temp + Math.abs(World.MINTEMP));
		double standardPVNRT = World.STARTINGMASS * sub / World.VOLUMEPERTILE;
		
		double power = (-g * MMair * (h - h0)) / sub;
		double pressure = P0 * Math.pow(Math.E, power);
		
		double pvnrt = mass * sub / World.VOLUMEPERTILE;
		double mix = pressure+(pvnrt - standardPVNRT);
//		System.out.println("Pressure: " + pressure);
//		System.out.println("pvnrt: "+other + "atm: "+pressure);
		this.pressure = pressure;

	}
	public double getDensity() {
		double density = this.pressure/World.STANDARDPRESSURE*World.MMAIR / (0.0821 * (getTemperature() + Math.abs(World.MINTEMP)) );
		return density;
	}
	public double getRelativeHumidity() {
		
		double first = (this.getTemperature() - getHumidity());
		
		double relativeHumidity = 100 - 5*first;
		return relativeHumidity;
		
//		
//		// pressure * volumeWater /total volume
//		double ppressureWaterVapor = pressure * humidity / 1;
//		
//		
//		double top = 17.625 * temperature;
//		double bottom = temperature + 243.04;
//		
//		// constants change when temperature is below zero
////		if(temperature < 0) {
////			top = 21.875 * temperature;
////			bottom = temperature + 265.5;
////		}
//		
//		double full = top / bottom;
//		
//		// tetens equation
//		// calculates saturation vapor pressure over liquid
//		// ie saturated pressure of the water
//		double saturatedPressure = Math.pow(0.61094, full);
//		
//		
//		double relativeHumidity = (ppressureWaterVapor) / saturatedPressure *100;
////		System.out.println("relative Humidity: " + relativeHumidity);
//		return relativeHumidity;
	}
	
	
	public double getDewPoint() {
		double relativeHumidity = 90;
		double first = (100 - relativeHumidity)/5;
		
		double dewPoint = this.getTemperature() - first;
		
		
		

//		
//		double first = (Math.log(relativeHumidity/100)-2)/0.4343;
//		
//		double second = (17.62*temperature)/(243.12+temperature);
//		double humidity = first + second;
//		double dewPoint = 243.12*humidity/(17.62-humidity); // this is the dew point in Celsius
//		
//		System.out.println("first: " + first);
//		System.out.println("second: " + second);
//		System.out.println("firstsecond: " + humidity);
//		System.out.println("Dew point: " + dewPoint);
		return dewPoint;
	}
	
	
	public boolean canRain() {
		if(this.volume >= this.maxVolume) {
			return true;
		}
		if(this.humidity >= 0.9) {
			return true;
		}
		return false;
	}
	
	
	
	
	
}
