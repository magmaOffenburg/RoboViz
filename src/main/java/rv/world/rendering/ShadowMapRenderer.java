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
import jsgl.jogl.FrameBufferObject;
import jsgl.jogl.RenderBuffer;
import jsgl.jogl.ShaderProgram;
import jsgl.jogl.Texture2D;
import jsgl.jogl.light.DirLight;
import jsgl.math.Gaussian;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.content.Model;
import rv.world.WorldModel;

/**
 * Variance shadow mapping
 *
 * @author justin
 */
public class ShadowMapRenderer implements SceneRenderer
{
	/**
	 * Encapsulates a directional light that casts shadow within an orthographic frustum
	 */
	public static class LightShadowVolume
	{
		private final DirLight light;
		private final Matrix view;
		private final Matrix projection;
		private final Matrix viewProjection;

		public Matrix getView()
		{
			return view;
		}

		public Matrix getProjection()
		{
			return projection;
		}

		public Matrix getViewProjection()
		{
			return viewProjection;
		}

		public DirLight getLight()
		{
			return light;
		}

		public LightShadowVolume(
				DirLight light, Vec3f eye, Vec3f target, Vec3f up, float width, float height, float depth)
		{
			this.light = light;

			double hw = width / 2;
			double hh = height / 2;
			projection = Matrix.createOrtho(-hw, hw, -hh, hh, 0.0, depth);
			view = Matrix.createLookAt(eye.x, eye.y, eye.z, target.x, target.y, target.z, up.x, up.y, up.z);
			viewProjection = projection.times(view);
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();
	private final static int TEX_FORMAT = GL2.GL_RG32F;

	private ContentManager content;
	private final static float BLURRINESS = 1.0f;
	private final static int SAMPLES = 5;
	private int texWidth;
	private int texHeight;
	private Texture2D shadowMapTexture;

	private FrameBufferObject shadowFBO;
	private FrameBufferObject blurFBO;

	private ShaderProgram depthShader;
	private ShaderProgram blurShader;

	private Gaussian.BlurParams[] blurParams;
	private int ulocBlurWeights;
	private int ulocBlurOffsets;

	private boolean useBlur = true;
	private final LightShadowVolume light;

	public LightShadowVolume getLight()
	{
		return light;
	}

	public Texture2D getShadowMap()
	{
		return shadowMapTexture;
	}

	public ShadowMapRenderer(LightShadowVolume light)
	{
		this.light = light;
	}

	private Texture2D createTexture(GL2 gl)
	{
		Texture2D tex = Texture2D.generate(gl);
		tex.bind(gl);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		tex.texImage(gl, 0, TEX_FORMAT, texWidth, texHeight, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		Texture2D.unbind(gl);

		return tex;
	}

	private FrameBufferObject createShadowFBO(GL2 gl)
	{
		FrameBufferObject fbo = FrameBufferObject.generate(gl);
		fbo.bind(gl);
		Texture2D colorTexture = createTexture(gl);
		fbo.attachColorTarget(gl, colorTexture, 0, 0, true);
		RenderBuffer depthRBO = RenderBuffer.createDepthBuffer(gl, texWidth, texHeight);

		fbo.attachDepthTarget(gl, depthRBO, true);
		int fboStatus = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		fbo.unbind(gl);

		if (fboStatus != GL.GL_FRAMEBUFFER_COMPLETE) {
			LOGGER.error("Error creating shadow FBO - disabling shadows");
			fbo.dispose(gl);
			fbo = null;
		}

		return fbo;
	}

	private FrameBufferObject createBlurFBO(GL2 gl)
	{
		// can change w/h of textures for downsampling maybe

		Texture2D hBlurTexture = createTexture(gl);
		Texture2D vBlurTexture = createTexture(gl);

		FrameBufferObject fbo = FrameBufferObject.generate(gl);
		fbo.bind(gl);
		fbo.attachColorTarget(gl, hBlurTexture, 0, 0, true);
		fbo.attachColorTarget(gl, vBlurTexture, 0, 1, true);

		RenderBuffer rbo = RenderBuffer.createDepthBuffer(gl, texWidth, texHeight);
		fbo.attachDepthTarget(gl, rbo, true);
		int fboStatus = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		fbo.unbind(gl);

		if (fboStatus != GL.GL_FRAMEBUFFER_COMPLETE) {
			LOGGER.error("Error creating blur FBO - disabling shadows");
			fbo.dispose(gl);
			fbo = null;
		}

		return fbo;
	}

	private boolean abortInit(GL2 gl, String error, Config.Graphics config)
	{
		LOGGER.error("Shadow Map: " + error);
		dispose(gl);
		config.setUseShadows(false);
		return false;
	}

	public void render(GL2 gl, WorldModel wm, Drawings drawings)
	{
		if (wm.getSceneGraph() == null)
			return;

		shadowMapTexture = renderShadowMap(gl, wm, drawings);
		if (useBlur)
			shadowMapTexture = blurShadowMap(gl);
	}

	private Texture2D renderShadowMap(GL2 gl, WorldModel world, Drawings drawings)
	{
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixd(light.getProjection().wrap());
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadMatrixd(light.getView().wrap());
		gl.glEnable(GL.GL_DEPTH_TEST);

		shadowFBO.bind(gl);
		shadowFBO.setViewport(gl);
		shadowFBO.clear(gl);
		depthShader.enable(gl);

		world.getField().render(gl);

		List<StaticMeshNode> transparentNodes = new ArrayList<>();
		List<StaticMeshNode> nodes = world.getSceneGraph().getAllMeshNodes();
		for (StaticMeshNode node : nodes) {
			if (node.isTransparent())
				transparentNodes.add(node);
			else {
				Model model = content.getModel(node.getName());
				if (model.isLoaded()) {
					Matrix modelMat = WorldModel.COORD_TFN.times(node.getWorldTransform());
					model.getMesh().render(gl, modelMat);
				}
			}
		}

		gl.glEnable(GL.GL_BLEND);
		for (StaticMeshNode node : transparentNodes) {
			Model model = content.getModel(node.getName());
			if (model.isLoaded()) {
				Matrix modelMat = WorldModel.COORD_TFN.times(node.getWorldTransform());
				model.getMesh().render(gl, modelMat);
			}
		}
		gl.glDisable(GL.GL_BLEND);

		depthShader.disable(gl);
		shadowFBO.unbind(gl);

		return shadowFBO.getColorTexture(0);
	}

	private Texture2D blurShadowMap(GL2 gl)
	{
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL.GL_TEXTURE_2D);
		shadowMapTexture.bind(gl);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_LIGHTING);

		blurFBO.bind(gl);
		blurFBO.setViewport(gl);
		blurFBO.clear(gl);
		blurShader.enable(gl);

		// horizontal pass
		gl.glUniform2fv(ulocBlurOffsets, blurParams[0].offsets.length / 2, blurParams[0].offsets, 0);
		gl.glUniform1fv(ulocBlurWeights, blurParams[0].weights.length, blurParams[0].weights, 0);
		gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		GLHelper.renderQuad(gl);
		blurFBO.getColorTexture(1).bind(gl);

		// vertical pass
		gl.glUniform2fv(ulocBlurOffsets, blurParams[1].offsets.length / 2, blurParams[1].offsets, 0);
		gl.glUniform1fv(ulocBlurWeights, blurParams[1].weights.length, blurParams[1].weights, 0);
		gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		GLHelper.renderQuad(gl);

		blurShader.disable(gl);
		blurFBO.unbind(gl);
		gl.glEnable(GL.GL_DEPTH_TEST);
		Texture2D.unbind(gl);

		return blurFBO.getColorTexture(0);
	}

