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

package jsgl.jogl.verts;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;

/**
 * Vertex with position, normal, and texture coordinate elements
 *
 * @author Justin Stoecker
 */
public class VertexPNT extends Vertex
{
	public static final int STRIDE = Float.SIZE / 8 * 8;

	public final float[] position = new float[3];
	public final float[] normal = new float[3];
	public final float[] texcoords = new float[2];

	public VertexPNT(float[] position, float[] normal, float[] texcoords)
	{
		System.arraycopy(position, 0, this.position, 0, 3);
		System.arraycopy(normal, 0, this.normal, 0, 3);
		System.arraycopy(texcoords, 0, this.texcoords, 0, 2);
	}

	@Override
	public int getSize()
	{
		return STRIDE;
	}

	@Override
	public float[] getElements()
	{
		float[] elements = new float[8];
		System.arraycopy(position, 0, elements, 0, 3);
		System.arraycopy(normal, 0, elements, 3, 3);
		System.arraycopy(texcoords, 0, elements, 6, 2);
		return elements;
	}

	@Override
	public void setState(GL2 gl)
	{
		set(gl);
	}

	@Override
	public void unsetState(GL2 gl)
	{
		unset(gl);
	}

	public static void set(GL2 gl)
	{
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, STRIDE, 0);
		gl.glNormalPointer(GL.GL_FLOAT, STRIDE, 12);
		gl.glTexCoordPointer(2, GL.GL_FLOAT, STRIDE, 24);
	}

	public static void unset(GL2 gl)
	{
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
	}
}
