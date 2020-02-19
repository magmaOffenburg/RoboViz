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
 * Vector with two single-precision components x and y
 *
 * @author Justin Stoecker
 */
public class Vec2f implements Tuplef
{
	public float x, y;

	/**
	 * Creates a new Vec2f object and initializes its two components using the x
	 * and y values
	 */
	public Vec2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new Vec2f object and initializes its two components using an
	 * array of two values
	 */
	public Vec2f(float[] c)
	{
		x = c[0];
		y = c[1];
	}

	/**
	 * Creates a new Vec2f object and initializes its two components using v for
	 * each component
	 */
	public Vec2f(float v)
	{
		x = v;
		y = v;
	}

	/** Returns the 2D dot product of this Vec2f and that Vec2f */
	public float dot(Vec2f that)
	{
		return x * that.x + y * that.y;
	}

	/** Returns the 2D cross product of this Vec2f and that Vec2f */
	public float cross(Vec2f that)
	{
		return x * that.y - y * that.x;
	}

	/** Calculates the length of this Vec2f */
	public float length()
	{
		return (float) Math.sqrt(dot(this));
	}

	/** Calculates the length squared of this Vec2f */
	public float lengthSquared()
	{
		return dot(this);
	}

	/** Returns the result of this Vec2f normalized to unit length */
	public Vec2f normalize()
	{
		return over(length());
	}

	/** Returns the result of subtracting that Vec2f from this Vec2f */
	public Vec2f minus(Vec2f that)
	{
		return new Vec2f(x - that.x, y - that.y);
	}

	/** Returns the result of adding this Vec2f with that Vec2f */
	public Vec2f plus(Vec2f that)
	{
		return new Vec2f(x + that.x, y + that.y);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec2f minus(float s)
	{
		return new Vec2f(x - s, y - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec2f plus(float s)
	{
		return new Vec2f(x + s, y + s);
	}

	/**
	 * Returns the result of multiplying this Vec2f by a scalar s
	 */
	public Vec2f times(float s)
	{
		return new Vec2f(x * s, y * s);
	}

	/**
	 * Returns the result of dividing this Vec2f by a scalar s
	 */
	public Vec2f over(float s)
	{
		return new Vec2f(x / s, y / s);
	}

	/**
	 * Returns the result of rotating this Vec2f by 90 degrees clockwise
	 */
	public Vec2f rot90()
	{
		return new Vec2f(-y, x);
	}

	/**
	 * Reflects the current vector across a surface with normal n<br>
	 * Uses the equation: i - 2n(i dot n) where i = this vector
	 *
	 * @param n
	 *           - the normal vector
	 */
	public Vec2f reflect(Vec2f n)
	{
		return this.minus(n.times(this.dot(n)).times(2));
	}

	/**
	 * Retrieves the component of index i
	 *
	 * @param i
	 *           - x = 0, y = 1
	 */
	@Override
	public float get(int i) throws IndexOutOfBoundsException
	{
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		}
		throw new IndexOutOfBoundsException(String.format("Index %d is outside of range [0,1]", i));
	}

	/**
	 * Sets the component of index i to the value v. If the index is outside the
	 * range [0,1] this method has no effect.
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
		}
	}

	/**
	 * Returns a new Vec2f using the current vector's components rearranged.<br>
	 * Ex: v.swizzle(1,0) is equivalent to v.yx<br>
	 * Ex: v.swizzle(1,1) is equivalent to v.yy
	 *
	 * @param x
	 *           - index of existing component for new first component
	 * @param y
	 *           - index of existing component for new second component
	 */
	public Vec2f swizzle(int x, int y) throws IndexOutOfBoundsException
	{
		return new Vec2f(get(x), get(y));
	}

	/**
	 * Converts the Vec2f into an array of floats wrapped in a buffer
	 */
	public FloatBuffer wrap()
	{
		return FloatBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec2f into an array of doubles
	 */
	@Override
	public float[] getVals()
	{
		return new float[] {x, y};
	}

	/**
	 * Returns a string representation of the Vec2f.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f}", x, y);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec2f clone()
	{
		return new Vec2f(x, y);
	}

	/**
	 * Returns a Vec2f with unit length in the X direction
	 */
	public static Vec2f unitX()
	{
		return new Vec2f(1, 0);
	}

	/**
	 * Returns a Vec2f with unit length in the Y direction
	 */
	public static Vec2f unitY()
	{
		return new Vec2f(0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec2f lerp(Vec2f a, Vec2f b, float s)
	{
		return a.plus(b.minus(a).times(s));
	}

	@Override
	public int getN()
	{
		return 2;
	}

	/**
	 * Adds components of v to this
	 */
	public void add(Vec2f v)
	{
		x += v.x;
		y += v.y;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(float v)
	{
		x += v;
		y += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec2f v)
	{
		x -= v.x;
		y -= v.y;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(float v)
	{
		x -= v;
		y -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec2f v)
	{
		x *= v.x;
		y *= v.y;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(float v)
	{
		x *= v;
		y *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec2f v)
	{
		x /= v.x;
		y /= v.y;
	}

	/**
	 * Divides each component of this by v
	 */
	public void div(float v)
	{
		x /= v;
		y /= v;
	}
}