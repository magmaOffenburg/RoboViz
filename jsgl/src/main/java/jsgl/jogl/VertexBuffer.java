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
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import jsgl.jogl.verts.Vertex;

/**
 * A buffer used to store vertex data (position, normal, color, etc.) in video
 * memory for non-immediate-mode rendering. Using a VBO will greatly improve
 * performance over immediate drawing.
 *
 * @author Justin Stoecker
 */
public class VertexBuffer implements GLDisposable
{
	/** Hint for how the buffer's memory should be allocated */
	public enum BufferUsage
	{
		/** GL_STATIC_DRAW: no changes to buffered data */
		STATIC(GL.GL_STATIC_DRAW),

		/** GL_DYNAMIC_DRAW: frequent changes to buffered data */
		DYNAMIC(GL.GL_DYNAMIC_DRAW),

		/** GL_STREAM_DRAW: buffered data changes every frame */
		STREAM(GL2ES2.GL_STREAM_DRAW);

		public final int glValue;

		BufferUsage(int glValue)
		{
			this.glValue = glValue;
		}
	}

	private boolean disposed = false;
	private GL gl;
	private int id;
	private int usage;

	public VertexBuffer(GL gl, BufferUsage usage)
	{
		IntBuffer idBuf = Buffers.newDirectIntBuffer(1);
		gl.glGenBuffers(1, idBuf);
		this.id = idBuf.get();
		this.gl = gl;
		this.usage = usage.glValue;
	}

	public void setData(Buffer data, int numBytes)
	{
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, id);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, numBytes, data, usage);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
	}

	public void setData(Vertex[] vertices)
	{
		int numBytes = vertices[0].getSize() * vertices.length;
		int elements = vertices[0].getSize() / (Float.SIZE / 8);
		FloatBuffer buf = Buffers.newDirectFloatBuffer(vertices.length * elements);
		for (Vertex vertex : vertices)
			buf.put(vertex.getElements());
		buf.rewind();
		setData(buf, numBytes);
	}

	public void bind()
	{
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, id);
	}

	public void unbind()
	{
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void dispose(GL gl)
	{
		gl.glDeleteBuffers(1, new int[] {id}, 0);
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}