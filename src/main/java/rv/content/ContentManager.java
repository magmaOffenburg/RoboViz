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
import com.jogamp.opengl.GLAutoDrawable;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import jsgl.jogl.GLInfo;
import jsgl.jogl.ShaderProgram;
import jsgl.jogl.Texture2D;
import jsgl.jogl.model.Mesh;
import jsgl.jogl.model.MeshPart;
import jsgl.jogl.model.ObjMaterial;
import jsgl.jogl.model.ObjMaterialLibrary;
import jsgl.jogl.model.ObjMeshImporter;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config.TeamColors;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.scenegraph.Node;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.SceneGraph.SceneGraphListener;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.util.jogl.MaterialUtil;

/**
 * Loads shaders and meshes used in scene graph.
 *
 * @author justin
 */
public class ContentManager implements SceneGraphListener, GameState.GameStateChangeListener
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String MODEL_ROOT = "models/";
	public static final String TEXTURE_ROOT = "textures/";
	public static final String MATERIAL_ROOT = "materials/";

	private class ModelLoader extends Thread
	{
		private final Model model;

		public ModelLoader(Model model)
		{
			this.model = model;
		}

		public void run()
		{
			model.readMeshData(ContentManager.this);
			synchronized (ContentManager.this) {
				modelsToInitialize.add(model);
			}
		}
	}

	private final TeamColors config;

	private Mesh.RenderMode meshRenderMode = Mesh.RenderMode.IMMEDIATE;
	private Texture2D whiteTexture;
	public static Texture2D selectionTexture;
	public static Texture2D selectionTextureThin;
	private final List<Model> modelsToInitialize = new ArrayList<>();
	private final List<Model> models = new ArrayList<>();
	private ObjMaterialLibrary naoMaterialLib;

	public Texture2D getWhiteTexture()
	{
		return whiteTexture;
	}

	public Mesh.RenderMode getMeshRenderMode()
	{
		return meshRenderMode;
	}

	public ObjMaterial getMaterial(String name)
	{
		for (ObjMaterial mat : naoMaterialLib.getMaterials())
			if (mat.getName().equals(name))
				return mat;
		return null;
	}

	/**
	 * Retrieves model from content manager. If model is not found in set of loaded models, it is
	 * added to a queue and loaded.
	 */
	public synchronized Model getModel(String name)
	{
		for (Model model : models) {
			if (model.getName().equals(name)) {
				return model;
			}
		}

		// The requested mesh was not found, so we create a new one and start
		// loading it in a thread.
		Model model = new Model(name);
		models.add(model);
		new ModelLoader(model).start();

		return model;
	}

	public ContentManager(TeamColors config)
	{
		this.config = config;
	}

	public synchronized void update(GL2 gl)
	{
		// meshes need a current OpenGL context to finish initializing, so this
		// update pass checks all models that are waiting to initialize and then
		// clears the list

		if (modelsToInitialize.size() == 0)
			return;

		for (Model m : modelsToInitialize)
			m.init(gl, meshRenderMode);

		modelsToInitialize.clear();
	}

	public static void renderSelection(GL2 gl, Vec3f p, float r, float[] color, float alpha, boolean thin)
	{
		float[] colorWithAlpha = {color[0], color[1], color[2], alpha};
		gl.glColor4fv(colorWithAlpha, 0);
		if (thin)
			ContentManager.selectionTextureThin.bind(gl);
		else
			ContentManager.selectionTexture.bind(gl);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(p.x - r, 0, p.z - r);
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(p.x - r, 0, p.z + r);
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(p.x + r, 0, p.z + r);
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(p.x + r, 0, p.z - r);
		gl.glEnd();
		Texture2D.unbind(gl);
	}

	public boolean init(GLAutoDrawable drawable, GLInfo glInfo)
	{
		// use VBOs if they are supported
		if (glInfo.extSupported("GL_ARB_vertex_buffer_object")) {
			meshRenderMode = Mesh.RenderMode.VBO;
		} else {
			// display lists would be preferred, but since the Nao model is
			// shared and the materials change it would require recompilation
			// every render pass
			meshRenderMode = Mesh.RenderMode.VERTEX_ARRAYS;
		}

		whiteTexture = loadTexture(drawable.getGL(), "white.png");
		if (whiteTexture == null)
			return false;
		selectionTexture = loadTexture(drawable.getGL(), "selection.png");
		if (selectionTexture == null)
			return false;
		selectionTextureThin = loadTexture(drawable.getGL(), "selection_thin.png");
		if (selectionTextureThin == null)
			return false;

		// load nao materials
		naoMaterialLib = new ObjMaterialLibrary();
		ClassLoader cl = getClass().getClassLoader();
		InputStream is = getClass().getResourceAsStream("/materials/nao.mtl");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			naoMaterialLib.load(br, "textures/", cl);
		} catch (IOException e) {
			LOGGER.error("Unable to load Nao material library", e);
		}

		for (ObjMaterial m : naoMaterialLib.getMaterials())
			m.init(drawable.getGL().getGL2());

		return true;
	}

	public Texture2D loadTexture(GL gl, String name)
	{
		BufferedImage img;
		try {
			img = ImageIO.read(getClass().getResourceAsStream("/textures/" + name));
			return Texture2D.loadTex(gl, img);
		} catch (IOException | IllegalArgumentException e) {
			LOGGER.error("Error loading texture: " + name, e);
		}
		return null;
	}

	public Mesh loadMesh(String name)
	{
		LOGGER.debug("Loading " + name);
		String modelPath = "models/";
		String texturePath = "textures/";
		String materialPath = "materials/";
		ObjMeshImporter importer = new ObjMeshImporter(modelPath, materialPath, texturePath);
		ClassLoader cl = this.getClass().getClassLoader();

		importer.setClassLoader(cl);
		InputStream is = this.getClass().getResourceAsStream(modelPath + name);
		Mesh mesh = null;
		try {
			mesh = importer.loadMesh(new BufferedReader(new InputStreamReader(is)));
		} catch (IOException e) {
			LOGGER.error("Unable to load mesh", e);
			return null;
		}

		// this is necessary for the shader to blend meshes that have textures
		// for some parts and materials for others
		for (MeshPart p : mesh.getParts()) {
			if (p.getMaterial() instanceof ObjMaterial) {
				ObjMaterial mat = (ObjMaterial) p.getMaterial();
				if (mat.getTexture() == null)
					mat.setTexture(whiteTexture, false);
			}
		}

		return mesh;
	}

	public void dispose(GL gl)
	{
		if (whiteTexture != null)
			whiteTexture.dispose(gl);
		if (selectionTexture != null)
			selectionTexture.dispose(gl);
		for (Model model : models)
			model.dispose(gl);
	}

	public ShaderProgram loadShader(GL2 gl, String name)
	{
		String v = "shaders/" + name + ".vs";
		String f = "shaders/" + name + ".fs";
		ClassLoader cl = this.getClass().getClassLoader();
		return ShaderProgram.create(gl, v, f, cl);
	}

	@Override
	public void newSceneGraph(SceneGraph sg)
	{
		checkForMeshes(sg.getRoot());
	}

	private void checkForMeshes(Node node)
	{
		if (node instanceof StaticMeshNode) {
			StaticMeshNode meshNode = (StaticMeshNode) node;
			getModel(meshNode.getName());
		}

		if (node.getChildren() != null) {
			for (int i = 0; i < node.getChildren().size(); i++)
				checkForMeshes(node.getChildren().get(i));
		}
	}

	@Override
	public void updatedSceneGraph(SceneGraph sg)
	{
	}

	private String teamNameLeft;
	private String teamNameRight;

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
		// if team name changed, update the materials
		this.teamNameLeft = gs.getTeamLeft();
		this.teamNameRight = gs.getTeamRight();

		Color colorLeft = config.getLeftColor(this.teamNameLeft, this.teamNameRight);
		Color colorRight = config.getRightColor(this.teamNameLeft, this.teamNameRight);

		updateTeamColor(this.teamNameLeft, "matLeft", colorLeft);
		updateTeamColor(this.teamNameRight, "matRight", colorRight);
	}

	private void updateTeamColor(String teamName, String materialName, Color color)
	{
		ObjMaterial mat = getMaterial(materialName);
		MaterialUtil.setColor(mat, color);

		// For goalie
		ObjMaterial matGoalie = getMaterial(materialName + "Goalie");
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float factor = 0.45f;

		// Brighten color for goalie
		// Color colorGoalie = color.brighter();
		Color colorGoalie = new Color(r + (1 - r) * factor, g + (1 - g) * factor, b + (1 - b) * factor);

		float tooBrightThresh = 0.7f;
		float tooWhiteThresh = 0.55f;
		float tooWhiteDiffThresh = 0.2f;

		float gr = colorGoalie.getRed() / 255f;
		float gg = colorGoalie.getGreen() / 255f;
		float gb = colorGoalie.getBlue() / 255f;
		if ((gr > tooBrightThresh && gg > tooBrightThresh && gb > tooBrightThresh) ||
				(gr > tooWhiteThresh && gg > tooWhiteThresh && gb > tooWhiteThresh &&
						Math.max(gr, Math.max(gg, gb)) - Math.min(gr, Math.min(gg, gb)) < tooWhiteDiffThresh)) {
			// Darken color for goalie
			// colorGoalie = color.darker();
			colorGoalie = new Color(r + r * -factor, g + g * -factor, b + b * -factor);
		}
		LOGGER.debug("New team color for {}: {}", teamName, color);
		LOGGER.debug("New goalie color for {}: {}", teamName, colorGoalie);

		MaterialUtil.setColor(matGoalie, colorGoalie);

		ObjMaterial matNumGoalie = getMaterial(materialName + "NumGoalie");
		MaterialUtil.setColor(matNumGoalie, colorGoalie);
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
	}
}