	@Override
	public void dispose(GL gl)
	{
		if (shadowFBO != null)
			shadowFBO.dispose(gl);
		if (blurFBO != null)
			blurFBO.dispose(gl);
		if (depthShader != null)
			depthShader.dispose(gl);
		if (blurShader != null)
			blurShader.dispose(gl);
	}

	@Override
	public boolean init(GL2 gl, Config.Graphics conf, ContentManager cm)
	{
		this.content = cm;
		this.useBlur = conf.getUseSoftShadows();

		texWidth = texHeight = conf.getShadowResolution();

		// generate FBOs
		shadowFBO = createShadowFBO(gl);
		if (shadowFBO == null)
			return abortInit(gl, "could not create shadow FBO", conf);
		blurFBO = createBlurFBO(gl);

		ClassLoader cl = getClass().getClassLoader();
		depthShader = ShaderProgram.create(gl, "shaders/vsm_depth.vert", "shaders/vsm_depth.frag", cl);
		if (depthShader == null)
			return abortInit(gl, "could not load depth pass shader", conf);

		if (useBlur) {
			if (blurFBO == null)
				return abortInit(gl, "could not create shadow FBO", conf);
			blurShader = ShaderProgram.create(gl, "shaders/vsm_blur.vert", "shaders/vsm_blur.frag", cl);
			if (blurShader == null)
				return abortInit(gl, "could not load blur pass shader", conf);

			// configure blur shader
			blurParams = Gaussian.calcBlurParams(BLURRINESS, SAMPLES, texWidth, texHeight);
			blurShader.enable(gl);
			ulocBlurWeights = blurShader.getUniform(gl, "weights");
			ulocBlurOffsets = blurShader.getUniform(gl, "offsets");
			blurShader.disable(gl);
		}
		return true;
	}
}
