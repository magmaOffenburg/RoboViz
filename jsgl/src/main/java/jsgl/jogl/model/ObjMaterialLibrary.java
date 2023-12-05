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

import java.io.*;
import java.util.ArrayList;

/**
 * A material library (.mtl) file used by an OBJ model
 *
 * @author Justin Stoecker
 */
public class ObjMaterialLibrary
{
	protected ArrayList<ObjMaterial> materials = new ArrayList<>();

	public ArrayList<ObjMaterial> getMaterials()
	{
		return materials;
	}

	public void load(BufferedReader br, String texturePath, ClassLoader cl) throws IOException 
	{
		ObjMaterial currentMaterial = null;
		String line;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			LineType lineType = getLineType(line);
			processLine(lineType, line, texturePath, cl, currentMaterial);
		}

		// end of file, so add current material to list
		if (currentMaterial != null)
			materials.add(currentMaterial);
	}

	private LineType getLineType(String line) {
		if (line.startsWith("newmtl ")) return LineType.NEW_MATERIAL;
		if (line.startsWith("Ka ")) return LineType.AMBIENT_COLOR;
		if (line.startsWith("Kd ")) return LineType.DIFFUSE_COLOR;
		if (line.startsWith("Ks ")) return LineType.SPECULAR_COLOR;
		if (line.startsWith("Ns ")) return LineType.SHININESS;
		if (line.startsWith("d ") || line.startsWith("Tr ")) return LineType.ALPHA;
		if (line.startsWith("illum ")) return LineType.ILLUMINATION_MODEL;
		if (line.startsWith("map_Kd ")) return LineType.TEXTURE_MAP;
		return LineType.UNKNOWN;
	}

	private void processLine(LineType lineType, String line, String texturePath, ClassLoader cl, ObjMaterial currentMaterial) throws IOException {
		switch (lineType) {
			case NEW_MATERIAL:
				// new material definition, so current material is finished
				if (currentMaterial != null)
					materials.add(currentMaterial);
				currentMaterial = new ObjMaterial(line.trim().split("\\s+")[1]);
				break;
			case AMBIENT_COLOR:
				currentMaterial.readAmbientColor(line);
				break;
			case DIFFUSE_COLOR:
				currentMaterial.readDiffuseColor(line);
				break;
			case SPECULAR_COLOR:
				currentMaterial.readSpecularColor(line);
				break;
			case SHININESS:
				currentMaterial.readShininess(line);
				break;
			case ALPHA:
				currentMaterial.readAlpha(line);
				break;
			case ILLUMINATION_MODEL:
				currentMaterial.readIlluminationModel(line);
				break;
			case TEXTURE_MAP:
				processTextureMap(line, texturePath, cl, currentMaterial);
				break;
			case UNKNOWN:
				break;
		}
	}

	private void processTextureMap(String line, String texturePath, ClassLoader cl, ObjMaterial currentMaterial) throws IOException {
		String textureName = line.split("\\s+")[1];
		InputStream is = null;
		if (cl != null) {
			is = cl.getResourceAsStream(texturePath + textureName);
		} else {
			is = new FileInputStream(new File("/" + texturePath, textureName));
		}
		currentMaterial.readTextureMap(is);
	}

	enum LineType {
		NEW_MATERIAL,
		AMBIENT_COLOR,
		DIFFUSE_COLOR,
		SPECULAR_COLOR,
		SHININESS,
		ALPHA,
		ILLUMINATION_MODEL,
		TEXTURE_MAP,
		UNKNOWN
	}
}