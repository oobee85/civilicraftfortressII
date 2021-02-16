package game;

public class Air {

	private double humidity;
	private double temperature;
	private double pressure;
	private double maxHumidity;
	
	public Air() {
		maxHumidity = 1.0;
		
	}
	
	
	public void update() {
		
	}
	public void setHumidity(double hum) {
		this.humidity = hum;
	}
	public void setTemperature(double temp) {
		this.temperature = temp;
	}
	public void setPressure(double pres) {
		this.pressure = pres;
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
	
	private double getRelativeHumidity() {
		// pressure * volumeWater /total volume
		double ppressureWaterVapor = pressure * humidity / 1;
		
		
		double top = 17.625 * temperature;
		double bottom = temperature + 243.04;
		
		// constants change when temperature is below zero
//		if(temperature < 0) {
//			top = 21.875 * temperature;
//			bottom = temperature + 265.5;
//		}
		
		double full = top / bottom;
		
		// tetens equation
		// calculates saturation vapor pressure over liquid
		// ie saturated pressure of the water
		double saturatedPressure = Math.pow(0.61094, full);
		
		
		double relativeHumidity = (ppressureWaterVapor) / saturatedPressure * 100;
		System.out.println(relativeHumidity);
		return relativeHumidity;
	}
	
	
	private double getDewPoint() {
		
		double relativeHumidity = getRelativeHumidity();
		double first = (Math.log(relativeHumidity)-2)/0.4343;
		double second = (17.62*temperature)/(243.12+temperature);
		
		double humidity = first + second;
		double dewPoint = 243.12*humidity/(17.62-humidity); // this is the dew point in Celsius
		
		System.out.println(dewPoint);
		return dewPoint;
	}
	
}
