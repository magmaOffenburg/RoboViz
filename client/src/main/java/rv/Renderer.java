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

package rv;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import roboviz.jsgl.jogl.FrameBufferObject;
import roboviz.jsgl.jogl.GLInfo;
import roboviz.jsgl.jogl.Texture2D;
import roboviz.jsgl.jogl.view.Camera3D;
import roboviz.jsgl.jogl.view.Viewport;
import rv.Viewer.WindowResizeEvent;
import rv.Viewer.WindowResizeListener;
import rv.content.ContentManager;
import rv.effects.EffectManager;
import rv.world.rendering.BasicSceneRenderer;
import rv.world.rendering.PhongWorldRenderer;
import rv.world.rendering.SceneRenderer;
import rv.world.rendering.ShadowMapRenderer;
import rv.world.rendering.VSMPhongWorldRenderer;

/**
 * Controls all rendering for the main RoboViz window
 *
 * @author justin
 */
public class Renderer implements WindowResizeListener
{
	public static final GLU glu = new GLU();
	public static final GLUT glut = new GLUT();
	private FrameBufferObject sceneFBO;
	private EffectManager effectManager;
	private final Viewer viewer;
	private final Configuration.Graphics graphics;
	private SceneRenderer sceneRenderer;
	private Camera3D vantage;

	// this FBO is only used if bloom and FSAA are enabled at the same time
	private FrameBufferObject msSceneFBO;
	private int numSamples = -1;

	public void setVantage(Camera3D vantage)
	{
		this.vantage = vantage;
	}

	public Camera3D getVantage()
	{
		return vantage;
	}

	public EffectManager getEffectManager()
	{
		return effectManager;
	}

	public Renderer(Viewer viewer)
	{
		this.viewer = viewer;
		this.graphics = viewer.getConfig().graphics;
	}

	public void init(GLAutoDrawable drawable, ContentManager cm, GLInfo info)
	{
		boolean supportAAFBO =
				info.extSupported("GL_EXT_framebuffer_multisample") && info.extSupported("GL_EXT_framebuffer_blit");

		if (graphics.useFsaa && !supportAAFBO)
			System.out.println("Warning: no support for FSAA while bloom enabled");
		boolean useFSAA = graphics.useFsaa && (supportAAFBO && graphics.useBloom || !graphics.useBloom);

		if (graphics.useVsync)
			drawable.getGL().setSwapInterval(1);
		if (useFSAA)
			drawable.getGL().glEnable(GL.GL_MULTISAMPLE);

		effectManager = new EffectManager();
		viewer.addWindowResizeListener(this);
		effectManager.init(drawable.getGL().getGL2(), viewer, viewer.getScreen(), viewer.getConfig().graphics, cm);

		if (graphics.useBloom) {
			if (useFSAA)
				numSamples = graphics.fsaaSamples;
			// if we do post-processing we'll need an FBO for the scene
			genFBO(drawable.getGL().getGL2(), viewer.getScreen());
		}

		selectRenderer(drawable.getGL().getGL2(), cm);

		drawable.getGL().setSwapInterval(viewer.getConfig().graphics.useVsync ? 1 : 0);

		vantage = viewer.getUI().getCamera();
	}

	/**
	 * Find best match for world renderer given user's graphics configuration
	 */
	private void selectRenderer(GL2 gl, ContentManager cm)
	{
		while (sceneRenderer == null) {
			if (graphics.useShadows)
				sceneRenderer = new VSMPhongWorldRenderer(effectManager);
			else if (graphics.usePhong)
				sceneRenderer = new PhongWorldRenderer();
			else
				sceneRenderer = new BasicSceneRenderer();

			if (!sceneRenderer.init(gl, graphics, cm)) {
				System.err.println("Could not initialize " + sceneRenderer);
				sceneRenderer = null;
			}
		}
	}

	private void genFBO(GL2 gl, Viewport vp)
	{
		if (numSamples > 0) {
			msSceneFBO = FrameBufferObject.create(gl, vp.w, vp.h, GL.GL_RGBA, numSamples);
			sceneFBO = FrameBufferObject.createNoDepth(gl, vp.w, vp.h, GL.GL_RGB8);
		} else {
			sceneFBO = FrameBufferObject.create(gl, vp.w, vp.h, GL.GL_RGB);
		}
	}

	public void render(GLAutoDrawable drawable, Configuration.Graphics config)
	{
		synchronized (viewer.getWorldModel())
		{
			GL2 gl = drawable.getGL().getGL2();

			if (config.useShadows) {
				ShadowMapRenderer shadowRenderer = effectManager.getShadowRenderer();
				shadowRenderer.render(gl, viewer.getWorldModel(), viewer.getDrawings());
			}

			if (graphics.useStereo) {
				vantage.applyLeft(gl, glu, viewer.getScreen());
				gl.glDrawBuffer(GL2.GL_BACK_LEFT);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				drawScene(gl);

				vantage.applyRight(gl, glu, viewer.getScreen());
				gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				drawScene(gl);

				gl.glDrawBuffer(GL.GL_BACK);
				viewer.getUI().render(gl, glu, glut);
			} else {
				gl.glDrawBuffer(GL.GL_BACK);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

				vantage.apply(gl, glu, viewer.getScreen());

				drawScene(gl);

				viewer.getUI().render(gl, glu, glut);
			}
		}
	}

	private void drawScene(GL2 gl)
	{
		if (graphics.useBloom) {
			if (msSceneFBO != null) {
				msSceneFBO.bind(gl);
				msSceneFBO.clear(gl);
				sceneFBO.setViewport(gl);
				sceneRenderer.render(gl, viewer.getWorldModel(), viewer.getDrawings());

				int w = sceneFBO.getColorTexture(0).getWidth();
				int h = sceneFBO.getColorTexture(0).getHeight();
				gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, msSceneFBO.getID());
				gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, sceneFBO.getID());
				gl.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
				msSceneFBO.unbind(gl);
			} else {
				sceneFBO.bind(gl);
				sceneFBO.clear(gl);
				sceneFBO.setViewport(gl);
				sceneRenderer.render(gl, viewer.getWorldModel(), viewer.getDrawings());
				sceneFBO.unbind(gl);
			}

			// post processing
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glEnable(GL.GL_TEXTURE_2D);
			Texture2D output = effectManager.getBloom().process(gl, sceneFBO.getColorTexture(0));
			gl.glColor4f(1, 1, 1, 1);

			// render result to window
			viewer.getScreen().apply(gl);
			output.bind(gl);
			EffectManager.renderScreenQuad(gl);
			Texture2D.unbind(gl);
		} else {
			viewer.getScreen().apply(gl);
			sceneRenderer.render(gl, viewer.getWorldModel(), viewer.getDrawings());
		}
	}

	public void dispose(GL gl)
	{
		if (effectManager != null)
			effectManager.dispose(gl);
		if (sceneFBO != null)
			sceneFBO.dispose(gl);
		if (msSceneFBO != null)
			msSceneFBO.dispose(gl);
		if (sceneRenderer != null)
			sceneRenderer.dispose(gl);
	}

	@Override
	public void windowResized(WindowResizeEvent event)
	{
		if (viewer.getConfig().graphics.useBloom || viewer.getConfig().graphics.useShadows) {
			if (sceneFBO != null)
				sceneFBO.dispose(event.getDrawable().getGL());
			if (msSceneFBO != null)
				msSceneFBO.dispose(event.getDrawable().getGL());
			genFBO(event.getDrawable().getGL().getGL2(), event.getWindow());
		}
	}
}
