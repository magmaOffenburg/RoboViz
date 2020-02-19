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
import java.nio.ShortBuffer;
import jsgl.jogl.VertexBuffer.BufferUsage;

public class IndexBuffer implements GLDisposable
{
	private boolean disposed = false;
	private GL gl;
	private int id;
	private int usage;

	public IndexBuffer(GL gl, BufferUsage usage)
	{
		IntBuffer idBuf = Buffers.newDirectIntBuffer(1);
		gl.glGenBuffers(1, idBuf);
		this.id = idBuf.get();
		this.gl = gl;
		this.usage = usage.glValue;
	}

	public void setData(Buffer data, int numBytes)
	{
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, id);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, numBytes, data, usage);
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void setData(int[] indices)
	{
		int numBytes = Integer.SIZE / 8 * indices.length;
		IntBuffer buf = Buffers.newDirectIntBuffer(indices.length);
		buf.put(indices);
		buf.rewind();
		setData(buf, numBytes);
	}

	public void setData(short[] indices)
	{
		int numBytes = Short.SIZE / 8 * indices.length;
		ShortBuffer buf = Buffers.newDirectShortBuffer(indices.length);
		buf.put(indices);
		buf.rewind();
		setData(buf, numBytes);
	}

	public void bind()
	{
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, id);
	}

	public void unbind()
	{
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
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
