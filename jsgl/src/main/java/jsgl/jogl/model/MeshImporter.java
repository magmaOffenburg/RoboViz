package jsgl.jogl.model;

import java.io.IOException;
import java.io.InputStream;

public abstract class MeshImporter
{
	// Locations where files may be found. If classLoader is set, the files
	// are loaded from the class loader; otherwise, files are located on disk
	protected String materialPath;
	protected String texturePath;
	protected String modelPath;
	protected ClassLoader classLoader;

	public void setClassLoader(ClassLoader cl)
	{
		this.classLoader = cl;
	}

	public MeshImporter(String modelPath)
	{
		this.modelPath = modelPath;
	}

	public MeshImporter(String modelPath, String materialPath)
	{
		this(modelPath);
		this.materialPath = materialPath;
	}

	public MeshImporter(String modelPath, String materialPath, String texturePath)
	{
		this(modelPath, materialPath);
		this.texturePath = texturePath;
	}

	public abstract Mesh loadMesh(InputStream is) throws IOException;
}
