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
import java.nio.FloatBuffer;
import jsgl.jogl.IndexBuffer;
import jsgl.jogl.VertexBuffer;
import jsgl.jogl.VertexBuffer.BufferUsage;

/**
 * Renders mesh geometry with maximum flexibility and great performance. This
 * style of rendering should be preferred if the system supports it.
 *
 * @author Justin Stoecker
 */
public class MeshRendererVBO implements MeshRenderer
{
	private Mesh mesh;
	private VertexBuffer vBuffer;
	private IndexBuffer[] indexBuffers;
	private int vertStride;
	private int normalOffset;
	private int texCoordOffset;

	@Override
	public void init(GL gl, Mesh mesh)
	{
		this.mesh = mesh;

		// buffer all vertex data into a single direct float buffer
		int elementsPerVertex = mesh.vertices.get(0).getNumElements();
		int totalElements = mesh.vertices.size() * elementsPerVertex;
		FloatBuffer vBufferLocal = Buffers.newDirectFloatBuffer(totalElements);
		for (MeshVertex v : mesh.vertices) {
			vBufferLocal.put(v.getPosition());
			if (v.getNormal() != null)
				vBufferLocal.put(v.getNormal());
			if (v.getTexCoords() != null)
				vBufferLocal.put(v.getTexCoords());
		}
		vBufferLocal.rewind();

		// create index buffers for each part
		indexBuffers = new IndexBuffer[mesh.parts.size()];
		for (int i = 0; i < indexBuffers.length; i++) {
			MeshPart part = mesh.parts.get(i);
			int indexSizeBytes = part.indexType == GL2.GL_UNSIGNED_SHORT ? 2 : 4;
			indexBuffers[i] = new IndexBuffer(gl, BufferUsage.STATIC);
			indexBuffers[i].setData(part.bufferData(gl.getGL2()), part.numIndices * indexSizeBytes);
		}

		vertStride = 12;
		if (mesh.useNormals) {
			normalOffset = vertStride;
			vertStride += 12;
		}
		if (mesh.useTexCoords) {
			texCoordOffset = vertStride;
			vertStride += mesh.vertices.get(0).getTexCoords().length * 4;
		}

		vBuffer = new VertexBuffer(gl, BufferUsage.STATIC);
		vBuffer.setData(vBufferLocal, vBufferLocal.capacity() * 4);
		vBufferLocal = null;
	}

	@Override
	public void setState(GL glContext)
	{
		GL2 gl = glContext.getGL2();

		vBuffer.bind();
		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL.GL_FLOAT, vertStride, 0);

		if (mesh.useNormals) {
			gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
			gl.glNormalPointer(GL.GL_FLOAT, vertStride, normalOffset);
		}

		if (mesh.useTexCoords) {
			gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL.GL_FLOAT, vertStride, texCoordOffset);
		}
	}

	@Override
	public void render(GL gl)
	{
		for (int i = 0; i < mesh.parts.size(); i++) {
			MeshPart part = mesh.parts.get(i);
			part.material.apply(gl.getGL2());
			indexBuffers[i].bind();
			gl.glDrawElements(GL.GL_TRIANGLES, part.numIndices, part.indexType, 0);
			indexBuffers[i].unbind();
		}
	}

	@Override
	public void unsetState(GL glContext)
	{
		GL2 gl = glContext.getGL2();
		gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		vBuffer.unbind();
	}

	@Override
	public void dispose(GL gl)
	{
		if (vBuffer != null)
			vBuffer.dispose(gl);
		for (IndexBuffer ibo : indexBuffers)
			ibo.dispose(gl);
	}
}
