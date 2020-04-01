import java.awt.*;

public class Driver {
	private static int worldSize = 64;
	public static void main(String[] run) {
		new Frame(Toolkit.getDefaultToolkit().getScreenSize().width * 3/4, Toolkit.getDefaultToolkit().getScreenSize().height * 3/4, worldSize);
	}


}
