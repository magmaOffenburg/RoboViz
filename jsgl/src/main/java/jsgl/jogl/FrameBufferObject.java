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

package jsgl.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An off-screen buffer that can be used for render-to-texture purposes. To use
 * an FBO, it must be bound as the current Framebuffer.
 *
 * @author Justin Stoecker
 * @see http://oss.sgi.com/projects/ogl-sample/registry/EXT/framebuffer_object.txt
 * @see http://www.songho.ca/opengl/gl_fbo.html
 */
public class FrameBufferObject implements GLDisposable
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed = false;
	private int id;
	private Texture2D depthTexture;
	private Texture2D[] colorTextures;
	private List<GLDisposable> disposables = new ArrayList<>();
	private int texWidth;
	private int texHeight;

	public int getID()
	{
		return id;
	}

	/** Returns color texture at attachment point i */
	public Texture2D getColorTexture(int i)
	{
		return colorTextures[i];
	}

	/** Returns depth texture if one has been attached; null otherwise */
	public Texture2D getDepthTexture()
	{
		return depthTexture;
	}

	private FrameBufferObject(GL gl, int fboID)
	{
		IntBuffer temp = Buffers.newDirectIntBuffer(1);
		gl.glGetIntegerv(GL2.GL_MAX_COLOR_ATTACHMENTS, temp);
		colorTextures = new Texture2D[temp.get()];

		this.id = fboID;
	}

	public static FrameBufferObject generate(GL gl)
	{
		IntBuffer temp = Buffers.newDirectIntBuffer(1);
		gl.glGenFramebuffers(1, temp);
		int id = temp.get();

		return new FrameBufferObject(gl, id);
	}

	/**
	 * Creates a Framebuffer Object that can be used for offscreen rendering to
	 * a texture of a specified with and height. The texture resolution does not
	 * affect the portion of the scene that is rendered, only its quality.
	 *
	 * @param gl - OpenGL context
	 * @param w - width of texture
	 * @param h - height of texture
	 * @param internalFormat -
	 * @return Returns a Framebuffer Objects if successful; null otherwise
	 */
	public static FrameBufferObject create(GL gl, int w, int h, int internalFormat)
	{
		Texture2D colorTex = Texture2D.generate(gl);
		colorTex.bind(gl);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		colorTex.texImage(gl, 0, internalFormat, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		Texture2D.unbind(gl);

		RenderBuffer depthBuffer = RenderBuffer.createDepthBuffer(gl, w, h);
		FrameBufferObject fbo = FrameBufferObject.generate(gl);

		fbo.bind(gl);
		fbo.attachColorTarget(gl, colorTex, 0, 0, true);
		fbo.attachDepthTarget(gl, depthBuffer, true);

		int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
			LOGGER.error("ERROR creating FBO - releasing resources");
			fbo.dispose(gl);
			return null;
		}

		return fbo;
	}

	/** Creates an FBO with a color texture attachment and no depth attachment */
	public static FrameBufferObject createNoDepth(GL gl, int w, int h, int internalFormat)
	{
		Texture2D colorTex = Texture2D.generate(gl);
		colorTex.bind(gl);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		Texture2D.setParameter(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		colorTex.texImage(gl, 0, internalFormat, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		Texture2D.unbind(gl);

		FrameBufferObject fbo = FrameBufferObject.generate(gl);

		fbo.bind(gl);
		fbo.attachColorTarget(gl, colorTex, 0, 0, true);

		int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
			LOGGER.error("ERROR creating FBO - releasing resources");
			fbo.dispose(gl);
			return null;
		}

		return fbo;
	}

	/**
	 * Creates a Framebuffer Object that can be used for offscreen rendering to
	 * a texture of a specified with and height. This FBO is setup for
	 * multisampling.
	 */
	public static FrameBufferObject create(GL gl, int w, int h, int internalFormat, int samples)
	{
		RenderBuffer depthBuffer = RenderBuffer.createDepthBuffer(gl, w, h, samples);
		RenderBuffer colorBuffer = RenderBuffer.createColorBuffer(gl, w, h, internalFormat, samples);
		FrameBufferObject fbo = FrameBufferObject.generate(gl);

		fbo.bind(gl);
		fbo.attachDepthTarget(gl, depthBuffer, true);
		fbo.attachColorTarget(gl, colorBuffer, true);

		int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
			LOGGER.error("ERROR creating FBO - releasing resources");
			fbo.dispose(gl);
			return null;
		}

		return fbo;
	}

	/**
	 * Attaches a texture that will be the target for colors while this FBO is
	 * bound.
	 *
	 * @param texture
	 *            - the texture object
	 * @param level
	 *            - texture level if using mip-maps
	 * @param attachPoint
	 *            - the color attachment point index
	 * @param autoDispose
	 *            - if the texture should be released when the FBO is disposed
	 */
	public void attachColorTarget(GL gl, Texture2D texture, int level, int attachPoint, boolean autoDispose)
	{
		texWidth = texture.getWidth();
		texHeight = texture.getHeight();

		colorTextures[attachPoint] = texture;
		gl.glFramebufferTexture2D(
				GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0 + attachPoint, GL.GL_TEXTURE_2D, texture.getID(), level);

		if (autoDispose)
			disposables.add(texture);
	}

	/**
	 * Attaches a renderbuffer that will be the target for colors while this FBO
	 * is bound.
	 */
	public void attachColorTarget(GL gl, RenderBuffer rbo, boolean autoDispose)
	{
		gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER, rbo.getID());

		if (autoDispose)
			disposables.add(rbo);
	}

	/**
	 * Attaches a texture that will be used as a depth buffer while this FBO is
	 * bound.
	 */
	public void attachDepthTarget(GL gl, Texture2D texture, boolean autoDispose)
	{
		this.depthTexture = texture;
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_TEXTURE_2D, texture.getID(), 0);

		if (autoDispose)
			disposables.add(texture);
	}

	/**
	 * Attaches a renderbuffer that will be used as a depth buffer while this
	 * FBO is bound.
	 */
	public void attachDepthTarget(GL gl, RenderBuffer rbo, boolean autoDispose)
	{
		gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, rbo.getID());

		if (autoDispose)
			disposables.add(rbo);
	}

	/**
	 * Sets viewport to match color texture dimensions.
	 */
	public void setViewport(GL gl)
	{
		gl.glViewport(0, 0, texWidth, texHeight);
	}

	/** Binds the current FBO for off-screen rendering */
	public void bind(GL gl)
	{
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, id);
	}

	/**
	 * Unbinds the current FBO and sets rendering to go to the selected draw
	 * buffer
	 */
	public void unbind(GL gl)
	{
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
	}

	/** Clears the color and depth buffers for the FBO */
	public void clear(GL gl)
	{
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void dispose(GL gl)
	{
		for (GLDisposable d : disposables)
			d.dispose(gl);

		gl.glDeleteFramebuffers(1, new int[] {id}, 0);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	@Override
	public void finalize()
	{
		if (!disposed)
			LOGGER.warn("FBO {} was not disposed!", id);
	}
}
