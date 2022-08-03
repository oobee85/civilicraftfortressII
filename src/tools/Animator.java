package tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

class Offset {
	int x, y;

	public Offset(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

public class Animator {

	public static void main(String[] args) {

		
		String filename = "E:\\Workspace\\_SettlersOfMightAndMagic\\resources\\Images\\interfaces\\flow\\up2.png";
		
		int deltax = 0;
		int deltay = -2;
		LinkedList<BufferedImage> frames = new LinkedList<>();
		try {
			BufferedImage image = ImageIO.read(new File(filename));
			
			for (int frameIndex = 0; ; frameIndex++) {
				int frameOffset = frameIndex;
				BufferedImage frameImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
				
				int xFrameOffset = frameOffset*deltax;
				int yFrameOffset = (frameOffset*deltay + image.getHeight()*100) % image.getHeight();
				
				if (xFrameOffset % (image.getWidth()*2) == 0 && yFrameOffset == 0 && frameIndex > 0) {
					System.out.println("Looped back around at frame " + frameIndex);
					break;
				}
				
				int closestXOffset = -frameOffset*deltax / image.getWidth();
				
				Graphics g = frameImage.createGraphics();
				for (int xoffset = -4 + closestXOffset; xoffset <= 4 + closestXOffset; xoffset++) {
					for (int yoffset = -2; yoffset <= 2; yoffset++) {
						int xoff = xoffset * image.getWidth();
						int yoff = yoffset * image.getHeight() + (((xoffset + 100000)%2 == 1) ? image.getHeight()/2 : 0);
						g.drawImage(image, xFrameOffset + xoff, yFrameOffset + yoff, null);
					}
				}
				g.dispose();
				
				frames.add(frameImage);
			}
			
			File outputdir = new File("./animation/frames");
			outputdir.mkdirs();
			
			int index = 0;
			for(BufferedImage frame : frames) {
				ImageIO.write(frame, "png", new File(String.format("./animation/frames/frame%d.png", index)));
				index++;
			}
			
			BufferedImage spritesheet = new BufferedImage(image.getWidth()*frames.size(), image.getHeight(), image.getType());
			Graphics g = spritesheet.createGraphics();
			index = 0;
			for(BufferedImage frame : frames) {
				g.drawImage(frame, index * image.getWidth(), 0, null);
				
//				g.setColor(Color.white);
//				g.drawRect(index * image.getWidth() + 2, 2, image.getWidth() - 4, image.getHeight() - 4);
				index++;
			}
			g.dispose();
			ImageIO.write(spritesheet, "png", new File("./animation/spritesheet.png"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}
}
