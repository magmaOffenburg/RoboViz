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

package rv.effects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import jsgl.jogl.GLDisposable;
import jsgl.jogl.light.DirLight;
import jsgl.jogl.view.Viewport;
import jsgl.math.vector.Vec3f;
import org.magmaoffenburg.roboviz.configuration.Config.Graphics;
import rv.content.ContentManager;
import rv.world.rendering.GLHelper;
import rv.world.rendering.ShadowMapRenderer;
import rv.world.rendering.ShadowMapRenderer.LightShadowVolume;

/**
 * Applies post-processing effects to an input texture; merge this with RENDERER?
 *
 * @author Justin Stoecker
 */
public class EffectManager implements GLDisposable
{
	private boolean disposed = false;
	private Bloom bloom;
	private ShadowMapRenderer shadowRenderer;

	public Bloom getBloom()
	{
		return bloom;
	}

	public ShadowMapRenderer getShadowRenderer()
	{
		return shadowRenderer;
	}

	public void init(GL2 gl, Viewport screen, Graphics config, ContentManager cm)
	{
		if (config.getUseBloom()) {
			initBloom(gl, screen, config, cm);
		}

		if (config.getUseShadows()) {
			initShadowRenderer(gl, config, cm);
		}
	}

	public void initBloom(GL2 gl, Viewport screen, Graphics config, ContentManager cm)
	{
		bloom = new Bloom();
		boolean success = bloom.init(gl, screen, cm, config);
		if (!success)
			bloom = null;
	}

	public void initShadowRenderer(GL2 gl, Graphics config, ContentManager cm)
	{
		// configure sun
		Vec3f lightPos = new Vec3f(-11, 10, 9);
		Vec3f lightDir = lightPos.times(-1).normalize();
		DirLight light = new DirLight(lightDir);
		LightShadowVolume sun = new LightShadowVolume(light, lightPos, new Vec3f(0, 0, 0), Vec3f.unitY(), 40, 40, 40);

		shadowRenderer = new ShadowMapRenderer(sun);
		if (!shadowRenderer.init(gl, config, cm))
			shadowRenderer = null;
	}

	public void disposeShadowRenderer(GL gl)
	{
		if (shadowRenderer != null)
			shadowRenderer.dispose(gl);
		shadowRenderer = null;
	}

	/**
	 * Renders a screen-aligned quad with identity viewing / projection matrices
	 */
	public static void renderScreenQuad(GL2 gl)
	{
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		GLHelper.renderQuad(gl);

		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	@Override
	public void dispose(GL gl)
	{
		if (bloom != null)
			bloom.dispose(gl);
		if (shadowRenderer != null)
			shadowRenderer.dispose(gl);

		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}
