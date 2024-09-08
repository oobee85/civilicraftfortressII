package game.ai;

public class QuantityReq {
	int min;
	int enough;
	int max;
	public QuantityReq(int min, int enough, int max) {
		this.min = min;
		this.enough = enough;
		this.max = max;
	}
	
	@Override
	public String toString() {
		if (enough == max) {
			return String.format("%d<%d", min, enough);
		}
		if (max == Integer.MAX_VALUE) {
			return String.format("%d<%d<inf", min, enough);
		}
		return String.format("%d<%d<%d", min, enough, max);
	}
}
