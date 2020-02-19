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
 * Vector with four single-precision components x, y, z, and w
 *
 * @author Justin Stoecker
 */
public class Vec4f implements Tuplef
{
	public float x, y, z, w;

	/**
	 * Creates a new Vec4f object and initializes its four components using the
	 * x, y, z, and w values
	 */
	public Vec4f(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Creates a new Vec4f object and initializes its four components using an
	 * array of four values
	 */
	public Vec4f(float[] v)
	{
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
		this.w = v[3];
	}

	/**
	 * Creates a new Vec4f object and initializes its four components using v for
	 * the first three components and w for the fourth component
	 */
	public Vec4f(Vec3f v, float w)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.w = w;
	}

	/**
	 * Creates a new Vec4f object and initializes its four components using v for
	 * the first two components and z, w for the last two components
	 */
	public Vec4f(Vec2f v, float z, float w)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Creates a new Vec4f object and initializes its four components using v for
	 * each component
	 */
	public Vec4f(float v)
	{
		this.x = v;
		this.y = v;
		this.z = v;
		this.w = v;
	}

	/**
	 * Returns the result of the 4D dot product of this Vec4f and that Vec4f
	 */
	public float dot(Vec4f that)
	{
		return x * that.x + y * that.y + z * that.z + w * that.w;
	}

	/**
	 * Calculates the length of this Vec4f
	 */
	public float length()
	{
		return (float) Math.sqrt(dot(this));
	}

	/**
	 * Calculates the length squared of this Vec4f. This avoids an expensive
	 * square root operation.
	 */
	public float lengthSquared()
	{
		return dot(this);
	}

	/**
	 * Returns the result of this Vec4f normalized to unit length
	 */
	public Vec4f normalize()
	{
		return over(length());
	}

	/**
	 * Returns the result of subtracting that Vec4f from this Vec4f
	 */
	public Vec4f minus(Vec4f that)
	{
		return new Vec4f(x - that.x, y - that.y, z - that.z, w - that.w);
	}

	/**
	 * Returns the result of adding this Vec4f with that Vec4f
	 */
	public Vec4f plus(Vec4f that)
	{
		return new Vec4f(x + that.x, y + that.y, z + that.z, w + that.w);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec4f minus(float s)
	{
		return new Vec4f(x - s, y - s, z - s, w - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec4f plus(float s)
	{
		return new Vec4f(x + s, y + s, z + s, w + s);
	}

	/**
	 * Returns the result of multiplying this Vec4f with a scalar s
	 */
	public Vec4f times(float s)
	{
		return new Vec4f(x * s, y * s, z * s, w * s);
	}

	/**
	 * Returns the result of dividing this Vec4f by a scalar s
	 */
	public Vec4f over(float s)
	{
		return new Vec4f(x / s, y / s, z / s, w / s);
	}

	/**
	 * Retrieves the component of index i
	 *
	 * @param i
	 *           - x = 0, y = 1, z = 2, w = 3
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
		case 3:
			return w;
		};
		throw new IndexOutOfBoundsException(String.format("Index %d is outside of range [0,3]", i));
	}

	/**
	 * Sets the component of index i to the value v. If the index is outside the
	 * range [0,3] this method has no effect.
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
		case 3:
			w = v;
			break;
		}
	}

	/**
	 * Returns a new Vec4f using the current vector's components rearranged.<br>
	 * Ex: v.swizzle(1,0,2,0) is equivalent to v.yxzx<br>
	 * Ex: v.swizzle(2,1,0,3) is equivalent to v.zyxw
	 *
	 * @param x
	 *           - index of existing component for new first component
	 * @param y
	 *           - index of existing component for new second component
	 * @param z
	 *           - index of existing component for new third component
	 * @param w
	 *           - index of existing component for new fourth component
	 */
	public Vec4f swizzle(int x, int y, int z, int w) throws IndexOutOfBoundsException
	{
		return new Vec4f(get(x), get(y), get(z), get(w));
	}

	/**
	 * Converts the Vec4f into an array of floats wrapped in a buffer
	 */
	public FloatBuffer wrap()
	{
		return FloatBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec4f into an array of doubles
	 */
	@Override
	public float[] getVals()
	{
		return new float[] {x, y, z, w};
	}

	/**
	 * Returns a string representation of the Vec4f.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f, %.2f, %.2f}", x, y, z, w);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec4f clone()
	{
		return new Vec4f(x, y, z, w);
	}

	/**
	 * Returns a Vec4f with unit length in the X direction
	 */
	public static Vec4f unitX()
	{
		return new Vec4f(1, 0, 0, 0);
	}

	/**
	 * Returns a Vec4f with unit length in the Y direction
	 */
	public static Vec4f unitY()
	{
		return new Vec4f(0, 1, 0, 0);
	}

	/**
	 * Returns a Vec4f with unit length in the Z direction
	 */
	public static Vec4f unitZ()
	{
		return new Vec4f(0, 0, 1, 0);
	}

	/**
	 * Returns a Vec4f with unit length in the W direction
	 */
	public static Vec4f unitW()
	{
		return new Vec4f(0, 0, 0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec4f lerp(Vec4f a, Vec4f b, float s)
	{
		return a.plus(b.minus(a).times(s));
	}

	@Override
	public int getN()
	{
		return 4;
	}

	/**
	 * Adds components of v to this
	 */
	public void add(Vec4f v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		w += v.w;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(float v)
	{
		x += v;
		y += v;
		z += v;
		w += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec4f v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
		w -= v.w;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(float v)
	{
		x -= v;
		y -= v;
		z -= v;
		w -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec4f v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
		w *= v.w;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(float v)
	{
		x *= v;
		y *= v;
		z *= v;
		w *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec4f v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
		w /= v.w;
	}

	/**
	 * Divides each component of this by v
	 */
	public void div(float v)
	{
		x /= v;
		y /= v;
		z /= v;
		w /= v;
	}
}
