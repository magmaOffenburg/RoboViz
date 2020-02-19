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

import java.nio.FloatBuffer;
import jsgl.math.Tuplef;

/**
 * Vector with three single-precision components x, y, and z
 *
 * @author Justin Stoecker
 */
public class Vec3f implements Tuplef
{
	public float x, y, z;

	/**
	 * Creates a new Vec3f object and initializes its three components using the
	 * x, y, and z values
	 */
	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a new Vec3f object and initializes its three components using an
	 * array of three values
	 */
	public Vec3f(float[] v)
	{
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}

	/**
	 * Creates a new Vec3f object and initializes its three components using v
	 * for the first two components and z for the last component
	 */
	public Vec3f(Vec2f v, float z)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = z;
	}

	/**
	 * Creates a new Vec3f object and initializes its three components using v
	 * for each component
	 */
	public Vec3f(float v)
	{
		this.x = v;
		this.y = v;
		this.z = v;
	}

	/**
	 * Returns the result of the 3D dot product of this Vec3f and that Vec3f
	 */
	public float dot(Vec3f that)
	{
		return x * that.x + y * that.y + z * that.z;
	}

	/**
	 * Returns the result of the 3D cross product of this Vec3f and that Vec3f
	 */
	public Vec3f cross(Vec3f that)
	{
		float x = this.y * that.z - this.z * that.y;
		float y = this.z * that.x - this.x * that.z;
		float z = this.x * that.y - this.y * that.x;

		return new Vec3f(x, y, z);
	}

	/**
	 * Calculates the length of this Vec3f
	 */
	public float length()
	{
		return (float) Math.sqrt(dot(this));
	}

	/**
	 * Calculates the length squared of this Vec3f. This avoids an expensive
	 * square root operation.
	 */
	public float lengthSquared()
	{
		return dot(this);
	}

	/**
	 * Returns the result of this Vec3f normalized to unit length
	 */
	public Vec3f normalize()
	{
		return over(length());
	}

	/**
	 * Returns the result of subtracting that Vec3f from this Vec3f
	 */
	public Vec3f minus(Vec3f that)
	{
		return new Vec3f(x - that.x, y - that.y, z - that.z);
	}

	/**
	 * Returns the result of adding this Vec3f with that Vec3f
	 */
	public Vec3f plus(Vec3f that)
	{
		return new Vec3f(x + that.x, y + that.y, z + that.z);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec3f minus(float s)
	{
		return new Vec3f(x - s, y - s, z - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec3f plus(float s)
	{
		return new Vec3f(x + s, y + s, z + s);
	}

	/**
	 * Returns the result of multiplying this Vec3f with a scalar s
	 */
	public Vec3f times(float s)
	{
		return new Vec3f(x * s, y * s, z * s);
	}

	/**
	 * Returns the result of dividing this Vec3f by a scalar s
	 */
	public Vec3f over(float s)
	{
		return new Vec3f(x / s, y / s, z / s);
	}

	/**
	 * Reflects the current vector across a surface with normal n<br>
	 * Uses the equation: i - 2n(i dot n) where i = this vector
	 *
	 * @param n
	 *           - the normal vector
	 */
	public Vec3f reflect(Vec3f n)
	{
		return this.minus(n.times(this.dot(n)).times(2.0f));
	}

	/**
	 * Retrieves the component of index i
	 *
	 * @param i
	 *           - x = 0, y = 1, z = 2
	 */
	@Override
	public float get(int i) throws IndexOutOfBoundsException
	{
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		}
		throw new IndexOutOfBoundsException(String.format("Index %d is outside of range [0,2]", i));
	}

	/**
	 * Sets the component of index i to the value v. If the index is outside the
	 * range [0,2] this method has no effect.
	 */
	public void set(int i, float v)
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
	 * Returns a new Vec3f using the current vector's components rearranged.<br>
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
	public Vec3f swizzle(int x, int y, int z) throws IndexOutOfBoundsException
	{
		return new Vec3f(get(x), get(y), get(z));
	}

	/**
	 * Converts the Vec3f into an array of floats wrapped in a buffer
	 */
	public FloatBuffer wrap()
	{
		return FloatBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec3f into an array of floats
	 */
	@Override
	public float[] getVals()
	{
		return new float[] {x, y, z};
	}

	/**
	 * Returns a string representation of the Vec3f.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f, %.2f}", x, y, z);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec3f clone()
	{
		return new Vec3f(x, y, z);
	}

	/**
	 * Returns a Vec3f with unit length in the X direction
	 */
	public static Vec3f unitX()
	{
		return new Vec3f(1, 0, 0);
	}

	/**
	 * Returns a Vec3f with unit length in the Y direction
	 */
	public static Vec3f unitY()
	{
		return new Vec3f(0, 1, 0);
	}

	/**
	 * Returns a Vec3f with unit length in the Z direction
	 */
	public static Vec3f unitZ()
	{
		return new Vec3f(0, 0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec3f lerp(Vec3f a, Vec3f b, float s)
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
	public void add(Vec3f v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(float v)
	{
		x += v;
		y += v;
		z += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec3f v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(float v)
	{
		x -= v;
		y -= v;
		z -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec3f v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(float v)
	{
		x *= v;
		y *= v;
		z *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec3f v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
	}

	/**
	 * Divides each component of this by v
	 */
	public void div(float v)
	{
		x /= v;
		y /= v;
		z /= v;
	}
}