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
 * Vector with four double-precision components x, y, z, and w
 *
 * @author Justin Stoecker
 */
public class Vec4d implements Tupled
{
	public double x, y, z, w;

	/**
	 * Creates a new Vec4d object and initializes its four components using the
	 * x, y, z, and w values
	 */
	public Vec4d(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Creates a new Vec4d object and initializes its four components using an
	 * array of four values
	 */
	public Vec4d(double[] v)
	{
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
		this.w = v[3];
	}

	/**
	 * Creates a new Vec4d object and initializes its four components using v for
	 * the first three components and w for the fourth component
	 */
	public Vec4d(Vec3d v, double w)
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
	public Vec4d(Vec2d v, double z, double w)
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
	public Vec4d(double v)
	{
		this.x = v;
		this.y = v;
		this.z = v;
		this.w = v;
	}

	/**
	 * Returns the result of the 4D dot product of this Vec4 and that Vec4
	 */
	public double dot(Vec4d that)
	{
		return x * that.x + y * that.y + z * that.z + w * that.w;
	}

	/**
	 * Calculates the length of this Vec4
	 */
	public double length()
	{
		return Math.sqrt(dot(this));
	}

	/**
	 * Calculates the length squared of this Vec4. This avoids an expensive
	 * square root operation.
	 */
	public double lengthSquared()
	{
		return dot(this);
	}

	/**
	 * Returns the result of this Vec4 normalized to unit length
	 */
	public Vec4d normalize()
	{
		return over(length());
	}

	/**
	 * Returns the result of subtracting that Vec4 from this Vec4
	 */
	public Vec4d minus(Vec4d that)
	{
		return new Vec4d(x - that.x, y - that.y, z - that.z, w - that.w);
	}

	/**
	 * Returns the result of adding this Vec4 with that Vec4
	 */
	public Vec4d plus(Vec4d that)
	{
		return new Vec4d(x + that.x, y + that.y, z + that.z, w + that.w);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec4d minus(double s)
	{
		return new Vec4d(x - s, y - s, z - s, w - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec4d plus(double s)
	{
		return new Vec4d(x + s, y + s, z + s, w + s);
	}

	/**
	 * Returns the result of multiplying this Vec4 with a scalar s
	 */
	public Vec4d times(double s)
	{
		return new Vec4d(x * s, y * s, z * s, w * s);
	}

	/**
	 * Returns the result of dividing this Vec4 by a scalar s
	 */
	public Vec4d over(double s)
	{
		return new Vec4d(x / s, y / s, z / s, w / s);
	}

	/**
	 * Retrieves the component of index i
	 *
	 * @param i
	 *           - x = 0, y = 1, z = 2, w = 3
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
		case 3:
			return w;
		};
		throw new IndexOutOfBoundsException(String.format("Index %d is outside of range [0,3]", i));
	}

	/**
	 * Sets the component of index i to the value v. If the index is outside the
	 * range [0,3] this method has no effect.
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
		case 3:
			w = v;
			break;
		}
	}

	/**
	 * Returns a new Vec4d using the current vector's components rearranged.<br>
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
	public Vec4d swizzle(int x, int y, int z, int w) throws IndexOutOfBoundsException
	{
		return new Vec4d(get(x), get(y), get(z), get(w));
	}

	/**
	 * Converts the Vec4 into an array of doubles wrapped in a buffer
	 */
	public DoubleBuffer wrap()
	{
		return DoubleBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec4 into an array of doubles
	 */
	@Override
	public double[] getVals()
	{
		return new double[] {x, y, z, w};
	}

	/**
	 * Converts the Vec4 into an array of floats wrapped in a buffer
	 */
	public FloatBuffer wrapf()
	{
		return toVec4f().wrap();
	}

	/**
	 * Converts the Vec4 into a Vec4f with single-precision values
	 */
	public Vec4f toVec4f()
	{
		return new Vec4f((float) x, (float) y, (float) z, (float) w);
	}

	/**
	 * Returns a string representation of the Vec4d.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f, %.2f, %.2f}", x, y, z, w);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec4d clone()
	{
		return new Vec4d(x, y, z, w);
	}

	/**
	 * Returns a Vec4 with unit length in the X direction
	 */
	public static Vec4d unitX()
	{
		return new Vec4d(1, 0, 0, 0);
	}

	/**
	 * Returns a Vec4 with unit length in the Y direction
	 */
	public static Vec4d unitY()
	{
		return new Vec4d(0, 1, 0, 0);
	}

	/**
	 * Returns a Vec4 with unit length in the Z direction
	 */
	public static Vec4d unitZ()
	{
		return new Vec4d(0, 0, 1, 0);
	}

	/**
	 * Returns a Vec4 with unit length in the W direction
	 */
	public static Vec4d unitW()
	{
		return new Vec4d(0, 0, 0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec4d lerp(Vec4d a, Vec4d b, double s)
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
	public void add(Vec4d v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		w += v.w;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(double v)
	{
		x += v;
		y += v;
		z += v;
		w += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec4d v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
		w -= v.w;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(double v)
	{
		x -= v;
		y -= v;
		z -= v;
		w -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec4d v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
		w *= v.w;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(double v)
	{
		x *= v;
		y *= v;
		z *= v;
		w *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec4d v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
		w /= v.w;
	}

	/**
	 * Divides each component of this by v
	 */
	public void div(double v)
	{
		x /= v;
		y /= v;
		z /= v;
		w /= v;
	}
}
