package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;

public enum ResourceType implements HasImage{
	
	COPPER_ORE ( "resources/Images/resourceicons/copper_ore.png"),
	IRON_ORE ( "resources/Images/resourceicons/iron_ore.png"),
	MITHRIL_ORE ( "resources/Images/resourceicons/mithril_ore.png"),
	GOLD_ORE ( "resources/Images/resourceicons/gold_ore.png"),
	SILVER_ORE ( "resources/Images/resourceicons/silver_ore.png"),
	ADAMANTITE_ORE ( "resources/Images/resourceicons/adamantite_ore.png"),
	RUNITE_ORE ( "resources/Images/resourceicons/runite_ore.png"),
	TITANIUM_ORE ( "resources/Images/resourceicons/titanium_ore.png")
	;
	
	private MipMap mipmap;
	private String name;
	
	ResourceType(String s) {
		 this.mipmap = new MipMap(s);
		 name = this.name().toLowerCase().replace('_', ' ');
	}

	 @Override
	    public Image getImage(int size) {
	    	return mipmap.getImage(size);
	    }
	    @Override
	    public ImageIcon getImageIcon(int size) {
	    	return mipmap.getImageIcon(size);
	    }
	
	 @Override
	public String toString() {
		return name;
	}
}
