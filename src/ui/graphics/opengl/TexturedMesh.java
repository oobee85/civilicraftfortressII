package ui.graphics.opengl;

public class TexturedMesh {
	public Mesh mesh;
	public String textureFile;
	public TexturedMesh(Mesh mesh, String textureFile) {
		this.mesh = mesh;
		this.textureFile = textureFile;
	}
	public Mesh getMesh() {
		return mesh;
	}
	public String getTextureFile() {
		return textureFile;
	}
}
