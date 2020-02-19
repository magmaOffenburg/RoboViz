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
 * Triangle class
 *
 * @author Justin Stoecker
 */
public class Triangle
{
	public final Vec3f[] v = new Vec3f[3];
	public final Vec3f n;

	/**
	 * A triangle defined by three points arranged counter-clockwise for the
	 * front face
	 */
	public Triangle(Vec3f[] v)
	{
		for (int i = 0; i < 3; i++)
			this.v[i] = v[i];
		n = v[2].minus(v[1]).cross(v[0].minus(v[1])).normalize();
	}

	/**
	 * A triangle defined by three points arranged counter-clockwise for the
	 * front face
	 */
	public Triangle(Vec3f a, Vec3f b, Vec3f c)
	{
		v[0] = a;
		v[1] = b;
		v[2] = c;
		n = v[2].minus(v[1]).cross(v[0].minus(v[1])).normalize();
	}

	public Vec3f intersect(Ray r)
	{
		float d = -v[0].dot(n);
		float div = r.getDirection().dot(n);
		if (div == 0)
			return null;
		float t = -(r.getPosition().dot(n) + d) / div;
		if (t < 0)
			return null;

		Vec3f x = r.getPosition().plus(r.getDirection().times(t));

		if (v[1].minus(v[0]).cross(x.minus(v[0])).dot(n) >= 0 && v[2].minus(v[1]).cross(x.minus(v[1])).dot(n) >= 0 &&
				v[0].minus(v[2]).cross(x.minus(v[2])).dot(n) >= 0)
			return x;

		return null;
	}
}