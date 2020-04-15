import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class MipMap {

	private final ImageIcon[] mipmaps;
	private final int[] mipmapSizes;


	public MipMap(String[] paths) {
		int numFiles = paths.length;
		
		mipmaps = new ImageIcon[numFiles];
		mipmapSizes = new int[numFiles];
		int index = 0;
		for (String s : paths) {
			mipmaps[index] = Utils.loadImageIcon(s);
			mipmapSizes[index] = mipmaps[index].getIconWidth();
			index++;
		}
	}
	
	public MipMap(String path) {
		this(new String[] { path});
	}

	public Image getImage(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i].getImage();
			}
		}
		return mipmaps[mipmaps.length - 1].getImage();
	}

	public ImageIcon getImageIcon(int size) {
		// Get the first mipmap that is larger than the tile size
		for (int i = 0; i < mipmapSizes.length; i++) {
			if (mipmapSizes[i] > size) {
				return mipmaps[i];
			}
		}
		return mipmaps[mipmaps.length - 1];
	}
}
