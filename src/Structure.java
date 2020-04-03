import java.awt.*;
import java.awt.image.BufferedImage;

public class Structure {

    private final Image image;
    public Structure() {
    	this.image = Utils.loadImage("Images/buildings/castle256.png");
    }
    public Image getImage() {
    	return image;
    }
}
