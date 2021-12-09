package ui.utils;

import java.awt.Color;
import java.awt.Graphics;

public class DrawingUtils {

	public static void drawStringWithShadow(Graphics g, String s, int x, int y) {
		g.setColor(Color.green);
		g.drawString(s, x, y);
		g.setColor(Color.black);
		g.drawString(s, x+1, y+1);
	}
}
