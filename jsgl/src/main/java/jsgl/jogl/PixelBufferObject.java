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
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;
import java.nio.Buffer;
import java.nio.IntBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A buffer used to store vertex and pixel data in video memory. Allows pixel
 * data transfer through direct memory access to improve performance of
 * read/write operations.
 *
 * @author Justin Stoecker
 * @see http://www.songho.ca/opengl/gl_pbo.html
 */
public class PixelBufferObject implements GLDisposable
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed = false;
	private int id;
	private int target;
	private int usage;

	public int getID()
	{
		return id;
	}

	public int getTarget()
	{
		return target;
	}

	public int getUsage()
	{
		return usage;
	}

	private PixelBufferObject(int pbo, int target, int usage)
	{
		this.id = pbo;
		this.target = target;
		this.usage = usage;
	}

	/**
	 * Creates a new Pixelbuffer Object. Requires GL version 2.1 or greater.
	 *
	 * @param gl
	 *           - OpenGL context
	 * @param data
	 *           - data to store into PBO
	 * @param dataSize
	 *           - size of data in bytes
	 * @param pack
	 *           - if true, data is transferred from OpenGL to app (read);
	 *           otherwise, data is transferred from app to OpenGL (unpack /
	 *           upload)
	 * @return Returns a new PBO if successful; otherwise, null
	 */
	public static PixelBufferObject createPBO(GL gl, Buffer data, int dataSize, boolean pack)
	{
		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenBuffers(1, idBuffer);
		int pbo = idBuffer.get();

		int target = pack ? GL2GL3.GL_PIXEL_PACK_BUFFER : GL2GL3.GL_PIXEL_UNPACK_BUFFER;
		int usage = pack ? GL2GL3.GL_STREAM_READ : GL2ES2.GL_STREAM_DRAW;

		gl.glBindBuffer(target, pbo);
		gl.glBufferData(target, dataSize, data, usage);
		gl.glBindBuffer(target, 0);

		return new PixelBufferObject(pbo, target, usage);
	}

	public static boolean getSupport(GLInfo info)
	{
		return info.getGLVersion() >= 2.1;
	}

	@Override
	public void dispose(GL gl)
	{
		gl.glDeleteBuffers(1, new int[] {id}, 0);
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
			LOGGER.warn("PBO {} was not disposed!", id);
	}
}
