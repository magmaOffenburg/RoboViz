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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.io.Serializable;
import java.util.ArrayList;
import jsgl.math.vector.Vec3f;

/**
 * An axis-aligned bounding box defined by two points
 *
 * @author Justin Stoecker
 */
public class BoundingBox implements Serializable
{
	private Vec3f min;
	private Vec3f max;
	private Vec3f[] corners;
	private Line[] edges;

	/**
	 * Creates a new bounding box using min and max
	 *
	 * @param min
	 *            - corner of box closest to the origin
	 * @param max
	 *            - corner of box furthest from the origin
	 */
	public BoundingBox(Vec3f min, Vec3f max)
	{
		this.min = min;
		this.max = max;

		// corners
		Vec3f d = max.minus(min);
		Vec3f u = new Vec3f(0, d.y, 0);
		Vec3f v0 = min;
		Vec3f v1 = min.plus(new Vec3f(0, 0, d.z));
		Vec3f v2 = min.plus(new Vec3f(d.x, 0, d.z));
		Vec3f v3 = min.plus(new Vec3f(d.x, 0, 0));
		Vec3f v4 = v0.plus(u);
		Vec3f v5 = v1.plus(u);
		Vec3f v6 = v2.plus(u);
		Vec3f v7 = v3.plus(u);
		corners = new Vec3f[] {v0, v1, v2, v3, v4, v5, v6, v7};

		// edges
		edges = new Line[] {new Line(corners[0], corners[1]), new Line(corners[1], corners[2]),
				new Line(corners[2], corners[3]), new Line(corners[3], corners[0]), new Line(corners[4], corners[5]),
				new Line(corners[5], corners[6]), new Line(corners[6], corners[7]), new Line(corners[7], corners[4]),
				new Line(corners[0], corners[4]), new Line(corners[1], corners[5]), new Line(corners[2], corners[6]),
				new Line(corners[3], corners[7])};
	}

	public Vec3f getMin()
	{
		return min;
	}

	public Vec3f getMax()
	{
		return max;
	}

	public Line[] getEdges()
	{
		return edges;
	}

	/**
	 * Returns corner vertices of box in counter-clockwise order from bottom to
	 * top first
	 */
	public Vec3f[] getCorners()
	{
		return corners;
	}

	public Vec3f getDiag()
	{
		return max.minus(min);
	}

	public Vec3f getCenter()
	{
		return max.minus(min).over(2.0f).plus(min);
	}

	public boolean contains(Vec3f p)
	{
		for (int i = 0; i < 3; i++)
			if (p.get(i) < min.get(i) || p.get(i) > max.get(i))
				return false;
		return true;
	}

	/**
	 * Checks for intersection with another bounding box
	 *
	 * @param b
	 *            - a second bounding box
	 */
	public boolean intersects(BoundingBox b)
	{
		return (min.x < b.max.x) && (max.x > b.min.x) && (min.y < b.max.y) && (max.y > b.min.y) && (min.z < b.max.z) &&
				(max.z > b.min.z);
	}

