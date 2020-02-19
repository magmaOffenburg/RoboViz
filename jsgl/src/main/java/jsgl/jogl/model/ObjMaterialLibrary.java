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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
			if (line.startsWith("newmtl ")) {
				// new material definition, so current material is finished
				if (currentMaterial != null)
					materials.add(currentMaterial);
				currentMaterial = new ObjMaterial(line.trim().split("\\s+")[1]);
			} else if (line.startsWith("Ka ")) {
				currentMaterial.readAmbientColor(line);
			} else if (line.startsWith("Kd ")) {
				currentMaterial.readDiffuseColor(line);
			} else if (line.startsWith("Ks ")) {
				currentMaterial.readSpecularColor(line);
			} else if (line.startsWith("Ns ")) {
				currentMaterial.readShininess(line);
			} else if (line.startsWith("d ") || line.startsWith("Tr ")) {
				currentMaterial.readAlpha(line);
			} else if (line.startsWith("illum ")) {
				currentMaterial.readIlluminationModel(line);
			} else if (line.startsWith("map_Kd ")) {
				String textureName = line.split("\\s+")[1];
				InputStream is = null;
				if (cl != null) {
					is = cl.getResourceAsStream(texturePath + textureName);
				} else {
					is = new FileInputStream(new File("/" + texturePath, textureName));
				}

				currentMaterial.readTextureMap(is);
			}
		}

		// end of file, so add current material to list
		if (currentMaterial != null)
			materials.add(currentMaterial);
	}
}
