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

package rv.world.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.List;
import jsgl.jogl.ShaderProgram;
import jsgl.math.vector.Matrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.content.Model;
import rv.world.WorldModel;

/**
 * Renders world model using Phong shading with no shadows
 *
 * @author justin
 */
public class PhongWorldRenderer implements SceneRenderer
{
	private static final Logger LOGGER = LogManager.getLogger();

	private ContentManager content;

	private ShaderProgram shader;
	private final List<String> suppressedMeshes = new ArrayList<>();

	@Override
	public boolean init(GL2 gl, Config.Graphics graphics, ContentManager cm)
	{
		this.content = cm;

		shader = cm.loadShader(gl, "phong");
		if (shader == null) {
			graphics.setUsePhong(false);
			LOGGER.error("Phong shader failed to load!");
		}

		if (shader == null) {
			graphics.setUsePhong(false);
			return false;
		}

		suppressedMeshes.add("field.obj");
		suppressedMeshes.add("skybox.obj");

		return true;
	}

	private void renderSceneGraphNode(GL2 gl, StaticMeshNode node, ContentManager content)
	{
		Model model = content.getModel(node.getName());
		if (model.isLoaded()) {
			// NOTE: this is a hack to avoid rendering certain meshes that are
			// replaced by
			// RoboViz; in particular, the field and skybox are treated
			// differently
			for (String s : suppressedMeshes)
				if (node.getName().endsWith(s))
					return;

			BasicSceneRenderer.applyAgentMats(model, node, content);

			Matrix modelMat = WorldModel.COORD_TFN.times(node.getWorldTransform());
			model.getMesh().render(gl, modelMat);
		}
	}

	public void render(GL2 gl, WorldModel world, Drawings drawings)
	{
		if (world.getSceneGraph() == null)
			return;

		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glColor3f(1, 1, 1);
		world.getSkyBox().render(gl);

		gl.glEnable(GL.GL_DEPTH_TEST);
		world.getLighting().apply(gl);

		shader.enable(gl);

		gl.glDepthMask(false);
		world.getField().render(gl);
		gl.glDepthMask(true);

		List<StaticMeshNode> transparentNodes = new ArrayList<>();
		List<StaticMeshNode> nodes = world.getSceneGraph().getAllMeshNodes();
		for (StaticMeshNode node : nodes) {
			if (node.isTransparent())
				transparentNodes.add(node);
			else
				renderSceneGraphNode(gl, node, content);
		}

		shader.disable(gl);
		gl.glEnable(GL.GL_BLEND);
		if (world.getSelectedObject() != null)
			world.getSelectedObject().renderSelected(gl);
		world.renderBallCircle(gl);
		if (drawings.isVisible())
			drawings.render(gl, Renderer.Companion.getGlut());
		shader.enable(gl);

		// transparent stuff

		for (StaticMeshNode transparentNode : transparentNodes)
			renderSceneGraphNode(gl, transparentNode, content);
		gl.glDisable(GL.GL_BLEND);

		shader.disable(gl);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

	@Override
	public void dispose(GL gl)
	{
		shader.dispose(gl);
	}

	@Override
	public String toString()
	{
		return "Phong Renderer";
	}
}
