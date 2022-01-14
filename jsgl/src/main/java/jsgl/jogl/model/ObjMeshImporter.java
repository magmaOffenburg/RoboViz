/*
 *  Copyright 2011 Justin Stoecker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jsgl.jogl.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Imports OBJ model as a standard Mesh object. Resources can be loaded from
 * within jar file by setting classLoader.
 *
 * @author Justin Stoecker
 */
public class ObjMeshImporter
{
	private static final Logger LOGGER = LogManager.getLogger();

	// Locations where files may be found. If classLoader is set, the files
	// are loaded from the class loader; otherwise, files are located on disk
	private String materialPath;
	private String texturePath;
	private String modelPath;
	private ClassLoader classLoader;

	// While reading keep track of the min/max vertex position values to
	// determine the bounding box for the mesh
	private Vec3f min;
	private Vec3f max;

	// These data structures store information being read from the OBJ file
	private ObjMaterialLibrary materialLib;
	private ArrayList<float[]> v;
	private ArrayList<float[]> vn;
	private ArrayList<float[]> vt;

	// OBJ files can have separate indices for vertex positions, normals, and
	// texture coordinates, but OpenGL vertex buffer objects cannot. This maps
	// an index combination in the OBJ file to a vertex index in the mesh.
	private HashMap<String, Integer> vertIndexMap;

	// The mesh being constructed and the current part / material being used
	private Mesh mesh;
	private MeshMaterial curMeshMaterial;
	private MeshPart curMeshPart;

	public void setClassLoader(ClassLoader cl)
	{
		this.classLoader = cl;
	}

	public ObjMeshImporter(String modelPath)
	{
		this.modelPath = modelPath;
	}

	public ObjMeshImporter(String modelPath, String materialPath)
	{
		this(modelPath);
		this.materialPath = materialPath;
	}

	public ObjMeshImporter(String modelPath, String materialPath, String texturePath)
	{
		this(modelPath, materialPath);
		this.texturePath = texturePath;
	}

	public Mesh loadMesh(BufferedReader br) throws IOException
	{
		initDataStructures();

		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();

			if (line.startsWith("v "))
				readVertexPosition(line);
			else if (line.startsWith("vn "))
				readNormalVector(line);
			else if (line.startsWith("vt "))
				readTextureCoordinate(line);
			else if (line.startsWith("f "))
				readFace(line);
			else if (line.startsWith("mtllib "))
				readMaterialLibrary(line);
			else if (line.startsWith("usemtl "))
				changeMaterial(line);
		}

		// remove any empty parts
		for (int i = 0; i < mesh.getParts().size(); i++) {
			if (mesh.getParts().get(i).getFaces().size() == 0)
				mesh.getParts().remove(i);
		}

		mesh.setBounds(new BoundingBox(min, max));

