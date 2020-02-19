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

import java.util.List;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;

/**
 * Miscellaneous math methods
 *
 * @author Justin Stoecker
 */
public class Maths
{
	private static final double PI3_OVER_2 = Math.PI * 1.5;

	/** Returns angle in radians between two vectors a and b in [0,pi] */
	public static double vecAngle(Vec3f a, Vec3f b)
	{
		Vec3f n = a.cross(b);
		double rads = Math.atan2(n.length(), a.dot(b));
		// if (n.dot(Vec3f.unitY()) > 0)
		// rads *= -1;
		return rads;
	}

	/**
	 * Calculates rotations (in radians) for absolute pitch (rotation x) and yaw
	 * (rotation y) for an object at position a to point toward b
	 */
	public static Vec2f calcPitchYaw(Vec3f a, Vec3f b)
	{
		// v1 = -z axis = (0,0,-1)
		// v2 = b-a = (b.x - a.x, 0, b.z - a.z)
		// rotation y = 2pi   - atan2(v2.z, v2.x) - atan2(v1.z, v1.x) =
		//            = 2pi   - atan2(b.z - a.z, b.x - a.x) - atan2(-1,0)
		//            = 2pi   - atan2(b.z - a.z, b.x - a.x) - pi/2
		//            = 3pi/2 - atan2(b.z - a.z, b.x - a.x)

		double yawRads = PI3_OVER_2 - Math.atan2(b.z - a.z, b.x - a.x);
		float yaw = (float) Math.toDegrees(yawRads);

		Vec3f v = b.minus(a);
		double pitchRads = Math.atan2(v.y, Math.sqrt(v.x * v.x + v.z * v.z));
		float pitch = (float) Math.toDegrees(pitchRads);

		return new Vec2f(pitch, yaw);
	}

	/**
	 * Clamps a double value to be within a specified range
	 */
	public static double clamp(double val, double min, double max)
	{
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}

	/**
	 * Clamps a float value to be within a specified range
	 */
	public static float clamp(float val, float min, float max)
	{
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}

	/**
	 * Clamps an integer value to be within a specified range
	 */
	public static int clamp(int val, int min, int max)
	{
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}

	/**
	 * Returns the point in a list that is nearest to another point
	 */
	public static Vec3f getNearest(Vec3f a, List<Vec3f> list)
	{
		float minD = 0;
		Vec3f nearest = null;
		for (Vec3f vec3f : list) {
			float d = vec3f.minus(a).lengthSquared();
			if (nearest == null || d < minD) {
				minD = d;
				nearest = vec3f;
			}
		}
		return nearest;
	}

	public static Vec2f rndVec2f(float min, float max)
	{
		float x = (float) Math.random() * (max - min) + min;
		float y = (float) Math.random() * (max - min) + min;
		return new Vec2f(x, y);
	}

	public static Vec3f rndVec3f(float min, float max)
	{
		float x = (float) Math.random() * (max - min) + min;
		float y = (float) Math.random() * (max - min) + min;
		float z = (float) Math.random() * (max - min) + min;
		return new Vec3f(x, y, z);
	}
}
