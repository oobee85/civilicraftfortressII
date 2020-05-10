package game;

import java.awt.*;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;

public enum ResourceType implements HasImage{
	
	COPPER_ORE ( "resources/Images/buildings/wall_brick.png"),
	IRON_ORE ( "resources/Images/buildings/wall_brick.png"),
	MITHRIL_ORE ( "resources/Images/buildings/wall_brick.png"),
	GOLD_ORE ( "resources/Images/buildings/wall_brick.png"),
	SILVER_ORE ( "resources/Images/buildings/wall_brick.png"),
	ADAMANTITE_ORE ( "resources/Images/buildings/wall_brick.png"),
	RUNITE_ORE ( "resources/Images/buildings/wall_brick.png"),
	TITANIUM_ORE ( "resources/Images/buildings/wall_brick.png")
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
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}
	
	 @Override
	public String toString() {
		return name;
	}
}
