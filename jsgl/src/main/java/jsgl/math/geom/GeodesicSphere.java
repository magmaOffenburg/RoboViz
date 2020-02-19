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

package jsgl.math.geom;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import jsgl.math.vector.Vec3f;

/**
 * Creates a geodesic sphere with a specified radius and level of detail
 *
 * @author Justin Stoecker
 */
public class GeodesicSphere
{
	/** Helper class that constructs sphere vertices and triangle indices */
	private class Builder
	{
		private Icosahedron ico = new Icosahedron(1);
		private float[][] icoVerts = ico.getVertices();
		private int[][] icoEdgeVerts = ico.getEdgeVertices();
		private int[][] icoTriVerts = ico.getTriangleVertices();
		private int[][] icoTriEdges = ico.getTriangleEdges();

		private int vertIndex = 0;
		private int triIndex = 0;
		private int trianglesPerFace = (level + 1) * (level + 1);
		private int faceVerts = (level + 3) * (level + 2) / 2;
		private int faceInnerVerts = faceVerts - 3 * level - 3;

		private int[] edgeL, edgeB, edgeR;
		private int ip, bp, lp, rp;
		private boolean flipL, flipB, flipR;
		int[] curTriVerts;
		int[] curTriEdges;

		private void build()
		{
			verts = new float[20 * faceInnerVerts + 30 * level + 12][3];
			triangles = new int[20 * trianglesPerFace][3];

			addIcoVerts();
			addEdgeVerts();
			addInnerVerts();
			calcTriangles();
		}

		private void addIcoVerts()
		{
			for (float[] vert : icoVerts) {
				float x = vert[0];
				float y = vert[1];
				float z = vert[2];
				float d = (float) (radius / Math.sqrt(x * x + y * y + z * z));
				verts[vertIndex++] = new float[] {x * d, y * d, z * d};
			}
		}

		private void addEdgeVerts()
		{
			float len = level + 1;
			for (int[] edge : icoEdgeVerts) {
				// the vertex positions for current edge
				float[] a = icoVerts[edge[0]];
				float[] b = icoVerts[edge[1]];

				// vector to place vertices evenly along edge
				float[] step = {(b[0] - a[0]) / len, (b[1] - a[1]) / len, (b[2] - a[2]) / len};

				for (int edgeVert = 1; edgeVert <= level; edgeVert++) {
					float x = a[0] + step[0] * edgeVert;
					float y = a[1] + step[1] * edgeVert;
					float z = a[2] + step[2] * edgeVert;
					float d = (float) (radius / Math.sqrt(x * x + y * y + z * z));
					verts[vertIndex++] = new float[] {x * d, y * d, z * d};
				}
			}
		}

		private void addInnerVerts()
		{
			float len = level + 1;
			for (int[] vert : icoTriVerts) {
				float[] a = icoVerts[vert[0]];
				float[] b = icoVerts[vert[1]];
				float[] c = icoVerts[vert[2]];

				float[] down = {(b[0] - a[0]) / len, (b[1] - a[1]) / len, (b[2] - a[2]) / len};
				float[] right = {(c[0] - b[0]) / len, (c[1] - b[1]) / len, (c[2] - b[2]) / len};

				for (int row = 2; row < level + 1; row++) {
					for (int iVert = 0; iVert < row - 1; iVert++) {
						float x = a[0] + down[0] * row + right[0] * (iVert + 1);
						float y = a[1] + down[1] * row + right[1] * (iVert + 1);
						float z = a[2] + down[2] * row + right[2] * (iVert + 1);
						float d = (float) (radius / Math.sqrt(x * x + y * y + z * z));
						verts[vertIndex++] = new float[] {x * d, y * d, z * d};
					}
				}
			}
		}

