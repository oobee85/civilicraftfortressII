package game.components;

public abstract class Component {
	public static int numC = 0;
	static {
		numC++;
		System.err.println("num components: " + numC);
	}
	public Component() {
		
	}
}
