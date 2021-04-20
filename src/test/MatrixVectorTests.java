package test;

import static org.junit.Assert.*;

import org.junit.*;

import ui.graphics.opengl.maths.*;

public class MatrixVectorTests {
	
	@Test
	public void invertTranslation() {
		// test translate 1, 1, 1 -> -1, -1, -1
		Matrix4f starting = Matrix4f.translate(new Vector3f(1, 1, 1));
		Matrix4f inverted = starting.inverse();
		Matrix4f expected = Matrix4f.translate(new Vector3f(1, 1, 1).multiply(-1));
		assertTrue(inverted.equals(expected));
		
	}
	
	@Test
	public void invertRotation() {
		// test rotate 90 around x -> -90 around x
		Matrix4f starting = Matrix4f.rotate(90, Vector3f.xAxis());
		Matrix4f inverted = starting.inverse();
		Matrix4f expected = Matrix4f.rotate(-90, Vector3f.xAxis());
		assertTrue(inverted.equals(expected));
	}
	
	@Test
	public void invertScale() {
		// test scale x2 -> x0.5
		Matrix4f starting = Matrix4f.scale(new Vector3f(2, 2, 2));
		Matrix4f inverted = starting.inverse();
		Matrix4f expected = Matrix4f.scale(new Vector3f(0.5f, 0.5f, 0.5f));
		assertTrue(inverted.equals(expected));
	}

}
