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

package roboviz.jsgl.jogl.model;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import roboviz.jsgl.jogl.GLDisposable;

/**
 * A mesh part is a collection of mesh faces with a shared material.
 *
 * @author Justin Stoecker
 */
public class MeshPart implements GLDisposable
{
	private static final int USHORT_MAX = 65535;

	protected MeshMaterial material;
	protected ArrayList<MeshFace> faces;
	protected int numIndices;
	protected int numTriangles;
	protected int indexType = GL.GL_UNSIGNED_SHORT;
	private boolean disposed = false;

	public int getNumTriangles()
	{
		return numTriangles;
	}

	public ArrayList<MeshFace> getFaces()
	{
		return faces;
	}

	public MeshMaterial getMaterial()
	{
		return material;
	}

	public void setMaterial(MeshMaterial material)
	{
		this.material = material;
	}

	public MeshPart()
	{
		faces = new ArrayList<MeshFace>();
	}

	public MeshPart(ArrayList<MeshFace> faces)
	{
		this.faces = faces;

		numTriangles = 0;
		for (MeshFace f : faces) {
			numTriangles += f.getNumTriangles();
			checkIndexSize(f);
		}
	}

	public void addFace(MeshFace face)
	{
		faces.add(face);
		checkIndexSize(face);
		numTriangles += face.getNumTriangles();
	}

	/**
	 * Checks if face contains indices with values exceeding 16 bits; if so,
	 * switches index type to unsigned int (32 bit).
	 */
	private void checkIndexSize(MeshFace face)
	{
		// initially use 16-bit indices; if an index value is found to exceed
		// 16 bits switch to 32-bit indices
		if (indexType == GL.GL_UNSIGNED_SHORT) {
			int[] indices = face.getVertIndices();
			for (int i = 0; i < indices.length; i++)
				if (indices[i] > USHORT_MAX)
					indexType = GL2ES2.GL_UNSIGNED_INT;
		}
	}

	/**
	 * Collect index data into a single buffer
	 */
	public Buffer bufferData(GL2 gl)
	{
		Buffer iBuffer;
		numIndices = numTriangles * 3;
		if (indexType == GL.GL_UNSIGNED_SHORT) {
			iBuffer = Buffers.newDirectShortBuffer(numIndices);
			ShortBuffer buf = (ShortBuffer) iBuffer;
			for (MeshFace f : faces) {
				int[] indices = f.getVertIndices();
				for (int i = 1; i <= f.getNumTriangles(); i++) {
					buf.put((short) indices[0]);
					buf.put((short) indices[i]);
					buf.put((short) indices[i + 1]);
				}
			}
		} else {
			iBuffer = Buffers.newDirectIntBuffer(numIndices);
			IntBuffer buf = (IntBuffer) iBuffer;
			for (MeshFace f : faces) {
				int[] indices = f.getVertIndices();
				for (int i = 1; i <= f.getNumTriangles(); i++) {
					buf.put(indices[0]);
					buf.put(indices[i]);
					buf.put(indices[i + 1]);
				}
			}
		}
		iBuffer.rewind();

		return iBuffer;
	}

	@Override
	public void dispose(GL gl)
	{
		if (material != null)
			material.dispose(gl);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	public void printInfo()
	{
		System.out.println("Mesh Part");
		System.out.println("  Material: " + material.name);
		System.out.println(String.format("  Num. Faces %d (%d tris)", faces.size(), numTriangles));
	}
}
