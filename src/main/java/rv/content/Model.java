/*
 *  Copyright 2011 RoboViz
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

package rv.content;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import jsgl.jogl.model.Mesh;
import jsgl.jogl.model.MeshPart;
import jsgl.jogl.model.ObjMaterial;
import jsgl.jogl.model.ObjMeshImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Named mesh loaded by and managed by the content manager.
 *
 * @author justin
 */
public class Model
{
	private static final Logger LOGGER = LogManager.getLogger();

	private Mesh mesh;
	private boolean loaded = false;
	private final String name;

	public Mesh getMesh()
	{
		return mesh;
	}

	public String getName()
	{
		return name;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	/**
	 * Creates a new content managed model
	 *
	 * @param name
	 *            - path to model within content root directory. For example:<br>
	 *            "../resources/models/lfoot.obj" should be "models/lfoot.obj"<br>
	 *            "../resources/models/new/rfoot.obj" should be "models/new/rfoot.obj"
	 */
	public Model(String name)
	{
		this.name = name;
	}

	public void readMeshData(ContentManager cm)
	{
		ObjMeshImporter importer = new ObjMeshImporter(
				ContentManager.MODEL_ROOT, ContentManager.MATERIAL_ROOT, ContentManager.TEXTURE_ROOT);
		ClassLoader cl = this.getClass().getClassLoader();
		importer.setClassLoader(cl);

		InputStream is = cl.getResourceAsStream(name);
		if (is == null) {
			failureMessage();
			return;
		}
		mesh = null;
		try {
			mesh = importer.loadMesh(new BufferedReader(new InputStreamReader(is)));
		} catch (IOException e) {
			failureMessage();
		}

		// this is necessary for the shader to blend meshes that have
		// textures for some parts and only color materials for others
		for (MeshPart p : mesh.getParts()) {
			if (p.getMaterial() instanceof ObjMaterial) {
				ObjMaterial mat = (ObjMaterial) p.getMaterial();
				if (mat.getTexture() == null && mat.getTextureSource() == null) {
					mat.setTexture(cm.getWhiteTexture(), false);
				}
			}
		}
	}

	private void failureMessage()
	{
		LOGGER.debug("Failed to load " + name);
	}

	/**
	 * Copies material information from src material to target material
	 */
	public void replaceMaterial(String target, ObjMaterial src)
	{
		for (MeshPart part : mesh.getParts()) {
			ObjMaterial mat = (ObjMaterial) part.getMaterial();
			if (mat.getName().equals(target)) {
				if (src.getTexture() != null)
					mat.setTexture(src.getTexture(), false);
				mat.setAmbient(src.getAmbient());
				mat.setDiffuse(src.getDiffuse());
				mat.setSpecular(src.getSpecular());
				mat.setShininess(src.getShininess());
				return;
			}
		}
	}

	public void init(GL2 gl, Mesh.RenderMode mode)
	{
		if (!loaded && mesh != null) {
			mesh.init(gl, mode);
			loaded = true;
		}
	}

	public void dispose(GL gl)
	{
		if (mesh != null)
			mesh.dispose(gl);
	}
}