		private void calcEdgePointers(int icoTriIndex)
		{
			edgeL = icoEdgeVerts[icoTriEdges[icoTriIndex][0]];
			edgeB = icoEdgeVerts[icoTriEdges[icoTriIndex][1]];
			edgeR = icoEdgeVerts[icoTriEdges[icoTriIndex][2]];
			flipL = edgeL[0] != curTriVerts[0];
			flipB = edgeB[0] != curTriVerts[1];
			flipR = edgeR[0] != curTriVerts[0];

			lp = flipL ? (curTriEdges[0] + 1) * level + 11 : curTriEdges[0] * level + 12;
			bp = flipB ? (curTriEdges[1] + 1) * level + 11 : curTriEdges[1] * level + 12;
			rp = flipR ? (curTriEdges[2] + 1) * level + 11 : curTriEdges[2] * level + 12;
		}

		private int[] calcAuxArray()
		{
			int[] aux = new int[faceVerts];
			int i = 0;
			for (int row = 0; row < level + 2; row++) {
				if (row == 0) {
					// top row is just 1 vertex
					aux[i++] = curTriVerts[0];
				} else if (row == level + 1) {
					// last row
					aux[i++] = curTriVerts[1];
					for (int bottomVert = 0; bottomVert < level; bottomVert++)
						aux[i++] = flipB ? bp-- : bp++;
					aux[i++] = curTriVerts[2];
				} else {
					// all other rows
					aux[i++] = flipL ? lp-- : lp++;
					for (int iVert = 0; iVert < row - 1; iVert++)
						aux[i++] = ip++;
					aux[i++] = flipR ? rp-- : rp++;
				}
			}
			return aux;
		}

		private void calcTriIndices(int[] aux)
		{
			int i = 0;
			int vertsOnRow = 1;
			int trisOnRow = 1;
			for (int row = 0; row < (level + 1); row++) {
				int[] a = {i, i + vertsOnRow, i + vertsOnRow + 1};
				int[] b = {a[0] + 1, a[0], a[2]};

				for (int tri = 0; tri < trisOnRow; tri++) {
					int offset = tri / 2;
					if (tri % 2 == 0) {
						triangles[triIndex++] = new int[] {aux[a[0] + offset], aux[a[1] + offset], aux[a[2] + offset]};
					} else {
						triangles[triIndex++] = new int[] {aux[b[0] + offset], aux[b[1] + offset], aux[b[2] + offset]};
					}
				}

				i += vertsOnRow;
				vertsOnRow++;
				trisOnRow += 2;
			}
		}

		private void calcTriangles()
		{
			ip = 12 + ico.getEdgeVertices().length * level;
			for (int t = 0; t < icoTriVerts.length; t++) {
				curTriVerts = icoTriVerts[t];
				curTriEdges = icoTriEdges[t];
				calcEdgePointers(t);
				int[] aux = calcAuxArray();
				calcTriIndices(aux);
			}
		}
	}

	private int[][] triangles;
	private float[][] verts;
	private int level;
	private float radius;

	public int[][] getTriangles()
	{
		return triangles;
	}

	public float[][] getVerts()
	{
		return verts;
	}

	public int getLevel()
	{
		return level;
	}

	public float getRadius()
	{
		return radius;
	}

	public GeodesicSphere(float radius, int level)
	{
		this.radius = radius;
		this.level = level;
		new Builder().build();
	}

	public void render(GL2 gl)
	{
		gl.glBegin(GL.GL_TRIANGLES);
		for (int[] triangle : triangles) {
			for (int j = 0; j < 3; j++) {
				Vec3f v = new Vec3f(verts[triangle[j]]);
				gl.glNormal3fv(v.normalize().getVals(), 0);
				gl.glVertex3fv(v.getVals(), 0);
			}
		}
		gl.glEnd();
	}

	public void renderPts(GL2 gl, TextRenderer tr)
	{
		gl.glColor3f(0, 0, 1);
		gl.glPointSize(6);
		gl.glBegin(GL.GL_POINTS);
		for (float[] vert : verts) {
			gl.glVertex3fv(vert, 0);
		}
		gl.glEnd();
		gl.glPointSize(1);

		tr.begin3DRendering();
		for (int i = 0; i < verts.length; i++)
			tr.draw3D("" + i, verts[i][0], verts[i][1], verts[i][2], 0.015f);
		tr.end3DRendering();
	}
}