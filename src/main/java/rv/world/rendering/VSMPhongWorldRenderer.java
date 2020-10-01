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
import jsgl.math.vector.Matrix;
import org.magmaoffenburg.roboviz.configuration.Config;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.content.Model;
import rv.effects.EffectManager;
import rv.effects.VSMPhongShader;
import rv.world.WorldModel;

/**
 * Renders world model scene using variance shadow mapping with Phong shading
 *
 * @author justin
 */
public class VSMPhongWorldRenderer implements SceneRenderer
{
	private ContentManager content;
	private final EffectManager effects;
	private VSMPhongShader shader;
	private final List<String> suppressedMeshes = new ArrayList<>();

	public VSMPhongShader getShader()
	{
		return shader;
	}

	public VSMPhongWorldRenderer(EffectManager effects)
	{
		this.effects = effects;
	}

	@Override
	public boolean init(GL2 gl, Config.Graphics graphics, ContentManager cm)
	{
		this.content = cm;

		shader = VSMPhongShader.create(gl);
		if (shader == null) {
			graphics.setUseShadows(false);
			return false;
		}

		shader.enable(gl);
		shader.setLightViewProjection(gl, effects.getShadowRenderer().getLight().getViewProjection());
		shader.disable(gl);

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
			shader.setModelMatrix(gl, modelMat);

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
		shader.setShadowMap(gl, effects.getShadowRenderer().getShadowMap());

		shader.setModelMatrix(gl, world.getField().getModelMatrix());
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

		// drawings
		gl.glEnable(GL.GL_BLEND);
		shader.disable(gl);
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
	}

	@Override
	public void dispose(GL gl)
	{
		shader.dispose(gl);
	}

	@Override
	public String toString()
	{
		return "VSM Phong Renderer";
	}
}
