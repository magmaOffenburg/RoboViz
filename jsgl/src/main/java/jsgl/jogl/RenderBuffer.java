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

/**
 * Buffer for direct offscreen rendering. Should be attached to an FBO.
 * @author justin
 */
public class RenderBuffer implements GLDisposable
{
	private boolean disposed = false;
	private int id;

	public int getID()
	{
		return id;
	}

	private RenderBuffer(int id)
	{
		this.id = id;
	}

	/**
	 * Generates a new render buffer ID, wraps it in a RenderBuffer object, and
	 * return a reference to that object.
	 */
	public static RenderBuffer generate(GL gl)
	{
		IntBuffer temp = Buffers.newDirectIntBuffer(1);
		gl.glGenRenderbuffers(1, temp);
		int id = temp.get();

		return new RenderBuffer(id);
	}

	/**
	 * Creates a depth renderbuffer with system's best supported depth precision
	 */
	public static RenderBuffer createDepthBuffer(GL gl, int width, int height)
	{
		RenderBuffer rbo = generate(gl);

		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, rbo.id);
		gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT, width, height);
		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

		return rbo;
	}

	/**
	 * Creates a depth renderbuffer that supports multisampling
	 */
	public static RenderBuffer createDepthBuffer(GL gl, int width, int height, int samples)
	{
		RenderBuffer rbo = generate(gl);

		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, rbo.id);
		gl.getGL2GL3().glRenderbufferStorageMultisample(
				GL.GL_RENDERBUFFER, samples, GL2.GL_DEPTH_COMPONENT, width, height);
		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

		return rbo;
	}

	/**
	 * Creates a color renderbuffer using specified pixel format
	 */
	public static RenderBuffer createColorBuffer(GL gl, int width, int height, int format)
	{
		RenderBuffer rbo = generate(gl);

		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, rbo.id);
		gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, format, width, height);
		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

		return rbo;
	}

	/**
	 * Creates a color renderbuffer using specified pixel format and multisampling
	 */
	public static RenderBuffer createColorBuffer(GL gl, int width, int height, int format, int samples)
	{
		RenderBuffer rbo = generate(gl);

		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, rbo.id);
		gl.getGL2GL3().glRenderbufferStorageMultisample(GL.GL_RENDERBUFFER, samples, format, width, height);
		gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

		return rbo;
	}

	public void dispose(GL gl)
	{
		if (!disposed) {
			gl.glDeleteRenderbuffers(1, new int[] {id}, 0);
			disposed = true;
		}
	}

	public boolean isDisposed()
	{
		return disposed;
	}
}
