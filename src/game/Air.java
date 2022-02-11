package game;

import utils.Direction;
import world.World;

public class Air {

	private double humidity;
	private double temperature;
	private double pressure;
	private double height;
	private double volume;
	private double mass;
	private double energy;
	
	private double maxHumidity;
	private double maxVolume;
	private boolean canRain;
	private double volumeChange;
	private Direction flowDirection;
	private double tileVolume;
	
	
	public Air(double height) {
		this.maxHumidity = 1.0;
		this.height = height;
		this.maxVolume = 10;
		this.canRain = false;
		this.volume = 1;
		this.humidity = 0.0;
		this.pressure = 760;
		this.updateMaxVolume();
		this.updateHumidity();
		this.flowDirection = Direction.NONE;
		this.energy = 0;
		this.tileVolume = World.VOLUMEPERTILE;
		
	}
	public void updateHeight(double height) {
		this.height = height;
	}
	public Direction getFlowDirection() {
		return flowDirection;
	}
	public void setFlowDirection(Direction direction) {
		flowDirection = direction;
	}
	
	public void setTemperature(double temp) {
		this.temperature = temp;
	}
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	public double getVolumeLiquid() {
		return volume;
	}
	public double getMaxVolumeLiquid() {
		return maxVolume;
	}
	public double getHumidity() {
		return humidity;
	}
	public double getTemperature() {
		return temperature;
	}
	public double getEnergy() {
		return energy;
	}
	public void addEnergy(double added) {
		this.energy += added;
	}
	public double getPressure() {
		return pressure;
	}
	public void setVolumeLiquid(double set) {
		volume = set;
	}
	public void addVolumeLiquid(double add) {
		this.volume += add;
		volumeChange = add;
	}
	public double getVolumeChange() {
		return volumeChange;
	}
	public void updateMaxVolume() {
		if(temperature < World.FREEZETEMP) {
			maxVolume = 0;
		}
		if(temperature > World.FREEZETEMP - 10) {
			maxVolume = (this.temperature - World.KELVINOFFSET)/4 + 3.5;
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
	public void decreaseVolumePerTile(double volume) {
//		if(this.tileVolume > 0) {
		System.out.println("working");
			this.tileVolume += volume;
			this.energy += 10000;
//		}
		
	}
	public void updatePressure() {
		
		double h = this.height; // m
		double temp = getTemperature();
		
		double moles = this.mass / World.MMAIR;
//		double moles = this.getHumidity();
		double pvnrt = (moles * World.R * temp) / (World.VOLUMEPERTILE *2);
//		this.pressure = pvnrt;
				
		
		double sub = World.R * (temp + World.KELVINOFFSET);
//		double standardPVNRT = World.STARTINGMASS * sub / World.VOLUMEPERTILE;
		
		double power = (-World.G * World.MMAIR * (h - World.SEALEVEL)) / sub;
		double pressure = World.STANDARDPRESSURE * Math.pow(Math.E, power);
		this.pressure = (pressure + pvnrt)/2 - 200;
//		if(this.flowDirection != Direction.NONE) {
//			this.pressure -= 5;
//		}
//		double pvnrt = mass * sub / tileVolume;
//		double mix = pressure+(pvnrt - standardPVNRT);
//		
////		System.out.println("Pressure: " + pressure);
////		System.out.println("pvnrt: "+other + "atm: "+pressure);
//		this.pressure = -1*mix - 1350;
	}

	public double getDensity() {
		double density = this.pressure/World.STANDARDPRESSURE*World.MMAIR / (World.RYDBERG * (getTemperature() + World.KELVINOFFSET) );
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
