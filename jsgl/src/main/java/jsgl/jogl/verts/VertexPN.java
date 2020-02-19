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
 * Vertex that has position and normal elements
 *
 * @author Justin Stoecker
 */
public class VertexPN extends Vertex
{
	public static final int STRIDE = Float.SIZE / 8 * 6;

	public final float[] position = new float[3];
	public final float[] normal = new float[3];

	public VertexPN(float[] position, float[] normal)
	{
		System.arraycopy(position, 0, this.position, 0, 3);
		System.arraycopy(normal, 0, this.normal, 0, 3);
	}

	@Override
	public void setState(GL2 gl)
	{
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, STRIDE, 0);
		gl.glNormalPointer(GL.GL_FLOAT, STRIDE, 12);
	}

	@Override
	public void unsetState(GL2 gl)
	{
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
	}

	@Override
	public int getSize()
	{
		return STRIDE;
	}

	@Override
	public float[] getElements()
	{
		float[] elements = new float[6];
		System.arraycopy(position, 0, elements, 0, 3);
		System.arraycopy(normal, 0, elements, 3, 3);
		return elements;
	}
}