		return mesh;
	}

	private void initDataStructures()
	{
		v = new ArrayList<>();
		vn = new ArrayList<>();
		vt = new ArrayList<>();
		min = new Vec3f(Float.POSITIVE_INFINITY);
		max = new Vec3f(Float.NEGATIVE_INFINITY);

		vertIndexMap = new HashMap<>();

		// If these directories are null, it is assumed that textures and
		// materials are either not used or located in the same directory as
		// the .obj files
		if (materialPath == null)
			materialPath = modelPath;
		if (texturePath == null)
			texturePath = modelPath;

		// Any face read from the OBJ file has the current material applied to
		// it. If no material has been set, it uses a default material. Faces
		// are organized into MeshPart objects by the material they use.
		mesh = new Mesh();
		curMeshMaterial = new ObjMaterial("Default");
		createNewMeshPart();

		materialLib = new ObjMaterialLibrary();
	}

	private void readVertexPosition(String line)
	{
		float[] vertexPosition = ObjModel.readFloatValues(line);
		if (vertexPosition.length > 2) {
			if (vertexPosition[0] > max.x)
				max.x = vertexPosition[0];
			if (vertexPosition[1] > max.y)
				max.y = vertexPosition[1];
			if (vertexPosition[2] > max.z)
				max.z = vertexPosition[2];
			if (vertexPosition[0] < min.x)
				min.x = vertexPosition[0];
			if (vertexPosition[1] < min.y)
				min.y = vertexPosition[1];
			if (vertexPosition[2] < min.z)
				min.z = vertexPosition[2];
		}
		v.add(vertexPosition);
	}

	private void readNormalVector(String line)
	{
		vn.add(ObjModel.readFloatValues(line));
	}

	private void readTextureCoordinate(String line)
	{
		vt.add(ObjModel.readFloatValues(line));
	}

	private void readFace(String line)
	{
		// Each OBJ face has a set of vertex, normal, and texcoord indices
		// ex. "f 1/1/1 3/4/2 5/4/4"
		String[] indexCombos = line.split("\\s+");

		// This face object contains the processed indexCombos in integer form
		ObjModel.Face objFace = new ObjModel.Face(line, null);

		// Go through each index combination in the OBJ face and convert it to
		// an index in the mesh.
		int[] meshFaceIndices = new int[objFace.vertIndices.length];
		for (int i = 0; i < objFace.vertIndices.length; i++) {
			int[] vI = objFace.vertIndices;
			int[] nI = objFace.normalIndices;
			int[] tI = objFace.texCoordIndices;

			// Use the string form of the index combination from the OBJ face as
			// key for the hash map. Offset by 1 since indexCombos[0] = "f"
			String key = indexCombos[i + 1];

			// If this key isn't in the map, the vertex has not been seen yet
			// and should be added to the mesh
			Integer vertIndex = vertIndexMap.get(key);
			if (vertIndex == null) {
				float[] vPos = v.get(vI[i]);
				float[] vNormal = nI == null ? null : vn.get(nI[i]);
				float[] vTexCoords = tI == null ? null : vt.get(tI[i]);
				MeshVertex vertex = new MeshVertex(vPos, vNormal, vTexCoords);

				vertIndex = mesh.vertices.size();
				vertIndexMap.put(key, vertIndex);
				mesh.addVertex(vertex);
			}

			// add the vertex index to current mesh face
			meshFaceIndices[i] = vertIndex;
		}

		curMeshPart.addFace(new MeshFace(meshFaceIndices));
	}

	private void readMaterialLibrary(String line)
	{
		String libName = line.split("\\s+")[1];

		BufferedReader br = null;
		if (classLoader != null) {
			String fullName = materialPath + libName;
			InputStream is = classLoader.getResourceAsStream(fullName);
			br = new BufferedReader(new InputStreamReader(is));
		} else {
			File libFile = new File(materialPath, libName);
			try {
				br = new BufferedReader(new FileReader(libFile));
			} catch (FileNotFoundException e) {
				LOGGER.error("File not found", e);
			}
		}

		try {
			materialLib.load(br, texturePath, classLoader);
		} catch (IOException e) {
			LOGGER.error("Unable to load material library", e);
		}
	}

	private void createNewMeshPart()
	{
		curMeshPart = new MeshPart();
		curMeshPart.setMaterial(curMeshMaterial);
		mesh.addPart(curMeshPart);
	}

	private void changeMaterial(String line)
	{
		String requestedMaterial = line.split("\\s+")[1];

		// if it's the same material being used, ignore
		if (requestedMaterial.equals(curMeshMaterial.name)) {
			return;
		}

		// if material has been used before, change to the part that uses it
		for (MeshPart part : mesh.getParts()) {
			if (requestedMaterial.equals(part.getMaterial().name)) {
				curMeshPart = part;
				curMeshMaterial = part.getMaterial();
				return;
			}
		}

		// Material hasn't been used before, so create a new MeshPart for it
		// and add the previous part being worked on to the mesh
		createNewMeshPart();

		// Select material from library and apply it to new part
		for (ObjMaterial material : materialLib.materials) {
			if (material.name.equals(requestedMaterial)) {
				curMeshMaterial = material;
				break;
			}
		}

		curMeshPart.setMaterial(curMeshMaterial);
	}
}
