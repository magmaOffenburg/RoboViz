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
import jsgl.jogl.FrameBufferObject;
import jsgl.jogl.GLDisposable;
import jsgl.jogl.ShaderProgram;
import jsgl.jogl.Texture2D;
import jsgl.jogl.Uniform;
import jsgl.jogl.view.Viewport;
import jsgl.math.Gaussian;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config;
import rv.content.ContentManager;
import rv.util.WindowResizeEvent;
import rv.util.WindowResizeListener;

/**
 * Bloom post-processing effect
 *
 * @author Justin Stoecker
 */
public class Bloom implements GLDisposable, WindowResizeListener
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed = false;

	private ShaderProgram luminosityShader;
	private ShaderProgram blurShader;
	private ShaderProgram compositeShader;

	private Uniform.Float luminosityThreshold;
	private Uniform.Float compositeIntensity;

	// --Commented out by Inspection (07.05.2015 20:41):private Uniform.Int compositeTex2;

	private final FrameBufferObject[] fullSizeFBOs = new FrameBufferObject[2];
	private final FrameBufferObject[] halfSizeFBOs = new FrameBufferObject[2];

	private Gaussian.BlurParams[] blurParams;
	private final static float BLURRINESS = 1.5f;
	private final static int SAMPLES = 15;
	private final static float INTENSITY = 2.4f;
	private final static float THRESHOLD = 0.85f;
	private int ulocBlurOffsets;
	private int ulocBlurWeights;

	public void setBlurParams(int w, int h)
	{
		this.blurParams = Gaussian.calcBlurParams(BLURRINESS, SAMPLES, w, h);
	}

	public Bloom()
	{
	}

	public boolean init(GL2 gl, Viewport screen, ContentManager cm, Config.Graphics config)
	{
		luminosityShader = cm.loadShader(gl, "luminosity");
		if (luminosityShader == null) {
			abortInit(gl, "could not compile luminosity shader", config);
			return false;
		}

		blurShader = cm.loadShader(gl, "blur");
		if (blurShader == null) {
			abortInit(gl, "could not compile blur shader", config);
			return false;
		}

		compositeShader = cm.loadShader(gl, "composite");
		if (compositeShader == null) {
			abortInit(gl, "could not compile composite shader", config);
			return false;
		}

		luminosityShader.enable(gl);
		luminosityThreshold = new Uniform.Float(gl, luminosityShader, "threshold", THRESHOLD);
		luminosityShader.disable(gl);

		blurShader.enable(gl);
		ulocBlurOffsets = blurShader.getUniform(gl, "offsets");
		ulocBlurWeights = blurShader.getUniform(gl, "weights");
		blurShader.disable(gl);

		compositeShader.enable(gl);
		compositeIntensity = new Uniform.Float(gl, compositeShader, "intensity", INTENSITY);
		Uniform.Int compositeTex1 = new Uniform.Int(gl, compositeShader, "inputTexture1", 0);
		compositeTex1 = new Uniform.Int(gl, compositeShader, "inputTexture2", 1);
		compositeShader.disable(gl);

		initFBOs(gl, screen);

		return true;
	}

	private void abortInit(GL2 gl, String error, Config.Graphics config)
	{
		LOGGER.error("Bloom: " + error);
		dispose(gl);
		config.setUseBloom(false);
	}

	/**
	 * Applies bloom effect to an input texture
	 *
	 * @param input
	 *            - texture to process
	 */
	public Texture2D process(GL2 gl, Texture2D input)
	{
		Texture2D brightPassOut = brightPass(gl, input);
		Texture2D blurPassOut = blurPass(gl, brightPassOut);
		return compositePass(gl, input, blurPassOut);
	}

	private Texture2D brightPass(GL2 gl, Texture2D input)
	{
		halfSizeFBOs[0].setViewport(gl);
		halfSizeFBOs[0].bind(gl);
		halfSizeFBOs[0].clear(gl);
		luminosityShader.enable(gl);
		luminosityThreshold.update(gl);
		input.bind(gl);
		EffectManager.renderScreenQuad(gl);
		input.unbind(gl);
		luminosityShader.disable(gl);
		halfSizeFBOs[0].unbind(gl);

		return halfSizeFBOs[0].getColorTexture(0);
	}

	private Texture2D blurPass(GL2 gl, Texture2D input)
	{
		// blur H
		halfSizeFBOs[1].bind(gl);
		halfSizeFBOs[1].clear(gl);
		blurShader.enable(gl);
		gl.glUniform2fv(ulocBlurOffsets, blurParams[0].offsets.length / 2, blurParams[0].offsets, 0);
		gl.glUniform1fv(ulocBlurWeights, blurParams[0].weights.length, blurParams[0].weights, 0);
		input.bind(gl);
		EffectManager.renderScreenQuad(gl);
		input.unbind(gl);
		blurShader.disable(gl);
		halfSizeFBOs[1].unbind(gl);

		// blur V
		halfSizeFBOs[0].bind(gl);
		halfSizeFBOs[0].clear(gl);
		blurShader.enable(gl);
		gl.glUniform2fv(ulocBlurOffsets, this.blurParams[1].offsets.length / 2, this.blurParams[1].offsets, 0);
		gl.glUniform1fv(ulocBlurWeights, this.blurParams[1].weights.length, this.blurParams[1].weights, 0);
		halfSizeFBOs[1].getColorTexture(0).bind(gl);
		EffectManager.renderScreenQuad(gl);
		halfSizeFBOs[1].getColorTexture(0).unbind(gl);

		blurShader.disable(gl);
		halfSizeFBOs[0].unbind(gl);

		return halfSizeFBOs[0].getColorTexture(0);
	}

	private Texture2D compositePass(GL2 gl, Texture2D input1, Texture2D input2)
	{
		fullSizeFBOs[1].setViewport(gl);
		fullSizeFBOs[1].bind(gl);
		fullSizeFBOs[1].clear(gl);
		compositeShader.enable(gl);
		compositeIntensity.update(gl);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		input1.bind(gl);
		gl.glActiveTexture(GL.GL_TEXTURE1);
		input2.bind(gl);

		EffectManager.renderScreenQuad(gl);

		compositeShader.disable(gl);
		input2.unbind(gl);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		input1.unbind(gl);
		fullSizeFBOs[1].unbind(gl);

		return fullSizeFBOs[1].getColorTexture(0);
	}

	@Override
	public void dispose(GL gl)
	{
		for (FrameBufferObject fbo : halfSizeFBOs)
			if (fbo != null)
				fbo.dispose(gl);
		for (FrameBufferObject fbo : fullSizeFBOs)
			if (fbo != null)
				fbo.dispose(gl);

		if (luminosityShader != null)
			luminosityShader.dispose(gl);
		if (blurShader != null)
			blurShader.dispose(gl);
		if (compositeShader != null)
			compositeShader.dispose(gl);

		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	private void initFBOs(GL2 gl, Viewport screen)
	{
		for (FrameBufferObject fbo : fullSizeFBOs)
			if (fbo != null)
				fbo.dispose(gl);
		for (FrameBufferObject fbo : halfSizeFBOs)
			if (fbo != null)
				fbo.dispose(gl);

		for (int i = 0; i < fullSizeFBOs.length; i++)
			fullSizeFBOs[i] = FrameBufferObject.create(gl, screen.w, screen.h, GL.GL_RGB);

		for (int i = 0; i < halfSizeFBOs.length; i++)
			halfSizeFBOs[i] = FrameBufferObject.create(gl, screen.w / 2, screen.h / 2, GL.GL_RGB);

		setBlurParams(screen.w / 2, screen.h / 2);
	}

	@Override
	public void windowResized(WindowResizeEvent event)
	{
		initFBOs(event.getDrawable().getGL().getGL2(), event.getWindow());
	}
}
