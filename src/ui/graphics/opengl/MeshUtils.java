package ui.graphics.opengl;

import java.io.*;
import java.nio.*;
import java.util.*;

import org.smurn.jply.*;

import ui.graphics.opengl.maths.*;
import utils.*;

public class MeshUtils {

	public static final Mesh cube;
	public static final Mesh skybox;
	public static final Mesh square;
	public static final Mesh cattail;
	public static final Mesh star;
	public static final Mesh mushroom;
	public static final Mesh house;

	public static final Mesh x;
	public static final Mesh y;
	public static final Mesh z;
	

	public static final Mesh defaultUnit;
	public static final Mesh defaultBuilding;
	public static final Mesh defaultPlant;
	
	private static final HashMap<String, Mesh> meshes = new HashMap<>();
	
	
	public static Mesh getMeshByFileName(String filename) {
		if(!meshes.containsKey(filename)) {
			Mesh mesh = loadMeshFromFile(filename);
			normalize(mesh, false);
			meshes.put(filename, mesh);
		}
		return meshes.get(filename);
	}
	
	private static void normalize(Mesh mesh, boolean zeroZ) {
		Vector3f min = new Vector3f(mesh.getVertices()[0].getPosition());
		Vector3f max = new Vector3f(min);
		for(Vertex v : mesh.getVertices()) {
			min.x = Math.min(v.getPosition().x, min.x);
			min.y = Math.min(v.getPosition().y, min.y);
			min.z = Math.min(v.getPosition().z, min.z);
			max.x = Math.max(v.getPosition().x, max.x);
			max.y = Math.max(v.getPosition().y, max.y);
			max.z = Math.max(v.getPosition().z, max.z);
		}
		
		Vector3f range = max.subtract(min);
		float maximumRange = Math.max(range.y, range.x);
		float scale = 1f/maximumRange;
		Vector3f offset = new Vector3f(-range.x*scale*0.5f, -range.y*scale*0.5f, 0);
		if(zeroZ) {
			offset.z = -range.z*scale*0.5f;
		}
		for(Vertex v : mesh.getVertices()) {
			Vector3f newPos = v.getPosition().subtract(min);
			newPos = newPos.multiply(scale);
			newPos = newPos.add(offset);
			v.getPosition().set(newPos);
		}
	}
	
	private static Mesh readObjFile(String filename) {
		String fileContents = Utils.readFile(filename);
		ArrayList<Vector3f> vertexLocations = new ArrayList<>();
		ArrayList<Vector2f> textureMapping = new ArrayList<>();
		ArrayList<Integer> faces = new ArrayList<>();
		for(String line : fileContents.split("\n")) {
			try {
				if(line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line);
				String lineIndicator = st.nextToken();
				if(lineIndicator.equals("v")) {
					float x=0, y=0, z=0;
					x = Float.parseFloat(st.nextToken());
					y = Float.parseFloat(st.nextToken());
					z = Float.parseFloat(st.nextToken());
					vertexLocations.add(new Vector3f(x, y, z));
					continue;
				}
				else if(lineIndicator.equals("f")) {
					// obj file faces are 1 indexed so have to -1
					int one = Integer.parseInt(st.nextToken()) -1;
					int two = Integer.parseInt(st.nextToken()) - 1;
					int three = Integer.parseInt(st.nextToken()) - 1;
					faces.add(three);
					faces.add(one);
					faces.add(two);
				}
				else if(lineIndicator.equals("vt")) {
					float u=0, v=0;
					u = Float.parseFloat(st.nextToken());
					v = Float.parseFloat(st.nextToken());
					textureMapping.add(new Vector2f(u, v));
					continue;
				}
			}
			catch(NumberFormatException e) {
				System.err.println("Failed to parse line \"" + line + "\" in file: " + filename);
			}
		}
		return arraysToMesh(vertexLocations, textureMapping, faces);
	}
	
	private static Mesh readPlyFile(String filename) {
		ByteBuffer buffer = ByteBuffer.allocate(10);
		buffer.clear();
		InputStream str = Utils.class.getClassLoader().getResourceAsStream(filename);
		if(str == null) {
			System.err.println("ERROR INPUT STREAM WAS NULL: " + filename);
			return cube;
		}
		try(PlyReader ply = new PlyReaderFile(str)) {
			ElementReader reader;
			ArrayList<Vector3f> vertexLocations = new ArrayList<>();
			ArrayList<Vector2f> textureMapping = new ArrayList<>();
			ArrayList<Integer> faces = new ArrayList<>();
			while((reader = ply.nextElementReader()) != null) {
				ElementType type = reader.getElementType();
				System.out.println(type.getName());
				
				HashSet<String> properties = new HashSet<>();
				for(Property p : type.getProperties()) {
					properties.add(p.getName());
					System.out.println(p.getName() + ": " + p.getType().toString());
				}
				
				boolean hasTextureCoords = false;
				if(properties.contains("s")) {
					hasTextureCoords = true;
				}
				if(type.getName().equals("vertex")) {
					System.out.println("reader has " + reader.getCount() + " elements");
					Element element;
					while((element = reader.readElement()) != null) {
						Vector3f loc = new Vector3f(
								(float)element.getDouble("x"), 
								(float)element.getDouble("y"), 
								(float)element.getDouble("z"));
						vertexLocations.add(loc);
						
						if(hasTextureCoords) {
							Vector2f texCoord = new Vector2f(
									(float)element.getDouble("s"), 
									1 - (float)element.getDouble("t"));
							textureMapping.add(texCoord);
						}
						else {
							textureMapping.add(new Vector2f());
						}
					}
				}
				else if(type.getName().equals("face")) {
					System.out.println("reader has " + reader.getCount() + " elements");
					Element element;
					String elementname = type.getProperties().get(0).getName();
					while((element = reader.readElement()) != null) {
						int[] aface = element.getIntList(elementname);
						for(int i = 2; i < aface.length; i++) {
							faces.add(aface[0]);
							faces.add(aface[i-1]);
							faces.add(aface[i]);
						}
					}
				}
				reader.close();
			}
			return arraysToMesh(vertexLocations, textureMapping, faces);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return star;
	}
	
	private static Mesh arraysToMesh(
			ArrayList<Vector3f> vertexLocations, 
			ArrayList<Vector2f> textureMapping, 
			ArrayList<Integer> faces) {
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
			facesArr[i] = faces.get(i);
		}
		return new Mesh(vertices, facesArr);
	}
	
	private static Mesh loadMeshFromFile(String filename) {
		String ext = filename.substring(filename.length() - 4);
		if(ext.equals(".obj")) {
			return readObjFile(filename);
		}
		else if(ext.equals(".ply")) {
			return readPlyFile(filename);
		}
		else {
			System.err.println("Unknown mesh file format: " + ext);
			return cube;
		}
	}
	static {
		cube = getMeshByFileName("models/cube.obj");
		skybox = getMeshByFileName("models/skybox.obj");
		normalize(skybox, true);
		square = getMeshByFileName("models/square.obj");
		cattail = getMeshByFileName("models/cattail.ply");
		star = getMeshByFileName("models/star.obj");
		mushroom = getMeshByFileName("models/mushroom.obj");
		house = getMeshByFileName("models/house.obj");
		x = getMeshByFileName("models/x.ply");
		y = getMeshByFileName("models/y.ply");
		z = getMeshByFileName("models/z.ply");
		defaultUnit = cube;
		defaultPlant = mushroom;
		defaultBuilding = house;
	}
}
