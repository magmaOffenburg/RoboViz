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

package jsgl.math.vector;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import jsgl.math.Tupled;

/**
 * Vector with three double-precision components x, y, and z
 *
 * @author Justin Stoecker
 */
public class Vec3d implements Tupled
{
	public double x, y, z;

	/**
	 * Creates a new Vec3d object and initializes its three components using the
	 * x, y, and z values
	 */
	public Vec3d(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a new Vec3d object and initializes its three components using an
	 * array of three values
	 */
	public Vec3d(double[] v)
	{
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}

	/**
	 * Creates a new Vec3d object and initializes its three components using v
	 * for the first two components and z for the last component
	 */
	public Vec3d(Vec2d v, double z)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = z;
	}

	/**
	 * Creates a new Vec3d object and initializes its three components using v
	 * for each component
	 */
	public Vec3d(double v)
	{
		this.x = v;
		this.y = v;
		this.z = v;
	}

	/**
	 * Returns the result of the 3D dot product of this Vec3 and that Vec3
	 */
	public double dot(Vec3d that)
	{
		return x * that.x + y * that.y + z * that.z;
	}

	/**
	 * Returns the result of the 3D cross product of this Vec3 and that Vec3
	 */
	public Vec3d cross(Vec3d that)
	{
		double x = this.y * that.z - this.z * that.y;
		double y = this.z * that.x - this.x * that.z;
		double z = this.x * that.y - this.y * that.x;

		return new Vec3d(x, y, z);
	}

	/**
	 * Calculates the length of this Vec3
	 */
	public double length()
	{
		return Math.sqrt(dot(this));
	}

	/**
	 * Calculates the length squared of this Vec3. This avoids an expensive
	 * square root operation.
	 */
	public double lengthSquared()
	{
		return dot(this);
	}

	/**
	 * Returns the result of this Vec3 normalized to unit length
	 */
	public Vec3d normalize()
	{
		return over(length());
	}

	/**
	 * Returns the result of subtracting that Vec3 from this Vec3
	 */
	public Vec3d minus(Vec3d that)
	{
		return new Vec3d(x - that.x, y - that.y, z - that.z);
	}

	/**
	 * Returns the result of adding this Vec3 with that Vec3
	 */
	public Vec3d plus(Vec3d that)
	{
		return new Vec3d(x + that.x, y + that.y, z + that.z);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec3d minus(double s)
	{
		return new Vec3d(x - s, y - s, z - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec3d plus(double s)
	{
		return new Vec3d(x + s, y + s, z + s);
	}

	/**
	 * Returns the result of multiplying this Vec3 with a scalar s
	 */
	public Vec3d times(double s)
	{
		return new Vec3d(x * s, y * s, z * s);
	}

	/**
	 * Returns the result of dividing this Vec3 by a scalar s
	 */
	public Vec3d over(double s)
	{
		return new Vec3d(x / s, y / s, z / s);
	}

	/**
	 * Reflects the current vector across a surface with normal n<br>
	 * Uses the equation: i - 2n(i dot n) where i = this vector
	 *
	 * @param n
	 *           - the normal vector
	 */
	public Vec3d reflect(Vec3d n)
	{
		return this.minus(n.times(this.dot(n)).times(2));
	}

	/**
	 * Retrieves the component of index i
	 *
	 * @param i
	 *           - x = 0, y = 1, z = 2
	 */
	@Override
	public double get(int i) throws IndexOutOfBoundsException
	{
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		};
		throw new IndexOutOfBoundsException(String.format("Index %d is outside of range [0,2]", i));
	}

	/**
	 * Sets the component of index i to the value v. If the index is outside the
	 * range [0,2] this method has no effect.
	 */
	public void set(int i, double v)
	{
		switch (i) {
		case 0:
			x = v;
			break;
		case 1:
			y = v;
			break;
		case 2:
			z = v;
			break;
		}
	}

	/**
	 * Returns a new Vec3d using the current vector's components rearranged.<br>
	 * Ex: v.swizzle(1,0,2) is equivalent to v.yxz<br>
	 * Ex: v.swizzle(2,1,0) is equivalent to v.zyx
	 *
	 * @param x
	 *           - index of existing component for new first component
	 * @param y
	 *           - index of existing component for new second component
	 * @param z
	 *           - index of existing component for new third component
	 */
	public Vec3d swizzle(int x, int y, int z) throws IndexOutOfBoundsException
	{
		return new Vec3d(get(x), get(y), get(z));
	}

	/**
	 * Converts the Vec3 into an array of doubles wrapped in a buffer
	 */
	public DoubleBuffer wrap()
	{
		return DoubleBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec3 into an array of doubles
	 */
	@Override
	public double[] getVals()
	{
		return new double[] {x, y, z};
	}

	/**
	 * Converts the Vec3 into an array of floats wrapped in a buffer
	 */
	public FloatBuffer wrapf()
	{
		return toVec3f().wrap();
	}

	/**
	 * Converts the Vec3 into a Vec3f with single-precision values
	 */
	public Vec3f toVec3f()
	{
		return new Vec3f((float) x, (float) y, (float) z);
	}

	/**
	 * Returns a string representation of the Vec3d.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f, %.2f}", x, y, z);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec3d clone()
	{
		return new Vec3d(x, y, z);
	}

	/**
	 * Returns a Vec3 with unit length in the X direction
	 */
	public static Vec3d unitX()
	{
		return new Vec3d(1, 0, 0);
	}

	/**
	 * Returns a Vec3 with unit length in the Y direction
	 */
	public static Vec3d unitY()
	{
		return new Vec3d(0, 1, 0);
	}

	/**
	 * Returns a Vec3 with unit length in the Z direction
	 */
	public static Vec3d unitZ()
	{
		return new Vec3d(0, 0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec3d lerp(Vec3d a, Vec3d b, double s)
	{
		return a.plus(b.minus(a).times(s));
	}

	@Override
	public int getN()
	{
		return 3;
	}

	/**
	 * Adds components of v to this
	 */
	public void add(Vec3d v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(double v)
	{
		x += v;
		y += v;
		z += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec3d v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(double v)
	{
		x -= v;
		y -= v;
		z -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec3d v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(double v)
	{
		x *= v;
		y *= v;
		z *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec3d v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
	}

	/**
	 * Divides each component of this by v
	 */
	public void div(double v)
	{
		x /= v;
		y /= v;
		z /= v;
	}
}
