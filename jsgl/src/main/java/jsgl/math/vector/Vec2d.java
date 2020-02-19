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
import jsgl.math.Tupled;

/**
 * Vector / point with two double-precision components x and y.
 *
 * @author Justin Stoecker
 */
public class Vec2d implements Tupled
{
	public double x, y;

	/** Retrieves the component at index i */
	@Override
	public double get(int i)
	{
		if (i == 0)
			return x;
		if (i == 1)
			return y;
		throw new IndexOutOfBoundsException();
	}

	/** Sets the component of index i to the value v */
	public void set(int i, double v)
	{
		if (i == 0) {
			x = v;
			return;
		}
		if (i == 1) {
			y = v;
			return;
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Creates a new Vec2f object and initializes its two components using the x
	 * and y values
	 */
	public Vec2d(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new Vec2d object and initializes its two components using an
	 * array of two values
	 */
	public Vec2d(double[] c)
	{
		this.x = c[0];
		this.y = c[1];
	}

	/**
	 * Creates a new Vec2d object and initializes its two components using v for
	 * each component
	 */
	public Vec2d(double v)
	{
		x = v;
		y = v;
	}

	/** Returns the result of the 2D dot product of this Vec2 and that Vec2 */
	public double dot(Vec2d v)
	{
		return x * v.x + y * v.y;
	}

	/** Returns the result of the 2D cross product of this Vec2 and that Vec2 */
	public double cross(Vec2d v)
	{
		return x * v.y - y * v.x;
	}

	/** Calculates the length of this Vec2 */
	public double length()
	{
		return Math.sqrt(dot(this));
	}

	/** Calculates the length squared of this Vec2. */
	public double lengthSquared()
	{
		return dot(this);
	}

	/** Returns the result of this Vec2 normalized to unit length */
	public Vec2d normalize()
	{
		return over(length());
	}

	/** Returns the result of subtracting that Vec2 from this Vec2 */
	public Vec2d minus(Vec2d v)
	{
		return new Vec2d(x - v.x, y - v.y);
	}

	/** Returns the result of subtracting a scalar from each component */
	public Vec2d minus(double s)
	{
		return new Vec2d(x - s, y - s);
	}

	/** Returns the result of adding this Vec2 with that Vec2 */
	public Vec2d plus(Vec2d v)
	{
		return new Vec2d(x + v.x, y + v.y);
	}

	/** Returns the result of adding a scalar to each component */
	public Vec2d plus(double s)
	{
		return new Vec2d(x + s, y + s);
	}

	/** Returns the result of multiplying this Vec2 by a scalar s */
	public Vec2d times(double s)
	{
		return new Vec2d(x * s, y * s);
	}

	/** Returns the result of dividing this Vec2 by a scalar s */
	public Vec2d over(double s)
	{
		return new Vec2d(x / s, y / s);
	}

	/** Returns the result of rotating this Vec2 90 degrees CCW */
	public Vec2d rot90()
	{
		return new Vec2d(-y, x);
	}

	/** Calculates the distance between two vectors */
	public double distance(Vec2d v)
	{
		return minus(v).length();
	}

	/** Calculates the distance squared between two vectors */
	public double distanceSquared(Vec2d v)
	{
		return minus(v).lengthSquared();
	}

	/** Reflects the current vector across a surface with normal n */
	public Vec2d reflect(Vec2d n)
	{
		return minus(n.times(dot(n)).times(2));
	}

	/**
	 * Returns a new Vec2d using the current vector's components rearranged.<br>
	 * v.swizzle(1,0) is equivalent to v.yx<br>
	 * v.swizzle(1,1) is equivalent to v.yy
	 */
	public Vec2d swizzle(int x, int y) throws IndexOutOfBoundsException
	{
		return new Vec2d(get(x), get(y));
	}

	/** Converts the Vec2 into an array of doubles wrapped in a buffer */
	public DoubleBuffer wrap()
	{
		return DoubleBuffer.wrap(getVals());
	}

	/** Converts the Vec2 into an array of doubles */
	@Override
	public double[] getVals()
	{
		return new double[] {x, y};
	}

	/** Converts the Vec2d into a Vec2f with single-precision values */
	public Vec2f toVec2f()
	{
		return new Vec2f((float) x, (float) y);
	}

	/** Returns a string representation of the Vec2d */
	@Override
	public String toString()
	{
		return String.format("{%.2f, %.2f}", x, y);
	}

	/** Creates a copy of this vector */
	@Override
	public Vec2d clone()
	{
		return new Vec2d(x, y);
	}

	/** Returns a Vec2d with unit length in the X direction */
	public static Vec2d unitX()
	{
		return new Vec2d(1, 0);
	}

	/** Returns a Vec2d with unit length in the Y direction */
	public static Vec2d unitY()
	{
		return new Vec2d(0, 1);
	}

	/** Linear interpolation between two vectors */
	public static Vec2d lerp(Vec2d a, Vec2d b, double s)
	{
		return a.plus(b.minus(a).times(s));
	}

	/** Returns the number of components in the vector */
	@Override
	public int getN()
	{
		return 2;
	}

	/**
	 * Adds components of v to this
	 */
	public void add(Vec2d v)
	{
		x += v.x;
		y += v.y;
	}

	/**
	 * Adds v to each component of this
	 */
	public void add(double v)
	{
		x += v;
		y += v;
	}

	/**
	 * Subtracts components of v from this
	 */
	public void sub(Vec2d v)
	{
		x -= v.x;
		y -= v.y;
	}

	/**
	 * Subtracts v from each component of this
	 */
	public void sub(double v)
	{
		x -= v;
		y -= v;
	}

	/**
	 * Multiplies each component of this with the respective component from v
	 */
	public void mul(Vec2d v)
	{
		x *= v.x;
		y *= v.y;
	}

	/**
	 * Multiplies each component of this with v
	 */
	public void mul(double v)
	{
		x *= v;
		y *= v;
	}

	/**
	 * Divides each component of this by the respective component from v
	 */
	public void div(Vec2d v)
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