	/**
	 * Checks for intersection with a triangle
	 *
	 * @see http://www.cs.lth.se/home/Tomas_Akenine_Moller/code/tribox3.txt
	 */
	public boolean intersects(Triangle t)
	{
		Vec3f boxHalfSize = max.minus(min).over(2.0f);
		Vec3f boxCenter = min.plus(boxHalfSize);

		// move everything so that the box center is in (0,0,0)
		Vec3f[] v = new Vec3f[3];
		for (int i = 0; i < 3; i++)
			v[i] = t.v[i].minus(boxCenter);

		// compute triangle edges
		Vec3f[] e = new Vec3f[3];
		for (int i = 0; i < 3; i++)
			e[i] = v[(i + 1) % 3].minus(v[i]);

		// bullet 3 : test the 9 tests firsts
		float fex, fey, fez;
		fex = Math.abs(e[0].x);
		fey = Math.abs(e[0].y);
		fez = Math.abs(e[0].z);
		if (!axisTestX01(e[0].z, e[0].y, fez, fey, v, boxHalfSize))
			return false;
		if (!axisTestY02(e[0].z, e[0].x, fez, fex, v, boxHalfSize))
			return false;
		if (!axisTestZ12(e[0].y, e[0].x, fey, fex, v, boxHalfSize))
			return false;

		fex = Math.abs(e[1].x);
		fey = Math.abs(e[1].y);
		fez = Math.abs(e[1].z);
		if (!axisTestX01(e[1].z, e[1].y, fez, fey, v, boxHalfSize))
			return false;
		if (!axisTestY02(e[1].z, e[1].x, fez, fex, v, boxHalfSize))
			return false;
		if (!axisTestZ0(e[1].y, e[1].x, fey, fex, v, boxHalfSize))
			return false;

		fex = Math.abs(e[2].x);
		fey = Math.abs(e[2].y);
		fez = Math.abs(e[2].z);
		if (!axisTestX2(e[2].z, e[2].y, fez, fey, v, boxHalfSize))
			return false;
		if (!axisTestY1(e[2].z, e[2].x, fez, fex, v, boxHalfSize))
			return false;
		if (!axisTestZ12(e[2].y, e[2].x, fey, fex, v, boxHalfSize))
			return false;

		// bullet 1 : first test overlap in {x,y,z} directions
		// find min, max of the triangle each direction, and test for overlap
		// in that direction -- this is equivalent to testing a minimal AABB
		// around the triangle against the AABB
		float[] minMax = new float[2];

		// test in x-direction
		findMinMax(v[0].x, v[1].x, v[2].x, minMax);
		if (minMax[0] > boxHalfSize.x || minMax[1] < -boxHalfSize.x)
			return false;

		// test in y-direction
		findMinMax(v[0].y, v[1].y, v[2].y, minMax);
		if (minMax[0] > boxHalfSize.y || minMax[1] < -boxHalfSize.y)
			return false;

		// test in z-direction
		findMinMax(v[0].z, v[1].z, v[2].z, minMax);
		if (minMax[0] > boxHalfSize.z || minMax[1] < -boxHalfSize.z)
			return false;

		// bullet 2 : test if the box intersects the plane of the triangle
		// compute plane equation of triangle: normal * x + d = 0
		Vec3f normal = e[0].cross(e[1]);
		if (!planeBoxOverlap(normal, v[0], boxHalfSize))
			return false;

		return true;
	}

	private boolean planeBoxOverlap(Vec3f n, Vec3f vert, Vec3f maxBox)
	{
		Vec3f vMin = new Vec3f(0);
		Vec3f vMax = new Vec3f(0);

		float v;
		for (int i = 0; i <= 2; i++) {
			v = vert.get(i);
			if (n.get(i) > 0.0f) {
				vMin.set(i, -maxBox.get(i) - v);
				vMax.set(i, maxBox.get(i) - v);
			} else {
				vMin.set(i, maxBox.get(i) - v);
				vMax.set(i, -maxBox.get(i) - v);
			}
		}

		if (n.dot(vMin) > 0.0f)
			return false;
		if (n.dot(vMax) >= 0.0f)
			return true;

		return false;
	}

	private void findMinMax(float x0, float x1, float x2, float[] minMax)
	{
		minMax[0] = minMax[1] = x0;
		if (x1 < minMax[0])
			minMax[0] = x1;
		if (x1 > minMax[1])
			minMax[1] = x1;
		if (x2 < minMax[0])
			minMax[0] = x2;
		if (x2 > minMax[1])
			minMax[1] = x2;
	}

