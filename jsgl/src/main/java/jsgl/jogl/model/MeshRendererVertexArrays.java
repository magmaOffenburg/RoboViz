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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Performs client-side buffering of mesh geometry and reduces overhead of
 * multiple GL function calls. Given the static geometry of a mesh, display
 * lists are preferred over vertex arrays.
 *
 * @author Justin Stoecker
 */
public class MeshRendererVertexArrays implements MeshRenderer
{
	private Mesh mesh;
	protected FloatBuffer vBuffer;
	private Buffer[] indexBuffers;
	protected int vertStride;
	protected int normalOffset;
	protected int texCoordOffset;

	@Override
	public void init(GL gl, Mesh mesh)
	{
		this.mesh = mesh;

		// buffer all vertex data into a single direct float buffer
		int elementsPerVertex = mesh.vertices.get(0).getNumElements();
		int totalElements = mesh.vertices.size() * elementsPerVertex;
		vBuffer = Buffers.newDirectFloatBuffer(totalElements);
		for (MeshVertex v : mesh.vertices) {
			vBuffer.put(v.getPosition());
			if (v.getNormal() != null)
				vBuffer.put(v.getNormal());
			if (v.getTexCoords() != null)
				vBuffer.put(v.getTexCoords());
		}
		vBuffer.rewind();

		vertStride = 12;
		if (mesh.useNormals) {
			normalOffset = vertStride;
			vertStride += 12;
		}
		if (mesh.useTexCoords) {
			texCoordOffset = vertStride;
			vertStride += mesh.vertices.get(0).getTexCoords().length * 4;
		}

		// create index buffers for each part
		indexBuffers = new Buffer[mesh.parts.size()];
		for (int i = 0; i < indexBuffers.length; i++) {
			MeshPart part = mesh.parts.get(i);
			indexBuffers[i] = part.bufferData(gl.getGL2());
		}
	}

	@Override
	public void setState(GL glContext)
	{
		GL2 gl = glContext.getGL2();

		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, vertStride, vBuffer.position(0));

		if (mesh.useNormals) {
			gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
			gl.glNormalPointer(GL.GL_FLOAT, vertStride, vBuffer.position(normalOffset / 4));
		}

		if (mesh.useTexCoords) {
			gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL.GL_FLOAT, vertStride, vBuffer.position(texCoordOffset / 4));
		}
	}

	@Override
	public void render(GL gl)
	{
		for (int i = 0; i < mesh.parts.size(); i++) {
			MeshPart part = mesh.parts.get(i);
			part.material.apply(gl.getGL2());
			gl.glDrawElements(GL.GL_TRIANGLES, part.numIndices, part.indexType, indexBuffers[i].arrayOffset()); // TODO
			// Don't know if arrayOffset() works, see:
			// https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/opengl/GL.htm
		}
	}

	@Override
	public void unsetState(GL glContext)
	{
		GL2 gl = glContext.getGL2();
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
	}

	@Override
	public void dispose(GL gl)
	{
		// Auto-generated method stub
	}
}
