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

/**
 * Icosahedron shape
 *
 * @author Justin Stoecker
 */
public class Icosahedron
{
	// indices for vertices of each triangle face (20 faces total)
	private static final int[][] TRI_VERTS = {{0, 1, 2}, {0, 2, 3}, {0, 3, 4}, {0, 4, 5}, {0, 5, 1}, {1, 9, 2},
			{2, 9, 10}, {2, 10, 3}, {3, 10, 6}, {3, 6, 4}, {4, 6, 7}, {4, 7, 5}, {5, 7, 8}, {5, 8, 1}, {1, 8, 9},
			{11, 6, 10}, {11, 10, 9}, {11, 9, 8}, {11, 8, 7}, {11, 7, 6}};

	// indices for edges of each triangle face (20 faces total)
	private static final int[][] TRI_EDGES = {
			{0, 1, 2},
			{2, 3, 4},
			{4, 5, 6},
			{6, 7, 8},
			{8, 9, 0},
			{10, 11, 1},
			{11, 12, 13},
			{13, 14, 3},
			{14, 15, 16},
			{16, 17, 5},
			{17, 18, 19},
			{19, 20, 7},
			{20, 21, 22},
			{22, 23, 9},
			{23, 24, 10},
			{25, 15, 26},
			{26, 12, 27},
			{27, 24, 28},
			{28, 21, 29},
			{29, 18, 25},
	};

	// indices for vertices of each edge (30 edges total)
	private static final int[][] EDGE_VERTS = {{0, 1}, {1, 2}, {2, 0}, {2, 3}, {3, 0}, {3, 4}, {4, 0}, {4, 5}, {5, 0},
			{5, 1}, {1, 9}, {9, 2}, {9, 10}, {10, 2}, {10, 3}, {10, 6}, {6, 3}, {6, 4}, {6, 7}, {7, 4}, {7, 5}, {7, 8},
			{8, 5}, {8, 1}, {8, 9}, {11, 6}, {11, 10}, {9, 11}, {8, 11}, {7, 11}};

	private float[][] verts;

	/** Returns indices for edges in each triangle */
	public int[][] getTriangleEdges()
	{
		return TRI_EDGES;
	}

	/** Returns indices for vertices in each triangle */
	public int[][] getTriangleVertices()
	{
		return TRI_VERTS;
	}

	/** Returns vertices of the icosahedron */
	public float[][] getVertices()
	{
		return verts;
	}

	/** Returns indices for vertices in each edge */
	public int[][] getEdgeVertices()
	{
		return EDGE_VERTS;
	}

	public Icosahedron(float side)
	{
		float hs = side / 2;
		float piOver5 = (float) (Math.PI / 5.0);
		float t2 = piOver5 / 2;
		float t4 = piOver5;
		float R = (float) ((side / 2) / Math.sin(t4));
		float H = (float) (Math.cos(t4) * R);
		float Cx = (float) (R * Math.cos(t2));
		float Cy = (float) (R * Math.sin(t2));
		float H1 = (float) (Math.sqrt(side * side - R * R));
		float H2 = (float) (Math.sqrt((H + R) * (H + R) - H * H));
		float Z2 = (H2 - H1) / 2;
		float Z1 = Z2 + H1;

		verts = new float[][] {
				{0, Z1, 0},
				{0, Z2, R},
				{Cx, Z2, Cy},
				{hs, Z2, -H},
				{-hs, Z2, -H},
				{-Cx, Z2, Cy},
				{0, -Z2, -R},
				{-Cx, -Z2, -Cy},
				{-hs, -Z2, H},
				{hs, -Z2, H},
				{Cx, -Z2, -Cy},
				{0, -Z1, 0},
		};
	}

	public void render(GL2 gl)
	{
		gl.glBegin(GL.GL_TRIANGLES);
		for (int[] triVert : TRI_VERTS) {
			gl.glVertex3fv(verts[triVert[0]], 0);
			gl.glVertex3fv(verts[triVert[1]], 0);
			gl.glVertex3fv(verts[triVert[2]], 0);
		}
		gl.glEnd();
	}

	public void renderFace(GL2 gl, int face, TextRenderer tr)
	{
		// render solid face
		gl.glBegin(GL.GL_TRIANGLES);
		float[] v0 = verts[TRI_VERTS[face][0]];
		float[] v1 = verts[TRI_VERTS[face][1]];
		float[] v2 = verts[TRI_VERTS[face][2]];
		gl.glVertex3fv(v0, 0);
		gl.glVertex3fv(v1, 0);
		gl.glVertex3fv(v2, 0);
		gl.glEnd();

		int[] triEdgeIndices = TRI_EDGES[face];

		gl.glColor3f(0, 0, 1);
		gl.glLineWidth(6);
		gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < 3; i++) {
			// if indices don't match, the edge's index order must be flipped
			float c = 1;
			if (TRI_VERTS[face][i] != EDGE_VERTS[triEdgeIndices[i]][0]) {
				c = 0;
			}

			gl.glColor3f(1 - c, 0, c);
			gl.glVertex3fv(verts[EDGE_VERTS[triEdgeIndices[i]][0]], 0);
			gl.glColor3f(c, 0, 1 - c);
			gl.glVertex3fv(verts[EDGE_VERTS[triEdgeIndices[i]][1]], 0);
		}
		gl.glEnd();
		gl.glLineWidth(1);

		tr.begin3DRendering();
		tr.draw3D("A", v0[0], v0[1], v0[2], 0.04f);
		tr.draw3D("B", v1[0], v1[1], v1[2], 0.04f);
		tr.draw3D("C", v2[0], v2[1], v2[2], 0.04f);
		tr.end3DRendering();
	}

	public void renderEdges(GL2 gl, TextRenderer tr)
	{
		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glBegin(GL.GL_LINES);
		for (int[] edgeVert : EDGE_VERTS) {
			gl.glVertex3fv(verts[edgeVert[0]], 0);
			gl.glVertex3fv(verts[edgeVert[1]], 0);
		}
		// gl.glVertex3fv(verts[triangles[face][0]], 0);
		// gl.glVertex3fv(verts[triangles[face][1]], 0);
		// gl.glVertex3fv(verts[triangles[face][2]], 0);
		gl.glEnd();
	}

	public void renderPts(TextRenderer tr)
	{
		tr.begin3DRendering();
		for (int i = 0; i < verts.length; i++)
			tr.draw3D("" + i, verts[i][0], verts[i][1], verts[i][2], 0.03f);
		tr.end3DRendering();
	}
}