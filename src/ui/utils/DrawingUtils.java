package ui.utils;

import java.awt.*;
import java.awt.image.*;

import utils.Utils;

public class DrawingUtils {

	public static final Image SKY_BACKGROUND = Utils.loadImage("Images/lightbluesky.png");

	public static void drawStringWithShadow(Graphics g, String s, int x, int y) {
		g.setColor(Color.green);
		g.drawString(s, x, y);
		g.setColor(Color.black);
		g.drawString(s, x+1, y+1);
	}
	
	public static void applyColorRescaleToImage(BufferedImage image, float red, float green, float blue) {
		float[] factors = new float[] {
				red, green, blue
		};
		float[] offsets = new float[] {
				0f, 0f, 0f
		};
		RescaleOp op = new RescaleOp(factors, offsets, null);
		op.filter(image, image);
	}

	public static float[] createSpreadingKernel(int r) {
		double[] unnormalized = new double[r*r];
		double total = 0;
		for(int y = 0; y < r; y++) {
			for(int x = 0; x < r; x++) {
				double B = (x - r/2)*(x - r/2) + (y - r/2)*(y - r/2);
				if(B == 0) {
					unnormalized[x + y*r] = 1;
				}
				else if(B <= r*r/4){
					unnormalized[x + y*r] = (float) (1f / B);
				}
			}
		}
		total = 1;
		float[] normalized = new float[r*r];
		for(int y = 0; y < r; y++) {
			for(int x = 0; x < r; x++) {
				normalized[x + y*r] = (float) (unnormalized[x + y*r] / total);
			}
		}
		return normalized;
	}
	
}
