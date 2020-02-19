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
import com.jogamp.opengl.GL2GL3;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * A 3D texture object that can be applied to geometry using texture coordinates
 *
 * @author Justin Stoecker
 */
public class Texture3D implements GLDisposable
{
	private boolean disposed = false;
	private int id;
	private int w;
	private int h;

	public int getID()
	{
		return id;
	}

	public int getWidth()
	{
		return w;
	}

	public int getHeight()
	{
		return h;
	}

	private Texture3D(int id, int w, int h)
	{
		this.id = id;
		this.w = w;
		this.h = h;
	}

	@Override
	public void dispose(GL gl)
	{
		gl.glDeleteTextures(1, new int[] {id}, 0);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	public void bind(GL gl)
	{
		gl.glBindTexture(GL2GL3.GL_TEXTURE_3D, id);
	}

	public void unbind(GL gl)
	{
		gl.glBindTexture(GL2GL3.GL_TEXTURE_3D, 0);
	}

	public static Texture3D create(GL gl, ByteBuffer texels, int w, int h, int depth, int format)
	{
		// generate texture id
		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int id = idBuffer.get();

		// set default texture parameters
		gl.glBindTexture(GL2GL3.GL_TEXTURE_3D, id);
		gl.glTexParameterf(GL2GL3.GL_TEXTURE_3D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL2GL3.GL_TEXTURE_3D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL2GL3.GL_TEXTURE_3D, GL2GL3.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
		gl.glTexParameteri(GL2GL3.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL2GL3.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);

		// upload pixel data to texture object
		gl.getGL2().glTexImage3D(GL2GL3.GL_TEXTURE_3D, 0, format, w, h, depth, 0, format, GL.GL_UNSIGNED_BYTE, texels);
		gl.glBindTexture(GL2GL3.GL_TEXTURE_3D, 0);

		return new Texture3D(id, w, h);
	}
}
