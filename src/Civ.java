import java.awt.Point;

public class Civ {
	private Point cityCenter;
	private int population;
	private int wealth;
	private int military;
	private int reputation;
	
	
	public Civ(Point p, int pop, int gold) {
		cityCenter = p;
		population = pop;
		wealth = gold;
		
	}
}
