package ui.graphics.opengl;

import java.awt.*;
import java.awt.image.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import ui.graphics.*;
import utils.*;

public class GLDrawer implements Drawer {

	private final GLCanvas glcanvas;
	public GLDrawer() {
		// getting the capabilities object of GL2 profile
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);

		// The canvas
		glcanvas = new GLCanvas(capabilities);
	}
	
	public Component getDrawingCanvas() {
		return glcanvas;
	}

	public void updateTerrainImages() {
	}

	public BufferedImage getImageToDrawMinimap() {
		return new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
	}
	public Position[] getVisibleTileBounds() {
		return null;
	}
}
