package ui.graphics.opengl;

import java.util.*;

import ui.graphics.opengl.maths.*;
import utils.*;

public class MeshUtils {

	public static final Mesh cube;
	public static final Mesh star;
	public static final Mesh mushroom;
	public static final Mesh house;
	

	public static final Mesh defaultUnit;
	public static final Mesh defaultBuilding;
	public static final Mesh defaultPlant;

	
	public static Mesh loadMeshFromFile(String filename, boolean swapYZ) {
		String ext = filename.substring(filename.length() - 4);
		if(ext.equals(".obj")) {
			String fileContents = Utils.readFile(filename);
			ArrayList<Vector3f> vertexLocations = new ArrayList<>();
			ArrayList<Vector2f> textureMapping = new ArrayList<>();
			ArrayList<Integer> faces = new ArrayList<>();
			for(String line : fileContents.split("\n")) {
				if(line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line);
				String lineIndicator = st.nextToken();
				if(lineIndicator.equals("v")) {
					float x=0, y=0, z=0;
					try {
						x = Float.parseFloat(st.nextToken());
						y = Float.parseFloat(st.nextToken());
						z = Float.parseFloat(st.nextToken());
					}
					catch(NumberFormatException e) {
						System.err.println("Failed to parse Float: " + line);
					}
					if(swapYZ) {
						vertexLocations.add(new Vector3f(x, z, y));
					}
					else {
						vertexLocations.add(new Vector3f(x, y, z));
					}
					continue;
				}
				else if(lineIndicator.equals("f")) {
					try {
						int one = Integer.parseInt(st.nextToken());
						int two = Integer.parseInt(st.nextToken());
						int three = Integer.parseInt(st.nextToken());
						faces.add(three);
						faces.add(two);
						faces.add(one);
					}
					catch(NumberFormatException e) {
						System.err.println("Failed to parse Integer: " + line);
					}
				}
				else if(lineIndicator.equals("vt")) {
					float u=0, v=0;
					try {
						u = Float.parseFloat(st.nextToken());
						v = Float.parseFloat(st.nextToken());
					}
					catch(NumberFormatException e) {
						System.err.println("Failed to parse Float: " + line);
					}
					textureMapping.add(new Vector2f(u, v));
					continue;
				}
			}
			Vertex[] vertices = new Vertex[vertexLocations.size()];
			for(int i = 0; i < vertices.length; i++) {
				Vector2f texCoord = new Vector2f(0, 0);
				if(i < textureMapping.size()) {
					texCoord = textureMapping.get(i);
				}
				vertices[i] = new Vertex(vertexLocations.get(i), new Vector3f(1, 1, 1), null, texCoord);
			}
			int[] facesArr = new int[faces.size()];
			for(int i = 0; i < facesArr.length; i++) {
				facesArr[i] = faces.get(i) - 1;
			}
			return new Mesh(vertices, facesArr);
		}
		else {
			System.err.println("Unknown mesh file format: " + ext);
			return cube;
		}
	}
	public static Matrix4f getModelMatrix(Vector3f position, Matrix4f rotation, Vector3f scale) {
//		return Matrix4f.multiply(Matrix4f.translate(position), rotation).multiply(Matrix4f.scale(scale));
		return Matrix4f.multiply(Matrix4f.translate(position), Matrix4f.multiply(rotation, Matrix4f.scale(scale)));
	}
	static {
		cube = loadMeshFromFile("models/cube.obj", true);
		star = loadMeshFromFile("models/star.obj", true);
		mushroom = loadMeshFromFile("models/mushroom.obj", true);
		house = loadMeshFromFile("models/house.obj", true);
		defaultUnit = cube;
		defaultPlant = mushroom;
		defaultBuilding = house;
	}
}
