package game;

public class Air {

	private double humidity;
	private double temperature;
	private double pressure;
	private double maxHumidity;
	private double height;
	private boolean canRain;
	
	public Air(double height) {
		this.maxHumidity = 1.0;
		this.height = height;
		this.canRain = false;
	}
	
	
	public void update() {
		
	}
	public void setHumidity(double hum) {
		this.humidity = hum;
	}
	public void setTemperature(double temp) {
		this.temperature = temp;
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
	
	public void addHumidity(double added) {
		this.humidity += added;
		if(humidity > 1.0) {
			canRain = true;
		}else {
			canRain = false;
		}
	}
	public void updatePressure() {

		double P0 = 760; // mmHg
		double g = 9.80665; // m/s^2
		double MMair = 0.0289644; // kg/mol
		double R = 8.31432; // Nm/molK
		double h0 = 0; // m
		double h = this.height; // m

		double sub = R * getTemperature();
		double power = (-g * MMair * (h - h0)) / sub;

		double pressure = P0 * Math.pow(Math.E, power);

//		System.out.println("Pressure: " + pressure);
		this.pressure = pressure;

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
		return canRain;
	}
	
}
