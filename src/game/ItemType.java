package game;

import java.awt.*;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;

public enum ItemType implements HasImage{
	
	COPPER_ORE ( "resources/Images/itemicons/copper_ore.png"),
	IRON_ORE ( "resources/Images/itemicons/iron_ore.png"),
	MITHRIL_ORE ( "resources/Images/itemicons/mithril_ore.png"),
	GOLD_ORE ( "resources/Images/itemicons/gold_ore.png"),
	SILVER_ORE ( "resources/Images/itemicons/silver_ore.png"),
	ADAMANTITE_ORE ( "resources/Images/itemicons/adamantite_ore.png"),
	RUNITE_ORE ( "resources/Images/itemicons/runite_ore.png"),
	TITANIUM_ORE ( "resources/Images/itemicons/titanium_ore.png"),
	WHEAT ( "resources/Images/itemicons/wheat.png"),
	HORSE ( "resources/Images/resources/horse.png")
	
	;
	
	private MipMap mipmap;
	private String name;
	
	ItemType(String s) {
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
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}
	
	 @Override
	public String toString() {
		return name;
	}
}
