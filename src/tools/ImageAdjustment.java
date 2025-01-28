package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class ImageAdjustment {
	
	public static HashMap<Color, Integer> getColorHistogram(BufferedImage image) {

    	HashMap<Color, Integer> pixelCounts = new HashMap<>();
        int[] rgbValues = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        for (int y = 0; y < image.getHeight(); y++) {
        	for (int x = 0; x < image.getWidth(); x++) {
        		int rgbValue = rgbValues[y*image.getWidth() + x];
        		Color color = new Color(rgbValue, true);
        		if (!pixelCounts.containsKey(color)) {
        			pixelCounts.put(color, 0);
        		}
    			pixelCounts.put(color, pixelCounts.get(color) + 1);
        	}
        }
        return pixelCounts;
	}
	
    public static void reduceContrast(String inputFile, String outputFile) throws IOException {

    	HashMap<Color, Color> conversion = new HashMap<>();
        BufferedImage original = ImageIO.read(new File(inputFile));
        HashMap<Color, Integer> pixelCounts = getColorHistogram(original);

        int avgRed = 0;
        int avgGreen = 0;
        int avgBlue = 0;
        for (Color c : pixelCounts.keySet()) {
        	avgRed += c.getRed() * pixelCounts.get(c);
        	avgGreen += c.getGreen() * pixelCounts.get(c);
        	avgBlue += c.getBlue() * pixelCounts.get(c);
        }
        
        int numPixels = original.getWidth() * original.getHeight();
        avgRed /= numPixels;
        avgGreen /= numPixels;
        avgBlue /= numPixels;

        int pixelInfluence = 1;
        int averageInfluence = 5;
        for (Color c : pixelCounts.keySet()) {
        	Color adjusted = new Color(
        			(c.getRed()*pixelInfluence + avgRed*averageInfluence)/(pixelInfluence+averageInfluence),
        			(c.getGreen()*pixelInfluence + avgGreen*averageInfluence)/(pixelInfluence+averageInfluence),
        			(c.getBlue()*pixelInfluence + avgBlue*averageInfluence)/(pixelInfluence+averageInfluence)
        			);
        	conversion.put(c, adjusted);
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
    
    public static void replaceColors(String filepath, Color[][] replacements, File outputDir) throws IOException {
    	HashMap<Color, Color> colorMap = new HashMap<>();
    	for (Color[] replacement : replacements) {
    		colorMap.put(replacement[0], replacement[1]);
    	}
        for (Color key : colorMap.keySet()) {
        	Color value = colorMap.get(key);
        	System.out.println(String.format("replace #%02X%02X%02X%02X with #%02X%02X%02X%02X",
    				key.getAlpha(), key.getRed(), key.getGreen(), key.getBlue(), 
    				value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()));
        }

    	File folder = new File(filepath);
    	if (!folder.exists()) {
    		return;
    	}
    	for (File file : folder.listFiles()) {
    		if (!file.isFile()) {
    			continue;
    		}
    		System.out.println("Found file " + file.getName());
    		
    		BufferedImage image = ImageIO.read(file);

            HashMap<Color, Integer> pixelCounts = getColorHistogram(image);
            
            for (Color key : pixelCounts.keySet()) {
            	System.out.println(String.format("#%02X%02X%02X%02X: %d", key.getAlpha(), key.getRed(), key.getGreen(), key.getBlue(), pixelCounts.get(key)));
            }

            WritableRaster raster = image.getRaster();
            int[] pixels = raster.getPixels(0, 0, image.getWidth(), image.getHeight(), (int[])null);
            int numColorComponents = pixels.length / (image.getWidth() * image.getHeight());
            for (int i = 0; i < pixels.length; i += numColorComponents) {
            	int red = pixels[i];
            	int green = pixels[i+1];
            	int blue = pixels[i+2];
            	int alpha = numColorComponents >= 4 ? pixels[i + 3] : 0xFF;
            	Color color = new Color(red, green, blue, alpha);
            	
            	if (colorMap.containsKey(color)) {
                	Color converted = colorMap.get(color);
                	
                	pixels[i] = converted.getRed();
                	pixels[i+1] = converted.getGreen();
                	pixels[i+2] = converted.getBlue();
                	if (numColorComponents >= 4) {
                		pixels[i+3] = converted.getAlpha();
                	}
            	}
            }
            raster.setPixels(0, 0, image.getWidth(), image.getHeight(), pixels);
            
            
            ImageIO.write(image, "png", new File(outputDir.getAbsolutePath() + "/" + file.getName()));
    	}
    }
    
    public static void main(String[] args) {
    	File outputDir = new File("imageAdjustmentOutput");
    	if (!outputDir.exists()) {
    		outputDir.mkdir();
    	}
    	else {
    		if (!outputDir.isDirectory()) {
    			System.err.println("ERROR output directory " + outputDir.getAbsolutePath() + " is not a directory!");
    			return;
    		}
    	}
    	try {
//    	reduceContrast("resources\\Images\\terrain\\rock16.png", "rock16_lowcontrast.png");
//    	reduceContrast("resources\\Images\\terrain\\grass16.png", "grass16_lowcontrast.png");
//    	reduceContrast("resources\\Images\\terrain\\dirt16.png", "dirt16_lowcontrast.png");
    	
			replaceColors("resources\\Images\\plants\\treeTilesetCustom", new Color[][]{
				{new Color(0xFF105926, true), new Color(0xFF16762E, true)},
				{new Color(0xFF0F5223, true), new Color(0xFF135D25, true)},
			}, outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
