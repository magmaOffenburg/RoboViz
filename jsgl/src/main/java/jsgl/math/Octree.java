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

package jsgl.math;

import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import jsgl.math.vector.Vec3f;

/**
 * Divides space up into axis-aligned bounding boxes that contain triangles.
 * Note that multiple nodes can contain a triangle, so each triangle is flagged
 * when it is rendered or used in a calculation. These flagged triangles are
 * stored in a hash set which is cleared before the octree is traversed.
 *
 * @author Justin Stoecker
 */
public class Octree
{
	private class Node
	{
		private BoundingBox bounds;
		private List<Triangle> triangles = null;
		private Node[] children = null;

		public Node(BoundingBox bounds, List<Triangle> triangles)
		{
			this.bounds = bounds;
			if (triangles.size() > MAX_NODE_SIZE)
				split(triangles);
			else
				this.triangles = triangles;
		}

		// divides the current node's box into 8 children nodes
		private void split(List<Triangle> triangles)
		{
			children = new Node[8];
			Vec3f toC = bounds.getDiag().over(2.0f);
			for (int i = 0; i < 8; i++) {
				// split the current node's box into the child's octant
				int j = (i % 4);
				Vec3f o = new Vec3f(j == 1 || j == 2 ? toC.x : 0, i > 3 ? toC.y : 0, j > 1 ? toC.z : 0);
				Vec3f cnMin = bounds.getMin().plus(o);
				BoundingBox cnBox = new BoundingBox(cnMin, cnMin.plus(toC));

				// check which triangles the smaller box contains
				List<Triangle> cnTriangles = checkTriangles(cnBox, triangles);

				// create the child node
				children[i] = new Node(cnBox, cnTriangles);
			}
		}

		// determines which triangles intersect a bounding box
		private List<Triangle> checkTriangles(BoundingBox box, List<Triangle> triangles)
		{
			List<Triangle> contained = new ArrayList<>();
			for (Triangle triangle : triangles)
				if (box.intersects(triangle))
					contained.add(triangle);
			return contained;
		}

		// returns the nearest ray/triangle intersection point
		public Vec3f intersect(Ray r)
		{
			// ignore nodes the ray doesn't intersect
			if (bounds.intersect(r) == null)
				return null;

			if (children == null) {
				// this is a leaf node, so check the triangles
				List<Vec3f> hits = new ArrayList<>();
				for (Triangle triangle : triangles) {
					// if the triangle is marked, ignore it; otherwise, mark it
					if (checked.contains(triangle))
						continue;
					checked.add(triangle);

					Vec3f x = triangle.intersect(r);
					if (x != null)
						hits.add(x);
				}

				return Maths.getNearest(r.getPosition(), hits);
			} else {
				// not a leaf, so check children
				List<Vec3f> hits = new ArrayList<>();
				for (int i = 0; i < 8; i++) {
					Vec3f x = children[i].intersect(r);
					if (x != null)
						hits.add(x);
				}
				return Maths.getNearest(r.getPosition(), hits);
			}
		}

		// recursively renders the current node and all its children
		public void render(GL2 gl)
		{
			bounds.render(gl);
			if (children != null) {
				for (Node child : children)
					child.render(gl);
			}
		}
	}

	private final int MAX_NODE_SIZE;
	private final Node root;
	private HashSet<Triangle> checked;

	public BoundingBox getBounds()
	{
		return root.bounds;
	}

	/**
	 * Creates an octree out of a set of triangles
	 *
	 * @param triangles
	 *           - the set of triangles used to divide the space
	 * @param maxNodeSize
	 *           - the number of triangles a node can contain before it is split;
	 *           must be greater than 0
	 */
	public Octree(List<Triangle> triangles, int maxNodeSize)
	{
		MAX_NODE_SIZE = maxNodeSize;
		checked = new HashSet<>();

		Vec3f min = new Vec3f(Float.MAX_VALUE);
		Vec3f max = new Vec3f(Float.MIN_VALUE);
		for (Triangle triangle : triangles) {
			for (int j = 0; j < 3; j++) {
				float x = triangle.v[j].x; // .getVertX(j);
				float y = triangle.v[j].y; // getVertY(j);
				float z = triangle.v[j].z; // getVertZ(j);

				if (x < min.x)
					min.x = x;
				else if (x > max.x)
					max.x = x;
				if (y < min.y)
					min.y = y;
				else if (y > max.y)
					max.y = y;
				if (z < min.z)
					min.z = z;
				else if (z > max.z)
					max.z = z;
			}
		}

		BoundingBox box = new BoundingBox(min, max);
		root = new Node(box, triangles);
	}

	/**
	 * Returns the closest ray-triangle intersection point, or null if there is
	 * no intersection
	 */
	public Vec3f intersect(Ray r)
	{
		checked.clear();
		Vec3f x = root.intersect(r);

		return x;
	}

	public void render(GL2 gl)
	{
		root.render(gl);
	}
}
