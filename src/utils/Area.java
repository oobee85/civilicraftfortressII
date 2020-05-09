package utils;

public class Area {

	public double x1, y1;
	public double x2, y2;
	
	public Area(double x1, double y1, double x2, double y2) {
		
		if(x1 > x2) {
			double tempx1 = x1;
			this.x1 = x2;
			this.x2 = tempx1;
		}else {
			this.x1 = x1;
			this.x2 = x2;
		}
		
		if(y1 > y2) {
			double tempy1 = y1;
			this.y1 = y2;
			this.y2 = tempy1;
		}else {
			this.y1 = y1;
			this.y2 = y2;
		}
		
	}
	
	public int getIntX1() {
		return (int)x1;
	}
	public int getIntY1() {
		return (int)y1;
	}
	public int getIntX2() {
		return (int)x2;
	}
	public int getIntY2() {
		return (int)y2;
	}
	
	public boolean contains(int i, int j) {
		if(i>x1 && j>y1 && i<x2 && j<y2) {
			return true;
		}
		return false;
		
		
	}
	
}
