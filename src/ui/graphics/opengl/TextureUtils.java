package ui.graphics.opengl;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;

import utils.*;

public class TextureUtils {
	
	public static Texture BLANK_TEXTURE;
	public static Texture ERROR_TEXTURE;
	private static final HashMap<String, Texture> textures = new HashMap<>();
	
	public static Texture getTextureByFileName(String filename, GL3 gl) {
		if(!textures.containsKey(filename)) {
			Texture texture = loadTextureFromFile(filename, gl);
			textures.put(filename, texture);
		}
		return textures.get(filename);
	}
	
	public static void dispose(GL3 gl) {
		for(Texture t : textures.values()) {
			t.destroy(gl);
		}
		textures.clear();
	}
	
	private static Texture loadTextureFromFile(String filename, GL3 gl) {
		BufferedImage image = Utils.toBufferedImage(Utils.loadImage(filename));
		return textureFromImage(gl, image);
	}
	
	public static void initDefaultTextures(GL3 gl) {
		BufferedImage blank = new BufferedImage(8, 8, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = blank.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, blank.getWidth()-1, blank.getHeight()-1);
		g.dispose();
		BLANK_TEXTURE = textureFromImage(gl, blank);

		BufferedImage error = new BufferedImage(8, 8, BufferedImage.TYPE_3BYTE_BGR);
		for(int x = 0; x < error.getWidth(); x++) {
			for(int y = 0; y < error.getHeight(); y++) {
				Color c = ((x+y)%2 == 0) ? Color.white : Color.magenta;
				error.setRGB(x, y, c.getRGB());
			}
		}
		ERROR_TEXTURE = textureFromImage(gl, error);
	}
	
	public static Texture cubeMapFromImages(GL3 gl, BufferedImage[] images) {
		Texture cubemap = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
		for(int i = 0; i < images.length; i++) {
			TextureData texData = AWTTextureIO.newTextureData(gl.getGLProfile(), images[i], false);
			if(texData == null) {
				break;
			}
			cubemap.updateImage(gl, texData, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
		}
		cubemap.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		cubemap.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		return cubemap;
	}
	public static Texture textureFromImage(GL3 gl, BufferedImage image) {
		TextureData texData = AWTTextureIO.newTextureData(gl.getGLProfile(), image, false);
		try {
			Texture tex = TextureIO.newTexture(texData);
			tex.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
			tex.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			return tex;
		} catch (GLException e) {
			e.printStackTrace();
		} 
		return ERROR_TEXTURE;
	}
}
