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
import java.nio.Buffer;
import java.nio.IntBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A buffer used to store vertex data (position, normal, color, etc.) in video
 * memory for non-immediate-mode rendering. Using a VBO will greatly improve
 * performance over immediate drawing.
 *
 * @author Justin Stoecker
 * @see http://www.songho.ca/opengl/gl_vbo.html
 */
public class VertexBufferObject implements GLDisposable
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

	/**
	 * Creates a new vertex buffer object
	 *
	 * @param data
	 *           - data to be stored in VBO
	 * @param dataSize
	 *           - total size in bytes of the data
	 * @param target
	 *           - the type of data the vertex buffer handles:<br>
	 *           GL_ARRAY_BUFFER_ARB - vertex, normal, tex. coords, etc.<br>
	 *           GL_ELEMENT_ARRAY_BUFFER_ARB - indexing data<br>
	 *
	 * @param usage
	 *           - determines how the VBO will be used for efficiency:<br>
	 *           GL_STATIC_DRAW_ARB - no changes to buffered data<br>
	 *           GL_DYNAMIC_DRAW_ARB - frequent changes to buffered data<br>
	 *           GL_STREAM_DRAW_ARB - buffered data changes every frame
	 */
	public VertexBufferObject(GL gl, Buffer data, int dataSize, int target, int usage)
	{
		this.target = target;
		this.usage = usage;

		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenBuffers(1, idBuffer);
		id = idBuffer.get();

		gl.glBindBuffer(target, id);
		gl.glBufferData(target, dataSize, data, usage);
		gl.glBindBuffer(target, 0);
	}

	/**
	 * Replaces the data in the buffer with new data
	 */
	public void bufferData(GL gl, Buffer data, int dataSize)
	{
		gl.glBufferData(GL.GL_ARRAY_BUFFER, dataSize, data, usage);
	}

	/**
	 * Replaces a portion of the data in the buffer starting at offset
	 *
	 * @param data
	 *           - the data to be written
	 * @param dataSize
	 *           - size of the buffered data in bytes
	 * @param offset
	 *           - the starting location for copying data
	 */
	public void bufferData(GL gl, Buffer data, int dataSize, int offset)
	{
		gl.glBufferSubData(target, offset, dataSize, data);
	}

	@Override
	public void finalize()
	{
		if (!disposed)
			LOGGER.warn("VBO {} was not disposed!", id);
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
}
