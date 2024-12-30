package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class ReduceContrast {
    public static void reduceContrast(String inputFile, String outputFile) {

    	HashMap<Color, Integer> pixelCounts = new HashMap<>();
    	HashMap<Color, Color> conversion = new HashMap<>();
        try {
            BufferedImage original = ImageIO.read(new File(inputFile));
            
            int[] rgbValues = original.getRGB(0, 0, original.getWidth(), original.getHeight(), null, 0, original.getWidth());
            
            for (int y = 0; y < original.getHeight(); y++) {
            	for (int x = 0; x < original.getWidth(); x++) {
            		Color color = new Color(rgbValues[y*original.getWidth() + x]);
            		if (!pixelCounts.containsKey(color)) {
            			pixelCounts.put(color, 0);
            		}
        			pixelCounts.put(color, pixelCounts.get(color) + 1);
            	}
            }

            BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

            int avgRed = 0;
            int avgGreen = 0;
            int avgBlue = 0;
            int offset = 0;
            for (Color c : pixelCounts.keySet()) {
            	result.setRGB(offset++, 0, c.getRGB());

            	avgRed += c.getRed() * pixelCounts.get(c);
            	avgGreen += c.getGreen() * pixelCounts.get(c);
            	avgBlue += c.getBlue() * pixelCounts.get(c);
            }
            
            int numPixels = original.getWidth() * original.getHeight();
            avgRed /= numPixels;
            avgGreen /= numPixels;
            avgBlue /= numPixels;

            for (Color c : pixelCounts.keySet()) {
            	Color adjusted = new Color(
            			(c.getRed() + avgRed)/2,
            			(c.getGreen() + avgGreen)/2,
            			(c.getBlue() + avgBlue)/2
            			);
            	conversion.put(c, adjusted);
            }

            offset = 0;
            for (Color c : conversion.keySet()) {
            	result.setRGB(offset, 1, c.getRGB());
            	result.setRGB(offset, 2, conversion.get(c).getRGB());
            	offset++;
            }
            
            WritableRaster raster = original.getRaster();
            
            int[] pixels = raster.getPixels(0, 0, original.getWidth(), original.getHeight(), (int[])null);
            
            int numColorComponents = pixels.length / numPixels;
            
            for (int i = 0; i < pixels.length; i += numColorComponents) {
            	int red = pixels[i];
            	int green = pixels[i+1];
            	int blue = pixels[i+2];
            	Color color = new Color(red, green, blue);
            	
            	if (!conversion.containsKey(color)) {
            		System.err.println("Missing color!");
            		continue;
            	}
            	Color converted = conversion.get(color);
            	
            	pixels[i] = converted.getRed();
            	pixels[i+1] = converted.getGreen();
            	pixels[i+2] = converted.getBlue();
            }
            
            raster.setPixels(0, 0, original.getWidth(), original.getHeight(), pixels);
            
            
            ImageIO.write(original, "png", new File(outputFile));

        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public static void main(String[] args) {
    	reduceContrast("resources\\Images\\terrain\\rock16.png", "rock16_lowcontrast.png");
    }
}
