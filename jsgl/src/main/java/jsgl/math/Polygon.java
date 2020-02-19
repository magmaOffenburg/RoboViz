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

import jsgl.math.vector.Vec3f;

/**
 * A convex polygon
 *
 * @author Justin Stoecker
 */
public class Polygon
{
	public Vec3f[] v;
	public Vec3f n = new Vec3f(0);

	/**
	 * Creates a face out of of vertices. Order them counter-clockwise for the
	 * front / normal direction
	 */
	public Polygon(Vec3f[] verts)
	{
		this.v = verts;

		// Newell's method:
		// http://www.opengl.org/wiki/Calculating_a_Surface_Normal
		for (int i = 0; i < verts.length; i++) {
			Vec3f a = verts[i];
			Vec3f b = verts[(i + 1) % verts.length];
			n.x += (a.y - b.y) * (a.z + b.z);
			n.y += (a.z - b.z) * (a.x + b.x);
			n.z += (a.x - b.x) * (a.y + b.y);
		}
		n = n.normalize();
	}

	public Vec3f intersect(Ray r)
	{
		// intersect w/ plane of the polygon first
		Plane plane = new Plane(v[0], n);
		Vec3f x = plane.intersect(r);
		if (x == null)
			return null;

		// then if intersection it outside any of the edges
		for (int i = 0; i < v.length; i++) {
			int j = (i + 1) % v.length;
			if (v[j].minus(v[i]).cross(x.minus(v[i])).dot(n) < 0)
				return null;
		}

		return x;
	}
}
