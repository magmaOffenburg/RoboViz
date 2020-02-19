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

import jsgl.math.vector.Vec2f;

/**
 * A circle on the XY plane
 *
 * @author Justin
 */
public class Circle2D
{
	private Vec2f center;
	private float radius;

	public Vec2f getCenter()
	{
		return center;
	}

	public float getRadius()
	{
		return radius;
	}

	/**
	 * Creates a circle from a central point and radius
	 */
	public Circle2D(Vec2f center, float radius)
	{
		this.center = center;
		this.radius = radius;
	}

	/**
	 * Creates a circle that goes through 3 points
	 */
	public static Circle2D createCircumcircle(Vec2f a, Vec2f b, Vec2f c)
	{
		// find intersection perpindicular bisectors
		Vec2f abM = a.plus(b).over(2);
		Vec2f bcM = b.plus(c).over(2);

		Vec2f cbU = b.minus(c).normalize();

		Vec2f abN = a.minus(b).rot90().normalize();

		float t = -cbU.dot(abM.minus(bcM)) / cbU.dot(abN);

		Vec2f center = abM.plus(abN.times(t));
		float radius = center.minus(a).length();

		return new Circle2D(center, radius);
	}

	/**
	 * Creates a circle that lies within the triangle made by 3 points
	 */
	public static Circle2D createInscribedCircle(Vec2f a, Vec2f b, Vec2f c)
	{
		return null;
	}
}
