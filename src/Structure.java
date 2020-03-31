import java.awt.image.BufferedImage;

public class Structure {

    private final BufferedImage image;
    public Structure() {
    	this.image = Utils.loadImage(50, 50, "Images/castle.jpg");
    }
    public BufferedImage getImage() {
    	return image;
    }
}
