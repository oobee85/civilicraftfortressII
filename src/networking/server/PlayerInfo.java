package networking.server;

import java.awt.*;
import java.io.*;

public class PlayerInfo implements Serializable {
	private final String name;
	private final Color color;
	
	public PlayerInfo(String name, Color color) {
		this.name = name;
		this.color = color;
	}
	public String getName() {
		return name;
	}
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return name + ":" + String.format("0x%08X", color.getRGB());
	}
}
