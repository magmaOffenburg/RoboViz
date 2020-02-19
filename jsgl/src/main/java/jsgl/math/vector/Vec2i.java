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

import java.nio.IntBuffer;
import jsgl.math.Tuplei;

/**
 * Vector with two integer components x and y
 *
 * @author Justin Stoecker
 */
public class Vec2i implements Tuplei
{
	public int x, y;

	/**
	 * Creates a new Vec2i object and initializes its two components using the x
	 * and y values
	 */
	public Vec2i(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new Vec2i object and initializes its two components using an
	 * array of two values
	 */
	public Vec2i(int[] c)
	{
		x = c[0];
		y = c[1];
	}

	/**
	 * Creates a new Vec2i object and initializes its two components using v for
	 * each component
	 */
	public Vec2i(int v)
	{
		x = v;
		y = v;
	}

	/** Returns the 2D dot product of this Vec2i and that Vec2i */
	public int dot(Vec2i that)
	{
		return x * that.x + y * that.y;
	}

	/** Returns the 2D cross product of this Vec2i and that Vec2i */
	public int cross(Vec2i that)
	{
		return x * that.y - y * that.x;
	}

	/** Calculates the length of this Vec2i */
	public int length()
	{
		return (int) Math.sqrt(dot(this));
	}

	/** Calculates the length squared of this Vec2i */
	public int lengthSquared()
	{
		return dot(this);
	}

	/** Returns the result of this Vec2i normalized to unit length */
	public Vec2i normalize()
	{
		return over(length());
	}

	/** Returns the result of subtracting that Vec2i from this Vec2i */
	public Vec2i minus(Vec2i that)
	{
		return new Vec2i(x - that.x, y - that.y);
	}

	/** Returns the result of adding this Vec2i with that Vec2i */
	public Vec2i plus(Vec2i that)
	{
		return new Vec2i(x + that.x, y + that.y);
	}

	/**
	 * Returns the result of subtracting a scalar from each component of this
	 * vector
	 */
	public Vec2i minus(int s)
	{
		return new Vec2i(x - s, y - s);
	}

	/**
	 * Returns the result of adding a scalar to each component of this vector
	 */
	public Vec2i plus(int s)
	{
		return new Vec2i(x + s, y + s);
	}

	/**
	 * Returns the result of multiplying this Vec2i by a scalar s
	 */
	public Vec2i times(int s)
	{
		return new Vec2i(x * s, y * s);
	}

	/**
	 * Returns the result of dividing this Vec2i by a scalar s
	 */
	public Vec2i over(int s)
	{
		return new Vec2i(x / s, y / s);
	}

	/**
	 * Returns the result of rotating this Vec2i by 90 degrees clockwise
	 */
	public Vec2i rot90()
	{
		return new Vec2i(-y, x);
	}

	/**
	 * Reflects the current vector across a surface with normal n<br>
	 * Uses the equation: i - 2n(i dot n) where i = this vector
	 *
	 * @param n
	 *           - the normal vector
	 */
	public Vec2i reflect(Vec2i n)
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
	public int get(int i) throws IndexOutOfBoundsException
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
	public void set(int i, int v)
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
	 * Returns a new Vec2i using the current vector's components rearranged.<br>
	 * Ex: v.swizzle(1,0) is equivalent to v.yx<br>
	 * Ex: v.swizzle(1,1) is equivalent to v.yy
	 *
	 * @param x
	 *           - index of existing component for new first component
	 * @param y
	 *           - index of existing component for new second component
	 */
	public Vec2i swizzle(int x, int y) throws IndexOutOfBoundsException
	{
		return new Vec2i(get(x), get(y));
	}

	/**
	 * Converts the Vec2i into an array of ints wrapped in a buffer
	 */
	public IntBuffer wrap()
	{
		return IntBuffer.wrap(getVals());
	}

	/**
	 * Converts the Vec2i into an array of doubles
	 */
	@Override
	public int[] getVals()
	{
		return new int[] {x, y};
	}

	/**
	 * Returns a string representation of the Vec2i.
	 */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f}", x, y);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec2i clone()
	{
		return new Vec2i(x, y);
	}

	/**
	 * Returns a Vec2i with unit length in the X direction
	 */
	public static Vec2i unitX()
	{
		return new Vec2i(1, 0);
	}

	/**
	 * Returns a Vec2i with unit length in the Y direction
	 */
	public static Vec2i unitY()
	{
		return new Vec2i(0, 1);
	}

	/**
	 * Linear interpolation between two vectors
	 */
	public static Vec2i lerp(Vec2i a, Vec2i b, int s)
	{
		return a.plus(b.minus(a).times(s));
	}

	@Override
	public int getN()
	{
		return 2;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec2i v)
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