	private boolean axisTestX01(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p0 = a * v[0].y - b * v[0].z;
		float p2 = a * v[2].y - b * v[2].z;

		if (p0 < p2) {
			min = p0;
			max = p2;
		} else {
			min = p2;
			max = p0;
		}
		float rad = fa * boxHalfSize.y + fb * boxHalfSize.z;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	private boolean axisTestX2(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p0 = a * v[0].y - b * v[0].z;
		float p1 = a * v[1].y - b * v[1].z;

		if (p0 < p1) {
			min = p0;
			max = p1;
		} else {
			min = p1;
			max = p0;
		}
		float rad = fa * boxHalfSize.y + fb * boxHalfSize.z;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	private boolean axisTestY02(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p0 = -a * v[0].x + b * v[0].z;
		float p2 = -a * v[2].x + b * v[2].z;

		if (p0 < p2) {
			min = p0;
			max = p2;
		} else {
			min = p2;
			max = p0;
		}
		float rad = fa * boxHalfSize.x + fb * boxHalfSize.z;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	private boolean axisTestY1(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p0 = -a * v[0].x + b * v[0].z;
		float p1 = -a * v[1].x + b * v[1].z;

		if (p0 < p1) {
			min = p0;
			max = p1;
		} else {
			min = p1;
			max = p0;
		}
		float rad = fa * boxHalfSize.x + fb * boxHalfSize.z;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	private boolean axisTestZ12(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p1 = a * v[1].x - b * v[1].y;
		float p2 = a * v[2].x - b * v[2].y;

		if (p2 < p1) {
			min = p2;
			max = p1;
		} else {
			min = p1;
			max = p2;
		}
		float rad = fa * boxHalfSize.x + fb * boxHalfSize.y;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	private boolean axisTestZ0(float a, float b, float fa, float fb, Vec3f[] v, Vec3f boxHalfSize)
	{
		float min, max;
		float p0 = a * v[0].x - b * v[0].y;
		float p1 = a * v[1].x - b * v[1].y;

		if (p0 < p1) {
			min = p0;
			max = p1;
		} else {
			min = p1;
			max = p0;
		}
		float rad = fa * boxHalfSize.x + fb * boxHalfSize.y;
		if (min > rad || max < -rad)
			return false;
		return true;
	}

	/**
	 * Returns intersection points of a ray with the bounding box
	 */
	public Vec3f[] intersect(Ray r)
	{
		int[] faces = new int[] {
				3,
				2,
				1,
				0,
				5,
				1,
				2,
				6,
				6,
				2,
				3,
				7,
				7,
				3,
				0,
				4,
				4,
				0,
				1,
				5,
				4,
				5,
				6,
				7,
		};

		ArrayList<Vec3f> intersections = new ArrayList<>();

		Vec3f[] corners = getCorners();

		for (int i = 0; i < 6; i++) {
			Vec3f[] v = new Vec3f[4];
			for (int j = 0; j < 4; j++)
				v[j] = corners[faces[i * 4 + j]];

			// intersect face plane
			Vec3f n = v[2].minus(v[1]).cross(v[0].minus(v[1])).normalize();
			float d = -v[0].dot(n);
			float div = r.getDirection().dot(n);
			if (div == 0)
				continue;
			float t = -(r.getPosition().dot(n) + d) / div;
			if (t < 0)
				continue;
			Vec3f x = r.getPosition().plus(r.getDirection().times(t));

			boolean good = true;
			for (int j = 0; j < 4; j++) {
				if (v[(j + 1) % 4].minus(v[j]).cross(x.minus(v[j])).dot(n) < 0) {
					good = false;
					break;
				}
			}

			if (x != null && good)
				intersections.add(x);
		}

		if (intersections.size() == 0)
			return null;
		return intersections.toArray(new Vec3f[0]);
	}

	public void render(GL2 gl)
	{
		Vec3f d = getDiag();
		Vec3f u = new Vec3f(0, d.y, 0);

		Vec3f v0 = min;
		Vec3f v1 = min.plus(new Vec3f(d.x, 0, 0));
		Vec3f v2 = min.plus(new Vec3f(d.x, 0, d.z));
		Vec3f v3 = min.plus(new Vec3f(0, 0, d.z));
		Vec3f v4 = v0.plus(u);
		Vec3f v5 = v1.plus(u);
		Vec3f v6 = v2.plus(u);
		Vec3f v7 = v3.plus(u);

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3fv(v0.getVals(), 0);
		gl.glVertex3fv(v1.getVals(), 0);
		gl.glVertex3fv(v2.getVals(), 0);
		gl.glVertex3fv(v3.getVals(), 0);
		gl.glEnd();

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3fv(v4.getVals(), 0);
		gl.glVertex3fv(v5.getVals(), 0);
		gl.glVertex3fv(v6.getVals(), 0);
		gl.glVertex3fv(v7.getVals(), 0);
		gl.glEnd();

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3fv(v0.getVals(), 0);
		gl.glVertex3fv(v4.getVals(), 0);
		gl.glVertex3fv(v1.getVals(), 0);
		gl.glVertex3fv(v5.getVals(), 0);
		gl.glVertex3fv(v2.getVals(), 0);
		gl.glVertex3fv(v6.getVals(), 0);
		gl.glVertex3fv(v3.getVals(), 0);
		gl.glVertex3fv(v7.getVals(), 0);
		gl.glEnd();
	}
}
