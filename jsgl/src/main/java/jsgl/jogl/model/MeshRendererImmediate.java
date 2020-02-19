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

package jsgl.jogl.model;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.util.ArrayList;

/**
 * Basic immediate mode rendering for mesh geometry. This style of rendering
 * should be avoided except for testing purposes.
 *
 * @author Justin Stoecker
 */
public class MeshRendererImmediate implements MeshRenderer
{
	private Mesh mesh;

	public void init(GL gl, Mesh mesh)
	{
		this.mesh = mesh;
	}

	public void setState(GL gl)
	{
	}

	public void render(GL glContext)
	{
		GL2 gl = glContext.getGL2();

		for (MeshPart part : mesh.parts) {
			part.getMaterial().apply(gl);
			gl.glBegin(GL.GL_TRIANGLES);
			ArrayList<MeshFace> faces = part.getFaces();
			for (MeshFace face : faces) {
				int[] indices = face.getVertIndices();
				for (int index : indices) {
					MeshVertex v = mesh.vertices.get(index);
					float[] vn = v.getNormal();
					float[] vp = v.getPosition();
					float[] vt = v.getTexCoords();
					if (vn != null)
						gl.glNormal3fv(vn, 0);
					if (vt != null)
						gl.glTexCoord2fv(vt, 0);
					gl.glVertex3fv(vp, 0);
				}
			}
			gl.glEnd();
		}
	}

	public void unsetState(GL gl)
	{
	}

	public void dispose(GL gl)
	{
	}
}
